package com.yjh.logs.module.log.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author
 * @ClassName:
 * @Description: 封装公共方法
 * @date 2018/5/17 14:43
 */
@Component
public class CommonUtil {
    /**
     * @Fields logger : 日志
     */
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

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


    public static Boolean isWindows(){
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
           return true;
        }else{
            return false;
        }
    }

    public static boolean isNumeric(String str){
        return NUMBER_PATTERN.matcher(str).matches();
    }
}
