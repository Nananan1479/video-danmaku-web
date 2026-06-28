import { adminLogin, currentUser as currentUserApi } from '@/api/index.js'
import { USER_TOKEN_KEY } from '@/constants/userSettingConstants.js'
import { currentAdminRef } from './userState.js'

// 重新导出 currentAdminRef，让组件可以统一从 userStorage 导入
export { currentAdminRef } from './userState.js'

// 防止并发重复请求
let pendingUserPromise = null

/**
 * 管理员登录（后端已校验 role=1，仅保存 token，用户数据存入响应式 ref）
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function loginUser(username, password) {
    try {
        const res = await adminLogin(username, password)
        const body = res.data

        if (body.code === 200) {
            // 仅保存 token 到 localStorage，不存用户数据
            localStorage.setItem(USER_TOKEN_KEY, body.token)
            // 用户数据存入响应式 ref
            currentAdminRef.value = body.data
            return { success: true, message: '登录成功' }
        }

        return { success: false, message: body.message || '用户名或密码错误' }
    } catch (e) {
        const msg = e.response?.data?.message || '网络错误，请稍后重试'
        return { success: false, message: msg }
    }
}

/**
 * 获取当前登录管理员信息（异步，从后端 API 获取）
 * 优先返回缓存的用户数据，若无缓存则请求后端
 * @returns {Promise<object|null>}
 */
export async function getCurrentUser() {
    // 已缓存则直接返回
    if (currentAdminRef.value) {
        return currentAdminRef.value
    }
    // 无 token 则未登录
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (!token) {
        return null
    }
    // 正在请求中则复用同一个 Promise
    if (pendingUserPromise) {
        return pendingUserPromise
    }
    // 请求后端获取管理员信息
    pendingUserPromise = currentUserApi().then(res => {
        if (res.data?.code === 200 && res.data.data) {
            currentAdminRef.value = res.data.data
            return currentAdminRef.value
        }
        return null
    }).catch(e => {
        console.error('获取当前管理员信息失败', e)
        return null
    }).finally(() => {
        pendingUserPromise = null
    })
    return pendingUserPromise
}

/**
 * 退出登录
 */
export function logout() {
    localStorage.removeItem(USER_TOKEN_KEY)
    localStorage.removeItem(USER_STORAGE_KEY) // 清除旧数据（兼容）
    currentAdminRef.value = null
}
