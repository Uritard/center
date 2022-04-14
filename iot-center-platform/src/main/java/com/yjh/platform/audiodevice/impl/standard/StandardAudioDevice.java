package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.impl.standard.tcp.InboundMessage;
import com.yjh.platform.audiodevice.impl.standard.tcp.Packet;
import lombok.extern.slf4j.Slf4j;

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
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final List<Packet> packets = new LinkedList<>();
    private final ReadWriteLock packetsLock = new ReentrantReadWriteLock();
    /**
     * 限制一下最大的录音数据，一帧数据差不多是4096个字节，限制单个录音文件不超过100MB
     */
    private final static int MAX_BUFFERED_PACKETS = (100 * 1024 * 1024) / 4096;

    public void onAudioData(InboundMessage inboundMessage) {
        //TODO: xiaoming
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
    synchronized public void startRecording() throws Exception {
        if (isRecording.compareAndSet(false, true)) {
            try {
                packetsLock.writeLock().lock();
                packets.clear();
            } finally {
                packetsLock.writeLock().unlock();
            }
            //TODO: 发开始录音指令
        } else {
            log.warn("当前已经处于录音中");
        }
    }

    @Override
    public boolean isRecording() {
        return isRecording.get();
    }

    @Override
    synchronized public void stopRecording() throws Exception {
        isRecording.set(false);
        //TODO: 发停止录音指令
    }

    @Override
    synchronized public void stopRecordingAndSave(String audioFilepath) throws Exception {
        stopRecording();

        try {
            packetsLock.readLock().lock();
            //TODO: xiaoming 写wav文件
            packets.clear();
        } finally {
            packetsLock.readLock().unlock();
        }
    }
}
