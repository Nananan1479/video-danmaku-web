// 用户数据存储模块
// 通过后端 API 获取用户信息，token 仅存储在 localStorage 中
import { login, getUserById, currentUser as currentUserApi } from '@/api/index.js'
import { register } from '../api/index.js'
import { USER_STORAGE_KEY, USER_TOKEN_KEY } from "@/constants/userSettingConstants.js";
import { currentUserRef } from './userState.js'

// 重新导出 currentUserRef，让组件可以从 userStorage 统一导入
export { currentUserRef } from './userState.js'

// 保存 token 到 localStorage
const saveToken = (token) => {
    localStorage.setItem(USER_TOKEN_KEY, token)
}

// 保存用户数据（兼容旧接口，仅保存 token 并更新缓存）
export const saveUsers = (users, token) => {
    if (token) {
        saveToken(token)
    }
    if (users) {
        currentUserRef.value = users
    }
}

// 注册新用户
export const registerUser = (userData) => {
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(userData.phone)) {
        return Promise.resolve({ success: false, message: '请输入正确的手机号' });
    }
    return register(userData.username, userData.phone, userData.password).then(res => {
        if (res.data.code == 200) {
            return { success: true, message: '注册成功', user: res.data.data }
        } else {
            return { success: false, message: res.data.code + ': ' + res.data.message }
        }
    })
}

// 用户登录
export const loginUser = (username, password) => {
    return login(username, password).then(res => {
        if (res.data.code == 200) {
            // 仅保存 token 到 localStorage，用户数据存入响应式 ref
            saveToken(res.data.token)
            currentUserRef.value = res.data.data
            return { success: true, message: '登录成功', user: res.data.data }
        } else {
            return { success: false, message: res.data.code + ': ' + res.data.message }
        }
    })
}

// 防止并发重复请求
let pendingUserPromise = null

/**
 * 获取当前登录用户（异步，从后端 API 获取）
 * 优先返回缓存的用户数据，若无缓存则请求后端
 * @returns {Promise<Object|null>} 用户数据或 null
 */
export const getCurrentUser = async () => {
    // 已缓存则直接返回
    if (currentUserRef.value) {
        return currentUserRef.value
    }
    // 无 token 则未登录
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (!token) {
        return null
    }
    // 正在请求中则复用同一个 Promise，避免并发重复请求
    if (pendingUserPromise) {
        return pendingUserPromise
    }
    // 请求后端获取用户信息
    pendingUserPromise = currentUserApi().then(res => {
        if (res.data?.code === 200 && res.data.data) {
            currentUserRef.value = res.data.data
            return currentUserRef.value
        }
        return null
    }).catch(e => {
        console.error('获取当前用户失败', e)
        return null
    }).finally(() => {
        pendingUserPromise = null
    })
    return pendingUserPromise
}

/**
 * 刷新用户缓存（强制重新从后端获取）
 */
export const refreshCurrentUser = async () => {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (!token) {
        currentUserRef.value = null
        return null
    }
    try {
        const res = await currentUserApi()
        if (res.data?.code === 200 && res.data.data) {
            currentUserRef.value = res.data.data
            return currentUserRef.value
        }
    } catch (e) {
        console.error('刷新用户信息失败', e)
    }
    return null
}

export const getCurrentUserByToken = () => {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (!token) {
        console.log('获取用户token失败')
        return null
    }
    return JSON.parse(atob(token.split('.')[1]))
}

// 用户登出
export const logoutUser = () => {
    localStorage.removeItem(USER_TOKEN_KEY)
    localStorage.removeItem(USER_STORAGE_KEY)
    currentUserRef.value = null
}

// 检查用户是否已登录（仅检查 token 是否存在）
export const isLoggedIn = () => {
    return localStorage.getItem(USER_TOKEN_KEY) !== null
}

/**
 * 根据用户ID从后端获取用户信息
 * @param {*} id 用户ID
 * @returns {Object} { username, nickname, avatar, signature, ... }
 */
export const fetchUserById = async (id) => {
    if (!id) return null
    try {
        const res = await getUserById(id)
        if (res.data) {
            return {
                id: res.data.id,
                username: res.data.username,
                nickname: res.data.nickname,
                avatar: res.data.avatar,
                signature: res.data.signature
            }
        }
        return null
    } catch (err) {
        console.error('获取用户信息失败', err)
        return null
    }
}

export const skipLogin = (router) => {
    if (isLoggedIn()) {
        router.push('/userSpace')
    } else {
        router.push('/login')
    }
}
