<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import Carousel1 from '@/assets/images/Carousel1.png'
import Carousel2 from '@/assets/images/Carousel2.png'

    const props = defineProps({
        slides: {
            type: Array,
            default: () => []
        }
    })

    // 数据中 TitleBg 可以是：
    // 颜色字符串：'yellow'、'#ffcc00'、'rgba(253,212,85,1)'
    // 渐变字符串：'linear-gradient(to bottom, rgba(253,217,107,0) 0%, rgba(253,214,96,0.72) 16%, rgba(253,212,85,1) 33%)'
    // 任何合法的 CSS background 值
    const defaultSlides = ref([
        { id: 0, Title: '哔哩哔哩 ( ゜-゜)つロ 干杯~', Image: Carousel1, TitleBg: 'linear-gradient(to bottom,rgba(253, 217, 107, 0) 0%, rgba(253, 214, 96, 0.72) 16%, rgba(253, 212, 85, 1) 33%' },
        { id: 1, Title: '萧敬腾x星铁3周年主题曲《天生鬼才》上线', Image: Carousel2, TitleBg: 'linear-gradient(to bottom, rgba(176,251,100,0) 0%, rgba(183,253,108,0.72) 16%, rgba(176,251,100,1) 33%)' },
        { id: 2, Title: '独家120星琼等你领~', Image: Carousel1, TitleBg: 'linear-gradient(to bottom,rgba(253, 217, 107, 0) 0%, rgba(253, 214, 96, 0.72) 16%, rgba(253, 212, 85, 1) 33%' },
        { id: 3, Title: '限时福利大放送', Image: Carousel1, TitleBg: 'linear-gradient(to bottom,rgba(253, 217, 107, 0) 0%, rgba(253, 214, 96, 0.72) 16%, rgba(253, 212, 85, 1) 33%' },
        { id: 4, Title: '众多新作首次曝光', Image: Carousel2, TitleBg: 'linear-gradient(to bottom, rgba(176,251,100,0) 0%, rgba(183,253,108,0.72) 16%, rgba(176,251,100,1) 33%)' },
        { id: 5, Title: '热门连载每日更新', Image: Carousel1, TitleBg: 'linear-gradient(to bottom,rgba(253, 217, 107, 0) 0%, rgba(253, 214, 96, 0.72) 16%, rgba(253, 212, 85, 1) 33%' },
        { id: 6, Title: '原创音乐人集合', Image: Carousel2, TitleBg: 'linear-gradient(to bottom, rgba(176,251,100,0) 0%, rgba(183,253,108,0.72) 16%, rgba(176,251,100,1) 33%)' },
        { id: 7, Title: '全民舞动挑战赛', Image: Carousel2, TitleBg: 'linear-gradient(to bottom, rgba(176,251,100,0) 0%, rgba(183,253,108,0.72) 16%, rgba(176,251,100,1) 33%)' },
        { id: 8, Title: '跟着UP主吃遍全国', Image: Carousel2, TitleBg: 'linear-gradient(to bottom, rgba(176,251,100,0) 0%, rgba(183,253,108,0.72) 16%, rgba(176,251,100,1) 33%)' }
    ])

    const currentIndex = ref(0)
    const isHovering = ref(false)
    let autoPlayTimer = null

    const slidesData = computed(() => {
        if (props.slides.length > 0) return props.slides
        return defaultSlides.value
    })

    function goTo(index) {
        currentIndex.value = index
    }

    function prev() {
        currentIndex.value = (currentIndex.value - 1 + slidesData.value.length) % slidesData.value.length
    }

    function next() {
        currentIndex.value = (currentIndex.value + 1) % slidesData.value.length
    }

    function startAutoPlay() {
        stopAutoPlay()
        autoPlayTimer = setInterval(() => {
            if (!isHovering.value) {
                next()
            }
        }, 4000)
    }

    function stopAutoPlay() {
        if (autoPlayTimer) {
            clearInterval(autoPlayTimer)
            autoPlayTimer = null
        }
    }

    onMounted(() => {
        startAutoPlay()
    })

    onUnmounted(() => {
        stopAutoPlay()
    })
</script>

