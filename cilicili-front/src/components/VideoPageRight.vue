<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVideoCoverUrlById, getAvatarUrl } from '@/api/index'
import { fetchVideoInfo, fetchRelatedVideos, formatCount } from '@/utils/videoData'
import { fetchUserById } from '@/utils/userStorage'

const route = useRoute()
const router = useRouter()
const videoId = computed(() => Number(route.query.id) || null)

const upUser = reactive({
    name: '---',
    signature: '',
    fans: 0,
    avatar: ''
})

async function loadUploader() {
    if (!videoId.value) return
    try {
        const videoData = await fetchVideoInfo(videoId.value)
        if (videoData && videoData.uploaderId) {
            const user = await fetchUserById(videoData.uploaderId)
            if (user) {
                upUser.name = user.nickname || user.username || 'UP主'
                upUser.signature = user.signature || ''
                upUser.avatar = user.avatar || ''
            }
        }
    } catch (err) {
        console.error('加载UP主信息失败', err)
    }
}

const avatarBackground = computed(() => {
    const url = getAvatarUrl(upUser.avatar)
    return url ? `url(${url})` : 'url(@/assets/images/Akalin.png)'
})

const recommendList = ref([])
const relatedTotal = ref(0)
const isExpanded = ref(false)

// 推荐的视频初始显示20条
const INITIAL_SHOW = 20

// 侧边栏默认推荐视频
let displayList = ref([
    {
        id: 1,
        title: '推荐视频1',
        cover: '',
        views: 0,
        comments: 0,
        likes: 0
    },
    {
        id: 2,
        title: '推荐视频2',
        cover: '',
        views: 0,
        comments: 0,
        likes: 0
    },
    {
        id: 3,
        title: '推荐视频3',
        cover: '',
        views: 0,
        comments: 0,
        likes: 0
    },
    {
        id: 4,
        title: '推荐视频4',
        cover: '',
        views: 0,
        comments: 0,
        likes: 0
    },
    {
        id: 5,
        title: '推荐视频5',
        cover: '',
        views: 0,
        comments: 0,
        likes: 0
    },
])
displayList = computed(() => {
    if (isExpanded.value || recommendList.value.length <= INITIAL_SHOW) {
        return recommendList.value
    }
    return recommendList.value.slice(0, INITIAL_SHOW)
})

// 侧边栏推荐视频剩余数量（点击展开后显示）
const remainingCount = computed(() => {
    return Math.max(0, recommendList.value.length - INITIAL_SHOW)
})

async function loadRelatedVideos() {
    if (!videoId.value) return
    try {
        const result = await fetchRelatedVideos(videoId.value, 50)
        if (result) {
            recommendList.value = result.videos
            relatedTotal.value = result.total
        }
    } catch (err) {
        console.error('加载相关视频失败', err)
    }
}

function toggleExpand() {
    isExpanded.value = !isExpanded.value
}

function skipVideo(videoId) {
    router.push({ name: 'VideoPage', query: { id: videoId } })
}

function skipAuthorSpace(uploaderId) {
    router.push('/userSpace')
}

watch(videoId, (newId) => {
    if (newId) {
        loadUploader()
        loadRelatedVideos()
        isExpanded.value = false
    }
}, { immediate: true })
</script>

<template>
    <aside class="video-sidebar">
        <!-- UP主信息 -->
        <div class="up-info">
            <div class="up-avatar" :style="{ backgroundImage: avatarBackground }"></div>
            <div class="up-detail">
                <div class="up-name">{{ upUser.name }}</div>
                <div class="up-sign">{{ upUser.signature }}</div>
                <div class="up-actions">
                    <button class="charge-btn">充电</button>
                    <button class="follow-btn">
                        <i class="icon icon-follow"></i>关注 {{ upUser.fans }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 弹幕列表 -->
        <div class="danmaku-list-bar">
            <span>弹幕列表</span>
            <i class="arrow-icon"></i>
        </div>

        <!-- 推荐视频列表 -->
        <div class="recommend-list">
            <div v-for="item in displayList" :key="item.id" class="recommend-item">
                <!-- 视频封面 -->
                <div 
                    class="rec-cover" 
                    :style="{ backgroundImage: `url(${getVideoCoverUrlById(item.id)})` }"
                    @click="skipVideo(item.id)"
                ></div>
                <!-- 视频信息 -->
                <div class="rec-info">
                    <a class="rec-title" @click="skipVideo(item.id)">{{ item.title }}</a>
                    <div class="rec-meta">
                        <i class="icon icon-up"></i>
                        <a class="rec-uploader-name" @click="skipAuthorSpace(item.uploaderId)">{{ item.uploaderName || 'UP主' }}</a>
                    </div>
                    <div class="rec-stats">
                        <span><i class="icon icon-play-sm"></i>{{ formatCount(item.playCount) }}</span>
                        <span><i class="icon icon-danmaku-sm"></i>{{ formatCount(item.commentCount) }}</span>
                    </div>
                </div>
            </div>

            <div
                v-if="remainingCount > 0 && !isExpanded"
                class="expand-bar"
                @click="toggleExpand"
            >
                展开更多 {{ remainingCount }} 条
            </div>

            <div
                v-if="isExpanded && recommendList.length > INITIAL_SHOW"
                class="expand-bar expand-bar--collapse"
                @click="toggleExpand"
            >
                收起
            </div>
        </div>
    </aside>
</template>

<style scoped>
.video-sidebar {
    width: 378px;
    flex-shrink: 0;
}

/* UP主信息 */
.up-info {
    display: flex;
    gap: 16px;
    margin-bottom: 17px;
}
.up-avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background: center/cover;
    background-color: #f1f2f3;
}
.up-detail {
    flex: 1;
}
.up-name {
    font-size: 16px;
    font-weight: 540;
    color: #18191c;
    margin-bottom: 4px;
}
.up-sign {
    font-size: 13px;
    color: #9499a0;
    margin-bottom: 6px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
}
.up-actions {
    display: flex;
    gap: 12px;
}
.charge-btn {
    height: 32px;
    padding: 0 33px;
    border: 1px solid #00AEEC;
    color: #00AEEC;
    background: transparent;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}
