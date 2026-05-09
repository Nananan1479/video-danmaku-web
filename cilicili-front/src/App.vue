<script setup>
    import { getCurrentInstance } from 'vue';
    window.app = getCurrentInstance();
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
