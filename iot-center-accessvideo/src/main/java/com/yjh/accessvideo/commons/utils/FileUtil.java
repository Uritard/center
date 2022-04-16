package com.yjh.accessvideo.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;


/**
 * @author hyh
 * @since 2022/4/16
 **/
@Slf4j
public class FileUtil {


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
     * @param path
     */
    public static void deleteFile(String path){
        File file = new File(path);
        if(file.delete()){
            log.info("Delete the file + {} success", file.getName());
        }else {
            log.warn("Delete the file + {} failed", file.getName());
        }
    }
}
