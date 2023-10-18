package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.dao.TMeterDao;
import com.yjh.accessmeter.module.dao.TMeterLogDao;
import com.yjh.accessmeter.module.device.entity.TMeter;
import com.yjh.accessmeter.module.service.TMeterCollectService;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Component
public class DLT645MessgeHandler extends ChannelInboundHandlerAdapter {

    private TMeterDao tMeterDao;

    private TMeterLogDao tMeterLogDao;

    public DLT645MessgeHandler(TMeterDao tMeterDao, TMeterLogDao tMeterLogDao) {
        this.tMeterDao = tMeterDao;
        this.tMeterLogDao = tMeterLogDao;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        TMeterCollectService.map.put(tMeter.getId(), ctx.channel());
        log.info("电表已经连接 remoteAddress:{},tMeter:{}", ctx.channel().remoteAddress(), tMeter);
        //连接时查询一次电量
        try {
            DLT645Message dlt645Message = new DLT645Message();
            dlt645Message.setControlCode(Constant.CONTROLL_CODE_REQUEST);
            dlt645Message.setAddress(tMeter.getAddress());
            dlt645Message.setData(Constant.DATA_TYPE_POSITVICE_POWER_TOTAL);
            ctx.writeAndFlush(dlt645Message);
            Thread.sleep(500);
            dlt645Message.setData(Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL);
            ctx.writeAndFlush(dlt645Message);
            Thread.sleep(500);
            dlt645Message.setData(Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL);
            ctx.writeAndFlush(dlt645Message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        log.info("连接断开:{}", tMeter);
        // 电表数据未删除时 60秒后重连
        TMeter meterData = tMeterDao.selectByPrimaryKey(tMeter.getId());
        if (Objects.nonNull(meterData)) {
            log.info("电表未移除,60秒后重连  tmeter:{}", tMeter);
            TMeterCollectService tMeterCollectService = SpringBeanUtils.getBean(TMeterCollectService.class);
            ctx.channel().eventLoop().schedule(() -> tMeterCollectService.initMeter(meterData), 60, TimeUnit.SECONDS);
        }
    }

    @Override
    @Transactional
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("msg:{}", msg);
        DLT645Message dlt645Message = (DLT645Message) msg;
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        // 电表地址不同更新电表地址
        if (!dlt645Message.getAddress().equalsIgnoreCase(tMeter.getAddress())) {
            tMeter.setAddress(dlt645Message.getAddress());
            tMeterDao.updateAddress(tMeter);
            log.info("更新电表地址 tMeter:{}", tMeter);
        }
        log.info("控制码 :{}", ByteUtils.toHexString(new byte[]{dlt645Message.getControlCode()}));
        // 判断控制码返回正确应答
        if (dlt645Message.getControlCode() == Constant.CONTROLL_CODE_ANSWER) {
            byte[] data = dlt645Message.getData();
            // 数据域前两位是数据类型
            byte[] dataType = new byte[2];
            System.arraycopy(data, 0, dataType, 0, 2);
            String dataTypeStr = ByteUtils.toHexString(dataType);
            log.info("dataType:{} ", dataTypeStr);
            // 判断数据类型是正向有功总电能
            if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL)
                    || Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL)
                    || Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL)) {
                // 解析值
                byte[] dataValue = new byte[data.length - 2];
                System.arraycopy(dlt645Message.getData(), 2, dataValue, 0, data.length - 2);
                ArrayUtils.reverse(dataValue);
                String hexString = ByteBufUtil.hexDump(dataValue);
                long powerTotal = Long.parseLong(hexString, 10);
                log.info("dataValue hex:{} value:{}", hexString, powerTotal);
                String powerTotalString = String.format("%.2f", powerTotal / 100.0);
                //数据入库
                if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL)) {
                    log.info("采集到正向有功电能:{}", powerTotalString);
                    tMeter.setTotalPositivePower(powerTotalString);
                } else if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL)) {
                    log.info("采集到正向无功电能:{}", powerTotalString);
                    tMeter.setTotalPositiveReactivePower(powerTotalString);
                } else if (Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL)) {
                    log.info("采集到反向无功电能:{}", powerTotalString);
                    tMeter.setTotalNegativeReactivePower(powerTotalString);
                }
                log.info("tMeter {}", tMeter);
                tMeterDao.updateData(tMeter);
                // 记录电表历史
                tMeterLogDao.insert(tMeter);
            } else {
                log.error("数据类型不是正向有功/正向无功/反向无功总电能, dataType:{}", ByteBufUtil.hexDump(dataType));
            }
        } else {
            log.error("从站未返回正常应答, 控制码:{}", ByteUtils.toHexString(new byte[]{dlt645Message.getControlCode()}));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("发生异常", cause);
    }

}
