package com.yjh.platform.audiodevice.impl.fake;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;

import java.util.Collections;
import java.util.List;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public class FakeAudioDeviceManager implements AudioDeviceManager {

    @Override
    public AudioDevice getAudioDevice(String deviceId) {
        return new FakeAudioDevice(deviceId);
    }

    @Override
    public void registerAudioDevice(AudioDevice audioDevice) {

    }

    /**
     * 已经注册的设备列表
     *
     * @return 声纹设备 code 列表
     */
    @Override
    public List<String> registedDevices() {
        return Collections.emptyList();
    }

}
