import Vue from 'vue'
import App from './App.vue'
import axios from 'axios';
import http from './http/request';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

Vue.config.productionTip = false

Vue.prototype.$http = http;
Vue.prototype.$axios = axios;
Vue.prototype.$bus = new Vue();
Vue.use(ElementUI)

window.vm = new Vue({
  render: h => h(App),
}).$mount('#app')
