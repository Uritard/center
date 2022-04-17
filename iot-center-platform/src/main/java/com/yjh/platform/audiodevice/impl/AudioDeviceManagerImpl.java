package com.yjh.platform.audiodevice.impl;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 录音设备管理器实现
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class AudioDeviceManagerImpl implements AudioDeviceManager {

    private final ConcurrentMap<String, AudioDevice> deviceMap = new ConcurrentHashMap<>();

    @Override
    public AudioDevice getAudioDevice(String deviceId) {
        return deviceMap.get(deviceId);
    }

    @Override
    public void registerAudioDevice(AudioDevice audioDevice) {
        log.info("Register audio device({}) with device id: {}", audioDevice, audioDevice.getDeviceId());
        deviceMap.put(audioDevice.getDeviceId(), audioDevice);
    }
}