<template>
    <!-- 分配grid大小 -->
    <div
        class="carousel"
        @mouseenter="isHovering = true"
        @mouseleave="isHovering = false"
    >
        <div class="carousel__viewport">
            <div
                v-for="(slide, index) in slidesData"
                :key="slide.id"
                class="carousel__slide"
                :class="{ 'carousel__slide--active': index === currentIndex }"
            >
                <img
                    class="carouselImg"
                    :src="slide.Image"
                    alt="轮播图图片">
                <!-- <div class="carousel__event">
                    <h2 class="carousel__event-title">{{ slide.eventTitle }}</h2>
                    <p class="carousel__event-sub">{{ slide.eventTitle }}</p>
                </div> -->
                <div
                    class="carousel__overlay"
                    :style="{background: slide.TitleBg}"
                    >
                    <div class="carousel__content">
                        <div class="carousel__text">
                            <p class="carousel__title">
                                <span class="carousel__title-main">{{ slide.Title }}</span>
                            </p>
                        </div>
                        <!-- 轮播图指示器 -->
                        <div class="carousel__dots">
                            <span
                                v-for="(dot, dotIndex) in slidesData"
                                :key="dot.id"
                                class="carousel__dot"
                                :class="{ 'carousel__dot--active': dotIndex === currentIndex }"
                                @click="goTo(dotIndex)"
                            ></span>
                        </div>
                    </div>
                    <div class="carousel__arrow">
                        <button class="carousel__arrow_button" @click="prev">
                            <img src="../assets/images/PC160.png" alt="上一张" />
                        </button>
                        <button class="carousel__arrow_button" @click="next">
                            <img src="@/assets/images/PC16.png" alt="下一张" />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.carousel {
    width: 100%;
    height: 100%;
    grid-column: span 2;
    grid-row: span 2;
    position: relative;
    overflow: hidden;
}

.carousel__viewport {
    width: 100%;
    height: 81.3%;
    position: relative;
    border-radius: 7px;
    overflow: hidden;
}

.carousel__slide {
    position: absolute;
    inset: 0;
    opacity: 0;
    transition: opacity 0.5s ease;
}

.carousel__slide--active {
    opacity: 1;
}

/* 修复因为opacity导致按钮hover失效的bug */
.carousel__slide:not(.carousel__slide--active) {
    pointer-events: none;
}

.carouselImg {
    width: 100%;
    height: auto;
    object-fit: cover;
}

/* .carousel__event {
    position: absolute;
    left: 28px;
    top: 50%;
    transform: translateY(calc(-50% - 30px));
    z-index: 2;
}

.carousel__event-title {
    font-size: 28px;
    font-family: "Noto Sans SC-Bold", "PingFang SC", sans-serif;
    font-weight: 700;
    color: #fff;
    margin: 0 0 8px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.carousel__event-sub {
    font-size: 16px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: rgba(255, 255, 255, 0.9);
    margin: 0;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
} */

.carousel__overlay {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 117px;
    background: linear-gradient(
        to bottom,
        rgba(253, 217, 107, 0) 0%,
        rgba(253, 214, 96, 0.72) 16%,
        rgba(253, 212, 85, 1) 33%
        /* rgba(253, 212, 85, 1) 30% */
    );
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: 40px 14px 17px 20px;
}

.carousel__content {
    width: 100%;
    display: flex;
    gap: 12px;
    align-items: flex-start;
    flex-direction: column;
    justify-content: space-between;
    /* padding-left: 20px; */
}

.carousel__text {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.carousel__title {
    font-size: 20px;
    line-height: 29px;
    color: #fff;
    margin: 0;
}

.carousel__title-main {
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #fff;
}

.carousel__title-emoji {
    font-family: "Microsoft YaHei-Regular", sans-serif;
    font-weight: 400;
    color: #fff;
}

.carousel__title-sub {
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #fff;
}

.carousel__dots {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-left: 5px;
    flex-shrink: 0;
}

.carousel__dot {
    width: 12px;
    height: 12px;
    background-image: url(@/assets/images/CarouselDot.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    cursor: pointer;
    transition: transform 0.2s ease;
    flex-shrink: 0;
}

.carousel__dot--active {
    width: 20px;
    height: 20px;
}

.carousel__arrow {
    width: 66px;
    display: flex;
    gap: 12px;
    align-items: center;
    justify-content: center;
}



.carousel__arrow_button {
    /* transform: translateY(-50%); */
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background-color: rgba(255, 255, 255, 0.7);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    padding: 0;
    transition: background-color 0.2s ease;
}

.carousel__arrow_button:hover {
    background-color: rgba(255, 255, 255, 0.9);
    /* background-color: black; */
}

.carousel__arrow--prev {
    left: 8px;
}

.carousel__arrow--next {
    right: 8px;
}

.carousel__arrow_button img {
    width: 57.14%;
    height: 57.14%;
    object-fit: contain;
}
</style>
