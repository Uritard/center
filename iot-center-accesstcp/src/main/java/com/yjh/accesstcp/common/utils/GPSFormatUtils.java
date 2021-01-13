package com.yjh.accesstcp.common.utils;

public class GPSFormatUtils {

    /**
     * 功能：         度-->度分秒
     * @param d 传入待转化格式的经度或者纬度
     */
    public static String DDtoDMS(Double d){

        String[] array=d.toString().split("[.]");
        String degrees=array[0];//得到度

        Double m=Double.parseDouble("0."+array[1])*60;
        String[] array1=m.toString().split("[.]");
        String minutes=array1[0];//得到分

        Double s=Double.parseDouble("0."+array1[1])*60;
        String[] array2=s.toString().split("[.]");
        String seconds=array2[0];//得到秒
        System.out.println(degrees+"  "+minutes+"  "+seconds);
        return degrees+"°"+minutes+"'"+seconds+'"';
    }

    /**
     * 功能：  度-->度分秒（满足图片格式）
     * @param d   传入待转化格式的经度或者纬度
     * @return
     */
    public static String DDtoDMS_photo(Double d){

        String[] array=d.toString().split("[.]");
        String D=array[0];//得到度

        Double m=Double.parseDouble("0."+array[1])*60;
        String[] array1=m.toString().split("[.]");
        String M=array1[0];//得到分

        Double s=Double.parseDouble("0."+array1[1])*60*10000;
        String[] array2=s.toString().split("[.]");
        String S=array2[0];//得到秒
        return  D+"/1,"+M+"/1,"+S+"/10000";
    }
}