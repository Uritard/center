//package com.yjh.accesstcp.common.utils;
//
//import net.sf.jsqlparser.parser.ParseException;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang.math.NumberUtils;
//import org.quartz.CronExpression;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
///**
// * @author lqh
// * @since 2021/1/15
// */
//public class CornUtil {
//
//    /**
//     * 解析corn表达式，生成指定日期的时间序列
//     *
//     * @param cronExpression cron表达式
//     * @param cronDate cron解析日期
//     * @param result crom解析时间序列
//     * @return 解析成功失败
//     */
//    public static boolean parser(String cronExpression, String cronDate, List<String> result)
//    {
//        if (cronExpression == null || cronExpression.length() < 1 || cronDate == null || cronDate.length() < 1)
//        {
//            return false;
//        }
//        else
//        {
//            CronExpression exp = null;
//            // 初始化cron表达式解析器
//            try
//            {
//                exp = new CronExpression(cronExpression);
//            }
//            catch (java.text.ParseException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                return false;
//            }
//
//            // 定义生成时间范围
//            // 定义开始时间，前一天的23点59分59秒
//            Calendar c = Calendar.getInstance();
//            String sStart = cronDate + " 00:00:00";
//            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date dStart = null;
//            try
//            {
//                dStart = sdf.parse(sStart);
//            }
//            catch (java.text.ParseException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.setTime(dStart);
//            c.add(Calendar.SECOND, -1);
//            dStart = c.getTime();
//
//            // 定义结束时间，当天的23点59分59秒
//            c.add(Calendar.DATE, 1);
//            Date dEnd = c.getTime();
//
//            // 生成时间序列
//            java.util.Date dd = dStart;
//            dd = exp.getNextValidTimeAfter(dd);
//            while ((dd.getTime() >= dStart.getTime()) && (dd.getTime() <= dEnd.getTime()))
//            {
//                result.add(sdf.format(dd));
//                dd = exp.getNextValidTimeAfter(dd);
//            }
//            exp = null;
//        }
//        return true;
//    }
//
//    public static String translateToChinese(String cronExp,int type) throws Exception
//    {
//        if (cronExp == null || cronExp.length() < 1)
//        {
//            return "55";
//        }
//        CronExpression exp = null;
//        // 初始化cron表达式解析器
//        try
//        {
//            exp = new CronExpression(cronExp);
//        }
//        catch (java.text.ParseException e)
//        {
//            return "66";
//        }
//        String[] tmpCorns = cronExp.split(" ");
//
//        if(type == 1){
//            //解析月
//            if(!tmpCorns[4].equals("*")){
//                return tmpCorns[4];
//            }
//            else
//            {
//                return "1,2,3,4,5,6,7,8,9,10,11,12";
//            }
//        }
//        if(type == 2){
//            //解析周
//            String str ="";
//            if(!tmpCorns[5].equals("*") && !tmpCorns[5].equals("?"))
//            {
//                char[] tmpArray =  tmpCorns[5].toCharArray();
//                for(char tmp:tmpArray)
//                {
//                    str = String.valueOf(tmp);
//                    break;
//                }
//            }
//            return str;
//        }
//
//
//
////            //解析日
////            if(!tmpCorns[3].equals("?"))
////            {
////                if(!tmpCorns[3].equals("*"))
////                {
////                    sBuffer.append(tmpCorns[3]).append("日");
////                }
////                else
////                {
////                    sBuffer.append("每日");
////                }
////            }
////
//            //解析时
//
//        if(type == 4){
//            StringBuffer sBuffer = new StringBuffer();
//            if(!tmpCorns[2].equals("*"))
//            {
//                sBuffer.append(tmpCorns[2]).append(":00:00");
//                return sBuffer.toString();
//            }
//            else
//            {
//                return "00:00:00";
//            }
//
////            //解析分
////            if(!tmpCorns[1].equals("*"))
////            {
////                sBuffer.append(tmpCorns[1]).append("分");
////            }
////            else
////            {
////                sBuffer.append("每分");
////            }
////
////            //解析秒
////            if(!tmpCorns[0].equals("*"))
////            {
////                sBuffer.append(tmpCorns[0]).append("秒");
////            }
////            else
////            {
////                sBuffer.append("每秒");
////            }
//        }
//
//
//
//        return null;
//
//    }
//
//
//}
//
//
//
//
