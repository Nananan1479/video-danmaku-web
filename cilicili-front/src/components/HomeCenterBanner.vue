<script setup>
const categories = [
    "番 剧", "国 创", "综 艺", "动 画", "鬼 畜", "舞 蹈", "娱 乐", "科技数码",
    "美 食", "汽 车", "体育运动", "电 影", "电 视 剧", "纪 录 片", "游 戏", "音 乐",
    "影 视", "知 识", "资 讯", "小 剧 场", "时尚美妆", "更多"
]

const quickLinks = [
    [
        { label: '专 栏', icon: 'Paper' },
        { label: '活 动', icon: 'flag_gray' },
        { label: '社区中心', icon: 'scomment' }
    ],
    [
        { label: '直 播', icon: 'video_camera' },
        { label: '课 堂', icon: 'book3fill' },
        { label: '热歌新榜', icon: 'filemusicfill' }
    ]
]
</script>

<template>
    <div class="center-banner">
        <div class="center-banner__inner">
            <!-- 左侧 -->
            <div class="center-banner__left">
                <div class="center-banner__trending-item">
                    <span class="center-banner__trending-icon center-banner__trending-icon--dynamic">
                        <img src="@/assets/images/Akalin.png" alt="动态" />
                    </span>
                    <span class="center-banner__trending-label">动态</span>
                </div>
                <div class="center-banner__trending-item">
                    <span class="center-banner__trending-icon center-banner__trending-icon--hot">
                        <span class="center-banner__hot-fire"></span>
                    </span>
                    <span class="center-banner__trending-label">热门</span>
                </div>
            </div>
            <!-- 中间 -->
            <div class="center-banner__categories">
              <div
                v-for="(category, index) in categories"
                :key="index"
                class="center-banner__category"
              >
                <span class="center-banner__category-label">{{ category }}</span>
                <span v-if="category === '更多'" class="center-banner__category-arrow"></span>
              </div>
            </div>
            <!-- 右侧 -->
            <div class="center-banner__right">
                <div class="center-banner__right-links">
                    <div
                      v-for="(link, linkIndex) in quickLinks[0]"
                      :key="link.label"
                      class="center-banner__right-link"
                    >
                        <span class="center-banner__right-icon" :class="`center-banner__right-icon--${link.icon}`"></span>
                        <span class="center-banner__right-label">{{ link.label }}</span>
                    </div>
                </div>
                <div class="center-banner__right-links center-banner__right-links--row2">
                    <div
                      v-for="(link, linkIndex) in quickLinks[1]"
                      :key="link.label"
                      class="center-banner__right-link"
                    >
                        <span class="center-banner__right-icon" :class="`center-banner__right-icon--${link.icon}`"></span>
                        <span class="center-banner__right-label">{{ link.label }}</span>
                    </div>
                  </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.center-banner {
    width: 100%;
    overflow: hidden;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
}

.center-banner__inner {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 16px 72px 16px 63px;
    width: 100%;
}

.center-banner__left {
    display: flex;
    gap: 24px;
    padding-right: 22px;
}

.center-banner__trending-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 3px;
}

/* .center-banner__trending-item:first-child {
    gap: 0;
} */

.center-banner__trending-icon {
    width: 48px;
    height: 48px;
    background-size: 100% 100%;
    background-repeat: no-repeat;
    flex-shrink: 0;
}

.center-banner__trending-icon img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.center-banner__trending-icon--hot {
    background-image: url(@/assets/images/Ellipse_65_596.png);
    position: relative;
}

.center-banner__hot-fire {
    position: absolute;
    left: 19.15%;
    right: 21.28%;
    top: 23.4%;
    bottom: 21.28%;
    background-image: url(@/assets/images/fireline.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.center-banner__trending-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #3e3e3e;
}

.center-banner__categories {
    width: 894px;
    height: 70px;
    flex-shrink: 0;
    display: grid;
    grid-template-rows: repeat(2 ,30px);
    grid-template-columns: repeat(11, 70px);
    /* flex-wrap: wrap; */
    gap: 10px;
    align-content: flex-start;
    padding-right: 24px;
}

.center-banner__category {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 0 8px;
    height: 30px;
    background-color: #f6f7f8;
    border: 1px solid #f1f2f3;
    border-radius: 7px;
    flex-shrink: 0;
}

.center-banner__category-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #5c5c5c;
    white-space: nowrap;
}

.center-banner__category-arrow {
    width: 12.58px;
    height: 18px;
    background-image: url(@/assets/images/Bace3Iconchevrondown.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
}

.center-banner__right {
    width: 259px;
    height: 66px;
    flex-shrink: 0;
    position: relative;
    border-left: 1px solid #d7d7d7;
}

.center-banner__right-links {
    position: absolute;
    left: 15px;
    top: 5px;
    display: flex;
    gap: 23px;
}

.center-banner__right-links--row2 {
    top: 40px;
}

.center-banner__right-link {
    display: flex;
    align-items: center;
    gap: 6px;
}

.center-banner__right-icon {
    width: 18px;
    height: 18px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    flex-shrink: 0;
}

.center-banner__right-icon--Paper { background-image: url(@/assets/images/Paper.png); }
.center-banner__right-icon--flag_gray { background-image: url(@/assets/images/flag_gray.png); }
.center-banner__right-icon--scomment { background-image: url(@/assets/images/scomment.png); }
.center-banner__right-icon--video_camera { background-image: url(@/assets/images/video_camera.png); }
.center-banner__right-icon--book3fill { background-image: url(@/assets/images/book3fill.png); }
.center-banner__right-icon--filemusicfill { background-image: url(@/assets/images/filemusicfill.png); }

.center-banner__right-label {
    font-size: 14px;
    font-family: "Noto Sans SC-Regular", sans-serif;
    font-weight: 400;
    color: #61666d;
    white-space: nowrap;
}
</style>
