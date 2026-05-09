<script setup>
import { reactive, ref } from 'vue'

const upUser = reactive({
  name: '用户名',
  signature: '个人签名',
  fans: 0
})

const recommendList = ref([
  {
    id: 1,
    cover: '@/assets/images/6ff74a8f45317f579bb358521a0aa33a917bafb9.png',
    title: '此…此视频无人能白嫖！',
    uploader: '果冻lui',
    date: '2020-06-25',
    playCount: 0,
    danmakuCount: 0
  }
])
</script>

<template>
    <aside class="video-sidebar">
        <!-- UP主信息 -->
        <div class="up-info">
            <div class="up-avatar"></div>
            <div class="up-detail">
                <div class="up-name">{{ upUser.name }}</div>
                <div class="up-sign">{{ upUser.signature }}</div>
                <div class="up-actions">
                    <button class="charge-btn">充电</button>
                    <button class="follow-btn">
                        <i class="icon icon-follow"></i>关注 {{ upUser.fans }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 弹幕列表 -->
        <div class="danmaku-list-bar">
            <span>弹幕列表</span>
            <i class="arrow-icon"></i>
        </div>

        <!-- 推荐视频列表 -->
        <div class="recommend-list">
            <div v-for="item in recommendList" :key="item.id" class="recommend-item">
                <div class="rec-cover" :style="{ backgroundImage: `url(${item.cover})` }"></div>
                <div class="rec-info">
                    <div class="rec-title">{{ item.title }}</div>
                    <div class="rec-meta">
                        <!-- <span class="rec-uploader"> -->
                            <i class="icon icon-up"></i>{{ item.uploader }}
                        <!-- </span> -->
                        <!-- <span class="rec-date">{{ item.date }}</span> -->
                    </div>
                    <div class="rec-stats">
                        <span><i class="icon icon-play-sm"></i>{{ item.playCount }}</span>
                        <span><i class="icon icon-danmaku-sm"></i>{{ item.danmakuCount }}</span>
                    </div>
                </div>
            </div>
        </div>
    </aside>
</template>

<style scoped>
.video-sidebar {
    width: 378px;
    flex-shrink: 0;
}

/* UP主信息 */
.up-info {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
}
.up-avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background: url(@/assets/images/Akalin.png) center/cover;
}
.up-detail {
    flex: 1;
}
.up-name {
    font-size: 16px;
    font-weight: 540;
    color: #18191c;
    margin-bottom: 4px;
}
.up-sign {
    font-size: 13px;
    color: #9499a0;
    margin-bottom: 6px;
}
.up-actions {
    display: flex;
    gap: 12px;
}
.charge-btn {
    height: 32px;
    padding: 0 33px;
    border: 1px solid #00AEEC;
    color: #00AEEC;
    background: transparent;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
}
.follow-btn {
    width: 200px;
    height: 32px;
    /* padding: 0 20px; */
    background: #00AEEC;
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 14px;
    cursor: pointer;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    gap: 6px;
}

/* 弹幕列表 */
.danmaku-list-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 48px;
    padding: 0 16px;
    background: #f1f2f3;
    border-radius: 6px;
    font-size: 15px;
    color: #18191c;
    margin-bottom: 20px;
    cursor: pointer;
}

/* 推荐视频 */
.recommend-item {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
}
.rec-cover {
    width: 150px;
    height: 85px;
    border-radius: 6px;
    background-color: #f1f2f3;
    background-size: cover;
    background-position: center;
    flex-shrink: 0;
}
.rec-info {
    flex: 1;
    display: flex;
    justify-content: flex-start;
    flex-direction: column;
    /* justify-content: space-between; */
    height: 85px;
    overflow: hidden;
}
.rec-title {
    font-size: 15px;
    color: #18191c;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex-shrink: 1;
    min-height: 0;
}
.rec-meta {
    height: 22px;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    -webkit-line-clamp: 2;
    color: #9499a0;
    flex-shrink: 0;
}
.rec-uploader {
    font-size: 13px;
}
.rec-stats {
    height: 22px;
    display: flex;
    gap: 12px;
    font-size: 13px;
    color: #9499a0;
    flex-shrink: 0;
}
.rec-stats span {
    display: inline-flex;
    align-items: center;
    gap: 3px;
}

/* 图标 */
.icon {
    display: inline-block;
    width: 18px;
    height: 18px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}
/* .video-icon {
    width: 16px;
    height: 16px;
} */
.icon.icon-follow { background-image: url(@/assets/images/Stroke2addplus.png); }
.icon-up { background-image: url(@/assets/images/uploader_icon.png); }
.icon-play-sm { background-image: url(@/assets/images/playsNum_gray.png); width:16px; height:13px; }
.icon-danmaku-sm { background-image: url(@/assets/images/papernote.png); width:16px; height:16px; }
.arrow-icon {
    display: inline-block;
    width: 10px;
    height: 18px;
    background: url(@/assets/images/Frame_79_864.png) center/contain no-repeat;
}
</style>