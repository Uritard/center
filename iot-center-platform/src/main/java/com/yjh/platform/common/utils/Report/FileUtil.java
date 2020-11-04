package com.yjh.platform.common.utils.Report;

import java.io.File;

/**
 * @author YC
 * @date 2020/11/4 - 11:10
 */
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
}
