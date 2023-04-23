package com.yjh.platform.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

/**
 * @author tt
 * @ClassName:
 * @Description: 封装公共方法
 * @date 2018/5/17 14:43
 */
public class CommonUtils {
    /**
     * @Fields logger : 日志
     */
    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * 获取payLoad 数据
     *
     * @param request request
     * @return payload 参数
     * @throws IOException 异常
     */
    public static String getPayloadRequest(HttpServletRequest request) throws IOException {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    logger.error("getPayloadRequest", ex);
                }
            }
        }
        body = stringBuilder.toString();
        //logger.info("by payload get params={}", body);
        return body;
    }

    /**
     * 检查对象是否是数组
     *
     * @param obj 对象
     * @return
     */
    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * 检查字节数组是否为空
     *
     * @param array
     * @return
     */
    public static boolean isEmpty(byte[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmptyOrNullstr(String str) {
        return StringUtils.isEmpty(str) || "null".equalsIgnoreCase(str);
    }

    /**
     * @param json String
     * @return true/false
     * @Title: isJsonObj
     * @Description: 判断是否是json 格式的字符串
     */
    public static boolean isJsonObj(String json) {
        if (null == json || "".equals(json)) {
            return false;
        }
        try {
            JSONObject.parseObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param json String
     * @return true/false
     * @Title: isJsonArray
     * @Description: 判断是否是json 格式的数组
     */
    public static boolean isJsonArray(String json) {
        if (null == json || "".equals(json)) {
            return false;
        }
        try {
            JSONObject.parseArray(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取格式化时间
     *
     * @param format 格式化类型  yyyy-MM-dd HH:mm:ss/YYYYMMddhhmmss
     * @return 格式化后的时间
     */
    public static String getCurrentDate(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateNowStr = sdf.format(date);
        return dateNowStr;
    }

    /**
     * 原格式时间数据转成新格式数据
     *
     * @param time      时间
     * @param oldFormat 原格式类型
     * @param newFormat 现格式类型
     * @return 新格式时间类型
     */
    public static String getFormatTime(String time, String oldFormat, String newFormat) {
        String newTime = "";
        try {
            SimpleDateFormat oldSdf = new SimpleDateFormat(oldFormat);
            SimpleDateFormat newSdf = new SimpleDateFormat(newFormat);
            Date oldDate = null;
            oldDate = oldSdf.parse(time);
            newTime = newSdf.format(oldDate);
        } catch (ParseException e) {
            logger.error("时间格式转换出错   time={}", time);
        }
        return newTime;
    }

    public static String mathRandom(int count) {
        StringBuilder math = new StringBuilder();
        if (count > 0) {
            while (math.length() < count) {
                math.append((int) (Math.random() * 10));
            }
        }
        return math.toString();

    }


    /**
     * 获取前端数据 参数
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static String getStringData(HttpServletRequest request) throws Exception {
        byte[] content = null;
        try (InputStream in = request.getInputStream();
             BufferedInputStream buf = new BufferedInputStream(in);){
            int buffSize = 1024;
            ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);
            byte[] temp = new byte[buffSize];
            int size = 0;
            while ((size = buf.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            content = out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * InputStream --> File
     *
     * @param ins
     * @param file
     * @throws Exception
     */
    public static void inputstreamToFile(InputStream ins, File file) {
        try (OutputStream os  = new FileOutputStream(file)){
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != ins) {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 检查是否是本机
     *
     * @param ipStr ip
     * @return
     */
    public boolean isLocalIp(String ipStr) {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress ip = ips.nextElement();
                    if (ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()   //127.开头的都是lookback地址
                            && ip.getHostAddress().indexOf(":") == -1
                            && ipStr.equals(ip.getHostAddress())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("检查是否是本机异常！", e);
        }
        return false;
    }


    /**
     * @param v
     * @return
     */
    public static String getValue(Object v) {
        return v == null ? "" : v.toString();
    }

    /**
     * 读配置文件
     *
     * @param projectDirPath 文件路径
     * @return
     */
    public static String readFileContent(String projectDirPath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(projectDirPath))));){
            String line = null;
            while ((line = br.readLine()) != null) {
                content.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * 下载文件
     *
     * @param name     文件名
     * @param path     文件路径
     * @param response response
     */
    public static void downFileByPath(String name, String path, HttpServletResponse response) {
        try {
            // 设置响应的编码方式;
            response.setCharacterEncoding("utf-8");
            try {
                response.addHeader("Content-Disposition", "attachment;filename="
                        + URLEncoder.encode(name, "utf-8"));
                response.setContentType("application/zip;charset=utf-8");
                // excel模板所在的位置;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
                 BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());) {
                int size = 0;
                byte[] buff = new byte[1024];
                while ((size = bis.read(buff)) > 0) {
                    bos.write(buff, 0, size);
                }
                bos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取前多少分钟时间
     *
     * @param format      转换成的时间格式 YYYYMMddHHmmss
     * @param forwardTime 前多少分钟
     * @return
     */
    public String getBeforeTime(String format, int forwardTime) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar beforeTime = Calendar.getInstance();
        // xx分钟之前的时间
        beforeTime.add(Calendar.MINUTE, -forwardTime);
        Date beforeD = beforeTime.getTime();
        String time = sdf.format(beforeD);
        return time;
    }

    /**
     * 比较数值型字符串大小
     *
     * @param version1
     * @param version2
     * @return
     */
    public static int compareVersion(String version1, String version2) {

        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");

        for (int i = 0; i < Math.max(v1.length, v2.length); i++) {
            int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            if (num1 < num2) {
                return -1;
            } else if (num1 > num2) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 除法，并格式化结果
     *
     * @param v1     除数
     * @param v2     被除数
     * @param format 数字格式化类型 00.00，0.00%等
     * @return
     */
    public static String percentFormat(String v1, String v2, String format) {
        if (!StringUtils.isEmpty(v1) && !StringUtils.isEmpty(v2)) {
            DecimalFormat decimalFormat = new DecimalFormat(format);
            double percent = Double.parseDouble(v1) / Double.parseDouble(v2) * 100;
            return decimalFormat.format(percent);
        }
        return "";
    }

    /**
     * 将数字格式化
     *
     * @param v
     * @param format
     * @return
     */
    public static String percentFormat(float v, String format) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat(format);
            return decimalFormat.format(v);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "0.0";
    }

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
            StringBuilder ret = new StringBuilder(bytes.length * 2);
            for (int i = 0; i < bytes.length; i++) {
                ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
                ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
            }
            return ret.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 将StringBuilder最后一个字符删除
     */
    public static void clearLastChar(StringBuilder sb) {
        if (sb != null && sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
