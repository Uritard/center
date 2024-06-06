package com.yjh.platform.algorithm;

import com.alibaba.fastjson2.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * @author huyuhang
 * <p>
 * 算法平台交互
 */
@Component
@Slf4j
public class AlgorithmService {

    @Autowired
    private ApplicationProperties applicationProperties;

    /**
     *
     * @param alarm 告警具体内容
     */
    public void pushAlarmMsg(Alarm alarm) {
        pushAlarmMsg(Collections.singletonList(alarm));
    }

    /**
     *
     * @param alarmList 告警具体内容
     */
    public void pushAlarmMsg(List<Alarm> alarmList) {
        if (applicationProperties.getManagerAlgorithmConfig().isEnable()) {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            xmlBaseModel.setType("312");
            for (Alarm alarm : alarmList) {
                List<Map<String, Object>> items = new ArrayList<>();
                Map<String, Object> item = getBasicConfig(alarm);
                item.put("pic_defect", alarm.getPic_defect());
                item.put("pic_different", alarm.getPic_different());
                item.put("pic_diff_base", alarm.getPic_diff_base());
                item.put("defect", alarm.getDefect().size());
                item.put("different", alarm.getDifferent().size());
                JSONObject alarms = new JSONObject();
                alarms.put("defect", alarm.getDefect());
                alarms.put("different", alarm.getDifferent());
                item.put("alarms", alarms.toJSONString());
                items.add(item);
                xmlBaseModel.setItems(items);
            }
            Constant.otherObjServer(xmlBaseModel, Constant.TCP_CLOUD_URL);
        }
    }

    /**
     * 发送正常样本
     * @param alarm  正常样本
     */
    public void pushNormalMsg(Alarm alarm) {
        if (applicationProperties.getManagerAlgorithmConfig().isEnable()) {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            xmlBaseModel.setType("68");
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = getBasicConfig(alarm);
            items.add(item);
            xmlBaseModel.setItems(items);
            Constant.otherObjServer(xmlBaseModel, Constant.TCP_CLOUD_URL);
        }
    }

    public Map<String, Object> getBasicConfig(Alarm alarm) {
        Map<String, Object> item = new HashMap<>(16);
        item.put("province_name", applicationProperties.getManagerAlgorithmConfig().getProvinceName());
        item.put("city_name", applicationProperties.getManagerAlgorithmConfig().getCityName());
        item.put("section_id", applicationProperties.getManagerAlgorithmConfig().getSectionId());
        item.put("section_name", applicationProperties.getManagerAlgorithmConfig().getSectionName());
        item.put("volt_level", applicationProperties.getManagerAlgorithmConfig().getVoltLevel());
        item.put("station_name", applicationProperties.getManagerAlgorithmConfig().getStationName());
        item.put("bay_name", alarm.getBay_name());
        item.put("main_device_name", alarm.getDevice_name());
        item.put("device_name", alarm.getPoint_name());
        item.put("time", alarm.getTime());
        item.put("pic_width", alarm.getPic_width());
        item.put("pic_height", alarm.getPic_height());
        item.put("pic_raw", alarm.getPic_raw());
        return item;
    }

}
