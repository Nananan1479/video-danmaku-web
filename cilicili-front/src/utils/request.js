import axios from 'axios'
import { useStaticDataStore } from '@/stores/index.js'
import router from '@/router'

// 创建 axios 实例（不与全局 axios 混淆）
const instance = axios.create({
    baseURL: import.meta.env.VITE_API_URL,   // 后端地址，从环境变量读取
    timeout: 10000,                          // 超时时间为 10 秒
})

/**
 * 请求拦截器
 * 作用：在请求发出前，自动从 localStorage 中取出 token 并添加到请求头
 * 触发时机：每次使用 instance.get/post/put/delete 等方法的瞬间
 */
instance.interceptors.request.use(
    (config) => {
        // console.log("request请求拦截器触发")
        // 从 Pinia 全局状态中读取 token 的存储键名（方便动态修改）
        const staticDataStore = useStaticDataStore()
        const tokenKey = staticDataStore.siteConfig.USER_TOKEN_KEY
        const token = localStorage.getItem(tokenKey)

        // 如果存在 token，就拼成 Bearer xxx 格式放到 Authorization 头中
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
        console.log("response响应拦截器触发")
        console.log(error)
        console.log(error.response)
        console.log(error.response.data)

        // 如果是 401 且后端明确返回未登录/未授权
        if (error.response?.status === 401) {
            const staticDataStore = useStaticDataStore()
            const tokenKey = staticDataStore.siteConfig.USER_TOKEN_KEY
            const userKey = staticDataStore.siteConfig.USER_STORAGE_KEY

            // 先取出原始 token 字符串（避免之前的变量引用错误）
            const rawToken = localStorage.getItem(tokenKey)
            if (rawToken) {
                try {
                    // 解析 token 中的 payload 部分
                    const decodedPayload = JSON.parse(atob(rawToken.split('.')[1]))
                    console.log('过期 token 内容：', decodedPayload)
                } catch (e) {
                    console.warn('解码 token 失败', e)
                }
            }

            // 清除过期信息
            localStorage.removeItem(tokenKey)
            localStorage.removeItem(userKey)

            // 跳转登录（避免重复跳转）
            if (router.currentRoute.value.path !== '/login') {
                router.push('/login')
            }

            // ★ 关键：返回一个 resolved promise，终止错误传播
            return Promise.resolve()
        }
        return Promise.reject(error)
    }
)

export default instance