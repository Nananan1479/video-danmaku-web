import axios from 'axios'
import { USER_TOKEN_KEY, USER_STORAGE_KEY } from '@/constants/userSettingConstants.js'
import router from '@/router'

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    timeout: 20000,
})

// 请求拦截器：自动附加 token
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

// 响应拦截器：统一处理 401
instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem(USER_TOKEN_KEY)
            localStorage.removeItem(USER_STORAGE_KEY)
            // 跳转到前台登录页
            window.location.href = '/login'
            return Promise.resolve()
        }
        return Promise.reject(error)
    }
)

export default instance
