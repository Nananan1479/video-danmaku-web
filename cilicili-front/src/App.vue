<script setup>
    import { getCurrentInstance, onMounted } from 'vue';
    import { checkToken } from '@/api/index';
    import { USER_TOKEN_KEY } from "@/constants/constants";
    import router from '@/router';
    window.app = getCurrentInstance();

    onMounted(async () => {
        const token = localStorage.getItem(USER_TOKEN_KEY)
        if (!token) return   // 未登录状态无需验证

        try {
            await checkToken(token)
            console.log('token 有效')
            router.push('/')
            // 正常，不做操作
        } catch (error) {
            // 401 表示 token 无效
            console.log('token 无效')
            router.push('/login')
            // 清除 token
            localStorage.removeItem(USER_TOKEN_KEY)
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
    }
    a {
        text-decoration: none;
    }
</style>
