<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
    videoId: {
        type: Number,
        default: null
    },
    src: {
        type: String,
        default: ''
    }
})

const videoSrc = computed(() => {
    if (props.src) return props.src
    if (props.videoId) return `${import.meta.env.VITE_API_URL}/api/videos/${props.videoId}`
    return ''
})

const videoRef = ref(null)
const playerContainer = ref(null)

// 状态
const duration = ref(0)
const currentTime = ref(0)
const buffered = ref(0)
const isPlaying = ref(false)
const volume = ref(1)
const playbackRate = ref(1)
const showControls = ref(true)
let hideTimer = null
let errorMessage = ref('')

// 百分比计算
const currentPercent = computed(() => (duration.value ? (currentTime.value / duration.value) * 100 : 0))
const bufferPercent = computed(() => (duration.value ? (buffered.value / duration.value) * 100 : 0))

// videoId 变化时，重置播放状态
watch(() => props.videoId, () => {
    isPlaying.value = false
    // duration.value = 0
    // currentTime.value = 0
    errorMessage.value = ''
})

// 视频数据加载完成，尝试自动播放
const onCanPlay = () => {
    const video = videoRef.value
    if (video && video.paused) {
        video.play().catch((e) => {
            // 浏览器自动播放策略可能拦截，静默处理
            console.warn('自动播放被阻止:', e.message)
        })
    }
}

// 事件处理
const onLoaded = () => {
    duration.value = videoRef.value.duration
    videoRef.value.volume = volume.value
}

const onTimeUpdate = () => {
    currentTime.value = videoRef.value.currentTime
}

const onProgress = () => {
    if (videoRef.value.buffered.length > 0) {
        buffered.value = videoRef.value.buffered.end(videoRef.value.buffered.length - 1)
    }
}

const onPlay = () => { isPlaying.value = true }
const onPause = () => { isPlaying.value = false }
const onEnded = () => { isPlaying.value = false }

// 向父组件暴露播放状态与当前时间，用于控制弹幕暂停/恢复及按时间显示
defineExpose({ isPlaying, currentTime })

const onError = (e) => {
    const video = e.target
    const error = video.error
    if (error) {
        const messages = {
            1: 'MEDIA_ERR_ABORTED - 视频加载被中止',
            2: 'MEDIA_ERR_NETWORK - 网络错误或无响应',
            3: 'MEDIA_ERR_DECODE - 视频解码失败(格式不支持或文件损坏)',
            4: 'MEDIA_ERR_SRC_NOT_SUPPORTED - 视频格式不支持或资源不存在'
        }
        console.error('视频加载失败 [' + error.code + ']:', messages[error.code] || '未知错误')
        console.error('请求地址:', video.src)
        console.error('浏览器提示:', error.message)
        errorMessage.value = `视频加载失败 [${error.code}] ${messages[error.code] || '未知错误'}`
    } else {
        console.error('视频加载失败:', e || '未知错误')
    }
}

const togglePlay = () => {
    if (videoRef.value.paused) videoRef.value.play()
    else videoRef.value.pause()
}

