package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.configuration.DynamicTask;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.dao.TMeterDao;
import com.yjh.accessmeter.module.dao.TMeterLogDao;
import com.yjh.accessmeter.module.device.entity.TMeter;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.module.service.TMeterCollectService;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
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

    private final TMeterDao tMeterDao;

    private final TMeterLogDao tMeterLogDao;

    private final PlatformProxy platformProxy;

    private final DynamicTask dynamicTask;

    public DLT645MessgeHandler(TMeterDao tMeterDao, TMeterLogDao tMeterLogDao, PlatformProxy platformProxy, DynamicTask dynamicTask) {
        this.tMeterDao = tMeterDao;
        this.tMeterLogDao = tMeterLogDao;
        this.platformProxy = platformProxy;
        this.dynamicTask = dynamicTask;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        TMeterCollectService.map.put(tMeter.getId(), ctx.channel());
        log.info("电表已经连接 remoteAddress:{},tMeter:{}", ctx.channel().remoteAddress(), tMeter);
        //连接时查询一次电量
        TMeterCollectService.sendCollectMsg(ctx.channel(), tMeter);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        log.info("连接断开:{}", tMeter);
        // 电表数据未删除时 60秒后重连
        TMeter meterData = tMeterDao.select(tMeter);
        if (Objects.nonNull(meterData)) {
            log.info("电表未移除,60秒后重连  tmeter:{}", tMeter);
            TMeterCollectService tMeterCollectService = SpringBeanUtils.getBean(TMeterCollectService.class);
            ctx.channel().eventLoop().schedule(() -> {
                assert tMeterCollectService != null;
                tMeterCollectService.initMeter(meterData);
            }, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 判断控制码返回正确应答  97版本2位数据  07版本4位数据
        int valueNum;
        if (dlt645Message.getControlCode() == Constant.CONTROLL_CODE_ANSWER) {
            valueNum = 2;
        }else if (dlt645Message.getControlCode() == Constant.CONTROLL_CODE_ANSWER_2007){
            valueNum = 4;
        }else {
            log.error("从站返回错误应答控制码！");
            return;
        }
        byte[] data = dlt645Message.getData();
        // 数据域前两位是数据类型
        byte[] dataType = new byte[valueNum];
        System.arraycopy(data, 0, dataType, 0, valueNum);
        String dataTypeStr = ByteUtils.toHexString(dataType);
        log.info("dataType:{} ", dataTypeStr);
        // 解析值
        byte[] dataValue = new byte[data.length - valueNum];
        System.arraycopy(dlt645Message.getData(), valueNum, dataValue, 0, data.length - valueNum);
        ArrayUtils.reverse(dataValue);
        String hexString = ByteBufUtil.hexDump(dataValue);
        long powerTotal = Long.parseLong(hexString, 10);
        log.info("dataValue hex:{} value:{}", hexString, powerTotal);
        String powerTotalString = String.format("%.2f", powerTotal / 100.0);
        //数据入库
        tMeter.setCollectPowerTime(new Date());
        if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL_2007)) {
            log.info("采集到--正向有功--电能:{}", powerTotalString);
            tMeter.setTotalPositivePower(powerTotalString);
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
            //查询上一次的
            TMeter lastMeter = tMeterDao.selectByPrimaryKey(tMeter.getId());
            // 记录电表历史
            Float value = 0f;
            try {
                value = Float.parseFloat(tMeter.getTotalPositivePower()) - Float.parseFloat(lastMeter.getTotalPositivePower());
                if (value < 0) {
                    value = 0f;
                }
            } catch (Exception e) {
                log.info("计算电表差值出错！", e);
            }
            tMeter.setTotalPositivePowerDifferenceValue(String.valueOf(value));
            platformProxy.uploadMeterInfo(tMeter);
        } else if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL_2007)) {
            log.info("采集到--正向无功--电能:{}", powerTotalString);
            tMeter.setTotalPositiveReactivePower(powerTotalString);
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
        } else if (Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL_2007)) {
            log.info("采集到--反向无功--电能:{}", powerTotalString);
            tMeter.setTotalNegativePositivePower(powerTotalString);
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
        }else {
            log.info("数据类型未定义: {} ", dataType);
            return;
        }
        tMeterDao.updateData(tMeter);
        log.info("tMeter {}", tMeter);
        String key = tMeter.getIp() + tMeter.getPort();
        dynamicTask.startDelay(key, () -> tMeterLogDao.insert(ctx.channel().attr(Constant.tMeterAttributeKey).get()), 3 * 1000);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("发生异常", cause);
    }

}
