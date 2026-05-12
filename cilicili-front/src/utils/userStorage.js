// 用户数据存储模块
// 使用 localStorage 存储用户信息
import { login } from '@/api/index.js'

const USER_STORAGE_KEY = 'cilicili_users'
const CURRENT_USER_KEY = 'cilicili_current_user'

// 获取所有用户数据
export const getAllUsers = () => {
    const users = localStorage.getItem(USER_STORAGE_KEY)
    return users ? JSON.parse(users) : []
}

// 保存所有用户数据
export const saveAllUsers = (users) => {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(users))
}

// 注册新用户
export const registerUser = (userData) => {
    const users = getAllUsers()
    
    // 检查用户名是否已存在
    const existingUser = users.find(user => user.username === userData.username)
    if (existingUser) {
        return { success: false, message: '用户名已存在' }
    }
    
    // 检查邮箱是否已存在
    const existingEmail = users.find(user => user.email === userData.email)
    if (existingEmail) {
        return { success: false, message: '邮箱已被注册' }
    }
    
    // 添加新用户
    const newUser = {
        id: Date.now(),
        userName: userData.userName,
        password: userData.password,
        email: userData.email,
        createdAt: new Date().toISOString()
    }
    
    users.push(newUser)
    saveAllUsers(users)
    
    return { success: true, message: '注册成功', user: newUser }
}

// 用户登录
export const loginUser = (username, password) => {
    // const users = getAllUsers()
    // const user = users.find(u => u.username === username && u.password === password)
    // let result = {}
    
    return login(username, password).then(res => {
        console.log(res)
        console.log(res.data.code)
        // result = res
        if (res.data.code == 200) {
            // 保存当前登录用户
            // 登录成功后，将用户信息保存到 localStorage
            // localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user))
            return { success: true, message: '登录成功', user: res.data.data }
        }
        else {
            return { success: false, message: '用户名或密码错误,'+res.data.msg }
        }
    })
    
    console.log(result)
    // return result
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
