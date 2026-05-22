<script setup>
import { ref, reactive } from 'vue'
import CustomPlayer from './VideoPage_CustomPlayer.vue'

// 视频元数据
const video = reactive({
    title: '标题栏',
    playCount: 0,
    danmakuCount: 0,
    date: '2020-06-25',
    time: '05:12:13',
    copyright: '未经作者授权，禁止转载',
    description: '这是简介',
    tags: ['标签1', '标签2', '标签3', '标签4', '标签5', '标签6', '标签7', '标签8'],
    likes: 0,
    coins: 0,
    collects: 0,
    shares: 0
})

// 弹幕状态
const danmakuOn = ref(true)
const danmakuOnline = ref(0)
const danmakuTotal = ref(0)
const danmakuInput = ref('')

// 发送弹幕
const sendDanmaku = () => {
    if (!danmakuInput.value.trim()) return
    // TODO: 调用发送弹幕接口
    console.log('发送弹幕:', danmakuInput.value)
    danmakuInput.value = ''
}

// 评论数据
const comments = ref([
    { id: 1, username: "用户1", content: "这个视频太棒了，内容很有深度！", date: "2024-01-15", time: "14:30", likes: 256, replies: 12 },
    { id: 2, username: "用户2", content: "学到了很多新知识，感谢分享！", date: "2024-01-14", time: "09:15", likes: 189, replies: 8 },
    { id: 2, username: "用户2", content: "学到了很多新知识，感谢分享！", date: "2024-01-14", time: "09:15", likes: 189, replies: 8 },
    { id: 3, username: "用户3", content: "制作精良，期待更多精彩内容！", date: "2024-01-13", time: "20:45", likes: 342, replies: 0 }
])

const commentSort = ref('latest')
const commentInput = ref('')

const submitComment = () => {
    if (!commentInput.value.trim()) return
    // TODO: 发表评论
    console.log('发表评论:', commentInput.value)
    commentInput.value = ''
}
</script>

<template>
    <div class="video-main">
        <!-- 标题区域 -->
        <div class="video-header">
            <h1 class="title">{{ video.title }}</h1>
            <div class="meta-info">
                <span class="meta-item">
                    <i class="icon icon-play"></i>{{ video.playCount }}
                </span>
                <span class="meta-item">
                    <i class="icon icon-danmaku"></i>{{ video.danmakuCount }}
                </span>
                <span class="meta-item">{{ video.date }}</span>
                <span class="meta-item">{{ video.time }}</span>
                <span class="meta-item copyright">
                    <i class="icon icon-copyright"></i>{{ video.copyright }}
                </span>
            </div>
        </div>

        <!-- 视频播放器 -->
        <div class="video-player">
            <CustomPlayer controls></CustomPlayer>
        </div>

        <!-- 弹幕控制栏 -->
        <div class="danmaku-bar">
            <div class="danmaku-status">
                <span class="online"><strong>{{ danmakuOnline }}</strong>人正在观看，已装填</span>
                <span class="total"><strong>{{ danmakuTotal }}</strong>条弹幕</span>
            </div>
            <div class="danmaku-switches">
                <button class="switch-btn" :class="{ on: danmakuOn }" @click="danmakuOn = !danmakuOn"></button>
                <button class="settings-btn"></button>
            </div>
            <div class="danmaku-send">
                <div class="send-input">
                    <i class="icon icon-word-setting"></i>
                    <input v-model="danmakuInput" placeholder="发个友善的弹幕见证当下" @keyup.enter="sendDanmaku" />
                </div>
                <span class="danmaku-guide">弹幕礼仪 &gt;</span>
                <button class="send-btn" @click="sendDanmaku">发送</button>
            </div>
        </div>

        <!-- 互动操作栏 (点赞投币收藏分享) -->
        <div class="action-bar">
            <div class="actions">
                <button class="action-btn"><i class="icon-videoReward icon icon-like"></i>{{ video.likes }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-coin"></i>{{ video.coins }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-collect"></i>{{ video.collects }}</button>
                <button class="action-btn"><i class="icon-videoReward icon icon-share"></i>{{ video.shares }}</button>
            </div>
            <div class="report">
                <i class="icon icon-report"></i>稿件举报
            </div>
        </div>

        <!-- 简介与标签 -->
        <div class="desc-section">
            <p class="desc-text">{{ video.description }}</p>
            <div class="tags">
                <span v-for="tag in video.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
            <div class="comment-header">
                <div class="comment-title">
                    评论 <span class="comment-count">{{ comments.length }}</span>
                </div>
                <div class="comment-sort">
                    <span :class="{ active: commentSort === 'latest' }" @click="commentSort = 'latest'">最新</span>
                    <span class="divider">|</span>
                    <span :class="{ active: commentSort === 'hot' }" @click="commentSort = 'hot'">最热</span>
                </div>
            </div>

            <!-- 发表评论 -->
            <div class="comment-post">
                <div class="post-avatar"></div>
                <div class="post-input-wrap">
                    <input v-model="commentInput" placeholder="宫廷玉液酒，评论走一走" @keyup.enter="submitComment" />
                </div>
            </div>

            <!-- 评论列表 -->
            <div class="comment-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <div class="comment-avatar"></div>
                    <div class="comment-body">
                        <div class="comment-user" style="color: #3C3C3C;">{{ comment.username }}</div>
                        <div class="comment-content">{{ comment.content }}</div>
                        <div class="comment-footer">
                            <span class="comment-time">{{ comment.date }} {{ comment.time }}</span>
                            <span class="comment-like"><i class="icon icon-comment-like"></i>{{ comment.likes }}</span>
                            <span class="comment-reply-btn">回复</span>
                        </div>
                        <!-- 回复挂件 -->
                        <div v-if="comment.replies > 0" class="comment-replies">
                            共 {{ comment.replies }} 条回复，点击查看
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.video-main {
    width: 902px;
    background: #fff;
    border-radius: 4px;
    /* padding: 16px 0; */
}

/* 标题 */
.video-header {
    padding: 0 0 40px;
}
.title {
    font-size: 22px;
    font-weight: 500;
    color: #18191c;
    margin: 0 0 10px;
    line-height: 1.3;
}
.meta-info {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 13px;
    color: #9499a0;
}
.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}
.copyright {
    color: #9499a0;
}

