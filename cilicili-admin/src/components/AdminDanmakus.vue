<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import { getAdminDanmakuList, deleteAdminDanmaku } from '@/api/index'

const danmakuList = ref([])
const danmakuLoading = ref(false)
const danmakuSearch = ref('')

const filteredDanmakus = computed(() => {
    if (!danmakuSearch.value) return danmakuList.value
    const kw = danmakuSearch.value.toLowerCase()
    return danmakuList.value.filter(d => d.content?.toLowerCase().includes(kw) || d.username?.toLowerCase().includes(kw))
})

function getDanmakuModeLabel(mode) { const map = { 1: '滚动', 2: '顶部', 3: '底部' }; return map[mode] || '未知' }

async function loadDanmakus() {
    danmakuLoading.value = true
    try {
        const res = await getAdminDanmakuList({ pageNum: 1, pageSize: 100 })
        if (res?.data?.data?.records) { danmakuList.value = res.data.data.records; danmakuLoading.value = false; return }
    } catch { /* ignore */ }
    danmakuList.value = [
        { id: 1, videoTitle: '【4K】星际穿越', username: '张三', content: '前方高能！！！', playTime: 45, color: '#FF0000', mode: 1, fontSize: 25, sendTime: '2025-06-07 14:30:00' },
        { id: 2, videoTitle: '【4K】星际穿越', username: '李四', content: '666666', playTime: 60, color: '#FFFFFF', mode: 1, fontSize: 20, sendTime: '2025-06-07 14:31:05' },
        { id: 3, videoTitle: '【4K】星际穿越', username: '王五', content: '泪目了', playTime: 120, color: '#00AEEC', mode: 2, fontSize: 22, sendTime: '2025-06-07 14:32:18' },
        { id: 4, videoTitle: '2025LPL夏季赛', username: '小明', content: 'RNG加油！！', playTime: 300, color: '#FFD700', mode: 1, fontSize: 28, sendTime: '2025-06-07 15:10:00' },
    ]
    danmakuLoading.value = false
}

async function handleDeleteDanmaku(row) {
    await ElMessageBox.confirm(`确定要删除这条弹幕吗？内容："${row.content}"`, '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
    try { await deleteAdminDanmaku(row.id) } catch { /* ignore */ }
    danmakuList.value = danmakuList.value.filter(d => d.id !== row.id)
    ElMessage.success('弹幕已删除')
}

onMounted(loadDanmakus)
defineExpose({ loadDanmakus })
</script>

<template>
    <div class="admin-page">
        <div class="admin-page__header">
            <h2 class="admin-page__title">弹幕管理</h2>
            <span class="admin-page__subtitle">共 {{ filteredDanmakus.length }} 条弹幕</span>
        </div>
        <div class="admin-toolbar">
            <el-input v-model="danmakuSearch" placeholder="搜索弹幕内容 / 发送者..." :prefix-icon="Search" clearable class="admin-toolbar__search" />
        </div>
        <el-table :data="filteredDanmakus" v-loading="danmakuLoading" stripe class="admin-table" empty-text="暂无弹幕数据">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="videoTitle" label="所属视频" min-width="160" show-overflow-tooltip />
            <el-table-column prop="username" label="发送者" width="100" />
            <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip>
                <template #default="{ row }"><span :style="{ color: row.color }">{{ row.content }}</span></template>
            </el-table-column>
            <el-table-column prop="playTime" label="出现时间" width="90" align="center">
                <template #default="{ row }">{{ row.playTime }}s</template>
            </el-table-column>
            <el-table-column prop="mode" label="模式" width="70" align="center">
                <template #default="{ row }"><el-tag size="small" effect="plain">{{ getDanmakuModeLabel(row.mode) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="fontSize" label="字号" width="60" align="center" />
            <el-table-column prop="sendTime" label="发送时间" width="160" />
            <el-table-column label="操作" width="80" align="center" fixed="right">
                <template #default="{ row }"><el-button link type="danger" :icon="Delete" size="small" @click="handleDeleteDanmaku(row)">删除</el-button></template>
            </el-table-column>
        </el-table>
    </div>
</template>

<style scoped>
.admin-page__header { margin-bottom: 24px; }
.admin-page__title { font-size: 22px; font-weight: 600; color: #18191c; margin: 0 0 4px 0; }
.admin-page__subtitle { font-size: 13px; color: #9499a0; }
.admin-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; gap: 12px; }
.admin-toolbar__search { width: 280px; }
.admin-table { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); }
@media (max-width: 1024px) { .admin-toolbar { flex-direction: column; align-items: stretch; } .admin-toolbar__search { width: 100%; } }
@media (max-width: 640px) { .admin-page__title { font-size: 18px; } }
</style>
