package com.yjh.platform.audiodevice.impl;

import com.yjh.platform.audiodevice.AudioDevice;

/**
 * 声纹设备创建工厂
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public interface AudioDeviceFactory {

    /**
     * 创建新的声纹设备
     * @return
     */
    AudioDevice newAudioDevice();
}
