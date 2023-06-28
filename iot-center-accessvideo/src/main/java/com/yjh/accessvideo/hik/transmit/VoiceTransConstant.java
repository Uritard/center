package com.yjh.accessvideo.hik.transmit;

/**
 * 语音对讲常量
 *
 * @author 丫C
 * @date 2023/05/11
 * @since [产品/模块版本] （可选）
 */
public interface VoiceTransConstant {

    String DEVICE_LOGIN_ERROR = "语音对讲设备注册失败";

    String START_VOICE_ERROR = "语音对讲设备开启失败";

    String STOP_VOICE_ERROR = "语音对讲设备关闭失败";

    String NOT_OPEN_VOICE = "语音对讲设备未开启";

    String X86 = "amd64";

    String ARM = "aarch64";

    /**
     * PCM数据发送间隔
     * 1920 / (16000 * 1 * 16 / 8 * 1 / 1000)
     */
    int PCM_DATA_INTERVAL = 60;
    /**
     * G711数据发送间隔
     * 320 / (8000 * 1 * 16 / 8 * 1 / 1000)
     */
    int G711_DATA_INTERVAL = 20;
    /**
     * G711编码前双通道数据的大小
     */
    int G711_DATA_SIZE = 640;
    /**
     * 每次发送PCM数据的大小
     */
    int PCM_DATA_SIZE = 1920;
    /**
     * 每次发送G711数据的大小
     */
    int G711_ENCODE_DATA_SIZE = 160;
    /**
     * 随意的一个数值
     */
    long CONSTANT = 5201314L;

    enum AudioEncType {

        /**
         * 音频编码类型，G711_U
         */
        G711_U(1),
        /**
         * 音频编码类型，G711_A
         */
        G711_A(2),
        /**
         * 音频编码类型，AAC
         */
        AAC(7),
        /**
         * 音频编码类型，PCM
         */
        PCM(8);

        final int code;

        AudioEncType(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }
    }
}
