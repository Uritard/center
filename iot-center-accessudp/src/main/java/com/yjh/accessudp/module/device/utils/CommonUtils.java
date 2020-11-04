package com.yjh.accessudp.module.device.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author
 * @ClassName:
 * @Description: 封装公共方法
 * @date 2018/5/17 14:43
 */
@Component
public class CommonUtils {
    /**
     * @Fields logger : 日志
     */
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private static String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取格式化时间
     *
     * @return 格式化后的时间
     */
    public static String getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        String dateNowStr = sdf.format(date);
        return dateNowStr;
    }

    public   static String formatDate(Date date){
        String formatDate = new SimpleDateFormat(DATE_PATTERN).format(date);
        return formatDate;
    }

    public static Boolean isWindows(){
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
           return true;
        }else{
            return false;
        }
    }

    public static  String getProFilePath (){
        String profile=null;
        try {
            Properties props = new Properties();
            String filePath = System.getProperty("user.dir") + File.separator + "config" + File.separator + "application.properties";
            props.load(new FileInputStream(filePath));
            profile=props.getProperty("spring.profiles.active");
        }catch (Exception e){
            logger.error("获取配置文件路径失败");
        }
        return profile==null||"".equals(profile)?"pubc":profile;
    }

}
