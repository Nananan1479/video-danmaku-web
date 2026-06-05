<script setup>
/**
 * DanmakuOverlay - Canvas 弹幕叠加层
 * 弹幕生命期与视频时间轴完全同步：
 * - 激活时预计算 speed / textWidth / totalTime
 * - seek 时根据 elapsed 恢复弹幕到正确位置
 * - 弹幕永远不会被移除，只切换状态
 */
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    visible: { type: Boolean, default: true },
    /** 视频是否正在播放 */
    isPlaying: { type: Boolean, default: true },
    /** 视频当前播放时间（秒） */
    currentTime: { type: Number, default: 0 }
})

// ---- Canvas 引用 ----
const canvasRef = ref(null)

// ---- 弹幕配置 ----
/** 基础滚动速度：像素/秒 */
const SCROLL_SPEED = 150
/** 每像素文字宽度增加的速度（像素/秒），弹幕越长滚动越快 */
const SPEED_PER_PIXEL = 0.3
/** 文字透明度 0~1 */
const OPACITY = 1
/** 文字描边透明度 0~1 */
const STROKE_OPACITY = 0.6
/** 描边宽度（px） */
const STROKE_WIDTH = 2
/** 全屏时弹幕缩放倍数（≥1） */
const FULLSCREEN_SCALE = 1.5

// ---- 全屏检测 ----
const isFullscreen = ref(false)

// ---- 轨道池 ----
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
    for (let i = 0; i < MAX_LANES; i++) lanePool.push(i)
}

/**
 * 占用一个空闲轨道
 * @returns {number} 轨道索引
 */
function occupyLane() {
    return lanePool.length === 0 ? Math.floor(Math.random() * MAX_LANES) : lanePool.shift()
}

/**
 * 释放一个轨道，使其重新可用
 * @param {number} index 轨道索引
 */
function releaseLane(index) {
    if (!lanePool.includes(index)) lanePool.push(index)
}

// ---- 弹幕数据 ----
/**
 * 弹幕状态：
 *  - pending:  playTime 尚未到达
 *  - active:   正在飞行中
 *  - finished: 已完全移出屏幕左侧
 *
 * active 弹幕的核心属性：
 *  - playTime / speed / textWidth / totalTime / laneIndex / x
 */
const danmakuItems = ref([])

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
        if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    }
}

/**
 * 根据弹幕文本和当前 scale 计算 speed 与 textWidth（存入 item）
 */
function calcSpeedAndWidth(ctx, item, scale) {
    const tw = measureTextWidth(ctx, item, scale)
    item.textWidth = tw
    item.speed = SCROLL_SPEED + tw * SPEED_PER_PIXEL
    item.totalTime = (currentWidth + tw) / item.speed
}

