<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import FloatBanner from '@/components/FloatBanner.vue'
import { getCurrentUser, saveUsers } from '@/utils/userStorage'
import { getAvatarUrl, updateUserInfo, uploadAvatar } from '@/api/index'
import Login_default from '@/assets/images/Login_default_icon.svg'

/** 当前选中的菜单 */
const activeMenu = ref('home')

/** 侧边栏菜单项 */
const menuItems = [
    { key: 'home', label: '首页', icon: 'icon-home' },
    { key: 'info', label: '我的信息', icon: 'icon-info' },
    { key: 'avatar', label: '我的头像', icon: 'icon-avatar' }
]

// ---- 用户数据 ----
const user = reactive({
    id: null,
    username: '',
    nickname: '',
    signature: '',
    email: '',
    phone: '',
    avatar: ''
})

/** 头像地址 */
const avatarSrc = computed(() => {
    if (user.avatar) return getAvatarUrl(user.avatar)
    return Login_default
})

/** 加载当前用户数据 */
function loadUser() {
    const stored = getCurrentUser()
    if (stored) {
        user.id = stored.id || null
        user.username = stored.username || ''
        user.nickname = stored.nickname || ''
        user.signature = stored.signature || ''
        user.email = stored.email || ''
        user.phone = stored.phone || ''
        user.avatar = stored.avatar || ''
    }
}

onMounted(loadUser)

function selectMenu(key) {
    activeMenu.value = key
}

// ---- 我的信息表单 ----
const infoForm = reactive({
    nickname: '',
    signature: '',
    phone: '',
    email: ''
})
const infoSaving = ref(false)

// 进入"我的信息"时预填表单
function initInfoForm() {
    infoForm.nickname = user.nickname
    infoForm.signature = user.signature
    infoForm.phone = user.phone
    infoForm.email = user.email
}

async function saveInfo() {
    // 只校验有填写的字段格式，空字段不参与校验也不发送
    if (infoForm.phone.trim() && !/^1[3-9]\d{9}$/.test(infoForm.phone.trim())) {
        return ElMessage.warning('手机号格式不正确')
    }
    if (infoForm.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(infoForm.email.trim())) {
        return ElMessage.warning('邮箱格式不正确')
    }

    // 只把非空字段放进请求，空字段不会传到后端，数据库不会变更
    const data = {}
    if (infoForm.nickname.trim()) data.nickname = infoForm.nickname.trim()
    if (infoForm.signature.trim()) data.signature = infoForm.signature.trim()
    if (infoForm.phone.trim()) data.phone = infoForm.phone.trim()
    if (infoForm.email.trim()) data.email = infoForm.email.trim()

    if (Object.keys(data).length === 0) {
        return ElMessage.warning('请至少填写一项信息')
    }

    infoSaving.value = true
    try {
        const res = await updateUserInfo(data)
        if (res.data.code === 200) {
            // 仅更新本次提交的字段到本地
            if (data.nickname !== undefined) user.nickname = data.nickname
            if (data.signature !== undefined) user.signature = data.signature
            if (data.phone !== undefined) user.phone = data.phone
            if (data.email !== undefined) user.email = data.email
            // 同步到 localStorage
            const stored = getCurrentUser()
            if (stored) {
                if (data.nickname !== undefined) stored.nickname = data.nickname
                if (data.signature !== undefined) stored.signature = data.signature
                if (data.phone !== undefined) stored.phone = data.phone
                if (data.email !== undefined) stored.email = data.email
                saveUsers(stored, localStorage.getItem('userToken'))
            }
            ElMessage.success('保存成功')
        } else {
            ElMessage.error(res.data.message || '保存失败')
        }
    } catch (err) {
        console.error('保存用户信息失败', err)
        ElMessage.error('保存失败，请重试')
    } finally {
        infoSaving.value = false
    }
}

// ---- 头像上传 ----
const avatarUploading = ref(false)
const fileInputRef = ref(null)

function triggerUpload() {
    fileInputRef.value?.click()
}

async function handleAvatarChange(e) {
    const file = e.target.files?.[0]
    if (!file) return

    // 类型校验
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(file.type)) {
        return ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 格式')
    }
    if (file.size > 5 * 1024 * 1024) {
        return ElMessage.warning('头像大小不能超过 5MB')
    }

    avatarUploading.value = true
    try {
        const formData = new FormData()
        formData.append('file', file)
        const res = await uploadAvatar(formData)
        if (res.data.code === 200) {
            const newAvatar = res.data.data
            user.avatar = newAvatar
            // 同步到 localStorage
            const stored = getCurrentUser()
            if (stored) {
                stored.avatar = newAvatar
                // saveUsers(stored, localStorage.getItem('userToken'))
            }
            ElMessage.success('头像更新成功')
        } else {
            ElMessage.error(res.data.message || '头像上传失败')
        }
    } catch (err) {
        console.error('头像上传失败', err)
        ElMessage.error('上传失败，请重试')
    } finally {
        avatarUploading.value = false
        // 重置 input 以允许重复上传同一文件
        if (fileInputRef.value) fileInputRef.value.value = ''
    }
}
</script>

