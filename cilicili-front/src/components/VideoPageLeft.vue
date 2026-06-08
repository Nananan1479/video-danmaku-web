<script setup>
import danmuSwitchOnDefaultIcon from '@/assets/images/danmuSwitch_on_default_icon.png'
import danmuSwitchOffDefaultIcon from '@/assets/images/danmuSwitch_off_default_icon.png'
import danmuSwitchOnHoverIcon from '@/assets/images/danmuSwitch_on_hover_icon.png'
import danmuSwitchOffHoverIcon from '@/assets/images/danmuSwitch_off_hover_icon.png'
import danmuSettingDefaultIcon from '@/assets/images/danmuSetting_default_icon.png'
import danmuSettingHoverIcon from '@/assets/images/danmuSetting_hover_icon.png'

import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * VideoPageLeft - 视频播放页左侧主内容区
 * 包含视频播放器、弹幕层、互动操作栏、简介标签及评论区。
 */
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import CustomPlayer from './VideoPage_CustomPlayer.vue'
import DanmakuOverlay from './DanmakuOverlay.vue'
import { fetchVideoInfo, formatCount, formatDuration, formatDate } from '@/utils/videoData'
import { sendDanmakuHTTP, fetchDanmakuByVideoId } from '@/api/index'
import { useWebSocket } from '@/hooks/useWebSocket'
import { getCurrentUser } from '@/utils/userStorage'

const route = useRoute()
/** 当前视频 ID，从路由 query 参数中获取 */
const videoId = computed(() => Number(route.query.id) || null)

/** 视频信息响应式对象 */
const video = reactive({
    id: null,
    title: '加载中...',
    description: '',
    playCount: 0,
    danmakuCount: 0,
    duration: 0,
    coverUrl: '',
    likeCount: 0,
    coinCount: 0,
    collectCount: 0,
    shareCount: 0,
    uploaderId: null,
    createdAt: '',
    copyright: '未经作者授权，禁止转载',
    tags: []
})

/** 格式化后的播放量 */
const displayPlayCount = computed(() => formatCount(video.playCount))
/** 格式化后的弹幕数 */
const displayDanmakuCount = computed(() => formatCount(video.danmakuCount))
/** 格式化后的视频时长 */
const displayDuration = computed(() => formatDuration(video.duration))
/** 格式化后的发布日期 */
const displayDate = computed(() => formatDate(video.createdAt))
/** 视频数据加载中 */
const loading = ref(true)

/**
 * 根据 videoId 从后端加载视频信息
 */
async function loadVideoData() {
    if (!videoId.value) return
    loading.value = true
    try {
        const data = await fetchVideoInfo(videoId.value)
        if (data) {
            Object.assign(video, {
                id: data.id ?? null,
                title: data.title || '未知标题',
                description: data.description || '',
                playCount: data.playCount ?? 0,
                danmakuCount: data.danmakuCount ?? 0,
                duration: data.duration ?? 0,
                coverUrl: data.coverUrl || '',
                likeCount: data.likeCount ?? 0,
                coinCount: data.coinCount ?? 0,
                collectCount: data.collectCount ?? 0,
                shareCount: data.shareCount ?? 0,
                uploaderId: data.uploaderId ?? null,
                createdAt: data.createdAt || '',
                copyright: '未经作者授权，禁止转载',
                tags: []
            })
        }
    } catch (err) {
        console.error('加载视频数据失败', err)
    } finally {
        loading.value = false
    }
}

// 视频 ID 变化时重新加载全部数据（首次由 onMounted 处理，这里仅处理切换）
watch(videoId, (newId) => {
    if (!newId) return
    loadVideoData()
    // 清空旧弹幕并重连 WebSocket，加载新视频弹幕
    if (danmakuOverlayRef.value) {
        danmakuOverlayRef.value.clearAll()
    }
    danmakuTotal.value = 0
    disconnect()
    connect()
    loadDanmaku()
})

// 监听视频标题变化，更新浏览器标签页标题
watch(() => video.title, (title) => {
    if (title && title !== '加载中...') {
        document.title = title + ' - CiliCili'
    }
})

// 组件卸载时恢复默认标题
onBeforeUnmount(() => {
    document.title = 'CiliCili'
})

