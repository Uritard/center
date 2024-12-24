package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.RedisUtil;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.service.*;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TAlgorithmInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.MAP_LOCK;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author YChen
 * @date 2021/12/16
 * 机器人巡视结果的告警处理线程
 */
@Slf4j
public class RobotInspectionWarnThread implements Runnable{

    private final RobotPatrolTaskAlarm taskAlarm;
    private final RedisTemplate redisTemplate;
    private final PatrolResultHandler patrolResultHandler;
    private final AnalyseDataOperateService analyseDataOperateService;
    private final UPatrolTaskService uPatrolTaskService;
    private final String taskCode;
    private final TRobotInspectionDao tRobotInspectionDao;
    private AutoreviewHandler autoreviewHandler;
    private final TAlgorithmInfoService tAlgorithmInfoService;

    private Map<String, String> tCruiseTaskResultMap = Collections.EMPTY_MAP;

    private final static Map<String, String> ALARM_TYPE_MAP = new HashMap<>(16);

    private final Object waiter = new Object();

    static {
        ALARM_TYPE_MAP.put("2022_1", "130");
        ALARM_TYPE_MAP.put("2022_2", "131");
        ALARM_TYPE_MAP.put("2022_3", "132");
        ALARM_TYPE_MAP.put("2022_4", "133");
        ALARM_TYPE_MAP.put("2024_1", "131");
        ALARM_TYPE_MAP.put("2024_2", "132");
        ALARM_TYPE_MAP.put("2024_3", "133");
    }

