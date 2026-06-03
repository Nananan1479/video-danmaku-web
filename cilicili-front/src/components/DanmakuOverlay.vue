<script setup>
/**
 * DanmakuOverlay - Canvas 弹幕叠加层
 * 使用 Canvas 渲染滚动弹幕，支持拖回进度条时重新显示已滚过的弹幕。
 */
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    visible: {
        type: Boolean,
        default: true
    },
    /** 视频是否正在播放（暂停时弹幕停止滚动） */
    isPlaying: {
        type: Boolean,
        default: true
    },
    /** 视频当前播放时间（秒），用于按时间过滤弹幕 */
    currentTime: {
        type: Number,
        default: 0
    }
})

// ---- Canvas 引用 ----
const canvasRef = ref(null)

// ---- 弹幕数据 ----
/**
 * 每条弹幕的状态：
 * - pending: playTime 未到达，等待激活
 * - active: 正在滚动中
 * - finished: 已滚出屏幕左侧
 */
const danmakuItems = ref([])

// ---- 轨道池管理 ----
/** 最大轨道数 */
const MAX_LANES = 12
/** 轨道高度（px） */
const LANE_HEIGHT = 40
/** 当前可用的轨道编号池 */
let lanePool = []

/**
 * 初始化轨道池，将所有轨道标记为空闲
 */
function initLanePool() {
    lanePool = []
    for (let i = 0; i < MAX_LANES; i++) {
        lanePool.push(i)
    }
}

/**
 * 占用一个空闲轨道
 * @returns {number} 轨道索引
 */
function occupyLane() {
    if (lanePool.length === 0) {
        return Math.floor(Math.random() * MAX_LANES)
    }
    return lanePool.shift()
}

/**
 * 释放一个轨道，使其重新可用
 * @param {number} index 轨道索引
 */
function releaseLane(index) {
    if (!lanePool.includes(index)) {
        lanePool.push(index)
    }
}

// ---- 进度跳转检测 ----
/** 上一次的 currentTime，用于检测用户是否拖回进度条 */
let prevTime = 0

/**
 * 将指定 playTime 之后（含）的所有弹幕重置为 pending 状态，
 * 让它们在播放到对应时间时重新从右侧滚入。
 */
function resetDanmakuFromTime(time) {
    const items = danmakuItems.value
    for (let i = 0; i < items.length; i++) {
        const item = items[i]
        if (item.playTime >= time) {
            if (item.state === 'active' || item.state === 'finished') {
                // 释放轨道
                releaseLane(item.laneIndex)
            }
            item.state = 'pending'
            item.started = false
        }
    }
}

// currentTime 回退时重置弹幕
watch(() => props.currentTime, (newTime, oldTime) => {
    if (newTime < oldTime - 1) {
        // 用户拖回了进度条（>1秒的回退才算拖动）
        resetDanmakuFromTime(newTime)
    }
    prevTime = newTime
})

// ---- 渲染循环 ----
/** 上一次帧的时间戳 */
let lastTime = 0
/** 当前 Canvas 逻辑宽度（CSS 像素） */
let currentWidth = 0
/** 当前 Canvas 逻辑高度（CSS 像素） */
let currentHeight = 0
let animFrameId = null

/**
 * 同步 Canvas 实际像素尺寸（根据 devicePixelRatio 缩放防模糊）
 */
function syncCanvasPixelSize() {
    const canvas = canvasRef.value
    if (!canvas) return
    const dpr = window.devicePixelRatio || 1
    const w = canvas.clientWidth
    const h = canvas.clientHeight
    if (w === 0 || h === 0) return
    if (canvas.width !== w * dpr || canvas.height !== h * dpr) {
        canvas.width = w * dpr
        canvas.height = h * dpr
        const ctx = canvas.getContext('2d')
        if (ctx) {
            ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
        }
    }
}

/**
 * 主渲染循环，使用 requestAnimationFrame 持续重绘 Canvas
 * @param {number} timestamp 当前帧时间戳
 */
