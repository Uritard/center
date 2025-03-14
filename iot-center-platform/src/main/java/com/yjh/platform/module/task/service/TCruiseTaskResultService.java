package com.yjh.platform.module.task.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yjh.commons.CollectionUtil;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.configuration.RedisUtil;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.CruiseCountOfType;
import com.yjh.platform.module.device.entity.CruiseOfPatrolDevice;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.controller.HelloController;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.video.service.CameraConService;
import com.yjh.platform.module.video.service.DroneCameraConService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseTaskResultService {

    @Autowired
    private TCruiseTaskResultDao tCruiseTaskResultDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;

    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;

    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Autowired
    private UPatrolResultDao uPatrolResultDao;

    @Autowired
    private UPatrolTaskService uPatrolTaskService;

    @Autowired
    private TWarnInfoDao tWarnInfoDao;

    @Resource
    private CameraConService cameraConService;

    @Resource
    private DroneCameraConService droneCameraConService;

    private Logger log = LoggerFactory.getLogger(HelloController.class);

    private final static Cache<String, List<CruiseInspectResult>> CRUISE_TIMER_CACHE = CacheUtil.newTimedCache(5*60*1000);
    private final static Cache<String, List<CruiseOfPatrolDevice>> CRUISE_PATROL_TIMER_CACHE = CacheUtil.newTimedCache(5*60*1000);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.insert(tCruiseTaskResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.deleteByPrimaryId(taskResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.update(tCruiseTaskResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.selectByPrimaryId(taskResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> select(String taskResultId, String taskId, String taskName, Integer taskAbnormal, Integer taskAlarm, String runExecute, Date cruiseTaskTime, Integer taskStatus, Integer cruiseResult, String remark) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.select(taskResultId, taskId, taskName, taskAbnormal, taskAlarm, runExecute, cruiseTaskTime, taskStatus, cruiseResult, remark);
        return tCruiseTaskResultList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> selectByPage(TCruiseTaskResult tCruiseTaskResult) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.selectByPage(tCruiseTaskResult);
        return tCruiseTaskResultList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskResult> list) {
        return this.tCruiseTaskResultDao.batchInsert(list);
    }

    public List<Map<String, Object>> selectCruiseTaskResult(String taskId, int pageNum, int pageSize) throws ParseException {
        List<Map<String, Object>> completeResult = new ArrayList<>();
        Map<String, Object> resultsMap = new HashMap<>();

        pageSize = pageSize < 20 ? 100 : pageSize;
        int start = (pageNum < 1) ? 1 : (pageNum - 1) * pageSize;

        Set<String> keyResult = uPatrolTaskService.queryTaskKeys(taskId, start, start + pageSize, true);

        List<CruiseInspectResult> inspectPageResults = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(keyResult)) {
            if (!keyResult.isEmpty()) {
                List<Map<String, String>> resultMapList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
                    keyResult.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                    return null;
                });
                for (Map<String, String> resultMap : resultMapList) {
                    CruiseInspectResult inspectResult = new CruiseInspectResult();
                    if (resultMap.size() > 0) {
                        inspectResult.setTaskId(taskId);
                        inspectResult.setCruiseResultName("--");
                        inspectResult.setEndTime(null);
                        getDataFromRedis(inspectResult, resultMap);
                        inspectPageResults.add(inspectResult);
                    }
                }
            }
            resultsMap.put("index", uPatrolTaskService.taskCount(taskId));
        } else {
            // 使用一个内部缓存，减少查询数据库压力
            List<CruiseInspectResult> cruiseInspectResults = CRUISE_TIMER_CACHE.get(taskId, ()-> uPatrolResultDao.selectCruiseInspectByTaskIdYC(taskId));
            List<CruiseInspectResult> pageResults = cruiseInspectResults.stream().skip(start).limit(pageSize).collect(Collectors.toList());

            for (CruiseInspectResult cruiseInspectResult : pageResults) {
                cruiseInspectResult.setCruiseResultName("--");
                cruiseInspectResult.setEndTime(null);

                inspectPageResults.add(cruiseInspectResult);
            }
            resultsMap.put("index", cruiseInspectResults.size());
        }
        resultsMap.put("list", inspectPageResults);

        completeResult.add(resultsMap);
        return completeResult;
    }

    private void getDataFromRedis(CruiseInspectResult inspectResult, Map<String, String> resultMap) throws ParseException {

        //最终结果集容器
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //从redis拿数据
        inspectResult.setInstanceId(Long.valueOf(resultMap.get("instanceId")));
        inspectResult.setInstanceName(resultMap.get("instanceName"));
        inspectResult.setCruiseType(Integer.valueOf(resultMap.get("cruiseType")));
        inspectResult.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", resultMap.get("cruiseType")));
        inspectResult.setDeviceName(resultMap.get("deviceName"));
        inspectResult.setCruiseStatus(DictConvertUtil.DICT.covertToDict("cruiseDataState", resultMap.get("cruiseStatus")));

        if (!CommonUtils.isEmptyOrNullstr(resultMap.get("resultDesc"))) {
            inspectResult.setCruiseResultName(resultMap.get("resultDesc"));
        }
        if (!CommonUtils.isEmptyOrNullstr(resultMap.get("cruiseTime"))) {
            inspectResult.setEndTime(sdf.parse(resultMap.get("cruiseTime")));
        }
        if (Objects.nonNull(resultMap.get("isWarn"))) {
            if ("1".equals(resultMap.get("isWarn"))) {
                inspectResult.setIsWarn("有");
            } else {
                inspectResult.setIsWarn("无");
            }
        }

        if (Objects.nonNull(resultMap.get("picpath"))) {
            inspectResult.setImagePath(resultMap.get("picpath"));
        } else {
            inspectResult.setImagePath("");
        }
        Map<String, String> videoInfo = Maps.newHashMap();
        switch (MapUtils.getString(resultMap, "cruiseType", "")) {
            //视频-红外
            case "230":
                videoInfo.put("videoCameraType", "2");
                videoInfo.put("cameraId", MapUtils.getString(resultMap, "cameraId", ""));
                break;
            //视频-可见光
            case "229":
                videoInfo.put("videoCameraType", "1");
                videoInfo.put("cameraId", MapUtils.getString(resultMap, "cameraId", ""));
                break;
            //机器人
            case "228":
                videoInfo.put("robotId", MapUtils.getString(resultMap, "robotId", ""));
                //机器人红外
                if ("2".equals(MapUtils.getString(resultMap, "fileType"))) {
                    videoInfo.put("videoCameraType", "2");
                } else {
                    //机器人可见光
                    videoInfo.put("videoCameraType", "1");
                }
                break;
            //无人机
            case "524":
                videoInfo.put("droneId", MapUtils.getString(resultMap, "robotId", ""));
                //无人机红外
                if ("2".equals(MapUtils.getString(resultMap, "fileType"))) {
                    videoInfo.put("videoCameraType", "2");
                } else {
                    //无人机可见光
                    videoInfo.put("videoCameraType", "1");
                }
                break;
            default:
                break;
        }
        inspectResult.setVideoInfo(videoInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public String cruiseCameraStop(String taskId) {
        String stopResult = "失败";
        Set<String> cruiseVideos = RedisUtil.redisScan("cruiseVideo:" + taskId);
        for (String cruiseVideo : cruiseVideos) {
            Map<String, Object> videoInfo = redisTemplate.opsForHash().entries(cruiseVideo);

//            HashMap<String, Object> camera = new HashMap<>();
//            camera.put("cameraId", Long.valueOf(videoInfo.get("cameraId").toString()));
//            camera.put("rtmpUrl", videoInfo.get("rtmpUrl").toString());
//            Result result = sendStopRequest(Constant.STOP_CAMERA_URL, camera);
            stopResult = cameraConService.stopRealPlay(Long.valueOf(videoInfo.get("cameraId").toString()), videoInfo.get("rtmpUrl").toString());
//            stopResult = result.getData().toString();
        }
        return stopResult;
    }

    public List<RealTimeWarn> realTimeWarnInfo(String taskId) {
        List<RealTimeWarn> realTimeWarns = new ArrayList<>();

        List<String> warnKeyList = redisTemplate.opsForList().range("warnInfo:" + taskId, 0, -1);
        List<String> warnKeys = CollectionUtils.isEmpty(warnKeyList) ? Collections.emptyList() : warnKeyList.stream().distinct().collect(Collectors.toList());
        List<String> defectKeyList = redisTemplate.opsForList().range("defectInfo:" + taskId, 0, -1);
        List<String> defectKeys = CollectionUtils.isEmpty(defectKeyList) ? Collections.emptyList() : defectKeyList.stream().distinct().collect(Collectors.toList());

        List<Map<String, String>> warnMapList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
            warnKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });
        if (!CollectionUtils.isEmpty(warnMapList)) {
//            List<String> instanceIds = warnMapList.stream().map(e -> e.get("instanceId")).collect(Collectors.toList());
//            List<TWarnInfo> warnInfos = tWarnInfoDao.selectCruiseInfoByTaskAndInstanceIds(taskId, instanceIds);
//            Map<String, TWarnInfo> warnInfoMap = warnInfos.stream().collect(Collectors.toMap(e -> taskId + '_' + e.getInstanceId(), Function.identity(), (a, b) -> a));
            //表计告警
            for (Map<String, String> warnMap : warnMapList) {
                String instanceId = warnMap.get("instanceId");
//                TWarnInfo tWarnInfo = warnInfoMap.get(taskId + '_' + instanceId);
                RealTimeWarn realTimeWarn = new RealTimeWarn();
                realTimeWarn.setDeviceName(warnMap.get("deviceName"));
                realTimeWarn.setInstanceName(warnMap.get("instanceName"));
                realTimeWarn.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", warnMap.get("cruiseType")));
                realTimeWarn.setWarnLevelName(DictConvertUtil.DICT.covertToDict("alarmLevel", warnMap.get("warnLevel")));
                String cruiseTime = warnMap.get("warnTime");
                if (CommonUtils.isEmptyOrNullstr(cruiseTime)){
                    realTimeWarn.setCruiseTime(new Date());
                }else {
                    realTimeWarn.setCruiseTime(DateTimeUtil.parse(cruiseTime, new Date()));
                }
                realTimeWarn.setInstanceId(NumberUtils.toLong(instanceId));
                realTimeWarn.setAlarmContent(warnMap.get("warnContent"));
                realTimeWarn.setWarnId(warnMap.get("warnId"));
                realTimeWarn.setDefectModel(warnMap.get("defectModel"));
                realTimeWarns.add(realTimeWarn);
            }
        }


        List<Map<String, String>> defectMapList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
            defectKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });
        //缺陷告警
        for (Map<String, String> defectMap : defectMapList) {
            String instanceId = defectMap.get("instanceId");
            RealTimeWarn realTimeWarn = new RealTimeWarn();
            realTimeWarn.setDeviceName(defectMap.get("deviceName"));
            realTimeWarn.setInstanceName(defectMap.get("instanceName"));
            realTimeWarn.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", defectMap.get("cruiseType")));
            realTimeWarn.setWarnLevelName(DictConvertUtil.DICT.covertToDict("alarmLevel", defectMap.get("defectLevel")));
            String cruiseTime = defectMap.get("defectTime");
            if (CommonUtils.isEmptyOrNullstr(cruiseTime)){
                realTimeWarn.setCruiseTime(new Date());
            }else {
                realTimeWarn.setCruiseTime(DateTimeUtil.parse(cruiseTime, new Date()));
            }
            realTimeWarn.setInstanceId(NumberUtils.toLong(instanceId));
            realTimeWarn.setAlarmContent(defectMap.get("defectContent"));
            realTimeWarn.setWarnId(defectMap.get("warnId"));
            realTimeWarn.setDefectModel(defectMap.get("defectModel"));
            realTimeWarns.add(realTimeWarn);
        }

        return realTimeWarns.stream().sorted((RealTimeWarn o1, RealTimeWarn o2) -> {
                    Date date1 = o1.getCruiseTime();
                    Date date2 = o2.getCruiseTime();
                    return Long.compare(date2.getTime(), date1.getTime());
                }
        ).collect(Collectors.toList());

    }


    public Map<String, Object> selectCruiseAdvance(String taskId) {

        Map<String, String> countResult = redisTemplate.opsForHash().entries(UPatrolTaskService.PATROL_SUMMARY_PREFIX + taskId);
        String rate = countResult.get("taskProgress");
        if (StringUtils.isEmpty(rate)){
            rate = "0";
        }
        log.info("执行完成点 taskId: {}, 进度: {}", taskId, rate);
        Map<String, Object> rateAndTaskInfo = new HashMap<>();
        rateAndTaskInfo.put("rate", rate);
        // TaskSimpleInfo taskSimpleInfo = uPatrolResultDao.selectTaskStateByTaskId(taskId);
        String taskState = countResult.getOrDefault("taskState", String.valueOf(CruiseConstant.TASK_STATE_EXECUTING));
        rateAndTaskInfo.put("taskState", taskState);
        rateAndTaskInfo.put("taskStateName", DictConvertUtil.DICT.covertToDict("taskState", taskState));

        try {
            CruiseResultCounter resultCounter = selectCruiseStatusCountNew(taskId);
            rateAndTaskInfo.putAll(Object2Map.objectToMap(resultCounter));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            //巡视类型个数 完成数/总数
            List<CruiseCountOfType> typeCountList = new ArrayList<>();
            //各个设备的任务进度
            List<Map<String, Object>> detailRateList = new ArrayList<>();
            //查询各个设备的对应点位
            List<CruiseOfPatrolDevice> patrolDeviceInstanceList = CRUISE_PATROL_TIMER_CACHE.get(taskId, () -> uPatrolResultDao.selectCruiseInstanceByPatrol(taskId));
            //根据巡视类型分组
            Map<Integer, List<CruiseOfPatrolDevice>> cruiseOfPatrolDeviceMap = patrolDeviceInstanceList.stream().collect(Collectors.groupingBy(CruiseOfPatrolDevice::getCruiseType));
            //从缓存中获取各个巡视类的完成数
            String subsetKey = UPatrolTaskService.PATROL_SUMMARY_PREFIX + taskId + UPatrolTaskService.SUBSET;
            Set<String> keys = new HashSet<>();
            keys.add(subsetKey);
            List<Map<String, String>> insAndCruiseList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
                keys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                return null;
            });
            Map<String, List<String>> groupedByValue = new HashMap<>();
            if (CollectionUtils.isNotEmpty(insAndCruiseList)) {
                groupedByValue = insAndCruiseList.stream()
                        .flatMap(map -> map.entrySet().stream()
                                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey())))
                        .collect(Collectors.groupingBy(
                                // 按value分组
                                Map.Entry::getKey,
                                // 收集具有相同value的所有key
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                        ));
            }
            //机器人
            List<CruiseOfPatrolDevice> robotList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.ROBOT.getCode());
            if (CollectionUtils.isNotEmpty(robotList)) {
                patrolDeviceAdvance(robotList, groupedByValue, CruiseConstant.TypeEnum.ROBOT.getDesc(), detailRateList, typeCountList);
            }
            //无人机
            List<CruiseOfPatrolDevice> droneList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.UAV.getCode());
            if (CollectionUtils.isNotEmpty(droneList)) {
                patrolDeviceAdvance(droneList, groupedByValue, CruiseConstant.TypeEnum.UAV.getDesc(), detailRateList, typeCountList);
            }
            //可见光
            List<CruiseOfPatrolDevice> videoList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.VIDEO.getCode());
            if (CollectionUtils.isNotEmpty(videoList)) {
                patrolDeviceAdvance(videoList, groupedByValue, CruiseConstant.TypeEnum.VIDEO.getDesc(), detailRateList, typeCountList);
            }
            //红外
            List<CruiseOfPatrolDevice> infraredList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.INFRARED.getCode());
            if (CollectionUtils.isNotEmpty(infraredList)) {
                patrolDeviceAdvance(infraredList, groupedByValue, CruiseConstant.TypeEnum.INFRARED.getDesc(), detailRateList, typeCountList);
            }
            //声纹
            List<CruiseOfPatrolDevice> voiceList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.VOICE.getCode());
            if (CollectionUtils.isNotEmpty(voiceList)) {
                patrolDeviceAdvance(voiceList, groupedByValue, CruiseConstant.TypeEnum.VOICE.getDesc(), detailRateList, typeCountList);
            }
            //主辅设备
            List<CruiseOfPatrolDevice> linkageList = cruiseOfPatrolDeviceMap.get(CruiseConstant.TypeEnum.ONLINE.getCode());
            if (CollectionUtils.isNotEmpty(linkageList)) {
                patrolDeviceAdvance(linkageList, groupedByValue, CruiseConstant.TypeEnum.ONLINE.getDesc(), detailRateList, typeCountList);
            }
            rateAndTaskInfo.put("typeCount", typeCountList);
            rateAndTaskInfo.put("detailRateList", detailRateList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rateAndTaskInfo;
    }

    private void patrolDeviceAdvance(List<CruiseOfPatrolDevice> list, Map<String, List<String>> groupedByValue, String typeName, List<Map<String, Object>> detailRateList, List<CruiseCountOfType> typeCountList) {
        if (typeName.equals(CruiseConstant.TypeEnum.ROBOT.getDesc()) || typeName.equals(CruiseConstant.TypeEnum.UAV.getDesc())) {
            AtomicInteger count = new AtomicInteger();
            Map<String, List<CruiseOfPatrolDevice>> robotDeviceMap = list.stream().collect(Collectors.groupingBy(CruiseOfPatrolDevice::getRobotName));
            robotDeviceMap.forEach((k, v) -> {
                List<String> robotStrList = v.stream().map(CruiseOfPatrolDevice::getInstanceId).collect(Collectors.toList());
                List<String> finish = groupedByValue.get(k);
                int robotCount = CollectionUtils.isNotEmpty(finish) ? finish.size() : 0;
                count.set(robotCount + count.get());
                Map<String, Object> map = getProgressMap(robotCount, robotStrList.size());
                map.put("deviceName", k);
                detailRateList.add(map);
            });
            typeCountList.add(getCruiseCountOfType(typeName, count.get(), list.size()));
        } else {
            List<String> insIdList = list.stream().map(CruiseOfPatrolDevice::getInstanceId).collect(Collectors.toList());
            List<String> finish = groupedByValue.get(typeName);
            int finishCount = CollectionUtils.isNotEmpty(finish) ? finish.size() : 0;
            Map<String, Object> map = getProgressMap(finishCount, insIdList.size());
            map.put("deviceName", typeName);
            detailRateList.add(map);
            typeCountList.add(getCruiseCountOfType(typeName, finishCount, insIdList.size()));
        }
    }

    private CruiseCountOfType getCruiseCountOfType(String typeName, Integer finishCount, Integer count){
        CruiseCountOfType cruiseCountOfType = new CruiseCountOfType();
        cruiseCountOfType.setCruiseTypeName(typeName);
        cruiseCountOfType.setCount(count);
        cruiseCountOfType.setFinishCount(finishCount);
        return cruiseCountOfType;
    }

    private Map<String, Object> getProgressMap(int finishCount, int all) {
        Map<String, Object> map = new HashMap<>(2);
        float progressF = (float) finishCount / all;
        String progress = CommonUtils.percentFormat(Math.min(progressF, 1.0F), "#.####");
        map.put("percentage", progress);
        return map;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CruiseResultCounter selectCruiseStatusCount(String taskId) throws ParseException {

        String cruiseExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "已执行");
        String cruiseNotExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未执行");
        String cruiseExecuteFailed = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "执行失败");
        String cruiseUnknown = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未知");

        Set<Long> deviceMete = new HashSet<>();//全部标准测点
        Set<Long> deviceMeteAbnormal = new HashSet<>();//结果异常的标准测点
        Set<Long> deviceMeteComp = new HashSet<>();//已执行的标准测点
        List<Long> deviceMeteIds = new ArrayList<>();//测点对比器
        CruiseResultCounter cruiseResultCounter = new CruiseResultCounter();
        Set<String> keyResult = uPatrolTaskService.queryTaskKeys(taskId, true);


