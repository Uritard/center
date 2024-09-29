package com.yjh.accessmeter.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;


/**
 * 字节数组工具类
 * Created by tt on 2018/3/6.
 */
public class ByteUtil {

    private static Logger logger = LoggerFactory.getLogger(ByteUtil.class);

    /**
     * 16进制中的字符集
     */
    private static final String HEX_CHAR = "0123456789ABCDEF";

    /**
     * 16进制中的字符集对应的字节数组
     */
    private static final byte[] HEX_STRING_BYTE = HEX_CHAR.getBytes();

    /**
     * int类型数据在Java中占据32 bit，byte占据8 bit，4个byte可以转换成一个int类型数据
     *
     * @param b
     * @return
     */
    public static int bytes4ToInt(byte[] b) {
        if (b == null) {
            return 0;
        }
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }

    /**
     * 3字节数组转int
     *
     * @param bytes
     * @return
     */
    public static int bytes3ToInt(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        return (bytes[0] & 0xff) << 16 | (bytes[1] & 0xff) << 8 | (bytes[2] & 0xff);
    }

    /**
     * 2字节数组转int
     *
     * @param bytes
     * @return
     */
    public static int bytes2ToInt(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        return bytes[0] << 8 | (bytes[1] & 0xff);
    }

    /**
     * 1节数组转int
     *
     * @param bytes
     * @return
     */
    public static int byte1ToInt(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        return bytes[0] & 0xFF;
    }

    /**
     * 1字节转10进制
     */
    public static int byte2Int(byte b) {
        return b & 0xFF;
    }

    /**
     * 10进制转字节数组
     *
     * @param i
     * @return
     */
    public static byte[] int2Byte(int i) {
        byte[] src = new byte[4];
        src[0] = (byte) ((i >> 24) & 0xFF);
        src[1] = (byte) ((i >> 16) & 0xFF);
        src[2] = (byte) ((i >> 8) & 0xFF);
        src[3] = (byte) (i & 0xFF);
        return src;
    }

    /**
     * 10进制字节数据组成16进制字符串
     *
     * @param byteArray
     * @return
     */
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            return "";

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    // 从十六进制字符串到字节数组转换
    public static byte[] HexString2Bytes(String hexstr) {
        hexstr = hexstr.replace(" ", "");
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    private static int parse(char c) {
        if (c >= 'a') {
            return (c - 'a' + 10) & 0x0f;
        }
        if (c >= 'A') {
            return (c - 'A' + 10) & 0x0f;
        }
        return (c - '0') & 0x0f;
    }
    /**
     * 10进制字节数据组成16进制字符串
     *
     * @param byteArray
     * @return
     */
    public static String toHexNoZeroString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            return "";

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    /**
     * 16进制字符串转换成为string类型字符串
     *
     * @param s
     * @param decodetype
     * @return
     */
    public static String hexStringToString(String s, int decodetype) {
        if (s == null || s.equals("") || s.trim().length() == 0) {
            return "";
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        try {
            if (decodetype == 2) {
                s = new String(baKeyword, "GBK");
                new String();
            } else if (decodetype == 3) {
                s = new String(baKeyword, "UTF-8");
                new String();
            }
        } catch (Exception e1) {
            logger.info(e1.getMessage());
        }
        return s.trim();
    }

    /**
     * 16进制字符串转字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | (low& 0xff));
            k += 2;
        }
        return byteArray;
    }

    /**
     * 对16进制数组进行base64编码
     *
     * @param byteArray
     * @return
     */
    public static String toBase64Encoding(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            return "";

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(byteArray);
    }

    /**
     * 截字节数组
     *
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    /**
     * 16进制字节数组转int
     *
     * @param bytes
     * @return
     */
    public static int[] bytesToInts(byte[] bytes) {
        int bytesLength = bytes.length;
        int[] ints = new int[bytesLength % 4 == 0 ? bytesLength / 4 : bytesLength / 4 + 1];
        int lengthFlag = 4;
        while (lengthFlag <= bytesLength) {
            ints[lengthFlag / 4 - 1] =
            (bytes[lengthFlag - 4] << 24) | (bytes[lengthFlag - 3] & 0xff) << 16 | (bytes[lengthFlag - 2] & 0xff) << 8
            | (bytes[lengthFlag - 1] & 0xff);
            lengthFlag += 4;
        }
        for (int i = 0; i < bytesLength + 4 - lengthFlag; i++) {
            if (i == 0)
                ints[lengthFlag / 4 - 1] |= bytes[lengthFlag - 4 + i] << 8 * (bytesLength + 4 - lengthFlag - i - 1);
            else
                ints[lengthFlag / 4 - 1] |=
                (bytes[lengthFlag - 4 + i] & 0xff) << 8 * (bytesLength + 4 - lengthFlag - i - 1);
        }
        return ints;
    }

    /**
     * 任意长度bytes转成int
     *
     * @param bytes
     * @return
     */
    public static int bytes2Int(byte[] bytes) {
        int[] ints = bytesToInts(bytes);
        int result = 0;

        for (int i = 0; i < ints.length; i++) {
            result += ints[i];
        }

        return result;
    }

    /**
     * 10进制字节数组转16进制字节数组
     * 网上抄的
     * @param hexArray
     * @return
     */
    public static byte[] byte2hex(byte[] hexArray) {
        if (hexArray == null || hexArray.length < 1) {
            return null;
        }
        int length = hexArray.length;
        byte[] intArray = new byte[length << 1];
        int pos;
        for (int i = 0; i < length; i++) {
            pos = 2 * i;
            intArray[pos] = HEX_STRING_BYTE[(hexArray[i] & 0xf0) >> 4];
            intArray[pos + 1] = HEX_STRING_BYTE[hexArray[i] & 0x0f];
        }
        return intArray;
    }

    /**
     * 16进制转10进制
     * 网上抄的
     * @param b
     * @return
     */
    public static byte[] hex2byte(byte[] b) {
        if (b.length % 2 != 0) {
            throw new IllegalArgumentException("byte array length is not even!");
        }

        int length = b.length >> 1;
        byte[] b2 = new byte[length];
        int pos;
        for (int i = 0; i < length; i++) {
            pos = i << 1;
            b2[i] = (byte) (HEX_CHAR.indexOf(b[pos]) << 4 | HEX_CHAR.indexOf(b[pos + 1]));
        }
        return b2;
    }

    public static String spaceAt2(String str,int count) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i += count) {
            if (length - i <= 2*count) {      //防止ArrayIndexOutOfBoundsException
                sb.append(str.substring(i, i + count)).append(" ");
                sb.append(str.substring(i + count));
                break;
            }
            sb.append(str.substring(i, i + count)).append(" ");
        }
        return sb.toString();
    }

    public static int parseInt(String num){
        int result =0;
        try{
            result = Integer.parseInt(num);
        }catch (NumberFormatException  e){

        }
        return result;
    }

    public static String bcdPhoneNum(byte[] bytes){
        if (bytes == null || bytes.length != 6) {
            return "";
        }
        String phone = toHexString(bytes);
        phone = phone.replaceAll("(^0?)|(0?$)","");
        return  phone;
    }
}
