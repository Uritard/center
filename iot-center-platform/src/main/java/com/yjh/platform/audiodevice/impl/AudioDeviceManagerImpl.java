package com.yjh.platform.audiodevice.impl;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
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

    ConcurrentMap<String, AudioDevice> deviceMap = new ConcurrentHashMap<>();

    @Override
    public Optional<AudioDevice> getAudioDevice(String deviceId) {
        return Optional.of(deviceMap.get(deviceId));
    }

    @Override
    public void registerAudioDevice(String deviceId, AudioDevice audioDevice) {
        log.info("Register audio device({}) with device id: {}", audioDevice, deviceId);
        deviceMap.put(deviceId, audioDevice);
    }
}
