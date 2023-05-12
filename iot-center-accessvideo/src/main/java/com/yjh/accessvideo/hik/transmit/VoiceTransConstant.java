package com.yjh.accessvideo.hik.transmit;

/**
 * 语音对讲常量
 *
 * @author 丫C
 * @date 2023/05/11
 * @since [产品/模块版本] （可选）
 */
public interface VoiceTransConstant {

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
