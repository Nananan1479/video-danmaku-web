<script setup>
import { useRouter } from 'vue-router'

const router = useRouter();

const skipLogin = () => {
    router.push('/login');
}
const skipHome = () => {
    router.push('/home');
}
</script>

<template>
    <header class="top-nav">
        <!-- 左侧：Logo + 导航菜单 -->
        <nav class="nav-left">
            <a href="#" class="logo" @click="skipHome">CiliCili</a>
            <a href="#" class="nav-item" @click="skipHome">首页</a>
            <a href="#" class="nav-item">番剧</a>
            <a href="#" class="nav-item">直播</a>
            <a href="#" class="nav-item">游戏中心</a>
            <a href="#" class="nav-item">会员购</a>
            <a href="#" class="nav-item">漫画</a>
            <a href="#" class="nav-item">赛事</a>
            <a href="#" class="nav-item download">
                <i class="icon icon-download"></i>下载客户端
            </a>
        </nav>

        <!-- 中间：搜索框 -->
        <div class="search-box">
            <i class="icon icon-search"></i>
            <input type="text" placeholder="搜索你感兴趣的视频" />
        </div>

        <!-- 右侧：用户操作 -->
        <div class="nav-right">
            <div class="user-avatar" @click="skipLogin">
                <img src="@/assets/images/Akalin.png" alt="头像" />
            </div>
            <div class="user-actions">
                <div class="action-item" @click="skipLogin">
                    <i class="icon icon-vip"></i>大会员
                </div>
                <span class="action-item">
                    <i class="icon icon-msg"></i>消息
                </span>
                <span class="action-item">
                    <i class="icon icon-dynamic"></i>动态
                </span>
                <span class="action-item">
                    <i class="icon icon-fav"></i>收藏
                </span>
                <span class="action-item">
                    <i class="icon icon-history"></i>历史
                </span>
                <span class="action-item">
                    <i class="icon icon-creator"></i>创作中心
                </span>
            </div>
            <button class="upload-btn">
                <i class="icon icon-upload"></i>投稿
            </button>
        </div>
    </header>
</template>

<style scoped>
.top-nav {
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
.top-nav.scrolled {
    box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}

.nav-left {
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
.nav-item {
    font-size: 14px;
    font-weight: 500;
    color: #444;
    text-decoration: none;
    white-space: nowrap;
}
.nav-item:hover {
    color: #00a1d6;
}
.download {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.search-box {
    display: flex;
    align-items: center;
    min-width: 200px;
    width: 300px;
    height: 40px;
    padding: 0 12px;
    background: #f1f2f3;
    border-radius: 8px;
}
.search-box input {
    flex: 1;
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    margin-left: 8px;
}

.nav-right {
    min-width: 494px;
    display: flex;
    align-items: center;
    gap: 16px;
    padding-left: 10px;
}
.user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    overflow: hidden;
}
.user-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.user-actions {
    display: flex;
    gap: 20px;
    font-size: 14px;
    color: #555;
}
.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    /* gap: 4px; */
    cursor: pointer;
}
.upload-btn {
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
    width: 20px;
    height: 20px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}
.icon-download { background-image: url(@/assets/images/download_gray.png); }
.icon-search { background-image: url(@/assets/images/search.png); }
.icon-vip { background-image: url(@/assets/images/bigVIP_black.png); }
.icon-msg { background-image: url(@/assets/images/messages_black.png); }
.icon-dynamic { background-image: url(@/assets/images/Group_black_icon.png); }
.icon-fav { background-image: url(@/assets/images/likes_black_icon.png); }
.icon-history { background-image: url(@/assets/images/pastRecodes_black_icon.png); }
.icon-creator { background-image: url(@/assets/images/creationCenter_black_icon.png); }
.icon-upload { background-image: url(@/assets/images/uploadWork.png); }
</style>