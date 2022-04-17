package com.yjh.platform.audiodevice.impl.fake;

import com.yjh.platform.audiodevice.AudioDevice;
import org.apache.commons.io.FilenameUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/14
 * @since [产品/模块版本] （可选）
 */
public class FakeAudioDevice implements AudioDevice {
    private String deviceId;
    private boolean isRecording;

    public FakeAudioDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void startRecording() throws Exception {
        isRecording = true;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void stopRecording() throws Exception {
        isRecording = false;
    }

    @Override
    public void stopRecordingAndSave(String audioFilepath) throws Exception {
        stopRecording();

        String dir = FilenameUtils.getFullPath(audioFilepath);
        Path dirPath = Paths.get(dir);
        Files.createDirectories(dirPath);
        Files.write(Paths.get(audioFilepath), "TESTING WAV FILE".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }
}
