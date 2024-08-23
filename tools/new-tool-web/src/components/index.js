import Vue from 'vue';

const header = r => require.ensure([], () => r(require('./Header.vue')), 'header');
const aside = r => require.ensure([], () => r(require('./Aside.vue')), 'aside');
const TableRollingEffect = r => require.ensure([], () => r(require('./TableRollingEffect.vue')), 'TableRollingEffect');

Vue.component('self-header', header);
Vue.component('self-aside', aside);
Vue.component('table-rolling-effect', TableRollingEffect);
