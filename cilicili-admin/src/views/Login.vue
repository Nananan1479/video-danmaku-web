<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { loginUser } from '@/utils/userStorage'

const router = useRouter()
const LOGIN_AFTER_JUMP_MS = 1200

const formData = reactive({ username: '', password: '' })
const errorMessage = ref('')
const successMessage = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
    errorMessage.value = ''
    successMessage.value = ''

    if (!formData.username || !formData.password) {
        errorMessage.value = '请填写所有字段'
        return
    }

    isLoading.value = true
    const result = await loginUser(formData.username, formData.password)
    isLoading.value = false

    if (result.success) {
        successMessage.value = '登录成功！正在跳转...'
        formData.username = ''
        formData.password = ''
        setTimeout(() => router.push('/admin'), LOGIN_AFTER_JUMP_MS)
    } else {
        errorMessage.value = result.message
    }
}
</script>

<template>
    <div class="login-page">
        <div class="login-page__card">
            <!-- Logo -->
            <div class="login-page__header">
                <div class="login-page__logo">
                    <span class="login-page__logo-icon">C</span>
                </div>
                <h1 class="login-page__title">CiliCili 后台管理</h1>
                <p class="login-page__subtitle">管理员登录</p>
            </div>

            <!-- 表单 -->
            <form @submit.prevent="handleLogin" class="login-page__form">
                <div class="login-page__field">
                    <label for="username">用户名</label>
                    <input
                        id="username"
                        v-model="formData.username"
                        type="text"
                        placeholder="请输入管理员账号"
                        autocomplete="username"
                        required
                    />
                </div>

                <div class="login-page__field">
                    <label for="password">密码</label>
                    <input
                        id="password"
                        v-model="formData.password"
                        type="password"
                        placeholder="请输入密码"
                        autocomplete="current-password"
                        required
                    />
                </div>

                <!-- 消息区 -->
                <div v-if="errorMessage" class="login-page__msg login-page__msg--error">
                    {{ errorMessage }}
                </div>
                <div v-if="successMessage" class="login-page__msg login-page__msg--success">
                    {{ successMessage }}
                </div>
                <div v-if="isLoading" class="login-page__msg login-page__msg--loading">
                    <span class="login-page__spinner"></span> 正在登录中...
                </div>

                <button type="submit" class="login-page__btn" :disabled="isLoading">登 录</button>
            </form>
        </div>
    </div>
</template>

<style scoped>
/* ==================== 登录页背景 ==================== */
.login-page {
    width: 100%;
    min-height: 100vh;
    background:
        linear-gradient(rgba(0, 0, 0, 0.45), rgba(0, 0, 0, 0.55)),
        url('/loginBg.jpg') center / cover no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

/* ==================== 卡片 ==================== */
.login-page__card {
    width: 100%;
    max-width: 400px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-radius: 16px;
    padding: 44px 36px 40px;
    box-shadow:
        0 8px 40px rgba(0, 0, 0, 0.25),
        0 0 0 1px rgba(255, 255, 255, 0.2) inset;
    animation: card-in 0.5s ease-out;
}

@keyframes card-in {
    from { opacity: 0; transform: translateY(24px); }
    to   { opacity: 1; transform: translateY(0); }
}

/* ==================== 头部 ==================== */
.login-page__header {
    text-align: center;
    margin-bottom: 36px;
}

.login-page__logo-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 14px;
    background: linear-gradient(135deg, #00AEEC, #FB7299);
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    box-shadow: 0 4px 16px rgba(0, 174, 236, 0.35);
    margin-bottom: 16px;
}

.login-page__title {
    font-size: 22px;
    font-weight: 600;
    color: #18191c;
    margin: 0 0 6px 0;
}

.login-page__subtitle {
    font-size: 14px;
    color: #9499a0;
    margin: 0;
}

/* ==================== 表单 ==================== */
.login-page__form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.login-page__field {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.login-page__field label {
    font-size: 14px;
    font-weight: 500;
    color: #18191c;
}

.login-page__field input {
    height: 46px;
    padding: 0 16px;
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid #e0e0e0;
    border-radius: 10px;
    font-size: 14px;
    color: #18191c;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
}

.login-page__field input:focus {
    border-color: #00AEEC;
    box-shadow: 0 0 0 3px rgba(0, 174, 236, 0.12);
}

.login-page__field input::placeholder {
    color: #9499a0;
}

/* ==================== 消息 ==================== */
.login-page__msg {
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 14px;
    text-align: center;
}

.login-page__msg--error {
    background: #fff1f0;
    border: 1px solid #ffa39e;
    color: #f5222d;
}

.login-page__msg--success {
    background: #f6ffed;
    border: 1px solid #b7eb8f;
    color: #52c41a;
}

.login-page__msg--loading {
    background: #e6f7ff;
    border: 1px solid #91d5ff;
    color: #1890ff;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.login-page__spinner {
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
    to   { transform: rotate(360deg); }
}

/* ==================== 按钮 ==================== */
.login-page__btn {
    height: 48px;
    background: linear-gradient(135deg, #00AEEC, #00c6ff);
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 2px;
    cursor: pointer;
    transition: opacity 0.2s, transform 0.15s;
    margin-top: 4px;
}

.login-page__btn:hover {
    opacity: 0.92;
}

.login-page__btn:active {
    transform: scale(0.98);
}

.login-page__btn:disabled {
    opacity: 0.55;
    cursor: not-allowed;
    transform: none;
}

/* ==================== 响应式 ==================== */
@media (max-width: 480px) {
    .login-page__card {
        padding: 32px 24px 30px;
    }

    .login-page__title {
        font-size: 20px;
    }
}
</style>
