//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.yjh.protocol_a.impl;

import java.nio.ByteOrder;
import org.apache.mina.core.buffer.IoBuffer;

public class Packet {
    public static final byte SESSION_TYPE_REQ = 0;
    public static final byte SESSION_TYPE_RESP = 1;
    private static final byte[] SOP = new byte[]{-21, -112};
    private static final byte EOP0 = -21;
    private static final byte EOP1 = -112;
    private static final int SOP_SIZE = 2;
    private static final int SEND_SESSION_ID_SIZE = 8;
    private static final int RECEIVE_SESSION_ID_SIZE = 8;
    private static final int SESSION_TYPE_SIZE = 1;
    private static final int PAYLOAD_LENGTH_SIZE = 4;
    private static final int EOP_SIZE = 2;
    private static final int MINIMUM_PARSING_SIZE = 23;
    private long sendSessionId;
    private long receiveSessionId;
    private byte sessionType;
    private byte[] payload;

    private Packet() {
    }

    private int getPacketLength() {
        return 23 + this.payload.length + 2;
    }

    public long getSendSessionId() {
        return this.sendSessionId;
    }

    public long getReceiveSessionId() {
        return this.receiveSessionId;
    }

    public byte getSessionType() {
        return this.sessionType;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public boolean isResponse() {
        return 1 == this.sessionType;
    }

    public IoBuffer toIoBuffer() {
        IoBuffer buffer = IoBuffer.allocate(this.getPacketLength());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(SOP[0]);
        buffer.put(SOP[1]);
        buffer.putLong(this.sendSessionId);
        buffer.putLong(this.receiveSessionId);
        buffer.put(this.sessionType);
        buffer.putInt(this.payload.length);
        buffer.put(this.payload);
        buffer.put((byte)-21);
        buffer.put((byte)-112);
        buffer.flip();
        return buffer;
    }

    public static Packet pack(long sendSessionId, long receiveSessionId, boolean isResponse, byte[] payload) {
        Packet packet = new Packet();
        packet.sendSessionId = sendSessionId;
        packet.receiveSessionId = receiveSessionId;
        packet.sessionType = (byte)(isResponse ? 1 : 0);
        packet.payload = payload;
        return packet;
    }

    public static Packet tryParse(IoBuffer in) {
        if (in.remaining() < 23) {
            in.reset();
            return null;
        } else {
            for(int i = 0; i < SOP.length; ++i) {
                if (in.get() != SOP[i]) {
                    in.reset();
                    in.skip(i + 1);
                    return null;
                }
            }

            in.order(ByteOrder.LITTLE_ENDIAN);
            long sendSessionId = in.getLong();
            long receiveSessionId = in.getLong();
            byte sessionType = in.get();
            int payloadLength = in.getInt();
            if (in.remaining() < payloadLength + 2) {
                in.reset();
                return null;
            } else {
                byte[] payload = new byte[payloadLength];
                in.get(payload);
                byte eop0 = in.get();
                byte eop1 = in.get();
                if (eop0 == -21 && eop1 == -112) {
                    Packet packet = new Packet();
                    packet.sendSessionId = sendSessionId;
                    packet.receiveSessionId = receiveSessionId;
                    packet.sessionType = sessionType;
                    packet.payload = payload;
                    return packet;
                } else {
                    throw new IllegalArgumentException(String.format("Unexpected ending bytes: 0X%2X 0X%2X", eop0, eop1));
                }
            }
        }
    }

    public String toString() {
        return "Packet{sendSessionId=" + this.sendSessionId + ", receiveSessionId=" + this.receiveSessionId + ", sessionType=" + this.sessionType + '}';
    }
}
