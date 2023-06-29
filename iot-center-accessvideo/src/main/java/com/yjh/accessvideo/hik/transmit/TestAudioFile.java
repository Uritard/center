package com.yjh.accessvideo.hik.transmit;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * @author 丫C
 * @date 2023/6/28
 */
@Slf4j
public class TestAudioFile {

    private static final String USE_DIR = System.getProperty("user.dir");

    private TestAudioFile() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 测试接收的音频文件
     *
     * @param deviceId 设备id
     */
    public static void testRecAudioFile(Long deviceId, int audioEncType) {
        String format = DateTimeUtil.formatThreadLocal(new Date());
        if (VoiceTransConstant.AudioEncType.G711_A.getCode() == audioEncType) {
            // 保存回调函数的G711解码后的音频数据
            File filePcm = new File(USE_DIR + "/AudioFile/ReceiveData/decodeData-" + format + ".pcm");
            if (!filePcm.exists()) {
                try {
                    if (!filePcm.getParentFile().exists()) {
                        filePcm.getParentFile().mkdirs();
                    }
                    filePcm.createNewFile();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            try {
                Constant.outputStreamPcm = new FileOutputStream(filePcm, true);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        // 保存回调函数中的原始音频数据
        String filePathName = USE_DIR + "/AudioFile/ReceiveData/originAudio-" + format + ".g7";
        if (VoiceTransConstant.AudioEncType.AAC.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            filePathName = filePathName.replace("g7", "aac");
        } else if (VoiceTransConstant.AudioEncType.PCM.getCode() == Constant.hikDeviceEncodeFormatMaps.get(deviceId)) {
            filePathName = filePathName.replace("g7", "pcm");
        }
        File file = new File(filePathName);

        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        try {
            Constant.outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 测试发送的音频文件
     *
     * @param filePathNameTemp 文件路径
     * @param fileOutputStream 文件流
     */
    public static FileOutputStream testSendAudioFile(String filePathNameTemp, FileOutputStream fileOutputStream) {
        String format = DateTimeUtil.formatThreadLocal(new Date());
        // G711编码音频文件
        File fileEncode = new File(filePathNameTemp + "encodeData-" + format + ".g7");
        if (!fileEncode.exists()) {
            try {
                if (!fileEncode.getParentFile().exists()) {
                    fileEncode.getParentFile().mkdirs();
                }
                fileEncode.createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        try {
            fileOutputStream = new FileOutputStream(fileEncode);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return fileOutputStream;
    }
}
