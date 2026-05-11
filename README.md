# 🎬 CiliCili — 仿哔哩哔哩视频弹幕平台

一个前后端分离的类 B 站视频分享与弹幕互动平台。（未完成）

***

## 📋 目录

- [项目简介](#-项目简介)
- [版本信息](#-版本信息)
- [技术架构](#-技术架构)
- [整体结构](#-整体结构)
- [前端项目](#-前端项目-cilicili-front)
- [后端项目](#-后端项目-cilicili-back)
- [快速开始](#-快速开始)
- [API 接口](#-api-接口)
- [页面路由](#-页面路由)
- [组件一览](#-组件一览)
- [数据库配置](#-数据库配置)

***

## 📖 项目简介

CiliCili 是一个模仿 B 站（哔哩哔哩）的视频弹幕平台，实现视频浏览、搜索、播放、评论等核心功能。

- **前端**：Vue 3 + Vite + Element Plus，采用 CSS Grid 布局实现类 B 站 5 列视频卡片主页
- **后端**：Spring Boot 2.7 + MyBatis-Plus + MySQL，提供 RESTful API 接口

***

## 🏷 版本信息

| 模块                          | 版本                 | 说明                                     |
| --------------------------- | ------------------ | -------------------------------------- |
| **前端项目** (`cilicili-front`) | **V0.3.2**         | package.json → `0.2.0` / UI 迭代至 V0.3.2 |
| **后端项目** (`cilicili-back`)  | **0.1.0-SNAPSHOT** | pom.xml 当前版本                           |
| Vue 3                       | ^3.2.8             | Composition API + `<script setup>`     |
| Vite                        | ^2.5.2             | 极速构建工具                                 |
| Vue Router                  | ^4.5.1             | 前端路由（History 模式）                       |
| Vuex                        | ^4.1.0             | 状态管理                                   |
| Element Plus                | ^2.4.4             | UI 组件库                                 |
| Axios                       | ^1.6.5             | HTTP 客户端                               |
| Spring Boot                 | 2.7.18             | Java Web 框架                            |
| MyBatis-Plus                | 3.4.3              | ORM 框架                                 |
| MySQL Connector             | 8.0.33             | 数据库驱动                                  |
| Java                        | 1.8                | JDK 版本                                 |
| Lombok                      | —                  | 简化 Java 代码                             |

***

## 🏗 技术架构

```
┌─────────────────────────────────────────────────┐
│                    浏览器                        │
│          Vue 3 + Element Plus (SPA)              │
└────────────────┬────────────────────────────────┘
                 │  HTTP / Axios
                 ▼
┌─────────────────────────────────────────────────┐
│              Vite Dev Server :8000               │
│            (开发代理可转发至 :6060)              │
└─────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│           Spring Boot :6060 (后端)               │
│   Controller → Service → Mapper → MySQL         │
│              cili_videodb 数据库                 │
└─────────────────────────────────────────────────┘
```

***

## 📁 整体结构

```
video-danmu web/
├── README.md                          # ← 你在这里
├── cilicili-front/                    # 前端项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js                    # 应用入口
│       ├── App.vue                    # 根组件
│       ├── api/index.js               # 后端 API 封装
│       ├── router/index.js            # 路由配置
│       ├── utils/
│       │   ├── request.js             # Axios 实例
│       │   └── userStorage.js         # 本地存储工具
│       ├── components/                # 通用组件
│       └── views/                     # 页面视图
└── cilicili-back/                     # 后端项目
    ├── pom.xml
    └── src/main/
        ├── java/com/zsn/
        │   ├── CiliCiliApplication.java
        │   ├── controller/UserController.java
        │   ├── service/UserService.java
        │   ├── mapper/UserMapper.java
        │   └── entity/User.java
        └── resources/
            └── application.yml
```

***

## 🎨 前端项目 (cilicili-front)

### 启动

```bash
cd cilicili-front
npm install                # 安装依赖
npm run dev                # 开发模式 → http://localhost:8000
npm run pro                # 生产模式
npm run build              # 构建 → dist/
npm run serve              # 预览构建结果
```

### 技术栈表

| 技术           | 版本     | 用途      |
| ------------ | ------ | ------- |
| Vue 3        | ^3.2.8 | 前端框架    |
| Vite         | ^2.5.2 | 构建工具    |
| Vue Router   | ^4.5.1 | 路由      |
| Vuex         | ^4.1.0 | 状态管理    |
| Element Plus | ^2.4.4 | UI 组件   |
| Axios        | ^1.6.5 | HTTP 请求 |

### 核心特性

- **CSS Grid 主页布局**：5 列视频卡片网格，轮播图跨 2 列 × 2 行
- **自动轮播**：`HomeMainCarousel` 支持自动播放、鼠标悬浮暂停、指示器切换
- **浮动导航栏**：页面下滚后浮现固定导航栏
- **BEM 命名**：CSS 统一采用 BEM 规范（如 `.carousel__dot--active`）
- **4 格缩进**：全项目统一 4 格缩进风格
- **路由懒加载**：所有页面组件 `() => import(...)`
- **Axios 封装**：`request.js` 统一请求拦截与错误处理

***

## ☕ 后端项目 (cilicili-back)

### 启动

```bash
cd cilicili-back
# 1. 确保 MySQL 运行，创建数据库 cili_videodb
# 2. 修改 src/main/resources/application.yml 中的数据库连接信息
# 3. 使用 IDE（IDEA / Eclipse）导入 Maven 项目并运行
mvn spring-boot:run       # 或使用 Maven 命令
```

服务默认运行在 \*\*<http://localhost:6060**。>

### 技术栈表

| 技术              | 版本     | 用途           |
| --------------- | ------ | ------------ |
| Spring Boot     | 2.7.18 | Web 框架       |
| MyBatis-Plus    | 3.4.3  | ORM          |
| MySQL Connector | 8.0.33 | 数据库驱动        |
| Druid           | 1.2.5  | 监控和管理的数据库连接池 |
| Java            | 1.8    | 运行环境         |
| Lombok          | —      | 代码简化         |
| Maven           | —      | 构建与依赖管理      |

### 项目分层

```
Controller (UserController)    ← 接收 HTTP 请求
    ↓
Service (UserServiceImpl)      ← 业务逻辑
    ↓
Mapper (UserMapper)            ← 数据库操作（MyBatis-Plus）
    ↓
Entity (User)                  ← 数据实体
```

- 启动类：[CiliCiliApplication.java](cilicili-back/src/main/java/com/zsn/CiliCiliApplication.java)
- Mapper 扫描路径：`com.zsn.mapper`（MyBatis-Plus 自动代理）
- 数据库：`cili_videodb` → 端口 `6060`

***

## 🚀 快速开始（完整流程）

### 环境要求

| 工具      | 最低版本 |
| ------- | ---- |
| Node.js | ≥ 16 |
| npm     | ≥ 8  |
| JDK     | 1.8  |
| Maven   | 3.6+ |
| MySQL   | 8.0  |

### 1. 启动后端

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cili_videodb DEFAULT CHARSET utf8mb4"

# 进入后端目录
cd cilicili-back

# 修改 application.yml 中的数据库用户名/密码

# 启动
mvn spring-boot:run
# 后端运行在 http://localhost:6060
```

### 2. 启动前端

```bash
cd cilicili-front
npm install
npm run dev
# 前端运行在 http://localhost:8000
```

### 3. 访问

打开浏览器访问 **<http://localhost:8000>**

***

## 🔌 API 接口

| 接口                            | 方法   | 说明             |
| ----------------------------- | ---- | -------------- |
| `/api/users/getById/{id}`     | POST | 通过用户 ID 获取用户信息 |
| `/api/users/getByName/{name}` | POST | 通过用户名获取用户信息    |

接口前缀 `/api` 由前端 Axios 实例 `baseURL` 统一配置。

***

## 🧭 页面路由

| 路径          | 页面        | 说明              |
| ----------- | --------- | --------------- |
| `/`         | —         | 重定向到 `/home`    |
| `/home`     | Home      | 主页：轮播图 + 视频卡片网格 |
| `/video`    | VideoPage | 视频播放详情页（左右布局）   |
| `/login`    | Login     | 用户登录页           |
| `/register` | Register  | 用户注册页           |

***

## 🧩 前端组件

| 组件                 | 层级  | 职责                 |
| ------------------ | --- | ------------------ |
| `HomeHeaderBanner` | 主页  | 顶部导航：Logo、搜索框、用户操作 |
| `HomeCenterBanner` | 主页  | 二级导航：分类标签、快捷入口     |
| `HomeMain`         | 主页  | 5 列 Grid 容器        |
| `HomeMainCarousel` | 主页  | 轮播图（跨 2×2 网格）      |
| `FloatBanner`      | 全局  | 滚动浮现的固定导航栏         |
| `VideoCard`        | 通用  | 可复用视频卡片            |
| `CarouselPointer`  | 通用  | 轮播图圆点指示器           |
| `VideoPageLeft`    | 播放页 | 视频播放区 + 视频信息       |
| `VideoPageRight`   | 播放页 | 右侧推荐视频列表           |
| `Videopagecomment` | 播放页 | 评论区                |

***

## 🗄 数据库配置

```yaml
# cilicili-back/src/main/resources/application.yml
server:
  port: 6060

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cili_videodb
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
......
```

> ⚠️ 部署前请修改 `username` 和 `password` 为你自己的 MySQL 凭据。

***

## ⚙️ 配置要点

- **跨域**：如需开发代理，在 `cilicili-front/vite.config.js` 中配置 `server.proxy`
- **端口**：前端 `:8000`，后端 `:6060`
- **路径别名**：前端 `@` → `src/`
- **CSS 规范**：BEM 命名 + 4 格缩进

***

## 📝 变更日志

| 日期         | 版本     | 变更内容                                                  |
| ---------- | ------ | ----------------------------------------------------- |
| 2026-05-09 | V0.3.2 | 主页 Grid 布局重构、轮播图跨 2×2 网格、浮动标题栏修复、ARIA 可访问性增强、缩进统一 4 格 |
| —          | V0.3.0 | Axios 封装 request.js、标题/icon 替换                        |
| —          | V0.2.0 | Element Plus 集成、登录页完善、FloatBanner 重构                  |
| —          | V0.1.0 | 项目初始化：Vue 3 + Vite + Spring Boot 基础模板                 |

