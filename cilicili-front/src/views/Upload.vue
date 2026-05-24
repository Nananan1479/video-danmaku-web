<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { uploadVideo } from '@/api/index.js'
import FloatBanner from '@/components/FloatBanner.vue'
import { getCurrentUser } from '@/utils/userStorage'

const router = useRouter()

const formData = reactive({
    title: '',
    description: ''
})

const videoFile = ref(null)
const videoFileName = ref('')
const coverFile = ref(null)
const coverPreview = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const isUploading = ref(false)
const uploadPercent = ref(0)

function handleVideoSelect(e) {
    const file = e.target.files[0]
    if (!file) return

    if (!file.name.toLowerCase().endsWith('.mp4')) {
        errorMessage.value = '仅支持MP4格式视频'
        videoFile.value = null
        videoFileName.value = ''
        e.target.value = ''
        return
    }

    videoFile.value = file
    videoFileName.value = file.name
    errorMessage.value = ''
}

function handleCoverSelect(e) {
    const file = e.target.files[0]
    if (!file) return

    coverFile.value = file
    const reader = new FileReader()
    reader.onload = (ev) => {
        coverPreview.value = ev.target.result
    }
    reader.readAsDataURL(file)
}

function triggerVideoInput() {
    document.getElementById('video-input').click()
}

function triggerCoverInput() {
    document.getElementById('cover-input').click()
}

function removeVideo() {
    videoFile.value = null
    videoFileName.value = ''
    document.getElementById('video-input').value = ''
}

const handleUpload = async () => {
    errorMessage.value = ''
    successMessage.value = ''

    if (!videoFile.value) {
        errorMessage.value = '请选择视频文件'
        return
    }

    if (!formData.title.trim()) {
        errorMessage.value = '请输入视频标题'
        return
    }

    const currentUser = getCurrentUser()
    if (!currentUser || !currentUser.id) {
        errorMessage.value = '请先登录'
        return
    }

    isUploading.value = true
    uploadPercent.value = 0

    const fd = new FormData()
    fd.append('video', videoFile.value)
    if (coverFile.value) {
        fd.append('cover', coverFile.value)
    }
    fd.append('title', formData.title)
    fd.append('description', formData.description)
    fd.append('uploaderId', currentUser.id)

    try {
        const res = await uploadVideo(fd)
        if (res.data.code === 200) {
            successMessage.value = '上传成功！即将跳转到视频页面...'
            setTimeout(() => {
                router.push({ name: 'VideoPage', query: { id: res.data.data.id } })
            }, 2000)
        } else {
            errorMessage.value = res.data.message || '上传失败'
        }
    } catch (err) {
        errorMessage.value = '网络错误，上传失败'
    } finally {
        isUploading.value = false
        uploadPercent.value = 0
    }
}

function goHome() {
    router.push('/home')
}

onUnmounted(() => {
    if (coverPreview.value) {
        URL.revokeObjectURL(coverPreview.value)
    }
})
</script>

