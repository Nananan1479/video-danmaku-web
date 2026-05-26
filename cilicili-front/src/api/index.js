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
    return `${import.meta.env.VITE_API_URL}/api/users/avatar/${filename}`
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