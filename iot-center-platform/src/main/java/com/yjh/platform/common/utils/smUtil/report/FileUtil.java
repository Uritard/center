package com.yjh.platform.common.utils.smUtil.report;

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

    /**
     * 删除文件
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.delete()) {
            log.info("Delete the file + {} success", file.getName());
        } else {
            log.warn("Delete the file + {} failed", file.getName());
        }
    }
}
