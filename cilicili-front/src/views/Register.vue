<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerUser } from '@/utils/userStorage'
import LoginBgCarousel from '@/components/LoginBgCarousel.vue'



const router = useRouter()
const REGISTER_AFTER_JUMP_TIME = 1500  // 注册成功后跳转到首页的时间间隔(1.5秒)


const formData = reactive({
    username: '',
    phone: '',
    password: '',
    confirmPassword: ''
})

const errorMessage = ref('')
const successMessage = ref('')

const handleRegister = () => {
    errorMessage.value = ''
    successMessage.value = ''
    
    // 验证表单
    if (!formData.username || !formData.phone || !formData.password || !formData.confirmPassword) {
        errorMessage.value = '请填写所有字段'
        return
    }
    
    if (formData.password !== formData.confirmPassword) {
        errorMessage.value = '两次输入的密码不一致'
        return
    }
    
    if (formData.password.length < 6) {
        errorMessage.value = '密码长度至少为6位'
        return
    }
    
    // 调用注册函数
    registerUser({
        username: formData.username,
        phone: formData.phone,
        password: formData.password
    }).then(result => {
        if (result.success) {
        successMessage.value = '注册成功！即将跳转到登录页...'
        
        // 清空表单
        formData.username = ''
        formData.phone = ''
        formData.password = ''
        formData.confirmPassword = ''
        
        // 2秒后跳转到登录页
        setTimeout(() => {
            router.push('/login')
        }, REGISTER_AFTER_JUMP_TIME)
        } else {
            errorMessage.value = result.message
        }
    })
}

function goHome() {
    router.push('/home')
}
</script>

<template>
    <div class="register-page">
        <LoginBgCarousel />
        
        <div class="register-container">
            <div class="register-header">
                <h1 class="logo">CiliCili</h1>
                <p class="subtitle">注册账号，开启精彩之旅</p>
            </div>
            
            <form @submit.prevent="handleRegister" class="register-form">
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
                    <label for="phone">手机号</label>
                    <input 
                        type="text" 
                        id="phone" 
                        v-model="formData.phone" 
                        placeholder="请输入手机号"
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
                
                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input 
                        type="password" 
                        id="confirmPassword" 
                        v-model="formData.confirmPassword" 
                        placeholder="请再次输入密码"
                        required
                    />
                </div>
                
                <div v-if="errorMessage" class="error-message">
                    {{ errorMessage }}
                </div>
                
                <div v-if="successMessage" class="success-message">
                    {{ successMessage }}
                </div>
                
                <button type="submit" class="register-btn">注册</button>
            </form>
            <button @click="goHome" class="goHome-btn">返回首页</button>
            
            <div class="login-link">
                已有账号？<router-link to="/login">立即登录</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
.register-page {
    width: 100%;
    min-height: 100vh;
    background-color: rgba(241, 242, 243);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.register-container {
    width: 100%;
    max-width: 420px;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 12px;
    padding: 40px 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    z-index: 1;
}

.register-header {
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

.register-form {
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

.register-btn {
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

.register-btn:hover {
    background: #00a9e1;
}

.register-btn:active {
    background: #009dd5;
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

.login-link {
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #9499a0;
}

.login-link a {
    color: rgba(0, 174, 1746, 1);
    text-decoration: none;
    font-weight: 500;
}

.login-link a:hover {
    text-decoration: underline;
}
</style>
