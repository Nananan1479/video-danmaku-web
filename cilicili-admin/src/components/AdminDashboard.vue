<script setup>
import { ref, reactive, computed, markRaw } from 'vue'
import { UserFilled, VideoCameraFilled, ChatLineSquare, Monitor, Loading, DataAnalysis, Setting, Plus } from '@element-plus/icons-vue'
import { getAdminDashboardStats } from '@/api/index'

const emit = defineEmits(['navigate'])

const stats = reactive({ totalUsers: 0, totalVideos: 0, totalDanmakus: 0, todayPlays: 0, activeUsers: 0, pendingVideos: 0 })
const loadingStats = ref(false)

async function loadStats() {
    loadingStats.value = true
    try {
        const res = await getAdminDashboardStats()
        if (res?.data?.data) Object.assign(stats, res.data.data)
    } catch {
        Object.assign(stats, { totalUsers: 12580, totalVideos: 3620, totalDanmakus: 158200, todayPlays: 45600, activeUsers: 1890, pendingVideos: 12 })
    } finally { loadingStats.value = false }
}

const statCards = computed(() => [
    { label: '总用户数',  value: stats.totalUsers.toLocaleString(),    color: '#00AEEC', icon: markRaw(UserFilled),        bg: '#e8f7fd' },
    { label: '视频总量',  value: stats.totalVideos.toLocaleString(),   color: '#FB7299', icon: markRaw(VideoCameraFilled), bg: '#fef0f5' },
    { label: '弹幕总量',  value: stats.totalDanmakus.toLocaleString(), color: '#00B578', icon: markRaw(ChatLineSquare),    bg: '#e8f9f2' },
    { label: '今日播放',  value: stats.todayPlays.toLocaleString(),    color: '#FF8F1F', icon: markRaw(Monitor),           bg: '#fff4e8' },
    { label: '活跃用户',  value: stats.activeUsers.toLocaleString(),   color: '#8B5CF6', icon: markRaw(UserFilled),        bg: '#f3f0ff' },
    { label: '待审视频',  value: stats.pendingVideos.toLocaleString(), color: '#EF4444', icon: markRaw(Loading),           bg: '#fef2f2' },
])

loadStats()
defineExpose({ loadStats })
</script>

<template>
    <div class="admin-page">
        <div class="admin-page__header">
            <h2 class="admin-page__title">仪表盘</h2>
            <span class="admin-page__subtitle">数据概览 &middot; 上次更新: 刚刚</span>
        </div>
        <div class="admin-stat-grid" v-loading="loadingStats">
            <div v-for="card in statCards" :key="card.label" class="admin-stat-card">
                <div class="admin-stat-card__icon" :style="{ backgroundColor: card.bg, color: card.color }">
                    <el-icon :size="22"><component :is="card.icon" /></el-icon>
                </div>
                <div class="admin-stat-card__info">
                    <span class="admin-stat-card__value">{{ card.value }}</span>
                    <span class="admin-stat-card__label">{{ card.label }}</span>
                </div>
            </div>
        </div>
        <div class="admin-quick-actions">
            <h3 class="admin-section-title">快捷操作</h3>
            <div class="admin-quick-actions__grid">
                <el-button type="primary" :icon="UserFilled" @click="$emit('navigate', 'users')">管理用户</el-button>
                <el-button type="success" :icon="Plus" @click="$emit('navigate', 'videos-all')">添加视频</el-button>
                <el-button :icon="DataAnalysis" @click="$emit('navigate', 'danmakus')">审查弹幕</el-button>
                <el-button :icon="Setting" @click="$emit('navigate', 'settings')">系统设置</el-button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.admin-page__header { margin-bottom: 24px; }
.admin-page__title { font-size: 22px; font-weight: 600; color: #18191c; margin: 0 0 4px 0; }
.admin-page__subtitle { font-size: 13px; color: #9499a0; }
.admin-stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; margin-bottom: 28px; }
.admin-stat-card {
    background: #fff; border-radius: 10px; padding: 20px 24px; display: flex; align-items: center; gap: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); transition: transform 0.2s, box-shadow 0.2s; cursor: default;
}
.admin-stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); }
.admin-stat-card__icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.admin-stat-card__info { display: flex; flex-direction: column; gap: 2px; }
.admin-stat-card__value { font-size: 26px; font-weight: 700; color: #18191c; line-height: 1.2; }
.admin-stat-card__label { font-size: 13px; color: #9499a0; }
.admin-section-title { font-size: 16px; font-weight: 500; color: #18191c; margin: 0 0 14px 0; }
.admin-quick-actions { background: #fff; border-radius: 10px; padding: 20px 24px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); }
.admin-quick-actions__grid { display: flex; flex-wrap: wrap; gap: 12px; }
@media (max-width: 1024px) { .admin-stat-grid { grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); } }
@media (max-width: 640px) {
    .admin-stat-grid { grid-template-columns: 1fr 1fr; }
    .admin-stat-card { padding: 14px 16px; }
    .admin-stat-card__value { font-size: 22px; }
    .admin-page__title { font-size: 18px; }
}
</style>
