import './assets/styles/common.css'
import './assets/styles/global.css'
import 'element-plus/dist/index.css'
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import './styles.css'
import './assets/styles/font.css'
import App from './App.vue'
import router from './router/index.js'
import { createPinia } from 'pinia'

const pinia = createPinia()



createApp(App).use(router).use(ElementPlus).use(pinia).mount('#app')

