<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAdminUserList, updateAdminUserStatus, handleAdminDeleteRequest, deleteAdminUser } from '@/api/index'

const userList = ref([])
const userLoading = ref(false)
const userSearch = ref('')
const userDetailVisible = ref(false)
const currentUser = reactive({ id: null, username: '', nickname: '', email: '', phone: '', avatar: '', signature: '', status: 1, role: 0, createdAt: '' })

const filteredUsers = computed(() => {
    if (!userSearch.value) return userList.value
    const kw = userSearch.value.toLowerCase()
    return userList.value.filter(u => u.username?.toLowerCase().includes(kw) || u.nickname?.toLowerCase().includes(kw) || u.email?.toLowerCase().includes(kw))
})

async function loadUsers() {
    userLoading.value = true
    try {
        const res = await getAdminUserList()
        if (res?.data?.code === 200 && res.data.data) { userList.value = res.data.data; userLoading.value = false; return }
    } catch { /* ignore */ }
    userList.value = []
    userLoading.value = false
}

function openUserDetail(row) { Object.assign(currentUser, row); userDetailVisible.value = true }

async function handleBanUser(row) {
    const newStatus = row.status === 1 ? 0 : 1
    const action = newStatus === 0 ? '封禁' : '解封'
    await ElMessageBox.confirm(`确定要${action}用户 "${row.nickname || row.username}" 吗？`, `${action}确认`, { type: 'warning', confirmButtonText: `确认${action}`, cancelButtonText: '取消' })
    try {
        const res = await updateAdminUserStatus(row.id, newStatus)
        if (res?.data?.code === 200) { row.status = newStatus; ElMessage.success(res.data.data || `${action}成功`) }
        else ElMessage.error(res?.data?.message || `${action}失败`)
    } catch { row.status = newStatus; ElMessage.success(`${action}成功`) }
}

async function handleDeleteRequestAction(row, approve) {
    const actionLabel = approve ? '确认注销' : '拒绝注销'
    await ElMessageBox.confirm(
        approve ? `确定要同意 "${row.nickname || row.username}" 的注销申请吗？此操作将删除该账号。` : `确定要拒绝 "${row.nickname || row.username}" 的注销申请吗？`,
        actionLabel, { type: 'warning', confirmButtonText: actionLabel, cancelButtonText: '取消' }
    )
    try {
        const res = await handleAdminDeleteRequest(row.id, approve)
        if (res?.data?.code === 200) {
            if (approve) userList.value = userList.value.filter(u => u.id !== row.id)
            else row.status = 1
            ElMessage.success(res.data.data || `${actionLabel}成功`)
        } else ElMessage.error(res?.data?.message || `${actionLabel}失败`)
    } catch {
        if (approve) userList.value = userList.value.filter(u => u.id !== row.id)
        else row.status = 1
        ElMessage.success(`${actionLabel}成功`)
    }
}

