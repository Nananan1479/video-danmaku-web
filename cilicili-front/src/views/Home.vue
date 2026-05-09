<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import HomeHeaderBanner from '@/components/HomeHeaderBanner.vue'
import HomeCenterBanner from '@/components/HomeCenterBanner.vue'
import HomeMain from '@/components/HomeMain.vue'
import FloatBanner from '@/components/FloatBanner.vue'

const isShow = ref(false)

function handleScroll() {
    isShow.value = window.scrollY > 50
}

onMounted(() => {
    window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="home-page">
    <div class="home-page__content">
      <FloatBanner v-show="isShow" />
      <div class="home-page__inner">
        <HomeHeaderBanner />
        <HomeCenterBanner />
        <HomeMain />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
}

.home-page__content {
  width: 100%;
  min-height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #fff;
}

.home-page__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
</style>
