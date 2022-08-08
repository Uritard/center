let voiceData = {
    code: 200,
    data: {
        count: 27197,
        'list|20': [
            {
                absolutePath: "/home/yjh/voice/voiceFile/YJHAS203/2021-12-29/YJHAS203_20211229111714-20211229111814.wav",
                audioData: null,
                audioFormat: null,
                audioName: "YJHAS203_20211229111714-20211229111814.wav",
                audioPath: "http://192.168.40.168:10010/files/YJHAS203/2021-12-29/YJHAS203_20211229111714-20211229111814.wav",
                audioTime: "2021-12-29 11:17:14",
                deviceId: 1,
                deviceName: "好奇变电站",
                deviceType: 136,
                deviceTypeName: "声纹音频设备",
                endTime: "2021-12-29 11:18:14",
                fileId: 800000027196,
                orientation: 142,
                orientationName: "铭牌正面（1#）",
                phasePosition: 141,
                phasePositionName: "C相",
                productName: "203",
                productSn: "YJHAS203",
                remark: null,
                samplingRate: 16000,
                voiceDeviceId: 910034
            }
        ]
    }
}

export default {
    'get|/apis/tVoiceFile/v1/selectByPage': voiceData,
}