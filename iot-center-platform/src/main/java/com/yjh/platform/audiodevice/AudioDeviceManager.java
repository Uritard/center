package com.yjh.platform.audiodevice;

/**
 * 录音设备管理器
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public interface AudioDeviceManager {

    /**
     * 根据id获取设备实例
     *
     * @param deviceId 设备ID
     * @return 设备实例
     * @throws Exception 不存在设备时抛出异常
     */
    AudioDevice getAudioDevice(String deviceId);

    /**
     * 注册声纹设备
     *
     * @param audioDevice 声纹设备实例
     */
    void registerAudioDevice(AudioDevice audioDevice);

}
