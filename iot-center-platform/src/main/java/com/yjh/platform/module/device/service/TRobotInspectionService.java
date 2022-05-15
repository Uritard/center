package com.yjh.platform.module.device.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.device.controller.TRobotInspectionController;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseResult;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-08
 */
@Service
public class TRobotInspectionService {

    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;

    private Logger log = LoggerFactory.getLogger(TRobotInspectionService.class);

    private final String ZERO = "0";
    private final String ONE = "1";
    private final String TWO = "2";


    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.insert(tRobotInspection);
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.deleteByPrimaryId(inspectionId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotInspection tRobotInspection) {
        return this.tRobotInspectionDao.update(tRobotInspection);
    }


    @Transactional(rollbackFor = Exception.class)
    public TRobotInspection selectByPrimaryId(Long inspectionId) {
        return this.tRobotInspectionDao.selectByPrimaryId(inspectionId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> select(Long inspectionId, String inspectionCode, Long robotId, String inspectionName,
                                         String componentId, Integer meterType, Integer appearanceType, String saveTypeList,
                                         String recognitionTypeList, String phase, String deviceInfo) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.select(inspectionId, inspectionCode, robotId, inspectionName,
                componentId, meterType, appearanceType, saveTypeList,
                recognitionTypeList, phase, deviceInfo);
        return tRobotInspectionList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TRobotInspection> selectByPage(TRobotInspection tRobotInspection) {
        List<TRobotInspection> tRobotInspectionList = tRobotInspectionDao.selectByPage(tRobotInspection);
        return tRobotInspectionList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotInspection> list) {
        return this.tRobotInspectionDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RobotTaskMessage> selectRobotTaskMessage(String taskId, Long robotId) throws Exception {
        List<RobotTaskMessage> re = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取此机器人的巡视点
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        Map<String, Object> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
        Map<String, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":41");
        //String taskId = (String)mapForRobotInstance.get("taskId");
        String robotState = (String) mapForRobotState.get("value");
        if ("4".equals(robotState)) {
            //机器人未在做任务
//            Map<String,String> jasonMap=new HashMap<>();
//            jasonMap.put("type","noTask");
//            //jasonMap.put("taskId",tCruiseTask.getTaskId());
//            String json= JSON.toJSONString(jasonMap);
//            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//            log.info("发送给前端的消息-停止调接口：   "+json);
            return null;
        }
        String instanceList = (String) mapForRobotInstance.get("instanceIdList");
        if (instanceList == null) {
            return null;
        }
        instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
        String[] instanceIdList = instanceList.split(", ");
        //List<TCruisePointAttr> nameList =  tRobotInspectionDao.selectRobotTaskMessage(instanceIdList);
        for (String item : instanceIdList) {
            //获取任务数据
            Map<String, String> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + item);
            RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
            robotTaskMessage.setDeviceName(mapForRobotTaskMessage.get("deviceName"));
            robotTaskMessage.setInstanceName(mapForRobotTaskMessage.get("instanceName"));
            if (mapForRobotTaskMessage.get("cruiseTime") != null && !"null".equals(mapForRobotTaskMessage.get("cruiseTime"))) {
                robotTaskMessage.setCruiseTime(mapForRobotTaskMessage.get("cruiseTime"));
                robotTaskMessage.setResult(mapForRobotTaskMessage.get("resultNum"));
            } else {
                robotTaskMessage.setCruiseTime("");
                robotTaskMessage.setResult("");
            }
            re.add(robotTaskMessage);
        }
        return re;
    }

    private String getName(Long instanceId, int flag, List<TCruisePointAttr> nameList) {
        for (TCruisePointAttr item : nameList) {
            if (instanceId.equals(item.getInstanceId())) {
                if (flag == 0) {//巡检点名称
                    return item.getInstanceName();
                }
                if (flag == 1) {//设备名称
                    return item.getAttrName();
                }
            }
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Robot> selectRobotInfo(Integer robotType) {
        return this.tRobotInspectionDao.selectRobotInfo(robotType);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectRobotStatus(String robotCode) {
        Map<String, Object> re = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00");
        re = patrolStatus(robotCode);
        //<101>: = 轮转状态 <0>: = 空闲 <1>: = 值班
        Map<String, Object> mapForRotaryState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":101");
        if (mapForRotaryState.size() != 0 && Optional.ofNullable(mapForRotaryState.get("value")).isPresent()) {
            String value = String.valueOf(mapForRotaryState.get("value"));
            if (ZERO.equals(value)) {
                re.put("rotaryState", "空闲");
            } else if (ONE.equals(value)) {
                re.put("rotaryState", "值班");
            } else {
                re.put("rotaryState", "");
            }
        } else {
            re.put("rotaryState", "");
        }
        //<11>: = 充电电流
        Map<String, Object> mapForChargeCurrent = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":11");
        if (mapForChargeCurrent.size() != 0) {
            if (Optional.ofNullable(mapForChargeCurrent.get("valueUnit")).isPresent()) {
                re.put("chargeCurrent", mapForChargeCurrent.get("valueUnit"));
            } else if (Optional.ofNullable(mapForChargeCurrent.get("value")).isPresent()) {
                Double chargeCurrent = Double.valueOf(String.valueOf(mapForChargeCurrent.get("value")));
                re.put("chargeCurrent", df.format(chargeCurrent) + mapForChargeCurrent.get("unit"));
            } else {
                re.put("chargeCurrent", "");
            }
        } else {
            re.put("chargeCurrent", "");
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectDroneStatus(String robotCode) {
        Map<String, Object> re = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00");
        re = patrolStatus(robotCode);
        //<201>: = 飞行状态
        Map<String, String> mapForDroneFlyState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":201");
        if (mapForDroneFlyState.size() != 0) {
            String value = mapForDroneFlyState.get("value");
            switch (value) {
                case "1":
                    re.put("flyType", "待命");
                    break;
                case "2":
                    re.put("flyType", "准备起飞");
                    break;
                case "3":
                    re.put("flyType", "飞行中");
                    break;
                case "4":
                    re.put("flyType", "返航");
                    break;
                case "5":
                    re.put("flyType", "着陆");
                    break;
                default:
                    re.put("flyType", "");
                    break;
            }
        } else {
            re.put("flyType", "");
        }
        //<4>: = 垂直速度
        Map<String, String> mapForVerticalSpeed = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":4");
        if (mapForVerticalSpeed.size() != 0) {
            if (Optional.ofNullable(mapForVerticalSpeed.get("valueUnit")).isPresent()) {
                re.put("verticalSpeed", mapForVerticalSpeed.get("valueUnit"));
            } else if (Optional.ofNullable(mapForVerticalSpeed.get("value")).isPresent()) {
                Double speed = Double.valueOf(mapForVerticalSpeed.get("value"));
                re.put("verticalSpeed", df.format(speed) + mapForVerticalSpeed.get("unit"));
            } else {
                re.put("verticalSpeed", "");
            }
        } else {
            re.put("verticalSpeed", "");
        }

        //<5>: = 飞行距离
        Map<String, String> mapForFlyDistance = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":5");
        if (mapForFlyDistance.size() != 0) {
            if (Optional.ofNullable(mapForFlyDistance.get("valueUnit")).isPresent()) {
                re.put("flyDistance", mapForFlyDistance.get("valueUnit"));
            } else if (Optional.ofNullable(mapForFlyDistance.get("value")).isPresent()) {
                Double speed = Double.valueOf(mapForFlyDistance.get("value"));
                re.put("flyDistance", df.format(speed) + mapForFlyDistance.get("unit"));
            } else {
                re.put("flyDistance", "");
            }
        } else {
            re.put("flyDistance", "");
        }

        //<6>: = 飞行高度
        Map<String, String> mapForFlyHeight = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":6");
        if (mapForFlyHeight.size() != 0) {
            if (Optional.ofNullable(mapForFlyHeight.get("valueUnit")).isPresent()) {
                re.put("flyHeight", mapForFlyHeight.get("valueUnit"));
            } else if (Optional.ofNullable(mapForFlyHeight.get("value")).isPresent()) {
                Double speed = Double.valueOf(mapForFlyHeight.get("value"));
                re.put("flyHeight", df.format(speed) + mapForFlyHeight.get("unit"));
            } else {
                re.put("flyHeight", "");
            }
        } else {
            re.put("flyHeight", "");
        }

        //<7>: = 飞行时长
        Map<String, String> mapForFlyTime = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":7");
        if (mapForFlyTime.size() != 0) {
            if (Optional.ofNullable(mapForFlyTime.get("valueUnit")).isPresent()) {
                re.put("flyTime", mapForFlyTime.get("valueUnit"));
            } else if (Optional.ofNullable(mapForFlyTime.get("value")).isPresent()) {
                Double speed = Double.valueOf(mapForFlyTime.get("value"));
                re.put("flyTime", df.format(speed) + mapForFlyTime.get("unit"));
            } else {
                re.put("flyTime", "");
            }

        } else {
            re.put("flyTime", "");
        }