function renderLoop(timestamp) {
    if (!lastTime) lastTime = timestamp
    const delta = Math.min(timestamp - lastTime, 33)
    lastTime = timestamp

    const canvas = canvasRef.value
    if (!canvas) {
        animFrameId = requestAnimationFrame(renderLoop)
        return
    }

    // 每帧同步 Canvas 像素尺寸（支持窗口大小变化）
    syncCanvasPixelSize()

    currentWidth = canvas.clientWidth
    currentHeight = canvas.clientHeight

    if (currentWidth === 0 || currentHeight === 0) {
        animFrameId = requestAnimationFrame(renderLoop)
        return
    }

    const ctx = canvas.getContext('2d')
    if (!ctx) {
        animFrameId = requestAnimationFrame(renderLoop)
        return
    }

    // 清除画布
    ctx.clearRect(0, 0, currentWidth, currentHeight)

    // 滚动速度：像素/秒
    const scrollSpeed = 150
    const dx = (scrollSpeed * delta) / 1000

    const items = danmakuItems.value
    const ct = props.currentTime

    for (let i = 0; i < items.length; i++) {
        const item = items[i]

        // === 状态迁跃 ===

        // pending -> active: 播放时间到达 playTime
        if (item.state === 'pending' && ct >= item.playTime) {
            item.state = 'active'
            item.started = true
            item.laneIndex = occupyLane()
            item.x = currentWidth
        }

        // === 滚动位置更新 ===
        if (item.state === 'active' && item.started && item.mode === 1 && props.isPlaying) {
            item.x -= dx
        }

        // === 出屏检测 ===
        if (item.state === 'active' && item.mode === 1) {
            const textWidth = measureTextWidth(ctx, item)
            if (item.x + textWidth <= 0) {
                item.state = 'finished'
                releaseLane(item.laneIndex)
                continue // 不再绘制
            }
        }

        // === 绘制 ===
        if (!props.visible) continue
        if (item.state !== 'active') continue

        // 跳过右侧尚未进入视图的弹幕
        if (item.mode === 1 && item.x > currentWidth) continue

        ctx.save()
        ctx.font = `bold ${item.fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
        ctx.fillStyle = item.color
        ctx.textBaseline = 'top'

        ctx.strokeStyle = 'rgba(0,0,0,0.6)'
        ctx.lineWidth = 2
        ctx.lineJoin = 'round'
        ctx.strokeText(item.content, item.x, item.laneIndex * LANE_HEIGHT + 10)
        ctx.fillText(item.content, item.x, item.laneIndex * LANE_HEIGHT + 10)
        ctx.restore()
    }

    animFrameId = requestAnimationFrame(renderLoop)
}

/**
 * 测量弹幕文字的渲染宽度
 */
function measureTextWidth(ctx, item) {
    ctx.save()
    ctx.font = `bold ${item.fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
    const w = ctx.measureText(item.content).width
    ctx.restore()
    return w
}

// ---- 对外 API ----

/**
 * 添加一条弹幕（来自 WebSocket 实时推送）
 */
function addDanmaku(danmaku) {
    if (!props.visible) return
    // 如果当前时间已经过了 playTime，仍然立即显示
    const shouldStartNow = props.currentTime >= (danmaku.playTime ?? 0)
    const item = {
        id: danmaku.id || Date.now() + Math.random(),
        content: danmaku.content,
        color: danmaku.color || '#FFFFFF',
        fontSize: danmaku.fontSize || 16,
        mode: danmaku.mode || 1,
        playTime: danmaku.playTime ?? 0,
        state: 'pending',
        started: false,
        laneIndex: 0,
        x: currentWidth || 902
    }
    if (shouldStartNow) {
        item.state = 'active'
        item.started = true
        item.laneIndex = occupyLane()
        item.x = currentWidth || 902
    }
    danmakuItems.value.push(item)
}

/**
 * 批量加载历史弹幕
 */
function addDanmakus(list) {
    if (!list || !list.length) return
    initLanePool()
    // 重置所有状态
    danmakuItems.value = []
    prevTime = props.currentTime
    const ct = props.currentTime

    list.forEach((d, i) => {
        const playTime = d.playTime ?? 0
        const shouldStartNow = ct >= playTime
        const item = {
            id: d.id || Date.now() + Math.random() + i,
            content: d.content,
            color: d.color || '#FFFFFF',
            fontSize: d.fontSize || 16,
            mode: d.mode || 1,
            playTime,
            state: shouldStartNow ? 'active' : 'pending',
            started: shouldStartNow,
            laneIndex: shouldStartNow ? occupyLane() : 0,
            x: currentWidth || 902
        }
        danmakuItems.value.push(item)
    })
}

// 暴露方法给父组件调用
defineExpose({ addDanmaku, addDanmakus })

// ---- 生命周期 ----
onMounted(() => {
    initLanePool()
    prevTime = props.currentTime
    animFrameId = requestAnimationFrame(renderLoop)
})

onUnmounted(() => {
    if (animFrameId) {
        cancelAnimationFrame(animFrameId)
        animFrameId = null
    }
    initLanePool()
})
</script>

<template>
    <canvas
        v-show="visible"
        ref="canvasRef"
        class="danmaku-canvas"
    />
</template>

<style scoped>
.danmaku-canvas {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 10;
}
</style>
