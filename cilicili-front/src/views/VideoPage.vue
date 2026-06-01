<script setup>
import FloatBanner from '../components/FloatBanner.vue'
import VideoPageLeft from '../components/VideoPageLeft.vue'
import VideoPageRight from '../components/VideoPageRight.vue'
</script>

<template>
    <FloatBanner />
    <!-- 滚动容器 -->
    <div class="scroll-container">
        <div class="video-page">
            <div class="video-page-content">
                <VideoPageLeft class="video-page-left" />
                <VideoPageRight class="video-page-right" />
            </div>
        </div>
    </div>
</template>

<style scoped>
    /* 滚动容器 */
    .scroll-container {
        width: 100%;
        height: auto;
        align-items: center;
        overflow-y: auto;
        overflow-x: auto;
    }

    .video-page {
        width: 100%;
        min-height: 100vh;
        background: #f6f7f8;
        padding-top: 60px; /* 避开顶部固定导航 */
    }

    .video-page-content {
        width: 100%;
        display: flex;
        justify-content: center;
        background-color: #fff;
        gap: 30px;
        /* max-width: 1500px; */
        padding: 26px 61px;
        min-height: 1200px; /* 确保内容足够长以显示滚动条 */
    }

    .video-page-left {
        width: 70%;
    }

    .video-page-right {
        width: 30%;
    }

    /* 响应式：窄屏时左右堆叠 */
    @media (max-width: 1200px) {
        .video-page-content {
            flex-direction: column;
            align-items: center;
        }
    }

    @media (max-width: 1500px) {
        .video-page-content {
            padding: 26px 61px;
        }
    }
    @media (min-width: 1500px) and (max-width: 1800px) {
        .video-page-content {
            padding: 26px 151px;
        }
    }
    @media (min-width: 1800px) and (max-width: 2000px) {
        .video-page-content {
            padding: 26px 201px;
        }
    }
</style>