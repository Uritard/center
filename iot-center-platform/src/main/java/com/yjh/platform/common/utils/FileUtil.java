package com.yjh.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;

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
}