// ---- 弹幕相关状态 ----
/** 弹幕开关 */
const danmakuOn = ref(true)
/** 当前在线人数 */
const danmakuOnline = ref(0)
/** 弹幕总数 */
const danmakuTotal = ref(0)
/** 弹幕输入框内容 */
const danmakuInput = ref('')
/** 弹幕叠加层组件引用 */
const danmakuOverlayRef = ref(null)
/** 播放器组件引用，用于获取播放状态 */
const customPlayerRef = ref(null)
/** 视频是否正在播放（由 CustomPlayer 暴露） */
const isVideoPlaying = computed(() => customPlayerRef.value?.isPlaying ?? true)
/** 视频当前播放时间（由 CustomPlayer 暴露） */
const videoCurrentTime = computed(() => customPlayerRef.value?.currentTime ?? 0)

// 初始化 WebSocket 弹幕连接
const { connect, disconnect, onDanmakuReceived } = useWebSocket(videoId)

/**
 * 从后端加载当前视频的历史弹幕
 */
async function loadDanmaku() {
    if (!videoId.value) return
    try {
        const res = await fetchDanmakuByVideoId(videoId.value)
        if (res.data.code === 200) {
            const list = res.data.data || []
            danmakuTotal.value = list.length
            // 将历史弹幕推入 Canvas 覆盖层渲染
            if (danmakuOverlayRef.value && list.length > 0) {
                danmakuOverlayRef.value.addDanmakus(list)
            }
        }
    } catch (err) {
        console.error('加载弹幕失败', err)
    }
}

/**
 * 发送弹幕：校验登录态 -> 构造请求 -> HTTP 发送
 */
const sendDanmaku = () => {
    let currentUser
    try {
        currentUser = getCurrentUser()
    } catch (e) {
        console.error('获取用户信息失败', e)
        danmakuInput.value = ''
        return ElMessage.error('未知错误')
    }
    if (!currentUser) {
        console.warn('未登录，无法发送弹幕')
        danmakuInput.value = ''
        ElMessageBox.confirm('请先登录', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            window.location.href = '/login'
        })
        return 
    }
    if (!danmakuInput.value.trim()) return ElMessage.error('请输入弹幕内容')
    const data = {
        videoId: videoId.value,
        content: danmakuInput.value,
        playTime: videoCurrentTime.value ?? 0,
        color: '#FFFFFF',
        mode: 1,
        fontSize: 16
    }
    sendDanmakuHTTP(data).then(res => {
        if (!res || !res.data) {
            console.warn('发送弹幕失败：响应为空')
            ElMessage.error('发送弹幕失败：响应为空')
            return 
        }
        if (res.data.code === 200) {
            danmakuInput.value = ''
            ElMessage.success('弹幕发送成功')
        } else {
            console.warn('发送弹幕失败:', res.data.message)
            ElMessage.error(res.data.message || '发送弹幕失败')
        }
    }).catch(err => {
        console.error('发送弹幕异常', err)
        danmakuInput.value = ''
        ElMessage.error('发送弹幕异常')
    })
}

// 注册 WebSocket 弹幕到达回调：将弹幕推入 Canvas 叠加层并更新总数
onDanmakuReceived((danmaku) => {
    if (danmakuOverlayRef.value) {
        danmakuOverlayRef.value.addDanmaku(danmaku)
    }
    danmakuTotal.value++
})

onMounted(() => {
    loadVideoData()
    connect()
    loadDanmaku()
})

onBeforeUnmount(() => {
    disconnect()
})

// ---- 评论区相关 ----
/** 评论列表（当前为静态 mock 数据） */
const comments = ref([
    { id: 1, username: "用户1", content: "这个视频太棒了，内容很有深度！", date: "2024-01-15", time: "14:30", likes: 256, replies: 12 },
    { id: 2, username: "用户2", content: "学到了很多新知识，感谢分享！", date: "2024-01-14", time: "09:15", likes: 189, replies: 8 },
    { id: 2, username: "用户2", content: "学到了很多新知识，感谢分享！", date: "2024-01-14", time: "09:15", likes: 189, replies: 8 },
    { id: 3, username: "用户3", content: "制作精良，期待更多精彩内容！", date: "2024-01-13", time: "20:45", likes: 342, replies: 0 }
])

