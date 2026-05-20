import { createRouter, createWebHistory  } from "vue-router";
import { USER_TOKEN_KEY } from "@/constants/constants";
const routes = [
    {
        path: "/",
        redirect: "/home"
    },
    {
        path: "/home", 
        name: "Home", 
        component: () => import("@/views/Home.vue"),
        meta: { guid: "35:1290" }
    },
    {
        path: "/video", 
        name: "VideoPage", 
        component: () => import("@/views/VideoPage.vue"),
        meta: { guid: "65:508" }
    },
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/Login.vue")
    },
    {
        path: "/register",
        name: "Register",
        component: () => import("@/views/Register.vue")
    },
    {
        path: "/test",
        name: "Test",
        component: () => import("@/views/Test.vue"),
        meta: { requiresAuth: true }  // 标记需要登录才能访问
    }
];

const routePathMap = new Map();

export const getRoutePathByGuid = (guid) => {
    if (!guid) return;
    if (routePathMap.has(guid)) return routePathMap.get(guid);

    const route = routes.find((item) => item.meta?.guid === guid);
    if (!route) return;
    routePathMap.set(guid, route.path);

    return route.path;
}

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem(USER_TOKEN_KEY)
    if (to.meta.requiresAuth && !token) {
        next('/home');   // 未登录则强制跳转首页
    } else {
        next();
    }
})

export default router;
