<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser } from '@/utils/userStorage'
import { getAvatarUrl } from '@/api/index'
import UserMenuPopover from './UserMenuPopover.vue'

const router = useRouter();

const currentUser = computed(() => getCurrentUser())
const avatarSrc = computed(() => {
    if (currentUser.value && currentUser.value.avatar) {
        return getAvatarUrl(currentUser.value.avatar)
    }
    return new URL('@/assets/images/Akalin.png', import.meta.url).href
})

const skipLogin = () => {
    router.push('/login');
}
const skipHome = () => {
    router.push('/home');
}
const skipUpload = () => {
    router.push('/upload');
}
</script>

<template>
    <header class="topNav">
        <!-- 左侧：Logo + 导航菜单 -->
        <nav class="navLeft">
            <a href="#" class="logo" @click="skipHome">CiliCili</a>
            <a href="#" class="navItem" @click="skipHome">首页</a>
            <a href="#" class="navItem">番剧</a>
            <a href="#" class="navItem">直播</a>
            <a href="#" class="navItem">游戏中心</a>
            <a href="#" class="navItem">会员购</a>
            <a href="#" class="navItem">漫画</a>
            <a href="#" class="navItem">赛事</a>
            <a href="#" class="navItem download">
                <i class="icon iconDownload"></i>下载客户端
            </a>
        </nav>

        <!-- 中间：搜索框 -->
        <div class="searchBox">
            <i class="icon iconSearch"></i>
            <input type="text" placeholder="搜索你感兴趣的视频" />
        </div>

        <!-- 右侧：用户操作 -->
        <div class="navRight">
            <UserMenuPopover :avatar-src="avatarSrc">
                <div class="userAvatar" @click="skipLogin">
                    <img :src="avatarSrc" alt="头像" />
                </div>
            </UserMenuPopover>
            <div class="userActions">
                <div class="actionItem" @click="skipLogin">
                    <i class="icon iconVip"></i>大会员
                </div>
                <span class="actionItem">
                    <i class="icon iconMsg"></i>消息
                </span>
                <span class="actionItem">
                    <i class="icon iconDynamic"></i>动态
                </span>
                <span class="actionItem">
                    <i class="icon iconFav"></i>收藏
                </span>
                <span class="actionItem">
                    <i class="icon iconHistory"></i>历史
                </span>
                <span class="actionItem">
                    <i class="icon iconCreator"></i>创作中心
                </span>
            </div>
            <button class="uploadBtn" @click="skipUpload">
                <i class="icon iconUpload"></i>投稿
            </button>
        </div>
    </header>
</template>

<style scoped>
.topNav {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 24px;
    background: #fff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.08);
    transition: box-shadow 0.2s;
}
.topNav.scrolled {
    box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}

.navLeft {
    display: flex;
    align-items: center;
    gap: 15px;
    padding-right: 10px;
}
.logo {
    width: 95px;
    height: 40px;
    font-size: 30px;
    font-weight: 600;
    line-height: 43px;
    color: #00AEEC;
    text-decoration: none;
    /* margin-right: 12px; */
}
.navItem {
    font-size: 14px;
    font-weight: 500;
    color: #444;
    text-decoration: none;
    white-space: nowrap;
}
.navItem:hover {
    color: #00a1d6;
}
.download {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.searchBox {
    display: flex;
    align-items: center;
    min-width: 200px;
    width: 300px;
    height: 40px;
    padding: 0 12px;
    background: #f1f2f3;
    border-radius: 8px;
}
.searchBox input {
    flex: 1;
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    margin-left: 8px;
}

.navRight {
    min-width: 494px;
    display: flex;
    align-items: center;
    gap: 16px;
    padding-left: 10px;
}
.userAvatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
}
.userAvatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.userActions {
    display: flex;
    gap: 20px;
    font-size: 14px;
    color: #555;
}
.actionItem {
    display: flex;
    flex-direction: column;
    align-items: center;
    /* gap: 4px; */
    cursor: pointer;
}
.uploadBtn {
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

/* 图标占位 */
.icon {
    display: inline-block;
    width: 16px;
    height: 16px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}
.iconDownload { background-image: url(@/assets/images/download_gray.png); }
.iconSearch { background-image: url(@/assets/images/search.png); }
.iconVip { background-image: url(@/assets/images/bigVIP_black.png); }
.iconMsg { background-image: url(@/assets/images/messages_black.png); }
.iconDynamic { background-image: url(@/assets/images/Group_black_icon.png); }
.iconFav { background-image: url(@/assets/images/likes_black_icon.png); }
.iconHistory { background-image: url(@/assets/images/pastRecodes_black_icon.png); }
.iconCreator { background-image: url(@/assets/images/creationCenter_black_icon.png); }
.iconUpload { background-image: url(@/assets/images/uploadWork.png); }
</style>