/** 评论排序方式：latest | hot */
const commentSort = ref('latest')
/** 评论输入框内容 */
const commentInput = ref('')

/**
 * 提交评论
 */
const submitComment = () => {
    if (!commentInput.value.trim()) return
    // TODO: 接入后端评论接口
    console.log('发表评论:', commentInput.value)
    commentInput.value = ''
}
</script>

<template>
    <div class="video-main">
        <!-- 标题区域 -->
        <div class="video-header">
            <h1 class="title">{{ video.title }}</h1>
            <div class="meta-info">
                <span class="meta-item">
                    <i class="icon icon-play"></i>{{ displayPlayCount }}
                </span>
                <span class="meta-item">
                    <i class="icon icon-danmaku"></i>{{ displayDanmakuCount }}
                </span>
                <span class="meta-item">{{ displayDate }}</span>
                <span class="meta-item">{{ displayDuration }}</span>
                <span class="meta-item copyright">
                    <i class="icon icon-copyright"></i>{{ video.copyright }}
                </span>
            </div>
        </div>

        <!-- 视频播放器 -->
        <div class="video-player">
            <CustomPlayer ref="customPlayerRef" :video-id="videoId" controls>
                <template #danmaku-overlay>
                    <DanmakuOverlay ref="danmakuOverlayRef" :visible="danmakuOn" :is-playing="isVideoPlaying" :current-time="videoCurrentTime" />
                </template> 
            </CustomPlayer>
        </div>

        <!-- 弹幕控制栏 -->
        <div class="danmaku-bar">
            <!-- 观看人数和弹幕总数 -->
            <div class="danmaku-status">
                <span class="online"><strong>{{ danmakuOnline }}</strong>人正在观看，已装填</span>
                <span class="total"><strong>{{ danmakuTotal }}</strong>条弹幕</span>
            </div>
            <!-- 弹幕开关和设置按钮 -->
            <div class="danmaku-switches">
                
                <button class="switch-btn" :class="{ on: danmakuOn }" @click="danmakuOn = !danmakuOn">
                    <img class="icon-default" :src="danmakuOn ? danmuSwitchOnDefaultIcon : danmuSwitchOffDefaultIcon" alt="弹幕开关" />
                    <img class="icon-hover" :src="danmakuOn ? danmuSwitchOnHoverIcon : danmuSwitchOffHoverIcon" alt="弹幕开关" />
                </button>
                <el-popover
                    placement="top"
                    trigger="hover"
                    :width="300"
                    popper-class="settings-popover"
                >
                    <div class="danmakuSettings-showArea">
                        显示区域
                        <el-slider v-model="value2" :step="25" show-stops />
                    </div>
                    <template #reference>
                        <button class="settings-btn">
                            <img class="icon-default" :src="danmuSettingDefaultIcon" alt="弹幕设置" />
                            <img class="icon-hover" :src="danmuSettingHoverIcon" alt="弹幕设置" />
                        </button>
                    </template>
                </el-popover>
            </div>
            <!-- 弹幕发送区域 -->
            <div class="danmaku-send">
                <div class="send-input">
                    <i class="icon icon-word-setting"></i>
                    <input v-model="danmakuInput" placeholder="发个友善的弹幕见证当下" @keyup.enter="sendDanmaku" />
                </div>
                <span class="danmaku-guide">弹幕礼仪 &gt;</span>
                <button class="send-btn" @click="sendDanmaku">发送</button>
            </div>
        </div>

        <!-- 互动操作栏 (点赞投币收藏分享) -->
        <div class="action-bar">
            <div class="actions">
                <button class="action-btn"><i class="icon-videoReward icon icon-like"></i>{{ video.likeCount }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-coin"></i>{{ video.coinCount }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-collect"></i>{{ video.collectCount }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-share"></i>{{ video.shareCount }}</button>
            </div>
            <div class="report">
                <i class="icon icon-report"></i>稿件举报
            </div>
        </div>

        <!-- 简介与标签 -->
        <div class="desc-section">
            <p class="desc-text">{{ video.description }}</p>
            <div class="tags">
                <span v-for="tag in video.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
            <div class="comment-header">
                <div class="comment-title">
                    评论 <span class="comment-count">{{ comments.length }}</span>
                </div>
                <div class="comment-sort">
                    <span :class="{ active: commentSort === 'latest' }" @click="commentSort = 'latest'">最新</span>
                    <span class="divider">|</span>
                    <span :class="{ active: commentSort === 'hot' }" @click="commentSort = 'hot'">最热</span>
                </div>
            </div>

            <!-- 发表评论 -->
            <div class="comment-post">
                <div class="post-avatar"></div>
                <div class="post-input-wrap">
                    <input v-model="commentInput" placeholder="宫廷玉液酒，评论走一走" @keyup.enter="submitComment" />
                </div>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <div class="comment-avatar"></div>
                    <div class="comment-body">
                        <div class="comment-user" style="color: #3C3C3C;">{{ comment.username }}</div>
                        <div class="comment-content">{{ comment.content }}</div>
                        <div class="comment-footer">
                            <span class="comment-time">{{ comment.date }} {{ comment.time }}</span>
                            <span class="comment-like"><i class="icon icon-comment-like"></i>{{ comment.likes }}</span>
                            <span class="comment-reply-btn">回复</span>
                        </div>
                        <!-- 回复挂件 -->
                        <div v-if="comment.replies > 0" class="comment-replies">
                            共 {{ comment.replies }} 条回复，点击查看
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.video-main {
    width: 902px;
    background: #fff;
    border-radius: 4px;
    /* padding: 16px 0; */
}

/* 标题 */
.video-header {
    padding: 0 0 40px;
}
.title {
    font-size: 22px;
    font-weight: 500;
    color: #18191c;
    margin: 0 0 10px;
    /* line-height: 1.3; */
}
.meta-info {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 13px;
    color: #9499a0;
}
.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}
.copyright {
    color: #9499a0;
}

/* 视频播放器占位 */
.video-player {
    position: relative;
    width: 100%;
    height: 486px;
    background: #000;
    border-radius: 2px;
}

/* 弹幕控制栏 */
.danmaku-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 8px 16px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 12px;
    border-radius: 2px;
}
.danmaku-status {
    display: flex;
    align-items: center;
    gap: 1px;
    font-size: 14px;
    color: #555;
    white-space: nowrap;
}
.danmaku-switches {
    display: flex;
    align-items: center;
    gap: 12px;
}
.switch-btn, .settings-btn {
    position: relative;
    width: 24px;
    height: 24px;
    border: none;
    background: center/contain no-repeat;
    cursor: pointer;
}
/* .switch-btn {
    background-image: url(@/assets/images/danmuSwitch_on_default_icon.png);
}

.settings-btn {
    background-image: url(@/assets/images/danmuSetting_default_icon.png);
} */