/* 视频播放器占位 */
.video-player {
    width: 100%;
    height: 486px;
    background: #000;
    border-radius: 2px;
}

/* 弹幕控制栏 */
.danmaku-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 8px 16px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 12px;
    border-radius: 2px;
}
.danmaku-status {
    display: flex;
    align-items: center;
    gap: 1px;
    font-size: 14px;
    color: #555;
    white-space: nowrap;
}
.danmaku-switches {
    display: flex;
    align-items: center;
    gap: 12px;
}
.switch-btn, .settings-btn {
    width: 24px;
    height: 24px;
    border: none;
    background: center/contain no-repeat;
    cursor: pointer;
}
.switch-btn {
    background-image: url(@/assets/images/danmuSwitch_on_default_icon.png);
}
.settings-btn {
    background-image: url(@/assets/images/danmuSetting_default_icon.png);
}
.danmaku-send {
    display: flex;
    align-items: center;
    flex: 1;
    height: 32px;
    background: #f1f2f3;
    border-radius: 8px;
    padding-left: 12px;
}
.send-input {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 8px;
}
.send-input input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 13px;
    outline: none;
    color: #555;
}
.danmaku-guide {
    font-size: 14px;
    color: #8c929b;
    margin: 0 12px;
    cursor: pointer;
    white-space: nowrap;
}
.send-btn {
    height: 100%;
    padding: 0 24px;
    background: #00AEEC;
    color: #fff;
    border: none;
    border-radius: 0 8px 8px 0;
    font-size: 14px;
    cursor: pointer;
}

/* 互动栏 */
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #e8e8e8;
}
.actions {
    display: flex;
    gap: 60px;
}
.action-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: none;
    border: none;
    font-size: 16px;
    color: #61666D;
    cursor: pointer;
}
.report {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #9499a0;
    cursor: pointer;
}

/* 简介 & 标签 */
.desc-section {
    padding: 16px 0;
    border-bottom: 1px solid #e8e8e8;
}
.desc-text {
    font-size: 15px;
    color: #18191c;
    line-height: 1.6;
    margin-bottom: 14px;
}
.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}
.tag {
    padding: 4px 12px;
    background: #f1f2f3;
    border-radius: 20px;
    font-size: 14px;
    color: #61666d;
}

/* 评论区 */
.comment-section {
    padding-top: 20px;
}
.comment-header {
    display: flex;
    align-items: flex-end;
    gap: 24px;
    margin-bottom: 24px;
}
.comment-title {
    font-size: 20px;
    font-weight: 600;
    color: #18191c;
}
.comment-count {
    font-size: 16px;
    color: #9499a0;
    margin-left: 4px;
}
.comment-sort {
    font-size: 15px;
    color: #9499a0;
    display: flex;
    gap: 8px;
}
.comment-sort span {
    cursor: pointer;
}
.comment-sort span:hover {
    color: #00AEEC;
}
.comment-sort .active {
    color: #3C3C3C;
}

.comment-post {
    display: flex;
    gap: 12px;
    margin-bottom: 28px;
}
.post-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
}
.post-input-wrap {
    flex: 1;
}
.post-input-wrap input {
    width: 100%;
    height: 100%;
    padding: 0 16px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    background: #f6f7f8;
    font-size: 15px;
    outline: none;
    transition: border-color 0.2s;
}
.post-input-wrap input:focus {
    border-color: #00a1d6;
    background: #fff;
}

.comment-item {
    display: flex;
    gap: 14px;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
}
.comment-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
    flex-shrink: 0;
}
.comment-body {
    flex: 1;
}
.comment-user {
    font-size: 14px;
    /* color: #fb7299; */
    margin-bottom: 6px;
}
.comment-content {
    font-size: 15px;
    color: #18191c;
    line-height: 1.5;
    margin-bottom: 8px;
}
.comment-footer {
    display: flex;
    align-items: center;
    gap: 20px;
    font-size: 13px;
    color: #9499a0;
}
.comment-like {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
}
.comment-reply-btn {
    cursor: pointer;
}
.comment-replies {
    margin-top: 8px;
    font-size: 14px;
    color: #9499a0;
    cursor: pointer;
}

/* 通用图标 */
.icon {
    display: inline-block;
    width: 18px;
    height: 18px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    vertical-align: middle;
}

.icon-videoReward{
    width: 24px;
    height: 24px;
}

.icon-play { background-image: url(@/assets/images/playsNum_gray.png); }
.icon-danmaku { background-image: url(@/assets/images/papernote0.png); }
.icon-copyright { background-image: url(@/assets/images/PhosphoriconsSecurityWarningsPhosphoriconsProhibit.png); }
.icon-word-setting { background-image: url(@/assets/images/wordSetting_icon.png); }
.icon-like { background-image: url(@/assets/images/like_solid.png); }
.icon-coin { background-image: url(@/assets/images/reward.png); }
.icon-collect { background-image: url(@/assets/images/Star_109_774.png); }
.icon-share { background-image: url(@/assets/images/share_icon.png); }
.icon-report { background-image: url(@/assets/images/attentiontriangle.png); }
.icon-comment-like { background-image: url(@/assets/images/Mobile0.png); }
</style>