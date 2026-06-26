<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import HomeHeaderBanner from '@/components/HomeHeaderBanner.vue'
import { getCurrentUser } from '@/utils/userStorage'
import { getAvatarUrl } from '@/api/index'

const activeTab = ref('主页')

const tabs = [
    '主页', '动态', '视频', '投稿',
    '集合和系列', '收藏', '追番追剧', '设置'
]

const user = reactive({
    nickname: '用户昵称',
    avatar: '',
    fans: 0,
    follow: 0,
    likes: 0,
    plays: 0
})

const collections = ref([
    { name: '游戏', visibility: '公开' },
    { name: '动画', visibility: '公开' },
    { name: '音乐', visibility: '私密' },
    { name: '科技', visibility: '公开' },
    { name: '学习', visibility: '公开' },
    { name: '影视', visibility: '公开' },
    { name: '收藏夹', visibility: '公开' }
])

const notice = ref('')
const navTabsRef = ref(null)
const underlineStyle = ref({ left: '0px', width: '0px' })

// 更新蓝色下划线的位置
function updateUnderline() {
    if (!navTabsRef.value) return
    const el = navTabsRef.value.querySelector('.user-space__nav-item--active')
    if (!el) return
    const parentRect = navTabsRef.value.getBoundingClientRect()
    const elRect = el.getBoundingClientRect()
    underlineStyle.value = {
        left: (elRect.left - parentRect.left) + 'px',
        width: elRect.width + 'px'
    }
}

const avatarSrc = computed(() => {
    if (user.avatar) return getAvatarUrl(user.avatar)
    return new URL('@/assets/images/Akalin.png', import.meta.url).href
})

onMounted(async () => {
    const stored = await getCurrentUser()
    if (stored) {
        user.nickname = stored.nickname || stored.username || '用户昵称'
        user.avatar = stored.avatar || ''
    }
    nextTick(updateUnderline)
})

function selectTab(tab) {
    activeTab.value = tab
    nextTick(updateUnderline)
}

// 检测标签栏点击事件
watch(activeTab, () => nextTick(updateUnderline))
</script>

