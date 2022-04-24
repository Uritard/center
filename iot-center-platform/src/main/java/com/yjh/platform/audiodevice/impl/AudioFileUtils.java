package com.yjh.platform.audiodevice.impl;

import java.io.FileOutputStream;

/**
 * <功能描述>
 *  音频文件生成工具类
 * @author xmchen
 * @date 2022/4/15
 * @since [产品/模块版本] （可选）
 */
public class AudioFileUtils {

    static int AUDIO_HEADER_LENGTH = 44;

    /**
     * 生成pcm格式音频文件
     * @param audioFilepath
     * @param headerByte
     * @param contentByte
     */
    public static void writeWavAudioFile(String audioFilepath, byte[] headerByte, byte[] contentByte) {
        try (FileOutputStream outStream = new FileOutputStream(audioFilepath)) {
            outStream.write(headerByte);
            outStream.write(contentByte);
        } catch (Exception e) {
            throw new RuntimeException("写WAV文件失败", e);
        }
    }

    /**
     * 生成音频文件头部消息
     * @param totalAudioLen 不包括header的音频数据总长度
     * @param sampleRate 采样率,也就是录制时使用的频率、音频采样级别 8000 = 8KHz
     * @param channels audioRecord的声道数1/2
     * @param audioFormat  采样精度; 譬如 16bit
     * @return
     */
    public static byte[] getWaveFileHeader(long totalAudioLen, long sampleRate, int channels,
                                            long audioFormat) {
        long totalDataLen = totalAudioLen + 36;
        byte[] header = new byte[AUDIO_HEADER_LENGTH];
        long byteRate = (sampleRate * audioFormat * channels) / 8;

        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;

        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * channels);
        header[33] = 0;
        header[34] = (byte) audioFormat;
        header[35] = 0;
        // data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }
}