<template>
    <div class="user-account">
        <!-- 顶部导航 -->
        <FloatBanner />

        <!-- Banner 区 -->
        <div class="banner">
            <div class="banner__inner">
                <!-- <div class="banner__profile">
                    <img class="banner__avatar" :src="avatarSrc" alt="头像" />
                    <span class="banner__nickname">{{ user.nickname || user.username || '未登录' }}</span>
                </div> -->
            </div>
        </div>

        <!-- 主体区域 -->
        <div class="main-body">
            <!-- 左侧边栏 -->
            <aside class="sidebar">
                <nav class="sidebar__menu">
                    <div
                        v-for="item in menuItems"
                        :key="item.key"
                        class="sidebar__item"
                        :class="{ 'sidebar__item--active': activeMenu === item.key }"
                        @click="selectMenu(item.key)"
                    >
                        <i class="sidebar__icon" :class="item.icon"></i>
                        <span class="sidebar__label">{{ item.label }}</span>
                    </div>
                </nav>
            </aside>

            <!-- 右侧内容 -->
            <main class="content">

                <!-- ==================== 首页 ==================== -->
                <div v-if="activeMenu === 'home'" class="card card--center">
                    <div class="home-hero">
                        <img class="home-hero__avatar" :src="avatarSrc" alt="头像" />
                        <h2 class="home-hero__name">{{ user.nickname || user.username || '未设置昵称' }}</h2>
                        <p class="home-hero__uid" v-if="user.id">UID: {{ user.id }}</p>
                    </div>
                </div>

                <!-- ==================== 我的信息 ==================== -->
                <div v-if="activeMenu === 'info'" class="card" @vue:mounted="initInfoForm">
                    <h3 class="card__title">我的信息</h3>
                    <div class="info-form">
                        <!-- 昵称 -->
                        <div class="form-row">
                            <label class="form-label">昵称</label>
                            <div class="form-control">
                                <input
                                    v-model="infoForm.nickname"
                                    class="form-input"
                                    placeholder="请输入昵称"
                                    maxlength="30"
                                />
                            </div>
                        </div>

                        <!-- 用户名（只读） -->
                        <div class="form-row">
                            <label class="form-label">用户名</label>
                            <div class="form-control">
                                <input
                                    class="form-input form-input--readonly"
                                    :value="user.username"
                                    readonly
                                    disabled
                                />
                                <span class="form-hint">用户名不可修改</span>
                            </div>
                        </div>

                        <!-- 签名 -->
                        <div class="form-row">
                            <label class="form-label">我的签名</label>
                            <div class="form-control">
                                <textarea
                                    v-model="infoForm.signature"
                                    class="form-textarea"
                                    placeholder="介绍一下你自己吧~"
                                    maxlength="100"
                                    rows="3"
                                ></textarea>
                            </div>
                        </div>

                        <!-- 电话 -->
                        <div class="form-row">
                            <label class="form-label">电话</label>
                            <div class="form-control">
                                <input
                                    v-model="infoForm.phone"
                                    class="form-input"
                                    placeholder="请输入手机号"
                                    maxlength="11"
                                />
                            </div>
                        </div>

                        <!-- 邮箱 -->
                        <div class="form-row">
                            <label class="form-label">邮箱</label>
                            <div class="form-control">
                                <input
                                    v-model="infoForm.email"
                                    class="form-input"
                                    placeholder="请输入邮箱地址"
                                />
                            </div>
                        </div>

                        <!-- 提交 -->
                        <div class="form-row">
                            <label class="form-label"></label>
                            <div class="form-control">
                                <button
                                    class="form-btn"
                                    :disabled="infoSaving"
                                    @click="saveInfo"
                                >
                                    {{ infoSaving ? '保存中...' : '保存' }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ==================== 我的头像 ==================== -->
                <div v-if="activeMenu === 'avatar'" class="card">
                    <h3 class="card__title">我的头像</h3>
                    <div class="avatar-section">
                        <div class="avatar-preview">
                            <img class="avatar-preview__img" :src="avatarSrc" alt="当前头像" />
                        </div>
                        <div class="avatar-actions">
                            <p class="avatar-desc">支持 JPG、PNG、GIF、WebP 格式，大小不超过 5MB</p>
                            <button
                                class="form-btn"
                                :disabled="avatarUploading"
                                @click="triggerUpload"
                            >
                                {{ avatarUploading ? '上传中...' : '选择新头像' }}
                            </button>
                            <input
                                ref="fileInputRef"
                                type="file"
                                accept="image/jpeg,image/png,image/gif,image/webp"
                                style="display: none"
                                @change="handleAvatarChange"
                            />
                        </div>
                    </div>
                </div>

            </main>
        </div>
    </div>
</template>

<style scoped>
/* ==================== 整体 ==================== */
.user-account {
    width: 100%;
    min-height: 100vh;
    background: #f1f2f3;
    padding-top: 60px;
}

/* ==================== Banner ==================== */
.banner {
    width: 100%;
    height: 160px;
    background: url(@/assets/images/73a22f6747cbf58a20f68484ef95224a79feeb2b.png) center/cover;
    display: flex;
    align-items: center;
}

.banner__inner {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 65px;
    display: flex;
    align-items: center;
}

.banner__profile {
    display: flex;
    align-items: center;
    gap: 16px;
}

.banner__avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    object-fit: cover;
    background: #c7c8ca;
    border: 3px solid rgba(255, 255, 255, 0.4);
    flex-shrink: 0;
}

