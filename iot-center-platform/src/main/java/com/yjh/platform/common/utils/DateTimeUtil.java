package com.yjh.platform.common.utils;

import com.mysql.jdbc.StringUtils;
import com.yjh.platform.module.task.entity.TCruiseTaskCron;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 日期时间处理公共类.
 *
 * @author tt
 */
public class DateTimeUtil {

    // 日志模块
    private static Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    private static final String MONTHFORMATTPLCABLE = "yyyy-MM";
    private static final String MONTHFORMATTPL = "yyyyMM";
    private static final String DATEFORMATTPL = "yyyy-MM-dd";
    private static final String DATETIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    private static final String PUNCTUALITYFORMATTPL = "yyyy-MM-dd HH:00:00";
    private static final String TAILTIMEFORMATTPL = "yyyy-MM-dd HH:59:59";
    private static final String DATETIMEMSFORMATTPL = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String TIMESIMPLEFORMATTPL = "HH:mm";
    private static final String TIMEFORMATTPL = "HH:mm:ss";
    private static ResourceBundle resource = null;

    private DateTimeUtil() {

    }

    /**
     * 整点日期格式。
     *
     * @return 日期格式
     */
    public static String getPunctualityPattern() {
        return PUNCTUALITYFORMATTPL;
    }

    /**
     * 小时末尾日期格式。
     *
     * @return 日期格式
     */
    public static String getTailTimePattern() {
        return TAILTIMEFORMATTPL;
    }

    /**
     * 日期格式。
     *
     * @return 日期格式
     */
    public static String getDatePattern() {
        return DATEFORMATTPL;
    }

    /**
     * 时间格式。
     *
     * @return 时间格式
     */
    public static String getTimePattern() {
        return TIMEFORMATTPL;
    }

    /**
     * 简单时间格式。
     *
     * @return 简单时间格式
     */
    public static String getSimpleTimePattern() {
        return TIMESIMPLEFORMATTPL;
    }

    /**
     * 日期时间格式。
     *
     * @return 时间日期格式
     */
    public static String getDateTimePattern() {
        return DATETIMEFORMATTPL;
    }

    /**
     * 获取当前时间字符串
     *
     * @return
     */
    public static String getDateTimeString() {
        return getDateTimeString(false);
    }

    /**
     * 获取当前日期字符串
     *
     * @return
     */
    public static String getDateString() {
        return getDateString(new Date());
    }

