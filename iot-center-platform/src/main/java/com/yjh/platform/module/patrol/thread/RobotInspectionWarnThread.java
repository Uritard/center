package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.ProcessResultToUpSystem;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TDefectInfo;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TDefectInfoService;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final TCruisePointInstanceDao tCruisePointInstanceDao;

    private final Object waiter = new Object();

    public RobotInspectionWarnThread(RobotPatrolTaskAlarm taskAlarm, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService, String taskCode){
        this.taskAlarm = taskAlarm;
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
        this.taskCode = taskCode;
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.tRobotInspectionDao = StaticContextAccessor.getBean(TRobotInspectionDao.class);
        this.tCruisePointInstanceDao = StaticContextAccessor.getBean(TCruisePointInstanceDao.class);
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
            if (Objects.nonNull(uPatrolTaskTemp) && StringUtils.isNotEmpty(uPatrolTaskTemp.getDateType())){
                boolean moreTime = uPatrolTaskTemp.getDateType().split(" ")[2].contains(",");
                if (moreTime) {
                    robotTaskId = taskId;
                }
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

            // 将产生的告警上送至上一级系统
            alarmToUpSystem(taskAlarm.getAlarmLevel(), warnInfo, taskId, instanceId);

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
            int alarmSource = 998;
            if (StringUtils.isNotEmpty(type)){
                alarmSource = NumberUtils.toInt(analyseDataOperateService.selectDictCode("alarm_source", type),998);
            }
            tDefectInfo.setAlarmSource(alarmSource);
            tDefectInfo.setValue(taskAlarm.getValue());
            tDefectInfo.setDefectType(Integer.valueOf(taskAlarm.getDefectType()));

            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            tDefectInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            tDefectInfo.setDefectLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));

        }catch (Exception e){
            log.error("组装缺陷信息异常：", e);
        }
        log.info("tDefectInfo==={}", tDefectInfo);
        StaticContextAccessor.getBean(TDefectInfoService.class).insert(tDefectInfo);
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
            Integer warnFlag = Integer.valueOf(analyseDataOperateService.selectDictCode("defect_model", "其他"));
            warnInfo.setDefectModel(warnFlag);
            String type = tRobotInspectionDao.selectTypeByInstanceId(instanceId);
            int alarmSource = 998;
            if (StringUtils.isNotEmpty(type)) {
                alarmSource = NumberUtils.toInt(analyseDataOperateService.selectDictCode("alarm_source", type),998);
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

            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            warnInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            warnInfo.setWarnLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));

        }catch (Exception e){
            log.error("组装告警信息异常：", e);
        }
        log.info("warnInfo==={}", warnInfo);
        StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);
        return warnInfo;
    }

    private void putWarnMapRedis(String taskId, TWarnInfo warnInfo) {
        String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        try {
            warnMap.put("deviceId", warnInfo.getDeviceId().toString());
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", warnInfo.getInstanceId().toString());
            warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
            warnMap.put("defectModel", warnInfo.getDefectModel().toString());
            warnMap.put("warnLevel", warnInfo.getWarnLevel().toString());
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", Objects.nonNull(warnInfo.getWarnTime()) ?
                    DateTimeUtil.format(warnInfo.getWarnTime()) : DateTimeUtil.format(new Date()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
        }catch (Exception e){
            log.error("组装告警map异常：", e);
        }
        log.info("warnMap==={}", warnMap);
        redisTemplate.opsForHash().putAll(warnName, warnMap);
    }

    private void putDefectMapRedis(String taskId, TDefectInfo tDefectInfo) {
        String defectName = "defectInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> defectMap = new HashMap<>(16);
        try {
            defectMap.put("defectLevel", String.valueOf(tDefectInfo.getDefectLevel()));
            defectMap.put("defectContent", tDefectInfo.getDefectContent());
            defectMap.put("deviceId", String.valueOf(tDefectInfo.getDeviceId()));
            defectMap.put("instanceId", String.valueOf(tDefectInfo.getInstanceId()));
            defectMap.put("customId", tDefectInfo.getCunstomId());
            defectMap.put("stdMeteId", String.valueOf(tDefectInfo.getStdMeteId()));
            defectMap.put("confMode", "276");
            defectMap.put("alarmSource", String.valueOf(tDefectInfo.getAlarmSource()));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", tDefectInfo.getValue());
        }catch (Exception e){
            log.error("组装缺陷map异常：" , e);
        }
        log.info("defectMap==={}", defectMap);
        redisTemplate.opsForHash().putAll(defectName, defectMap);
        redisTemplate.expire(defectName, 3, TimeUnit.DAYS);
    }

    private Map<String, String> getWarnOrDefectInfo(String taskId, Long instanceId){
        HashMap<String, String> map = new HashMap<>(4);
        try {
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            log.info("taskId是：{}，instanceId是：{}的 tCruiseTaskResultMap：{}", taskId, instanceId, tCruiseTaskResultMap);
            if (Objects.nonNull(tCruiseTaskResultMap.get("picpath"))) {
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            } else {
                MAP_LOCK.put(taskId + instanceId, waiter);
                synchronized (waiter) {
                    waiter.wait(5000);
                }
                tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            }

            String alarmLevel = taskAlarm.getAlarmLevel();
            switch (alarmLevel) {
                case "1":
                    map.put("level", "130");
                    break;
                case "2":
                    map.put("level", "131");
                    break;
                case "3":
                    map.put("level", "132");
                    break;
                case "4":
                    map.put("level", "133");
                    break;
                default:
                    break;
            }
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
