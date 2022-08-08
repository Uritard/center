<template>
    <div class="main-page" style="overflow: auto;">
        <el-header style="height: 8vh;padding: 0;" :class=" isLogError ? 'alarm-animation' : '' ">
            <self-header></self-header>
        </el-header>
        <el-container style="position:relative;height: 92vh;">
            <el-aside class="menu-dev" :class="[isCollapse ? 'short-width' : 'normal-width']"
                      v-if="this.fullscreen === false">
                <img :src="arrowImg" class="arrow-img" @click="collapseClick">
                <self-aside :isCollapse="isCollapse"></self-aside>
            </el-aside>
            <transition name="fade">
                <el-main style=" height:100%;overflow: auto;"><!--flex:0 0 1700px;-->
                    <template v-if="this.$route.name === 'homePage'">
                        <home-page/>
                    </template>
                    <template v-else-if="this.$route.name === 'alarmPage'">
                        <alarm-page/>
                    </template>
                    <template v-else-if="this.$route.name === 'voicePage'">
                        <voice-page/>
                    </template>
                    <template v-else>
                        <router-view></router-view>
                    </template>
                </el-main>
            </transition>
        </el-container>
    </div>
</template>
<script>
    import HomePage from '@/views/home/Home.vue'
    import AlarmPage from '@/views/alarmManagement/AlarmManage.vue'
    import VoicePage from '@/views/voiceManagement/VoiceManage.vue'

    export default {
        name: 'home',
        components: {HomePage, AlarmPage, VoicePage},
        data() {
            return {
                isCollapse: false, // 表示展开或闭合状态
                arrowImg: require('@/assets/menuIcon/arrow_right.png'),
                fullscreen: false,
                isLogError: false,
                sitTime: null
            }
        },
        mounted() {
            this.$nextTick(() => {
                this.onTimerLoginOut();
            })
        },
        destroyed() {
            this.sitTime && clearInterval(this.sitTime);
        },
        methods: {
            /**
             * 目录展开/收缩
             * */
            collapseClick: function () {
                this.isCollapse = !this.isCollapse;
                this.arrowImg = this.isCollapse ? require('@/assets/menuIcon/arrow_right.png') : require('@/assets/menuIcon/arrow_left.png')
                this.$bus.$emit('resize')
            },
            /**
             * 前端定时判断超时退出
             * */
            onTimerLoginOut() {
                this.sitTime && clearInterval(this.sitTime);
                this.second = 0;
                this.sitTime = setInterval(_ => {
                    this.second++;
                    if (this.second > SYS_CONFIG.logoutDuration) {
                        this.sitTime && clearInterval(this.sitTime);
                        this.second = 0
                        this.$http.post('/apis/sysUser/v1/logout').then((res) => {
                            sessionStorage.clear();
                            this.$router.push('/login');
                        })
                    }
                }, 1000)
            }
        }
    };
</script>
<style lang="less">
    .main-page {
        width: 100%;
        height: 100%;
        background: url("~@/assets/background.png") center top no-repeat;
        background-size: 100% 100%;


        .menu-dev {
            position: relative;
            background: #06262C;
            box-shadow: 2px 0px 0px 0px rgba(14, 166, 172, 0.3);

            .arrow-img {
                position: absolute;
                right: -24px;
                top: 0;
                width: 24px;
                height: 66px;
                z-index: 1000;
                cursor: pointer;
            }
        }

        .alarm-div {
            position: absolute;
            right: 10px;
            top: 4px;
            /*border: 1px solid red;*/
            width: inherit;
            max-width: 30%;
            height: 48px;
            color: coral;
            font-size: 14px;
            /*letter-spacing: 2px;*/
            line-height: 48px;
        }
    }

    .el-aside {
        overflow: visible;
    }

    .el-menu-item:focus, .el-menu-item:hover {
        background-color: #649598 !important;
    }

    .el-menu--popup-right-start {
        border: 1px solid #3ae4ef !important;
        box-shadow: 2px 0px 0px 0px rgba(14, 166, 172, 0.3);
    }

    .normal-width {
        width: 200px !important;
    }

    .short-width {
        width: 68px !important;
    }

</style>
