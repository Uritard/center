package com.yjh.accessvideo.hik.transmit;

import lombok.Data;

/**
 * 语音对讲音频参数结构体
 *
 * @author 丫C
 * @date 2023/4/26
 */
@Data
public class CompressionAudio {
    /**
     * 音频编码类型
     * 0- G722，1- G711_U，2- G711_A，5- MP2L2，6- G726，7- AAC，8- PCM，9-G722，
     * 10-G723，11-G729，12-AAC_LC，13-AAC_LD，14-Opus，15-MP3，16-ADPCM
     */
    private byte byAudioEncType;

    /**
     * 音频采样率
     * 0- 默认，1- 16kHZ，2- 32kHZ，3- 48kHZ，4- 44.1kHZ，5- 8kHZ
     */
    private byte byAudioSamplingRate;

    /**
     * 音频码率
     */
    private byte byAudioBitRate;

    private byte[] byres;

    private byte bySupport;
}
