package com.yjh.platform.audiodevice.impl.standard;

import cn.hutool.core.util.HexUtil;
import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.impl.AudioFileUtils;
import com.yjh.platform.audiodevice.impl.standard.tcp.InboundMessage;
import com.yjh.platform.audiodevice.impl.standard.tcp.Packet;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.MqttUtilsServer;
import com.yjh.platform.module.device.entity.AuidoOprInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
            if (Constant.isPacketLog()) {
                log.warn("非录音时段收到录音数据: {}", inboundMessage);
            }
            return;
        }
        if (Constant.isPacketLog()) {
            log.info("准备处理录音数据: {}", inboundMessage);
        }
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

    public void genAudioFile(String audioFilepath, List<Packet> packets) {
        // 生成文件头
        long[] totalAudioLen = getTotalAudioLen(packets);
        byte[][] headersByte = getHeaderByte(totalAudioLen, packets);
//        byte[] contentByte = getContentByte(totalAudioLen, packets);
        log.info("写WAV文件({}), heard: {}", audioFilepath, HexUtil.encodeHexStr(headersByte[0]));
//        AudioFileUtils.writeWavAudioFile(audioFilepath, headerByte, contentByte);
        writeWAVFile(audioFilepath, packets, headersByte);
    }

    private void writeWAVFile(String audioFilepath, List<Packet> packets, byte[][] headersByte) {
        FileOutputStream[] outStreams = new FileOutputStream[headersByte.length];
        // 创建多个通道文件
        int idx = audioFilepath.lastIndexOf(".");
        String fileName = audioFilepath.substring(0, idx);
        String fileExt = audioFilepath.substring(idx);
        for (int i = 0; i < headersByte.length; i++) {
            String filePath = fileName + (i == 0 ? "" : "_" + (i + 1)) + fileExt;
            File file = new File(filePath);
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                outStreams[i] = new FileOutputStream(file);
                outStreams[i].write(headersByte[i]);
            } catch (Exception e) {
                throw new RuntimeException("创建文件失败", e);
            }
        }

        // 将声音数据写入文件
        ByteBuffer[] byteBuffers = null;
        ByteBuffer channelBuffer = null;
        boolean reverse = Constant.voiceByteReverse();
        int chtype = Constant.voiceChtype();
        for (Packet packet : packets) {
            try {
                List<byte[]> audioDatas = packet.getDataGroup().getAudioDatas();
                if (chtype == 0) {
                    // 默认多通道 0.1 0.2 0.1 0.2 0.1 0.2
                    int dataLength = audioDatas.get(0).length;
                    int block = packet.getDataGroup().getBits() / 8;
                    if (channelBuffer == null) {
                        channelBuffer = ByteBuffer.allocate(MAX_BUFFERED_PACKETS * 1024);
                    }
                    for (int offset = 0; offset < dataLength; offset += 2) {
                        for (byte[] audioData : audioDatas) {
                            // outStreams[0].write(audioData, offset, 2);
                            if (reverse) {
                                channelBuffer.put(audioData, offset + 1, 1);
                                channelBuffer.put(audioData, offset, 1);
                            } else {
                                channelBuffer.put(audioData, offset, 2);
                            }
                        }
                    }
                } else if (chtype == 1) {
                    // 单通道，只获取一个通道数据，写入多个文件 111
                    int j = 0;
                    for (byte[] dates : audioDatas) {
                        if (reverse) {
                            for (int offset = 0; offset < dates.length; offset += 2) {
                                outStreams[j].write(dates, offset + 1, 1);
                                outStreams[j].write(dates, offset, 1);
                            }
                            j++;
                        } else {
                            outStreams[j++].write(dates);
                        }
                    }
                } else if (chtype == -1) {
                    // 单通道或多通道，多个通道数据 121212 写入
                    for (byte[] dates : audioDatas) {
                        if (reverse) {
                            for (int offset = 0; offset < dates.length; offset += 2) {
                                outStreams[0].write(dates, offset + 1, 1);
                                outStreams[0].write(dates, offset, 1);
                            }
                        } else {
                            outStreams[0].write(dates);
                        }
                    }
                } else if(chtype == -2){
                    // 单通道，通道1写完写通道2， 111222 写入
                    if (byteBuffers == null) {
                        byteBuffers = new ByteBuffer[audioDatas.size()];
                    }
                    int j = 0;
                    for (byte[] dates : audioDatas) {
                        if (byteBuffers[j] == null) {
                            byteBuffers[j] = ByteBuffer.allocate(MAX_BUFFERED_PACKETS * 1024);
                        }
                        if (reverse) {
                            for (int offset = 0; offset < dates.length; offset += 2) {
                                byteBuffers[j].put(dates, offset + 1, 1);
                                byteBuffers[j].put(dates, offset, 1);
                            }
                            j++;
                        } else {
                            byteBuffers[j++].put(dates);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("写WAV文件失败", e);
            }
        }
        if (channelBuffer != null) {
            try {
                outStreams[0].write(channelBuffer.array(), 0, channelBuffer.position());
                channelBuffer.clear();
            } catch (IOException e) {
                throw new RuntimeException("写WAV文件失败", e);
            }
        }
        if (byteBuffers != null) {
            for (ByteBuffer buffer : byteBuffers) {
                try {
                    outStreams[0].write(buffer.array(), 0, buffer.position());
                    buffer.clear();
                } catch (IOException e) {
                    throw new RuntimeException("写WAV文件失败", e);
                }
            }
            Arrays.fill(byteBuffers, null);
        }

        // 关闭文件流
        for (FileOutputStream outStream : outStreams) {
            IOUtils.closeQuietly(outStream);
        }
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

    byte[][] getHeaderByte(long[] totalAudioLens, List<Packet> packets) {
        long sampleRate = packets.get(0).getDataGroup().getSampleRate();
        int channels = packets.get(0).getDataGroup().getChannels();
        long audioFormat = packets.get(0).getDataGroup().getBits();
        byte[][] headersByte = new byte[totalAudioLens.length][];
        for (int i = 0; i < totalAudioLens.length; i++) {
            headersByte[i] = AudioFileUtils.getWaveFileHeader(totalAudioLens[i], sampleRate, channels, audioFormat);
        }
        return headersByte;
    }

    byte[] getHeaderByte(long totalAudioLen, List<Packet> packets) {
        long sampleRate = packets.get(0).getDataGroup().getSampleRate();
        int channels = packets.get(0).getDataGroup().getChannels();
        long audioFormat = packets.get(0).getDataGroup().getBits();
        return AudioFileUtils.getWaveFileHeader(totalAudioLen, sampleRate, channels, audioFormat);
    }

    private long[] getTotalAudioLen(List<Packet> packets) {
        long[] totalLens = null;
        for (Packet packet : packets) {
            List<byte[]> list = packet.getDataGroup().getAudioDatas();
            if (totalLens == null) {
                totalLens = new long[Constant.voiceChannelOne() ? list.size() : 1];
            }
            if (Constant.voiceChannelOne()) {
                int i = 0;
                for (byte[] b : list) {
                    totalLens[i++] += b.length;
                }
            } else {
                for (byte[] b : list) {
                    totalLens[0] += b.length;
                }
            }
        }
        return totalLens;
    }
}
