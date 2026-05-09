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
                <VideoPageLeft />
                <VideoPageRight />
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
        display: flex;
        justify-content: center;
        background-color: #fff;
        gap: 30px;
        max-width: 1500px;
        padding: 26px 61px;
        min-height: 1200px; /* 确保内容足够长以显示滚动条 */
    }

    /* 响应式：窄屏时左右堆叠 */
    @media (max-width: 1200px) {
        .video-page-content {
            flex-direction: column;
            align-items: center;
        }
    }
</style>