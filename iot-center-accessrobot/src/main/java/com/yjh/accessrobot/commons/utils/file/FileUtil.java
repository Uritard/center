package com.yjh.accessrobot.commons.utils.file;

import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * 文件路径生成和获取
 */
@Slf4j
public class FileUtil {

    private final static Log logger = LogFactory.getLog(FileUtil.class);

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

    public static void deleteDirectory(String path) {
        File dir = new File(path);

        if(!dir.isDirectory()) {
            System.out.println("Not a directory. Do nothing");
            return;
        }
        File[] listFiles = dir.listFiles();
        for(File file : listFiles){
            System.out.println("Deleting "+file.getName());
            file.delete();
        }
        System.out.println("Deleting Directory. Success = "+dir.delete());
    }

    /**
     * 获取文件的绝对保存路径
     *
     * @return
     */
    public static String getAbsolutePath(String fileSaveConfigPath) {
        //操作系统名称
        String osName = System.getProperties().getProperty("os.name");
        logger.info("osName:" + osName);

        String fileDirPath = fileSaveConfigPath + File.separator;
        logger.info("fileDirPath:" + fileDirPath);

        File file = new File(fileDirPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return fileDirPath;
    }

    public static String getRelativePath(String serverName, String relative) {
        String monthStr = DateTimeUtil.getDateString();
        String relativePath = serverName + File.separator +
                monthStr + File.separator;
        if (StringUtils.isNotEmpty(relative)) {
            relativePath += relative;
            relativePath += File.separator;
        }
        return relativePath;
    }

    /**
     * 根据不同浏览器返回不同的数据
     *
     * @param fileName
     * @param userAgent
     * @return
     */
    public static String encodeByBrowser(String fileName, String userAgent) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        try {
            if (StringUtils.isBlank(userAgent)) {
                fileName = URLEncoder.encode(fileName, "utf-8");
            } else if (userAgent.toLowerCase().indexOf("firefox") > -1) {
                //火狐浏览器编码（ie11 采用这种没有效果）
                fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            } else if (userAgent.toLowerCase().indexOf("safari") > -1) {
                fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
            } else {
                //其他浏览器编码（火狐采用这种没有效果）
                fileName = URLEncoder.encode(fileName, "utf-8");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }


    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static void copyFileUsingStream(String sourcePath, String descPath) throws IOException {
        File source = new File(sourcePath);
        File dest = new File(descPath);
        //目标文件夹不存在会报错
        createDirectory(StringUtils.substringBeforeLast(descPath,"/"));
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
    public static boolean checkFileName(String fileName,String type){
        String name = StringUtils.substringAfterLast(fileName,".");
        if (name.equals(type)){
            return true;
        }
        return false;
    }

    /**
     * 检查文件是否是zip格式
     *
     * @param file 页面上传的文件
     * @return 判断结果
     */
    public static boolean isZipFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) return false;
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[4];
            if (inputStream.read(header) != 4) return false;
            //zip文件头: 0x50 0x4B 0x03 0x04
            return (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件是否是tar格式
     *
     * @param file 页面上传的文件
     * @return 判断结果
     */
    public static boolean isTarFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) return false;
        try (InputStream inputStream = file.getInputStream()) {
            //tar魔数位于257字节处,共5字节: 0x75 0x73 0x74 0x61 0x72
            byte[] magicNumber = new byte[5];
            long skipped = inputStream.skip(257);
            if (skipped != 257) return false;
            if (inputStream.read(magicNumber) != 5) return false;
            return (magicNumber[0] == 0x75 &&
                    magicNumber[1] == 0x73 &&
                    magicNumber[2] == 0x74 &&
                    magicNumber[3] == 0x61 &&
                    magicNumber[4] == 0x72);
        } catch (IOException e) {
            return false;
        }
    }
}
