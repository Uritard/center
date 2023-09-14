package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class DLT645Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        log.info("消息进站 before decode remoteAddress:{} msg: {}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(byteBuf));
        if (byteBuf.readableBytes() < Constant.minLength) {
            log.error("length 小于最小 ,length:{}", byteBuf.readableBytes());
            return;
        }
        // 过滤0xfe
        byte b = byteBuf.readByte();
        while (b == Constant.FRAME_PREAMBLE) {
            b = byteBuf.readByte();
        }
        byteBuf.readerIndex(byteBuf.readerIndex() - 1);
        // 获取数据帧
        byte[] dataFrame = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(dataFrame);
        if (dataFrame[0] != Constant.START_OF_FRAME || dataFrame[7] != Constant.START_OF_FRAME) {
            log.error("帧起始符不正确");
            return;
        }
        //计算校验码
        byte cs = 0;
        for (int i = 0; i < dataFrame.length - 2; i++) {
            cs += Byte.toUnsignedInt(dataFrame[i]) % 256;
        }
        byte realCS = dataFrame[dataFrame.length - 2];
        if (cs != realCS) {
            log.error("校验码检验失败,计算校验码:{} 实际校验码:{}", cs, realCS);
            return;
        }
        List<String> addressList = new ArrayList<>();
        //读取地址
        for (int i = 0; i < 6; i++) {
            byte address = dataFrame[1 + i];
            addressList.add(StringUtils.leftPad(Integer.toHexString(Byte.toUnsignedInt(address)), 2, '0'));
        }
        Collections.reverse(addressList);
        String addressStr = String.join("", addressList);
        // 获取控制码
        byte controlCode = dataFrame[8];
        //数据长度
        int dataLength = Byte.toUnsignedInt(dataFrame[9]);
        log.info("数据长度 {}", dataLength);
        // 获取数据
        byte[] dataArray = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            //读数据需要减0x33
            byte data = (byte) (dataFrame[10 + i] - Constant.DIFF_VALUE);
            dataArray[i] = data;
        }
        DLT645Message message = new DLT645Message();
        message.setControlCode(controlCode);
        message.setAddress(addressStr);
        message.setData(dataArray);
        log.info("消息进站 after decode message :{}", message);
        list.add(message);
    }
}
