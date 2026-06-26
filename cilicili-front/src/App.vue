<script setup>
    import { getCurrentInstance, onMounted } from 'vue';
    import { USER_TOKEN_KEY } from "@/constants/userSettingConstants.js";
    import { getCurrentUser } from '@/utils/userStorage';
    window.app = getCurrentInstance();

    onMounted(async () => {
        const token = localStorage.getItem(USER_TOKEN_KEY)
        if (!token) return   // 未登录状态无需验证

        // 初始化用户缓存（getCurrentUser 内部会调用后端验证 token）
        await getCurrentUser()
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
    }
    a {
        text-decoration: none;
    }
</style>
