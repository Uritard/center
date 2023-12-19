package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.ValueUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TIotDeviceData;
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
import java.util.Objects;

/**
 * <功能描述> 上级系统接收巡视主机消息
 *
 * @author
 * @date
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class IotDeviceDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统接收到物联环控设备数据 xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
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

        List<TIotDeviceData> deviceDataList = tStdDeviceMapper.selectByOriginId(xmlBaseModel.getItems());
        List<TIotDeviceData> insertDataList = new ArrayList<>();
        List<TIotDeviceData> insertDeviceList = new ArrayList<>();
        List<TStdRegion> regionList = tStdRegionDao.selectAll();
        for (Map<String, Object> item : xmlBaseModel.getItems()) {
            Long upRegionId = isContains(regionList, ValueUtil.Object2String(item.get("upRegionId"), "-1"));
            if (upRegionId == null) {
                continue;
            }
            TIotDeviceData data = dataContains(deviceDataList, MapUtils.getLong(item, "iotDeviceId"));
            if (data == null) {
                data = new TIotDeviceData();
                insertDeviceList.add(data);
            }
            data.setEdgeCode(sendCode);
            data.setPointName(MapUtils.getString(item,"pointName"));
            data.setOriginId(MapUtils.getLong(item,"iotDeviceId"));
            data.setIotDeviceName(MapUtils.getString(item,"iotDeviceName"));
            data.setIp(MapUtils.getString(item,"ip"));
            data.setPort(MapUtils.getInteger(item,"port"));
            data.setAddress(MapUtils.getString(item,"address"));
            data.setValue(MapUtils.getString(item,"value"));
            data.setUnit(MapUtils.getString(item,"unit"));
            data.setUpRegionId(upRegionId);
            data.setCreateTime(DateTimeUtil.parse(MapUtils.getString(item,"createTime")));
            data.setChannelNum(MapUtils.getInteger(item,"channelNum"));
            data.setIotDeviceType(MapUtils.getInteger(item,"iotDeviceType"));
            data.setIotDeviceId(MapUtils.getLong(item,"deviceId"));
            data.setUpRegionName(MapUtils.getString(item,"upRegionName"));
            data.setCreatePerson("10001");
            data.setUpdatePerson("10001");
            insertDataList.add(data);
        }
        if (!insertDeviceList.isEmpty()) {
            tStdDeviceMapper.batchInsertIotDevice(insertDeviceList);
            tStdDeviceMapper.batchInsertIotDevicePoint(insertDeviceList);
        }
        if (!insertDataList.isEmpty()) {
            tStdDeviceMapper.batchInsertIotDeviceData(insertDataList);
        }

    }

    private Long isContains(List<TStdRegion> regionList, String upRegionId) {
        for (TStdRegion region : regionList) {
            if (upRegionId.equals(region.getOriginRegionId())) {
                return region.getRegionId();
            }
        }
        return null;
    }

    private TIotDeviceData dataContains(List<TIotDeviceData> dataList, Long address) {
        for (TIotDeviceData data : dataList) {
            if (data.getOriginId().equals(address)) {
                return data;
            }
        }
        return null;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.IOT_DEVICE_DATA.getCode(), this);
    }
}
