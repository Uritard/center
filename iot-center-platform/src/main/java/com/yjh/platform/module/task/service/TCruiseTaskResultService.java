package com.yjh.platform.module.task.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.commons.CollectionUtil;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.CruiseCountOfType;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.controller.HelloController;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
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
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_ABNORMAL;
import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_NORMAL;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

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
    private TWarnInfoDao tWarnInfoDao;

    private Logger log = LoggerFactory.getLogger(HelloController.class);

    private final static Cache<String, List<CruiseInspectResult>> CRUISE_TIMER_CACHE = CacheUtil.newTimedCache(15*60*1000);
    private final static Cache<String, List<CruiseCountOfType>> CRUISE_COUNT_TIMER_CACHE = CacheUtil.newTimedCache(6*60*60*1000);

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

    @Transactional(rollbackFor = Exception.class)
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match(key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectCruiseTaskResult(String taskId, int pageNum, int pageSize) throws ParseException {
        List<Map<String, Object>> completeResult = new ArrayList<>();
        Map<String, Object> resultsMap = new HashMap<>();

        // 使用一个内部缓存，减少查询数据库压力
        List<CruiseInspectResult> cruiseInspectResults = CRUISE_TIMER_CACHE.get(taskId, ()-> uPatrolResultDao.selectCruiseInspectByTaskIdYC(taskId));

        pageSize = pageSize < 20 ? 100 : pageSize;
        int start = (pageNum < 1) ? 1 : (pageNum - 1) * pageSize;

        List<CruiseInspectResult> inspectPageResults = new ArrayList<>();
        if (CollectionUtils.isEmpty(cruiseInspectResults)){
            Set<String> keyResult = redisScan(UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":");
            if (keyResult.size() != 0) {
                List<String> pageKeys = keyResult.stream().skip(start).limit(pageSize).collect(Collectors.toList());
                for (String keys : pageKeys) {
                    Map<String, String> resultMap = redisTemplate.opsForHash().entries(keys);
                    CruiseInspectResult inspectResult = new CruiseInspectResult();
                    if (resultMap.size() > 0) {
                        inspectResult.setTaskId(taskId);
                        inspectResult.setCruiseResultName("--");
                        inspectResult.setEndTime(null);
                        getDataFromRedis(inspectResult, resultMap);
                        inspectPageResults.add(inspectResult);
                    }
                    //最新的巡视点在之前List的位置(查询发生产生结果点地索引)
                    // int index = cruiseInspectResults.indexOf(inspectResult);
                    // if (index == -1) {
                    //     index = 0;
                    // }
                    // resultsMap.put("index", index);
                    // resultsMap.put("list", cruiseInspectResults);
                }
            }
            resultsMap.put("index", keyResult.size());
        } else {
            List<CruiseInspectResult> pageResults = cruiseInspectResults.stream().skip(start).limit(pageSize).collect(Collectors.toList());
            for (CruiseInspectResult cruiseInspectResult : pageResults) {
                cruiseInspectResult.setCruiseResultName("--");
                cruiseInspectResult.setEndTime(null);
                String instanceId = String.valueOf(cruiseInspectResult.getInstanceId());
                // 测试数据，正式使用需删除
//                instanceId = StringUtils.left(instanceId, 3);
                String key = UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                Map<String, String> resultMap = redisTemplate.opsForHash().entries(key);
                if (resultMap.size() > 0) {
                    getDataFromRedis(cruiseInspectResult, resultMap);
                    inspectPageResults.add(cruiseInspectResult);
                }
                //最新的巡视点在之前List的位置(查询发生产生结果点地索引)
                // int index = cruiseInspectResults.indexOf(cruiseInspectResult);
                // if (index == -1) {
                //     index = 0;
                // }
                // resultsMap.put("index", index);
                // resultsMap.put("list", cruiseInspectResults);
            }
            resultsMap.put("index", cruiseInspectResults.size());
        }
        resultsMap.put("list", inspectPageResults);

        completeResult.add(resultsMap);
        return completeResult;
    }

    private void getDataFromRedis(CruiseInspectResult inspectResult, Map<String, String> resultMap) throws ParseException {
        if (Constant.logUpLv3()) {
            log.info("getDataFromRedis 方法入参：inspectResult：{}， resultMap：{}", JSONUtil.toJSONString(inspectResult), JSONUtil.toJSONString(resultMap));
        }

        //最终结果集容器
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //从redis拿数据
        inspectResult.setInstanceId(Long.valueOf(resultMap.get("instanceId")));
        inspectResult.setInstanceName(resultMap.get("instanceName"));
        inspectResult.setCruiseType(Integer.valueOf(resultMap.get("cruiseType")));
        inspectResult.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", resultMap.get("cruiseType")));
        inspectResult.setDeviceName(resultMap.get("deviceName"));
        inspectResult.setCruiseStatus(DictConvertUtil.DICT.covertToDict("cruiseDataState", resultMap.get("cruiseStatus")));
        if ("".equals(resultMap.get("resultNum")) || "null".equals(resultMap.get("resultNum"))) {
            inspectResult.setCruiseResultName("--");
        } else {
//            inspectResult.setCruiseResultName(resultMap.get("resultNum"));
            inspectResult.setCruiseResultName(resultMap.get("resultDesc"));
        }
        if ("null".equals(resultMap.get("cruiseTime")) || "".equals(resultMap.get("cruiseTime"))) {
            inspectResult.setEndTime(null);
        } else {
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
        Map<String, String> videoInfo = new HashMap<>();
        if ("230".equals(resultMap.containsKey("cruiseType") ?
            String.valueOf(resultMap.get("cruiseType")) : "")) {
            videoInfo.put("videoCameraType", "2");
        } else if ("524".equals(resultMap.containsKey("cruiseType") ?
            String.valueOf(resultMap.get("cruiseType")) : "")) {
            // 无人机视频流红外和可见光为一路流，需要额外处理
            videoInfo.put("videoCameraType", "3");
        } else {
            videoInfo.put("videoCameraType", "1");
        }
        if (!Constant.fastTurbo()) {
            String videoPrefixKey = "videoInfo:";
            if (!CommonUtils.isEmptyOrNullstr(resultMap.get("robotId"))) {
                String robotId = resultMap.get("robotId");
                String cacheKey = videoPrefixKey + "robot:" + robotId;
                boolean getInRedis = getVideoFromRedis(cacheKey, videoInfo, inspectResult);
                if (getInRedis) {
                    return;
                }
                //判断当前机器人巡视点的采集设备为 红外或可见光
                String runningCameraFlag = inspectResult.getSaveTypeList();
                if (Objects.nonNull(runningCameraFlag) && runningCameraFlag.equals("fir")) {
                    videoInfo.put("videoCameraType", "2");
                }
                HashMap<String, Long> robot = new HashMap<>();
                robot.put("robotId", Long.valueOf(resultMap.get("robotId")));

                log.info("switch (videoInfo.get(\"videoCameraType\")), 参数：{}", JSONUtil.toJSONString(videoInfo));
                switch (videoInfo.get("videoCameraType")) {
                    case "1":

                        Result result = sendGetRequest(Constant.START_ROBOT_CAMERA_URL, robot);
                        List<Map<String, String>> robotVideoInfo = (List<Map<String, String>>)result.getData();
                        if (CollectionUtils.isNotEmpty(robotVideoInfo)) {
                            videoInfo.putAll(robotVideoInfo.get(0));
                        } else {
                            videoInfo.put("flvUrl", "null");
                            videoInfo.put("rtmpUrl", "null");
                            videoInfo.put("webRtcUrl", "null");
                        }
                        videoInfo.put("cameraId", robot.get("robotId").toString());
                        inspectResult.setVideoInfo(videoInfo);
                        break;
                    case "2":
                        Result result2 = sendGetRequest(Constant.START_ROBOT_CAMERA_URL, robot);
                        List<Map<String, String>> robotInfraredVideoInfo = (List<Map<String, String>>)result2.getData();
                        if (CollectionUtils.isNotEmpty(robotInfraredVideoInfo)) {
                            videoInfo.putAll(robotInfraredVideoInfo.get(1));
                        } else {
                            videoInfo.put("flvUrl", "null");
                            videoInfo.put("rtmpUrl", "null");
                            videoInfo.put("webRtcUrl", "null");
                        }
                        videoInfo.put("cameraId", robot.get("robotId").toString());
                        inspectResult.setVideoInfo(videoInfo);
                        break;
                    case "3":
                        // 获取无人机视频流
                        Map<String, String> map = getDroneVideoInfo(robotId);
                        if (map != null && map.size() == 3) {
                            videoInfo.put("flvUrl", map.get("flvUrl"));
                            videoInfo.put("rtmpUrl", map.get("rtmpUrlInferad"));
                            videoInfo.put("webRtcUrl", map.get("webRtcUrl"));
                        } else {
                            videoInfo.put("flvUrl", "null");
                            videoInfo.put("rtmpUrl", "null");
                            videoInfo.put("webRtcUrl", "null");
                        }

                        videoInfo.put("cameraId", robot.get("robotId").toString());
                        inspectResult.setVideoInfo(videoInfo);
                        break;
                    default:
                        break;
                }

                log.info("视频流信息存入redis， cacheKey: {}, videoInfo: {}", cacheKey, JSONUtil.toJSONString(videoInfo));

                try {
                    redisTemplate.opsForHash().putAll(cacheKey, videoInfo);
                    redisTemplate.expire(cacheKey, 60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("redisTemplate.opsForHash().putAll(cacheKey, videoInfo) err", e);
                }
            }

            //拉机器人的红外和可见光的视频流
            if (!CommonUtils.isEmptyOrNullstr(resultMap.get("cameraId"))) {
                String camera = resultMap.get("cameraId");
                String cacheKey = videoPrefixKey + "camera:" + camera;
                boolean getInRedis = getVideoFromRedis(cacheKey, videoInfo, inspectResult);
                if (getInRedis) {
                    return;
                }
                HashMap<String, Long> cameraId = new HashMap<>();
                cameraId.put("cameraId", Long.valueOf(camera));
                Result result = sendGetRequest(Constant.START_CAMERA_URL, cameraId);
                Map<String, String> cameraVideoInfo = (Map<String, String>)result.getData();
                if (MapUtils.isNotEmpty(cameraVideoInfo)) {
                    videoInfo.putAll(cameraVideoInfo);
                } else {
                    videoInfo.put("flvUrl", null);
                    videoInfo.put("rtmpUrl", null);
                    videoInfo.put("webRtcUrl", null);
                }
                inspectResult.setVideoInfo(videoInfo);

                redisTemplate.opsForHash().putAll(cacheKey, videoInfo);
                redisTemplate.expire(cacheKey, 60, TimeUnit.SECONDS);
            }
        }
    }

    private Map<String, String> getDroneVideoInfo(String robotId) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            Result re = serviceRestTemplate.getForObject(Constant.DRONE_VIDEO, Result.class, robotId);
            return ((List<Map<String, String>>)re.getData()).get(0);
        } catch (Exception e) {
            log.error("getDroneVideoInfo err, ", e);
            return null;
        }
    }

    private synchronized boolean getVideoFromRedis(String key, Map<String, String> videoInfo, CruiseInspectResult inspectResult){
        Map<String, String> videoRedis = redisTemplate.opsForHash().entries(key);
        if(MapUtils.isEmpty(videoRedis)) {
            return false;
        }
        videoInfo.putAll(videoRedis);
        inspectResult.setVideoInfo(videoInfo);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public String cruiseCameraStop(String taskId) {
        String stopResult = "失败";
        Set<String> cruiseVideos = redisScan("cruiseVideo:" + taskId);
        for (String cruiseVideo : cruiseVideos) {
            Map<String, Object> videoInfo = redisTemplate.opsForHash().entries(cruiseVideo);

            HashMap<String, Object> camera = new HashMap<>();
            camera.put("cameraId", Long.valueOf(videoInfo.get("cameraId").toString()));
            camera.put("rtmpUrl", videoInfo.get("rtmpUrl").toString());
            Result result = sendStopRequest(Constant.STOP_CAMERA_URL, camera);
            stopResult = result.getData().toString();
        }
        return stopResult;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<RealTimeWarn> realTimeWarnInfo(String taskId) throws ParseException {
        List<RealTimeWarn> realTimeWarns = new ArrayList<>();

        Set<String> warnKeys = redisScan("warnInfo:" + taskId);
        Set<String> defectKeys = redisScan("defectInfo:" + taskId);
        //表计告警
        for (String warnKey : warnKeys) {
            Map<String, String> warnMap = redisTemplate.opsForHash().entries(warnKey);
            String instanceId = warnMap.get("instanceId");
            Map<String, String> cruiseMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
//            log.info("cruiseMap==={}", cruiseMap);
            if (MapUtils.isEmpty(cruiseMap)) {
                log.info("realTimeWarnInfo get cruiseMap empty, warnKey: {}", warnKey);
                break;
            }
            RealTimeWarn realTimeWarn = new RealTimeWarn();
            realTimeWarn.setDeviceName(cruiseMap.get("deviceName"));
            realTimeWarn.setInstanceName(cruiseMap.get("instanceName"));
            realTimeWarn.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", cruiseMap.get("cruiseType")));
            realTimeWarn.setWarnLevelName(DictConvertUtil.DICT.covertToDict("alarmLevel", warnMap.get("warnLevel")));
            String cruiseTime = cruiseMap.get("cruiseTime");
            if (CommonUtils.isEmptyOrNullstr(cruiseTime)){
                realTimeWarn.setCruiseTime(new Date());
            }else {
                realTimeWarn.setCruiseTime(DateTimeUtil.parse(cruiseTime));
            }
            realTimeWarn.setInstanceId(NumberUtils.toLong(cruiseMap.get("instanceId")));
            realTimeWarn.setAlarmContent(warnMap.get("warnContent"));
            realTimeWarns.add(realTimeWarn);
        }

        //缺陷告警
        for (String defectKey : defectKeys) {
            Map<String, String> defectMap = redisTemplate.opsForHash().entries(defectKey);
            String instanceId = defectMap.get("instanceId");
            Map<String, String> cruiseMap = redisTemplate.opsForHash().entries(UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + instanceId);
//            log.info("cruiseMap==={}", cruiseMap);
            RealTimeWarn realTimeWarn = new RealTimeWarn();
            realTimeWarn.setDeviceName(cruiseMap.get("deviceName"));
            realTimeWarn.setInstanceName(cruiseMap.get("instanceName"));
            realTimeWarn.setCruiseTypeName(DictConvertUtil.DICT.covertToDict("cruiseType", cruiseMap.get("cruiseType")));
            realTimeWarn.setWarnLevelName(DictConvertUtil.DICT.covertToDict("alarmLevel", defectMap.get("defectLevel")));
            String cruiseTime = cruiseMap.get("cruiseTime");
            if (CommonUtils.isEmptyOrNullstr(cruiseTime)){
                realTimeWarn.setCruiseTime(new Date());
            }else {
                realTimeWarn.setCruiseTime(DateTimeUtil.parse(cruiseTime));
            }
            realTimeWarn.setInstanceId(NumberUtils.toLong(cruiseMap.get("instanceId")));
            realTimeWarn.setAlarmContent(defectMap.get("defectContent"));
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
            List<CruiseCountOfType> typeCountList = CRUISE_COUNT_TIMER_CACHE.get(taskId, ()-> uPatrolResultDao.selectCruiseCountByType(taskId));
            rateAndTaskInfo.put("typeCount", typeCountList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return rateAndTaskInfo;
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
        Set<String> keyResult = redisScan(UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":");


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
        if (Constant.isUpSystem()) {
            //处理巡视点数量信息
            getCruiseCountByCache(cruiseResultCounter, taskId);
            Map<String, Object> countResult = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);

            if (!countResult.isEmpty()) {
                //获取任务开始时间
                String startTime = countResult.get("taskStart").toString();
                //将两个时间字符串转为日期类型
                Date d1 = DateTimeUtil.parse(startTime);
                Date d2 = new Date();
                cruiseResultCounter.setRunningTime((d2.getTime() - d1.getTime()) / (60 * 1000));
            } else {
                cruiseResultCounter.setRunningTime(Long.valueOf("0"));
            }
             return cruiseResultCounter;

        }

        //计算运行时间
        int abnormalCounts = 0;
        int normalCounts = 0;

        Set<String> robotInfoKeys = redisScan(PATROL_TASK_PREFIX + taskId + ":");
        for (String key : robotInfoKeys) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            boolean conditionRes = ArrayUtils.contains(new String[]{String.valueOf(CRUISE_RESULT_NORMAL), String.valueOf(CRUISE_RESULT_ABNORMAL)}, redisInfoMap.get("cruiseResult"));
            if (conditionRes && Objects.equals(String.valueOf(CRUISE_RESULT_ABNORMAL), redisInfoMap.get("cruiseResult"))) {
                abnormalCounts++;
            } else if (conditionRes && Objects.equals(String.valueOf(CRUISE_RESULT_NORMAL), redisInfoMap.get("cruiseResult"))){
                normalCounts++;
            }
        }
        Map<String, String> countResult = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);

        if (MapUtils.isNotEmpty(countResult)) {
            //获取任务开始时间
            String startTime = countResult.get("taskStart");
            Integer all = ValueUtil.toInteger(countResult.get("all"),0);
            Integer normal = ValueUtil.toInteger(normalCounts, 0);
            Integer abnormal = ValueUtil.toInteger(abnormalCounts,0);
            cruiseResultCounter.setAlarmCount(getAlarmCount(taskId));
            cruiseResultCounter.setCruisedCount(normal+abnormal);
            cruiseResultCounter.setCruiseNotCount(all - abnormal -normal);
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
        int alarmCount = 0;

        try {
            Set<String> warnKeys = redisScan("warnInfo:" + taskId);
            //表计告警
            for (String warnKey : warnKeys) {
                Map<String, String> warnMap = redisTemplate.opsForHash().entries(warnKey);
                String instanceId = warnMap.get("instanceId");
                Map<String, String> cruiseMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
                if (!MapUtils.isEmpty(cruiseMap)) {
                    alarmCount++;
                }
            }

            Set<String> defectKeys = redisScan("defectInfo:" + taskId);
            alarmCount += defectKeys.size();
        } catch (Exception e) {
            log.error("getAlarmCount err: ", e);
        }

        return alarmCount;
    }



    private void getCruiseCountByCache(CruiseResultCounter cruiseResultCounter,String taskId) {
        //取出taskId对应下的所有instanceId
        Integer cruiseNotCount = 0;
        Integer cruisedCount = 0;
        Integer alarmCount = 0;
        Set<String> instanceKey = redisTemplate.keys(PATROL_TASK_PREFIX + taskId +":*");
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


        Set<String> cruiseKeys = redisScan(UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":");
        Set<String> robotKeys = redisScan("robot_info*");
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


        Set<String> cameraKeys = redisScan("camera_info*");
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
        Set<String> cruiseKeys = redisScan(UPatrolTaskService.PATROL_TASK_PREFIX+"*");
        for (String cruiseKey : cruiseKeys) {
            Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
            CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
            cruiseTypeInfo.getCruiseId().toString();

        }
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

