// const Forbidden = r => require.ensure([], () => r(require('@/view/error/403.vue')), '403');
// const NotFound = r => require.ensure([], () => r(require('@/view/error/404.vue')), '404');
const Home = r => require.ensure([], () => r(require('@/view/ToolIndex.vue')), 'home');
// const BatchSend = r => require.ensure([], () => r(require('@/components/BatchSend.vue')), 'batchsend');
const BatchTask = r => require.ensure([], () => r(require('@/components/BatchTask.vue')), 'batchTask');


const defaultRouter = [
    {
        path: '/',
        redirect: {
            name: 'homePage'
        }
    },
    {
        path: '/home', // 登录页 -- 用户登录鉴权
        component: Home,
        name: 'homePage'
    },
    // {
    //     path: '/403',
    //     component: Forbidden
    // },
    // {
    //     path: '/batchsend',
    //     component: BatchSend
    // },
    {
        path: '/batchTask',
        component: BatchTask
    },
    // {
    //     path: '*',
    //     component: NotFound
    // }
]

export default defaultRouter;