        /*机巢状态*/
        //<1>: = 运行状态
        Map<String, String> mapForNestStatus = redisTemplate.opsForHash().entries("nestStatus:" + robotCode + ":1");
        if (mapForNestStatus.size() != 0 && Optional.ofNullable(mapForNestStatus.get("value")).isPresent()) {
            String value = mapForNestStatus.get("value");
            switch (value){
                case "0":
                    re.put("nestStatus", "初始化");
                    break;
                case "1":
                    re.put("nestStatus", "自检中");
                    break;
                case "2":
                    re.put("nestStatus", "等待重置");
                    break;
                case "3":
                    re.put("nestStatus", "重置中");
                    break;
                case "4":
                    re.put("nestStatus", "待机");
                    break;
                case "5":
                    re.put("nestStatus", "异常");
                    break;
                default:
                    re.put("nestStatus", "");
                    break;
            }
        } else {
            re.put("nestStatus", "");
        }
        //<2>: = 舱门状态  <0>: = 关闭 <1>: = 启动
        Map<String, String> mapForNestDoorState = redisTemplate.opsForHash().entries("nestStatus:" + robotCode + ":2");
        if (mapForNestDoorState.size() != 0 && Optional.ofNullable(mapForNestDoorState.get("value")).isPresent()) {
            String value = mapForNestDoorState.get("value");
            if (ZERO.equals(value)) {
                re.put("nestDoorStatus", "关闭");
            } else if (ONE.equals(value)) {
                re.put("nestDoorStatus", "启动");
            } else {
                re.put("nestDoorStatus", "");
            }
        } else {
            re.put("nestDoorStatus", "");
        }
        //<3>: = 平台状态 <0>: = 关闭 <1>: = 启动
        Map<String, String> mapForNestPlatformState = redisTemplate.opsForHash().entries("nestStatus:" + robotCode + ":3");
        if (mapForNestPlatformState.size() != 0 && Optional.ofNullable(mapForNestPlatformState.get("value")).isPresent()) {
            String value = mapForNestPlatformState.get("value");
            if (ZERO.equals(value)) {
                re.put("nestPlatformStatus", "关闭");
            } else if (ONE.equals(value)) {
                re.put("nestPlatformStatus", "启动");
            } else {
                re.put("nestPlatformStatus", "");
            }
        } else {
            re.put("nestPlatformStatus", "");
        }
        //<4>: = 充电状态 <0>: = 未充电 <1>: = 充电中 <2>: = 充电完成
        Map<String, String> mapForNestChargeState = redisTemplate.opsForHash().entries("nestStatus:" + robotCode + ":4");
        if (mapForNestChargeState.size() != 0 && Optional.ofNullable(mapForNestChargeState.get("value")).isPresent()) {
            String value = mapForNestChargeState.get("value");
            switch (value) {
                case "0":
                    re.put("nestChargeStatus", "未充电");
                    break;
                case "1":
                    re.put("nestChargeStatus", "充电中");
                    break;
                case "2":
                    re.put("nestChargeStatus", "充电完成");
                    break;
                default:
                    re.put("nestChargeStatus", "");
                    break;
            }
        } else {
            re.put("nestChargeStatus", "");
        }

