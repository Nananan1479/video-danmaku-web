import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()],
    define:{
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__:'true',
    },
    server:{
        // dev 模式仅本机访问
        port:8000,
        host: 'localhost',
    },
    preview:{
        // serve 模式允许局域网其他设备访问
        port:8000,
        host: '0.0.0.0',
    },
    resolve:{
        alias:{
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})
