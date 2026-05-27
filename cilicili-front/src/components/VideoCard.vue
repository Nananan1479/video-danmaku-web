<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getVideoCoverUrlById } from '@/api/index'
import { formatCount, formatDuration, formatDate } from '@/utils/videoData'

const router = useRouter()

const props = defineProps({
    video: {
        type: Object,
        required: true
    }
})

defineEmits(['click'])

const playCount = computed(() => formatCount(props.video.playCount))
const commentCount = computed(() => formatCount(props.video.commentCount))
const duration = computed(() => formatDuration(props.video.duration))
const dateText = computed(() => formatDate(props.video.updatedAt))
const coverUrl = computed(() => getVideoCoverUrlById(props.video.id))

function skipAuthorSpace(id) {
    console.log("跳转UP主空间", id)
}

function skipVideo(video) {
    router.push({ name: 'VideoPage', query: { id: video.id } })
}

</script>

<template>
    <div class="video-card" @click="$emit('click')">
        <!-- 视频封面部分 -->
        <div class="video-card__cover" @click="skipVideo(video)">
            <img
                class="video-card__cover-img"
                :src="coverUrl"
                alt="视频封面"
            />
            <div class="video-card__cover-overlay">
                <div class="video-card__stats">
                    <div class="video-card__stat">
                        <img class="video-card__stat-icon" src="@/assets/images/videoPlays_white_icon.png" alt="播放图标" />
                        <span class="video-card__stat-text">{{ playCount }}</span>
                    </div>
                    <div class="video-card__stat">
                        <img class="video-card__stat-icon" src="@/assets/images/videoComments_white_icon.png" alt="评论图标" />
                        <span class="video-card__stat-text">{{ commentCount }}</span>
                    </div>
                </div>
                <span class="video-card__duration">{{ duration }}</span>
            </div>
        </div>
        <!-- 视频信息部分 -->
        <div class="video-card__info" :title="video.title">
            <h3 class="video-card__title">
                <a @click="skipVideo(video)">{{ video.title }}</a>
            </h3>
            <div class="video-card__meta">
                <a @click="skipAuthorSpace(video.uploaderId)">
                    <span class="video-card__up-badge"></span>
                    <span class="video-card__author">{{ video.uploaderName || 'UP主' }}</span>
                    <!-- <span class="video-card__dot">·</span> -->
                    <span class="video-card__date">· {{ dateText }}</span>
                </a>
            </div>
        </div>
    </div>
</template>

<style scoped>
.video-card {
    /* width: 236.6px;
    height: 214px; */
    width: 100%;
    height: 100%;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.video-card__cover {
    /* height: 132px; */
    width: 100%;
    background-color: #f1f2f3;
    border-radius: 7px;
    /* 16比9的宽度 */
    /* 9 / 16 * 100% = 56.25% */
    padding-top: 56.25%;
    overflow: hidden;
    position: relative;
    display: flex;
    align-items: flex-end;
    cursor: pointer;
}

.video-card__cover-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    z-index: 1;
}

.video-card__cover-overlay {
    position: absolute;
    z-index: 2;
    width: 100%;
    height: 24px;
    padding: 1px 10px;
    background: linear-gradient(180.00deg, rgba(57, 57, 57, 0) 0%,rgba(57, 57, 57, 0.6) 50%,rgba(57, 57, 57, 0.86) 100%);
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.video-card__stats {
    display: flex;
    align-items: center;
    gap: 10px;
}

.video-card__stat {
    display: flex;
    align-items: center;
    gap: 4px;
}

.video-card__stat-icon {
    width: 18px;
    height: 20px;
    object-fit: cover;
}

.video-card__stat-text {
    font-size: 12px;
    font-weight: 500;
    color: #fff;
}

.video-card__duration {
    font-size: 12px;
    font-weight: 500;
    color: #fff;
}

.video-card__info {
    height: 74px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding-right: 36px;
}

.video-card__title {
    font-size: 16px;
    font-weight: 400;
    color: #000;
    letter-spacing: 1px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    line-height: 21px;
    max-height: 42px;
    margin: 0;
}

.video-card__title a {
    transition: color 0.2s linear;
    cursor: pointer;
}

.video-card__title a:hover {
    color: rgba(0, 174, 236, 1);
}

.video-card__meta {
    display: flex;
    align-items: center;
    /* gap: 4px; */
    height: 21px;
}

.video-card__meta a {
    display: flex;
    align-items: center;
    color: #9499a0;
    gap: 4px;
    /* height: 21px; */
    cursor: pointer;
    transition: color 0.2s ease;
}

.video-card__meta a:hover {
    color: rgba(0, 174, 236, 1);
}

.video-card__up-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 15px;
    /* 使用CSS mask确保可以自动变色 */
    background-color: currentColor;
    mask-size: cover;
    mask-position: center;
    mask-repeat: no-repeat;
    mask-image: url(@/assets/images/uploader_icon.png);
    -webkit-mask-size: cover;
    -webkit-mask-position: center;
    -webkit-mask-repeat: no-repeat;
    -webkit-mask-image: url(@/assets/images/uploader_icon.png);
}

.video-card__author {
    font-size: 12px;
    font-weight: 400;
}

.video-card__date {
    /* margin-left: 2px; */
    font-size: 12px;
    font-weight: 400;
}
</style>
