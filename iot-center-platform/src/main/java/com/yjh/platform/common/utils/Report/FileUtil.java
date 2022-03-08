package com.yjh.platform.common.utils.Report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author YC
 * @date 2020/11/4 - 11:10
 */
@Slf4j
public class FileUtil {
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty())
            return false;
        try {
            return new File(filePath).exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建文件夹
     *
     * @param directoryPath
     */
    public static void createDirectory(String directoryPath) {
        try {
            FileUtils.forceMkdir(new File(directoryPath));
        } catch (Exception e) {
            log.error("createDirectory: ", e);
        }
    }

}
