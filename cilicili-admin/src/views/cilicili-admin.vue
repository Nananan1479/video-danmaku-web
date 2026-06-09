<script setup>
import { ref, computed, markRaw } from 'vue'
import { HomeFilled, Refresh, DataAnalysis, UserFilled, VideoCameraFilled, ChatLineSquare, Setting } from '@element-plus/icons-vue'
import AdminSidebar from '@/components/AdminSidebar.vue'
import AdminDashboard from '@/components/AdminDashboard.vue'
import AdminUsers from '@/components/AdminUsers.vue'
import AdminVideos from '@/components/AdminVideos.vue'
import AdminDanmakus from '@/components/AdminDanmakus.vue'
import AdminSettings from '@/components/AdminSettings.vue'

// ==================== 侧边栏 & 导航 ====================
const isCollapsed = ref(false)
const activeMenu = ref('dashboard')
const expandedMenu = ref('')
const videoPendingCount = ref(0)

const menuItems = [
    { key: 'dashboard', label: '仪表盘',   icon: markRaw(DataAnalysis) },
    { key: 'users',     label: '用户管理', icon: markRaw(UserFilled) },
    { key: 'videos',    label: '视频管理', icon: markRaw(VideoCameraFilled),
        children: [
            { key: 'videos-all',     label: '全部视频' },
            { key: 'videos-pending', label: '待审核' },
        ]
    },
    { key: 'danmakus',  label: '弹幕管理', icon: markRaw(ChatLineSquare) },
    { key: 'settings',  label: '系统设置', icon: markRaw(Setting) },
]

function handleMenuClick(item) {
    if (item.children) {
        expandedMenu.value = expandedMenu.value === item.key ? '' : item.key
        if (!activeMenu.value.startsWith('videos')) activeMenu.value = item.children[0].key
    } else {
        activeMenu.value = item.key
    }
}

function handleSubMenuClick(subKey) {
    activeMenu.value = subKey
    // 通知视频组件切换筛选
    if (videoRef.value) {
        videoRef.value.setFilter(subKey === 'videos-pending' ? 'pending' : 'all')
    }
}

function handleNavigate(key) {
    activeMenu.value = key
}

const pageTitle = computed(() => {
    for (const m of menuItems) {
        if (m.key === activeMenu.value) return m.label
        if (m.children) {
            const found = m.children.find(c => c.key === activeMenu.value)
            if (found) return m.label + ' / ' + found.label
        }
    }
    return ''
})

// ==================== 组件引用 ====================
const dashboardRef = ref(null)
const usersRef = ref(null)
const videoRef = ref(null)
const danmakuRef = ref(null)
const settingsRef = ref(null)

function handleRefresh() {
    const map = { dashboard: dashboardRef, users: usersRef, danmakus: danmakuRef, settings: settingsRef }
    const key = activeMenu.value.startsWith('videos') ? 'videos' : activeMenu.value
    if (key === 'videos' && videoRef.value) videoRef.value.loadVideos()
    else if (map[key]?.value) map[key].value.loadUsers?.() || map[key].value.loadDanmakus?.() || map[key].value.loadSettings?.() || map[key].value.loadStats?.()
}

function onVideosLoaded(count) { videoPendingCount.value = count }
</script>

<template>
    <div class="admin-layout">
        <AdminSidebar
            :menu-items="menuItems"
            :active-menu="activeMenu"
            :expanded-menu="expandedMenu"
            :is-collapsed="isCollapsed"
            :pending-count="videoPendingCount"
            @menu-click="handleMenuClick"
            @sub-menu-click="handleSubMenuClick"
            @toggle-collapse="isCollapsed = !isCollapsed"
        />

        <div class="admin-main">
            <!-- 顶部栏 -->
            <header class="admin-topbar">
                <div class="admin-topbar__left">
                    <el-icon class="admin-topbar__home-icon"><HomeFilled /></el-icon>
                    <span class="admin-topbar__separator">/</span>
                    <span class="admin-topbar__title">{{ pageTitle }}</span>
                </div>
                <div class="admin-topbar__right">
                    <el-button circle :icon="Refresh" @click="handleRefresh" title="刷新数据" />
                    <el-avatar :size="36" class="admin-topbar__avatar" />
                </div>
            </header>

            <!-- 内容区：按菜单切换 -->
            <div class="admin-content">
                <AdminDashboard v-if="activeMenu === 'dashboard'" ref="dashboardRef" @navigate="handleNavigate" />
                <AdminUsers v-else-if="activeMenu === 'users'" ref="usersRef" />
                <AdminVideos v-else-if="activeMenu.startsWith('videos')" ref="videoRef" />
                <AdminDanmakus v-else-if="activeMenu === 'danmakus'" ref="danmakuRef" />
                <AdminSettings v-else-if="activeMenu === 'settings'" ref="settingsRef" />
            </div>
        </div>
    </div>
</template>

<style scoped>
/* ==================== 布局 ==================== */
.admin-layout { display: flex; width: 100%; min-height: 100vh; background: #f1f2f3; }

/* ==================== 主内容区 ==================== */
.admin-main { flex: 1; min-width: 0; display: flex; flex-direction: column; }

/* ==================== 顶部栏 ==================== */
.admin-topbar {
    height: 56px; background: #fff; display: flex; align-items: center; justify-content: space-between;
    padding: 0 24px; border-bottom: 1px solid #e8e8e8; position: sticky; top: 0; z-index: 50;
}
.admin-topbar__left { display: flex; align-items: center; gap: 8px; }
.admin-topbar__home-icon { font-size: 18px; color: #00AEEC; }
.admin-topbar__separator { color: #c0c4cc; font-size: 15px; }
.admin-topbar__title { font-size: 15px; font-weight: 500; color: #18191c; }
.admin-topbar__right { display: flex; align-items: center; gap: 12px; }
.admin-topbar__avatar { cursor: pointer; border: 2px solid #e8e8e8; transition: border-color 0.2s; }
.admin-topbar__avatar:hover { border-color: #00AEEC; }

/* ==================== 内容区 ==================== */
.admin-content { flex: 1; padding: 24px; overflow-y: auto; }

@media (max-width: 640px) { .admin-content { padding: 16px; } }
</style>
