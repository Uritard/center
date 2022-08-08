import Vue from 'vue';
import App from './App.vue';

// ElementUI
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css'

import router from './router';
import store from './store';

import './style/common.css'
import 'nprogress/nprogress.css'
import './plugin/directive' // 全局注入组件

import * as utils from './utils/util';
import * as RegExp from './utils/validate';

import http from './http/request'

import echarts from 'echarts';// echarts

import './style/element-reset.css';
import './style/base-reset.css'

Vue.use(ElementUI, {size: 'mini'});

import './components'; // 加载全局封装组件
import websocket from "./http/websocket";
 //require('./mock'); // mock模拟请求数据

Vue.config.productionTip = false; // 消息提示的环境配置开发环境-生产环境

Vue.prototype.$utils = utils; // 系统工具类
Vue.prototype.$RegExp = RegExp; // Form表单正则验证集合
Vue.prototype.$http = http;
Vue.prototype.$echarts = echarts;
Vue.prototype.$bus = new Vue();

window.vm = new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')

