package com.yjh.platform.module.patrol.event;

import cn.hutool.core.bean.BeanUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.iot.dao.TIotDeviceDataMapper;
import com.yjh.platform.module.iot.dao.TIotDeviceMapper;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDeviceDataService;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: lqh
 * @Date: 2023/11/07
 */
@Slf4j
@Component
public class MeterInfoHandler {
    /**
     * 正向有功总:2131.17        无功I总:16.07                        无功IV总:226.88
     * total_positive_power     total_positive_reactive_power       total_negative_positive_power
     * 电表的数据结果 对结果进行字符匹配
     */

    private final static String meterKey = "meter:";

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private TCruisePointInstanceDao cruisePointInstanceDao;
    @Resource
    private TIotDeviceDataMapper iotDeviceDataMapper;
    @Resource
    private TIotDeviceMapper iotDeviceMapper;
    @Resource
    private TIotDeviceDataService iotDeviceDataService;

    @EventListener
    public void handleResultEvent(InspectionResultEvent event) {
        try {
            // 巡视结果
            TIotDevicePoint iotDevicePoint = new TIotDevicePoint();
            if (StringUtils.isNotEmpty(event.getResult())) {
                String value = getNumeric(event.getResult());
                if (StringUtils.isEmpty(value)) {
                    return;
                }
                // 根据instanceId查询关联的测点及判断物联设备是否绑定测定
                iotDevicePoint = cruisePointInstanceDao.getBindIotDevicePoint(Long.valueOf(event.getInstanceId()));
                if (iotDevicePoint != null) {
                    handleMeterEndEvent(iotDevicePoint, event);
                }

            }
        }catch (Exception e){
            log.error("处理电表数据出错：{},错误：",event,e);
        }
    }

    public void handleMeterEndEvent(TIotDevicePoint iotDevicePoint, InspectionResultEvent event) {
        // 查询物联设备信息
        TIotDevice iotDevice = iotDeviceMapper.selectById(iotDevicePoint.getIotDeviceId());

        TIotDeviceData iotDeviceData = new TIotDeviceData();
        iotDeviceData.setPointId(iotDevicePoint.getId());
        iotDeviceData.setPointName(iotDevicePoint.getPointName());
        iotDeviceData.setIotDeviceId(iotDevicePoint.getIotDeviceId());
        iotDeviceData.setIotDeviceName(iotDevice.getDeviceName());
        iotDeviceData.setValue(getNumeric(event.getResult()));
        iotDeviceData.setUnit(iotDevicePoint.getUnit());
        iotDeviceData.setUpRegionId(iotDevice.getUpRegionId());
        iotDeviceData.setCreateTime(new Date());
        iotDeviceData.setMagnificationCoefficient(iotDevice.getMagnificationCoefficient());
        iotDeviceData.setIotDeviceType(iotDevice.getIotDeviceType());
        iotDeviceDataMapper.insert(iotDeviceData);

        IotDeviceDataEx iotDeviceDataEx = new IotDeviceDataEx();
        BeanUtil.copyProperties(iotDeviceData, iotDeviceDataEx);
        iotDeviceDataEx.setIp(iotDevice.getIp());
        iotDeviceDataEx.setPort(iotDevice.getPort());
        iotDeviceDataEx.setAddress(iotDevice.getAddress());
        iotDeviceDataEx.setMeterType(iotDevice.getMeterType());
        iotDeviceDataEx.setControllable(iotDevice.getControllable());
        iotDeviceDataEx.setChannelNum(iotDevicePoint.getChannelNum());
        List<IotDeviceDataEx> list = new ArrayList<>();
        list.add(iotDeviceDataEx);
        iotDeviceDataService.addToRedis(list);
        iotDeviceDataUpload(list);
    }

    public static String getNumeric(String str) {
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if ((str.charAt(i) >= 48 && str.charAt(i) <= 57) || str.charAt(i) == '.') {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }

    private void iotDeviceDataUpload(List<IotDeviceDataEx> deviceDataList) {
        try {
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> itemList = new ArrayList<>();

            xmlBaseModel.setItems(itemList);
            xmlBaseModel.setType("iotDeviceData");
            xmlBaseModelList.add(xmlBaseModel);

            map.put("list", xmlBaseModelList);
            deviceDataList.forEach(tIotDeviceData -> {
                Map<String, Object> item = new HashMap<>();
                item.put("pointId", tIotDeviceData.getPointId());
                item.put("pointName", tIotDeviceData.getPointName());
                item.put("iotDeviceId", tIotDeviceData.getIotDeviceId());
                item.put("iotDeviceName", tIotDeviceData.getIotDeviceName());
                item.put("ip", tIotDeviceData.getIp());
                item.put("port", tIotDeviceData.getPort());
                item.put("address", tIotDeviceData.getAddress());
                item.put("value", tIotDeviceData.getValue());
                item.put("unit", tIotDeviceData.getUnit());
                item.put("upRegionId", tIotDeviceData.getUpRegionId());
                item.put("createTime", DateTimeUtil.format(tIotDeviceData.getCreateTime()));
                item.put("magnificationCoefficient", tIotDeviceData.getMagnificationCoefficient());
                item.put("channelNum", tIotDeviceData.getChannelNum());
                item.put("iotDeviceType", tIotDeviceData.getIotDeviceType());
                item.put("meterType", tIotDeviceData.getMeterType());
                item.put("upRegionName", tIotDeviceData.getUpRegionName());
                item.put("controllable", tIotDeviceData.getControllable());
                itemList.add(item);
            });
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            log.info("上报环控数据出错！", e);
        }
    }
}
