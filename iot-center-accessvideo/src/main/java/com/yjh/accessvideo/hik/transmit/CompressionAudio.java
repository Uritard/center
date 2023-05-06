package com.yjh.accessvideo.hik.transmit;

import lombok.Data;

/**
 * @author 丫C
 * @date 2023/4/26
 */
@Data
public class CompressionAudio {
    private byte byAudioEncType;
    private byte byAudioSamplingRate;
    private byte byAudioBitRate;
    private byte[] byres;
    private byte bySupport;
}
