import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
    // 加载环境变量
    const env = loadEnv(mode, process.cwd(), '')
    // 从环境变量读取 VITE_BASE_URL，默认为 '/'
    const base = env.VITE_BASE_URL || '/'

    return {
        base, // 动态设置公共路径
        plugins: [vue()],
        define: {
            __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'true',
        },
        server: {
            port: 8001,
            host: 'localhost',
        },
        preview: {
            port: 8001,
            host: '0.0.0.0',
        },
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url))
            }
        }
    }
})