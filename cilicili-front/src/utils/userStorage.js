// 用户数据存储模块
// 使用 localStorage 存储用户信息
import { login } from '@/api/index.js'
import { register } from '../api/index.js'
import { useStaticDataStore } from '@/stores/index.js'


// 获取所有用户数据
export const getAllUsers = () => {
    const staticDataStore = useStaticDataStore()

    const users = localStorage.getItem(staticDataStore.siteConfig.USER_STORAGE_KEY)
    return users ? JSON.parse(users) : []
}

// 保存用户数据
export const saveUsers = (users,token) => {
    const staticDataStore = useStaticDataStore()

    // console.log("users:",users,"token:",token)
    localStorage.setItem(staticDataStore.siteConfig.USER_STORAGE_KEY, JSON.stringify(users))
    localStorage.setItem(staticDataStore.siteConfig.USER_TOKEN_KEY, token)
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
    const staticDataStore = useStaticDataStore()
    
    return login(username, password).then(res => {
        // console.log("res:", res)
        if (res.data.code == 200) {
            // 保存当前登录用户信息和登录凭证（token）
            saveUsers(res.data.data,res.data.token)
            // 保存登录凭证（token）
            // localStorage.setItem('token', res.data.token)

            // localStorage.setItem(staticDataStore.siteConfig.USER_TOKEN_KEY, res.data.token)
            // console.log(res.data.token)
            // const token = localStorage.getItem(staticDataStore.siteConfig.USER_TOKEN_KEY)
            // console.log(token)
            // const payload = JSON.parse(atob(token.split('.')[1]))
            // console.log(payload)


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
    const staticDataStore = useStaticDataStore()
    const user = localStorage.getItem(staticDataStore.siteConfig.USER_STORAGE_KEY)
    return user ? JSON.parse(user) : null
}

// 用户登出
export const logoutUser = () => {
    const staticDataStore = useStaticDataStore()
    localStorage.removeItem(staticDataStore.siteConfig.USER_STORAGE_KEY)
    localStorage.removeItem(staticDataStore.siteConfig.USER_TOKEN_KEY)
}

// 检查用户是否已登录
export const isLoggedIn = () => {
    return getCurrentUser() !== null
}