<template>
    <FloatBanner />
    <div class="upload-page">
        <div class="upload-container">
            <div class="upload-header">
                <h1 class="logo">CiliCili</h1>
                <p class="subtitle">上传视频</p>
            </div>

            <form @submit.prevent="handleUpload" class="upload-form">
                <div class="form-group">
                    <label>视频文件 <span class="required">*</span></label>
                    <div
                        class="file-drop-zone"
                        :class="{ 'has-file': videoFileName }"
                        @click="triggerVideoInput"
                    >
                        <template v-if="videoFileName">
                            <div class="file-info">
                                <span class="file-icon">▶</span>
                                <span class="file-name">{{ videoFileName }}</span>
                                <button type="button" class="file-remove" @click.stop="removeVideo">×</button>
                            </div>
                        </template>
                        <template v-else>
                            <div class="drop-placeholder">
                                <span class="drop-icon">+</span>
                                <span>点击选择MP4视频文件</span>
                            </div>
                        </template>
                    </div>
                    <input
                        id="video-input"
                        type="file"
                        accept=".mp4,video/mp4"
                        style="display: none"
                        @change="handleVideoSelect"
                    />
                </div>

                <div class="form-group">
                    <label>封面图片</label>
                    <div
                        class="cover-upload-area"
                        @click="triggerCoverInput"
                    >
                        <template v-if="coverPreview">
                            <img :src="coverPreview" class="cover-preview" alt="封面预览" />
                        </template>
                        <template v-else>
                            <div class="cover-placeholder">
                                <span class="drop-icon">+</span>
                                <span>点击上传封面</span>
                            </div>
                        </template>
                    </div>
                    <input
                        id="cover-input"
                        type="file"
                        accept="image/*"
                        style="display: none"
                        @change="handleCoverSelect"
                    />
                </div>

                <div class="form-group">
                    <label for="title">视频标题 <span class="required">*</span></label>
                    <input
                        type="text"
                        id="title"
                        v-model="formData.title"
                        placeholder="请输入视频标题"
                        maxlength="100"
                    />
                </div>

                <div class="form-group">
                    <label for="description">视频简介</label>
                    <textarea
                        id="description"
                        v-model="formData.description"
                        placeholder="简单介绍视频内容..."
                        rows="4"
                        maxlength="500"
                    ></textarea>
                </div>

                <div v-if="errorMessage" class="error-message">
                    {{ errorMessage }}
                </div>

                <div v-if="isUploading" class="loading-message">
                    <span class="loading-spinner"></span>
                    正在上传中...
                </div>

                <div v-if="successMessage" class="success-message">
                    {{ successMessage }}
                </div>

                <button type="submit" class="upload-btn" :disabled="isUploading">上传视频</button>
            </form>

            <button @click="goHome" class="goHome-btn">返回首页</button>
        </div>
    </div>
</template>

<style scoped>
.upload-page {
    width: 100%;
    min-height: 100vh;
    background-color: rgba(241, 242, 243);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 80px 20px 20px;
}

.upload-container {
    width: 100%;
    max-width: 560px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    padding: 40px 32px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.upload-header {
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

.upload-form {
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

.required {
    color: #f5222d;
}

.form-group input[type="text"],
.form-group textarea {
    padding: 0 16px;
    background-color: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(232, 232, 232, 0.9);
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;
    font-family: inherit;
}

.form-group input[type="text"] {
    height: 44px;
}

.form-group textarea {
    padding: 12px 16px;
    resize: vertical;
}

.form-group input:focus,
.form-group textarea:focus {
    border-color: rgba(0, 174, 236, 1);
}

.form-group input::placeholder,
.form-group textarea::placeholder {
    color: #9499a0;
}

.file-drop-zone {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    padding: 24px;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.2s, background-color 0.2s;
}

.file-drop-zone:hover {
    border-color: rgba(0, 174, 236, 1);
    background-color: rgba(0, 174, 236, 0.04);
}

.file-drop-zone.has-file {
    border-style: solid;
    border-color: rgba(0, 174, 236, 1);
    background-color: rgba(0, 174, 236, 0.04);
}

.drop-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #9499a0;
    font-size: 14px;
}

.drop-icon {
    font-size: 32px;
    color: #d9d9d9;
}

.file-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.file-icon {
    color: rgba(0, 174, 236, 1);
}

.file-name {
    color: #18191c;
    font-size: 14px;
    word-break: break-all;
}

.file-remove {
    margin-left: auto;
    background: none;
    border: none;
    font-size: 20px;
    color: #9499a0;
    cursor: pointer;
    padding: 0 4px;
    line-height: 1;
}

.file-remove:hover {
    color: #f5222d;
}

.cover-upload-area {
    width: 200px;
    height: 120px;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    transition: border-color 0.2s;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

.cover-upload-area:hover {
    border-color: rgba(0, 174, 236, 1);
}

.cover-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #9499a0;
    font-size: 13px;
}

.cover-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
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

.success-message {
    padding: 12px;
    background: #f6ffed;
    border: 1px solid #b7eb8f;
    border-radius: 6px;
    color: #52c41a;
    font-size: 14px;
    text-align: center;
}

.upload-btn {
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

.upload-btn:hover {
    background: #00a9e1;
}

.upload-btn:active {
    background: #009dd5;
}

.upload-btn:disabled {
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
</style>
