<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import LoginBgCarousel from '../components/LoginBgCarousel.vue'
import { loginUser } from '@/utils/userStorage'
import { getUserById, currentUser } from '@/api/index.js'
import { USER_TOKEN_KEY } from "@/constants/userSettingConstants.js";

const LOGIN_AFTER_JUMP_TIME = 1500  // 登录成功后跳转到首页的时间间隔(1.5秒)
const router = useRouter()



const formData = reactive({
    username: '',
    password: ''
})

const errorMessage = ref('')
const successMessage = ref('')
const isLoading = ref(false)

const handleLogin = () => {
    errorMessage.value = ''
    successMessage.value = ''
    
    // 验证表单
    if (!formData.username || !formData.password) {
        errorMessage.value = '请填写所有字段'
        return
    }

    isLoading.value = true
    loginUser(formData.username, formData.password).then(result => {
        console.log(result)
        isLoading.value = false
    
        if (result.success) {
            successMessage.value = '登录成功！即将跳转到首页...'
            
            // 清空表单
            formData.username = ''
            formData.password = ''
            
            setTimeout(() => {
                router.push('/home')
            }, LOGIN_AFTER_JUMP_TIME)
        } else {
            errorMessage.value = result.message
        }
    }).catch(() => {
        isLoading.value = false
        errorMessage.value = '网络错误，请稍后重试'
    })
}

function goHome() {
    router.push('/home')
}

function testBtnById() {
    getUserById(1).then(res => {
        console.log(res)
        console.log(res.data)
    })
}

function testToken() {
    router.push('/test')
}

function testBtnCurrent() {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    // console.log(JSON.parse(atob(token.split('.')[1])))
    currentUser().then(res => {
        console.log(res)
        console.log(res.data)
    })
}
</script>

<template>
    <div class="login-page">
        <LoginBgCarousel  />
        <div class="login-container">
            <div class="login-header">
                <h1 class="logo">CiliCili</h1>
                <p class="subtitle">登录享受更多功能</p>
            </div>
            
            <form @submit.prevent="handleLogin" class="login-form">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input 
                        type="text" 
                        id="username" 
                        v-model="formData.username" 
                        placeholder="请输入用户名"
                        required
                    />
                </div>
                
                <div class="form-group">
                    <label for="password">密码</label>
                    <input 
                        type="password" 
                        id="password" 
                        v-model="formData.password" 
                        placeholder="请输入密码"
                        required
                    />
                </div>
                
                <div v-if="errorMessage" class="error-message">
                    {{ errorMessage }}
                </div>
                
                <div v-if="isLoading" class="loading-message">
                    <span class="loading-spinner"></span>
                    正在登录中...
                </div>

                <div v-if="successMessage" class="success-message">
                    {{ successMessage }}
                </div>
                
                <button type="submit" class="login-btn" :disabled="isLoading">登录</button>
                <!-- <button type="submit" class="testBtnById" @click="testBtnById">后端接口测试（ById）</button> -->
                <!-- <button type="submit" @click="testToken">token测试（跳转至测试页面）</button> -->
                <!-- <button type="submit" @click="testBtnCurrent">当前用户测试</button> -->
            </form>
            <button @click="goHome" class="goHome-btn">返回首页</button>
            
            <div class="register-link">
                还没有账号？<router-link to="/register">立即注册</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
.login-page {
    width: 100%;
    min-height: 100vh;
    /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
    background-color: rgba(241, 242, 243);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.login-container {
    width: 100%;
    max-width: 420px;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 12px;
    padding: 40px 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    z-index: 1;
}

.login-header {
    text-align: center;
    margin-bottom: 32px;
}

.logo {
    font-size: 36px;
    font-weight: 600;
    color: rgba(0, 174, 236, 1);
    margin: 0 0 8px 0;
}

.subtitle {
    font-size: 14px;
    color: #9499a0;
    margin: 0;
}

.login-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.form-group label {
    font-size: 14px;
    font-weight: 500;
    color: #18191c;
}

.form-group input {
    height: 44px;
    padding: 0 16px;
    background-color: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(232, 232, 232, 0.9);
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
}

.form-group input:focus {
    border-color: rgba(0, 174, 236, 1);
}

.form-group input::placeholder {
    color: #9499a0;
}

.error-message {
    padding: 12px;
    background: #fff1f0;
    border: 1px solid #ffa39e;
    border-radius: 6px;
    color: #f5222d;
    font-size: 14px;
    text-align: center;
}

.success-message {
    padding: 12px;
    background: #f6ffed;
    border: 1px solid #b7eb8f;
    border-radius: 6px;
    color: #52c41a;
    font-size: 14px;
    text-align: center;
}

.loading-message {
    padding: 12px;
    background: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 6px;
    color: #1890ff;
    font-size: 14px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.loading-spinner {
    display: inline-block;
    width: 14px;
    height: 14px;
    border: 2px solid #91d5ff;
    border-top-color: #1890ff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.login-btn {
    height: 48px;
    background: rgba(0, 174, 236, 1);
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
}

.login-btn:hover {
    background: #00a9e1;
}

.login-btn:active {
    background: #009dd5;
}

.login-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.goHome-btn {
    width: 100%;
    height: 48px;
    margin-top: 24px;
    background: rgba(255, 255, 255, 0.80);
    color: rgba(0, 174, 236, 0.85);
    border: 2px solid rgba(0, 174, 236, 0.85);
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
}

.goHome-btn:hover {
    background: #f9f9f9;
}

.goHome-btn:active {
    background: #e8e8e8;
}

.register-link {
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #9499a0;
}

.register-link a {
    color: rgba(0, 174, 236, 1);
    text-decoration: none;
    font-weight: 500;
}

.register-link a:hover {
    text-decoration: underline;
}
</style>
