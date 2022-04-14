package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.platform.audiodevice.AudioDevice;

/**
 * 通过国网标准接口接入的声纹设备
 *
 * @author zilong
 * @date 2022/4/14
 */
public class StandardAudioDevice implements AudioDevice {

    @Override
    public void startRecording() throws Exception {

    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void stopRecording() throws Exception {

    }

    @Override
    public void stopRecordingAndSave(String audioFilepath) throws Exception {

    }
}
