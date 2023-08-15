/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessvideo.hik.transmit;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/5/18
 * @since [产品/模块版本] （可选）
 */
public class AudioFormatUtil {

    public static void main(String[] args) {
        String wavFilePath = "D:\\testFile\\cnhc-1-16000.wav";
        String pcmFilePath = "D:\\testFile\\cnhc-1-16000.pcm";
//        convertAudioFiles(wavFilePath, pcmFilePath);

        String srcFilePath = "D:\\testFile\\g7\\originAudio-20230629155214.g7";
        String destFilePath = "D:\\testFile\\pcm\\originAudio-20230629155214.pcm";
        decodeTest(srcFilePath, destFilePath);

        String srcFilePath2 = "D:\\testFile\\pcm\\audio-1-16-8000.pcm";
        String destFilePath2 = "D:\\testFile\\g7\\audio-1-16-8000.g7";
//        encodeTest(srcFilePath2, destFilePath2);

        HashMap<Long, Integer> hashMap = new HashMap<>();
        hashMap.remove(123);
    }

    public static void coverToWav(String path, long sampleRate, int channels, long audioFormat, int format) {
        try {
            FileInputStream voiceFile = new FileInputStream(path);
            int dataLength = voiceFile.available();
            byte[] ptrVoiceByte = new byte[dataLength];
            voiceFile.read(ptrVoiceByte);

            byte[] head = AudioFileUtils.getWaveFileHeader(dataLength, sampleRate, channels, audioFormat, format);
            String toPath = path + "_to.wav";
            AudioFileUtils.writeWavAudioFile(toPath, head, ptrVoiceByte);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encodeTest(String srcFilePath, String destFilePath) {
        try {
            FileInputStream voiceFile = new FileInputStream(srcFilePath);
            int dataLength = voiceFile.available();
            byte[] ptrVoiceByte = new byte[dataLength];
            voiceFile.read(ptrVoiceByte);
            byte[] encode = encode(ptrVoiceByte);

            File fileEncode = new File(destFilePath);
            if (!fileEncode.exists()) {
                fileEncode.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileEncode);

            fileOutputStream.write(encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decodeTest(String srcFilePath, String destFilePath) {
        try {
            FileInputStream voiceFile = new FileInputStream(srcFilePath);
            int dataLength = voiceFile.available();
            byte[] ptrVoiceByte = new byte[dataLength];
            voiceFile.read(ptrVoiceByte);

            File fileEncode1 = new File(destFilePath + "二.pcm");
            if (!fileEncode1.exists()) {
                fileEncode1.createNewFile();
            }
            FileOutputStream fileOutputStream1 = new FileOutputStream(fileEncode1, true);

            // 每次解码160字节
            int iEncodeSize = 0;
            while ((dataLength - iEncodeSize) > 160 || ((dataLength - iEncodeSize) > 0 && (dataLength - iEncodeSize) <= 160)){
                byte[] g711Byte = new byte[160];
                int length = Math.min((dataLength - iEncodeSize), 160);
                System.arraycopy(ptrVoiceByte, iEncodeSize, g711Byte, 0, length);

                iEncodeSize += 160;
                byte[] decode = decode(g711Byte);
                fileOutputStream1.write(decode);
            }


            byte[] decode = decode(ptrVoiceByte);

            File fileEncode = new File(destFilePath);
            if (!fileEncode.exists()) {
                fileEncode.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(fileEncode);

            fileOutputStream.write(decode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static short[] aLawDecompressTable = new short[]
            { -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784, -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368, -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392, -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944, -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136, -11008, -10496, -12032, -11520, -8960, -8448, -9984, -9472, -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568, -344, -328, -376,
                    -360, -280, -264, -312, -296, -472, -456, -504, -488, -408, -392, -440, -424, -88, -72, -120, -104, -24, -8, -56, -40, -216, -200, -248, -232, -152, -136, -184, -168, -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184, -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592, -944, -912, -1008, -976, -816, -784, -880, -848, 5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784, 2752, 2624,
                    3008, 2880, 2240, 2112, 2496, 2368, 3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392, 22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944, 30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136, 11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296, 472, 456, 504, 488, 408, 392, 440, 424, 88, 72, 120, 104, 24, 8, 56, 40, 216, 200, 248, 232, 152, 136, 184, 168, 1376, 1312, 1504, 1440, 1120,
                    1056, 1248, 1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696, 688, 656, 752, 720, 560, 528, 624, 592, 944, 912, 1008, 976, 816, 784, 880, 848 };
    private static byte[] aLawCompressTable = new byte[]
            { 1, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
    private final static int C_CLIP = 32635;

    /**
     * PCM编码为G711 a-law
     *
     * @param b 输入数据
     */
    public static byte[] encode(byte[] b) {
        int j = 0;
        int count = b.length / 2;
        short sample = 0;
        byte[] res = new byte[count];
        for (int i = 0; i < count; i++) {
            sample = (short) (((b[j++] & 0xff) | (b[j++]) << 8));
            res[i] = linearToAlawSample(sample);
        }
        return res;
    }

    public static byte[] encode(byte[] b, int pcmSampleRate) {
        int srcLen = b.length;
        int g7SampleRate = 8000;
        double arc = pcmSampleRate * 1.0D / g7SampleRate;
        int destLen = (int)(Math.ceil(srcLen / arc));
        int lastPos = srcLen - 1;

        int j = 0;
        int count = destLen / 2;

        short sample = 0;
        byte[] res = new byte[count];
        for (int i = 0; i < count; i++) {
            double index = i * arc;
            int p1 = (int)index;
            double coef = index - p1;
            int p2 = p1 < lastPos ? p1 + 1 : p1;
            byte d1 = (byte)((1 - coef) * b[p1] + coef * b[p2]);

            double index2 = i * arc * 2;
            int p3 = (int)index2;
            /*if (p3 > lastPos) {
                break;
            }*/
            double coef2 = index2 - p3;
            int p4 = p3 < lastPos ? p3 + 1 : p3;
            byte d2 = (byte)((1 - coef2) * b[p3] + coef2 * b[p4]);

            sample = (short)((d1 & 0xff) | d2 << 8);
            res[i] = linearToAlawSample(sample);
        }
        return res;
    }

    /**
     * G711 a-law解码为PCM
     *
     * @param b 输入数据
     */
    public static byte[] decode(byte[] b) {
        int j = 0;
        byte[] res = new byte[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            short s = aLawDecompressTable[b[i] & 0xff];
            res[j++] = (byte) s;
            res[j++] = (byte) (s >> 8);
        }
        return res;
    }

    private static byte linearToAlawSample(short sample ){
        int sign;
        int exponent;
        int mantissa;
        int s;

        sign = ( ( ~sample ) >> 8 ) & 0x80;
        if ( !( sign == 0x80 ) )
        {
            sample = (short) -sample;
        }
        if ( sample > C_CLIP )
        {
            sample = C_CLIP;
        }
        if ( sample >= 256 )
        {
            exponent = (int) aLawCompressTable[( sample >> 8 ) & 0x7F];
            mantissa = ( sample >> ( exponent + 3 ) ) & 0x0F;
            s = ( exponent << 4 ) | mantissa;
        }
        else
        {
            s = sample >> 4;
        }
        s ^= ( sign ^ 0x55 );
        return (byte) s;
    }

    /**
     * WAV转PCM文件
     *
     * @param wavFilePath wav文件路径
     * @param pcmFilepath pcm要保存的文件路径及文件名
     */
    public static String convertAudioFiles(String wavFilePath, String pcmFilepath) {
        FileInputStream fileInputStream;
        FileOutputStream fileOutputStream;
        try {
            fileInputStream = new FileInputStream(wavFilePath);
            fileOutputStream = new FileOutputStream(pcmFilepath);
            byte[] wavByte = inputStreamToByte(fileInputStream);
            byte[] pcmByte = Arrays.copyOfRange(wavByte, 44, wavByte.length);
            fileOutputStream.write(pcmByte);
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pcmFilepath;
    }

    /**
     * 输入流转byte二进制数据
     *
     * @param fis 输入流
     */
    private static byte[] inputStreamToByte(FileInputStream fis) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        long size = fis.getChannel().size();
        byte[] buffer = null;
        if (size <= Integer.MAX_VALUE) {
            buffer = new byte[(int) size];
        } else {
            buffer = new byte[8];
            for (int ix = 0; ix < 8; ++ix) {
                int offset = 64 - (ix + 1) * 8;
                buffer[ix] = (byte) ((size >> offset) & 0xff);
            }
        }
        int len;
        while ((len = fis.read(buffer)) != -1) {
            byteStream.write(buffer, 0, len);
        }
        byte[] data = byteStream.toByteArray();
        IOUtils.closeQuietly(byteStream);
        return data;
    }
}
