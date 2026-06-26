<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { currentUserRef, logoutUser } from '@/utils/userStorage'
import { getAvatarUrl } from '@/api/index'

const props = defineProps({
    avatarSrc: {
        type: String,
        required: true
    }
})

const router = useRouter()
const wrapRef = ref(null)
const isVisible = ref(false)
const popoverStyle = ref({})
let showTimer = null
let hideTimer = null

function updatePosition() {
    if (!wrapRef.value) return
    const rect = wrapRef.value.getBoundingClientRect()
    popoverStyle.value = {
        position: 'fixed',
        top: rect.bottom + 10 + 'px',
        left: rect.left + rect.width / 2 + 'px',
        marginLeft: '-130px'
    }
}

onMounted(() => {
    window.addEventListener('scroll', updatePosition, true)
    window.addEventListener('resize', updatePosition)
})

onBeforeUnmount(() => {
    window.removeEventListener('scroll', updatePosition, true)
    window.removeEventListener('resize', updatePosition)
})

const user = computed(() => currentUserRef.value)

const displayName = computed(() => {
    return user.value?.nickname || user.value?.username || '未登录'
})

const signature = computed(() => {
    return user.value?.signature || '这个人很懒，什么都没写~'
})

const bigAvatarSrc = computed(() => {
    if (user.value?.avatar) {
        return getAvatarUrl(user.value.avatar)
    }
    return props.avatarSrc
})

function onMouseEnter() {
    if (!user.value) return
    clearTimeout(hideTimer)
    updatePosition()
    showTimer = setTimeout(() => {
        isVisible.value = true
    }, 200)
}

function onMouseLeave() {
    clearTimeout(showTimer)
    hideTimer = setTimeout(() => {
        // 原本为false
        isVisible.value = false
    }, 150)
}

function onPopoverEnter() {
    clearTimeout(hideTimer)
}

function onPopoverLeave() {
    hideTimer = setTimeout(() => {
        isVisible.value = false
    }, 150)
}

function goToUserCenter() {
    isVisible.value = false
    router.push('/userAccount')
}

function handleLogout() {
    logoutUser()
    isVisible.value = false
    router.push('/login')
}
</script>

<template>
    <div
        ref="wrapRef"
        class="user-avatar-wrap"
        @mouseenter="onMouseEnter"
        @mouseleave="onMouseLeave"
    >
        <slot />

        <Teleport to="body">
            <Transition name="popover">
                <div
                    v-if="isVisible"
                    class="user-popover"
                    :style="popoverStyle"
                    @mouseenter="onPopoverEnter"
                    @mouseleave="onPopoverLeave"
                >
                <!-- <div class="user-popover__arrow"></div> -->

                    <div class="user-popover__header">
                        <div class="user-popover__avatar">
                            <img :src="bigAvatarSrc" alt="" />
                        </div>
                        <div class="user-popover__info">
                            <div class="user-popover__name">{{ displayName }}</div>
                            <div class="user-popover__uid">
                                UID: {{ user?.id || '---' }}
                            </div>
                        </div>
                    </div>

                    <div class="user-popover__signature">{{ signature }}</div>

                    <div class="user-popover__stats">
                        <div class="user-popover__stat">
                            <span class="user-popover__stat-num">0</span>
                            <span class="user-popover__stat-label">粉丝</span>
                        </div>
                        <div class="user-popover__stat">
                            <span class="user-popover__stat-num">0</span>
                            <span class="user-popover__stat-label">关注</span>
                        </div>
                        <div class="user-popover__stat">
                            <span class="user-popover__stat-num">0</span>
                            <span class="user-popover__stat-label">获赞</span>
                        </div>
                    </div>

                    <div class="user-popover__menu">
                        <div class="user-popover__menu-item" @click="goToUserCenter">
                            <span class="user-popover__menu-icon">👤</span>
                            <span>个人中心</span>
                        </div>
                        <div class="user-popover__menu-item">
                            <span class="user-popover__menu-icon">📺</span>
                            <span>我的视频</span>
                        </div>
                        <div class="user-popover__menu-item">
                            <span class="user-popover__menu-icon">📋</span>
                            <span>稍后再看</span>
                        </div>
                        <div class="user-popover__menu-item">
                            <span class="user-popover__menu-icon">⚙</span>
                            <span>设置</span>
                        </div>
                        <div
                            class="user-popover__menu-item user-popover__menu-item--logout"
                            @click="handleLogout"
                        >
                            <span class="user-popover__menu-icon">🚪</span>
                            <span>退出登录</span>
                        </div>
                    </div>
                </div>
            </Transition>
        </Teleport>
    </div>
</template>

<style scoped>
.user-avatar-wrap {
    position: relative;
    display: inline-flex;
}
</style>

<style>
.user-popover {
    width: 260px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 36px rgba(0, 0, 0, 0.15);
    padding: 18px 20px 12px;
    z-index: 9999;
}

.user-popover__header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;
}

.user-popover__avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    background: #f1f2f3;
}

.user-popover__avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.user-popover__name {
    font-size: 15px;
    font-weight: 600;
    color: #18191c;
    margin-bottom: 2px;
}

.user-popover__uid {
    font-size: 12px;
    color: #9499a0;
}

.user-popover__signature {
    font-size: 13px;
    color: #61666d;
    line-height: 1.4;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.user-popover__stats {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
}

.user-popover__stat {
    display: flex;
    align-items: center;
    gap: 3px;
}

.user-popover__stat-num {
    font-size: 14px;
    font-weight: 600;
    color: #18191c;
}

.user-popover__stat-label {
    font-size: 12px;
    color: #9499a0;
}

.user-popover__menu {
    display: flex;
    flex-direction: column;
}

.user-popover__menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 6px;
    border-radius: 6px;
    font-size: 14px;
    color: #18191c;
    cursor: pointer;
    transition: background 0.12s;
}

.user-popover__menu-item:hover {
    background: #f6f7f8;
}

.user-popover__menu-item--logout {
    color: #e06c6c;
    margin-top: 2px;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;
    border-radius: 0 0 6px 6px;
}

.user-popover__menu-item--logout:hover {
    background: #fff1f0;
}

.user-popover__menu-icon {
    font-size: 16px;
    width: 20px;
    text-align: center;
}

.popover-enter-active {
    transition: opacity 0.15s ease, transform 0.15s ease;
}

.popover-leave-active {
    transition: opacity 0.1s ease, transform 0.1s ease;
}

.popover-enter-from {
    opacity: 0;
    transform: translateY(-4px);
}

.popover-leave-to {
    opacity: 0;
    transform: translateY(-4px);
}
</style>
