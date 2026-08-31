import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
// 业务页面包含 Dock、右键菜单和玻璃合成层，统一抬高 Element Plus 浮层基准，
// 让 Dialog、MessageBox、Select/DatePicker 等仍按组件内部顺序递增。
app.use(ElementPlus, { locale: zhCn, zIndex: 12000 })

app.mount('#app')
