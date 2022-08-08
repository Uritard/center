<template>
    <!--频谱-->
    <div style="height: 100%;width: 100%;">
        <!-- 为wavesurfer.js开辟一个空间，用来画图 -->
        <div id="waveformDom" ref="waveform" class="waveform" style="height: calc(100% - 80px);position: relative;">
<!--                        <el-image :src="imgUrl" style="position:absolute;top:0;left:0;width: 100%;height: 100%;">-->
<!--                            <div slot="error" class="image-slot">-->
<!--                                <i class="el-icon-picture-outline"></i>-->
<!--                            </div>-->
<!--                        </el-image>-->
        </div>
        <!--时间轴 -->
        <div id="wave-timeline" ref="wave-timeline" style="background-color: #082634;height: 20px;"></div>
        <!--控制按钮-->
        <div style="" class="control" v-if="showControl">
            <div class="control-div" style="">
                <span class="play-span" v-if="isPlay"
                      style="display: inline-block;width: 40px;height: 40px;cursor: pointer;" @click="plays"></span>
                <span class="pause-span" v-else style="display: inline-block;width: 40px;height: 40px;cursor: pointer;"
                      @click="plays"></span>
                <span class="stop-span" style="display: inline-block;width: 15px;height: 15px;cursor: pointer;"
                      @click="replay"></span>
                <div class="time-div" v-if="showTimePanel">
                    <span class="text-span">倍速</span>
                    <el-button size="mini" style="width: 60px;" round @click="DoubleSpeed(index)"> {{ speed[index] +' X'
                        }}
                    </el-button>
                    <span class="text-span"
                          style="margin: 0 0 0 40px;display: inline-block;width: 100px;text-align: right;">{{currentTime}}</span>
                    <span class="text-span" style="color: #000000;">/</span>
                    <span class="text-span"
                          style="color: #000000;margin: 0;display: inline-block;width: 120px;text-align: left;">{{duration}}</span>
                </div>
                <div style="display: flex;align-items: center;">
                                    <span class="volume-span"
                                          style="display: inline-block;width: 20px;height: 16px;margin: 0 10px"></span>
                    <div class="block" style="width: 120px;margin-left: 10px;">
                        <el-slider v-model="volumeValue" @change="setVolume"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import WaveSurfer from 'wavesurfer.js' // 导入wavesurfer.js
    import Timeline from 'wavesurfer.js/dist/plugin/wavesurfer.timeline.js' // 导入时间轴插件
    // import CursorPlugin from 'wavesurfer.js/dist/plugin/wavesurfer.cursor.js' // 导入光标插件
    export default {
        name: 'FrequencySpectrum',
        components: {},
        props: ['audioSrc', 'showControl', 'showTimePanel', 'imgUrl'],
        data() {
            return {
                wavesurfer: null, // 频谱对象
                isPlay: false, // 播放或暂停
                ds: 1.00, // 播放倍速
                volumeValue: 66, // 设置音量
                index: 1,
                speed: [0.5, 1.0, 1.5, 2.0], // 倍速的范围
                duration: '00:00:00', // 音频总时间
                currentTime: '00:00:00', // 音频当前时间
                zoomValue: 0
            }
        },
        watch: {
            audioSrc(val) {
                this.loadAudio()
            }
        },
        created() {

        },
        mounted() {
            this.waveSurferInit()
            // this.$nextTick(_ => {
            //
            // })
        },
        destroyed() {
        },
        methods: {

            /**
             * 创建一个wavesurfer实例
             * */
            waveSurferInit() {
                if (this.wavesurfer) {
                    this.wavesurfer.destroy() // 销毁实例 重新创建
                } else {
                    // 创建一个wavesurfer实例
                    this.wavesurfer = WaveSurfer.create({
                        // 应该在其中绘制波形的CSS选择器或HTML元素。这是唯一必需的参数。
                        container: this.$refs.waveform,
                        // 光标的填充颜色，指示播放头的位置。
                        cursorColor: 'red',
                        // 更改波形容器的背景颜色。
                        backgroundColor: '#000000',
                        // 光标后的波形填充颜色。
                        waveColor: '#3F9D6E',
                        // 光标后面的波形部分的填充色。当progressColor和waveColor相同时，完全不渲染进度波
                        progressColor: 'purple',
                        backend: 'MediaElement',
                        // 音频播放时间轴
                        mediaControls: false,
                        // 播放音频的速度
                        audioRate: 1,
                        barHeight: SYS_CONFIG.barHeight,
                        // 插件：配置了光标插件和时间轴插件
                        plugins: [
                            // 光标插件
                            // CursorPlugin.create({
                            //     showTime: true,
                            //     opacity: 1,
                            //     customShowTimeStyle: {
                            //         'background-color': '#000',
                            //         color: '#fff',
                            //         padding: '2px',
                            //         'font-size': '10px'
                            //     }
                            // }),
                            // 时间轴插件
                            Timeline.create({
                                container: '#wave-timeline',
                                fontSize: 12,
                                notchPercentHeight: 22, // 刻度线的长度  是百分比
                                unlabeledNotchColor: 'red', // 	 没有标签的刻度线（槽口）的颜色
                                labelPadding: -25, // 刻度时间距离刻度线的距离
                                primaryFontColor: 'white', // 部分时间数字的颜色 10,20,30 这种
                                primaryColor: 'white', // 主要时间标签颜色
                                secondaryFontColor: 'white', // 刻度值 和其对应刻度线的颜色
                                secondaryColor: 'white',
                                offset: 0, // 时间轴的偏移量以秒为单位
                                // formatTimeCallback: this._formatTimeCallback,
                                timeInterval: this._timeInterval,
                                primaryLabelInterval: this._primaryLabelInterval,
                                secondaryLabelInterval: this._secondaryLabelInterval,
                            })
                        ]
                    });
                }
                // 设置初始音量
                this.wavesurfer.setVolume(this.volumeValue / 100)
                // this.wavesurfer.load(require('./aa.mp3'));
                // this.zoomValue = this.wavesurfer.params.minPxPerSec - 1; // 1s对应的像素宽度
                // this.wavesurfer.zoom(this.zoomValue - 1);
                // 加载成功
                this.wavesurfer.on('ready', () => {
                    // this.wavesurfer.play()
                    // 获取总时长
                    let duration = parseInt(this.wavesurfer.getDuration());
                    this.duration = this.$utils.secToTime(duration)
                    // console.log('原始值：  ' + this.wavesurfer.getDuration())
                    // console.log('ready  ' + duration)
                })
                // 播放中
                this.wavesurfer.on('audioprocess', () => {
                    // 获取当前时间
                    this.currentTime = this.$utils.secToTime(parseInt(this.wavesurfer.getCurrentTime(), 10));
                    // console.log('当前时间是： ' + this.$utils.secToTime(this.wavesurfer.getCurrentTime()));
                });
                // 播放结束
                this.wavesurfer.on('finish', () => {
                    this.isPlay = !this.isPlay
                });
                // 播放出错
                this.wavesurfer.on('error', (err) => {
                    console.log(err)
                    this.$message.error('音频文件出错，' + err)
                });
            },
            // 开始加载音频
            loadAudio() {
                if (this.audioSrc) {
                    this.wavesurfer.load(this.audioSrc);
                    if (!this.showControl) {
                        this.plays()
                    }
                } else {
                    this.$message.warning('暂无音频来源！')
                }
            },
            /**
             * 音频控制事件
             * */
            // 播放时暂停，暂停时播放
            plays() {
                this.isPlay = !this.isPlay
                this.wavesurfer.playPause()
            },
            // 后退，
            backPlay() {
                this.wavesurfer.skip(-3) // 从当前位置跳数秒（使用负值向后移动）
            },
            // 前进，
            forwardPlay() {
                this.wavesurfer.skip(3)
            },
            // 重放 -- 停止
            replay() {
                this.isPlay = false
                this.wavesurfer.stop()
            },
            // 设置音量：
            setVolume(val) {
                this.wavesurfer.setVolume(val / 100)
            },
            // 倍速
            DoubleSpeed(index) {
                if (index === 3) {
                    this.index = 0
                    this.wavesurfer.setPlaybackRate(this.speed[this.index])
                } else {
                    this.index = index + 1
                    this.wavesurfer.setPlaybackRate(this.speed[this.index])
                }
            },
            setZoom() {
                this.wavesurfer.zoom(this.zoomValue);
            },
            // 重写时间数字的格式：
            _formatTimeCallback(seconds, pxPerSec) {
                seconds = Number(seconds)
                var minutes = Math.floor(seconds / 60)
                seconds = seconds % 60
                var secondsStr = Math.round(seconds).toString()
                if (pxPerSec >= 25 * 10) {
                    secondsStr = seconds.toFixed(2)
                } else if (pxPerSec >= 25 * 1) {
                    secondsStr = seconds.toFixed(1)
                }
                if (minutes > 0) {
                    if (seconds < 10) {
                        secondsStr = '0' + secondsStr
                    }
                    return `${minutes}:${secondsStr}`
                }
                return secondsStr
            },
            /**
             * @param pxPerSec
             * @return 以分钟为单位的值   重写时间间隔数，以分钟为单位的持续时间：
             */
            _timeInterval(pxPerSec) { // pxPerSec 值越大， 音频总时间越短
                var retval = 10 // 一个间隔是10分钟
                // if (pxPerSec >= 100) { // 0.5,1,1.5,2,...,9.5,10
                //     retval = 0.5
                // } else if (pxPerSec >= 80) { // 1,2,...,9,10
                //     retval = 1
                // } else if (pxPerSec >= 60) { // 2,4,6,8,10
                //     retval = 2
                // } else if (pxPerSec >= 40) { // 5,10
                //     retval = 1
                // } else if (pxPerSec >= 20) {
                //     retval = 5
                // } else {
                //     retval = Math.ceil(0.5 / pxPerSec) * 30  // 30分钟一个间隔
                // }
                return retval
            },
            // 重写主要时间标签的数量
            _primaryLabelInterval(pxPerSec) {
                var retval = 1
                if (pxPerSec >= 100) {
                    retval = 2
                } else if (pxPerSec >= 80) {
                    retval = 1
                } else if (pxPerSec >= 60) {
                    retval = 1
                } else if (pxPerSec >= 40) {
                    retval = 5
                } else if (pxPerSec >= 20) {
                    retval = 2
                } else {
                    retval = 2 // 一个大间隔中有2个空
                }
                return retval
            },
            // 重写次要时间标签的数量：
            _secondaryLabelInterval(pxPerSec) {
                if (pxPerSec >= 20 && pxPerSec < 40) {
                    return 12
                } else if (pxPerSec >= 0 && pxPerSec < 20) {
                    return 10
                } else {
                    return Math.floor(10 / this._timeInterval(pxPerSec))
                }
            }
        }
    }
</script>

<style scoped lang="less">
    .waveform wave {
        height: 100%;
    }

    .control {
        display: flex;
        background: #14738A;
        height: 60px;

        .control-div {
            width: 100%;
            display: flex;
            align-items: center;
            position: relative;
            /*border-right: 1px solid #01070C;*/

            .text-span {
                font-size: 16px;
                color: #ffffff;
                font-weight: 500;
            }

            .play-span {
                background: url('~@/assets/audioIcon/pause.png') center center no-repeat;
            }

            .pause-span {
                background: url('~@/assets/audioIcon/play.png') center center no-repeat;
            }

            .stop-span {
                background: url('~@/assets/audioIcon/stop_video.png') center center no-repeat;
            }

            .volume-span {
                background: url('~@/assets/audioIcon/volume.png') center center no-repeat;
            }
        }

        .time-div > span {
            margin: 0 10px;
        }

        .control-div > span {
            margin: 0 10px;
        }
    }

    /deep/ .waveform wave {
        height: 100% !important;
    }

    /deep/ .el-slider__runway {
        background-color: #0B1D23;
    }

    /deep/ .el-slider__bar {
        background-color: #FFF
    }
</style>
