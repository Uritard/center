package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.ValueUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TMeter;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <功能描述> 上级系统接收巡视主机消息
 *
 * @author
 * @date
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class MeterHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统接收到电表数据 xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        List<TMeter> meterList = tStdDeviceMapper.selectAllMeterByEdgeCode(sendCode);
        List<TMeter> updateMeterList = new ArrayList<>();
        List<TMeter> addMeterList = new ArrayList<>();
        List<TMeter> meterLogList = new ArrayList<>();
        List<TStdRegion> regionList = tStdRegionDao.selectAll();
        for (Map<String,Object> item: xmlBaseModel.getItems()) {
            Long upRegionId = isContains(regionList,ValueUtil.Object2String(item.get("upRegionId"),"-1"));
            if (upRegionId == null){
                continue;
            }
            TMeter tMeter = meterContains(meterList,MapUtils.getString(item,"address"));

            if (tMeter != null){
                //计算差值
                tMeter.setTotalPositivePower(MapUtils.getString(item,"totalPositivePower"));
                tMeter.setTotalPositiveReactivePower(MapUtils.getString(item,"totalPositiveReactivePower"));
                tMeter.setTotalNegativePositivePower(MapUtils.getString(item,"totalNegativePositivePower"));
                tMeter.setCollectPowerTime(DateTimeUtil.parse(MapUtils.getString(item,"collectPowerTime")));
                tMeter.setTotalPositivePowerDifferenceValue(MapUtils.getString(item,"totalPositivePowerDifferenceValue"));
                tMeter.setMagnificationCoefficient(MapUtils.getIntValue(item,"magnificationCoefficient", 1));
                updateMeterList.add(tMeter);
            } else {
                tMeter = new TMeter();
                tMeter.setName(MapUtils.getString(item,"name"));
                tMeter.setIp(MapUtils.getString(item,"ip"));
                tMeter.setPort(MapUtils.getInteger(item,"port"));
                tMeter.setAddress(MapUtils.getString(item,"address"));
                tMeter.setProtocol(MapUtils.getString(item,"protocol"));
                tMeter.setUpRegionId(upRegionId);
                tMeter.setEdgeCode(sendCode);
                tMeter.setType(MapUtils.getInteger(item,"type"));
                tMeter.setTotalPositivePower(MapUtils.getString(item,"totalPositivePower"));
                tMeter.setTotalPositiveReactivePower(MapUtils.getString(item,"totalPositiveReactivePower"));
                tMeter.setTotalNegativePositivePower(MapUtils.getString(item,"totalNegativePositivePower"));
                tMeter.setCollectPowerTime(DateTimeUtil.parse(MapUtils.getString(item,"collectPowerTime")));
                tMeter.setTotalPositivePowerDifferenceValue(MapUtils.getString(item,"totalPositivePowerDifferenceValue"));
                tMeter.setMagnificationCoefficient(MapUtils.getIntValue(item,"magnificationCoefficient", 1));
                addMeterList.add(tMeter);
            }
            meterLogList.add(tMeter);
        }
        if (!addMeterList.isEmpty()){
            tStdDeviceMapper.batchInsertMeter(addMeterList);
        }
        if (!updateMeterList.isEmpty()){
            updateMeterList.forEach(tMeter -> {
                tStdDeviceMapper.updateById(tMeter);
            });
        }
        if (!meterLogList.isEmpty()){
            tStdDeviceMapper.batchInsertMeterLog(meterLogList);
        }

    }

    private Long isContains(List<TStdRegion> regionList,String upRegionId){
        for (TStdRegion region: regionList){
            if (upRegionId.equals(region.getOriginRegionId())){
                return region.getRegionId();
            }
        }
        return null;
    }

    private TMeter meterContains(List<TMeter> meterList,String address){
        for (TMeter meter: meterList){
            if (meter.getAddress().equals(address)){
                return meter;
            }
        }
        return null;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.METER_INFO.getCode(), this);
    }
}
