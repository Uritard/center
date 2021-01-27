package com.yjh.logs.common.utils.uid;

import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lichensi
 * @date 2020/12/9 15:14
 */
public final class UIDTools {

    private static String IP_ADDRESS;
    private static int PROCESS_ID;
    private static char[] charSet32;
    private static final char PRE_FILL = 'O';
    private static final int BIT_5 = 5;
    private static final long LONG_31 = 0x1FL;

    static {
        IP_ADDRESS = "";
        PROCESS_ID = 0;
        charSet32 = "0123456789ABCDEFGHJKLMNPQRSTUVWX".toCharArray();
    }

    private UIDTools() {

    }

    public static final int getProcessID() {
        if (PROCESS_ID > 0) {
            return PROCESS_ID;
        }
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return PROCESS_ID = Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

    public static String base10ToBase32String(final long idx) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (long val = idx; val != 0L; val >>= BIT_5) {
            stringBuilder.append(charSet32[(int)(val & LONG_31)]);
        }
        return stringBuilder.toString();
    }

    public static long base32String2Base10Long(final String base32String) {
        final String str = trim(base32String);
        long base10 = 0L;
        final int length = str.length();
        for (int i = length - 1; i >= 0; --i) {
            base10 <<= 5;
            final char ch = str.charAt(i);
            for (int j = 0; j < charSet32.length; ++j) {
                if (ch == charSet32[j]) {
                    base10 ^= j;
                    break;
                }
            }
        }
        return base10;
    }

    public static String getLocalMac() {
        final InetAddress local = getInetAddress();
        try {
            return byte2String(NetworkInterface.getByInetAddress(local).getHardwareAddress());
        }
        catch (SocketException e) {
            throw new IllegalStateException("获取本地MAC地址失败", e);
        }
    }

    public static String reverse2Mac(final String base32MacString) {
        final long mac = base32String2Base10Long(base32MacString);
        final StringJoiner stringJoiner = new StringJoiner(":");
        final String hexStr = Long.toHexString(mac);
        for (int idx = 0; idx < hexStr.length(); idx += 2) {
            final String bitHex = hexStr.substring(idx, idx + 2);
            stringJoiner.add(bitHex.toUpperCase());
        }
        return stringJoiner.toString();
    }

    public static String getLocalIP() {
        final InetAddress local = getInetAddress();
        return byte2String(local.getAddress());
    }

    public static InetAddress getCurrentIp() {
        try {
            final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                final Enumeration<InetAddress> enumerationInetAddress = networkInterface.getInetAddresses();
                while (enumerationInetAddress.hasMoreElements()) {
                    final InetAddress inetAddress = enumerationInetAddress.nextElement();
                    if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        }
        catch (SocketException e) {
            throw new IllegalStateException("获取本地IP地址失败 getCurrentIp()", e);
        }
        throw new IllegalStateException("获取本地IP地址失败 匹配到0个可用IP");
    }

    public static String getHostAddress() {
        if (StringUtils.isNotBlank(IP_ADDRESS)) {
            return IP_ADDRESS;
        }
        final InetAddress local = getInetAddress();
        return IP_ADDRESS = local.getHostAddress();
    }

    private static InetAddress getInetAddress() {
        InetAddress local;
        try {
            local = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            local = getCurrentIp();
        }
        return local;
    }

    public static String reverse2IP(final String base32IPString) {
        final long address = base32String2Base10Long(base32IPString);
        final StringJoiner stringJoiner = new StringJoiner(".");
        String hexStr = Long.toHexString(address);
        if (1 == hexStr.length() % 2) {
            hexStr = "0" + hexStr;
        }
        for (int idx = 0; idx < hexStr.length(); idx += 2) {
            final String bitHex = hexStr.substring(idx, idx + 2);
            stringJoiner.add(String.valueOf(Integer.parseInt(bitHex, 16)));
        }
        return stringJoiner.toString();
    }

    private static String byte2String(final byte[] bytes) {
        return byte2String(bytes, 10);
    }

    private static String byte2String(final byte[] bytes, final int fixedLen) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            final int temp = bytes[i] & 0xFF;
            if (temp > 15) {
                stringBuilder.append(Integer.toHexString(temp));
            }
            else {
                stringBuilder.append(0);
                stringBuilder.append(Integer.toHexString(temp));
            }
        }
        final long val1 = Long.parseLong(stringBuilder.toString(), 16);
        final String base32Str = base10ToBase32String(val1);
        return appendPrefixWithFixedLength(base32Str, fixedLen);
    }

    public static String appendPrefixWithFixedLength(final String base32Str, final int fixedLen) {
        return appendPrefixWithFixedLength(PRE_FILL, base32Str, fixedLen);
    }

    public static String appendPrefixWithFixedLength(final char fill, final String base32Str, final int fixedLen) {
        final int len = base32Str.length();
        if (len > fixedLen) {
            throw new IllegalStateException("字符串超长");
        }
        if (len < fixedLen) {
            final StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < fixedLen - len; ++i) {
                prefix.append(fill);
            }
            return prefix.append(base32Str).toString();
        }
        return base32Str;
    }

    private static String trim(final String base32IPString) {
        int startIndex = 0;
        for (int i = 0; i < base32IPString.length() && PRE_FILL == base32IPString.charAt(i); ++i) {
            ++startIndex;
        }
        return (0 == startIndex) ? base32IPString : base32IPString.substring(startIndex);
    }

    public static String fixedLengthRandomHexString(int length) {
        if (length <= 0) {
            length = 1;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            stringBuilder.append(charSet32[random.nextInt(32)]);
        }
        return stringBuilder.toString();
    }
}