//        Set<Long> instanceIds = tCruiseTaskAttrDao.selectInstanceIdByTask(taskId);
        Set<Long> instanceIds = uPatrolResultDao.selectInstanceIdByTask(taskId);
        for (Long instanceId : instanceIds) {
            deviceMete.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(instanceId));
            deviceMeteIds.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(instanceId));
        }


//        log.info("Set测点数量---------" + deviceMete.size());
//        log.info("List测点数量--------" + deviceMeteIds.size());
//        log.info("redisSet长度-------" + keyResult.size());
//        for(String keys:keyResult){
//            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
//            deviceMete.add(Long.valueOf(resultMap.get("device_mete_id").toString()));
//            deviceMeteIds.add(Long.valueOf(resultMap.get("device_mete_id").toString()));
//        }
        if (keyResult.size() != 0) {
            for (String keys : keyResult) {
                Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
//            Long a = Long.valueOf(resultMap.get("cruiseId").toString());
//            Long deviceMeteId=tStdDevicemeteDao.getdeviceMeteByPointinstance(a);//当前点对应的标准测点
                if (resultMap.containsKey("device_mete_id")){
                    Long deviceMeteId = NumberUtils.toLong(String.valueOf(resultMap.get("device_mete_id")));
                    //巡视点执行失败、未知状态-异常测点
                    if (resultMap.get("cruiseStatus").equals(cruiseExecuteFailed) || resultMap.get("cruiseStatus").equals(cruiseUnknown)) {
                        deviceMeteIds.remove(deviceMeteId);
                        if (!(deviceMeteIds.contains(deviceMeteId))) {
                            deviceMeteComp.add(deviceMeteId);
                        }
                        deviceMeteAbnormal.add(deviceMeteId);
                        //巡视点执行完成
                    } else if (resultMap.get("cruiseStatus").equals(cruiseExecuted)) {
                        deviceMeteIds.remove(deviceMeteId);
                        if (!(deviceMeteIds.contains(deviceMeteId))) {
                            deviceMeteComp.add(deviceMeteId);
                        }

                    }
                }
            }

            Integer cruisedNotCount = deviceMete.size() - deviceMeteComp.size();//已执行的标准测点数量
            cruiseResultCounter.setAlarmCount(deviceMeteAbnormal.size());//异常点数
            cruiseResultCounter.setCruisedCount(deviceMeteComp.size());//已执行的标准测点数量
            cruiseResultCounter.setCruiseNotCount(cruisedNotCount);//未执行点数

        } else {
            cruiseResultCounter.setAlarmCount(0);//异常点数
            cruiseResultCounter.setCruisedCount(0);//已执行的标准测点数量
            cruiseResultCounter.setCruiseNotCount(deviceMete.size());//未执行点数
        }


        //计算运行时间

        Map<String, Object> countResult = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);

        if (Objects.nonNull(countResult)) {
            //获取任务开始时间
            String startTime = countResult.get("taskStart").toString();
            //将两个时间字符串转为日期类型
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = simpleDateFormat.parse(startTime);
            String d2String = simpleDateFormat.format(new Date());
            Date d2 = simpleDateFormat.parse(d2String);
            cruiseResultCounter.setRunningTime((d2.getTime() - d1.getTime()) / (60 * 1000));
        } else {
            cruiseResultCounter.setRunningTime(Long.valueOf("0"));
        }

        log.info("总点数：" + deviceMete.size()+", 已执行点数：" + deviceMeteComp.size());

        return cruiseResultCounter;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CruiseResultCounter selectCruiseStatusCountNew(String taskId) {
        CruiseResultCounter cruiseResultCounter = new CruiseResultCounter();
        //处理上级系统逻辑
        Map<String, String> countResult = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);

        //计算运行时间
        int abnormal = MapUtils.getIntValue(countResult, "abnormal");
        int normal = MapUtils.getIntValue(countResult, "normal");

        if (MapUtils.isNotEmpty(countResult)) {
            //获取任务开始时间
            String startTime = countResult.get("taskStart");
            Integer all = ValueUtil.toInteger(countResult.get("all"),0);
            cruiseResultCounter.setAlarmCount(getAlarmCount(taskId));
            if (all > 0) {
                cruiseResultCounter.setCruisedCount(normal+abnormal);
                cruiseResultCounter.setCruiseNotCount(all - abnormal -normal);
            } else {
                cruiseResultCounter.setCruisedCount(normal);
                cruiseResultCounter.setCruiseNotCount(abnormal);
            }
            cruiseResultCounter.setTaskProgress(countResult.get("taskProgress"));
            Date d1 = DateTimeUtil.parse(startTime, new Date());
            Date d2 = new Date();
            cruiseResultCounter.setRunningTime((d2.getTime() - d1.getTime()) / (60 * 1000));
        } else {
            UPatrolResult result = uPatrolResultDao.selectByPrimaryId(taskId);
            cruiseResultCounter.setCruisedCount(result.getTaskCount());
            cruiseResultCounter.setAlarmCount(0);
            cruiseResultCounter.setCruiseNotCount(0);
            cruiseResultCounter.setRunningTime(Long.valueOf("0"));
        }
        return cruiseResultCounter;
    }

    /**
     * 获取告警数量
     *
     * @param taskId taskId
     * @return result
     */
    private int getAlarmCount(String taskId) {
        List<String> warnKeyList = redisTemplate.opsForList().range("warnInfo:" + taskId, 0, -1);
        List<String> warnKeys = CollectionUtils.isEmpty(warnKeyList) ? Collections.emptyList() : warnKeyList.stream().distinct().collect(Collectors.toList());
        List<String> defectKeyList = redisTemplate.opsForList().range("defectInfo:" + taskId, 0, -1);
        List<String> defectKeys = CollectionUtils.isEmpty(defectKeyList) ? Collections.emptyList() : defectKeyList.stream().distinct().collect(Collectors.toList());

        return warnKeys.size() + defectKeys.size();
    }



    private void getCruiseCountByCache(CruiseResultCounter cruiseResultCounter,String taskId) {
        //取出taskId对应下的所有instanceId
        Integer cruiseNotCount = 0;
        Integer cruisedCount = 0;
        Integer alarmCount = 0;
        Set<String> instanceKey = uPatrolTaskService.queryTaskKeys(taskId, true);
        log.info("查询任务[{}]下所有instance:{}", taskId, JSON.toJSONString(instanceKey));
        if (CollectionUtil.isNotEmpty(instanceKey)) {
            //遍历key，根据缓存信息判别巡视点结果
            Iterator<String> iterator = instanceKey.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Map<String, String> body = redisTemplate.opsForHash().entries(key);
                if ("246".equals(body.get("cruiseResult"))) {
                    cruisedCount ++;
                } else if ("247".equals(body.get("cruiseResult"))) {
                    cruiseNotCount++;
                }

                // 统计告警点位数
                if ("1".equals(body.get("isWarn"))) {
                    alarmCount ++;
                }
            }
        }
        //填充数据
        cruiseResultCounter.setCruisedCount(cruisedCount);
        cruiseResultCounter.setCruiseNotCount(cruiseNotCount);
        cruiseResultCounter.setAlarmCount(alarmCount);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Object> selectCruiseDeviceAndCruiseAdvance(String taskId) {

        String cruiseExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "已执行");
        String cruiseNotExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未执行");
        String cruiseExecuteFailed = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "执行失败");
        String cruiseUnknown = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未知");


        Integer cameraType = Integer.valueOf(tDictBusinessDao.selectCameraTypeAndRobotPosition("camera_type", "可见光摄像机"));//可见光摄像头TypeId
        Integer Inferad = Integer.valueOf(tDictBusinessDao.selectCameraTypeAndRobotPosition("camera_type", "红外摄像机"));//红外摄像头TypeId
        List<Object> finalResult = new ArrayList<>();//最终结果集(封装机器人、可见光、红外相机的信息)


        Set<String> cruiseKeys = uPatrolTaskService.queryTaskKeys(taskId, true);
        Set<String> robotKeys = RedisUtil.redisScan("robot_info");
        for (String robotKey : robotKeys) {
            Map<String, Object> robotInfo = redisTemplate.opsForHash().entries(robotKey);
            String taskIdTemp = taskId.toString();
            float cruisedNotCount = 0;//未执行的巡视点个数
            float cruiseCount = 0;//该巡视设别下巡视点总个数
            RobotCruiseInfo robotCruiseInfo = new RobotCruiseInfo();
            for (String cruiseKey : cruiseKeys) {
                Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
                if (taskIdTemp.equals(cruiseInfo.get("taskId"))) {
                    CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
                    if (robotInfo.get("inspectionId").equals(cruiseTypeInfo.getCruiseId().toString())) {
                        robotCruiseInfo.setElePower((robotInfo.get("elePower")).toString());
                        robotCruiseInfo.setTranSignal((robotInfo.get("tranSignal")).toString());
                        robotCruiseInfo.setRobotId(Long.valueOf((robotInfo.get("robotId").toString())));
                        robotCruiseInfo.setRobotName((robotInfo.get("robotName")).toString());
                        robotCruiseInfo.setRobotPosition(Integer.valueOf((robotInfo.get("robotPosition").toString())));
                        robotCruiseInfo.setRobotPositionName(tDictBusinessDao.selectDictNoteByDictCode((robotInfo.get("robotPosition").toString())));
                        cruiseCount = cruiseCount + 1;
                        if (cruiseInfo.get("cruiseStatus").equals(cruiseNotExecuted)) {
                            cruisedNotCount = cruisedNotCount + 1;
                        }
                    }
                }
            }
            robotCruiseInfo.setRate((cruiseCount - cruisedNotCount) / cruiseCount);//已执行=总-未执行
            if (robotCruiseInfo.getRobotId() != null) {
                finalResult.add(robotCruiseInfo);

            }

        }


        Set<String> cameraKeys = RedisUtil.redisScan("camera_info");
        float cruiseCount = 0;//总巡检点数量
        float cruisedCount = 0;//已执行数量
        float cruisedFailCount = 0;//执行失败数量
        float cruisedUnknown = 0;//未知数量
        int cameraFault = 0;//摄像头故障数量
        int cameraNotFault = 0;//可运行数量
        List<Object> cameraIds = new ArrayList<>();//配置了多个预置位的同一个摄像头,正常或故障状态的数量与摄像头数量保持一致与预置位数量无关
        CameraCruiseInfo cameraCruiseInfo = new CameraCruiseInfo();//可见光相机所有信息属性

        float cruiseCountIn = 0;
        float cruisedCountIn = 0;
        float cruisedFailCountIn = 0;
        float cruisedUnknownIn = 0;
        int cameraFaultIn = 0;
        int cameraNotFaultIn = 0;
        List<Object> cameraIdList = new ArrayList<>();
        CameraCruiseInfo inferadCruiseInfo = new CameraCruiseInfo();//红外相机所有信息属性

        for (String cameraKey : cameraKeys) {
            Map<String, Object> cameraInfo = redisTemplate.opsForHash().entries(cameraKey);
            String TaskId = taskId.toString();
            String CameraType = cameraType.toString();
            String InferadS = Inferad.toString();

            if (cameraInfo.get("cameraType").equals(CameraType)) {//从数据库或缓存中获取相机类型
                for (String cruiseKey : cruiseKeys) {
                    Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
                    if (cruiseInfo.get("taskId").equals(TaskId)) {
                        CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
                        if (cameraInfo.get("presetId").equals(cruiseTypeInfo.getCruiseId().toString())) {
//                                result.add(cameraInfo);
                            cruiseCount = cruiseCount + 1;//摄像头预置位/巡检点总数
                            if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseExecuted)) {//摄像头正常-巡检点正常
                                //巡检点已执行个数
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFault = cameraNotFault + 1;//可运行数量
                                }
                                cruisedCount = cruisedCount + 1; //巡检点已执行个数
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseExecuteFailed)) {  //摄像头正常-巡检点异常
                                //执行失败个数
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFault = cameraNotFault + 1;//可运行数量

                                }
                                cruisedFailCount = cruisedFailCount + 1;//执行失败个数
                            } else if ((!cameraInfo.get("isFault").equals("0")) && cruiseInfo.get("cruiseStatus").equals(cruiseExecuteFailed)) {  //摄像头故障-巡检点异常
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraFault = cameraFault + 1;//故障数量
                                }
                                cruisedFailCount = cruisedFailCount + 1;

                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseUnknown)) {  //摄像头正常-巡检点未知异常
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFault = cameraNotFault + 1;
                                }
                                cruisedUnknown = cruisedUnknown + 1;

                            } else if ((!cameraInfo.get("isFault").equals("0")) && cruiseInfo.get("cruiseStatus").equals(cruiseUnknown)) {   //摄像头故障-巡检点未知异常
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraFault = cameraFault + 1;
                                }
                                cruisedUnknown = cruisedUnknown + 1;

                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseNotExecuted)) {
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFault = cameraNotFault + 1;//可运行数量
                                }
                            } else if ((!cameraInfo.get("isFault").equals("0")) && cruiseInfo.get("cruiseStatus").equals(cruiseNotExecuted)) {
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraFault = cameraFault + 1;//故障数量
                                }
                            }
                            cameraIds.add(cameraInfo.get("cameraId"));
                        }

                    }
                }

            } else if (cameraInfo.get("cameraType").equals(InferadS)) {//从数据库或缓存中获取相机类型
                for (String cruiseKey : cruiseKeys) {
                    Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
                    if (cruiseInfo.get("taskId").equals(TaskId)) {
                        CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
                        if (cameraInfo.get("presetId").equals(cruiseTypeInfo.getCruiseId().toString())) {
                            cruiseCountIn = cruiseCountIn + 1;//摄像头/巡检点总数
                            if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseExecuted)) { //摄像头正常-巡检点正常
                                //巡检点已执行个数
                                if (!cameraIdList.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                                }
                                cruisedCountIn = cruisedCountIn + 1; //巡检点已执行个数
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseExecuteFailed)) {  //摄像头正常-巡检点异常
                                //执行失败个数
                                if (!cameraIdList.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                                }
                                cruisedFailCountIn = cruisedFailCountIn + 1;//执行失败个数
                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseExecuteFailed)) {  //摄像头故障-巡检点异常
                                if (!cameraIdList.contains(cameraInfo.get("cameraId"))) {
                                    cameraFaultIn = cameraFaultIn + 1;//故障数量
                                }
                                cruisedFailCountIn = cruisedFailCountIn + 1;
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseUnknown)) {  //摄像头正常-巡检点未知异常
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFaultIn = cameraNotFaultIn + 1;
                                }
                                cruisedUnknownIn = cruisedUnknownIn + 1;

                            } else if ((!cameraInfo.get("isFault").equals("0")) && cruiseInfo.get("cruiseStatus").equals(cruiseUnknown)) {   //摄像头故障-巡检点未知异常
                                if (!cameraIds.contains(cameraInfo.get("cameraId"))) {
                                    cameraFaultIn = cameraFaultIn + 1;
                                }
                                cruisedUnknownIn = cruisedUnknownIn + 1;

                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseNotExecuted)) {
                                if (!cameraIdList.contains(cameraInfo.get("cameraId"))) {
                                    cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                                }

                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals(cruiseNotExecuted)) {
                                if (!cameraIdList.contains(cameraInfo.get("cameraId"))) {
                                    cameraFaultIn = cameraFaultIn + 1;//故障数量
                                }

                            }
                            cameraIdList.add(cameraInfo.get("cameraId"));
                        }

                    }
                }

            }

        }

        //防止出现NaN错误
        if (cruiseCount == 0 && (cruisedCount + cruisedFailCount + cruisedUnknown) == 0) {
            cameraCruiseInfo.setRate(Float.valueOf("0.0"));
        } else {
            cameraCruiseInfo.setRate((cruisedCount + cruisedFailCount + cruisedUnknown) / cruiseCount);
        }
        cameraCruiseInfo.setCameraNotFault(cameraNotFault);
        cameraCruiseInfo.setCameraFault(cameraFault);
        cameraCruiseInfo.setCameraType(cameraType);
        cameraCruiseInfo.setCameraTypeName("可见光");
        finalResult.add(cameraCruiseInfo);


        if (cruiseCountIn == 0.0 && (cruisedCountIn + cruisedFailCountIn + cruisedUnknownIn) == 0.0) {
            inferadCruiseInfo.setRate(Float.valueOf("0.0"));
        } else {
            inferadCruiseInfo.setRate((cruisedCountIn + cruisedFailCountIn + cruisedUnknownIn) / cruiseCountIn);
        }
        System.out.println();
        inferadCruiseInfo.setCameraNotFault(cameraNotFaultIn);
        inferadCruiseInfo.setCameraFault(cameraFaultIn);
        inferadCruiseInfo.setCameraType(Inferad);
        inferadCruiseInfo.setCameraTypeName("红外");
        finalResult.add(inferadCruiseInfo);

        return finalResult;

    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> PictureCompare(String taskId, Long instanceId) {
        String preImg = tCameraPresetDao.selectPreImgByCruiseId(instanceId);
        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + instanceId.toString());
        String meteType = "";
        if (Objects.nonNull(redisInfoMap.get("device_mete_id"))) {
            TStdDeviceMete tStdDeviceMete = tStdDevicemeteDao.selectByPrimaryId(Long.parseLong(redisInfoMap.get("device_mete_id")));
            meteType = tStdDeviceMete.getMeteType();
        }
        Map<String, String> map = new HashMap<>(3);
        map.put("preImg", preImg);
        map.put("collectPic", redisInfoMap.get("picpath"));
        map.put("meteType", meteType);
        return map;
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cameraInfoByRedis() {

        Map<String, Object> cameraInfo = redisTemplate.opsForHash().entries("camera_info:21000000015");
        System.out.print(cameraInfo.getClass());
        return cameraInfo;
    }

    //跨发GET请求带参
    public Result sendGetRequest(String url, HashMap<String, Long> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class, params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response = new Result(500, "server error!");
        }
        return response;

    }


    public Result sendStopRequest(String url, HashMap<String, Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class, params.get("cameraId"), params.get("rtmpUrl"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;

    }
}

