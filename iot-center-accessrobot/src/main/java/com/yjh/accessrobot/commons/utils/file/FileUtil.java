package com.yjh.accessrobot.commons.utils.file;

import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URLEncoder;

/**
 * 文件路径生成和获取
 */
public class FileUtil {

    private final static Log logger = LogFactory.getLog(FileUtil.class);

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
        } finally {
            is.close();
            os.close();
        }
    }
    public static boolean checkFileName(String fileName,String type){
        String name = StringUtils.substringAfterLast(fileName,".");
        if (name.equals(type)){
            return true;
        }
        return false;
    }

}
