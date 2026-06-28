<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Refresh, Plus, VideoCameraFilled } from '@element-plus/icons-vue'
import { getAdminVideoList, updateAdminVideoStatus, deleteAdminVideo, getAdminVideoSignedUrl } from '@/api/index'

/** 当前播放视频的 OSS 签名 URL */
const signedVideoUrl = ref('')
/** 视频元素引用 */
const videoRef = ref(null)
/** 签名续期标记 */
const renewingUrl = ref(false)

// status: 1=正常, 0=下架, 2=审核中
const STATUS = { NORMAL: 1, REMOVED: 0, PENDING: 2 }

function getStatusLabel(status) {
    const map = { [STATUS.NORMAL]: '正常', [STATUS.REMOVED]: '已下架', [STATUS.PENDING]: '审核中' }
    return map[status] || '未知'
}
function getStatusType(status) {
    const map = { [STATUS.NORMAL]: 'success', [STATUS.REMOVED]: 'danger', [STATUS.PENDING]: 'warning' }
    return map[status] || 'info'
}

const videoList = ref([])
const videoLoading = ref(false)
const videoSearch = ref('')
const videoStatusFilter = ref('all')
const videoDetailVisible = ref(false)
const currentVideo = reactive({ id: null, title: '', description: '', duration: 0, playCount: 0, danmakuCount: 0, likeCount: 0, status: 1, createdAt: '' })

const pendingCount = computed(() => videoList.value.filter(v => Number(v.status) === STATUS.PENDING).length)

const filteredVideos = computed(() => {
    let list = videoList.value
    if (videoStatusFilter.value === 'pending') list = list.filter(v => Number(v.status) === STATUS.PENDING)
    if (videoSearch.value) { const kw = videoSearch.value.toLowerCase(); list = list.filter(v => v.title?.toLowerCase().includes(kw) || v.description?.toLowerCase().includes(kw)) }
    return list
})

