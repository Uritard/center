package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.utils.ByteUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.netty.entiy.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


/**
 * 国网 A 接口解码
 *
 * @author fei23
 * @date 2022-5-8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class StateGridADecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(StateGridADecoder.class);

    private static final byte[] TAG = {(byte) 0xEB, (byte) 0x90};

    private byte[] tmpBuff;
    private int headLength = 2 + 8 + 8 + 1 + 4;
    private Message message;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.info("+++++++++++++++++ Received the robot decode request +++++++++++++++++");

        //将二进制字节码转为对象
        if (in.readableBytes() >= headLength) {
            if (message == null) {
                log.info("+++++++++++++++++ 开始一个全新的消息 +++++++++++++++++");
                tmpBuff = new byte[headLength];
                byte stag = in.readByte();
                if (stag != TAG[0]) {
                    //　开头不对，抛弃
                    log.error("错误的开头: {}", ByteUtil.toHexString(new byte[]{stag}));
                    return;
                }
                byte stag1 = in.readByte();
                if (stag1 != TAG[1]) {
                    //　开头不对，抛弃
                    log.error("错误的开头: {}", ByteUtil.toHexString(new byte[]{stag, stag1}));
                    return;
                }
                tmpBuff[0] = stag;
                tmpBuff[1] = stag1;

                long sendSerNo = in.readLongLE();
                long receiveSerNo = in.readLongLE();
                byte b = in.readByte();
                int len = in.readIntLE();

                System.arraycopy(PlatformPacketUtil.long2Bytes(sendSerNo), 0, tmpBuff, 2, 8);
                System.arraycopy(PlatformPacketUtil.long2Bytes(receiveSerNo), 0, tmpBuff, 10, 8);
                tmpBuff[18] = b;
                System.arraycopy(PlatformPacketUtil.int2Bytes(len), 0, tmpBuff, 19, 4);

                message = new Message(sendSerNo, receiveSerNo, b, len);
            }
            int length = message.getLength();
            if (in.readableBytes() < length + 2) {
                log.info("当前可读数据不够，继续等待");
                return;
            }
            if (length < 0) {
                log.info("读取报文出错，放弃消息: {}", ByteUtil.toHexString(tmpBuff));
                tmpBuff = null;
                message = null;
                return;
            }
            byte[] content = new byte[length];
            in.readBytes(content);
            message.setContent(content);
            // 传递给下一个handler
            out.add(message);
            byte[] stag = new byte[2];
            in.readBytes(stag);
//            log.info("读取完成， 原始报文 head:{},\n\tcontent:{},\n\tend tag: {}", ByteUtil.toHexString(tmpBuff),
//                    ByteUtil.toHexString(content), ByteUtil.toHexString(TAG));
            message = null;
            tmpBuff = null;
        }
    }
}
