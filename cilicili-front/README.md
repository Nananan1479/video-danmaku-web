# 🎬 CiliCili — 仿哔哩哔哩视频弹幕平台
### V0.3.8

> Vue 3 + Vite + Element Plus + Axios 构建的前端项目，配合 Spring Boot 后端提供类 B 站视频弹幕体验。

## 📝 变更日志

 - 测试后端链接成功
 - 修改主页样式（浮动标题栏右侧对齐主页顶部标题栏）
 - 修改主页样式（浮动标题栏右侧对齐主页顶部标题栏）
 - 修改标题和icon
 - 添加request.js，用于封装axios请求

---

## 📋 目录

- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [快速开始](#-快速开始)
- [页面路由](#-页面路由)
- [组件一览](#-组件一览)
- [API 接口](#-api-接口)
- [后端项目](#-后端项目)

---

## 🛠 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) | ^3.2.8 |
| 构建 | Vite | ^2.5.2 |
| 路由 | Vue Router | ^4.5.1 |
| 状态管理 | Vuex | ^4.1.0 |
| UI 组件库 | Element Plus | ^2.4.4 |
| HTTP 客户端 | Axios | ^1.6.5 |
| 后端 | Spring Boot + MyBatis + MySQL | — |

---

## 📁 项目结构

```
cilicili-front/
├── index.html                          # 入口 HTML
├── vite.config.js                      # Vite 配置（端口/别名）
├── package.json
└── src/
    ├── main.js                         # 应用入口
    ├── App.vue                         # 根组件
    ├── api/
    │   └── index.js                    # 后端 API 封装
    ├── assets/
    │   └── images/                     # 静态图片资源
    ├── components/
    │   ├── HomeMain.vue                # 主页视频网格 + 轮播图容器
    │   ├── HomeMainCarousel.vue        # 主页轮播图（跨2×2网格）
    │   ├── HomeHeaderBanner.vue        # 主页顶部导航栏
    │   ├── HomeCenterBanner.vue        # 主页分类/快捷入口栏
    │   ├── FloatBanner.vue             # 滚动时浮现的固定导航栏
    │   ├── VideoCard.vue               # 视频卡片通用组件
    │   ├── Homevideo.vue               # 视频列表子组件
    │   ├── VideoPageLeft.vue           # 视频播放页左侧
    │   ├── VideoPageRight.vue          # 视频播放页右侧
    │   ├── Videopagecomment.vue        # 视频评论组件
    │   └── CarouselPointer.vue         # 轮播图指示器
    ├── router/
    │   └── index.js                    # 路由配置（懒加载）
    ├── utils/
    │   ├── request.js                  # Axios 实例封装
    │   └── userStorage.js              # 用户本地存储工具
    └── views/
        ├── Home.vue                    # 主页
        ├── VideoPage.vue               # 视频播放页
        ├── Login.vue                   # 登录页
        └── Register.vue                # 注册页
```

---

## 🚀 快速开始

### 环境要求

- **Node.js** ≥ 16
- **npm** ≥ 8

### 1. 安装依赖

```bash
cd cilicili-front
npm install
```

### 2. 启动开发服务器

```bash
# 开发模式
npm run dev

# 生产模式
npm run pro
```

开发服务器默认运行在 **http://localhost:8000** （可在 `vite.config.js` 中修改 `server.port`）。

### 3. 构建生产包

```bash
npm run build
```

构建产物输出到 `dist/` 目录。

### 4. 预览构建结果

```bash
npm run serve
```

---

## 🧭 页面路由

| 路径 | 名称 | 说明 | 懒加载 |
|------|------|------|--------|
| `/` | — | 重定向到 `/home` | — |
| `/home` | Home | 主页（视频列表 + 轮播图）| ✅ |
| `/video` | VideoPage | 视频播放详情页 | ✅ |
| `/login` | Login | 用户登录页 | ✅ |
| `/register` | Register | 用户注册页 | ✅ |

路由基于 **Vue Router 4**，使用 `createWebHistory` 模式。所有页面组件均采用路由级懒加载。

---

## 🧩 组件一览

### 主页组件

| 组件 | 职责 |
|------|------|
| `HomeHeaderBanner` | 顶部导航栏：Logo、导航链接、搜索框、用户操作区 |
| `HomeCenterBanner` | 二级栏目：动态/热门入口、分类标签、快捷链接 |
| `HomeMain` | **CSS Grid 布局容器**：5 列网格，承载轮播图 + 视频卡片 |
| `HomeMainCarousel` | 主轮播图：跨 2 列 × 2 行，支持自动播放/悬浮暂停/指示器 |
| `FloatBanner` | 滚动浮现的固定导航，由 `Home.vue` 通过 `window.scrollY` 控制显隐 |
| `VideoCard` | 可复用视频卡片：封面、播放量/评论数、时长、标题、作者信息 |
| `CarouselPointer` | 轮播图圆点指示器子组件 |

### 播放页组件

| 组件 | 职责 |
|------|------|
| `VideoPageLeft` | 视频播放区 + 视频信息 |
| `VideoPageRight` | 右侧推荐视频列表 |
| `Videopagecomment` | 视频评论区 |

---

## 🔌 API 接口

基于 **Axios** 封装，统一配置在 `src/utils/request.js` 中。

```js
// 通过 ID 获取用户
getUserById(id)     → POST /api/users/getById/{id}

// 通过用户名获取用户
getUserByName(name) → POST /api/users/getByName/{name}
```

> 接口前缀 `/api` 由 Axios 实例的 `baseURL` 配置。

---

## ☕ 后端项目

后端使用 **Spring Boot + MyBatis + MySQL**，入口类位于：

```
cilicili-back/src/main/java/com/zsn/CiliCiliApplication.java
```

Mapper 扫描路径：`com.zsn.mapper`

---

## 🔧 配置要点

- **路径别名**：`@` → `src/`，在 `vite.config.js` 中配置，`jsconfig.json` 中同步声明以支持 IDE 智能提示。
- **代理**：如需配置开发代理以解决跨域，在 `vite.config.js` 的 `server.proxy` 中设置。
- **CSS 规范**：使用 BEM 命名（如 `.carousel__dot--active`），缩进统一为 4 格。

---

