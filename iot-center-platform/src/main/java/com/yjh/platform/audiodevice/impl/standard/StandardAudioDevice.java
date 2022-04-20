package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.impl.AudioFileUtils;
import com.yjh.platform.audiodevice.impl.standard.tcp.InboundMessage;
import com.yjh.platform.audiodevice.impl.standard.tcp.Packet;
import com.yjh.platform.common.mqtt.MqttUtilsServer;
import com.yjh.platform.module.device.entity.AuidoOprInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 通过国网标准接口接入的声纹设备
 *
 * @author zilong
 * @date 2022/4/14
 */
@Slf4j
public class StandardAudioDevice implements AudioDevice {
    private final String deviceId;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final List<Packet> packets = new LinkedList<>();
    private final ReadWriteLock packetsLock = new ReentrantReadWriteLock();
    private final MqttUtilsServer mqttUtilsServer;
    /**
     * 限制一下最大的录音数据，一帧数据差不多是4096个字节，限制单个录音文件不超过100MB
     */
    private final static int MAX_BUFFERED_PACKETS = (100 * 1024 * 1024) / 4096;

    public StandardAudioDevice(String deviceId, MqttUtilsServer mqttUtilsServer) {
        this.deviceId = deviceId;
        this.mqttUtilsServer = mqttUtilsServer;
    }

    public void onAudioData(InboundMessage inboundMessage) {
        if (!isRecording.get()) {
            log.warn("非录音时段收到录音数据: {}", inboundMessage);
            return;
        }

        log.info("准备处理录音数据: {}", inboundMessage);
        try {
            packetsLock.writeLock().lock();
            if (packets.size() > MAX_BUFFERED_PACKETS) {
                log.warn("录音数据缓存已经超过最大限制，丢弃");
            } else {
                packets.add(inboundMessage.getPacket());
            }
        } finally {
            packetsLock.writeLock().unlock();
        }
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    synchronized public void startRecording() throws Exception {
        if (isRecording.compareAndSet(false, true)) {
            log.info("声纹设备({})开始录音", getDeviceId());
            try {
                packetsLock.writeLock().lock();
                packets.clear();
            } finally {
                packetsLock.writeLock().unlock();
            }
            sendCommand(ActionType.START.getCode());
        } else {
            log.warn("当前已经处于录音中");
        }
    }

    private void sendCommand(String code) {
        AuidoOprInfo info = new AuidoOprInfo();
        info.setGlobalClientid(deviceId);
        info.setActionPower(code);
        sendMqttMsg(info);
    }

    private void sendMqttMsg(Object obj) {
        String topic = "CONTROL/" + deviceId;
        log.info("发送声纹设备控制指令：topic({}), 消息实体({})", topic, obj);
        mqttUtilsServer.pushMsg(topic, obj, 1);
    }

    @Override
    public boolean isRecording() {
        return isRecording.get();
    }

    @Override
    synchronized public void stopRecording() throws Exception {
        log.info("声纹设备({})停止录音", getDeviceId());
        isRecording.set(false);
        sendCommand(ActionType.STOP.getCode());
    }

    @Override
    synchronized public void stopRecordingAndSave(String audioFilepath) throws Exception {
        stopRecording();
        List<Packet> audioPackets = null;
        try {
            packetsLock.writeLock().lock();
            if (!packets.isEmpty()) {
                audioPackets = new ArrayList<>(packets);
                packets.clear();
            }
        } finally {
            packetsLock.writeLock().unlock();
        }

        if (audioPackets != null) {
            genAudioFile(audioFilepath, audioPackets);
        }
    }

    private void genAudioFile(String audioFilepath, List<Packet> packets) {
        // 生成文件头
        int totalAudioLen = getTotalAudioLen(packets);
        byte[] headerByte = getHeaderByte(totalAudioLen, packets);
        byte[] contentByte = getContentByte(totalAudioLen, packets);
        log.info("写WAV文件({})", audioFilepath);
        AudioFileUtils.writeWavAudioFile(audioFilepath, headerByte, contentByte);
    }

    private byte[] getContentByte(int totalAudioLen, List<Packet> packets) {
        byte[] contentByte = new byte[totalAudioLen];
        int startLen = 0;
        for (Packet packet : packets) {
            List<byte[]> lb = packet.getDataGroup().getAudioDatas();
            for (byte[] b : lb) {
                System.arraycopy(b, 0, contentByte, startLen, b.length);
                startLen += b.length;
            }
        }
        return contentByte;
    }

    byte[] getHeaderByte(long totalAudioLen, List<Packet> packets) {
        long sampleRate = packets.get(0).getDataGroup().getSampleRate();
        int channels = packets.get(0).getDataGroup().getChannels();
        long audioFormat = packets.get(0).getDataGroup().getBits();
        return AudioFileUtils.getWaveFileHeader(totalAudioLen, sampleRate, channels, audioFormat);
    }

    private int getTotalAudioLen(List<Packet> packets) {
        int totalLen = 0;
        for (Packet packet : packets) {
            List<byte[]> list = packet.getDataGroup().getAudioDatas();
            for (byte[] b : list) {
                totalLen += b.length;
            }
        }
        return totalLen;
    }
}
