// 用户数据存储模块
// 使用 localStorage 存储用户信息
import { login } from '@/api/index.js'
import { register } from '../api'

const USER_STORAGE_KEY = 'cilicili_users'
const CURRENT_USER_KEY = 'cilicili_current_user'

// 获取所有用户数据
export const getAllUsers = () => {
    const users = localStorage.getItem(USER_STORAGE_KEY)
    return users ? JSON.parse(users) : []
}

// 保存所有用户数据
export const saveUsers = (users) => {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(users))
}

// 注册新用户
export const registerUser = (userData) => {
    // const users = getAllUsers()
    
    // 检查用户名是否已存在
    // const existingUser = users.find(user => user.username === userData.username)
    // if (existingUser) {
    //     return { success: false, message: '用户名已存在' }
    // }

    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(userData.phone)) {
        // alert('请输入正确的手机号');
        return { success: false, message: '请输入正确的手机号' };
    }
    return register(userData.username, userData.phone, userData.password).then(res => {
        console.log(res)
        if (res.data.code == 200) {
            return { success: true, message: '注册成功', user: res.data.data }
        }
        else {
            return { success: false, message: res.data.code + ': ' + res.data.message }
        }
    })
    
    // 检查手机号是否已存在
    // const existingPhone = users.find(user => user.phone === userData.phone)
    // if (existingPhone) {
    //     return { success: false, message: '手机号已被注册' }
    // }
    
    // 添加新用户
    // const newUser = {
    //     id: Date.now(),
    //     username: userData.username,
    //     password: userData.password,
    //     phone: userData.phone,
    //     createdAt: new Date().toISOString()
    // }
    
    // users.push(newUser)
    // saveAllUsers(users)
    
    // return { success: true, message: '注册成功', user: newUser }
}

// 用户登录
export const loginUser = (username, password) => {
    return login(username, password).then(res => {
        console.log(res)
        // result = res
        if (res.data.code == 200) {
            // 保存当前登录用户
            saveUsers(res.data.data)
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
    const user = localStorage.getItem(CURRENT_USER_KEY)
    return user ? JSON.parse(user) : null
}

// 用户登出
export const logoutUser = () => {
    localStorage.removeItem(CURRENT_USER_KEY)
}

// 检查用户是否已登录
export const isLoggedIn = () => {
    return getCurrentUser() !== null
}
