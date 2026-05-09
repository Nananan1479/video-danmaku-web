<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser } from '@/utils/userStorage'
import { getUserById, getUserByName } from '@/api/index.js'

const router = useRouter()

const formData = reactive({
    username: '',
    password: ''
})

const errorMessage = ref('')
const successMessage = ref('')

const handleLogin = () => {
    errorMessage.value = ''
    successMessage.value = ''
    
    // 验证表单
    if (!formData.username || !formData.password) {
        errorMessage.value = '请填写所有字段'
        return
    }
    
    // 调用登录函数
    const result = loginUser(formData.username, formData.password)
    
    if (result.success) {
        successMessage.value = '登录成功！即将跳转到首页...'
        
        // 清空表单
        formData.username = ''
        formData.password = ''
        
        // 1.5秒后跳转到首页
        setTimeout(() => {
            router.push('/home')
        }, 1500)
    } else {
        errorMessage.value = result.message
    }
}

function testBtnById() {
    getUserById(1).then(res => {
        console.log(res)
        console.log(res.data)
    })
}
</script>

<template>
    <div class="login-page">
        <div class="login-container">
            <div class="login-header">
                <h1 class="logo">CiliCili</h1>
                <p class="subtitle">欢迎回来</p>
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
                
                <div v-if="successMessage" class="success-message">
                    {{ successMessage }}
                </div>
                
                <button type="submit" class="login-btn">登录</button>
                <button type="submit" class="testBtnById" @click="testBtnById">后端接口测试（ById）</button>
            </form>
            
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
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.login-container {
    width: 100%;
    max-width: 420px;
    background: #fff;
    border-radius: 12px;
    padding: 40px 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
    text-align: center;
    margin-bottom: 32px;
}

.logo {
    font-size: 36px;
    font-weight: 600;
    color: #00a1d6;
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
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
}

.form-group input:focus {
    border-color: #00a1d6;
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

.login-btn {
    height: 48px;
    background: #00a1d6;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
}

.login-btn:hover {
    background: #0095c8;
}

.login-btn:active {
    background: #0089ba;
}

.register-link {
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #9499a0;
}

.register-link a {
    color: #00a1d6;
    text-decoration: none;
    font-weight: 500;
}

.register-link a:hover {
    text-decoration: underline;
}
</style>
