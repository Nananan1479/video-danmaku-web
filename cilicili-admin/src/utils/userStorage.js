import { adminLogin } from '@/api/index.js'
import { USER_TOKEN_KEY, USER_STORAGE_KEY } from '@/constants/userSettingConstants.js'

/**
 * 管理员登录（后端已校验 role=1，此处直接保存 token）
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function loginUser(username, password) {
    try {
        const res = await adminLogin(username, password)
        const body = res.data

        if (body.code === 200) {
            localStorage.setItem(USER_TOKEN_KEY, body.token)
            localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(body.data))
            return { success: true, message: '登录成功' }
        }

        return { success: false, message: body.message || '用户名或密码错误' }
    } catch (e) {
        const msg = e.response?.data?.message || '网络错误，请稍后重试'
        return { success: false, message: msg }
    }
}

/**
 * 获取当前登录用户信息
 * @returns {object|null}
 */
export function getCurrentUser() {
    try {
        const raw = localStorage.getItem(USER_STORAGE_KEY)
        return raw ? JSON.parse(raw) : null
    } catch {
        return null
    }
}

/**
 * 退出登录
 */
export function logout() {
    localStorage.removeItem(USER_TOKEN_KEY)
    localStorage.removeItem(USER_STORAGE_KEY)
}
