package com.yjh.platform.module.task.scheduled;

import com.yjh.commons.DateUtils;
import com.yjh.platform.common.Constant;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.patrol.dao.UPatrolDeviceStaticsDao;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.dao.StatisticsDao;
import com.yjh.platform.module.task.service.StatisticsService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import org.apache.commons.collections4.MapUtils;
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
 * @date 2022/5/31
 */


@Service
public class DeviceStaticsToUpSystem {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StatisticsDao statisticsDao;

    @Autowired
    private UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private ApplicationProperties applicationProperties;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String MINUTE = "MINUTE";
    public static final String DAY = "DAY";

    private final Logger log = LoggerFactory.getLogger(DeviceStaticsToUpSystem.class);

    /**
     * 可靠性指标定时上报上级系统
     **/


    @Scheduled(cron = "${spring.device.time}")
    public void resultToUpSystem() {
        String reportDate = DateUtils.dateToString(new Date());
        List<Map<String, Object>> infoMaps = new ArrayList<>();
        // 获取摄像机，机器人，无人机的信息
        List<Map<String, Object>> robotList = statisticsDao.selectRobot();
        List<Map<String, Object>> cameraList = statisticsDao.selectCamera();
        List<Map<String, Object>> droneList = statisticsDao.selectDrone();
        // 加载进入redis缓存
        statisticsService.selectStatisticsRobot(null, "robot", 0, 0);
        statisticsService.selectStatisticsRobot(null, "", 0, 0);
        statisticsService.countCamera(null, 0, 0);
        robotList.addAll(cameraList);
        robotList.addAll(droneList);
        robotList.forEach(device -> {
            if (device.containsKey("robotId")) {
                packageInfo("robotId", device, infoMaps, reportDate);
            }
            if (device.containsKey("cameraId")) {
                packageInfo("cameraId", device, infoMaps, reportDate);
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
        try {
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void packageInfoBack(String key, Map<String, String> device, List<Map<String, Object>> infoMaps, String reportDate) {
        Map<String, Object> deviceStaticsInfo;
        if (Constant.isUpSystem()) {
            // 是上级系统上报，直接查询数据库
            deviceStaticsInfo = uPatrolDeviceStaticsDao.selectByDeviceCode(device.get("patrolDeviceCode"));
        } else {
            String keys = "deviceStaticsInfo:" + key + ":" + device.get(key).toString();
            deviceStaticsInfo = redisTemplate.opsForHash().entries(keys);
        }
        for (int type = 1; type < 7; type++) {
            Map<String, Object> infoMap = new HashMap<>(16);
            infoMap.put("patroldevice_code", device.get("patrolDeviceCode"));
            infoMap.put("patroldevice_name", device.get("patrolDeviceName"));
            infoMap.put("commission_time", Optional.ofNullable(device.get("commissionTime")).orElse(""));
            infoMap.put("report_time", reportDate);
            infoMap.put("type", type);
            switch (type) {
                case 1:
                    // 累积在线时长总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("duration"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("duration"));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "分");
                    break;
                case 2:
                    // 累积离线次数总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("offLineCount"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("offLineCount"));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "次");
                    break;
                case 3:
                    // 累计连续正常运行天数
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("normalDay"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("normalDay"));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "天");
                    break;
                case 4:
                    // 正常巡检天数
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("commissionDays"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("commissionDays"));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "天");
                    break;
                case 5:
                    // 巡检出勤率
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("cruisePercent"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("cruisePercent").toString().replace("%", ""));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "%");
                    break;
                case 6:
                    // 录像完整率
                    if (!device.containsKey("cameraId")) {
                        break;
                    }
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("intactPercent"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("intactPercent").toString().replace("%", ""));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "%");
                    break;
                default:
                    break;
            }
            infoMaps.add(infoMap);
        }
    }

    private void packageInfo(String key, Map<String, Object> device, List<Map<String, Object>> infoMaps, String reportDate) {
        String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
        Map<String, Object> deviceStaticsInfo = new HashMap(8);
        if ("3".equals(sysLevel)) {
            // 是上级系统上报，直接查询数据库
            deviceStaticsInfo = uPatrolDeviceStaticsDao.selectByDeviceCode(device.get("patrolDeviceCode").toString());
        } else {
            deviceStaticsInfo = redisTemplate.opsForHash().entries("deviceStaticsInfo:" + key + ":" + device.get(key).toString());
        }
        for (int type = 1; type < 7; type++) {
            Map<String, Object> infoMap = new HashMap<>(16);
            switch (type) {
                case 1:
                    // 累积在线时长总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    deviceStaticsInfo = uPatrolDeviceStaticsDao.selectRobotInfo(Long.parseLong(device.get("robotId").toString()));
                    infoMap.put("value", StringUtils.isEmpty(deviceStaticsInfo.get("duration")) ? 0L : deviceStaticsInfo.get("duration").toString());
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "分");
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                case 2:
                    // 累积离线次数总和
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    deviceStaticsInfo = uPatrolDeviceStaticsDao.selectRobotInfo(Long.parseLong(device.get("robotId").toString()));
                    infoMap.put("value", StringUtils.isEmpty(deviceStaticsInfo.get("off_line_count")) ? 0 : Integer.parseInt(deviceStaticsInfo.get("off_line_count").toString()));
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "次");
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                case 3:
                    // 累计连续正常运行天数
                    if (!device.containsKey("robotId")) {
                        break;
                    }
                    Integer NormalDays = uPatrolDeviceStaticsDao.selectNormalDays(device.get("patrolDeviceCode").toString());
                    if (NormalDays == null ) {
                        Map<String,Object> result = uPatrolDeviceStaticsDao.selectCommissionDays(Long.parseLong(device.get("robotId").toString()));
                        NormalDays = (Integer) result.get("commission_days");
                    }
                    infoMap.put("value", NormalDays == null ? 0 : NormalDays);
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "天");
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                case 4:
                    // 正常巡检天数
                    if (device.containsKey("robotId")) {
                        Map<String,Object> result = uPatrolDeviceStaticsDao.selectCommissionDays(Long.parseLong(device.get("robotId").toString()));
                        infoMap.put("value",result.get("cruise_day") == null ?  0:result.get("cruise_day"));
                        infoMap.put("value_unit", "1");
                        infoMap.put("unit", "天");
                    }
                    else if (device.containsKey("cameraId"))  {
                        Map<String,Object> result = uPatrolDeviceStaticsDao.selectCommissionDaysForCamera(Long.parseLong(device.get("cameraId").toString()));
                        infoMap.put("value",result.get("cruise_day") == null ?  0:result.get("cruise_day"));
                        infoMap.put("value_unit", "1");
                        infoMap.put("unit", "天");
                    }
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                case 5:
                    // 巡检出勤率
                    if (device.containsKey("robotId")) {
                        Map<String,Object> result = uPatrolDeviceStaticsDao.selectCommissionDays(Long.parseLong(device.get("robotId").toString()));
                        infoMap.put("value",result.get("cruise_rate") == null ?  0:Double.parseDouble(result.get("cruise_rate").toString()));
                        infoMap.put("value_unit", "1");
                        infoMap.put("unit", "%");
                    }
                    else if (device.containsKey("cameraId"))  {
                        Map<String,Object> result = uPatrolDeviceStaticsDao.selectCommissionDaysForCamera(Long.parseLong(device.get("cameraId").toString()));
                        infoMap.put("value",result.get("cruise_rate") == null ?  0:Double.parseDouble(result.get("cruise_rate").toString()));
                        infoMap.put("value_unit", "1");
                        infoMap.put("unit", "%");
                    }
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                case 6:
                    // 录像完整率
                    if (!device.containsKey("cameraId")) {
                        break;
                    }
                    if (StringUtils.isEmpty(deviceStaticsInfo.get("intactPercent"))) {
                        infoMap.put("value", "0");
                    } else {
                        infoMap.put("value", deviceStaticsInfo.get("intactPercent").toString().replace("%", ""));
                    }
                    infoMap.put("value_unit", "1");
                    infoMap.put("unit", "%");
                    dealFuncInfo(device, reportDate, type, infoMap);
                    break;
                default:
                    break;
            }
            if (MapUtils.isNotEmpty(infoMap)){
                infoMaps.add(infoMap);
            }
        }
    }

    private void dealFuncInfo(Map<String, Object> device, String reportDate, int type, Map<String, Object> infoMap) {
        infoMap.put("patroldevice_code", device.get("patrolDeviceCode"));
        infoMap.put("patroldevice_name", device.get("patrolDeviceName"));
        infoMap.put("commission_time", Optional.ofNullable(device.get("commissionTime")).orElse(""));
        infoMap.put("report_time", reportDate);
        infoMap.put("type", type);
        infoMap.put("device_type", device.get("device_type"));
        infoMap.put("value_unit", String.valueOf(infoMap.get("value")) + infoMap.get("unit"));
    }

    private Long parseDate(String dateString, String target) {
        long ms = Long.parseLong(dateString);
        if (MINUTE.equals(target)) {
            return ms / 60 / 1000;
        }
        if (DAY.equals(target)) {
            return ms / 1000 / 60 / 60 / 24;
        }
        return null;
    }
}