.follow-btn {
    width: 200px;
    height: 32px;
    /* padding: 0 20px; */
    background: #00AEEC;
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    gap: 6px;
}

/* 弹幕列表 */
.danmaku-list-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 48px;
    padding: 0 16px;
    background: #f1f2f3;
    border-radius: 6px;
    font-size: 15px;
    color: #18191c;
    margin-bottom: 20px;
    cursor: pointer;
}

/* 推荐视频 */
.recommend-item {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
}
.rec-cover {
    width: 150px;
    height: 85px;
    border-radius: 6px;
    background-color: #f1f2f3;
    background-size: cover;
    background-position: center;
    flex-shrink: 0;
    cursor: pointer;
}
.rec-info {
    flex: 1;
    display: flex;
    justify-content: flex-start;
    flex-direction: column;
    /* justify-content: space-between; */
    height: 85px;
    overflow: hidden;
}
.rec-title {
    font-size: 15px;
    color: #18191c;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex-shrink: 1;
    min-height: 0;
    text-decoration: none;
    transition: color 0.2s linear;
    cursor: pointer;
}

.rec-title:hover {
    color: rgba(0, 174, 236, 1);
}
.rec-meta {
    height: 22px;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #9499a0;
    flex-shrink: 0;
}
.rec-uploader {
    font-size: 13px;
}

.rec-uploader-name {
    color: #9499a0;
    text-decoration: none;
    transition: color 0.2s ease;
    cursor: pointer;
}
.rec-stats {
    height: 22px;
    display: flex;
    gap: 12px;
    font-size: 13px;
    color: #9499a0;
    flex-shrink: 0;
}
.rec-stats span {
    display: inline-flex;
    align-items: center;
    gap: 3px;
}

.expand-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    margin-top: 8px;
    border-radius: 6px;
    background: #f6f7f8;
    color: #00AEEC;
    font-size: 14px;
    cursor: pointer;
    transition: background 0.15s;
}

.expand-bar:hover {
    background: #e8edf0;
}

.expand-bar--collapse {
    color: #9499a0;
}

/* 图标 */
.icon {
    display: inline-block;
    width: 18px;
    height: 18px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}

.icon.icon-follow { background-image: url(@/assets/images/Stroke2addplus.png); }
.icon-up {
    position: relative;
    /* 使用 SVG mask，颜色由 background 控制 */
    background-color: currentColor;
    mask-size: cover;
    mask-position: center;
    mask-repeat: no-repeat;
    mask-image: url(@/assets/images/uploader_default_icon.svg);
    -webkit-mask-size: cover;
    -webkit-mask-position: center;
    -webkit-mask-repeat: no-repeat;
    -webkit-mask-image: url(@/assets/images/uploader_default_icon.svg);
    transition: color 0.2s ease;
}

/* 伪元素承载渐变，通过 opacity 过渡避免闪烁 */
.icon-up::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #00a1d6, #00c8ff, #00a1d6);
    mask: inherit;
    -webkit-mask: inherit;
    opacity: 0;
    transition: opacity 0.2s ease;
}

.rec-meta:hover .icon-up,
.rec-meta:hover .rec-uploader-name {
    color: rgba(0, 174, 236, 1);
}

.rec-meta:hover .icon-up::after {
    opacity: 1;
}
.icon-play-sm { background-image: url(@/assets/images/playsNum_gray.png); width:16px; height:13px; }
.icon-danmaku-sm { background-image: url(@/assets/images/papernote0.png); width:16px; height:16px; }
.arrow-icon {
    display: inline-block;
    width: 10px;
    height: 18px;
    background: url(@/assets/images/Frame_79_864.png) center/contain no-repeat;
}
</style>