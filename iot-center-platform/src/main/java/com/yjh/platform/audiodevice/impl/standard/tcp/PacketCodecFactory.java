/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.audiodevice.impl.standard.tcp;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

/**
 * @author zilong
 * @date 2022/4/14
 */
public class PacketCodecFactory implements ProtocolCodecFactory {
    private final TcpPacketEncoder encoder = new TcpPacketEncoder();
    private final TcpPacketDecoder decoder = new TcpPacketDecoder();
    private static boolean byteOrderLittleFlag;
    public PacketCodecFactory(boolean byteOrderLittleFlag) {
        PacketCodecFactory.byteOrderLittleFlag = byteOrderLittleFlag;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this.decoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return this.encoder;
    }

    public static class TcpPacketEncoder extends ProtocolEncoderAdapter {

        @Override
        public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
            throw new UnsupportedOperationException("encode not supported yet");
        }
    }

    public static class TcpPacketDecoder extends CumulativeProtocolDecoder {

        @Override
        public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
            //只要本次decode不完，都要reset到此位置
            in.mark();

            Packet packet = Packet.tryParse(in, byteOrderLittleFlag);
            if (packet == null) {
                //继续读
                return false;
            }

            out.write(packet);
            return in.hasRemaining(); //粘包的话，继续decode
        }
    }

}

