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
    private boolean isRecording;

    @Override
    public void startRecording(String deviceId) throws Exception {
        isRecording = true;
    }

    @Override
    public boolean isRecording(String deviceId) {
        return isRecording;
    }

    @Override
    public void stopRecording(String deviceId) throws Exception {
        isRecording = false;
    }

    @Override
    public void stopRecordingAndSave(String audioFilepath,String deviceId) throws Exception {
        stopRecording(deviceId);

        String dir = FilenameUtils.getFullPath(audioFilepath);
        Path dirPath = Paths.get(dir);
        Files.createDirectories(dirPath);
        Files.write(Paths.get(audioFilepath), "TESTING WAV FILE".getBytes(StandardCharsets.UTF_8));
    }
}
