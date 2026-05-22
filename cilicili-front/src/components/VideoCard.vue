<script setup>
defineProps({
    video: {
        type: Object,
        required: true
    }
})

defineEmits(['click'])
</script>

<template>
    <div class="video-card" @click="$emit('click')">
        <div class="video-card__cover">
            <div class="video-card__cover-backgroundColor"></div>
            <img
                class="video-card__cover-img"
                src="@/assets/images/6ff74a8f45317f579bb358521a0aa33a917bafb9.png"
                alt=""
            />
            <div class="video-card__cover-overlay">
                <div class="video-card__stats">
                    <div class="video-card__stat">
                        <img class="video-card__stat-icon" src="@/assets/images/videoPlays_white_icon.png" alt="" />
                        <span class="video-card__stat-text">{{ video.views }}</span>
                    </div>
                    <div class="video-card__stat">
                        <img class="video-card__stat-icon" src="@/assets/images/videoComments_white_icon.png" alt="" />
                        <span class="video-card__stat-text">{{ video.comments }}</span>
                    </div>
                </div>
                <span class="video-card__duration">{{ video.duration }}</span>
            </div>
        </div>
        <div class="video-card__info">
            <h3 class="video-card__title">{{ video.title }}</h3>
            <div class="video-card__meta">
                <span class="video-card__up-badge"></span>
                <span class="video-card__author">{{ video.author }}</span>
                <span class="video-card__dot">·</span>
                <span class="video-card__date">{{ video.date }}</span>
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
    cursor: pointer;
}

.video-card__cover {
    /* height: 132px; */
    /* height: 100%; */
    width: 100%;
    background-color: #f1f2f3;
    border-radius: 7px;
    overflow: hidden;
    position: relative;
    display: flex;
    align-items: flex-end;
}

.video-card__cover-backgroundColor {
    width: 100%;
    height: 100%;
    /* position: absolute; */
    /* inset: 0; */
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 7px;
    z-index: 0;
}

.video-card__cover-img {
    /* position: absolute; */
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
    padding: 1px 10px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 24px;
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
    width: 20px;
    height: 22px;
    object-fit: cover;
}

.video-card__stat-text {
    font-size: 13px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #fff;
}

.video-card__duration {
    font-size: 13px;
    font-family: "Noto Sans SC-Medium", sans-serif;
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
    font-family: "Microsoft YaHei-Regular", sans-serif;
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

.video-card__meta {
    display: flex;
    align-items: center;
    gap: 4px;
    height: 21px;
}

.video-card__up-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 19px;
    height: 18px;
    font-size: 9px;
    font-weight: 500;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    background-image: url(@/assets/images/uploader_icon.png);
}

.video-card__author {
    font-size: 14px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #9499a0;
}

.video-card__dot {
    font-size: 11px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #9499a0;
}

.video-card__date {
    font-size: 14px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #9499a0;
}
</style>