function renderLoop(timestamp) {
    if (!lastTime) lastTime = timestamp
    const delta = Math.min(timestamp - lastTime, 33)
    lastTime = timestamp

    const canvas = canvasRef.value
    if (!canvas) { animFrameId = requestAnimationFrame(renderLoop); return }

    // 每帧同步 Canvas 像素尺寸（支持窗口大小变化）
    syncCanvasPixelSize()

    currentWidth = canvas.clientWidth
    currentHeight = canvas.clientHeight
    if (currentWidth === 0 || currentHeight === 0) { animFrameId = requestAnimationFrame(renderLoop); return }

    const ctx = canvas.getContext('2d')
    if (!ctx) { animFrameId = requestAnimationFrame(renderLoop); return }

    // 清除画布，计算缩放
    ctx.clearRect(0, 0, currentWidth, currentHeight)

    const scale = isFullscreen.value ? FULLSCREEN_SCALE : 1
    const scaledLaneH = LANE_HEIGHT * scale
    const scaledOffset = 10 * scale
    const ct = props.currentTime
    const items = danmakuItems.value

    for (let i = 0; i < items.length; i++) {
        const item = items[i]

        // ---- 状态迁跃：pending → active ----
        if (item.state === 'pending' && ct >= item.playTime) {
            item.state = 'active'
            item.laneIndex = occupyLane()
            item.x = currentWidth
            // 首次激活时预计算 speed 与 totalTime
            if (item.speed == null) calcSpeedAndWidth(ctx, item, scale)
        }

        // ---- 位置更新 ----
        if (item.state === 'active' && item.mode === 1 && props.isPlaying) {
            // 若首次激活后 speed 未计算（例如 pending 时已预置 speed），每帧重新计算适配全屏切换
            if (item.speed == null) calcSpeedAndWidth(ctx, item, scale)
            item.x -= item.speed * delta / 1000
        }

        // === 出屏检测 ===
        if (item.state === 'active' && item.mode === 1) {
            const tw = item.textWidth ?? measureTextWidth(ctx, item, scale)
            if (item.x + tw <= 0) {
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
        ctx.font = `bold ${item.fontSize * scale}px "Microsoft YaHei", "PingFang SC", sans-serif`
        ctx.globalAlpha = OPACITY
        ctx.fillStyle = item.color
        ctx.textBaseline = 'top'

        ctx.strokeStyle = `rgba(0, 0, 0, ${STROKE_OPACITY})`
        ctx.lineWidth = STROKE_WIDTH * scale
        ctx.lineJoin = 'round'
        ctx.strokeText(item.content, item.x, item.laneIndex * scaledLaneH + scaledOffset)
        ctx.fillText(item.content, item.x, item.laneIndex * scaledLaneH + scaledOffset)
        ctx.restore()
    }

    animFrameId = requestAnimationFrame(renderLoop)
}

/**
 * 测量弹幕文字的渲染宽度
 * @param {CanvasRenderingContext2D} ctx
 * @param {{content:string, fontSize:number}} item
 * @param {number} [scale=1] 全屏缩放倍数
 * @returns {number} 文字宽度
 */
function measureTextWidth(ctx, item, scale = 1) {
    ctx.save()
    ctx.font = `bold ${item.fontSize * scale}px "Microsoft YaHei", "PingFang SC", sans-serif`
    const w = ctx.measureText(item.content).width
    ctx.restore()
    return w
}

// ---- Seek 恢复：根据 video time 重算所有弹幕位置 ----
/**
 * 当用户拖拽进度条时调用。
 * 遍历所有弹幕，根据 elapsed = currentTime - playTime 计算其正确位置。
 * 若弹幕生命期覆盖当前时间，则激活并设置 x；否则重置为 pending / finished。
 */
function recoverPositions(time) {
    // 需要一个临时 ctx 来测量文字宽度
    const canvas = canvasRef.value
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const scale = isFullscreen.value ? FULLSCREEN_SCALE : 1
    const items = danmakuItems.value

    // 第一遍：释放所有当前活跃的轨道 → 全部回 lanePool
    for (let i = 0; i < items.length; i++) {
        if (items[i].state === 'active') {
            releaseLane(items[i].laneIndex)
        }
    }

    // 第二遍：重新分配状态和位置
    // 已占用的轨道集合，用于"保留原轨道"的冲突检测
    const takenLanes = new Set()

    for (let i = 0; i < items.length; i++) {
        const item = items[i]
        const elapsed = time - item.playTime

        if (elapsed < 0) {
            // 尚未发射
            item.state = 'pending'
            item.speed = null  // 清除旧 speed，激活时重新计算
            continue
        }

        // 计算（或重新计算）speed & totalTime
        calcSpeedAndWidth(ctx, item, scale)

        if (elapsed >= item.totalTime) {
            // 生命期已结束，轨道已在第一遍释放，无需再操作
            item.state = 'finished'
            continue
        }

        // 生命期中：激活并恢复正确位置
        item.state = 'active'
        const origLane = item.laneIndex

        // 优先尝试保留原轨道（若未被本轮的其它弹幕先占走）
        if (origLane != null && origLane < MAX_LANES && !takenLanes.has(origLane)) {
            takenLanes.add(origLane)
            item.laneIndex = origLane
            // 从轨道池移除，标记为已占用
            const idx = lanePool.indexOf(origLane)
            if (idx !== -1) lanePool.splice(idx, 1)
        } else {
            // 原轨道不可用，新分配一个
            item.laneIndex = occupyLane()
        }

        // 核心公式：x = screenWidth - 已飞行时间 × 速度
        item.x = currentWidth - elapsed * item.speed
    }
}

// 监听 currentTime 大幅度跳跃（>1秒）视为 seek
watch(() => props.currentTime, (newTime, oldTime) => {
    if (oldTime == null) return
    const delta = newTime - oldTime
    if (Math.abs(delta) > 1) {
        recoverPositions(newTime)
    }
})

// ---- 对外 API ----

/**
 * 添加一条弹幕（来自 WebSocket 实时推送）
 */
function addDanmaku(danmaku) {
    if (!props.visible) return
    const playTime = danmaku.playTime ?? 0
    const elapsed = props.currentTime - playTime

    const item = {
        id: danmaku.id || Date.now() + Math.random(),
        content: danmaku.content,
        color: danmaku.color || '#FFFFFF',
        fontSize: danmaku.fontSize || 16,
        mode: danmaku.mode || 1,
        playTime,
        state: 'pending',
        laneIndex: 0,
        x: currentWidth || 902,
        speed: null,
        textWidth: null,
        totalTime: null
    }

    // 如果当前时间已经到达或超过 playTime，立即激活
    if (elapsed >= 0) {
        const canvas = canvasRef.value
        if (canvas) {
            const ctx = canvas.getContext('2d')
            if (ctx) {
                const scale = isFullscreen.value ? FULLSCREEN_SCALE : 1
                calcSpeedAndWidth(ctx, item, scale)
                if (elapsed >= item.totalTime) {
                    item.state = 'finished'
                } else {
                    item.state = 'active'
                    item.laneIndex = occupyLane()
                    item.x = currentWidth - elapsed * item.speed
                }
            }
        }
        // 如果 canvas 不可用（初始挂载前），回退为 active 从右侧开始
        if (item.state === 'pending') {
            item.state = 'active'
            item.laneIndex = occupyLane()
            item.x = currentWidth || 902
        }
    }

    danmakuItems.value.push(item)
}

/**
 * 批量加载历史弹幕
 */
function addDanmakus(list) {
    if (!list || !list.length) return
    initLanePool()
    danmakuItems.value = []
    const ct = props.currentTime

    const canvas = canvasRef.value
    const ctx = canvas ? canvas.getContext('2d') : null
    const scale = isFullscreen.value ? FULLSCREEN_SCALE : 1

    list.forEach((d, i) => {
        const playTime = d.playTime ?? 0
        const elapsed = ct - playTime
        const item = {
            id: d.id || Date.now() + Math.random() + i,
            content: d.content,
            color: d.color || '#FFFFFF',
            fontSize: d.fontSize || 16,
            mode: d.mode || 1,
            playTime,
            state: 'pending',
            laneIndex: 0,
            x: currentWidth || 902,
            speed: null,
            textWidth: null,
            totalTime: null
        }

        if (elapsed < 0) {
            // 尚未发射，保持 pending
        } else if (ctx) {
            calcSpeedAndWidth(ctx, item, scale)
            if (elapsed >= item.totalTime) {
                item.state = 'finished'
            } else {
                item.state = 'active'
                item.laneIndex = occupyLane()
                item.x = currentWidth - elapsed * item.speed
            }
        } else {
            // 没有 canvas 上下文时保守处理
            item.state = 'active'
            item.laneIndex = occupyLane()
            item.x = currentWidth || 902
        }

        danmakuItems.value.push(item)
    })
}

// 暴露方法给父组件调用
/**
 * 清空所有弹幕并重置轨道池
 */
function clearAll() {
    // 释放所有活跃弹幕占用的轨道
    const items = danmakuItems.value
    for (let i = 0; i < items.length; i++) {
        if (items[i].state === 'active') releaseLane(items[i].laneIndex)
    }
    danmakuItems.value = []
    initLanePool()
}

defineExpose({ addDanmaku, addDanmakus, clearAll })

// ---- 生命周期 ----
/**
 * 全屏状态变化回调
 */
function onFullscreenChange() {
    isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
    initLanePool()
    animFrameId = requestAnimationFrame(renderLoop)
    document.addEventListener('fullscreenchange', onFullscreenChange)
})

onUnmounted(() => {
    document.removeEventListener('fullscreenchange', onFullscreenChange)
    if (animFrameId) { cancelAnimationFrame(animFrameId); animFrameId = null }
    initLanePool()
})
</script>

<template>
    <canvas v-show="visible" ref="canvasRef" class="danmaku-canvas" />
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
