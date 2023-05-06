package com.yjh.accessvideo.hik.transmit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 生成WAV头
 * @author 丫C
 * @date 2023/4/27
 */
public class WaveHeader {
    private final char[] fileId = {'R', 'I', 'F', 'F'};
    public int fileLength;
    private final char[] wavTag = {'W', 'A', 'V', 'E'};
    private final char[] fmtHdrId = {'f', 'm', 't', ' '};
    public int fmtHdrLength;
    public short formatTag;
    public short channels;
    public int samplesPerSec;
    public int avgBytesPerSec;
    public short blockAlign;
    public short bitsPerSample;
    private final char[] dataHdrId = {'d', 'a', 't', 'a'};
    public int dataHdrLength;

    public byte[] getHeader() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeChar(bos, fileId);
        writeInt(bos, fileLength);
        writeChar(bos, wavTag);
        writeChar(bos, fmtHdrId);
        writeInt(bos, fmtHdrLength);
        writeShort(bos, formatTag);
        writeShort(bos, channels);
        writeInt(bos, samplesPerSec);
        writeInt(bos, avgBytesPerSec);
        writeShort(bos, blockAlign);
        writeShort(bos, bitsPerSample);
        writeChar(bos, dataHdrId);
        writeInt(bos, dataHdrLength);
        bos.flush();
        byte[] header = bos.toByteArray();
        bos.close();
        return header;
    }

    private void writeShort(ByteArrayOutputStream bos, int s) throws IOException {
        byte[] byteTemp = new byte[2];
        byteTemp[1] = (byte) ((s << 16) >> 24);
        byteTemp[0] = (byte) ((s << 24) >> 24);
        bos.write(byteTemp);
    }


    private void writeInt(ByteArrayOutputStream bos, int n) throws IOException {
        byte[] byteTemp = new byte[4];
        byteTemp[3] = (byte) (n >> 24);
        byteTemp[2] = (byte) ((n << 8) >> 24);
        byteTemp[1] = (byte) ((n << 16) >> 24);
        byteTemp[0] = (byte) ((n << 24) >> 24);
        bos.write(byteTemp);
    }

    private void writeChar(ByteArrayOutputStream bos, char[] id) {
        for (int i = 0; i < id.length; i++) {
            char c = id[i];
            bos.write(c);
        }
    }
}
