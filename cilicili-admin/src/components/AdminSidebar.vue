<script setup>
import { Fold, Expand } from '@element-plus/icons-vue'

const props = defineProps({
    menuItems: { type: Array, required: true },
    activeMenu: { type: String, required: true },
    expandedMenu: { type: String, default: '' },
    isCollapsed: { type: Boolean, default: false },
    pendingCount: { type: Number, default: 0 },
})

const emit = defineEmits(['menu-click', 'sub-menu-click', 'toggle-collapse'])
</script>

<template>
    <aside class="admin-sidebar" :class="{ 'admin-sidebar--collapsed': isCollapsed }">
        <div class="admin-sidebar__header">
            <div class="admin-sidebar__logo" v-show="!isCollapsed">
                <span class="admin-sidebar__logo-icon">C</span>
                <span class="admin-sidebar__logo-text">CiliCili Admin</span>
            </div>
            <span class="admin-sidebar__logo-icon admin-sidebar__logo-icon--mini" v-show="isCollapsed">C</span>
        </div>

        <nav class="admin-sidebar__nav">
            <template v-for="item in menuItems" :key="item.key">
                <div v-if="item.children" class="admin-sidebar__submenu" :class="{ 'admin-sidebar__submenu--open': expandedMenu === item.key }">
                    <div
                        class="admin-sidebar__nav-item"
                        :class="{ 'admin-sidebar__nav-item--active': activeMenu.startsWith(item.key) }"
                        @click="$emit('menu-click', item)"
                    >
                        <el-icon class="admin-sidebar__nav-icon"><component :is="item.icon" /></el-icon>
                        <span class="admin-sidebar__nav-label" v-show="!isCollapsed">{{ item.label }}</span>
                        <el-icon v-show="!isCollapsed" class="admin-sidebar__nav-arrow" :class="{ 'admin-sidebar__nav-arrow--open': expandedMenu === item.key }">
                            <Fold />
                        </el-icon>
                    </div>
                    <div v-show="expandedMenu === item.key && !isCollapsed" class="admin-sidebar__sub-items">
                        <div
                            v-for="sub in item.children" :key="sub.key"
                            class="admin-sidebar__sub-item"
                            :class="{ 'admin-sidebar__sub-item--active': activeMenu === sub.key }"
                            @click="$emit('sub-menu-click', sub.key)"
                        >
                            {{ sub.label }}
                            <el-badge v-if="sub.key === 'videos-pending' && pendingCount > 0" :value="pendingCount" :max="99" class="admin-sidebar__badge" />
                        </div>
                    </div>
                </div>
                <div v-else
                    class="admin-sidebar__nav-item"
                    :class="{ 'admin-sidebar__nav-item--active': activeMenu === item.key }"
                    @click="$emit('menu-click', item)"
                >
                    <el-icon class="admin-sidebar__nav-icon"><component :is="item.icon" /></el-icon>
                    <span class="admin-sidebar__nav-label" v-show="!isCollapsed">{{ item.label }}</span>
                </div>
            </template>
        </nav>

        <div class="admin-sidebar__collapse" @click="$emit('toggle-collapse')">
            <el-icon class="admin-sidebar__collapse-icon">
                <Expand v-if="isCollapsed" />
                <Fold v-else />
            </el-icon>
        </div>
    </aside>
</template>

<style scoped>
.admin-sidebar {
    width: 230px; min-height: 100vh;
    background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
    display: flex; flex-direction: column;
    transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    flex-shrink: 0; position: sticky; top: 0; z-index: 100;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
}
.admin-sidebar--collapsed { width: 68px; }
.admin-sidebar__header {
    height: 64px; display: flex; align-items: center; justify-content: center;
    padding: 0 16px; border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.admin-sidebar__logo { display: flex; align-items: center; gap: 10px; }
.admin-sidebar__logo-icon {
    width: 36px; height: 36px; border-radius: 8px;
    background: linear-gradient(135deg, #00AEEC, #FB7299);
    display: flex; align-items: center; justify-content: center;
    font-size: 18px; font-weight: 700; color: #fff; flex-shrink: 0;
}
.admin-sidebar__logo-icon--mini { margin: 0 auto; }
.admin-sidebar__logo-text { font-size: 15px; font-weight: 600; color: #fff; white-space: nowrap; letter-spacing: 0.5px; }
.admin-sidebar__nav { flex: 1; padding: 12px 8px; display: flex; flex-direction: column; gap: 2px; }
.admin-sidebar__nav-item {
    display: flex; align-items: center; gap: 12px; padding: 11px 14px;
    border-radius: 8px; cursor: pointer; color: rgba(255, 255, 255, 0.6);
    transition: all 0.2s ease; user-select: none; font-size: 14px;
}
.admin-sidebar__nav-item:hover { background: rgba(255, 255, 255, 0.08); color: #fff; }
.admin-sidebar__nav-item--active {
    background: linear-gradient(135deg, rgba(0, 174, 236, 0.25), rgba(0, 174, 236, 0.1));
    color: #00AEEC; font-weight: 500;
}
.admin-sidebar__nav-icon { font-size: 20px; flex-shrink: 0; }
.admin-sidebar__nav-label { white-space: nowrap; overflow: hidden; }
.admin-sidebar__collapse {
    height: 48px; display: flex; align-items: center; justify-content: center;
    border-top: 1px solid rgba(255, 255, 255, 0.08); cursor: pointer;
    color: rgba(255, 255, 255, 0.5); transition: color 0.2s;
}
.admin-sidebar__collapse:hover { color: #fff; }
.admin-sidebar__collapse-icon { font-size: 18px; }
.admin-sidebar__submenu { overflow: hidden; }
.admin-sidebar__nav-arrow { margin-left: auto; font-size: 12px; transition: transform 0.25s; color: rgba(255, 255, 255, 0.4); }
.admin-sidebar__nav-arrow--open { transform: rotate(-90deg); }
.admin-sidebar__sub-items { padding: 2px 0 2px 28px; }
.admin-sidebar__sub-item {
    display: flex; align-items: center; padding: 8px 14px; border-radius: 6px;
    cursor: pointer; color: rgba(255, 255, 255, 0.5); font-size: 13px;
    transition: all 0.2s ease; user-select: none;
}
.admin-sidebar__sub-item:hover { color: rgba(255, 255, 255, 0.85); background: rgba(255, 255, 255, 0.05); }
.admin-sidebar__sub-item--active { color: #00AEEC; background: rgba(0, 174, 236, 0.12); font-weight: 500; }
.admin-sidebar__badge { margin-left: 6px; }
@media (max-width: 1024px) {
    .admin-sidebar { width: 68px; }
    .admin-sidebar__logo-text, .admin-sidebar__nav-label { display: none; }
}
</style>
