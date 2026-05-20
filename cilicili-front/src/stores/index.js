// stores/staticData.js
import { defineStore } from 'pinia'
import { ref, readonly } from 'vue'

export const useStaticDataStore = defineStore('staticData', () => {
    // 静态配置对象（硬编码）
    const siteConfig = {
        appName: 'CiliCili',
        version: '0.3.8',
        USER_STORAGE_KEY: 'cilicili_users',
        USER_TOKEN_KEY: 'cilicili_token',
    }

    // 静态选项列表（如性别、状态等）
    const genderOptions = ref([
        { value: 'USER_STORAGE_KEY', label: 'cilicili_users' },
        { value: 'USER_TOKEN_KEY', label: 'cilicili_token' },
        { value: 'other', label: '其他' }
    ])

    // 不想被意外修改，可以用 readonly 包装
    const readonlyConfig = readonly(siteConfig)

    // 直接返回这些静态数据
    return {
        siteConfig: readonlyConfig,   // 只读对象
        genderOptions                 // 虽然 ref 本身可变，但静态数据一般不修改
    }
})