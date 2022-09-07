const Forbidden = r => require.ensure([], () => r(require('@/views/error/403.vue')), '403');
const NotFound = r => require.ensure([], () => r(require('@/views/error/404.vue')), '404');
const Login = r => require.ensure([], () => r(require('@/views/login/Login.vue')), 'login');
const BatchSend = r => require.ensure([], () => r(require('@/components/BatchSend.vue')), 'batchsend');
const BatchTask = r => require.ensure([], () => r(require('@/components/BatchTask.vue')), 'batchTask');
/* Router Modules */
import systemManageRouter from './modules/systemManage.js'


const defaultRouter = [
    {
        path: '/',
        redirect: {
            name: 'loginPage'
        }
    },
    {
        path: '/login', // 登录页 -- 用户登录鉴权
        component: Login,
        name: 'loginPage'
    },
    {
        path: '/403',
        component: Forbidden
    },
    {
        path: '/batchsend',
        component: BatchSend
    },
    {
        path: '/batchTask',
        component: BatchTask
    },
    {
        path: '*',
        component: NotFound
    }
]

export default defaultRouter;
