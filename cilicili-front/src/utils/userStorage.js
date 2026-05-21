// 用户数据存储模块
// 使用 localStorage 存储用户信息
import { login } from '@/api/index.js'
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

// 用户登出
export const logoutUser = () => {
    localStorage.removeItem(USER_STORAGE_KEY)
    localStorage.removeItem(USER_TOKEN_KEY)
}

// 检查用户是否已登录
export const isLoggedIn = () => {
    return getCurrentUser() !== null
}
