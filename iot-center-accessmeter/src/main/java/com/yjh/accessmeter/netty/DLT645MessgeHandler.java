package com.yjh.accessmeter.netty;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.configuration.DynamicTask;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.dao.TMeterDao;
import com.yjh.accessmeter.module.dao.TMeterLogDao;
import com.yjh.accessmeter.module.device.entity.TMeter;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.module.service.TMeterCollectService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    private final SendMeterCodeManager sendMeterCodeManager;

    public DLT645MessgeHandler(TMeterDao tMeterDao, TMeterLogDao tMeterLogDao, PlatformProxy platformProxy,
                               DynamicTask dynamicTask, SendMeterCodeManager sendMeterCodeManager) {
        this.tMeterDao = tMeterDao;
        this.tMeterLogDao = tMeterLogDao;
        this.platformProxy = platformProxy;
        this.dynamicTask = dynamicTask;
        this.sendMeterCodeManager = sendMeterCodeManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        TMeter tMeter = ctx.channel().attr(Constant.tMeterAttributeKey).get();
        TMeterCollectService.map.put(tMeter.getId(), ctx.channel());
        log.info("电表已经连接 remoteAddress:{},tMeter:{}", ctx.channel().remoteAddress(), tMeter);
        //连接时查询一次电量
        sendMeterCodeManager.sendCollectMsg(ctx.channel(), tMeter);
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
        // 数据域前两位是数据类型
        byte[] dataType = dlt645Message.getDataType();
        String powerTotalString = dlt645Message.getValue();
        //数据入库
        tMeter.setCollectPowerTime(new Date());
        //查询上一次的
        TMeter lastMeter = tMeterDao.selectByPrimaryKey(tMeter.getId());
        if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_POSITVICE_POWER_TOTAL_2007)) {
            log.info("采集到--正向有功--电能:{}", powerTotalString);
            tMeter.setTotalPositivePower(powerTotalString);
            Float value = getPowerDifferenceValue(tMeter.getTotalPositivePower(), lastMeter.getTotalPositivePower());
            tMeter.setTotalPositivePowerDifferenceValue(String.valueOf(value));
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
            platformProxy.uploadMeterInfo(tMeter);
        } else if (Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL_2007)) {
            log.info("采集到--正向无功--电能:{}", powerTotalString);
            tMeter.setTotalPositiveReactivePower(powerTotalString);
            Float value = getPowerDifferenceValue(tMeter.getTotalPositiveReactivePower(), lastMeter.getTotalPositiveReactivePower());
            tMeter.setTotalPositiveReactivePowerDifferenceValue(String.valueOf(value));
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
        } else if (Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL)
                || Arrays.equals(dataType, Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL_2007)) {
            log.info("采集到--反向无功--电能:{}", powerTotalString);
            tMeter.setTotalNegativePositivePower(powerTotalString);
            Float value = getPowerDifferenceValue(tMeter.getTotalNegativePositivePower(), lastMeter.getTotalNegativePositivePower());
            tMeter.setTotalNegativePositivePowerDifferenceValue(String.valueOf(value));
            ctx.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
        }else {
            log.info("数据类型未定义: {} ", dataType);
            return;
        }
        tMeterDao.updateData(tMeter);
        log.info("tMeter {}", tMeter);
        String key = tMeter.getIp() + tMeter.getPort();
        dynamicTask.startDelay(key, () -> tMeterLogDao.insert(ctx.channel().attr(Constant.tMeterAttributeKey).get()), 12 * 1000);
    }

    private Float getPowerDifferenceValue(String value, String lastValue) {
        // 记录电表历史
        float res = 0f;
        try {
            res = Float.parseFloat(value) - Float.parseFloat(lastValue);
            if (res < 0) {
                res = 0f;
            }
        } catch (Exception e) {
            log.info("计算电表差值出错！", e);
        }
        return res;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("发生异常", cause);
    }

}
