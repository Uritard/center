const SYS_CONFIG = {
    homePageTimerStep: 10000,   //ms
    isBuild: true,
    stationName: '南京亿嘉和变电站11',
    wsServer:'ws://192.168.10.194:12140/ws',
    logoutDuration: 60 * 60, // 60分钟
    barHeight:'33' // 时域波形起伏大小调节（一档：33，二档：xxx,三档：xxx,四档：xxx,）
}
const SITUATION_WEBSOCKET = 'ws://192.168.40.168:11013/ws'
const YC_SITUATION_WEBSOCKET = 'ws://192.168.40.71:11013/ws'
