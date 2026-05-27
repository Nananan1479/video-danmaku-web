<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
    videoFile: {
        type: File,
        required: true
    }
})

const emit = defineEmits(['confirm', 'cancel'])

const videoRef = ref(null)
const canvasRef = ref(null)
const coverSrc = ref('')
const isPlaying = ref(false)
const duration = ref(0)
const currentTime = ref(0)
const isDragging = ref(false)
let videoUrl = null

onMounted(() => {
    if (props.videoFile) {
        videoUrl = URL.createObjectURL(props.videoFile)
        videoRef.value.src = videoUrl
    }
})

onUnmounted(() => {
    if (videoUrl) {
        URL.revokeObjectURL(videoUrl)
    }
})

const currentPercent = computed(() =>
    duration.value ? (currentTime.value / duration.value) * 100 : 0
)

function onLoaded() {
    duration.value = videoRef.value.duration
    videoRef.value.currentTime = Math.min(1, duration.value)
}

function onTimeUpdate() {
    if (!isDragging.value) {
        currentTime.value = videoRef.value.currentTime
    }
}

function onSeeked() {
    captureFrame()
}

function onEnded() {
    isPlaying.value = false
}

function onError(e) {
    const error = videoRef.value?.error
    if (error) {
        console.error('视频加载失败 [' + error.code + ']:', error.message)
    }
}

function togglePlay() {
    if (!videoRef.value) return
    if (videoRef.value.paused) {
        videoRef.value.play()
        isPlaying.value = true
    } else {
        videoRef.value.pause()
        isPlaying.value = false
    }
}

function onProgressMouseDown(e) {
    isDragging.value = true
    const bar = e.currentTarget
    const update = (e) => {
        const rect = bar.getBoundingClientRect()
        let percent = (e.clientX - rect.left) / rect.width
        percent = Math.max(0, Math.min(1, percent))
        const seekTime = percent * duration.value
        currentTime.value = seekTime
        videoRef.value.currentTime = seekTime
    }
    update(e)
    const onMove = (e) => update(e)
    const onUp = () => {
        isDragging.value = false
        document.removeEventListener('mousemove', onMove)
        document.removeEventListener('mouseup', onUp)
    }
    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
}

function captureFrame() {
    const video = videoRef.value
    const canvas = canvasRef.value
    if (!video || !canvas) return
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    coverSrc.value = canvas.toDataURL('image/png')
}

function handleConfirm() {
    captureFrame()
    if (coverSrc.value) {
        emit('confirm', coverSrc.value)
    }
}

function handleCancel() {
    emit('cancel')
}

function formatTime(sec) {
    if (!sec || !isFinite(sec)) return '00:00'
    const m = Math.floor(sec / 60)
    const s = Math.floor(sec % 60)
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}
</script>

<template>
    <div class="cover-selector-overlay" @click.self="handleCancel">
        <div class="cover-selector">
            <div class="cover-selector__header">
                <h3>从视频中截取封面</h3>
                <button class="cover-selector__close" @click="handleCancel">✕</button>
            </div>

            <div class="cover-selector__body">
                <div class="cover-selector__player">
                    <video
                        ref="videoRef"
                        class="cover-selector__video"
                        preload="metadata"
                        @loadedmetadata="onLoaded"
                        @timeupdate="onTimeUpdate"
                        @seeked="onSeeked"
                        @ended="onEnded"
                        @error="onError"
                    ></video>

                    <div class="cover-selector__controls">
                        <!-- 播放/暂停按钮 - 可通过具名插槽 playBtn 自定义 -->
                        <slot name="playBtn" :playing="isPlaying" :toggle="togglePlay">
                            <button class="cover-selector__play-btn" @click="togglePlay">
                                {{ isPlaying ? '⏸' : '▶' }}
                            </button>
                        </slot>

                        <!-- 时间显示 -->
                        <span class="cover-selector__time">
                            {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
                        </span>

                        <!-- 进度条 - 可通过具名插槽 progressBar 自定义 -->
                        <slot name="progressBar" :percent="currentPercent" :onMouseDown="onProgressMouseDown">
                            <div class="cover-selector__progress" @mousedown="onProgressMouseDown">
                                <div
                                    class="cover-selector__progress-fill"
                                    :style="{ width: currentPercent + '%' }"
                                ></div>
                                <div
                                    class="cover-selector__progress-thumb"
                                    :style="{ left: currentPercent + '%' }"
                                ></div>
                            </div>
                        </slot>
                    </div>
                </div>

                <div class="cover-selector__preview">
                    <div class="cover-selector__preview-label">封面预览</div>
                    <div class="cover-selector__preview-box">
                        <img v-if="coverSrc" :src="coverSrc" alt="封面预览" />
                        <span v-else class="cover-selector__preview-placeholder">拖动进度条选择画面</span>
                    </div>
                </div>
            </div>

            <div class="cover-selector__footer">
                <button class="cover-selector__btn cover-selector__btn--cancel" @click="handleCancel">取消</button>
                <button class="cover-selector__btn cover-selector__btn--confirm" @click="handleConfirm">确认使用</button>
            </div>

            <canvas ref="canvasRef" style="display:none"></canvas>
        </div>
    </div>
</template>

<style scoped>
.cover-selector-overlay {
    position: fixed;
    inset: 0;
    z-index: 2000;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
}

.cover-selector {
    width: 720px;
    max-width: 95vw;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3);
}

.cover-selector__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
}

.cover-selector__header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #18191c;
}

.cover-selector__close {
    background: none;
    border: none;
    font-size: 18px;
    color: #9499a0;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
}

.cover-selector__close:hover {
    background: #f5f5f5;
    color: #18191c;
}

.cover-selector__body {
    display: flex;
    gap: 16px;
    padding: 16px 20px;
}

.cover-selector__player {
    flex: 1;
    min-width: 0;
    background: #000;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
}

.cover-selector__video {
    width: 100%;
    display: block;
}

.cover-selector__controls {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 12px;
    background: rgba(0, 0, 0, 0.65);
}

.cover-selector__play-btn {
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: background 0.15s;
}

.cover-selector__play-btn:hover {
    background: rgba(255, 255, 255, 0.35);
}

.cover-selector__time {
    font-size: 12px;
    color: #fff;
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
    flex-shrink: 0;
}

.cover-selector__progress {
    flex: 1;
    height: 6px;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 3px;
    cursor: pointer;
    position: relative;
}

.cover-selector__progress-fill {
    height: 100%;
    background: #00AEEC;
    border-radius: 3px;
    transition: width 0.08s linear;
}

.cover-selector__progress-thumb {
    position: absolute;
    top: 50%;
    width: 14px;
    height: 14px;
    background: #fff;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.cover-selector__preview {
    width: 160px;
    flex-shrink: 0;
}

.cover-selector__preview-label {
    font-size: 13px;
    color: #61666d;
    margin-bottom: 8px;
}

.cover-selector__preview-box {
    width: 160px;
    height: 90px;
    border-radius: 6px;
    background: #f1f2f3;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

.cover-selector__preview-box img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.cover-selector__preview-placeholder {
    font-size: 12px;
    color: #9499a0;
}

.cover-selector__footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
}

.cover-selector__btn {
    height: 36px;
    padding: 0 24px;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    border: none;
    transition: all 0.15s;
}

.cover-selector__btn--cancel {
    background: #f1f2f3;
    color: #61666d;
}

.cover-selector__btn--cancel:hover {
    background: #e8e8e8;
}

.cover-selector__btn--confirm {
    background: #00AEEC;
    color: #fff;
}

.cover-selector__btn--confirm:hover {
    background: #009dd5;
}
</style>
