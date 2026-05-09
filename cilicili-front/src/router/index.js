import { createRouter, createWebHistory  } from "vue-router";

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

export default router;