    public RobotInspectionWarnThread(RobotPatrolTaskAlarm taskAlarm, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService, String taskCode){
        this.taskAlarm = taskAlarm;
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
        this.taskCode = taskCode;
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.tRobotInspectionDao = StaticContextAccessor.getBean(TRobotInspectionDao.class);
        this.autoreviewHandler = StaticContextAccessor.getBean(AutoreviewHandler.class);
        this.tAlgorithmInfoService = StaticContextAccessor.getBean(TAlgorithmInfoService.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡视结果产生的告警数据 >>>>>>> taskAlarm==={}", JSON.toJSONString(taskAlarm));
            String taskId = taskAlarm.getTaskCode();
            String robotCode = taskAlarm.getRobotCode();

            // taskId是巡视主机的id,robotTaskId是机器人上报的id
            String robotTaskId = taskCode;
            log.info("robotTaskId==={}", robotTaskId);

            UPatrolTask uPatrolTaskTemp = uPatrolTaskService.selectTaskByTaskCode(robotTaskId);
            log.info("uPatrolTaskTemp=={}", uPatrolTaskTemp);
            if (Objects.nonNull(uPatrolTaskTemp) && uPatrolTaskService.scheduledByLocal(uPatrolTaskTemp.getDateType())){
                robotTaskId = taskId;
            }
            log.info("robotTaskId=={}", robotTaskId);

            String redisKey = "Robot_SPAndIN_Info:" + robotCode + ":" + taskAlarm.getDeviceId();
            Map<String,String> robotInfoKeyMap = redisTemplate.opsForHash().entries(redisKey);
            long instanceId = NumberUtils.toLong(robotInfoKeyMap.get("instanceId"));
            // 上级系统没有存储对应值，DeviceId 就是下级的 instanceId
            if (instanceId == 0) {
                String originId = taskAlarm.getDeviceId();
                TCruisePointInstance insInfo;
                if (Constant.standardPoints()){
                    insInfo = tRobotInspectionDao.selectRealInstanceByDevicePoint(originId);
                }else {
                    insInfo = tRobotInspectionDao.selectRealInstance(originId, robotCode);
                }
                log.info("instanceInfo: {}", JSON.toJSONString(insInfo));
                instanceId = insInfo.getInstanceId();
            }

            log.info("instanceId=={}", instanceId);
            TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(instanceId);

            TWarnInfo warnInfo = new TWarnInfo();
            if (CommonUtils.isEmptyOrNullstr(taskAlarm.getDefectType())){
                warnInfo = getWarnInfo(tStdDevicemete, taskId, instanceId, robotCode, warnInfo);
                putWarnMapRedis(taskId, warnInfo);
            }else {
                TDefectInfo tDefectInfo = getDefectInfo(tStdDevicemete, taskId, instanceId);
                warnInfo.setWarnSubtype(tDefectInfo.getDefectType());
                putDefectMapRedis(taskId, tDefectInfo);
            }

            Map<String, String> cruiseMap = new HashMap<>();
            // cruiseMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            // cruiseMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
            // 为了避免存入告警时初始化的值不正确，再次传入一下 instanceId避免问题
            cruiseMap.put("instanceId", String.valueOf(instanceId));
            cruiseMap.put("isWarn", "1");

            redisTemplate.opsForHash().putAll(PATROL_TASK_PREFIX + taskId + ":" + instanceId, cruiseMap);

            // 将产生的告警上送至上一级系统 (操作告警除外)
            if (!Objects.equals(warnInfo.getWarnType(), 507)) {
                alarmToUpSystem(taskAlarm.getAlarmLevel(), warnInfo, taskId, instanceId);
            }
            // 告警推送
            Map<String, String> infoMap = new HashMap<>(5);
            infoMap.put("alarmLevel", String.valueOf(warnInfo.getWarnLevel()));
            infoMap.put("flag", "robot");
            infoMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            infoMap.put("warnId", String.valueOf(warnInfo.getWarnId()));
            patrolResultHandler.alarmPopUp(tStdDevicemete, infoMap);

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    private TDefectInfo getDefectInfo(TStdDeviceMete tStdDevicemete, String taskId, Long instanceId){
        TDefectInfo tDefectInfo = new TDefectInfo();
        try {
            tDefectInfo.setDefectTime(DateTimeUtil.parse(taskAlarm.getTime()));
            tDefectInfo.setDefectName(taskAlarm.getContent());
            tDefectInfo.setDefectContent(taskAlarm.getContent());
            tDefectInfo.setDeviceId(tStdDevicemete.getDeviceId());
            tDefectInfo.setCunstomId(tStdDevicemete.getCustomId());
            tDefectInfo.setInstanceId(instanceId);
            tDefectInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            tDefectInfo.setConfMode(276);
            String type = tRobotInspectionDao.selectTypeByInstanceId(instanceId);
            int alarmSource = 279;
            if (StringUtils.isNotEmpty(type)){
                String cruiseTypeName = DictConvertUtil.DICT.covertToDict("cruiseType", type);
                alarmSource = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("alarmSource", cruiseTypeName),279);
            }
            tDefectInfo.setAlarmSource(alarmSource);
            tDefectInfo.setValue(taskAlarm.getValue());
            //转成数字
            String[] defcetTypeList = taskAlarm.getDefectType().split(",");
            String numDefectType = "";
            for (String defectType:defcetTypeList){
                TAlgorithmInfo defect = tAlgorithmInfoService.getDefectInfo(defectType);
                numDefectType = ","+defect.getDefectType()+numDefectType;
            }
            tDefectInfo.setDefectType(numDefectType.replaceFirst(",",""));

            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            tDefectInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            tDefectInfo.setDefectLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));
            tDefectInfo.setOriginId(taskAlarm.getOriginId());
            tDefectInfo.setEdgeCode(taskAlarm.getEdgeCode());
            tDefectInfo.setTaskId(taskId);
            tDefectInfo.setLabelAttri(CommonUtils.defaultEmpty(tStdDevicemete.getLabelAttri()));
            tDefectInfo.setDeviceType(String.valueOf(tStdDevicemete.getDeviceType()));

            autoreviewHandler.autoreviewCheckDefect(tDefectInfo);
        }catch (Exception e){
            log.error("组装缺陷信息异常：", e);
        }
        log.info("tDefectInfo==={}", tDefectInfo);
        analyseDataOperateService.insertDefectInfo(tDefectInfo);
        return tDefectInfo;
    }

    private TWarnInfo getWarnInfo(TStdDeviceMete tStdDevicemete, String taskId, Long instanceId, String robotCode, TWarnInfo warnInfo){
        try {
            warnInfo.setWarnTime(DateTimeUtil.parse(taskAlarm.getTime()));
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(instanceId);
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(276);
            Integer warnFlag = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("defectModel", "其他"), 450);
            warnInfo.setDefectModel(warnFlag);
            String type = tRobotInspectionDao.selectTypeByInstanceId(instanceId);
            int alarmSource = 279;
            if (StringUtils.isNotEmpty(type)) {
                String cruiseTypeName = DictConvertUtil.DICT.covertToDict("cruiseType", type);
                alarmSource = NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("alarmSource", cruiseTypeName),279);
            }
            warnInfo.setAlarmOwner(1);
            warnInfo.setAlarmSource(alarmSource);
            warnInfo.setValue(taskAlarm.getValue());
            warnInfo.setTaskId(taskId);
            String robotId = String.valueOf(StaticContextAccessor.getBean(TRobotInfoDao.class).selectRobotIdByCode(robotCode));
            warnInfo.setDeviceCode(robotId);
            warnInfo.setWarnName(taskAlarm.getContent());
            String alarmType = taskAlarm.getAlarmType();
            if (StringUtils.isNotEmpty(alarmType)) {
                warnInfo.setWarnType(analyseDataOperateService.selectDictCodeByUpDict("point_alarm_type", alarmType));
            }
            warnInfo.setWarnContent(taskAlarm.getContent());
            warnInfo.setOriginId(taskAlarm.getOriginId());
            warnInfo.setEdgeCode(taskAlarm.getEdgeCode());
            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            warnInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            warnInfo.setWarnLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));
            warnInfo.setLabelAttri(tStdDevicemete.getLabelAttri());
            warnInfo.setDeviceType(String.valueOf(tStdDevicemete.getDeviceType()));
        }catch (Exception e){
            log.error("组装告警信息异常：", e);
        }
        log.info("warnInfo==={}", warnInfo);
        StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);
        return warnInfo;
    }

    private void putWarnMapRedis(String taskId, TWarnInfo warnInfo) {
        String warnPrefix = "warnInfo:" + taskId;
        String tempKey = String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        try {
            warnMap.put("deviceId", warnInfo.getDeviceId().toString());
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", warnInfo.getInstanceId().toString());
            warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("confMode", String.valueOf(warnInfo.getConfMode()));
            warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
            warnMap.put("defectModel", warnInfo.getDefectModel().toString());
            warnMap.put("warnLevel", warnInfo.getWarnLevel().toString());
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", Objects.nonNull(warnInfo.getWarnTime()) ?
                    DateTimeUtil.format(warnInfo.getWarnTime()) : DateTimeUtil.format(new Date()));
            warnMap.put("warnContent", warnInfo.getWarnContent());

            warnMap.put("deviceName", tCruiseTaskResultMap.get("deviceName"));
            warnMap.put("instanceName", tCruiseTaskResultMap.get("instanceName"));
            warnMap.put("cruiseType", tCruiseTaskResultMap.get("cruiseType"));
            warnMap.put("cruiseTime", ValueUtil.getOrDefault(tCruiseTaskResultMap.get("time"),DateTimeUtil.format(new Date())));
            warnMap.put("warnId", String.valueOf(warnInfo.getWarnId()));
        }catch (Exception e){
            log.error("组装告警map异常：", e);
        }
        log.info("warnMap==={}", warnMap);
        RedisUtil.setHashGroupAndExpire(warnPrefix, tempKey, warnMap, 7);
    }

    private void putDefectMapRedis(String taskId, TDefectInfo tDefectInfo) {
        String defectPrefix = "defectInfo:" + taskId;
        String tempKey = String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> defectMap = new HashMap<>(32);
        try {
            defectMap.put("defectLevel", String.valueOf(tDefectInfo.getDefectLevel()));
            defectMap.put("defectContent", tDefectInfo.getDefectContent());
            defectMap.put("deviceId", String.valueOf(tDefectInfo.getDeviceId()));
            defectMap.put("instanceId", String.valueOf(tDefectInfo.getInstanceId()));
            defectMap.put("customId", tDefectInfo.getCunstomId());
            defectMap.put("stdMeteId", String.valueOf(tDefectInfo.getStdMeteId()));
            defectMap.put("confMode", String.valueOf(tDefectInfo.getConfMode()));
            defectMap.put("alarmSource", String.valueOf(tDefectInfo.getAlarmSource()));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", tDefectInfo.getValue());

            defectMap.put("deviceName", tCruiseTaskResultMap.get("deviceName"));
            defectMap.put("instanceName", tCruiseTaskResultMap.get("instanceName"));
            defectMap.put("cruiseType", tCruiseTaskResultMap.get("cruiseType"));
            defectMap.put("cruiseTime", tCruiseTaskResultMap.get("cruiseTime"));
            defectMap.put("taskId", taskId);
            defectMap.put("warnId", String.valueOf(tDefectInfo.getDefectId()));
            defectMap.put("defectModel", String.valueOf(tDefectInfo.getDefectType()));
        }catch (Exception e){
            log.error("组装缺陷map异常：" , e);
        }
        log.info("defectMap==={}", defectMap);
        RedisUtil.setHashGroupAndExpire(defectPrefix, tempKey, defectMap, 7);
    }

    private Map<String, String> getWarnOrDefectInfo(String taskId, Long instanceId){
        HashMap<String, String> map = new HashMap<>(16);
        try {
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            log.info("taskId是：{}，instanceId是：{}的 tCruiseTaskResultMap：{}", taskId, instanceId, tCruiseTaskResultMap);
            if (!CommonUtils.isEmptyOrNullstr(tCruiseTaskResultMap.get("picpath"))) {
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            } else {
                MAP_LOCK.put(taskId + instanceId, waiter);
                synchronized (waiter) {
                    waiter.wait(5000);
                }
                tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            }

            TRobotInfo robotInfo = uPatrolTaskService.selectRobotInfoByCode(taskAlarm.getRobotCode());
            int robotApiType = Optional.ofNullable(robotInfo).map(TRobotInfo::getApiType).orElse(2024);
            String alarmLevel = robotApiType + "_" + taskAlarm.getAlarmLevel();
            map.put("level", ALARM_TYPE_MAP.getOrDefault(alarmLevel, "131"));
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        log.info("map==={}", map);
        return map;
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    private void alarmToUpSystem(String alarmLevel, TWarnInfo warnInfo, String taskId, Long instanceId){
        try{
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, warnInfo);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}
