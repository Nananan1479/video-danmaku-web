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
    commentCount: 0,
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
/** 格式化后的评论数 */
const displayCommentCount = computed(() => formatCount(video.commentCount))
/** 格式化后的视频时长 */
const displayDuration = computed(() => formatDuration(video.duration))
/** 格式化后的发布日期 */
const displayDate = computed(() => formatDate(video.createdAt))
/** 格式化后的点赞数 */
const displayLikeCount = computed(() => formatCount(video.likeCount))
/** 格式化后的投币数 */
const displayCoinCount = computed(() => formatCount(video.coinCount))
/** 格式化后的收藏数 */
const displayCollectCount = computed(() => formatCount(video.collectCount))
/** 格式化后的分享数 */
const displayShareCount = computed(() => formatCount(video.shareCount))
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
                commentCount: data.commentCount ?? 0,
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
const sendDanmaku = async () => {
    let currentUser
    try {
        currentUser = await getCurrentUser()
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
                    <i class="icon icon-comment"></i>{{ displayCommentCount }}
                </span>
                <span class="meta-item">{{ displayDate }}</span>
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
                <button class="icon-videoReward action-btn ">
                    <svg viewBox="0 0 34 34" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="34.000000" height="34.000000" fill="currentColor" customFrame="#000000">
                        <!-- <rect id="like_solid_default" width="34.000000" height="34.000000" x="0.000000" y="0.000000" /> -->
                        <path id="like_solid_default" d="M20.3325 0.566229C21.2822 -0.362051 22.2106 0.00676935 22.8743 0.566209C23.3106 0.933989 24.1218 1.52549 24.3273 2.25735C24.6303 3.11089 24.7768 3.50451 24.8314 3.91047C24.8817 4.28463 24.8538 4.66929 24.7985 5.43421C24.79 5.55203 24.7808 5.67887 24.7711 5.81607C24.6309 7.81069 24.2647 8.71185 24.1378 9.00123C24.0069 9.29963 23.8362 9.64181 23.6256 10.0277L23.2798 10.6395C23.1546 10.8543 23.0194 11.0801 22.8743 11.3168L28.5515 11.3168C31.0741 11.3168 33.1191 13.4586 33.1191 16.1007C33.1191 16.4627 33.0798 16.8236 33.0021 17.1764L31.3597 24.6306C30.4032 28.9717 26.7123 32.0469 22.4585 32.0469L11.4554 32.0469L11.4554 11.3168C11.4554 11.3168 13.136 10.2923 15.1181 9.17681C17.1003 8.06133 16.9859 7.69571 17.6694 6.87031L20.3325 0.566229ZM10.1351 31.9859L10.1351 11.2559L4.04504 11.2559C2.36332 11.2559 1 12.6837 1 14.4451L1 28.7967C1 30.558 2.36332 31.9859 4.04504 31.9859L10.1351 31.9859Z" fill-rule="evenodd" />
                    </svg>
                    {{ displayLikeCount }}
                </button>
                <button class="action-btn icon-videoReward">
                    <svg viewBox="0 0 34 34" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="34.000000" height="34.000000" fill="currentColor" customFrame="#000000">
                        <path id="reward_default" d="M0 17C0 26.3888 7.61116 34 17 34C26.3888 34 34 26.3888 34 17C34 7.61116 26.3888 0 17 0C7.61116 0 0 7.61116 0 17ZM24.2001 6.5L9.80004 6.5Q9.48938 6.5 9.22422 6.60982L9.22218 6.61066Q9.17622 6.6298 9.13162 6.65224C8.83544 6.79666 8.5967 7.0354 8.45228 7.33158Q8.42986 7.37612 8.41074 7.42206L8.40986 7.42418Q8.30004 7.68934 8.30004 8Q8.30004 8.31062 8.40984 8.57576L8.40986 8.5758Q8.42934 8.62282 8.45228 8.66842C8.5967 8.9646 8.83544 9.20334 9.13162 9.34776Q9.1749 9.36952 9.21946 9.38818Q9.22184 9.38918 9.22422 9.39016L9.22424 9.39016Q9.48938 9.5 9.80004 9.5L15.5 9.5L15.5 11.7251Q13.6347 11.954 12.0777 12.7708Q11.2803 13.189 10.5637 13.7615Q9.22956 14.8274 8.47638 16.2098Q7.49998 18.0019 7.5 20.326L7.5 23.522Q7.50004 23.8326 7.6099 24.0978Q7.62934 24.1447 7.65222 24.1902C7.79666 24.4864 8.0354 24.7251 8.3316 24.8695Q8.37718 24.8925 8.42418 24.9119Q8.6862 25.0204 8.99262 25.0217L9 25.0218L9.00024 25.0218Q9.31076 25.0217 9.57584 24.9119Q9.62284 24.8924 9.6684 24.8695C9.9646 24.7251 10.2033 24.4864 10.3478 24.1902Q10.3707 24.1445 10.3902 24.0975Q10.4987 23.8355 10.5 23.5291L10.5 23.5218L10.5 20.326Q10.5 18.7209 11.1978 17.5219L11.1978 17.5218Q11.6624 16.7236 12.4362 16.1053Q12.7684 15.84 13.1294 15.6226L13.1294 15.6225Q14.1901 14.9837 15.5 14.7585L15.5 28.0002Q15.5 28.3108 15.6098 28.5758Q15.6293 28.6228 15.6522 28.6684C15.7967 28.9646 16.0354 29.2033 16.3316 29.3478Q16.3727 29.3685 16.4151 29.3864Q16.4196 29.3883 16.4242 29.3902Q16.6862 29.4987 16.9926 29.5L17 29.5L17.0002 29.5Q17.3108 29.4999 17.5759 29.3901Q17.6228 29.3707 17.6684 29.3478C17.9646 29.2033 18.2033 28.9646 18.3478 28.6684Q18.3707 28.6227 18.3902 28.5756Q18.4987 28.3137 18.5 28.0074L18.5 28L18.5 14.7585Q19.8098 14.9836 20.8705 15.6225L20.8707 15.6226Q21.2317 15.84 21.5638 16.1053Q22.3376 16.7236 22.8022 17.5218Q23.5 18.7208 23.5 20.326L23.5 23.5217Q23.5 23.8325 23.6099 24.0977Q23.6294 24.1448 23.6524 24.1905C23.7968 24.4864 24.0353 24.7249 24.3312 24.8693Q24.377 24.8923 24.4242 24.9119Q24.6862 25.0204 24.9926 25.0217L24.9999 25.0217Q25.3106 25.0217 25.5758 24.9119Q25.623 24.8923 25.6688 24.8693C25.9648 24.7248 26.2034 24.4861 26.3477 24.1901Q26.3706 24.1446 26.3901 24.0976Q26.4984 23.8361 26.4999 23.5303L26.5 23.5217L26.5 20.3261Q26.5 18.0019 25.5236 16.2097L25.5236 16.2096L25.5234 16.2094Q24.7703 14.8272 23.4363 13.7615Q22.7197 13.189 21.9223 12.7707L21.9222 12.7707Q20.3652 11.954 18.5 11.725L18.5 9.5L24.2003 9.5Q24.511 9.49994 24.7761 9.3901Q24.823 9.37066 24.8685 9.34778C25.1647 9.20334 25.4034 8.9646 25.5479 8.6684Q25.5708 8.62282 25.5903 8.57582L25.5903 8.57574Q25.6988 8.31376 25.7001 8.00738L25.7001 8L25.7001 7.99976Q25.7 7.68922 25.5902 7.42414Q25.5708 7.37716 25.5479 7.3316C25.4034 7.0354 25.1647 6.79666 24.8685 6.65222Q24.8264 6.63106 24.7831 6.61282Q24.7796 6.61134 24.7761 6.60988L24.7759 6.60982Q24.5139 6.5013 24.2075 6.50002L24.2001 6.5Z" fill-rule="evenodd" />
                    </svg>
                    {{ displayCoinCount }}
                </button>
                <button class="action-btn icon-videoReward">
                    <svg viewBox="5.3 5.2 34.4 32.6" xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor">
                        <path id="collect_default" d="M28.0757 12.6819C28.4386 13.5072 29.2176 14.0731 30.1146 14.1632L38.241 14.9791C40.4059 15.1964 41.2791 17.8839 39.6554 19.3322L33.5606 24.7689C32.8878 25.369 32.5903 26.2848 32.7818 27.1657L34.5171 35.1465C34.9793 37.2726 32.6932 38.9335 30.8141 37.8369L23.7601 33.7203C22.9815 33.266 22.0185 33.266 21.2399 33.7203L14.1859 37.8369C12.3068 38.9335 10.0207 37.2726 10.4829 35.1465L12.2182 27.1657C12.4097 26.2848 12.1122 25.369 11.4394 24.7689L5.34457 19.3322C3.72092 17.8839 4.59413 15.1964 6.75899 14.9791L14.8854 14.1632C15.7824 14.0731 16.5614 13.5071 16.9243 12.6819L20.2114 5.20531C21.0871 3.21358 23.9129 3.21358 24.7886 5.20531L28.0757 12.6819Z" fill-rule="evenodd" />
                    </svg>
                    {{ displayCollectCount }}
                </button>
                <button class="action-btn icon-videoReward">
                    <svg viewBox="0 0 36.0344 35.0575" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="36.034424" height="35.057495" fill="currentColor">
                        <path id="share_default" d="M15.6517 31.4273C15.6517 32.2735 16.6373 32.7372 17.2893 32.1977L35.6719 16.985C36.1552 16.5851 36.1552 15.8441 35.6719 15.4442L17.2893 0.231501C16.6373 -0.308019 15.6517 0.155661 15.6517 1.0019L15.6517 11.5145C12.4407 11.6171 11.5628 11.812 8.50001 12.4407C5.00003 13.4406 3.64821 15.6196 2.48133 17.9812C-0.414968 23.8427 0.0237124 35.0575 0.0237124 35.0575C0.0237124 35.0575 2.54383 28.3628 5.92205 25.0473C7.10759 23.8838 7.39635 23.2808 10.3455 22.6919C12.3144 22.2201 14.7578 22.1263 15.6517 22.1077L15.6517 31.4273Z" fill-rule="evenodd" />
                    </svg>
                    {{ displayShareCount }}
                </button>
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
                    评论 <span class="comment-count">{{ displayCommentCount }}</span>
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
    font-size: 19px;
    font-weight: 500;
    color: #222;
    margin: 0 0 10px;
    letter-spacing: 1px;
    font-family: "Noto Sans SC";
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
    transition: all 0.3s;
}
.action-btn:hover {
    /* fill: #00AEEC; */
    color: #00AEEC;
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
    white-space: pre-wrap;
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

.icon-videoReward svg {
    width: 24px;
    height: 24px;
    /* transition: all 0.3s;
    fill: #61666D; */
}




.icon-play { background-image: url(@/assets/images/playsNum_gray.png); }
.icon-comment { background-image: url(@/assets/images/papernote0.png); }
.icon-copyright { background-image: url(@/assets/images/PhosphoriconsSecurityWarningsPhosphoriconsProhibit.png); }
.icon-word-setting { background-image: url(@/assets/images/wordSetting_icon.png); }
/* .icon-like { background-image: url(@/assets/images/like_solid_hover.svg); } */
/* .icon-like :hover { background-image: url(@/assets/images/like_solid_hover.svg);} */
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