<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { USER_TOKEN_KEY } from '@/constants/userSettingConstants.js'

const router = useRouter()

onMounted(() => {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (!token && router.currentRoute.value.path !== '/login') {
        router.push('/login')
    }
})
</script>

<template>
    <router-view></router-view>
</template>

<style>
* {
    box-sizing: border-box !important;
}
body, html {
    margin: 0;
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
                 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica,
                 Arial, sans-serif;
}
a {
    text-decoration: none;
}
</style>