.banner__nickname {
    font-size: 22px;
    font-weight: 600;
    color: #fff;
}

/* ==================== 主体 ==================== */
.main-body {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    gap: 20px;
    padding: 20px 65px 40px;
}

/* ==================== 侧边栏 ==================== */
.sidebar {
    width: 200px;
    flex-shrink: 0;
}

.sidebar__menu {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    position: sticky;
    top: 80px;
}

.sidebar__item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 0 20px;
    height: 48px;
    font-size: 15px;
    color: #18191c;
    cursor: pointer;
    transition: background 0.15s, color 0.15s;
    border-left: 3px solid transparent;
    user-select: none;
}

.sidebar__item:hover {
    background: #f6f7f8;
    color: #00AEEC;
}

.sidebar__item--active {
    color: #00AEEC;
    background: #f0fbff;
    border-left-color: #00AEEC;
    font-weight: 500;
}

.sidebar__icon {
    display: inline-block;
    width: 20px;
    height: 20px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    flex-shrink: 0;
}

.icon-home   { background-image: url(@/assets/images/House_Black_Icon.svg); }
.icon-info   { background-image: url(@/assets/images/User_black_icon.svg); }
.icon-avatar { background-image: url(@/assets/images/Group_black_icon.png); }

/* ==================== 内容区 ==================== */
.content {
    flex: 1;
    min-width: 0;
}

.card {
    background: #fff;
    border-radius: 8px;
    padding: 28px 32px;
}

.card--center {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
}

.card__title {
    font-size: 18px;
    font-weight: 500;
    color: #18191c;
    margin: 0 0 24px;
    padding-bottom: 14px;
    border-bottom: 1px solid #f0f0f0;
}

/* ==================== 首页 ==================== */
.home-hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 14px;
}

.home-hero__avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    background: #f1f2f3;
    margin-bottom: 8px;
}

.home-hero__name {
    font-size: 24px;
    font-weight: 600;
    color: #18191c;
    margin: 0;
}

.home-hero__uid {
    font-size: 14px;
    color: #9499a0;
    margin: 0;
}

/* ==================== 表单 ==================== */
.info-form {
    max-width: 520px;
}

.form-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
}

.form-label {
    width: 80px;
    flex-shrink: 0;
    font-size: 14px;
    font-weight: 500;
    color: #18191c;
    line-height: 38px;
    text-align: right;
    padding-right: 16px;
}

.form-control {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.form-input {
    width: 100%;
    height: 38px;
    padding: 0 12px;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    font-size: 14px;
    color: #18191c;
    outline: none;
    background: #fff;
    transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
    border-color: #00AEEC;
    box-shadow: 0 0 0 2px rgba(0, 174, 236, 0.1);
}

.form-input--readonly {
    background: #f6f7f8;
    color: #9499a0;
    cursor: not-allowed;
}

.form-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    font-size: 14px;
    color: #18191c;
    outline: none;
    background: #fff;
    resize: vertical;
    transition: border-color 0.2s, box-shadow 0.2s;
    font-family: inherit;
}

.form-textarea:focus {
    border-color: #00AEEC;
    box-shadow: 0 0 0 2px rgba(0, 174, 236, 0.1);
}

.form-hint {
    font-size: 12px;
    color: #9499a0;
}

.form-btn {
    height: 38px;
    padding: 0 28px;
    background: #00AEEC;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.15s, opacity 0.15s;
}

.form-btn:hover:not(:disabled) {
    background: #009fd6;
}

.form-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* ==================== 头像页 ==================== */
.avatar-section {
    display: flex;
    align-items: center;
    gap: 32px;
}

.avatar-preview {
    flex-shrink: 0;
}

.avatar-preview__img {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    background: #f1f2f3;
    border: 2px solid #e8e8e8;
}

.avatar-actions {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.avatar-desc {
    font-size: 13px;
    color: #9499a0;
    margin: 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
    .banner__inner {
        padding: 0 20px;
    }

    .main-body {
        flex-direction: column;
        padding: 16px 20px 40px;
    }

    .sidebar {
        width: 100%;
    }

    .sidebar__menu {
        display: flex;
    }

    .sidebar__item {
        flex: 1;
        justify-content: center;
        border-left: none;
        border-bottom: 3px solid transparent;
        padding: 0 12px;
    }

    .sidebar__item--active {
        border-left-color: transparent;
        border-bottom-color: #00AEEC;
    }

    .form-row {
        flex-direction: column;
        gap: 4px;
    }

    .form-label {
        width: auto;
        text-align: left;
        line-height: 1.5;
        padding-right: 0;
    }

    .avatar-section {
        flex-direction: column;
        align-items: flex-start;
    }
}
</style>
