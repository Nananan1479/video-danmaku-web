import { ref } from 'vue'

/**
 * 当前登录用户的响应式引用
 * 独立文件以避免循环依赖（request.js ↔ userStorage.js ↔ api/index.js）
 */
export const currentUserRef = ref(null)
