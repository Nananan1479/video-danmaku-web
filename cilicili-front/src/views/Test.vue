<script setup>
    import { getCurrentInstance } from 'vue';
    window.app = getCurrentInstance();
</script>

<template>
    <div>
        <h1>Test</h1>
    </div>
</template>

<style scoped>
    h1 {
        color: red;
    }
</style>
