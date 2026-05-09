import './assets/styles/common.css'
import './assets/styles/global.css'
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import './styles.css'
import './assets/styles/font.css'
import App from './App.vue'
import router from './router/index.js'
import request from './utils/request.js'


createApp(App).use(router).use(ElementPlus).use(request).mount('#app')