.switch-btn img, .settings-btn img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    transition: opacity 0.25s ease;
}
.switch-btn .icon-default, .settings-btn .icon-default {
    opacity: 1;
}
.switch-btn .icon-hover, .settings-btn .icon-hover {
    opacity: 0;
}
.switch-btn:hover .icon-default, .settings-btn:hover .icon-default {
    opacity: 0;
}
.switch-btn:hover .icon-hover, .settings-btn:hover .icon-hover {
    opacity: 1;
}

.danmakuSettings-showArea {
    width: 100%;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.danmaku-send {
    display: flex;
    align-items: center;
    flex: 1;
    height: 32px;
    background: #f1f2f3;
    border-radius: 8px;
    padding-left: 12px;
}
.send-input {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 8px;
}
.send-input input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 13px;
    outline: none;
    color: #555;
}
.danmaku-guide {
    font-size: 14px;
    color: #8c929b;
    margin: 0 12px;
    cursor: pointer;
    white-space: nowrap;
}
.send-btn {
    height: 100%;
    padding: 0 24px;
    background: #00AEEC;
    color: #fff;
    border: none;
    border-radius: 0 8px 8px 0;
    font-size: 14px;
    cursor: pointer;
}

/* 互动栏 */
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #e8e8e8;
}
.actions {
    display: flex;
    gap: 60px;
}
.action-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: none;
    border: none;
    font-size: 16px;
    color: #61666D;
    cursor: pointer;
}
.report {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #9499a0;
    cursor: pointer;
}

