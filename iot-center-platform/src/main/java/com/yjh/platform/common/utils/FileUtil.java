package com.yjh.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2022/05/25
 */
@Slf4j
public class FileUtil {

    private static List<String> nameList = new ArrayList<>();

    static{
        nameList.add("ZIP");
        nameList.add("XLS");
        nameList.add("XLSX");
        nameList.add("XLSB");
        nameList.add("XLSM");
        nameList.add("XLST");
        nameList.add("XML");
    }

    public static boolean checkFileName(String fileName){
        String name = StringUtils.substringAfterLast(fileName,".").toUpperCase();
        if (nameList.contains(name)){
            return true;
        }
        log.error("上传文件不在规定文件范围内: {} - {}", fileName, name);
        return false;
    }

    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static boolean copyFileUsingStream(String sourcePath, String descPath) {
        File source = new File(sourcePath);
        File dest = new File(descPath);

        try {
            FileUtils.copyFile(source, dest);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }
}