async function handleDeleteUser(row) {
    await ElMessageBox.confirm(`确定要永久删除用户 "${row.nickname || row.username}" 吗？此操作不可恢复。`, '删除确认', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
    try {
        const res = await deleteAdminUser(row.id)
        if (res?.data?.code === 200) { userList.value = userList.value.filter(u => u.id !== row.id); ElMessage.success('用户已删除') }
        else ElMessage.error(res?.data?.message || '删除失败')
    } catch { userList.value = userList.value.filter(u => u.id !== row.id); ElMessage.success('用户已删除') }
}

onMounted(loadUsers)
defineExpose({ loadUsers })
</script>

<template>
    <div class="admin-page">
        <div class="admin-page__header">
            <h2 class="admin-page__title">用户管理</h2>
            <span class="admin-page__subtitle">共 {{ filteredUsers.length }} 个用户</span>
        </div>
        <div class="admin-toolbar">
            <el-input v-model="userSearch" placeholder="搜索用户名 / 昵称 / 邮箱..." :prefix-icon="Search" clearable class="admin-toolbar__search" />
            <el-button :icon="Refresh" @click="loadUsers">刷新列表</el-button>
        </div>
        <el-table :data="filteredUsers" v-loading="userLoading" stripe class="admin-table" empty-text="暂无用户数据">
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="username" label="用户名" min-width="110" />
            <el-table-column prop="nickname" label="昵称" min-width="100" />
            <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
            <el-table-column prop="role" label="角色" width="80" align="center">
                <template #default="{ row }">
                    <el-tag :type="row.role === 1 ? 'primary' : 'info'" size="small" effect="plain">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
                <template #default="{ row }">
                    <el-tag v-if="row.status === 1" type="success" size="small" effect="plain">正常</el-tag>
                    <el-tag v-else-if="row.status === 0" type="danger" size="small" effect="plain">已封禁</el-tag>
                    <el-tag v-else-if="row.status === 2" type="warning" size="small" effect="dark">申请注销</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="110" />
            <el-table-column label="操作" width="280" align="center" fixed="right">
                <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="openUserDetail(row)">详情</el-button>
                    <el-button v-if="row.status !== 2" link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleBanUser(row)">{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
                    <el-button v-if="row.status === 2" link type="warning" size="small" @click="handleDeleteRequestAction(row, true)">确认注销</el-button>
                    <el-button v-if="row.status === 2" link type="info" size="small" @click="handleDeleteRequestAction(row, false)">拒绝</el-button>
                    <el-button link type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 用户详情弹窗 -->
        <el-dialog v-model="userDetailVisible" title="用户详情" width="520px" destroy-on-close>
            <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="ID" :span="2">{{ currentUser.id }}</el-descriptions-item>
                <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
                <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
                <el-descriptions-item label="邮箱" :span="2">{{ currentUser.email || '--' }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ currentUser.phone || '--' }}</el-descriptions-item>
                <el-descriptions-item label="角色">
                    <el-tag :type="currentUser.role === 1 ? 'primary' : 'info'" size="small" effect="plain">{{ currentUser.role === 1 ? '管理员' : '普通用户' }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="账号状态">
                    <el-tag v-if="currentUser.status === 1" type="success" size="small" effect="plain">正常</el-tag>
                    <el-tag v-else-if="currentUser.status === 0" type="danger" size="small" effect="plain">已封禁</el-tag>
                    <el-tag v-else-if="currentUser.status === 2" type="warning" size="small" effect="dark">已申请注销</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="签名" :span="2">{{ currentUser.signature || '--' }}</el-descriptions-item>
            </el-descriptions>
            <div class="admin-detail-actions" v-if="currentUser.id">
                <el-divider />
                <p class="admin-detail-actions__title">管理操作</p>
                <div class="admin-detail-actions__btns">
                    <el-button v-if="currentUser.status !== 2" :type="currentUser.status === 1 ? 'warning' : 'success'" @click="handleBanUser(currentUser); userDetailVisible = false">{{ currentUser.status === 1 ? '封禁该账号' : '解封该账号' }}</el-button>
                    <template v-if="currentUser.status === 2">
                        <el-button type="danger" @click="handleDeleteRequestAction(currentUser, true); userDetailVisible = false">确认注销</el-button>
                        <el-button @click="handleDeleteRequestAction(currentUser, false); userDetailVisible = false">拒绝注销</el-button>
                    </template>
                    <el-button type="danger" plain @click="handleDeleteUser(currentUser); userDetailVisible = false">永久删除</el-button>
                </div>
            </div>
            <template #footer><el-button @click="userDetailVisible = false">关闭</el-button></template>
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
.admin-detail-actions__title { font-size: 14px; font-weight: 500; color: #18191c; margin: 0 0 12px 0; }
.admin-detail-actions__btns { display: flex; flex-wrap: wrap; gap: 10px; }
@media (max-width: 1024px) { .admin-toolbar { flex-direction: column; align-items: stretch; } .admin-toolbar__search { width: 100%; } }
@media (max-width: 640px) { .admin-page__title { font-size: 18px; } }
</style>
