<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import HomeMainCarousel from './HomeMainCarousel.vue'
import VideoCard from './VideoCard.vue'

const router = useRouter();

const videos = ref([
    {
        id: 2,
        title: "杀疯了！DeepSeek 视觉模型让AI绕过权限直接操作电脑",
        author: "黑鸦Heya",
        date: "22小时前",
        views: "5.7万",
        comments: "136",
        duration: "02:13"
    },
    {
        id: 3,
        title: "【空洞骑士】无伤四锁五门，这就是纯粹容器的含金量",
        author: "末影龙校长",
        date: "04-27",
        views: "6224",
        comments: "22",
        duration: "59:43"
    },
    {
        id: 4,
        title: "这就是GTA6还不敢发布的原因！看完笑死",
        author: "Q宏大大",
        date: "昨天",
        views: "44.2万",
        comments: "441",
        duration: "01:18"
    },
    {
        id: 7,
        title: "智能辅助驾驶越用越觉得视野受限怎么办？",
        author: "差评君",
        date: "04-22",
        views: "52.6万",
        comments: "343",
        duration: "01:46"
    },
    {
        id: 8,
        title: "华强买瓜但程序员版，梦开始的地方！",
        author: "尕得法克尔",
        date: "04-04",
        views: "62.8万",
        comments: "638",
        duration: "01:46"
    },
    {
        id: 9,
        title: "新地图玩固定翼飞机，爽到飞起！",
        author: "咖喱FPS",
        date: "04-19",
        views: "30.8万",
        comments: "1091",
        duration: "15:12"
    },
    {
        id: 10,
        title: "【城】开源Agent Hermes，让AI自己写代码部署网站",
        author: "网络小白_Uncle",
        date: "04-28",
        views: "4.8万",
        comments: "217",
        duration: "13:12"
    },
    {
        id: 11,
        title: "晚饭后，来上这首首都高架兜风曲听听看吧",
        author: "娱乐玩家李维斯",
        date: "04-27",
        views: "9584",
        comments: "1",
        duration: "01:24"
    },
    {
        id: 12,
        title: "空之神殿 原神支援 - 从零开始的原神之旅",
        author: "円学",
        date: "04-13",
        views: "31.7万",
        comments: "40",
        duration: "09:14"
    },
    {
        id: 20,
        title: "一人一狗组合灭鼠，效率高到离谱！",
        author: "城市猎人飞哥",
        date: "04-12",
        views: "53.9万",
        comments: "4467",
        duration: "06:13"
    },
    {
        id: 21,
        title: "看韩国达人 Gwak 如何玩转气垫板，太上头了",
        author: "mory今天也要早睡",
        date: "04-26",
        views: "140.1万",
        comments: "164",
        duration: "06:29"
    }
])

function handleVideoClick(video) {
    router.push({ name: 'VideoPage', query: { id: video.id } })
}
</script>

<template>
    <div class="home-main">
        <div class="home-main__grid">
            <HomeMainCarousel />

            <VideoCard
                v-for="video in videos"
                :key="video.id"
                :video="video"
                @click="handleVideoClick(video)"
            />
        </div>
    </div>
</template>

<style scoped>
.home-main {
    width: 1430px;
    display: flex;
    justify-content: center;
}

.home-main__grid {
    display: grid;
    grid-template-columns: repeat(5, 236.6px);
    column-gap: 28px;
    row-gap: 16px;
    padding: 0 72px 0 63px;
}

/* 轮播图跨2列2行 */
.home-main__grid :deep(.carousel) {
    grid-column: span 2;
    grid-row: span 2;
}
</style>