    /**
     * 获取当前日期字符串
     *
     * @param date
     * @return
     */
    public static String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATTPL);
        return sdf.format(date);
    }

    /**
     * 获取当前时间字符串
     *
     * @return
     */
    public static String getTimeString() {
        return getTimeString(new Date());
    }

    /**
     * 获取当前时间字符串
     *
     * @param date
     * @return
     */
    public static String getTimeString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        return sdf.format(date);
    }

    /**
     * 获取当前时间字符串
     *
     * @return
     */
    public static String getSimpleTimeString() {
        return getSimpleTimeString(new Date());
    }

    /**
     * 获取当前时间字符串
     *
     * @param date
     * @return
     */
    public static String getSimpleTimeString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESIMPLEFORMATTPL);
        return sdf.format(date);
    }

    /**
     * 获取当前时间字符串
     *
     * @param withMillisecond
     * @return
     */
    public static String getDateTimeString(boolean withMillisecond) {
        return getDateTimeString(new Date(), withMillisecond);
    }

    /**
     * 获取当前时间字符串
     *
     * @param date
     * @return
     */
    public static String getDateTimeString(Date date) {
        return getDateTimeString(date, false);
    }

    /**
     * 获取当前时间字符串
     *
     * @param date
     * @param withMillisecond
     * @return
     */
    public static String getDateTimeString(Date date, boolean withMillisecond) {
        SimpleDateFormat sdf = null;
        if (withMillisecond) {
            sdf = new SimpleDateFormat(DATETIMEMSFORMATTPL);
        } else {
            sdf = new SimpleDateFormat(DATETIMEFORMATTPL);
        }
        return sdf.format(date);
    }

    /**
     * 日期转为字符串.
     *
     * @param date 要格式化的日期
     * @return 日期字符串
     */
    public static String format(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(getDateTimePattern());
        return sdf.format(date);
    }

    /**
     * 字符串转换为日期时间.
     *
     * @param source 日期字符串
     * @return 日期
     */
    public static Date parse(String source) {
        Date rtn = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(getDateTimePattern());
            rtn = sdf.parse(source);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return rtn;
    }

    /**
     * 获得WEB服务器UTC时间。
     *
     * @return 返回WEB服务器UTC时间
     */
    public static Date getWebServerUTCDate() {
        Calendar calendar = new GregorianCalendar();
        TimeZone zone = calendar.getTimeZone();
        return getUTCDate(calendar.getTime(), zone);
    }

    /**
     * 获得UTC时间.
     *
     * @param date 本地时间
     * @param zone 本地所在时区
     * @return UTC时间
     */
    public static Date getUTCDate(Date date, TimeZone zone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(zone);
        calendar.add(Calendar.MILLISECOND, -zone.getRawOffset());
        return calendar.getTime();
    }

    /**
     * 获得本地时间.
     *
     * @param date UTC时间
     * @param zone 本地所在时区
     * @return 本地时间
     */
    public static Date getLocalDate(Date date, TimeZone zone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(zone);
        calendar.add(Calendar.MILLISECOND, zone.getRawOffset());
        return calendar.getTime();
    }

    /**
     * 字符串转换为时间
     *
     * @param source
     * @return
     */
    public static Date getDate(String source) {
        return getDate(source, false);
    }

    /**
     * 获得当前时间的日期
     *
     * @return Date 不包括time，只有日期
     */
    public static Date getCurDate() {
        String curDateStr = getDateString();
        return parse(curDateStr + " 00:00:00");
    }

    /**
     * 获取昨天日期字符串
     *
     * @return
     */
    public static String getYesterdayDateStr() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1); //得到前一天
        Date date = calendar.getTime();
        return getDateString(date);
    }

    /**
     * 获取昨天的日期
     *
     * @return
     */
    public static Date getYesterdayDate() {
        String datestring = getYesterdayDateStr();
        return parse(datestring + " 00:00:00");
    }

    /**
     * 字符串转换为时间
     *
     * @param source
     * @param withMillisecond
     * @return
     */
    public static Date getDate(String source, boolean withMillisecond) {
        try {
            SimpleDateFormat sdf = null;
            if (withMillisecond) {
                sdf = new SimpleDateFormat(DATETIMEMSFORMATTPL);
            } else {
                sdf = new SimpleDateFormat(DATETIMEFORMATTPL);
            }
            return sdf.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 字符串转换为时间
     *
     * @param source
     * @return
     */
    public static Date getDate(String source, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param date 要格式话的日期 yyyy-MM-dd HH:mm:ss
     * @return 今天 HH:mm:ss or 昨天 HH:mm:ss or MM-dd HH:mm:ss
     * @throws ParseException
     */
    public static String convertDateFormat(String date) throws ParseException {
        SimpleDateFormat allformat = new SimpleDateFormat(DATETIMEFORMATTPL);
        SimpleDateFormat dateformat = new SimpleDateFormat(DATEFORMATTPL);
        SimpleDateFormat timeformat = new SimpleDateFormat(TIMEFORMATTPL);
        SimpleDateFormat datetimeformat = new SimpleDateFormat("MM-dd HH:mm:ss");

        Date myDate = allformat.parse(date);
        Calendar cal = Calendar.getInstance();
        String dateNow = dateformat.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String dateYesterday = dateformat.format(cal.getTime());

        if (dateformat.format(myDate).equals(dateNow)) {
            return resource.getString("today") + " " + timeformat.format(myDate);
        } else if (dateformat.format(myDate).equals(dateYesterday)) {
            return resource.getString("yesterday") + " " + timeformat.format(myDate);
        } else {
            return datetimeformat.format(myDate);
        }
    }

    /**
     * 两个日期相隔的天数
     *
     * @param c1 :起始日期
     * @param c2 :结束日期
     * @return c2-c1相隔的天数
     */
    public static double daysBetween(Calendar c1, Calendar c2) {
        // 将时分秒都置0
        Calendar startTime = Calendar.getInstance();
        startTime.clear();
        startTime.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH),
                c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND));

        Calendar endTime = Calendar.getInstance();
        endTime.clear();
        endTime.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH),
                c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), c2.get(Calendar.SECOND));

        long time1 = startTime.getTimeInMillis();
        long time2 = endTime.getTimeInMillis();
        return (time2 - time1) / (1000 * 3600 * 24 + 0d);
    }

    /**
     * 两个日期相隔的天数
     *
     * @param beginDate :起始日期
     * @param endDate   :结束日期
     * @return beginDate-endDate相隔的天数
     */
    public static int daysBetween(Date beginDate, Date endDate) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(clearTime(beginDate));

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(clearTime(endDate));

        long time1 = startTime.getTimeInMillis();
        long time2 = endTime.getTimeInMillis();
        long betweenDays = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(betweenDays));
    }

    /**
     * 两个日期相隔的月份
     *
     * @param beginDate :起始日期
     * @param endDate   :结束日期
     * @return beginDate-endDate相隔的月份
     */
    public static int monthsBetween(Date beginDate, Date endDate) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(beginDate);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(endDate);

        int year = (endTime.get(Calendar.YEAR) - startTime.get(Calendar.YEAR)) * 12;
        int month = endTime.get(Calendar.MONTH) - startTime.get(Calendar.MONTH);
        return Math.abs(year + month);
    }

    /**
     * 两个日期相隔的年份
     *
     * @param beginDate :起始日期
     * @param endDate   :结束日期
     * @return beginDate-endDate相隔的年份
     */
    public static int yearsBetween(Date beginDate, Date endDate) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(beginDate);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(endDate);

        int year = endTime.get(Calendar.YEAR) - startTime.get(Calendar.YEAR);
        return Math.abs(year);
    }

    /**
     * 两个日期相隔的小时数
     *
     * @param beginDate :起始日期
     * @param endDate   :结束日期
     * @return beginDate-endDate相隔的小时数
     */
    public static int hoursBetween(Date beginDate, Date endDate) {
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(beginDate);

        Calendar endTime = Calendar.getInstance();
        endTime.setTime(endDate);

        long time1 = startTime.getTimeInMillis();
        long time2 = endTime.getTimeInMillis();
        long betweenHours = (time2 - time1) / (1000 * 3600);
        return Integer.parseInt(String.valueOf(betweenHours));
    }

    /**
     * 清空日期的时分秒
     *
     * @param time
     * @return 返回时间为00:00:00的日期对象
     */
    public static Date clearTime(Date time) {
        Date date = null;
        if (null == time) {
            return date;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
            date = sdf.parse(sdf.format(time));
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return date;
    }

    /**
     * 两个日期相隔的秒数
     *
     * @param c1 起始日期
     * @param c2 结束日期
     * @return c2- c1相隔的秒数
     */
    public static long sencondsBetween(Calendar c1, Calendar c2) {
        Calendar startTime = Calendar.getInstance();
        startTime.clear();
        startTime.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH),
                c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND));

        Calendar endTime = Calendar.getInstance();
        endTime.clear();
        endTime.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH),
                c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), c2.get(Calendar.SECOND));

        long time1 = startTime.getTimeInMillis();
        long time2 = endTime.getTimeInMillis();
        return (time2 - time1) / 1000L;
    }

    /**
     * 复制一个calendar 避免调用add的时候原始对象产生副作用
     *
     * @return
     */
    public static Calendar cloneCalendar(Calendar cal) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
        return c;
    }

    /**
     * 获取当前系统时间，带时分秒
     *
     * @return
     */
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 根据日期和时间进行拼接
     *
     * @param date 格式 yyyy-MM-dd
     * @param time 格式 hh:mm
     * @return
     */
    public static Date concatDateTime(Date date, String time) {
        return parse(getDateString(date) + " " + time + ":00");
    }

    /**
     * 获取到月份的时间：yyyyMM
     *
     * @return
     */
    public static String getMonthDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(MONTHFORMATTPL);
        return sdf.format(new Date());
    }

    /**
     * 获取到月份的时间：yyyy-MM
     *
     * @return
     */
    public static String getMonthDateStringCable() {
        SimpleDateFormat sdf = new SimpleDateFormat(MONTHFORMATTPLCABLE);
        return sdf.format(new Date());
    }

    /**
     * 获取当前的月份时间
     *
     * @return
     */
    public static Date getCurMonthDate() {
        Date rtn = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(MONTHFORMATTPL);
            String curDate = getMonthDateString();
            rtn = sdf.parse(curDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return rtn;
    }

    public static boolean isFirstDayOfMonth() {
        boolean isFirstDay = false;
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        if (today == 1) {
            isFirstDay = true;
        }
        return isFirstDay;
    }

    public static String getBeforeMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat(MONTHFORMATTPL);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date beforMonthDate = calendar.getTime();
        return sdf.format(beforMonthDate);
    }

    /**
     * 获取一年内时间字符串列表(时间格式：yyyyMM)
     *
     * @return
     */
    public static List<String> getYearDateList() {
        List<String> yearDates = new ArrayList<>();
        Date startDate = getCurMonthDate();
        // 定义日期实例
        Calendar dd = Calendar.getInstance();
        // 设置日期起始时间
        dd.setTime(startDate);
        SimpleDateFormat sdf = new SimpleDateFormat(MONTHFORMATTPL);
        for (int i = 0; i < 12; i++) {
            String str = sdf.format(dd.getTime());
            yearDates.add(str);
            dd.add(Calendar.MONTH, 1); //进行当前日期月份加1
        }
        return yearDates;
    }
    /**
     * 获取任意时间字符串列表(时间格式：yyyy-MM-dd HH:mm:ss)
     *
     * @return
     */
    public static List<String> getYearDateList1(int mon) {
        List<String> yearDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(calendar.MONTH,1);
        Date zero = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 定义日期实例
        GregorianCalendar gc =new GregorianCalendar();
        // 设置日期起始时间
        gc.setTime(zero);

        for (int i = 0; i < mon; i++) {
            String beforeTime = sdf.format(gc.getTime());
            yearDates.add(beforeTime);
            gc.add(GregorianCalendar.MONTH,-1);//进行当前日期月份减1
        }
        return yearDates;
    }

    public static List<String> getDayDateList(int dy) {
        List<String> yearDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
//        calendar.add(calendar.DATE,1);
        Date zero = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 定义日期实例
        GregorianCalendar gc =new GregorianCalendar();
        // 设置日期起始时间
        gc.setTime(zero);

        for (int i = 0; i < dy; i++) {
            String beforeTime = sdf.format(gc.getTime());
            yearDates.add(beforeTime);
            gc.add(GregorianCalendar.DATE,-1);//进行当前日期天数减1
        }
        return yearDates;
    }
    //获取前一天23:59:59
    public static String getDayBefore(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        Date dd = calendar.getTime();
        String time = format(dd);
        return time;
    }
    public static boolean isValidDate(String str, String pattern) {
        boolean convertSuccess = true;
        // 指定时间格式由外部传入
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证时间，
            // 比如   1）2017/02/29会被接受，并转换成2017/03/01
            //     2）2017-07-01 66:66:66会被接受，并转换成2017-07-03 19:07:06
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 字符串转日期(严格模式)
     *
     * @param source
     * @return
     */
    public static Date getDateStrict(String source, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 获取date后一天的时间
     *
     * @param date
     * @return
     */
    public static Date getNextDate(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    /**
     * 日期转为指定格式的字符串.
     *
     * @param date 要格式化的日期
     * @return 日期字符串
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return "";
        }

        if (StringUtils.isNullOrEmpty(format)) {
            format = getDateTimePattern();
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 获取指定月份
     * month: 2019-05
     */
    public static Date getMonthEndTime(String month) {
        String time = month + "-01 00:00:00";
        Date date = parse(time);
        if (date != null){
            date.setMonth(date.getMonth() + 1);
            date.setSeconds(date.getSeconds() - 1);
        }
        return date;
    }

    public static Date getYearEndTime(int year) {
        String time = (year + 1) + "-01-01 00:00:00";
        Date date = parse(time);
        if (date != null){
            date.setSeconds(date.getSeconds() - 1);
        }
        return date;
    }

    /**
     * 获取精确到秒的时间戳
     *
     * @param date
     * @return
     */
    public static long getSecondTimestamp(Date date) {
        if (null == date) {
            return 0;
        }
        return date.getTime() / 1000;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(long s) {
        Date date = new Date(s);
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATTPL);
        return sdf.format(date);
    }

    /**
     * 日期加n个月
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addMonth(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);// 日期加n个月
        return cal.getTime();
    }

    /**
     * 日期加n天
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addDay(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, n);// 日期加n个小时
        return cal.getTime();
    }

    /**
     * 日期加n个小时
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addHour(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, n);// 日期加n个小时
        return cal.getTime();
    }

    /**
     * 日期加n分钟
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addMinute(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, n);// 日期加n个分钟
        return cal.getTime();
    }

    /**
     * 日期加n秒
     *
     * @param date
     * @param n
     * @return
     */
    public static Date addSecond(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, n);// 日期加n秒
        return cal.getTime();
    }

    /**
     * 获取指定日期所在月份的最后一天
     *
     * @param date
     * @return
     */
    public static String getLastDayOfMonth(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATTPL);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
            //获取最终的时间
            return sdf.format(c.getTime());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 验证年份有效性
     *
     * @param year
     * @return
     */
    public static Boolean isYearValid(String year) {
        try {
            int yearTemp = Integer.parseInt(year);
            if (yearTemp < 1900) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public static int getCurYear() {
        Calendar date = Calendar.getInstance();
        return date.get(Calendar.YEAR);
    }

    /**
     * 设置整点
     *
     * @return
     */
    public static Date setPunctuality(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    /**
     * 设置小时末尾
     *
     * @return
     */
    public static Date setTailTime(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    /**
     * @Author tt
     * @CreateTime 2020/8/27 18:15
     */
    public static List<Date> cornTransTime(String cronExpression, Date dayBefore,Date dayAfter) {
        List<Date> validTimeList = new ArrayList<Date>();
        if (cronExpression == null || cronExpression.length() < 1) {
            return validTimeList;
        } else {
            CronExpression exp = null;
            try {
                exp = new CronExpression(cronExpression);
            } catch (Exception e) {
                e.getMessage();
                return validTimeList;
            }
            Date date = new Date();
            Date dd = new Date();
            if (dayAfter.getYear() <= date.getYear()) {
                if (date.getMonth() > dayBefore.getMonth()) { return validTimeList;}
                if (date.getMonth() == dayBefore.getMonth()) { dd = exp.getNextValidTimeAfter(date); }
                if (date.getMonth() < dayBefore.getMonth()) { dd = exp.getNextValidTimeAfter(dayBefore); }
                while (dd.getTime() < dayAfter.getTime()) {
                    validTimeList.add(dd);
                    dd = exp.getNextValidTimeAfter(dd);
                }
            } else {
                dd = exp.getNextValidTimeAfter(dayBefore);
                while (dd.getTime() < dayAfter.getTime()) {
                    validTimeList.add(dd);
                    dd = exp.getNextValidTimeAfter(dd);
                }
            }
            exp = null;
//            Calendar calendar = Calendar.getInstance();
//            String cronDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
//            String sStart = cronDate + " 00:00:00";
//            Date dStart = null;
//            Date dEnd = null;
//            try {
//                dStart = sdf.parse(sStart);
//                calendar.setTime(dStart);
//                calendar.add(Calendar.DATE, 1);
//                dEnd = calendar.getTime();
//            } catch (Exception e) { e.getMessage(); }
//            validTimeList.add(sdf.format(dd));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> validTimeList2 = new ArrayList<String>();
        for (Date aValidTimeList:validTimeList) {
            validTimeList2.add(sdf.format(aValidTimeList));
        }
        System.out.println("validTimeList2: "+validTimeList2);
        return validTimeList;
    }

    public static String createCronExpression(Map<String, String> mapTime){

        String croExp="";
        String min = mapTime.get("min");
        String hour = mapTime.get("hour");
        String dayOfMonth = mapTime.get("dayOfMonth");
        String dayOfWeek = mapTime.get("dayOfWeek");
        String month = mapTime.get("month");
        String year = mapTime.get("year");


        //按日执行：每天上午10:15触发：0 15 10 ? * *
        if (!hour.equals("") && dayOfMonth.equals("") && dayOfWeek.equals("")) {
            if (Objects.equals("", min)) {
                croExp = String.format("0 %s %s ? * *", 0, hour);
            } else {
                croExp = String.format("0 %s %s ? * *", min, hour);
            }
        }
        //按周执行：每周三四点执行：0 0 4 ? * 3
        if (dayOfMonth.equals("") && !dayOfWeek.equals("")) {
            if (Objects.equals("", min)) {
                croExp = String.format("0 %s %s ? * %s", 0, hour,
                        dayOfWeek);
            } else {
                croExp = String.format("0 %s %s ? * %s", min, hour,
                        dayOfWeek);
            }
        }
        //按月执行：每月15日上午10:15触发：0 15 10 15 * ?
        if (!dayOfMonth.equals("") && dayOfWeek.equals("")) {
            if (Objects.equals("", min)) {
                croExp = String.format("0 %s %s %s * ?", 0, hour,
                        dayOfMonth);
            } else {
                croExp = String.format("0 %s %s %s * ?", min, hour,
                        dayOfMonth);
            }
        }
        return croExp;
    }

    /**
     *
     *方法摘要：构建Cron表达式
     *@param  tCruiseTaskCron
     *@return String
     */
    public static String createCronExpression2(TCruiseTaskCron tCruiseTaskCron){
        StringBuffer cronExp = new StringBuffer("");

        if(null == tCruiseTaskCron.getJobType()) {
            System.out.println("执行周期未配置" );//执行周期未配置
        }

        if (null != tCruiseTaskCron.getSecond()
                && null == tCruiseTaskCron.getMinute()
                && null == tCruiseTaskCron.getHour()){
            //每隔几秒
            if (tCruiseTaskCron.getJobType().equals("0")) {
                cronExp.append("0/").append(tCruiseTaskCron.getSecond());
                cronExp.append(" ");
                cronExp.append("* ");
                cronExp.append("* ");
                cronExp.append("* ");
                cronExp.append("* ");
                cronExp.append("?");
            }

        }

        if (null != tCruiseTaskCron.getSecond()
                && null != tCruiseTaskCron.getMinute()
                && null == tCruiseTaskCron.getHour()){
            //每隔几分钟
            if (tCruiseTaskCron.getJobType().equals("4")) {
                cronExp.append("* ");
                cronExp.append("0/").append(tCruiseTaskCron.getMinute());
                cronExp.append(" ");
                cronExp.append("* ");
                cronExp.append("* ");
                cronExp.append("* ");
                cronExp.append("?");
            }

        }

        if (null != tCruiseTaskCron.getSecond()
                && null != tCruiseTaskCron.getMinute()
                && null != tCruiseTaskCron.getHour()) {
            //秒
            cronExp.append(tCruiseTaskCron.getSecond()).append(" ");
            //分
            cronExp.append(tCruiseTaskCron.getMinute()).append(" ");
            //小时
            cronExp.append(tCruiseTaskCron.getHour()).append(" ");

            //每天
            if(tCruiseTaskCron.getJobType().equals("1")){
                cronExp.append("* ");//日
                cronExp.append("* ");//月
                cronExp.append("?");//周
            }

            //按每周
            else if(tCruiseTaskCron.getJobType().equals("3")){
                //一个月中第几天
                cronExp.append("? ");
                //月份
                cronExp.append("* ");
                //周
                String[] weeks = tCruiseTaskCron.getDayOfWeeks();
                for(int i = 0; i < weeks.length; i++){
                    if(i == 0){
                        cronExp.append(weeks[i]);
                    } else{
                        cronExp.append(",").append(weeks[i]);
                    }
                }

            }

            //按每月
            else if(tCruiseTaskCron.getJobType().equals("2")){
                //一个月中的哪几天
                String[] days = tCruiseTaskCron.getDayOfMonths();
                for(int i = 0; i < days.length; i++){
                    if(i == 0){
                        cronExp.append(days[i]);
                    } else{
                        cronExp.append(",").append(days[i]);
                    }
                }
                //月份
                cronExp.append(" * ");
                //周
                cronExp.append("?");
            }

        }
        else {
            System.out.println("时或分或秒参数未配置" );//时或分或秒参数未配置
        }
        return cronExp.toString();
    }
    // 获得本周一0点时间
    public static String getWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, 0);
        cal.set(Calendar.DAY_OF_WEEK, 2);
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time);
    }
    // 获得本周日24点时间
    public static String getWeekEnd(){
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date time=cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(time);
    }
    //获得上周一0点时间
    public static String getLastWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK,cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, -12);
        Date time=cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time);
    }
    //获得上周日24点时间
    public static String getLastWeekend() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK,cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, -6);
        Date time=cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(time);
    }
    //将yyyyMMddHHmmss格式时间的字符串转为yyyy-MM-dd HH:mm:ss格式时间的字符串
    public static String changeTime1(String cTime){
        if (cTime == null || cTime == ""){
            return "";
        }else {
            SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String changedTime = null;
            try {
                Date date = sf1.parse(cTime);
                changedTime = sf2.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return changedTime;
        }
    }
    //将yyyy-MM-dd HH:mm:ss格式时间的字符串转为yyyyMMddHHmmss格式时间的字符串
    public static String changeTime2(String cTime){
        if (cTime == null || cTime == ""){
            return "";
        }else {
            SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String changedTime = null;
            try {
                Date date = sf2.parse(cTime);
                changedTime = sf1.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return changedTime;
        }
    }
}
