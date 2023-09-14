package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class DLT645Encoder extends MessageToByteEncoder<DLT645Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DLT645Message dlt645Message, ByteBuf byteBuf) throws Exception {
        log.info("消息出站 before encode  remoteAddress:{} msg:{}", ctx.channel().remoteAddress(), dlt645Message);
        //数据帧
        byte[] dataFarme = new byte[dlt645Message.getData().length + 12];
        dataFarme[0] = Constant.START_OF_FRAME;
        byte[] address = ByteBufUtil.decodeHexDump(dlt645Message.getAddress());
        //地址域
        for (int i = 0; i < 6; i++) {
            dataFarme[1 + i] = address[5 - i];
        }
        dataFarme[7] = Constant.START_OF_FRAME;
        //控制码
        dataFarme[8] = dlt645Message.getControlCode();
        //数据长度
        dataFarme[9] = (byte) dlt645Message.getData().length;
        //数据域
        for (int i = 0; i < dlt645Message.getData().length; i++) {
            dataFarme[10 + i] = (byte) (dlt645Message.getData()[i] + Constant.DIFF_VALUE);
        }
        int cs = 0;
        //计算校验码
        for (int i = 0; i < dataFarme.length - 2; i++) {
            cs = cs + Byte.toUnsignedInt(dataFarme[i]) % 256;
        }
        dataFarme[dataFarme.length - 2] = (byte) cs;
        dataFarme[dataFarme.length - 1] = Constant.END_OF_FRAME;
        //前导字节四个0xfe
        for (int i = 0; i < 4; i++) {
            byteBuf.writeByte(Constant.FRAME_PREAMBLE);
        }
        byteBuf.writeBytes(dataFarme);
        log.info("消息出站 after encode:{}", ByteBufUtil.hexDump(byteBuf));
    }

}
