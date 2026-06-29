<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendVideos } from '@/api/index'
import { fetchVideos } from '@/utils/videoData'
import HomeMainCarousel from './HomeMainCarousel.vue'
import VideoCard from './VideoCard.vue'

const router = useRouter();

const videos = ref([
    {
        id: 2,
        title: "杀疯了！DeepSeek 视觉模型让AI绕过权限直接操作电脑",
        playCount: 57000,
        danmakuCount: 420,
        commentCount: 136,
        duration: 133,
        coverUrl: "",
        updatedAt: "2026-05-20 14:30:00"
    },
    {
        id: 3,
        title: "【空洞骑士】无伤四锁五门，这就是纯粹容器的含金量",
        playCount: 6224,
        danmakuCount: 58,
        commentCount: 22,
        duration: 3583,
        coverUrl: "",
        updatedAt: "2026-04-27 09:15:00"
    },
    {
        id: 4,
        title: "这就是GTA6还不敢发布的原因！看完笑死",
        playCount: 442000,
        danmakuCount: 1200,
        commentCount: 441,
        duration: 78,
        coverUrl: "",
        updatedAt: "2026-05-21 22:10:00"
    },
    {
        id: 7,
        title: "智能辅助驾驶越用越觉得视野受限怎么办？",
        playCount: 526000,
        danmakuCount: 890,
        commentCount: 343,
        duration: 106,
        coverUrl: "",
        updatedAt: "2026-04-22 16:45:00"
    },
    {
        id: 8,
        title: "华强买瓜但程序员版，梦开始的地方！",
        playCount: 628000,
        danmakuCount: 2100,
        commentCount: 638,
        duration: 106,
        coverUrl: "",
        updatedAt: "2026-04-04 11:20:00"
    },
    {
        id: 9,
        title: "新地图玩固定翼飞机，爽到飞起！",
        playCount: 308000,
        danmakuCount: 3500,
        commentCount: 1091,
        duration: 912,
        coverUrl: "",
        updatedAt: "2026-04-19 08:00:00"
    },
    {
        id: 10,
        title: "【城】开源Agent Hermes，让AI自己写代码部署网站",
        playCount: 48000,
        danmakuCount: 156,
        commentCount: 217,
        duration: 792,
        coverUrl: "",
        updatedAt: "2026-04-28 19:30:00"
    },
    {
        id: 11,
        title: "晚饭后，来上这首首都高架兜风曲听听看吧",
        playCount: 9584,
        danmakuCount: 12,
        commentCount: 1,
        duration: 84,
        coverUrl: "",
        updatedAt: "2026-04-27 23:55:00"
    },
    {
        id: 12,
        title: "空之神殿 原神支援 - 从零开始的原神之旅",
        playCount: 317000,
        danmakuCount: 280,
        commentCount: 40,
        duration: 554,
        coverUrl: "",
        updatedAt: "2026-04-13 12:10:00"
    },
    {
        id: 20,
        title: "一人一狗组合灭鼠，效率高到离谱！",
        playCount: 539000,
        danmakuCount: 8900,
        commentCount: 4467,
        duration: 373,
        coverUrl: "",
        updatedAt: "2026-04-12 17:25:00"
    },
    {
        id: 21,
        title: "看韩国达人 Gwak 如何玩转气垫板，太上头了",
        playCount: 1401000,
        danmakuCount: 520,
        commentCount: 164,
        duration: 389,
        coverUrl: "",
        updatedAt: "2026-04-26 10:40:00"
    }
])
const total = ref(0)
const pageNum = ref(1)
// TODO: 后续改为滚动加载更多（无限滚动），当前临时取全部视频
const pageSize = ref(1000)
const loading = ref(false)

// 获取视频数据
fetchVideos(pageNum.value, pageSize.value).then(res => {
    videos.value = res.videos
    total.value = res.total
})

</script>

<template>
    <div class="homeMain">
        <div class="homeMain_grid">
            <HomeMainCarousel class="HomeMainCarousel" />

            <VideoCard
                v-for="video in videos"
                :key="video.id"
                :video="video"
            />
        </div>
    </div>
</template>

<style scoped>
.homeMain {
    width: 100%;
    /* max-width: 1430px; */
    display: flex;
    justify-content: center;
}

.homeMain_grid {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(236.6px, 1fr));
    column-gap: 28px;
    row-gap: 16px;
    padding: 0 64px 0 60px;
    /* padding: 0 72px 0 63px; */
}

/* 选中第 3 行及以后的卡片，（每行3列，前两行共6个，n+7 即从第7个开始） */
.homeMain_grid :deep(.video-card:nth-child(n+8)) {
    margin-top: 40px;
}

@media (max-width: 1374px) {
    .homeMain_grid :deep(.video-card:nth-child(n+6)) {
        margin-top: 30px;
    }
}

/* 轮播图跨2列2行 */
/* .homeMain__grid :deep(.carousel) {
    grid-column: span 2;
    grid-row: span 2;
} */

@media (max-width: 2560px) {
    .homeMain_grid {
        grid-template-columns: repeat(5, minmax(200px, 1fr));
        column-gap: 20px;
        padding: 0 102px 0 93px;
    }
}

@media (max-width: 1599px) {
    .homeMain_grid {
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        column-gap: 20px;
        padding: 0 60px 0 60px;
    }
}

@media (max-width: 1135px) {
    .homeMain_grid {
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        column-gap: 16px;
        padding: 0 52px 0 44px;
        row-gap: 12px;
    }
    /* .homeMain_grid :deep(.video-card:nth-child(n+8)) {
        margin-top: 0;
    } */
}

@media (max-width: 944px) {
    .homeMain_grid {
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        column-gap: 16px;
        padding: 0 52px 0 44px;
        row-gap: 12px;
    }
    .homeMain_grid :deep(.video-card:nth-child(n)) {
        margin-top: 20px;
    }
    .HomeMainCarousel {
        display: none;
    }
}

@media (max-width: 730px) {
    .homeMain_grid {
        grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
        column-gap: 12px;
        padding: 0 22px 0 14px;
        row-gap: 10px;
    }
    .homeMain_grid :deep(.video-card:nth-child(n)) {
        margin-top: 20px;
    }
    .HomeMainCarousel {
        display: none;
    }
}

@media (max-width: 480px) {
    .homeMain_grid {
        grid-template-columns: repeat(2, 1fr);
        column-gap: 8px;
        padding: 0 8px;
        row-gap: 8px;
    }
    .homeMain_grid :deep(.video-card:nth-child(n)) {
        margin-top: 20px;
    }
    .HomeMainCarousel {
        display: none;
    }
}
</style>
