import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()],
    define:{
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__:'true'
    },
    server:{
        //服务器端口号
        port:8000,
        
    },
    resolve:{
        alias:{
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})