function formatDuration(sec) { const h = Math.floor(sec / 3600), m = Math.floor((sec % 3600) / 60), s = sec % 60; if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`; return `${m}:${String(s).padStart(2, '0')}` }
function formatCount(n) { if (n >= 10000) return (n / 10000).toFixed(1) + '万'; return n?.toLocaleString() ?? '0' }

async function loadVideos() {
    videoLoading.value = true
    try {
        const statusParam = videoStatusFilter.value === 'pending' ? { status: STATUS.PENDING } : {}
        const res = await getAdminVideoList(statusParam)
        if (res?.data?.code === 200 && Array.isArray(res.data.data)) { videoList.value = res.data.data; videoLoading.value = false; return }
        if (res?.data?.code === 403) ElMessage.warning('无管理员权限，请重新登录')
    } catch (e) { console.error('加载视频列表失败:', e) }
    if (videoList.value.length === 0) {
        videoList.value = [
            { id: 1001, title: '【4K】星际穿越', description: '科幻片段', playCount: 128000, danmakuCount: 5600, likeCount: 32000, duration: 185, status: 1, createdAt: '2025-06-01' },
            { id: 1002, title: '测试视频-审核中', description: '审核中视频', playCount: 0, danmakuCount: 0, likeCount: 0, duration: 60, status: 2, createdAt: '2025-06-08' },
            { id: 1003, title: '已下架视频', description: '被下架', playCount: 500, danmakuCount: 20, likeCount: 100, duration: 120, status: 0, createdAt: '2025-05-20' },
        ]
    }
    videoLoading.value = false
}

async function openVideoDetail(row) {
    Object.assign(currentVideo, { id: row.id, title: row.title, description: row.description, duration: row.duration, playCount: row.playCount, danmakuCount: row.danmakuCount, likeCount: row.likeCount, status: row.status, createdAt: row.createdAt })
    videoDetailVisible.value = true
    await refreshSignedUrl()
}

/** 获取/续期 OSS 签名 URL */
async function refreshSignedUrl() {
    if (renewingUrl.value) return
    renewingUrl.value = true
    try {
        const res = await getAdminVideoSignedUrl(currentVideo.id)
        if (res?.data?.code === 200) {
            signedVideoUrl.value = res.data.data
        }
    } catch (e) { console.error('获取签名URL失败', e) } finally {
        renewingUrl.value = false
    }
}

/** 签名过期时自动续期，保持播放进度 */
async function handleVideoError() {
    if (!currentVideo.id || renewingUrl.value) return
    // 保存当前播放位置
    const video = videoRef.value
    const savedTime = video?.currentTime || 0
    const wasPlaying = video && !video.paused
    // 续期签名
    await refreshSignedUrl()
    if (signedVideoUrl.value && videoRef.value) {
        // 恢复播放位置
        videoRef.value.addEventListener('loadedmetadata', () => {
            videoRef.value.currentTime = savedTime
            if (wasPlaying) videoRef.value.play().catch(() => {})
        }, { once: true })
        videoRef.value.load()
    }
}

async function handleDeleteVideo(row) {
    await ElMessageBox.confirm(`确定要删除视频 "${row.title}" 吗？此操作不可恢复。`, '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
    try {
        const res = await deleteAdminVideo(row.id)
        if (res?.data?.code === 200) { videoList.value = videoList.value.filter(v => v.id !== row.id); ElMessage.success('视频已删除') }
        else ElMessage.error(res?.data?.message || '删除失败')
    } catch { ElMessage.error('网络错误，删除失败') }
}

async function handleAuditVideo(row, approve) {
    const newStatus = approve ? STATUS.NORMAL : STATUS.REMOVED
    const action = approve ? '审核通过' : '驳回'
    await ElMessageBox.confirm(approve ? `确定要让 "${row.title}" 通过审核吗？` : `确定要驳回视频 "${row.title}" 吗？`, action, { type: 'warning', confirmButtonText: action, cancelButtonText: '取消' })
    try {
        const res = await updateAdminVideoStatus(row.id, newStatus)
        if (res?.data?.code === 200) {
            row.status = newStatus
            ElMessage.success(approve ? '视频已通过审核并发布' : '视频已驳回并下架')
        } else ElMessage.error(res?.data?.message || '操作失败')
    } catch { ElMessage.error('网络错误，操作失败') }
}

function setFilter(val) { videoStatusFilter.value = val; loadVideos() }

onMounted(loadVideos)
defineExpose({ loadVideos, setFilter, pendingCount })
</script>

<template>
    <div class="admin-page">
        <div class="admin-page__header">
            <h2 class="admin-page__title">视频管理</h2>
            <span class="admin-page__subtitle">共 {{ filteredVideos.length }} 个视频</span>
        </div>
        <div class="admin-toolbar">
            <el-input v-model="videoSearch" placeholder="搜索视频标题 / 描述..." :prefix-icon="Search" clearable class="admin-toolbar__search" />
            <el-button :icon="Refresh" @click="loadVideos">刷新列表</el-button>
        </div>
        <el-table :data="filteredVideos" v-loading="videoLoading" stripe class="admin-table" empty-text="暂无视频数据">
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="duration" label="时长" width="80" align="center">
                <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
            </el-table-column>
            <el-table-column prop="playCount" label="播放量" width="90" align="center">
                <template #default="{ row }">{{ formatCount(row.playCount) }}</template>
            </el-table-column>
            <el-table-column prop="danmakuCount" label="弹幕数" width="80" align="center">
                <template #default="{ row }">{{ formatCount(row.danmakuCount) }}</template>
            </el-table-column>
            <el-table-column prop="likeCount" label="点赞数" width="80" align="center">
                <template #default="{ row }">{{ formatCount(row.likeCount) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
                <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)" size="small" effect="plain">{{ getStatusLabel(row.status) }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传日期" width="100" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="openVideoDetail(row)">详情</el-button>
                    <el-button link type="danger" :icon="Delete" size="small" @click="handleDeleteVideo(row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 视频详情弹窗 -->
        <el-dialog v-model="videoDetailVisible" title="视频详情" width="900px" destroy-on-close top="3vh">
            <div class="video-detail-layout">
                <div class="video-detail-layout__player">
                    <video v-if="signedVideoUrl" ref="videoRef" class="video-detail-layout__video" :src="signedVideoUrl" controls preload="metadata" @error="handleVideoError">您的浏览器不支持视频播放</video>
                    <div v-else-if="currentVideo.id" class="video-detail-layout__placeholder"><span>加载视频中...</span></div>
                    <div v-else class="video-detail-layout__placeholder"><el-icon :size="48"><VideoCameraFilled /></el-icon><span>暂无视频</span></div>
                </div>
                <div class="video-detail-layout__info">
                    <h3 class="video-detail-layout__title">{{ currentVideo.title }}</h3>
                    <el-descriptions :column="1" size="small" class="video-detail-layout__desc">
                        <el-descriptions-item label="ID">{{ currentVideo.id }}</el-descriptions-item>
                        <el-descriptions-item label="状态">
                            <el-tag :type="getStatusType(currentVideo.status)" size="small" effect="plain">{{ getStatusLabel(currentVideo.status) }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="时长">{{ formatDuration(currentVideo.duration) }}</el-descriptions-item>
                        <el-descriptions-item v-if="Number(currentVideo.status) !== STATUS.PENDING" label="播放量">{{ formatCount(currentVideo.playCount) }}</el-descriptions-item>
                        <el-descriptions-item v-if="Number(currentVideo.status) !== STATUS.PENDING" label="弹幕数">{{ formatCount(currentVideo.danmakuCount) }}</el-descriptions-item>
                        <el-descriptions-item v-if="Number(currentVideo.status) !== STATUS.PENDING" label="点赞数">{{ formatCount(currentVideo.likeCount) }}</el-descriptions-item>
                        <el-descriptions-item label="上传日期">{{ currentVideo.createdAt || '--' }}</el-descriptions-item>
                        <el-descriptions-item label="描述"><p class="video-detail-layout__desc-text">{{ currentVideo.description || '--' }}</p></el-descriptions-item>
                    </el-descriptions>
                </div>
            </div>
            <div class="video-detail-actions">
                <el-divider style="margin: 16px 0" />
                <div class="video-detail-actions__row">
                    <span class="video-detail-actions__label">审核操作：</span>
                    <div class="video-detail-actions__btns">
                        <!-- 审核中 → 可执行通过/驳回 -->
                        <template v-if="Number(currentVideo.status) === STATUS.PENDING">
                            <el-button type="success" size="large" @click="handleAuditVideo(currentVideo, true); videoDetailVisible = false"><el-icon><Plus /></el-icon> 审核通过</el-button>
                            <el-button type="warning" size="large" @click="handleAuditVideo(currentVideo, false); videoDetailVisible = false">驳回</el-button>
                        </template>
                        <!-- 已下架 → 可重新上架 -->
                        <template v-if="Number(currentVideo.status) === STATUS.REMOVED">
                            <el-button type="success" size="large" @click="handleAuditVideo(currentVideo, true); videoDetailVisible = false"><el-icon><Plus /></el-icon> 重新上架</el-button>
                        </template>
                        <el-button type="danger" size="large" plain @click="handleDeleteVideo(currentVideo); videoDetailVisible = false"><el-icon><Delete /></el-icon> 删除视频</el-button>
                    </div>
                </div>
            </div>
            <template #footer><el-button @click="videoDetailVisible = false">关闭</el-button></template>
        </el-dialog>
    </div>
</template>

<style scoped>
.admin-page__header { margin-bottom: 24px; }
.admin-page__title { font-size: 22px; font-weight: 600; color: #18191c; margin: 0 0 4px 0; }
.admin-page__subtitle { font-size: 13px; color: #9499a0; }
.admin-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; gap: 12px; }
.admin-toolbar__search { width: 280px; }
.admin-table { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); }
.video-detail-layout { display: flex; gap: 20px; }
.video-detail-layout__player { width: 480px; flex-shrink: 0; background: #000; border-radius: 8px; overflow: hidden; min-height: 270px; display: flex; align-items: center; justify-content: center; }
.video-detail-layout__video { width: 100%; height: 100%; display: block; max-height: 360px; }
.video-detail-layout__placeholder { display: flex; flex-direction: column; align-items: center; gap: 12px; color: #666; font-size: 14px; }
.video-detail-layout__info { flex: 1; min-width: 0; }
.video-detail-layout__title { font-size: 16px; font-weight: 600; color: #18191c; margin: 0 0 12px 0; line-height: 1.4; }
.video-detail-layout__desc-text { color: #61666d; font-size: 13px; line-height: 1.5; 
    white-space: pre-wrap; }
.video-detail-actions__row { display: flex; align-items: center; gap: 12px; }
.video-detail-actions__label { font-size: 14px; font-weight: 500; color: #18191c; white-space: nowrap; }
.video-detail-actions__btns { display: flex; flex-wrap: wrap; gap: 10px; }
@media (max-width: 1024px) { .admin-toolbar { flex-direction: column; align-items: stretch; } .admin-toolbar__search { width: 100%; } }
@media (max-width: 768px) { .video-detail-layout { flex-direction: column; } .video-detail-layout__player { width: 100%; min-height: 200px; } }
@media (max-width: 640px) { .admin-page__title { font-size: 18px; } }
</style>
