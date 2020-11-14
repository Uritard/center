package com.yjh.accessrobot.common.utils;

import java.io.UnsupportedEncodingException;

/**
 * @author YC
 * @date 2020/11/13 - 13:40
 */
public class PlatformPacketUtil {
    private final static byte[] hex = "0123456789ABCDEF".getBytes();
    private String packet = "";
    private static PlatformPacketUtil packetUtil = null;

    public synchronized static PlatformPacketUtil getInstance() {
        if (packetUtil == null) {
            packetUtil = new PlatformPacketUtil();
        }
        return packetUtil;
    }


    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    // 从字节数组到十六进制字符串转换
    public static String Bytes2HexString(byte[] b) {
        byte[] buff = new byte[3 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[3 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[3 * i + 1] = hex[b[i] & 0x0f];
            buff[3 * i + 2] = 45;
        }
        String re = new String(buff);
        return re.replace("-", "");
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

    // 转化十六进制编码为字符串
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, "UTF-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    //byte数组转int
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }

    public static int bytesToInt1(byte[] src, int offset) {
        int value;
        value = (int) ( ((src[offset] & 0xFF))
                |((src[offset+1] & 0xFF)<<8)
                |((src[offset+2] & 0xFF)<<16)
                |(src[offset+3] & 0xFF)<<24);
        return value;
    }

    public static long bytesToLong(byte[] src) {
        long value = 0;
        value = (((long)src[0] & 0xFFL)) |
                ((long)src[1] << 8 & 0xFF00L) |
                ((long)src[2] << 16 & 0xFF0000L) |
                ((long)src[3] << 24 & 0xFF000000L) |
                ((long)src[4] << 32 & 0xFF00000000L)|
                ((long)src[5] << 40 & 0xFF0000000000L)|
                ((long)src[6] << 48 & 0xFF000000000000L)|
                ((long)src[7] << 56 & 0xFF00000000000000L);
        return value;
    }
    /**
     *
     * 生成发送的报文
     */
    public static byte[] createPacket(long receiveSessionId,long sendSessionId,boolean isSend,String body){
        //实际报文消息体
        byte[] soc = null;

        try {
            byte[] bodyBytes = body.getBytes("utf-8");
            int bodyLength = bodyBytes.length;

            //总的报文长度
            int socketLength = 2 + 8 + 8 + 1 + 4 + bodyLength + 2;
            // 准备发送的报文比特流
            soc = new byte[socketLength];
            int index = 0;

            //1.起始标志符
            soc[index++] = (byte)0xEB;
            soc[index++] = (byte)0x90;
            //2.发送会话序列号
            if(sendSessionId>0){
                soc[index++] = (byte) (sendSessionId & 0xff);
                soc[index++] = (byte) (sendSessionId >> 8 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 16 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 24 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 32 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 40 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 48 & 0xff);
                soc[index++] = (byte) (sendSessionId >> 56 & 0xff);
            }else{
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
            }

            //3.接收会话序列号
            if(receiveSessionId>0){
                soc[index++] = (byte) (receiveSessionId & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 8 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 16 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 24 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 32 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 40 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 48 & 0xff);
                soc[index++] = (byte) (receiveSessionId >> 56 & 0xff);
            }else{
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
                soc[index++] = 0x00;
            }
            //4.会话源标识
            if(isSend){
                soc[index++] = 0x00;//请求
            }else{
                soc[index++] = 0x01;//响应
            }
            //5.xml的字节长度
            soc[index++] = (byte) (bodyLength & 0xff);
            soc[index++] = (byte) (bodyLength >> 8 & 0xff);
            soc[index++] = (byte) (bodyLength >> 16 & 0xff);
            soc[index++] = (byte) (bodyLength >> 24 & 0xff);
            //6.交互内容
            for (int i = 0; i < bodyLength; i++) {
                soc[index++] = bodyBytes[i];
            }
            //7.结束标志符号
            soc[index++] = (byte)0xEB;
            soc[index++] = (byte)0x90;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return soc;
    }
}
