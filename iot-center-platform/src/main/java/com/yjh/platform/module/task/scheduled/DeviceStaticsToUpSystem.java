package com.yjh.platform.module.task.scheduled;

import com.yjh.platform.common.Constant;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.task.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 可靠性指标上报上级系统
 *
 * @author 丫C
 * @date 2022/5/31
 */


@Service
public class DeviceStaticsToUpSystem {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StatisticsDao statisticsDao;

    @Autowired
    private StatisticsService statisticsService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Logger log = LoggerFactory.getLogger(DeviceStaticsToUpSystem.class);

    /**
     * 可靠性指标定时上报上级系统
     **/


    @Scheduled(cron = "0 0 0 * * ?")
    public void resultToUpSystem() {
        List<Map<String, Object>> infoMaps = new ArrayList<>();
        // 获取摄像机，机器人，无人机的信息
        List<Map<String, String>> robotList = statisticsDao.selectRobot();
        List<Map<String, String>> cameraList = statisticsDao.selectCamera();
        List<Map<String, String>> droneList = statisticsDao.selectDrone();
        statisticsService.selectStatisticsRobot(null,"robot");
        statisticsService.selectStatisticsRobot(null,"");
        statisticsService.countCamera(null);
        robotList.addAll(cameraList);
        robotList.addAll(droneList);
        robotList.forEach(device -> {
            if (device.containsKey("robotId")) {
                packageInfo("robotId", device, infoMaps);
            }
            if (device.containsKey("cameraId")) {
                packageInfo("cameraId", device, infoMaps);
            }
        });
        sendInfoToUpSystem(infoMaps);
    }

    private void sendInfoToUpSystem(List<Map<String, Object>> infoMaps) {
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        xmlBaseModel.setType("81");
        xmlBaseModel.setCode("");
        xmlBaseModel.setCommand("");
        xmlBaseModel.setItems(infoMaps);
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> map = new HashMap<>();
        map.put("list", list);
        log.info("The information to be reported one level up is==={}", map);
        try {
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void packageInfo(String key, Map<String, String> device, List<Map<String, Object>> infoMaps) {
        Map deviceStaticsInfo = redisTemplate.opsForHash().entries("deviceStaticsInfo:" + key + ":" + device.get(key));
        for (int type = 1; type < 7; type++) {
            Map<String, Object> infoMap = new HashMap<>(16);
            infoMap.put("patroldevice_code", device.get("patrolDeviceCode"));
            infoMap.put("patroldevice_name", device.get("patrolDeviceName"));
            infoMap.put("commission_time", Optional.ofNullable(device.get("commissionTime")).orElse(""));
            infoMap.put("report_time", simpleDateFormat.format(new Date()));
            infoMap.put("type", type);
            switch (type) {
                case 1:
                    // 累积在线时长总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("duration"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("duration"));
                    }
                    infoMap.put("value_unit", "时");
                    infoMap.put("unit", "分");
                    break;
                case 2:
                    // 累积离线次数总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("offLineCount"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("offLineCount"));
                    }
                    infoMap.put("value_unit", "次");
                    infoMap.put("unit", "次");
                    break;
                case 3:
                    // 累计连续正常运行天数
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("normalDay"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("normalDay"));
                    }
                    infoMap.put("value_unit", "天");
                    infoMap.put("unit", "天");
                    break;
                case 4:
                    // 正常巡检天数
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("commissionDays"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("commissionDays"));
                    }
                    infoMap.put("value_unit", "天");
                    infoMap.put("unit", "天");
                    break;
                case 5:
                    // 巡检出勤率
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("cruisePercent"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("cruisePercent").toString().replace("%",""));
                    }
                    infoMap.put("value_unit", "%");
                    infoMap.put("unit", "%");
                    break;
                case 6:
                    // 录像完整率
                    if (!device.containsKey("cameraId")) {
                        break;
                    }
                    if(StringUtils.isEmpty(deviceStaticsInfo.get("intactPercent"))){
                        infoMap.put("value", "0");
                    }else {
                        infoMap.put("value",deviceStaticsInfo.get("intactPercent").toString().replace("%",""));
                    }
                    infoMap.put("value_unit", "%");
                    infoMap.put("unit", "%");
                    break;
                default:
                    break;
            }
            infoMaps.add(infoMap);
        }
    }
}
