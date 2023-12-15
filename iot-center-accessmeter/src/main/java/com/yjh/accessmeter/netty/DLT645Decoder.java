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
        log.info("消息进站 before decode remoteAddress:{} msg: {}" , ctx.channel().remoteAddress(), ByteBufUtil.hexDump(byteBuf));
        if (byteBuf.readableBytes() < Constant.MIN_LENGTH) {
            log.error("length 小于最小 ,length:{}" , byteBuf.readableBytes());
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }
        byteBuf.markReaderIndex();
        if (byteBuf.readByte() != Constant.START_OF_FRAME) {
            byteBuf.skipBytes(1);
            return;
        }
        List<String> addressList = new ArrayList<>();
        //读取地址
        for (int i = 0; i < Constant.ADDRESS; i++) {
            byte address = byteBuf.readByte();
            addressList.add(StringUtils.leftPad(Integer.toHexString(Byte.toUnsignedInt(address)), 2, '0'));
        }
        Collections.reverse(addressList);
        String addressStr = String.join("" , addressList);

        if (byteBuf.readByte() != Constant.START_OF_FRAME) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            log.error("结束符不正确{}" , Byte.toUnsignedInt(byteBuf.readByte()));
            return;
        }
        // 获取控制码
        byte controlCode = byteBuf.readByte();
        //数据长度
        int dataLength = Byte.toUnsignedInt(byteBuf.readByte());
        log.info("数据长度 {}" , dataLength);
        byteBuf.resetReaderIndex();
        if (byteBuf.readableBytes() < dataLength + Constant.MIN_LENGTH) {
            log.error("完整报文length:{}, 报文缺失length:{}" , dataLength + Constant.MIN_LENGTH, byteBuf.readableBytes());
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }
        byte[] dataFrame = new byte[dataLength + Constant.MIN_LENGTH];
        byteBuf.readBytes(dataFrame);
        //计算校验码
        byte cs = 0;
        int end = 2;
        for (int i = 0; i < dataFrame.length - end; i++) {
            cs += Byte.toUnsignedInt(dataFrame[i]) % 256;
        }
        byte realCs = dataFrame[dataFrame.length - 2];
        if (cs != realCs) {
            log.error("校验码检验失败,计算校验码:{} 实际校验码:{}" , cs, realCs);
            return;
        }
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
        log.info("消息进站 after decode message :{}" , message);
        list.add(message);
    }
}
