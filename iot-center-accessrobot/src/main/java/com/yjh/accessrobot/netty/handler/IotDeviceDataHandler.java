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
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.proxy.PlatformProxy;
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
public class IotDeviceDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private PlatformProxy platformProxy;

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

        List<TIotDeviceData> deviceDataList = tStdDeviceMapper.selectIotDeviceByEdgeCode(sendCode);
        List<TIotDeviceData> insertDataList = new ArrayList<>();
        List<TStdRegion> regionList = tStdRegionDao.selectByRegionCodeAndState(sendCode,null);
        for (Map<String, Object> item : xmlBaseModel.getItems()) {
            Long upRegionId = isContains(regionList, ValueUtil.Object2String(item.get("upRegionId"), "-1"));
            if (upRegionId == null) {
                continue;
            }
            TIotDeviceData data = new TIotDeviceData();
            TIotDeviceData oldData = dataContains(deviceDataList, MapUtils.getLong(item, "iotDeviceId"),MapUtils.getLong(item, "pointId"));
            if (oldData == null) {
                log.info("根据数据查找不到对应设备和测点！{}",item);
                continue;
            } else {
                data.setId(oldData.getId());
                data.setPointId(oldData.getPointId());
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
            data.setChannelNum(MapUtils.getString(item,"channelNum"));
            data.setMagnificationCoefficient(MapUtils.getInteger(item, "magnificationCoefficient"));
            data.setIotDeviceType(MapUtils.getInteger(item,"iotDeviceType"));
            data.setIotDeviceId(data.getId());
            data.setUpRegionName(MapUtils.getString(item,"upRegionName"));
            data.setCreatePerson("10001");
            data.setUpdatePerson("10001");
            data.setControllable(MapUtils.getInteger(item,"controllable"));
            insertDataList.add(data);
        }
        if (!insertDataList.isEmpty()) {
            tStdDeviceMapper.batchInsertIotDeviceData(insertDataList);
        }
        platformProxy.uploadToRedis(insertDataList);
    }

    private Long isContains(List<TStdRegion> regionList, String upRegionId) {
        for (TStdRegion region : regionList) {
            if (upRegionId.equals(region.getOriginRegionId())) {
                return region.getRegionId();
            }
        }
        return null;
    }

    private TIotDeviceData dataContains(List<TIotDeviceData> dataList, Long iotDeviceID,Long pointId) {
        for (TIotDeviceData data : dataList) {
            if (data.getOriginId().equals(iotDeviceID) && data.getPointOriginId() != null && data.getPointOriginId().equals(pointId)) {
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
