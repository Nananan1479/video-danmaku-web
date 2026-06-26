import axios from 'axios'
import { USER_TOKEN_KEY } from "@/constants/userSettingConstants.js";
import { currentUserRef } from '@/utils/userState'
import router from '@/router'

// 创建 axios 实例（不与全局 axios 混淆）
const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL,   // 后端地址，从环境变量读取
    timeout: 20000,                          // 超时时间为 20 秒
})

/**
 * 请求拦截器
 * 作用：在请求发出前，自动从 localStorage 中取出 token 并添加到请求头
 * 触发时机：每次使用 instance.get/post/put/delete 等方法的瞬间
 */
instance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem(USER_TOKEN_KEY)
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

/**
 * 响应拦截器
 * 作用：统一处理所有接口返回，特别是 401 未登录状态
 * 触发时机：收到服务器的响应（无论成功或失败）后
 */
instance.interceptors.response.use(
    (response) => response, // 正常响应直接返回
    (error) => {
        // 如果是 401 且后端明确返回未登录/未授权
        if (error.response?.status === 401) {
            // 避免重复清除：如果 token 不存在说明已经处理过了
            const token = localStorage.getItem(USER_TOKEN_KEY)
            if (!token) return Promise.reject(error)

            // 清除过期信息
            localStorage.removeItem(USER_TOKEN_KEY)
            currentUserRef.value = null

            // 跳转登录
            if (router.currentRoute.value.path !== '/login') {
                router.push('/login')
            }

            return Promise.reject(error)
        }
        return Promise.reject(error)
    }
)

export default instance