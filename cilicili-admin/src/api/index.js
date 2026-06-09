import request from '@/utils/request.js'

// ================= 管理员认证 =================
/** 管理员登录（后端校验 role=1） */
export function adminLogin(username, password) {
    return request({
        url: '/api/admin/login',
        method: 'POST',
        data: { username, password }
    })
}

// ================= 用户管理 =================
/** 获取用户列表 */
export function getAdminUserList() {
    return request.get('/api/admin/users')
}
/** 封禁/解封用户 */
export function updateAdminUserStatus(id, status) {
    return request.put(`/api/admin/users/${id}/status`, { status })
}
/** 处理注销申请（approve=true 确认注销，false 拒绝） */
export function handleAdminDeleteRequest(id, approve) {
    return request.put(`/api/admin/users/${id}/delete-request`, { approve })
}
/** 硬删除用户 */
export function deleteAdminUser(id) {
    return request.delete(`/api/admin/users/${id}`)
}

// ================= 视频管理 =================
/** 获取视频列表（可传 { status } 筛选） */
export function getAdminVideoList(params) {
    return request.get('/api/admin/videos', { params })
}
/** 更新视频信息 */
export function updateAdminVideo(id, data) {
    return request.put(`/api/admin/videos/${id}`, data)
}
/** 更新视频状态（审核通过/驳回） */
export function updateAdminVideoStatus(id, status) {
    return request.put(`/api/admin/videos/${id}/status`, { status })
}
/** 删除视频 */
export function deleteAdminVideo(id) {
    return request.delete(`/api/admin/videos/${id}`)
}

// ================= 弹幕管理 =================
/** 获取弹幕列表（分页） */
export function getAdminDanmakuList(params) {
    return request.get('/api/admin/danmakus', { params })
}
/** 删除弹幕 */
export function deleteAdminDanmaku(id) {
    return request.delete(`/api/admin/danmakus/${id}`)
}

// ================= 仪表盘统计 =================
/** 获取仪表盘统计数据 */
export function getAdminDashboardStats() {
    return request.get('/api/admin/dashboard/stats')
}

// ================= 系统设置 =================
/** 获取系统设置 */
export function getAdminSettings() {
    return request.get('/api/admin/settings')
}
/** 保存系统设置 */
export function saveAdminSettings(data) {
    return request.put('/api/admin/settings', data)
}
