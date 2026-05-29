// 用户数据存储模块
// 使用 localStorage 存储用户信息
import { login, getUserById } from '@/api/index.js'
import { register } from '../api/index.js'
import { useStaticDataStore } from '@/stores/index.js'
import { USER_STORAGE_KEY, USER_TOKEN_KEY } from "@/constants/userSettingConstants.js";


// 获取所有用户数据
export const getAllUsers = () => {

    const users = localStorage.getItem(USER_STORAGE_KEY)
    return users ? JSON.parse(users) : []
}

// 保存用户数据
export const saveUsers = (users,token) => {
    // console.log("users:",users,"token:",token)
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(users))
    localStorage.setItem(USER_TOKEN_KEY, token)
}

// 注册新用户
export const registerUser = (userData) => {
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(userData.phone)) {
        return Promise.resolve({ success: false, message: '请输入正确的手机号' });
    }
    // TODO 此处需加密密码，避免明文存储密码
    return register(userData.username, userData.phone, userData.password).then(res => {
        // console.log(res)
        if (res.data.code == 200) {
            return { success: true, message: '注册成功', user: res.data.data }
        }
        else {
            return { success: false, message: res.data.code + ': ' + res.data.message }
        }
    })
}

// 用户登录
export const loginUser = (username, password) => {

    return login(username, password).then(res => {
        // console.log("res:", res)
        if (res.data.code == 200) {
            // 保存当前登录用户信息和登录凭证（token）
            saveUsers(res.data.data,res.data.token)
            // 登录成功后，将用户信息保存到 localStorage
            return { success: true, message: '登录成功', user: res.data.data }
        }
        else {
            return { success: false, message: res.data.code + ': ' + res.data.message }
        }
    })
}

// 获取当前登录用户
export const getCurrentUser = () => {
    const user = localStorage.getItem(USER_STORAGE_KEY)
    return user ? JSON.parse(user) : null
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
    localStorage.removeItem(USER_STORAGE_KEY)
    localStorage.removeItem(USER_TOKEN_KEY)
}

// 检查用户是否已登录
export const isLoggedIn = () => {
    return getCurrentUser() !== null
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