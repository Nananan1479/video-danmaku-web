<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

import loginCarouselImg1 from '@/assets/images/login_banner_1.avif'
import loginCarouselImg2 from '@/assets/images/login_banner_2.avif'
import loginCarouselImg3 from '@/assets/images/login_banner_3.avif'
import loginCarouselImg4 from '@/assets/images/login_banner_4.avif'
import loginCarouselImg5 from '@/assets/images/login_banner_5.avif'
import loginCarouselImg6 from '@/assets/images/login_banner_6.avif'
import loginCarouselImg7 from '@/assets/images/login_banner_7.avif'
import loginCarouselImg8 from '@/assets/images/login_banner_8.avif'
import loginCarouselImg9 from '@/assets/images/login_banner_9.avif'
import loginCarouselImg10 from '@/assets/images/login_banner_10.avif'
import loginCarouselImg11 from '@/assets/images/login_banner_11.avif'
import loginCarouselImg12 from '@/assets/images/login_banner_12.avif'

const images = [
    loginCarouselImg6, loginCarouselImg2, 
    loginCarouselImg4, loginCarouselImg5, loginCarouselImg1,
    loginCarouselImg7, loginCarouselImg8, loginCarouselImg9,
    loginCarouselImg10, loginCarouselImg11, loginCarouselImg12
]

// 轮播图切换时间间隔（毫秒）
const SWITCH_TIME = 5000
const total = images.length

// 第一张图的克隆
const displayImages = [...images, images[0]]

const currentIndex = ref(0)
const transitionEnabled = ref(true)
let timer = null

function startAutoPlay() {
    timer = setInterval(() => {
        currentIndex.value++
    }, SWITCH_TIME)
}

function stopAutoPlay() {
    if (timer) {
        clearInterval(timer)
        timer = null
    }
}

// 解决轮播图循环播放问题（一直向右滑动）
// 动画结束时检测并瞬间跳回第一张图
// 浏览器可能在这一帧恢复过渡后才执行 currentIndex = 0
function handleTransitionEnd() {
    if (currentIndex.value >= total) {
        transitionEnabled.value = false
        currentIndex.value = 0
        // 跳回0 —— 同一帧，无动画
        requestAnimationFrame(() => {
            // 等浏览器绘制完跳转，确保动画生效
            requestAnimationFrame(() => {
                // 再等一帧
                transitionEnabled.value = true
                // 恢复过渡（下次轮播正常滑动）
            })
        })
    }
}

onMounted(() => {
    startAutoPlay()
})

onBeforeUnmount(() => {
    // stopAutoPlay()
})
</script>

<template>
    <div class="carousel-container">
        <div
            class="carousel-track"
            :class="{ 'no-transition': !transitionEnabled }"
            :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
            @transitionend="handleTransitionEnd"
        >
            <div
                v-for="(img, idx) in displayImages"
                :key="idx"
                class="carousel-slide"
            >
                <img :src="img" alt="轮播图" class="carousel-image">
            </div>
        </div>
    </div>
</template>

<style scoped>
.carousel-container {
    position: fixed;     /* 或 absolute，根据布局需要 */
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    overflow: hidden;    /* 隐藏轨道超出部分 */
}

.carousel-track {
    display: flex;
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease-in-out;
    will-change: transform;
}

.carousel-track.no-transition {
    transition: none;
}

.carousel-slide {
    flex-shrink: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
}

.carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
</style>