package com.yjh.platform.audiodevice.impl.fake;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;

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
        return new FakeAudioDevice();
    }

    @Override
    public void registerAudioDevice(String deviceId, AudioDevice audioDevice) {

    }

}
