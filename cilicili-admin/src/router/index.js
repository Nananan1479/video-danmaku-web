import { createRouter, createWebHistory } from 'vue-router'
import { USER_TOKEN_KEY } from '@/constants/userSettingConstants.js'

const routes = [
    {
        path: '/',
        redirect: '/admin'
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/cilicili-admin.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.path === '/login' && token) {
        next('/admin')
    } else {
        next()
    }
})

export default router
