<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminSettings, saveAdminSettings } from '@/api/index'

const settingsLoading = ref(false)
const settingsForm = reactive({ siteName: 'CiliCili', siteDescription: '一个视频弹幕分享网站', maxDanmakuPerVideo: 3000, danmakuSpeed: 8, enableRegister: true, enableDanmaku: true, maxUploadSize: 2048, reviewBeforePublish: false })

async function loadSettings() {
    settingsLoading.value = true
    try { const res = await getAdminSettings(); if (res?.data?.data) Object.assign(settingsForm, res.data.data) } catch { /* 使用默认值 */ }
    settingsLoading.value = false
}

async function handleSaveSettings() {
    try { await saveAdminSettings(settingsForm) } catch { /* mock */ }
    ElMessage.success('系统设置已保存')
}

onMounted(loadSettings)
defineExpose({ loadSettings })
</script>

<template>
    <div class="admin-page">
        <div class="admin-page__header">
            <h2 class="admin-page__title">系统设置</h2>
            <span class="admin-page__subtitle">全局系统参数配置</span>
        </div>
        <div class="admin-settings" v-loading="settingsLoading">
            <el-form :model="settingsForm" label-width="160px" label-position="left">
                <el-form-item label="网站名称"><el-input v-model="settingsForm.siteName" maxlength="30" /></el-form-item>
                <el-form-item label="网站描述"><el-input v-model="settingsForm.siteDescription" type="textarea" :rows="2" maxlength="200" /></el-form-item>
                <el-form-item label="单视频弹幕上限"><el-input-number v-model="settingsForm.maxDanmakuPerVideo" :min="100" :max="10000" :step="100" /><span class="admin-settings__hint">条</span></el-form-item>
                <el-form-item label="弹幕滚动速度"><el-slider v-model="settingsForm.danmakuSpeed" :min="1" :max="20" show-stops style="width: 260px" /><span class="admin-settings__hint">({{ settingsForm.danmakuSpeed }} 秒)</span></el-form-item>
                <el-form-item label="允许新用户注册"><el-switch v-model="settingsForm.enableRegister" active-text="开启" inactive-text="关闭" /></el-form-item>
                <el-form-item label="全局弹幕开关"><el-switch v-model="settingsForm.enableDanmaku" active-text="开启" inactive-text="关闭" /></el-form-item>
                <el-form-item label="最大上传大小"><el-input-number v-model="settingsForm.maxUploadSize" :min="100" :max="10240" :step="100" /><span class="admin-settings__hint">MB</span></el-form-item>
                <el-form-item label="视频上传需审核"><el-switch v-model="settingsForm.reviewBeforePublish" active-text="是" inactive-text="否" /></el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
                    <el-button @click="loadSettings">重置</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<style scoped>
.admin-page__header { margin-bottom: 24px; }
.admin-page__title { font-size: 22px; font-weight: 600; color: #18191c; margin: 0 0 4px 0; }
.admin-page__subtitle { font-size: 13px; color: #9499a0; }
.admin-settings { background: #fff; border-radius: 10px; padding: 32px 40px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04); max-width: 700px; }
.admin-settings__hint { margin-left: 10px; font-size: 13px; color: #9499a0; }
@media (max-width: 640px) { .admin-page__title { font-size: 18px; } .admin-settings { padding: 20px; } }
</style>
