package com.yjh.accessvideo.commons.utils.uid;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lichensi
 * @date 2020/12/9 15:24
 */
public class UniqueID implements UID {

    private final String ADDRESS;
    private final String PROCESS_ID;
    private final AtomicLong sequenceCounter;
    private final AtomicLong clockCallbackCounter;
    private long lastTimeOfMillis;
    private static final int SEQUENCE_BIT_25 = 33554431;
    private static final int SEQUENCE_COUNTER_LEN = 5;
    private static final int SEQUENCE_BIT_15 = 32767;
    private static final int CLOCK_CALLBACK_COUNTER_LEN = 3;
    private static final int FIXED_LEN = 32;

    private UniqueID(final Type type) {
        this.sequenceCounter = new AtomicLong(0L);
        this.lastTimeOfMillis = System.currentTimeMillis();
        this.clockCallbackCounter = new AtomicLong(0L);
        if (Type.MAC.equals(type)) {
            this.ADDRESS = UIDTools.getLocalMac();
        } else {
            if (!Type.IP.equals(type)) {
                throw new IllegalArgumentException("not support@" + type.name());
            }
            this.ADDRESS = UIDTools.getLocalIP();
        }
        this.PROCESS_ID = String.format("%05d", UIDTools.getProcessID());
    }

    @Override
    public String ID() {
        final long currentTimeOfMillis = this.currentTimeMillis();
        if (currentTimeOfMillis < this.lastTimeOfMillis) {
            this.clockCallbackCounter.incrementAndGet();
        } else if (this.lastTimeOfMillis < currentTimeOfMillis) {
            this.lastTimeOfMillis = currentTimeOfMillis;
        }
        final StringBuilder stringBuilder = new StringBuilder(FIXED_LEN);
        stringBuilder.append(this.ADDRESS);
        stringBuilder.append(this.PROCESS_ID);
        stringBuilder.append(UIDTools.base10ToBase32String(currentTimeOfMillis));
        final long sequence = this.sequenceCounter.incrementAndGet() & SEQUENCE_BIT_25;
        stringBuilder.append(UIDTools.appendPrefixWithFixedLength(UIDTools.base10ToBase32String(sequence), SEQUENCE_COUNTER_LEN));
        final long clockCallback = this.clockCallbackCounter.get() & SEQUENCE_BIT_15;
        stringBuilder.append(UIDTools.appendPrefixWithFixedLength(UIDTools.base10ToBase32String(clockCallback), CLOCK_CALLBACK_COUNTER_LEN));
        return stringBuilder.toString();
    }

    public static UID getInstance(final Type type) {
        return UniqueIDHolder.get(type);
    }

    static class UniqueIDHolder {

        static final ConcurrentMap<Type, UID> CACHE = new ConcurrentHashMap<Type, UID>();

        public static UID get(final Type type) {
            return UniqueIDHolder.CACHE.get(type);
        }

        static {
            CACHE.put(Type.IP, new UniqueID(Type.IP));
            CACHE.put(Type.MAC, new UniqueID(Type.MAC));
        }
    }

    public enum Type {
        MAC, IP
    }
}
