package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.impl.standard.tcp.InboundMessage;
import com.yjh.platform.audiodevice.impl.standard.tcp.Packet;
import com.yjh.platform.common.mqtt.MqttUtilsServer;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.device.entity.AuidoOprInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
    private String deviceId;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final List<Packet> packets = new LinkedList<>();
    private final ReadWriteLock packetsLock = new ReentrantReadWriteLock();
    @Autowired
    private MqttUtilsServer mqttUtilsServer;
    /**
     * 限制一下最大的录音数据，一帧数据差不多是4096个字节，限制单个录音文件不超过100MB
     */
    private final static int MAX_BUFFERED_PACKETS = (100 * 1024 * 1024) / 4096;

    public StandardAudioDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    public void onAudioData(InboundMessage inboundMessage) {
        if (!isRecording.get()) {
            log.warn("非录音时段收到录音数据: {}", inboundMessage);
            return;
        }

        log.info("准备处理录音数据: {}", inboundMessage);
        if (packets.size() > MAX_BUFFERED_PACKETS) {
            log.warn("录音数据缓存已经超过最大限制，丢弃");
            return;
        }
        try {
            packetsLock.writeLock().lock();
            packets.add(inboundMessage.getPacket());
        } finally {
            packetsLock.writeLock().unlock();
        }
    }

    @Override
    public String getDeviceID() {
        return deviceId;
    }

    @Override
    synchronized public void startRecording() throws Exception {
        if (isRecording.compareAndSet(false, true)) {
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
        sendMTQQ(info);
    }

    private void sendMTQQ(Object obj) {
        log.info("发送语音控制指令：{}", obj.toString());
        mqttUtilsServer.pushMsg("CONTROL/" + deviceId, obj, 1);
    }

    @Override
    public boolean isRecording() {
        return isRecording.get();
    }

    @Override
    synchronized public void stopRecording() throws Exception {
        isRecording.set(false);
        sendCommand(ActionType.STOP.getCode());
    }

    @Override
    synchronized public void stopRecordingAndSave(String audioFilepath) throws Exception {
        stopRecording();
        try {
            packetsLock.readLock().lock();
            if (packets.size() > 0) {
                genAudioFile(audioFilepath, packets);
            }
            packets.clear();
        } finally {
            packetsLock.readLock().unlock();
        }
    }

    private void genAudioFile(String audioFilepath, List<Packet> packets) {
        // 生成文件头
        int totalAudioLen = getTotalAudioLen(packets);
        byte[] headerByte = getHeaderByte(totalAudioLen,packets);
        byte[] contentByte = getContentByte(totalAudioLen,packets);
        writeWAVFile(audioFilepath, headerByte,contentByte);
    }

    private void writeWAVFile(String audioFilepath, byte[] headerByte, byte[] contentByte) {
        boolean flag = AudioFileUtils.writePCMAudioFile(audioFilepath,headerByte,contentByte);
        log.info("wav数据文件生成状态：{}",flag);
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

    byte[] getHeaderByte(long totalAudioLen, List<Packet> packets){
        long totalDateLen = totalAudioLen + 36;
        long sampleRate = packets.get(0).getDataGroup().getSampleRate();
        int channels = packets.get(0).getDataGroup().getChannels();
        long byteRate = packets.get(0).getDataGroup().getBits();
        return AudioFileUtils.getWaveFileHeader(totalAudioLen,totalDateLen,sampleRate,channels,byteRate);
    }

    private int getTotalAudioLen(List<Packet> packets) {
        int totolLen = 0;
        for (Packet packet : packets){
            List<byte[]> list = packet.getDataGroup().getAudioDatas();
            for (byte[] b : list){
                totolLen += b.length;
            }
        }
        return totolLen;
    }
}