/* 简介 & 标签 */
.desc-section {
    padding: 16px 0;
    border-bottom: 1px solid #e8e8e8;
}
.desc-text {
    font-size: 15px;
    color: #18191c;
    line-height: 1.6;
    margin-bottom: 14px;
}
.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}
.tag {
    padding: 4px 12px;
    background: #f1f2f3;
    border-radius: 20px;
    font-size: 14px;
    color: #61666d;
}

/* 评论区 */
.comment-section {
    padding-top: 20px;
}
.comment-header {
    display: flex;
    align-items: flex-end;
    gap: 24px;
    margin-bottom: 24px;
}
.comment-title {
    font-size: 20px;
    font-weight: 600;
    color: #18191c;
}
.comment-count {
    font-size: 16px;
    color: #9499a0;
    margin-left: 4px;
}
.comment-sort {
    font-size: 15px;
    color: #9499a0;
    display: flex;
    gap: 8px;
}
.comment-sort span {
    cursor: pointer;
}
.comment-sort span:hover {
    color: #00AEEC;
}
.comment-sort .active {
    color: #3C3C3C;
}

.comment-post {
    display: flex;
    gap: 12px;
    margin-bottom: 28px;
}
.post-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
}
.post-input-wrap {
    flex: 1;
}
.post-input-wrap input {
    width: 100%;
    height: 100%;
    padding: 0 16px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    background: #f6f7f8;
    font-size: 15px;
    outline: none;
    transition: border-color 0.2s;
}
.post-input-wrap input:focus {
    border-color: #00a1d6;
    background: #fff;
}

.comment-item {
    display: flex;
    gap: 14px;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
}
.comment-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
    flex-shrink: 0;
}
.comment-body {
    flex: 1;
}
.comment-user {
    font-size: 14px;
    /* color: #fb7299; */
    margin-bottom: 6px;
}
.comment-content {
    font-size: 15px;
    color: #18191c;
    line-height: 1.5;
    margin-bottom: 8px;
}
.comment-footer {
    display: flex;
    align-items: center;
    gap: 20px;
    font-size: 13px;
    color: #9499a0;
}
.comment-like {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
}
.comment-reply-btn {
    cursor: pointer;
}
.comment-replies {
    margin-top: 8px;
    font-size: 14px;
    color: #9499a0;
    cursor: pointer;
}

/* 通用图标 */
.icon {
    display: inline-block;
    width: 16px;
    height: 16px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    vertical-align: middle;
}

.icon-videoReward{
    width: 24px;
    height: 24px;
}

.icon-play { background-image: url(@/assets/images/playsNum_gray.png); }
.icon-danmaku { background-image: url(@/assets/images/papernote0.png); }
.icon-copyright { background-image: url(@/assets/images/PhosphoriconsSecurityWarningsPhosphoriconsProhibit.png); }
.icon-word-setting { background-image: url(@/assets/images/wordSetting_icon.png); }
.icon-like { background-image: url(@/assets/images/like_solid.png); }
.icon-coin { background-image: url(@/assets/images/reward.png); }
.icon-collect { background-image: url(@/assets/images/Star_109_774.png); }
.icon-share { background-image: url(@/assets/images/share_icon.png); }
.icon-report { background-image: url(@/assets/images/attentiontriangle.png); }
.icon-comment-like { background-image: url(@/assets/images/Mobile0.png); }
</style>


<!-- 专门用于控制ElementPlus的Popover组件 -->
<style>
.settings-popover {
    background: #0000008c !important;
    /* border-radius: 4px; */
    margin-bottom: 12px;
    padding: 12px;
    border: none !important;
    color: #fff !important;
}
.settings-popover .el-popper__arrow {
    display: none !important;
}
.settings-popover .el-slider {
    --el-slider-height: 4px;
    --el-slider-main-bg-color-height: 4px;
    --el-slider-button-size: 12px;
}
.settings-popover .el-slider__runway {
    height: 4px;
}
.settings-popover .el-slider__bar {
    height: 4px;
}
.settings-popover .el-slider__button {
    width: 12px;
    height: 12px;
}
</style>