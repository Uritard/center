/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.audiodevice.impl.standard.tcp;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author zilong
 * @date 2022/4/14
 */
public class Packet {
    private static final byte[] HEAD = {(byte) 0xFC, (byte) 0xFC, (byte) 0xFC, (byte) 0xFC};
    private static final byte END = (byte) 0xFD;

    private static final int HEAD_SIZE = 4;
    private static final int VERSION_SIZE = 1;
    private static final int PAYLOAD_LENGTH_SIZE = 4;
    private static final int MINIMUM_PARSING_SIZE = HEAD_SIZE + VERSION_SIZE + PAYLOAD_LENGTH_SIZE;

    /**
     * 包号: “请求开始” 时， 包号置为 0， 逐帧连续累加， 直至“请求结束”。
     */
    private long timestamp;

    /**
     * 预留
     */
    private byte[] basicInfo = new byte[6];

    /**
     * 设备 ID
     */
    private byte[] deviceId = new byte[6];

    /**
     * 软件版本 n.n.n
     */
    private String firmware;

    /**
     * 硬件版本 n
     */
    private String hardware;

    /**
     * 预留
     */
    private byte[] protocol = new byte[2];

    /**
     * 预留
     */
    private byte[] flag = new byte[3];

    /**
     * 0:PCM,1:g711a
     */
    AudioEncoding encoding;

    /**
     * 数据结构
     */
    private DataGroup dataGroup;

    /**
     * CRC16 校验
     */
    private byte[] crc = new byte[2];

    private Packet() {
    }

    private static int calcDataGroupSize(int dataSize) {
        return dataSize - (8 + 6 + 6 + 3 + 1 + 2 + 3 + 1 + 2 + 1);
    }

    public static Packet tryParse(IoBuffer in) {
        if (in.remaining() < MINIMUM_PARSING_SIZE) {
            //数据长度不够解析(断包)，重置读位置，放弃此轮解析
            in.reset();
            return null;
        }

        //检查开始标志
        for (int i = 0; i < HEAD.length; ++i) {
            if (in.get() != HEAD[i]) {
                in.reset();
                //开始标志没检测到，跳过多余的字节字节
                in.skip(i + 1);
                return null;
            }
        }

        in.order(ByteOrder.BIG_ENDIAN);

        //包格式版本
        byte version = in.get();
        int remainingLength = in.getInt();
        //剩下的数据不够，重置读位置，放弃此轮解析
        if (in.remaining() < remainingLength) {
            in.reset();
            return null;
        }

        Packet packet = new Packet();

        packet.timestamp = in.getLong();
        in.get(packet.basicInfo);
        in.get(packet.deviceId);
        packet.firmware = String.format("%d.%d.%d", in.get(), in.get(), in.get());
        packet.hardware = String.format("%d", in.get());
        in.get(packet.protocol);
        in.get(packet.flag);
        packet.encoding = AudioEncoding.getInstance(in.get());
        int dataGroupSize = calcDataGroupSize(remainingLength);
        byte[] dataGroupBytes = new byte[dataGroupSize];
        in.get(dataGroupBytes);
        //调试作用，当version是0x7F时，认为每个通道数据时两个字节
        if (0x7F == version) {
            packet.dataGroup = new DataGroup(dataGroupBytes, 2);
        } else {
            packet.dataGroup = new DataGroup(dataGroupBytes);
        }

        packet.crc[0] = in.get();
        packet.crc[1] = in.get();
        //TODO: CRC校验

        byte eop0 = in.get();
        if (eop0 != END) {
            throw new IllegalArgumentException(String.format("Unexpected ending bytes: 0X%2X", eop0));
        }

        return packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Packet setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public byte[] getBasicInfo() {
        return basicInfo;
    }

    public Packet setBasicInfo(byte[] basicInfo) {
        this.basicInfo = basicInfo;
        return this;
    }

    public byte[] getDeviceId() {
        return deviceId;
    }

    public Packet setDeviceId(byte[] deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getFirmware() {
        return firmware;
    }

    public Packet setFirmware(String firmware) {
        this.firmware = firmware;
        return this;
    }

    public String getHardware() {
        return hardware;
    }

    public Packet setHardware(String hardware) {
        this.hardware = hardware;
        return this;
    }

    public byte[] getProtocol() {
        return protocol;
    }

    public Packet setProtocol(byte[] protocol) {
        this.protocol = protocol;
        return this;
    }

    public byte[] getFlag() {
        return flag;
    }

    public Packet setFlag(byte[] flag) {
        this.flag = flag;
        return this;
    }

    public AudioEncoding getEncoding() {
        return encoding;
    }

    public Packet setEncoding(AudioEncoding encoding) {
        this.encoding = encoding;
        return this;
    }

    public DataGroup getDataGroup() {
        return dataGroup;
    }

    public Packet setDataGroup(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
        return this;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "timestamp=" + timestamp +
                ", basicInfo=" + Arrays.toString(basicInfo) +
                ", deviceId=" + Arrays.toString(deviceId) +
                ", firmware='" + firmware + '\'' +
                ", hardware='" + hardware + '\'' +
                ", protocol=" + Arrays.toString(protocol) +
                ", flag=" + Arrays.toString(flag) +
                ", encoding=" + encoding +
                ", dataGroup=" + dataGroup +
                '}';
    }
}