// 进度条拖动
const onProgressMouseDown = (e) => {
    const bar = e.currentTarget
    const update = (e) => {
        const rect = bar.getBoundingClientRect()
        let percent = (e.clientX - rect.left) / rect.width
        percent = Math.max(0, Math.min(1, percent))
        videoRef.value.currentTime = percent * duration.value
    }
    update(e)
    const onMove = (e) => update(e)
    const onUp = () => {
        document.removeEventListener('mousemove', onMove)
        document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
}

// 音量
const toggleMute = () => {
    videoRef.value.muted = !videoRef.value.muted
    volume.value = videoRef.value.muted ? 0 : videoRef.value.volume
}
const onVolumeChange = () => {
    videoRef.value.volume = volume.value
    videoRef.value.muted = (volume.value === 0)
}

// 倍速
const onRateChange = () => {
    videoRef.value.playbackRate = playbackRate.value
}

// 全屏
const toggleFullscreen = () => {
    if (document.fullscreenElement) {
        document.exitFullscreen()
    } else {
        playerContainer.value.requestFullscreen()
    }
}

// 控制栏自动隐藏（桌面端）
const resetHideTimer = () => {
    showControls.value = true
    clearTimeout(hideTimer)
    hideTimer = setTimeout(() => {
        if (isPlaying.value) showControls.value = false
    }, 3000)
}
onMounted(() => {
    playerContainer.value.addEventListener('mousemove', resetHideTimer)
    playerContainer.value.addEventListener('mouseleave', () => showControls.value = false)
})
onUnmounted(() => clearTimeout(hideTimer))

// 时间格式化
const formatTime = (sec) => {
    const m = Math.floor(sec / 60)
    const s = Math.floor(sec % 60)
    return `${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}`
}
</script>

<template>
    <div class="custom-player" ref="playerContainer">
        <!-- 视频元素（隐藏原生控件） -->

        <!-- 将弹幕层放到 CustomPlayer 内部，让它随播放器一起进入全屏。 -->
        <slot name="danmaku-overlay" />
        <video
            ref="videoRef"
            :src="videoSrc"
            @loadedmetadata="onLoaded"
            @timeupdate="onTimeUpdate"
            @progress="onProgress"
            @play="onPlay"
            @pause="onPause"
            @ended="onEnded"
            @canplay="onCanPlay"
            @error="onError"
        ></video>

        <!-- 控制栏（悬停显示） -->
        <div class="controls" :class="{ hidden: !showControls }">
            <!-- 进度条 -->
            <div class="progress-bar" @mousedown="onProgressMouseDown">
                <div class="progress-buffer" :style="{ width: bufferPercent + '%' }"></div>
                <div class="progress-current" :style="{ width: currentPercent + '%' }"></div>
                <div class="progress-thumb" :style="{ left: currentPercent + '%' }"></div>
            </div>

            <!-- 底部按钮区 -->
            <div class="bottom-controls">
                <button @click="togglePlay">{{ isPlaying ? '⏸' : '▶' }}</button>
                <span class="time">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
                
                <div class="volume-area">
                    <button @click="toggleMute">{{ volume === 0 ? '🔇' : '🔊' }}</button>
                    <input type="range" min="0" max="1" step="0.1" v-model="volume" @input="onVolumeChange" />
                </div>

                <select v-model="playbackRate" @change="onRateChange">
                    <option value="0.5">0.5x</option>
                    <option value="1">1x</option>
                    <option value="1.5">1.5x</option>
                    <option value="2">2x</option>
                </select>

                <button @click="toggleFullscreen">⛶</button>
            </div>
        </div>
        <div class="videoError" v-if="errorMessage">{{ errorMessage }}</div>
    </div>
</template>

<style scoped>
.custom-player {
    position: relative;
    width: 100%;
    height: 100%;
    /* aspect-ratio: 16/9; */
    background: #000;
    overflow: hidden;
}
video {
    width: 100%;
    height: 100%;
    object-fit: contain;
}
.controls {
    position: absolute;
    bottom: 0; left: 0; right: 0;
    background: rgba(0,0,0,0.6);
    padding: 8px;
    opacity: 1;
    transition: opacity 0.3s;
}
.controls.hidden {
    opacity: 0;
}
.progress-bar {
    position: relative;
    height: 4px;
    background: #555;
    cursor: pointer;
    margin-bottom: 8px;
}
.progress-buffer {
    position: absolute;
    top: 0; left: 0;
    height: 100%;
    background: #aaa;
}
.progress-current {
    position: absolute;
    top: 0; left: 0;
    height: 100%;
    background: #00a1d6;
}
.progress-thumb {
    position: absolute;
    top: -4px;
    width: 12px; height: 12px;
    background: #fff;
    border-radius: 50%;
    transform: translateX(-50%);
}
.bottom-controls {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #fff;
}
.time {
    
}
.volume-area {
    display: flex;
    align-items: center;
    gap: 4px;
}

.videoError {
    position: absolute;
    top: 50%; left: 50%;
    transform: translate(-50%,-50%);
    font-size: 14px;
    font-weight: 500;
    color: #fff;
}
</style>