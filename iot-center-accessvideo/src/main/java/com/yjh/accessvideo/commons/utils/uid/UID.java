package com.yjh.accessvideo.commons.utils.uid;

import java.util.StringJoiner;

/**
 * @author lichensi
 * @date 2020/12/9 15:13
 */
public interface UID {

    String ID();

    default String reverse(final String uniqueID) {
        final StringJoiner stringJoiner = new StringJoiner("-");
        if ('O' == uniqueID.charAt(0)) {
            final String address = UIDTools.reverse2IP(uniqueID.substring(0, 10));
            stringJoiner.add(address);
        } else {
            final String address = UIDTools.reverse2Mac(uniqueID.substring(0, 10));
            stringJoiner.add(address);
        }
        stringJoiner.add(uniqueID.substring(10, 15));
        final long timeOfMillis = UIDTools.base32String2Base10Long(uniqueID.substring(15, 24));
        stringJoiner.add(String.valueOf(timeOfMillis));
        final long sequence = UIDTools.base32String2Base10Long(uniqueID.substring(24, 29));
        stringJoiner.add(String.valueOf(sequence));
        final long clock = UIDTools.base32String2Base10Long(uniqueID.substring(29));
        stringJoiner.add(String.valueOf(clock));
        return stringJoiner.toString();
    }

    default long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