        //<4>: = 空调状态 <0>: = 关闭 <1>: = 启动 <2>: = 异常
        Map<String, String> mapForNestAirState = redisTemplate.opsForHash().entries("nestStatus:" + robotCode + ":5");
        if (mapForNestAirState.size() != 0 && Optional.ofNullable(mapForNestAirState.get("value")).isPresent()) {
            String value = mapForNestAirState.get("value");
            switch (value) {
                case "0":
                    re.put("nestAirStatus", "关闭");
                    break;
                case "1":
                    re.put("nestAirStatus", "启动");
                    break;
                case "2":
                    re.put("nestAirStatus", "异常");
                    break;
                default:
                    re.put("nestAirStatus", "");
                    break;
            }
        } else {
            re.put("nestAirStatus", "");
        }
        /*机巢运行数据*/
        //<1>: = 电池电量
        Map<String, String> mapForNestBattery = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":1");
        if (mapForNestBattery.size() != 0) {
            if (Optional.ofNullable(mapForNestBattery.get("valueUnit")).isPresent()) {
                re.put("nestBatteryLevel", mapForNestBattery.get("valueUnit"));
            } else if (Optional.ofNullable(mapForNestBattery.get("value")).isPresent()) {
                Double headYawAngle = Double.valueOf(String.valueOf(mapForNestBattery.get("value")));
                re.put("nestBatteryLevel", df.format(headYawAngle) + mapForNestBattery.get("unit"));
            } else {
                re.put("nestBatteryLevel", "");
            }
        } else {
            re.put("nestBatteryLevel", "");
        }
        //<2>: = 电池使用状态 <1>: = 充电中 <2>: = 使用中
        Map<String, String> mapForNestBatterUseState = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":2");
        if (mapForNestBatterUseState.size() != 0 && Optional.ofNullable(mapForNestBattery.get("value")).isPresent()) {
            String value = mapForNestBatterUseState.get("value");
            if (ONE.equals(value)) {
                re.put("nestBatteryUseState", "充电中");
            } else if (TWO.equals(value)) {
                re.put("nestBatteryUseState", "使用中");
            } else {
                re.put("nestBatteryUseState", "");
            }
        } else {
            re.put("nestBatteryUseState", "");
        }
        //<3>: = 电池状态 <1>: = 充电中 <2>: = 使用中
        Map<String, String> mapForNestBatteryState = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":3");
        if (mapForNestBatteryState.size() != 0 && Optional.ofNullable(mapForNestBattery.get("value")).isPresent()) {
            String value = mapForNestBatteryState.get("value");
            if (ONE.equals(value)) {
                re.put("nestBatteryState", "正常");
            } else if (TWO.equals(value)) {
                re.put("nestBatteryState", "异常");
            } else {
                re.put("nestBatteryState", "");
            }
        } else {
            re.put("nestBatteryState", "");
        }

        //<4>: = 电池电压
        Map<String, String> mapForNestBatteryVoltage = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":4");
        if (mapForNestBatteryVoltage.size() != 0) {
            if (Optional.ofNullable(mapForNestBatteryVoltage.get("valueUnit")).isPresent()) {
                re.put("nestBatteryVoltage", mapForNestBatteryVoltage.get("valueUnit"));
            } else if (Optional.ofNullable(mapForNestBatteryVoltage.get("value")).isPresent()) {
                Double headYawAngle = Double.valueOf(String.valueOf(mapForNestBatteryVoltage.get("value")));
                re.put("nestBatteryVoltage", df.format(headYawAngle) + mapForNestBatteryVoltage.get("unit"));
            } else {
                re.put("nestBatteryVoltage", "");
            }
        } else {
            re.put("nestBatteryVoltage", "");
        }
        //<5>: = 舱内温度
        Map<String, String> mapForNestTemperature = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":5");
        if (mapForNestTemperature.size() != 0) {
            if (Optional.ofNullable(mapForNestTemperature.get("valueUnit")).isPresent()) {
                re.put("nestTemperature", mapForNestTemperature.get("valueUnit"));
            } else if (Optional.ofNullable(mapForNestTemperature.get("value")).isPresent()) {
                Double headYawAngle = Double.valueOf(String.valueOf(mapForNestTemperature.get("value")));
                re.put("nestTemperature", df.format(headYawAngle) + mapForNestTemperature.get("unit"));
            } else {
                re.put("nestTemperature", "");
            }
        } else {
            re.put("nestTemperature", "");
        }
        //<6>: = 舱内湿度
        Map<String, String> mapForNestHumidity = redisTemplate.opsForHash().entries("nestOperation:" + robotCode + ":6");
        if (mapForNestHumidity.size() != 0) {
            if (Optional.ofNullable(mapForNestHumidity.get("valueUnit")).isPresent()) {
                re.put("nestHumidity", mapForNestHumidity.get("valueUnit"));
            } else if (Optional.ofNullable(mapForNestHumidity.get("value")).isPresent()) {
                Double headYawAngle = Double.valueOf(String.valueOf(mapForNestHumidity.get("value")));
                re.put("nestHumidity", df.format(headYawAngle) + mapForNestHumidity.get("unit"));
            } else {
                re.put("nestHumidity", "");
            }
        } else {
            re.put("nestHumidity", "");
        }
        return re;
    }

    private Map<String, Object> patrolStatus(String robotCode) {
        Map<String, Object> re = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00");
        /*状态数据*/
        //<1>电池电量低 <0>: = 正常 <1>: = 低
        Map<String, Object> mapForBatteryLow = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":1");
        if (mapForBatteryLow.size() != 0 && Optional.ofNullable(mapForBatteryLow.get("value")).isPresent()) {
            String value = String.valueOf(mapForBatteryLow.get("value"));
            if (ZERO.equals(value)) {
                re.put("batteryLow", "正常");
            } else if (ONE.equals(value)) {
                re.put("batteryLow", "低");
            } else {
                re.put("batteryLow", "");
            }
        } else {
            re.put("batteryLow", "");
        }

        //<2>: = 通信状态异常 <0>: = 正常 <1>: = 异常
        Map<String, Object> mapForOnlineState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
        if (mapForOnlineState.size() != 0 && Optional.ofNullable(mapForOnlineState.get("value")).isPresent()) {
            String value = String.valueOf(mapForOnlineState.get("value"));
            if (ZERO.equals(value)) {
                re.put("onlineState", "正常");
            }else  if (ONE.equals(value)) {
                re.put("onlineState", "异常");
            } else {
                re.put("onlineState", "");
            }
        } else {
            re.put("onlineState", "");
        }

        //<3>: = 超声停障 <0>: = 正常 <1>: = 停障
        Map<String, Object> mapForUltrasonicStop = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":3");
        if (mapForUltrasonicStop.size() != 0 && Optional.ofNullable(mapForUltrasonicStop.get("value")).isPresent()) {
            String value = String.valueOf(mapForUltrasonicStop.get("value"));
            if (ZERO.equals(value)) {
                re.put("ultrasonicStop", "正常");
            } else if (ONE.equals(value)) {
                re.put("ultrasonicStop", "停障");
            } else {
                re.put("ultrasonicStop", "");
            }
        } else {
            re.put("ultrasonicStop", "");
        }

        //<4>: = 驱动异常 <0>: = 正常 <1>: = 异常
        Map<String, Object> mapForDriveAnomalies = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":4");
        if (mapForDriveAnomalies.size() != 0 && Optional.ofNullable(mapForDriveAnomalies.get("value")).isPresent()) {
            String value = String.valueOf(mapForDriveAnomalies.get("value"));
            if (ZERO.equals(value)) {
                re.put("driveAnomalies", "正常");
            } else if (ONE.equals(value)) {
                re.put("driveAnomalies", "异常");
            } else {
                re.put("driveAnomalies", "");
            }
        } else {
            re.put("driveAnomalies", "");
        }

        //<21>: = 故障报警 <0>: = 正常 <1>: = 报警
        Map<String, Object> mapForFaultAlarm = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":21");
        if (mapForFaultAlarm.size() != 0 && Optional.ofNullable(mapForFaultAlarm.get("value")).isPresent()) {
            String value = String.valueOf(mapForFaultAlarm.get("value"));
            if (ZERO.equals(value)) {
                re.put("faultAlarm", "正常");
            } else if (ONE.equals(value)) {
                re.put("faultAlarm", "报警");
            } else {
                re.put("faultAlarm", "");
            }
        } else {
            re.put("faultAlarm", "");
        }

        //<41>: = 运行状态 <1>: = 空闲状态 <2>: = 巡视状态 <3>: = 充电状态 <4>: = 检修状态
        Map<String, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":41");
        if (mapForRobotState.size() != 0 && Optional.ofNullable(mapForRobotState.get("value")).isPresent()) {
            String value = String.valueOf(mapForRobotState.get("value"));
            switch (value) {
                case "1":
                    re.put("robotState", "空闲状态");
                    break;
                case "2":
                    re.put("robotState", "巡视状态");
                    break;
                case "3":
                    re.put("robotState", "充电状态");
                    break;
                case "4":
                    re.put("robotState", "检修状态");
                    break;
                default:
                    re.put("robotState", "");
                    break;
            }
            if (!"2".equals(mapForRobotState.get("value"))) {
                re.put("cruiseMapPath", "");
            }
        } else {
            re.put("robotState", "");
        }

        //<61>: = 控制模式 <1>: = 任务模式 <2>: = 紧急定位模式 <3>: = 后台遥控模式 <4>: = 手持遥控模式
        Map<String, Object> mapForRobotDefeatState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":61");
        if (mapForRobotDefeatState.size() != 0 && Optional.ofNullable(mapForRobotDefeatState.get("value")).isPresent()) {
            String value = String.valueOf(mapForRobotDefeatState.get("value"));
            switch (value) {
                case "1":
                    re.put("modelType", "任务模式");
                    break;
                case "2":
                    re.put("modelType", "紧急定位模式");
                    break;
                case "3":
                    re.put("modelType", "后台遥控模式");
                    break;
                case "4":
                    re.put("modelType", "手持遥控模式");
                    break;
                case "5":
                    re.put("modelType", "操作模式");
                    break;
                default:
                    re.put("modelType", "");
                    break;
            }
        } else {
            re.put("modelType", "");
        }

        //<81>: = 控制权状态 <0>: = 空闲 <1>: = 获得
        Map<String, Object> mapForControlState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":81");
        if (mapForControlState.size() != 0 && Optional.ofNullable(mapForControlState.get("value")).isPresent()) {
            String value = String.valueOf(mapForControlState.get("value"));
            if (ZERO.equals(value)) {
                re.put("controlState", "空闲");
            } else if (ONE.equals(value)) {
                re.put("controlState", "获得");
            } else {
                re.put("controlState", "");
            }
        } else {
            re.put("controlState", "");
        }

        /*运行数据*/
        //<1>: = 水平速度
        Map<String, Object> mapForSpeed = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":1");
        if (mapForSpeed.size() != 0) {
            if (Optional.ofNullable(mapForSpeed.get("valueUnit")).isPresent()) {
                re.put("speed", mapForSpeed.get("valueUnit"));
            } else if (Optional.ofNullable(mapForSpeed.get("value")).isPresent()) {
                Double speed = Double.valueOf(String.valueOf(mapForSpeed.get("value")));
                re.put("speed", df.format(speed) + mapForSpeed.get("unit"));
            } else {
                re.put("speed", "");
            }
        } else {
            re.put("speed", "");
        }
        //<2>: = 行驶里程
        Map<String, Object> mapForMileage = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":2");
        if (mapForMileage.size() != 0) {
            if (Optional.ofNullable(mapForMileage.get("valueUnit")).isPresent()) {
                re.put("mileage", mapForMileage.get("valueUnit"));
            } else if (Optional.ofNullable(mapForMileage.get("value")).isPresent()) {
                Double mileage = Double.valueOf(String.valueOf(mapForMileage.get("value")));
                re.put("mileage", df.format(mileage) + mapForMileage.get("unit"));
            } else {
                re.put("mileage", "");
            }
        } else {
            re.put("mileage", "");
        }

        //<3>: = 电池电量
        Map<String, Object> mapForCell = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":3");
        if (mapForCell.size() != 0) {
            if (Optional.ofNullable(mapForCell.get("valueUnit")).isPresent()) {
                re.put("batteryLevel", mapForCell.get("valueUnit"));
            } else if (Optional.ofNullable(mapForCell.get("value")).isPresent()) {
                Double batteryLevel = Double.valueOf(String.valueOf(mapForCell.get("value")));
                re.put("batteryLevel", df.format(batteryLevel) + mapForCell.get("unit"));
            } else {
                re.put("batteryLevel", "");
            }
        } else {
            re.put("batteryLevel", "");
        }

        // <8>: = 云台俯仰角
        Map<String, Object> mapForHeadPitchAngle = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":8");
        if (mapForHeadPitchAngle.size() != 0) {
            if (Optional.ofNullable(mapForHeadPitchAngle.get("valueUnit")).isPresent()) {
                re.put("headPitchAngle", mapForHeadPitchAngle.get("valueUnit"));
            } else if (Optional.ofNullable(mapForHeadPitchAngle.get("value")).isPresent()) {
                Double headPitchAngle = Double.valueOf(String.valueOf(mapForHeadPitchAngle.get("value")));
                re.put("headPitchAngle", df.format(headPitchAngle) + mapForHeadPitchAngle.get("unit"));
            } else {
                re.put("headPitchAngle", "");
            }
        } else {
            re.put("headPitchAngle", "");
        }

        //<9>: = 云台横滚角
        Map<String, Object> mapForHeadRollAngle = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":9");
        if (mapForHeadRollAngle.size() != 0) {
            if (Optional.ofNullable(mapForHeadRollAngle.get("valueUnit")).isPresent()) {
                re.put("headRollAngle", mapForHeadRollAngle.get("valueUnit"));
            } else if (Optional.ofNullable(mapForHeadRollAngle.get("value")).isPresent()) {
                Double headRollAngle = Double.valueOf(String.valueOf(mapForHeadRollAngle.get("value")));
                re.put("headRollAngle", df.format(headRollAngle) + mapForHeadRollAngle.get("unit"));
            } else {
                re.put("headRollAngle", "");
            }

        } else {
            re.put("headRollAngle", "");
        }

        //<10>: = 云台偏航角
        Map<String, Object> mapForHeadYawAngle = redisTemplate.opsForHash().entries("RobotOperation:" + robotCode + ":10");
        if (mapForHeadYawAngle.size() != 0) {
            if (Optional.ofNullable(mapForHeadYawAngle.get("valueUnit")).isPresent()) {
                re.put("headYawAngle", mapForHeadYawAngle.get("valueUnit"));
            } else if (Optional.ofNullable(mapForHeadYawAngle.get("value")).isPresent()) {
                Double headYawAngle = Double.valueOf(String.valueOf(mapForHeadYawAngle.get("value")));
                re.put("headYawAngle", df.format(headYawAngle) + mapForHeadYawAngle.get("unit"));
            } else {
                re.put("headYawAngle", "");
            }
        } else {
            re.put("headYawAngle", "");
        }

        //坐标
        Map<String, Object> mapForDroneCoordinate = redisTemplate.opsForHash().entries("RobotCoordinate:" + robotCode);
        if (mapForDroneCoordinate.size() != 0 && Optional.ofNullable(mapForDroneCoordinate.get("coordinatePixel")).isPresent()) {
            re.put("coordinatePixel", mapForDroneCoordinate.get("coordinatePixel"));
        } else {
            re.put("coordinatePixel", "0,0,0,0");
        }
        //经纬度
        Map<String, Object> mapForDroneCoordinateGeography = redisTemplate.opsForHash().entries("RobotCoordinate:" + robotCode);
        if (mapForDroneCoordinateGeography.size() != 0 && Optional.ofNullable(mapForDroneCoordinateGeography.get("coordinateGeography")).isPresent()) {
            re.put("coordinateGeography", mapForDroneCoordinateGeography.get("coordinateGeography"));
        } else {
            re.put("coordinateGeography", "0,0");
        }

        //巡视路径地图路径
        Map<String, Object> mapForCruiseMap = redisTemplate.opsForHash().entries("RobotRoad:" + robotCode);
        if (mapForCruiseMap.size() != 0 && Optional.ofNullable(mapForCruiseMap.get("coordinatePixel")).isPresent()) {
            re.put("cruiseMapPath", mapForCruiseMap.get("relativePath"));
        } else {
            re.put("cruiseMapPath", "");
        }

        //0 开启状态 1 关闭状态
        Map<String, String> mapForRobotStopStatus = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":102");
        if (mapForRobotStopStatus.size() != 0 && Optional.ofNullable(mapForRobotStopStatus.get("value")).isPresent()) {
            re.put("stopFlag", mapForRobotStopStatus.get("value"));
        } else {
            re.put("stopFlag", "");
        }
        return re;
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectRobotTaskProgress(Long robotId) throws Exception {
        Map<String, Object> reMap = new HashMap<>();
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        String taskId = tRobotInspectionDao.selectRobotTaskOnStart(robotId);
        if (!StringUtils.isEmpty(taskId)) {
            reMap.put("taskId", taskId);
            Map<String, String> mapForRobotInstance = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
            if (mapForRobotInstance.size() == 0) {
                reMap.put("taskProgress", 0);
                reMap.put("taskName", "");
                reMap.put("startTime", "");
                reMap.put("taskState", "");
                return reMap;
            }
            String instanceList = mapForRobotInstance.get("instanceIdList");
            instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
            String[] instanceIdList = instanceList.split(", ");
            int i = 0;
            for (String item : instanceIdList) {
                Map<String, Object> mapForRobotTaskMessage = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + item);
                if (mapForRobotTaskMessage.size() != 0) {
                    if ("null".equals(mapForRobotTaskMessage.get("cruiseResult"))) {
                        continue;
                    }
                    i = i + 1;
                }
            }
            Integer re = (int) ((new BigDecimal((float) i / instanceIdList.length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()) * 100);
            TCruiseResult tc = tCruiseResultDao.selectForTaskId(taskId);
            if (tc == null) {
                reMap.put("taskProgress", 0);
                reMap.put("taskName", "");
                reMap.put("startTime", "");
                reMap.put("taskState", "");
            } else {
                if (tc.getCState() == 239 || tc.getCState() == 241) {
                    reMap.put("taskProgress", re);
                    reMap.put("taskName", tc.getTaskName());
                    reMap.put("startTime", mapForRobotInstance.get("startTime"));
                    String state = mapForRobotInstance.get("taskState");
                    reMap.put("taskState", taskStatusToString(state));
                    List<RobotTaskMessage> list = this.selectRobotTaskMessage(taskId, robotId);
                    reMap.put("list", list);
                } else {
                    reMap.put("taskProgress", 0);
                    reMap.put("taskName", "");
                    reMap.put("startTime", "");
                    reMap.put("taskState", "");
                }
            }
        } else {
            reMap.put("taskProgress", 0);
            reMap.put("taskName", "");
            reMap.put("startTime", "");
            reMap.put("taskState", "");
            reMap.put("taskId", "");
        }

        return reMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectRobotOperationTask(Long robotId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> reMap = tRobotInspectionDao.selectRobotOperationTaskOnStart(robotId);
        if (Objects.nonNull(reMap)) {
            resultMap.put("taskId", reMap.get("taskId"));
            resultMap.put("taskName", reMap.get("taskName"));
            resultMap.put("taskState", reMap.get("taskState"));
        } else {
            resultMap.put("taskId", "");
            resultMap.put("taskName", "");
            resultMap.put("taskState", "");
        }
        return resultMap;
    }

    private String taskStatusToString(String state) {
        if (!StringUtils.isEmpty(state)) {
            //1=已执行 2=正在执行 3=暂停 4=终止 5=未执行 6=超期
            if ("1".equals(state)) {
                state = "已执行";
            }
            if ("2".equals(state)) {
                state = "正在执行";
            }
            if ("3".equals(state)) {
                state = "暂停";
            }
            if ("4".equals(state)) {
                state = "终止";
            }
            if ("5".equals(state)) {
                state = "未执行";
            }
            if ("6".equals(state)) {
                state = "超期";
            }
        } else {
            state = "";
        }
        return state;
    }

    //机器人树
    @Transactional(rollbackFor = Exception.class)
    public List<Robot> robotTree(Integer robotType) {
        List<Robot> reList = new ArrayList<>();
        Robot node = new Robot();
        node.setId(1L);
        node.setLabel("机器人树");
        node.setInfoType("tree");
        List<Robot> robotList = this.selectRobotInfo(robotType);
        for (Robot robot : robotList) {
            robot.setId(robot.getRobotId());
            robot.setLabel(robot.getRobotName());
            robot.setUpId(1L);
            robot.setUpName("机器人树");
            robot.setInfoType("robot");
        }
        node.setChildren(robotList);
        reList.add(node);
        return reList;
    }

    //无人机树
    @Transactional(rollbackFor = Exception.class)
    public List<Robot> droneTree(Integer droneType) {
        List<Robot> reList = new ArrayList<>();
        Robot node = new Robot();
        node.setId(1L);
        node.setLabel("无人机树");
        node.setInfoType("tree");
        List<Robot> robotList = this.tRobotInspectionDao.selectDroneInfo(droneType);
        for (Robot robot : robotList) {
            robot.setId(robot.getRobotId());
            robot.setLabel(robot.getRobotName());
            robot.setUpId(1L);
            robot.setUpName("无人机树");
            robot.setInfoType("drone");
        }
        node.setChildren(robotList);
        reList.add(node);
        return reList;
    }

}

