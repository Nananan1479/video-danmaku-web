<script setup>
/**
 * DanmakuOverlay - Canvas 弹幕叠加层
 * 使用 Canvas 渲染滚动弹幕，替代 DOM 节点方式，提升大量弹幕时的渲染性能。
 */
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    visible: {
        type: Boolean,
        default: true
    }
})

// ---- Canvas 引用 ----
const canvasRef = ref(null)

// ---- 弹幕数据 ----
/** @type {import('vue').Ref<Array<{id:number|string, content:string, color:string, fontSize:number, mode:number, laneIndex:number, x:number}>>} */
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

    const toRemove = []
    const items = danmakuItems.value

    for (let i = 0; i < items.length; i++) {
        const item = items[i]

        // 模式1（滚动弹幕）：更新 x 坐标
        if (item.mode === 1) {
            item.x -= dx
        }

        const textWidth = measureTextWidth(ctx, item)
        const inView = item.x + textWidth > 0

        if (item.mode === 1 && !inView) {
            releaseLane(item.laneIndex)
            toRemove.push(i)
            continue
        }

        if (!props.visible) continue

        // 跳过完全在屏幕右侧之外的弹幕（刚添加还没进入可视区的情况由 inView 处理）
        if (item.mode === 1 && item.x > currentWidth) continue

        // 绘制弹幕文字
        ctx.save()
        ctx.font = `bold ${item.fontSize}px "Microsoft YaHei", "PingFang SC", sans-serif`
        ctx.fillStyle = item.color
        ctx.textBaseline = 'top'

        // 文字描边（模拟 text-shadow 效果）
        ctx.strokeStyle = 'rgba(0,0,0,0.6)'
        ctx.lineWidth = 2
        ctx.lineJoin = 'round'
        ctx.strokeText(item.content, item.x, item.laneIndex * LANE_HEIGHT + 10)
        ctx.fillText(item.content, item.x, item.laneIndex * LANE_HEIGHT + 10)
        ctx.restore()
    }

    // 移除已出屏的弹幕
    if (toRemove.length > 0) {
        danmakuItems.value = items.filter((_, i) => !toRemove.includes(i))
    }

    animFrameId = requestAnimationFrame(renderLoop)
}

/**
 * 测量弹幕文字的渲染宽度
 * @param {CanvasRenderingContext2D} ctx
 * @param {{content:string, fontSize:number}} item
 * @returns {number} 文字宽度
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
 * 添加一条弹幕到画布
 * @param {{id?:number|string, content:string, color?:string, fontSize?:number, mode?:number}} danmaku
 */
function addDanmaku(danmaku) {
    if (!props.visible) return
    const laneIndex = occupyLane()
    const item = {
        id: danmaku.id || Date.now() + Math.random(),
        content: danmaku.content,
        color: danmaku.color || '#FFFFFF',
        fontSize: danmaku.fontSize || 16,
        mode: danmaku.mode || 1,
        laneIndex,
        x: currentWidth || 902 // 起始位置：屏幕右侧边缘，currentWidth===0 时兜底
    }
    danmakuItems.value.push(item)
}

/**
 * 批量添加弹幕（历史弹幕加载用）
 * @param {Array} list 弹幕数据数组
 */
function addDanmakus(list) {
    if (!list || !list.length) return
    initLanePool()
    danmakuItems.value = []
    list.forEach((d, i) => {
        setTimeout(() => {
            addDanmaku(d)
        }, i * 200)
    })
}

// 暴露方法给父组件调用
defineExpose({ addDanmaku, addDanmakus })

// ---- 生命周期 ----
onMounted(() => {
    initLanePool()
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
