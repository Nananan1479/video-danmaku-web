<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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

function handleNavClick(item) {
    if (item.isHome) {
        skipHome()
    }
}
</script>

<template>
    <div class="header-banner">
        <div class="header-banner__inner">
            <!-- 左侧导航栏 -->
            <div class="header-banner__left">
                <div class="header-banner__nav-item header-banner__nav-item--home" @click="skipHome">
                    <span class="header-banner__home-icon"></span>
                    <span class="header-banner__nav-label">首页</span>
                </div>
                <div
                    v-for="item in navItems.slice(1)"
                    :key="item.label"
                    class="header-banner__nav-item"
                >
                    <span v-if="item.icon" class="header-banner__icon" :class="`header-banner__icon--${item.icon}`"></span>
                    <span class="header-banner__nav-label">{{ item.label }}</span>
                </div>
            </div>

            <!-- 搜索栏 -->
            <div class="header-banner__search">
                <span class="header-banner__search-icon"></span>
            </div>

            <!-- 右侧导航栏 -->
            <div class="header-banner__right">
                <div class="header-banner__avatar" @click="skipLogin">
                    <img src="@/assets/images/Akalin.png" alt="头像" />
                </div>
                <div class="header-banner__actions">
                    <div
                        v-for="action in userActions"
                        :key="action.label"
                        class="header-banner__action"
                        @click="skipLogin"
                    >
                        <span class="header-banner__action-icon" :class="`header-banner__action-icon--${action.icon}`"></span>
                        <span class="header-banner__action-label">{{ action.label }}</span>
                    </div>
                </div>
                <button class="header-banner__upload-btn">
                    <span class="header-banner__upload-icon"></span>
                    投稿
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.header-banner {
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

.header-banner__inner {
    width: 100%;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 24px;
}

.header-banner__left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.header-banner__nav-item {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
}

.header-banner__nav-item--home {
    width: 55px;
    height: 24px;
    justify-content: center;
}

.header-banner__home-icon {
    width: 20px;
    height: 20px;
    background-image: url(@/assets/images/tv03.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.header-banner__nav-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #fff;
    white-space: nowrap;
}

.header-banner__icon {
    width: 28px;
    height: 28px;
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.header-banner__icon--download {
    width: 12px;
    height: 13.5px;
    background-image: url(@/assets/images/download.png);
}

.header-banner__search {
    width: 390px;
    height: 34px;
    position: relative;
    border-radius: 8px;
    background-color: rgba(225, 225, 225, 0.9);
}

.header-banner__search-icon {
    position: absolute;
    right: 15.5px;
    top: 7px;
    width: 20px;
    height: 20px;
    background-image: url(@/assets/images/search.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.header-banner__right {
    min-width: 480px;
    display: flex;
    align-items: center;
    gap: 16px;
    /* width: 495px;
    height: 56px; */
}

.header-banner__avatar {
    width: 39px;
    height: 39px;
    border-radius: 50%;
    background: #fff;
    flex-shrink: 0;
    cursor: pointer;
}

.header-banner__avatar img {
    width: 35px;
    height: 35px;
    border-radius: 50%;
    object-fit: cover;
    margin: 2px 0 0 2px;
}

.header-banner__actions {
    display: flex;
    gap: 20px;
    margin-left: 0;
}

.header-banner__action {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
}

.header-banner__action-icon {
    width: 22px;
    height: 22px;
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.header-banner__action-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Medium", sans-serif;
    font-weight: 500;
    color: #fff;
    text-align: center;
}

.header-banner__upload-btn {
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

.header-banner__upload-icon {
    width: 22px;
    height: 18px;
    background-image: url(@/assets/images/uploadWork.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.header-banner__action-icon--Frame_120_179 { background-image: url(@/assets/images/Frame_120_179.png); }
.header-banner__action-icon--IconEmail { background-image: url(@/assets/images/IconEmail.png); }
.header-banner__action-icon--Group { background-image: url(@/assets/images/Group.png); }
.header-banner__action-icon--star { background-image: url(@/assets/images/star.png); }
.header-banner__action-icon--timeclock { background-image: url(@/assets/images/timeclock.png); }
.header-banner__action-icon--Idea { background-image: url(@/assets/images/Idea.png); }

</style>
