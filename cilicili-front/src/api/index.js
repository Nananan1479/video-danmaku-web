import request from '@/utils/request.js'

//================= 用户相关接口 =================================
export function getUserById(id) {
    return request({
        url: `/api/users/getById/${id}`,
        method: 'POST'
    })
}
/**
 * 登录用户
 * @param {*} username 
 * @param {*} password 
 * @returns 
 */
export function login(username, password) {
    // console.log(username, password)
    return request({
        url: `/api/users/login/`,
        method: 'POST',
        data: {
            username,
            password
        }
    })
}
/**
 * 注册用户
 * @param {*} username 
 * @param {*} phone 
 * @param {*} password 
 * @returns 
 */
export function register(username, phone, password) {
    console.log(username, phone, password)
    return request({
        url: `/api/users/register/`,
        method: 'POST',
        data: {
            username,
            phone,
            password,
        }
    })
}
/**
 * 获取当前用户信息
 * @returns 
 */
export function currentUser() {
    return request({
        url: `/api/users/currentUser/`,
        method: 'POST'
    })
}
/**
 * 检查token是否有效
 * @returns 
 */
export function checkToken() {
    return request({
        url: `/api/users/check`,
        method: 'GET'
    })
}
//================= 视频相关接口 =================================
/**
 * 获取视频详情
 * @param {*} id 视频ID
 * @returns 
 */
export function getVideoInfo(id) {
    return request({
        url: `/api/videos/${id}/info`,
        method: 'GET'
    })
}
/**
 * 上传视频
 * @param {*} formData 
 * @returns 
 */
export function uploadVideo(formData) {
    return request({
        url: `/api/videos/upload/`,
        method: 'POST',
        headers: { 'Content-Type': 'multipart/form-data' },
        data: formData
    })
}
/**
 * 获取推荐视频(分页功能)
 * @param {*} params 
 * @returns 
 */
export const getRecommendVideos = (params) => {
    return request.get('/api/videos/recommend', { params })
}

/**
 * 获取视频页侧边栏相关推荐视频
 * @param {*} params { pageNum, pageSize, currentVideoId }
 * @returns 
 */
export const getRelatedVideos = (params) => {
    return request.get('/api/videos/related', { params })
}

/**
 * 获取视频封面URL（通过视频ID直接获取图片）
 * @param {*} videoId
 * @returns {string}
 */
export function getVideoCoverUrlById(videoId) {
    return `${import.meta.env.VITE_API_URL}/api/videos/${videoId}/cover`
}

/**
 * 获取用户头像URL
 * @param {*} filename
 * @returns {string}
 */
export function getAvatarUrl(filename) {
    if (!filename) return ''
    // 数据库存的可能是完整路径，也可能是纯文件名
    // 用 lastIndexOf 找最后一个 / 或 \，截取后面的纯文件名
    var i = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'))
    var name = i >= 0 ? filename.substring(i + 1) : filename
    // encodeURIComponent 防止中文或特殊字符导致 URL 无效
    return `${import.meta.env.VITE_API_URL}/api/users/avatar/${encodeURIComponent(name)}`
}

/**
 * 上传用户头像
 * @param {*} formData
 * @returns 
 */
export function uploadAvatar(formData) {
    return request({
        url: '/api/users/avatar/',
        method: 'POST',
        headers: { 'Content-Type': 'multipart/form-data' },
        data: formData
    })
}