package com.yjh.platform.audiodevice.impl.standard.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/14
 */
public class DataGroup {

    /**
     * 数据类型: 0x0A 表示语音
     */
    private byte sType;

    /**
     * 采样率: 48000、 32000 等
     */
    private int sampleRate;

    /**
     * 位深: 支持 16 位
     */
    private byte bits;

    /**
     * N 个通道
     */
    private byte channels;

    private List<byte[]> audioDatas;

    public DataGroup(byte[] data) {
        this(data, 2048);
    }

    public DataGroup(byte[] data, int bytesPerChannel) {
        this.bytesPerChannel = bytesPerChannel;
        parse(data);
    }

    private void parse(byte[] data) {
        int offset = 0;
        sType = data[offset++];
        sampleRate = (Byte.toUnsignedInt(data[offset]) << 8) | Byte.toUnsignedInt(data[offset + 1]);
        offset += 2;
        bits = data[offset++];
        channels = data[offset++];
        audioDatas = new ArrayList<>(channels);
        for (; offset < data.length; offset += bytesPerChannel) {
            byte[] audioData = new byte[bytesPerChannel];
            System.arraycopy(data, offset, audioData, 0, bytesPerChannel);
            audioDatas.add(audioData);
        }
    }

    public int getBytesPerChannel() {
        return bytesPerChannel;
    }

    private final int bytesPerChannel;

    public byte getsType() {
        return sType;
    }

    public DataGroup setsType(byte sType) {
        this.sType = sType;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public DataGroup setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public byte getBits() {
        return bits;
    }

    public DataGroup setBits(byte bits) {
        this.bits = bits;
        return this;
    }

    public byte getChannels() {
        return channels;
    }

    public DataGroup setChannels(byte channels) {
        this.channels = channels;
        return this;
    }

    public List<byte[]> getAudioDatas() {
        return audioDatas;
    }

    public DataGroup setAudioDatas(List<byte[]> audioDatas) {
        this.audioDatas = audioDatas;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataGroup dataGroup = (DataGroup) o;
        return sType == dataGroup.sType && sampleRate == dataGroup.sampleRate && bits == dataGroup.bits && channels == dataGroup.channels && bytesPerChannel == dataGroup.bytesPerChannel && Objects.equals(audioDatas, dataGroup.audioDatas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sType, sampleRate, bits, channels, audioDatas, bytesPerChannel);
    }

    @Override
    public String toString() {
        return "DataGroup{" +
                "sType=" + sType +
                ", sampleRate=" + sampleRate +
                ", bits=" + bits +
                ", channels=" + channels +
                ", bytesPerChannel=" + bytesPerChannel +
                '}';
    }
}
