package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.ValueUtil;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TIotDeviceData;
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

import java.util.*;

/**
 * <功能描述> 上级系统接收巡视主机消息
 *
 * @author
 * @date
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class IotDeviceHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统接收到物联环控设备信息数据 xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
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
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.AtomicSessionId.incrementAndGet(), sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        List<TIotDeviceData> deviceDataList = tStdDeviceMapper.selectIotDeviceByEdgeCode(sendCode);
        List<Long> deleteDeviceDataList = new ArrayList<>();
        List<Long> deletePointDataList = new ArrayList<>();
        List<TIotDeviceData> insertDeviceList = new ArrayList<>();
        List<TIotDeviceData> updateDeviceList = new ArrayList<>();
        List<TIotDeviceData> insertDevicePointList = new ArrayList<>();
        List<TIotDeviceData> allDevicePointList = new ArrayList<>();
        List<TStdRegion> regionList = tStdRegionDao.selectByRegionCodeAndState(sendCode,null);
        for (Map<String, Object> item : xmlBaseModel.getItems()) {
            Long upRegionId = isContains(regionList, ValueUtil.Object2String(item.get("upRegionId"), "-1"));
            if (upRegionId == null) {
                continue;
            }
            TIotDeviceData data = new TIotDeviceData();
            TIotDeviceData oldData = dataDeviceContains(deviceDataList, MapUtils.getLong(item, "iotDeviceId"));
            if (oldData == null) {
                insertDevicePointList.add(data);
                if (!insertDataContains(insertDeviceList,MapUtils.getLong(item,"iotDeviceId"))){
                    insertDeviceList.add(data);
                }
            } else {
                if (dataPointContains(deviceDataList, MapUtils.getLong(item, "pointId")) == null){
                    insertDevicePointList.add(data);
                }
                data.setId(oldData.getId());
                data.setPointId(oldData.getPointId());
                updateDeviceList.add(data);
            }
            data.setEdgeCode(sendCode);
            data.setPointId(MapUtils.getLong(item,"pointId"));
            data.setPointName(MapUtils.getString(item,"pointName"));
            data.setOriginId(MapUtils.getLong(item,"iotDeviceId"));
            data.setIotDeviceName(MapUtils.getString(item,"iotDeviceName"));
            data.setIp(MapUtils.getString(item,"ip"));
            data.setPort(MapUtils.getInteger(item,"port"));
            data.setAddress(MapUtils.getString(item,"address"));
            data.setUpRegionId(upRegionId);
            data.setCreateTime(new Date());
            data.setChannelNum(MapUtils.getString(item,"channelNum"));
            data.setIotDeviceType(MapUtils.getInteger(item,"iotDeviceType"));
            data.setMeterType(MapUtils.getInteger(item,"meterType"));
            data.setUpRegionName(MapUtils.getString(item,"upRegionName"));
            data.setCreatePerson("10001");
            data.setUpdatePerson("10001");
            data.setControllable(MapUtils.getInteger(item,"controllable"));
            data.setExtend(MapUtils.getString(item,"extend"));
            data.setUnit(MapUtils.getString(item,"unit"));
            allDevicePointList.add(data);
        }
        if (!insertDeviceList.isEmpty()){
            tStdDeviceMapper.batchInsertIotDevice(insertDeviceList);
            if (!insertDevicePointList.isEmpty()){
                insertDevicePointList.forEach(point ->{
                    insertDeviceList.forEach(device ->{
                        if (device.getOriginId().equals(point.getOriginId())){
                            point.setId(device.getId());
                        }
                    });
                });
                tStdDeviceMapper.batchInsertIotDevicePoint(insertDevicePointList);
            }
        } else {
            if (!insertDevicePointList.isEmpty()){
                tStdDeviceMapper.batchInsertIotDevicePoint(insertDevicePointList);
            }
        }

        if (!updateDeviceList.isEmpty()){
            updateDeviceList.forEach(tIotDeviceData -> {
                tStdDeviceMapper.updateIotDevice(tIotDeviceData);
                tStdDeviceMapper.updateIotDevicePoint(tIotDeviceData);
            });
        }

        deleteDeviceDataList = dealDelete(deviceDataList,1,allDevicePointList);
        deletePointDataList = dealDelete(deviceDataList,2,allDevicePointList);
        if (!deleteDeviceDataList.isEmpty()){
            tStdDeviceMapper.deleteIotDevice(deleteDeviceDataList);
        }
        if (!deletePointDataList.isEmpty()){
            tStdDeviceMapper.deleteIotDevicePoint(deletePointDataList);
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

    private TIotDeviceData dataDeviceContains(List<TIotDeviceData> dataList, Long iotDeviceId) {
        for (TIotDeviceData data : dataList) {
            if (data.getOriginId().equals(iotDeviceId)) {
                return data;
            }
        }
        return null;
    }

    private TIotDeviceData dataPointContains(List<TIotDeviceData> dataList, Long pointId) {
        for (TIotDeviceData data : dataList) {
            if (data.getPointOriginId() != null && data.getPointOriginId().equals(pointId)) {
                return data;
            }
        }
        return null;
    }

    private boolean insertDataContains(List<TIotDeviceData> dataList, Long iotDeviceId) {
        if (dataList.size() <= 0){
            return false;
        }
        for (TIotDeviceData data : dataList) {
            if (data.getOriginId().equals(iotDeviceId)) {
                return true;
            }
        }
        return false;
    }

    private List<Long> dealDelete(List<TIotDeviceData> deviceDataList, int type,List<TIotDeviceData> allDeviceDataList){
        //type  1-设备 2-point
        List<Long> reList = new ArrayList<>();
        for (TIotDeviceData data: deviceDataList) {
            if(type == 1 && !allDeviceDataList.stream().anyMatch(item -> item.getOriginId().equals(data.getOriginId()))){
              reList.add(data.getId());
            }
            if(type == 2 && !allDeviceDataList.stream().anyMatch(item -> item.getPointId().equals(data.getPointOriginId()) )){
                reList.add(data.getPointId());
            }
        }
        return reList;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.IOT_DEVICE.getCode(), this);
    }
}
