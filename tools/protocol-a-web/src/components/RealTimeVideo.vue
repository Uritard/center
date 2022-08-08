<template>
    <!--频谱-->
    <div ref="canvasDiv" style="height: 68%;width: 100%;">
        <canvas style="height:100%;width:100%;margin: 0 auto;border: 1px solid #265A60;" id="canvas"></canvas>
    </div>
</template>

<script>
    export default {
        name: 'RealTimeVideo',
        components: {},
        props: ['productSn'],
        data() {
            return {
                wsServer: 'ws://192.168.10.194:12140/ws/10001-44123456', // webSocket地址
                currentProductSn:'',
                objSocket: null,
                canvas: false, // 播放或暂停
                ctx: false, // 播放或暂停
                context: false, // 播放或暂停
            }
        },
        watch: {
            productSn(newVal,oldVal) {
                this.changeWS(newVal)
            }
        },
        created() {
        },
        mounted() {
            this.canvas = document.getElementById("canvas");
            this.ctx = this.canvas.getContext("2d");

            //Web Audio API
            try {
                this.context = new (window.AudioContext || window.webkitAudioContext)();
            } catch (e) {
                alert('您当前的浏览器不支持Web Audio API ');
            }
            // this.changeWS()

        },
        destroyed() {
            if(this.objSocket){
                this.objSocket.close()
            }
        },
        methods: {
            changeWS:function(productionSn){
                if(this.objSocket){
                    this.objSocket.close()
                }
                this.wsServer = SYS_CONFIG.wsServer + '/' + productionSn + new Date().getTime();
                var that = this
                /**
                 WebSocket服务连接
                 */
                this.objSocket = new WebSocket(this.wsServer);
                this.objSocket.onopen = function (evt) {
                    console.log("Connected to WebSocket server.");
                };
                this.objSocket.onclose = function (evt) {
                    console.log("Connected to WebSocket server.");
                };
                this.objSocket.onmessage = function (evt) {
                    if (evt.data == '连接成功') return;
                    var reader = new FileReader(); //文件阅读器
                    reader.readAsArrayBuffer(evt.data); //读取成ArrayBuffer对象

                    reader.onload = function () { //读取完毕
                        //解码
                        that.context.decodeAudioData(this.result, function (buffer) {
                            that.playSound(buffer);
                        }, function (e) {
                            "Error with decoding audio data" + e.err
                        });
                    }
                };
                this.objSocket.onerror = function (evt) {
                    console.log("Connected to WebSocket server.");
                };
            },
            playSound: function (buffer) {
                var that = this
                var source = this.context.createBufferSource(); // 创建一个声音源
                source.buffer = buffer; // 告诉该源播放何物
                source.connect(this.context.destination); //将该源与硬件相连
                source.start(0); // 开始播放

                var analyser = this.context.createAnalyser();
                analyser.smoothingTimeConstant = 0.85;
                analyser.fftSize = 32;//傅里叶变换参数 简化成16个元素数组
                //将source与分析器连接
                source.connect(analyser);
                const bufferLength = analyser.frequencyBinCount;
                const dataArray = new Uint8Array(bufferLength);

                analyser.getByteFrequencyData(dataArray);

                this.canvas.width = this.$refs.canvasDiv.offsetWidth;
                this.canvas.height = this.$refs.canvasDiv.offsetHeight;

                var WIDTH = this.canvas.width;
                var HEIGHT = this.canvas.height;

                var barWidth = WIDTH / bufferLength * 1.25;
                var barHeight;

                function renderFrame() {
                    requestAnimationFrame(renderFrame);
                    analyser.getByteFrequencyData(dataArray);
                    that.ctx.clearRect(0, 0, WIDTH, HEIGHT);
                    for (var i = 0, x = 0; i < bufferLength; i++) {
                        barHeight = dataArray[i];
                        var r = barHeight + 25 * (i / bufferLength);
                        var g = 250 * (i / bufferLength);
                        var b = 50;

                        that.ctx.fillStyle = "rgb(" + r + "," + g + "," + b + ")";
                        that.ctx.fillRect(x, HEIGHT - barHeight, barWidth, barHeight);

                        x += barWidth + 2;
                    }
                }

                renderFrame();
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