<template>
    <div class="user-space">
        
        <!-- 导航栏 -->
        <div class="user-space__banner">
            <HomeHeaderBanner />
            <div class="user-space__banner-content">
                <div class="user-space__profile">
                    <img class="user-space__avatar-img" :src="avatarSrc" alt="" />
                    <div class="user-space__profile-text">
                        <span class="user-space__nickname">{{ user.nickname }}</span>
                        <button class="user-space__edit-sign">编辑个性签名</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="user-space__navbar">
            <div ref="navTabsRef" class="user-space__nav-tabs">
                <span
                    v-for="tab in tabs"
                    :key="tab"
                    class="user-space__nav-item"
                    :class="{ 'user-space__nav-item--active': activeTab === tab }"
                    @click="selectTab(tab)"
                >{{ tab }}</span>
                <div class="user-space__nav-underline" :style="underlineStyle"></div>
            </div>

            <div class="user-space__navbar-search">
                <input placeholder="搜索视频，动态" />
            </div>

            <div class="user-space__navbar-stats">
                <span class="user-space__navbar-stat">关注 <strong>{{ user.follow }}</strong></span>
                <span class="user-space__navbar-stat">粉丝 <strong>{{ user.fans }}</strong></span>
                <span class="user-space__navbar-stat">获赞 <strong>{{ user.likes }}</strong></span>
                <span class="user-space__navbar-stat">播放 <strong>{{ user.plays }}</strong></span>
            </div>
        </div>

        <div class="user-space__body">
            <div class="user-space__main">
                <div class="user-space__section">
                    <div class="user-space__section-header">
                        <span class="user-space__section-title">收藏夹</span>
                        <span class="user-space__section-count">·{{ collections.length }}</span>
                        <span class="user-space__section-more">查看更多 &gt;</span>
                    </div>
                    <div class="user-space__collection-grid">
                        <div
                            v-for="(item, i) in collections"
                            :key="i"
                            class="user-space__collection-card"
                        >
                            <div class="user-space__collection-cover"></div>
                            <div class="user-space__collection-info">
                                <span class="user-space__collection-name">{{ item.name }}</span>
                                <span class="user-space__collection-visibility">{{ item.visibility }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="user-space__sidebar">
                <div class="user-space__notice">
                    <p class="user-space__notice-title">公告</p>
                    <p class="user-space__notice-text" v-if="notice">{{ notice }}</p>
                    <p class="user-space__notice-empty" v-else>编辑我的公告</p>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.user-space {
    width: 100%;
    min-height: 100vh;
    background: #f1f2f3;
}

.user-space__banner {
    width: 100%;
    height: 201px;
    background: url(@/assets/images/space_nav@3840w_400h_1c_100q.avif) center/cover;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.user-space__banner-content {
    width: 100%;
    flex: 1;
    display: flex;
    align-items: flex-end;
    gap: 24px;
    padding: 0 65px 20px;
}



.user-space__profile {
    display: flex;
    align-items: center;
    gap: 16px;
}

.user-space__avatar-img {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    object-fit: cover;
    background: #c7c8ca;
}

.user-space__profile-text {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.user-space__nickname {
    font-size: 24px;
    color: #fff;
}

.user-space__edit-sign {
    background: none;
    border: 1px solid rgba(255, 255, 255, 0.5);
    color: rgba(255, 255, 255, 0.85);
    border-radius: 4px;
    padding: 2px 12px;
    font-size: 13px;
    cursor: pointer;
    width: fit-content;
}

.user-space__edit-sign:hover {
    background: rgba(255, 255, 255, 0.15);
}

.user-space__navbar {
    display: flex;
    align-items: center;
    height: 50px;
    padding: 0 65px;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
}

.user-space__nav-tabs {
    height: 100%;
    position: relative;
    display: flex;
    align-items: center;
    gap: 28px;
    flex-shrink: 0;
}

.user-space__nav-item {
    font-size: 15px;
    color: #61666d;
    cursor: pointer;
    /* padding-bottom: 10px; */
    transition: color 0.15s;
    user-select: none;
}

.user-space__nav-item:hover {
    transition: color 0.2s linear;
    color: #00AEEC;
}

.user-space__nav-item--active {
    color: #00AEEC;
}

.user-space__nav-underline {
    position: absolute;
    bottom: 0;
    height: 2px;
    background: #00AEEC;
    border-radius: 1px;
    transition: left 0.3s ease, width 0.3s ease;
}

.user-space__navbar-search {
    flex: 1;
    display: flex;
    justify-content: center;
    padding: 0 24px;
}

.user-space__navbar-search input {
    /* width: 360px; */
    height: 30px;
    padding: 0 12px;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
    font-size: 13px;
    outline: none;
    background: #f6f7f8;
    color: #18191c;
    transition: border-color 0.15s, background 0.15s;
}

.user-space__navbar-search input::placeholder {
    color: #9499a0;
}

.user-space__navbar-search input:focus {
    border-color: #00AEEC;
    background: #fff;
}

.user-space__navbar-stats {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-shrink: 0;
}

.user-space__navbar-stat {
    font-size: 14px;
    color: #61666d;
    display: flex;
    flex-direction: column;
    align-items: center;
    white-space: nowrap;
}

.user-space__navbar-stat strong {
    font-weight: 600;
    color: #18191c;
    margin-left: 2px;
}

.user-space__body {
    max-width: 1430px;
    margin: 0 auto;
    display: flex;
    gap: 24px;
    padding: 16px 65px;
}

.user-space__main {
    flex: 1;
    min-width: 0;
}

.user-space__section {
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
}

.user-space__section-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 14px;
}

.user-space__section-title {
    font-size: 16px;
    font-weight: 500;
    color: #18191c;
}

.user-space__section-count {
    font-size: 13px;
    color: #9499a0;
}

.user-space__section-more {
    margin-left: auto;
    font-size: 13px;
    color: #9499a0;
    cursor: pointer;
}

.user-space__section-more:hover {
    color: #00AEEC;
}

.user-space__collection-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
}

.user-space__collection-card {
    cursor: pointer;
}

.user-space__collection-cover {
    width: 100%;
    aspect-ratio: 16 / 10;
    background: #f1f2f3;
    border-radius: 6px;
    margin-bottom: 6px;
}

.user-space__collection-info {
    display: flex;
    align-items: center;
    gap: 6px;
}

.user-space__collection-name {
    font-size: 14px;
    color: #18191c;
}

.user-space__collection-visibility {
    font-size: 12px;
    color: #9499a0;
    background: #f6f7f8;
    padding: 1px 6px;
    border-radius: 3px;
}

.user-space__sidebar {
    width: 300px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.user-space__notice {
    background: #fff;
    border-radius: 8px;
    padding: 14px 16px;
}

.user-space__notice-title {
    font-size: 15px;
    font-weight: 500;
    color: #18191c;
    margin: 0 0 8px;
}

.user-space__notice-text {
    font-size: 14px;
    color: #61666d;
    margin: 0;
    line-height: 1.6;
}

.user-space__notice-empty {
    font-size: 14px;
    color: #9499a0;
    margin: 0;
    cursor: pointer;
}

.user-space__notice-empty:hover {
    color: #00AEEC;
}
</style>
