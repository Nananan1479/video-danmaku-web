<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser } from '@/utils/userStorage'
import { getAvatarUrl } from '@/api/index'

const router = useRouter()

// computed 创建一个基于现有响应式数据计算出来的值,它返回一个只读的 Ref 对象
const currentUser = computed(() => getCurrentUser())
console.log(currentUser.value)
const avatarSrc = computed(() => {
    if (currentUser.value && currentUser.value.avatar) {
        return getAvatarUrl(currentUser.value.avatar)
    }
    return new URL('@/assets/images/Akalin.png', import.meta.url).href
})
console.log(avatarSrc.value)

const navItems = [
    { label: '首页', icon: 'tv03', isHome: true },
    { label: '番剧' },
    { label: '直播' },
    { label: '游戏中心' },
    { label: '会员购' },
    { label: '漫画' },
    { label: '赛事' },
    { label: '下载客户端', icon: 'download', hasDownloadIcon: true }
]

const userActions = [
    { label: '大会员', icon: 'Frame_120_179' },
    { label: '消息', icon: 'IconEmail' },
    { label: '动态', icon: 'Group' },
    { label: '收藏', icon: 'star' },
    { label: '历史', icon: 'timeclock' },
    { label: '创作中心', icon: 'Idea' }
]

function skipLogin() {
    router.push('/login')
}

function skipHome() {
    router.push('/home')
}

function skipUpload() {
    router.push('/upload')
}

function handleNavClick(item) {
    if (item.isHome) {
        skipHome()
    }
}
</script>

<template>
    <div class="headerBanner">
        <div class="headerBanner__inner">
            <!-- 左侧导航栏 -->
            <div class="headerBanner__left">
                <div class="headerBanner__nav-item headerBanner__nav-item--home" @click="skipHome">
                    <span class="headerBanner__home-icon"></span>
                    <span class="headerBanner__nav-label">首页</span>
                </div>
                <div
                    v-for="item in navItems.slice(1)"
                    :key="item.label"
                    class="headerBanner__nav-item"
                >
                    <span v-if="item.icon" class="headerBanner__icon" :class="`headerBanner__icon--${item.icon}`"></span>
                    <span class="headerBanner__nav-label">{{ item.label }}</span>
                </div>
            </div>

            <!-- 搜索栏 -->
            <div class="headerBanner__search">
                <span class="headerBanner__search-icon"></span>
            </div>

            <!-- 右侧导航栏 -->
            <div class="headerBanner__right">
                <div class="headerBanner__avatar" @click="skipLogin">
                    <img :src="avatarSrc" alt="头像" />
                </div>
                <div class="headerBanner__actions">
                    <div
                        v-for="action in userActions"
                        :key="action.label"
                        class="headerBanner__action"
                        @click="skipLogin"
                    >
                        <span class="headerBanner__action-icon" :class="`headerBanner__action-icon--${action.icon}`"></span>
                        <span class="headerBanner__action-label">{{ action.label }}</span>
                    </div>
                </div>
                <button class="headerBanner__upload-btn" @click="skipUpload">
                    <span class="headerBanner__upload-icon"></span>
                    投稿
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.headerBanner {
    width: 100%;
    height: 156px;
    overflow: hidden;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    background-image: url(@/assets/images/73a22f6747cbf58a20f68484ef95224a79feeb2b.png);
}

.headerBanner__inner {
    width: 100%;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 24px;
}

.headerBanner__left {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 10px;
}

.headerBanner__nav-item {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
}

.headerBanner__nav-item--home {
    width: 55px;
    height: 24px;
    justify-content: center;
}

.headerBanner__home-icon {
    width: 20px;
    height: 20px;
    background-image: url(@/assets/images/tv03.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.headerBanner__nav-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #fff;
    white-space: nowrap;
}

.headerBanner__icon {
    width: 28px;
    height: 28px;
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.headerBanner__icon--download {
    width: 12px;
    height: 13.5px;
    background-image: url(@/assets/images/download.png);
}

.headerBanner__search {
    min-width: 200px;
    width: 390px;
    height: 34px;
    position: relative;
    border-radius: 8px;
    background-color: rgba(225, 225, 225, 0.9);
}

.headerBanner__search-icon {
    position: absolute;
    right: 15.5px;
    top: 7px;
    width: 20px;
    height: 20px;
    background-image: url(@/assets/images/search.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.headerBanner__right {
    min-width: 494px;
    display: flex;
    align-items: center;
    gap: 16px;
    padding-left: 10px;
    /* width: 495px;
    height: 56px; */
}

.headerBanner__avatar {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    overflow: hidden;
}

.headerBanner__avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.headerBanner__actions {
    display: flex;
    gap: 20px;
    margin-left: 0;
}

.headerBanner__action {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
}

.headerBanner__action-icon {
    width: 22px;
    height: 22px;
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.headerBanner__action-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #fff;
    text-align: center;
}

.headerBanner__upload-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 38px;
    padding: 0 22px;
    background: #fb7299;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
}

.headerBanner__upload-icon {
    width: 22px;
    height: 18px;
    background-image: url(@/assets/images/uploadWork.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.headerBanner__action-icon--Frame_120_179 { background-image: url(@/assets/images/Frame_120_179.png); }
.headerBanner__action-icon--IconEmail { background-image: url(@/assets/images/IconEmail.png); }
.headerBanner__action-icon--Group { background-image: url(@/assets/images/Group.png); }
.headerBanner__action-icon--star { background-image: url(@/assets/images/likes_white_icon.png); }
.headerBanner__action-icon--timeclock { background-image: url(@/assets/images/timeclock.png); }
.headerBanner__action-icon--Idea { background-image: url(@/assets/images/Idea.png); }

</style>
