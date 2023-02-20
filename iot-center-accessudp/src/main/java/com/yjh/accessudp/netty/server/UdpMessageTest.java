/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessudp.netty.server;

import com.alibaba.fastjson.JSON;
import com.yjh.accessudp.common.Constant;
import com.yjh.accessudp.common.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/9/22
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class UdpMessageTest {
    private static final Charset charset = Charset.forName("GB2312");

    public static List<String> listAllByte = new ArrayList<>();
    public static Map<Integer, List<String>> dataMap = new HashMap<>();

    public static void main(String[] args) {
        String str = udpParse(1001, 1, "变位", "控合");
        udpVerify(str);

        String path = "D:\\webapp\\temp\\udpParseFile.cime";
        List<String> filePackage = udpParseFile(path);
        System.out.println("==================== 报文验证 ====================");
        for (String pac : filePackage) {
            udpVerify(pac);
        }
        System.out.println("==================== 报文文本 ====================");
        System.out.println(String.join("\n\n", filePackage));
    }

    public static List<String> udpParseFile(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            log.error("资源配置文件不存在！");
            return Collections.emptyList();
        }

        List<String> packageList = new ArrayList<>();
        try {
            String fileContent = FileUtils.readFileToString(file, charset);
            log.info("file content: {}", fileContent);
            byte[] packageBytes = fileContent.getBytes(charset);
            int plen = packageBytes.length / 240;
            for (int i = 0; i < plen; i++) {
                boolean isEnd = (i + 1) * 240 == packageBytes.length;

                packageList.add(udpFilePackage(ArrayUtils.subarray(packageBytes, i * 240, (i + 1) * 240), i, i * 240, isEnd));
            }
            int stx = plen * 240;
            if (stx < packageBytes.length) {
                packageList.add(udpFilePackage(ArrayUtils.subarray(packageBytes, stx, packageBytes.length), plen, stx, true));
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return packageList;
    }

    public static String udpFilePackage(byte[] content, int idx, int pos, boolean isEnd) {

        StringBuilder sb = new StringBuilder();
        sb.append("eb 90 eb 90 43").append(" 01 00").append(isEnd ? "00" : "01");
        sb.append(toIntHex(idx, 2));
        sb.append(toIntHex(pos, 4));
        sb.append(toIntHex(content.length));
        sb.append(ByteUtil.toHexString(content));

        String hexStr = StringUtils.deleteWhitespace(sb.toString());
        int he = 0;
        for (int i = 10; i < hexStr.length() - 1; i += 2) {
            he = he + Integer.parseInt(hexStr.substring(i, i + 2), 16);
        }
        int check = he % 256;
        log.info("计算校验和 {}，{}", he, check);
        hexStr += toIntHex(check);
        String udpByte = hexFormat(hexStr);
        System.out.println("UDP: " + udpByte);
        return udpByte;
    }

    public static String udpParse(int meteId, int meteKind, String attrValue, String action) {
        StringBuilder sb = new StringBuilder();
        sb.append("eb 90 eb 90 55").append(" 01 00");
        sb.append(toIntHex(meteId));
        sb.append(toIntHex(meteKind));
        sb.append(toIntHex(attrValue.getBytes(charset).length));
        sb.append(Hex.encodeHexString(attrValue.getBytes(charset)));
        sb.append(toIntHex(action.getBytes(charset).length));
        sb.append(Hex.encodeHexString(action.getBytes(charset)));
        // sb.append("d4 b6 1f 14 99 08 16");
        String time = convert2CP56Time2a(LocalDateTime.now());
        System.out.println("time: " + time);
        sb.append(time);
        String hexStr = StringUtils.deleteWhitespace(sb.toString());
        int he = 0;
        for (int i = 10; i < hexStr.length() - 1; i += 2) {
            he = he + Integer.parseInt(hexStr.substring(i, i + 2), 16);
        }
        int check = he % 256;
        log.info("计算校验和 {}，{}", he, check);
        hexStr += toIntHex(check);
        String udpByte = hexFormat(hexStr);
        System.out.println("UDP: " + udpByte);
        return udpByte;
    }

    public static String toIntHex(int i) {
        String str = Integer.toHexString(i);
        if (str.length() % 2 != 0) {
            str = "0" + str;
        }
        StringBuilder ret = new StringBuilder(str);
        if (str.length() > 2) {
            ret = new StringBuilder();
            for (int j = str.length(); j >= 2; j -= 2) {
                ret.append(str, j - 2, j);
            }
        }
        return ret.toString();
    }

    public static String toIntHex(int i, int len) {
        String str = toIntHex(i);
        int lone = len * 2 - str.length();
        if (lone > 0) {
            for (int j = 0; j < lone; j += 2) {
                str += "00";
            }
        }
        return str;
    }

    public static String convert2CP56Time2a(LocalDateTime localDateTime) {
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        byte[] result = new byte[7];
        Calendar aTime = Calendar.getInstance();
        aTime.setTime(date);
        int milliseconds = localDateTime.getSecond() * 1000 + localDateTime.getNano() / 1000000;
        result[0] = (byte)(milliseconds % 256);
        result[1] = (byte)(milliseconds / 256);
        result[2] = (byte)aTime.get(Calendar.MINUTE);
        result[3] = (byte)aTime.get(Calendar.HOUR_OF_DAY);
        result[4] = (byte)aTime.get(Calendar.DAY_OF_MONTH);
        result[5] = (byte)(aTime.get(Calendar.MONTH) + 1);
        result[6] = (byte)(aTime.get(Calendar.YEAR) % 100);
        return Hex.encodeHexString(result);
    }

    public static String hexFormat(String hexString) {
        String hexStr = StringUtils.deleteWhitespace(hexString);

        int length = hexStr.length();
        StringBuilder builder = new StringBuilder(length + length / 2);
        builder.append(hexStr.charAt(0)).append(hexStr.charAt(1));

        for (int i = 2; i < length - 1; i += 2) {
            builder.append(' ').append(hexStr.charAt(i)).append(hexStr.charAt(i + 1));
        }

        return builder.toString();
    }

    public static void udpVerify(String str) {

        log.info("commandSend:  {}", str);

        //解析 Str = eb 90 eb 90 55 01 00 0a 27 01 06 e5 8a a8 e4 bd 9c 06 e5 90 88 e4 bd 8d
        String[] udp = str.split(" ");
        // 处理校验和
        if (udp.length > 6) {
            int he = 0;
            for (int i = 5; i < udp.length - 1; i++) {
                he = he + Integer.parseInt(udp[i], 16);
            }
            int check = Integer.parseInt(udp[udp.length - 1], 16);
            if (check != (he % 256)) {
                //校验和不对
                log.info("计算校验和{}，报文校验和{}", he, check);
                return;
            }
        }
        if ("55".equals(udp[4])) {
            //获取meteid
            int meteId = new BigInteger(udp[8] + udp[7], 16).intValue();
            log.info("meteId:       {}", meteId);
            //获取meteKind
            int meteKind = new BigInteger(udp[9], 16).intValue();

            log.info("meteKind:     {}", meteKind);
            //获取属性长度
            int valueLength = new BigInteger(udp[10], 16).intValue();
            log.info("valueLength:  {}", valueLength);
            //获取属性
            String value = arrayToString(udp, 11, valueLength, true);
            log.info("属性:          {}", value);
            //获取描述长度
            int weizhi = 10 + valueLength + 1;
            int commitLength = new BigInteger(udp[weizhi], 16).intValue();
            log.info("commitLength: {}", commitLength);
            //获取描述
            String commit = arrayToString(udp, weizhi + 1, commitLength, true);
            log.info("描述:          {}", commit);
            //获取时间
            String shijianchuo = arrayToString(udp, weizhi + 1 + commitLength, 7, false);
            String time = timeScale(shijianchuo);
            log.info("time:         {}", time);

        } else if ("43".equals(udp[4])) {
            int doesHas = new BigInteger(udp[7], 16).intValue();
            log.info("有无后续：  " + doesHas);
            int xuHao = new BigInteger(arrayToStringL(udp, 8, 2, false), 16).intValue();
            log.info("序号：{}", xuHao);

            // 其实传输位置 9-12
            int valueLength = new BigInteger(udp[14], 16).intValue();
            log.info("数据长度：{}", valueLength);
            List<String> listByte = new ArrayList<>();
            for (int i = 0; i < valueLength; i++) {
                listByte.add(udp[15 + i]);
                listAllByte.add(udp[15 + i]);
            }
            int weiZhi = new BigInteger(arrayToStringL(udp, 10, 4, false), 16).intValue();
            log.info("位置：" + weiZhi);
            dataMap.put(xuHao, listByte);

            if (doesHas == 0) {
                log.info("数据：{}", JSON.toJSONString(dataMap));
                List<String> listForSortByte = new ArrayList<>();
                for (int i = 0; i < dataMap.size(); i++) {
                    listForSortByte.addAll(dataMap.get(i));
                }
                log.info("listForSortByte:{}", JSON.toJSONString(listForSortByte));
                log.info(" listForAllByte:{}", JSON.toJSONString(listAllByte));
                String data;
                if (Constant.sort) {
                    data = arrayToString(listForSortByte);
                    data = toStringHex(data);
                    log.info("文件内容map： {}", data);
                } else {
                    data = arrayToString(listAllByte);
                    data = toStringHex(data);
                    log.info("文件内容： {}", data);
                }
            }
        }
    }

    public static void udpVerify2(String str) {
        String[] udp = str.split(" ");
        // 处理校验和
        if (udp.length > 6) {
            int he = 0;
            for (int i = 5; i < udp.length - 1; i++) {
                he = he + Integer.parseInt(udp[i], 16);
            }
            int check = Integer.parseInt(udp[udp.length - 1], 16);
            if (check != (he % 256)) {
                //校验和不对
                log.info("计算校验和{}，报文校验和{}", he, check);
                return;
            }
        }
        if ("55".equals(udp[4])) {
            //获取meteid
            int meteId = Integer.parseInt(new BigInteger(udp[8] + udp[7], 16).toString());
            log.info("meteId:       {}", meteId);
            //获取meteKind
            int meteKind = Integer.parseInt(new BigInteger(udp[9], 16).toString());
            log.info("meteKind:     {}", meteKind);
            //获取属性长度
            int valueLength = Integer.parseInt(new BigInteger(udp[10], 16).toString());
            log.info("valueLength:  {}", valueLength);
            //获取属性
            String value = arrayToString(udp, 11, valueLength, true);
            log.info("属性:          {}", value);
            //获取描述长度
            int weizhi = 10 + valueLength + 1;
            int commitLength = Integer.parseInt(new BigInteger(udp[weizhi], 16).toString());
            log.info("commitLength: {}", commitLength);
            //获取描述
            String commit = arrayToString(udp, weizhi + 1, commitLength, true);
            log.info("描述:          {}", commit);
            //获取时间
            String shijianchuo = arrayToString(udp, weizhi + 1 + commitLength, 7, false);
            String time = timeScale(shijianchuo);
            log.info("time:         {}", time);
        }
    }

    public static String arrayToString(List<String> udp) {
        return String.join("", udp);
    }

    public static String arrayToString(String[] udp, int start, int length, boolean flag) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(udp[start + i]);
        }
        String str = stringBuilder.toString();
        if (flag) {
            return toStringHex(str);
        }
        return str;
    }

    public static String arrayToStringL(String[] udp, int start, int length, boolean flag) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = length - 1; i >= 0; i--) {
            stringBuilder.append(udp[start + i]);
        }
        String str = stringBuilder.toString();
        if (flag) {
            return toStringHex(str);
        }
        return str;
    }

    public static String toStringHex(String s) {
        try {
            return new String(Hex.decodeHex(s), charset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String timeScale(String str) {
        StringBuilder result = new StringBuilder();

        int year = Integer.parseInt(str.substring(12, 14), 16) & 0x7F;
        int month = Integer.parseInt(str.substring(10, 12), 16) & 0x0F;
        int day = Integer.parseInt(str.substring(8, 10), 16) & 0x1F;
        int week = (Integer.parseInt(str.substring(8, 10), 16) & 0xE0) / 32;
        int hour = Integer.parseInt(str.substring(6, 8), 16) & 0x1F;
        int minute = Integer.parseInt(str.substring(4, 6), 16) & 0x3F;
        int second = (Integer.parseInt(str.substring(2, 4), 16) << 8) + Integer.parseInt(str.substring(0, 2), 16);

        result.append("20");
        result.append(year).append("-");
        result.append(String.format("%02d", month)).append("-");
        result.append(String.format("%02d", day)).append(" ");
        result.append(hour).append(":").append(minute).append(":");
        result.append(second / 1000);

        return result.toString();
    }
}
