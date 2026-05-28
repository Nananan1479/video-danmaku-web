<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getVideoCoverUrlById, getAvatarUrl } from '@/api/index'
import { fetchVideoInfo, fetchRelatedVideos, formatCount } from '@/utils/videoData'
import { fetchUserById } from '@/utils/userStorage'

const route = useRoute()
const videoId = computed(() => Number(route.query.id) || null)

const upUser = reactive({
    name: '加载中...',
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

const INITIAL_SHOW = 20

const displayList = computed(() => {
    if (isExpanded.value || recommendList.value.length <= INITIAL_SHOW) {
        return recommendList.value
    }
    return recommendList.value.slice(0, INITIAL_SHOW)
})

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
                <div class="rec-cover" :style="{ backgroundImage: `url(${getVideoCoverUrlById(item.id)})` }"></div>
                <div class="rec-info">
                    <div class="rec-title">{{ item.title }}</div>
                    <div class="rec-meta">
                        <i class="icon icon-up"></i>{{ item.uploaderName || 'UP主' }}
                    </div>
                    <div class="rec-stats">
                        <span><i class="icon icon-play-sm"></i>{{ formatCount(item.playCount) }}</span>
                        <span><i class="icon icon-danmaku-sm"></i>{{ formatCount(item.danmakuCount) }}</span>
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
    margin-bottom: 16px;
}
.up-avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
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
}
.rec-meta {
    height: 22px;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    -webkit-line-clamp: 2;
    color: #9499a0;
    flex-shrink: 0;
}
.rec-uploader {
    font-size: 13px;
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
/* .video-icon {
    width: 16px;
    height: 16px;
} */
.icon.icon-follow { background-image: url(@/assets/images/Stroke2addplus.png); }
.icon-up { background-image: url(@/assets/images/uploader_icon.png); }
.icon-play-sm { background-image: url(@/assets/images/playsNum_gray.png); width:16px; height:13px; }
.icon-danmaku-sm { background-image: url(@/assets/images/papernote.png); width:16px; height:16px; }
.arrow-icon {
    display: inline-block;
    width: 10px;
    height: 18px;
    background: url(@/assets/images/Frame_79_864.png) center/contain no-repeat;
}
</style>