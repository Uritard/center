<template>
    <div class="app">
        <router-view v-if="isRouterAlive"></router-view>
    </div>
</template>

<script>
    import websocket from "./http/websocket";

    export default {
        name: 'App',
        provide() {
            return {
                reload: this.reload,
                ws_platform:null
            };
        },
        data() {
            return {
                isRouterAlive: true,

            };
        },
        destroyed() {
        },
        mounted() {
            this.connectWebsocket();
            /*this.$nextTick(_ => {
                this.onTimerLoginOut();
            })*/
        },
        methods: {
            connectWebsocket() {
                if (sessionStorage.getItem('userId') == null) {
                    return;
                }
                if (this.ws_platform) return;
                this.ws_platform = new websocket({
                    wxurl: YC_SITUATION_WEBSOCKET,
                    userId: sessionStorage.getItem('userId')
                })
                this.ws_platform.onmessageWS((msgData) => {
                    if (msgData == '连接成功') return
                    let oo = JSON.parse(msgData)
                    if (oo.type == 'latestTrueAlarm') {
                        this.$message.success(oo.name)
                        this.$bus.$emit('latestTrueAlarm', oo.name)
                    }
                })
            },
            /**
             * 重载页面
             * */
            reload() {
                this.isRouterAlive = false;
                this.$nextTick(() => {
                    this.isRouterAlive = true;
                });
            },

        }
    };
</script>

<style>

</style>
