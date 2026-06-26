import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
    base: '/admin/',
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
})
