package com.yjh.accessvideo.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;


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

    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static void copyFileUsingIOUtils(String sourcePath, String descPath) {
        File source = new File(sourcePath);
        String[] split = descPath.split("/");
        String tempPath = descPath.replace(split[split.length-1],"");
        File temp = new File(tempPath);
        if (!temp.exists()){
            temp.setWritable(true, false);
            temp.mkdirs();
        }
        File dest = new File(descPath);
        try {
            IOUtils.copy(new FileInputStream(source), new FileWriter(dest));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static void copyFileUsingStream(String sourcePath, String descPath) {
        File source = new File(sourcePath);
        String[] split = descPath.split("/");
        String tempPath = descPath.replace(split[split.length-1],"");
        File temp = new File(tempPath);
        if (!temp.exists()){
            temp.setWritable(true, false);
            temp.mkdirs();
        }
        File dest = new File(descPath);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
