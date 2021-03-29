package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.task.controller.HelloController;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;
import com.yjh.platform.common.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.task.entity.CruiseInspectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

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
    private TStdDeviceDao stdDeviceDao;

    @Autowired
    private TDictBusinessDao tDictBusinessDao;

    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;

    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

    @Autowired
    private TCruiseResultDao tCruiseResultDao;

    private Logger log = LoggerFactory.getLogger(HelloController.class);

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
            scanParams.match("*" + key + "*");
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
    public List<Map<String, Object>> selectCruiseTaskResult(String taskId) throws ParseException {
        //最终结果集容器
        List<Map<String, Object>> completeResult = new ArrayList<>();
        Map<String, Object> resultsMap = new HashMap<>();

        CruiseInspectResult inspectResult = new CruiseInspectResult();

        List<CruiseInspectResult> cruiseInspectResults = tCruiseTaskDao.selectCruiseInspectByTaskId(taskId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (CruiseInspectResult cruiseInspectResult : cruiseInspectResults) {
            List<TStdDevice> tStdDevice = stdDeviceDao.selectByPrimaryId(cruiseInspectResult.getDeviceId());
            if (tStdDevice.size()==0) {
                cruiseInspectResult.setDeviceName("");
            } else {
                cruiseInspectResult.setDeviceName(tStdDevice.get(0).getDeviceName());
            }
            cruiseInspectResult.setInstanceName(tCruisePointInstanceDao.selectInstanceName(cruiseInspectResult.getInstanceId()));
            cruiseInspectResult.setCruiseResultName("--");
            cruiseInspectResult.setEndTime(null);
            //log.info("离谱");
            Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + taskId);
            String key = "t_cruise_task_result:" + taskId + ":" + cruiseInspectResult.getInstanceId().toString();

//            for (String key : cruiseKeys) {
            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(key);
            log.info("------------------MAP-----------------" + resultsMap.size());

            if (resultMap.size() != 0) {
                //log.info("---___---:" + resultMap);
//                if (resultMap.get("instanceId").toString().equals(cruiseInspectResult.getInstanceId().toString())) {
                cruiseInspectResult.setCruiseStatus(tDictBusinessDao.selectDictNoteByDictCode(resultMap.get("cruiseStatus").toString()));
                if (resultMap.get("resultNum").toString().equals("") || resultMap.get("resultNum").toString().equals("null")) {
                    cruiseInspectResult.setCruiseResultName("--");
                } else {
                    cruiseInspectResult.setCruiseResultName(resultMap.get("resultNum").toString());
                }
                if (resultMap.get("cruiseTime").equals("null") || resultMap.get("cruiseTime").toString().equals("")) {
                    cruiseInspectResult.setEndTime(null);
                } else {
                    cruiseInspectResult.setEndTime(simpleDateFormat.parse(resultMap.get("cruiseTime").toString()));
                }
                if (Objects.nonNull(resultMap.get("isWarn"))) {
                    if (resultMap.get("isWarn").toString().equals("1")) {
                        cruiseInspectResult.setIsWarn("有");
                    } else {
                        cruiseInspectResult.setIsWarn("无");
                    }
                }
                if (Objects.nonNull(resultMap.get("picpath"))) {
                    cruiseInspectResult.setImagePath(resultMap.get("picpath").toString());
                } else {
                    cruiseInspectResult.setImagePath("");
                }

                Map<String, String> videoInfo = new HashMap<>();
                log.info("redis-cameraId-------:"+resultMap.get("cameraId"));
                if (Objects.nonNull(resultMap.get("cameraId")) && !(resultMap.get("cameraId").toString().equals(""))) {
                    if (redisTemplate.opsForHash().entries("cruiseVideo:" + taskId + (cruiseInspectResult.getInstanceId()).toString()).size() == 0) {
                        HashMap<String, Long> camera = new HashMap<>();
                        camera.put("cameraId", Long.valueOf(resultMap.get("cameraId").toString()));
                        try {
                            Result result = sendGetRequest(Constant.START_CAMERA_URL, camera);
                            videoInfo.putAll((Map<String, String>) result.getData());
                            log.info("result：" + result);
                        } catch (Exception e) {
                            log.error("播放失败：" + e);
                            videoInfo.put("flvUrl", "null");
                            videoInfo.put("rtmpUrl", "null");
                        } finally {
                            videoInfo.put("cameraId", resultMap.get("cameraId").toString());
                            redisTemplate.opsForHash().putAll("cruiseVideo:" + taskId + (cruiseInspectResult.getInstanceId()).toString(), videoInfo);
                        }
                    } else {
                        Map<String, Object> cruiseVideoInfo = redisTemplate.opsForHash().entries("cruiseVideo:" + taskId + (cruiseInspectResult.getInstanceId()).toString());
                        videoInfo.put("cameraId", cruiseVideoInfo.get("cameraId").toString());
                        if(Objects.nonNull(cruiseVideoInfo.get("flvUrl"))){
                            videoInfo.put("flvUrl", cruiseVideoInfo.get("flvUrl").toString());
                        }else {
                            videoInfo.put("flvUrl",null);
                        }
                       if(Objects.nonNull(cruiseVideoInfo.get("rtmpUrl"))){
                           videoInfo.put("rtmpUrl", cruiseVideoInfo.get("rtmpUrl").toString());
                       }else {
                           videoInfo.put("rtmpUrl",null);
                       }


                    }
                    cruiseInspectResult.setVideoInfo(videoInfo);

                } else if (Objects.nonNull(resultMap.get("robotId"))) {
                    HashMap<String, Long> robot = new HashMap<>();
                    robot.put("robotId", tRobotInfoDao.selectRobotScreen(Long.valueOf(resultMap.get("instanceId").toString())));
                    Result result = sendGetRequest(Constant.START_ROBOT_CAMERA_URL, robot);
                    log.info("robot-VideoINfo:"+result);
                    List<Map<String, String>> robotVideoInfo=(List<Map<String, String>>)result.getData();
                    videoInfo.putAll(robotVideoInfo.get(0));
                    videoInfo.put("cameraId", null);
                    cruiseInspectResult.setVideoInfo(videoInfo);
                } else {

                }
                inspectResult = cruiseInspectResult;
//                }
//            }

//            Long instanceCount = tCruiseResultDao.cruiseInspectCount(taskId).get(3);
//            List<String> imageArray = new ArrayList<>();
//            for (int i = 0; i < instanceCount; i++) {
//                imageArray.add(null);
//            }
//            log.info("instanceIds:"+instanceCount);
//            log.info("Array*****"+imageArray);
//            if(cruiseKeys.size() !=0){
//                int i=0;
//                for(String key:cruiseKeys){
//                    log.info("tem:"+i);
//                    Map<String,Object>cruiseMap=redisTemplate.opsForHash().entries(key);
//                    String imagePath=cruiseMap.get("picpath").toString();
//                    imageArray.set(i,imagePath);
//                    i++;
//
//                }
//                log.info("Array2077*****"+imageArray);
//                resultsMap.put("imageArray",imageArray);
//            }

            }
        }


//        //获取排序前的结果list
//        List<CruiseInspectResult> temList = new ArrayList<>();
//        for (CruiseInspectResult temC : cruiseInspectResults) {
//            temList.add(temC);
//        }


//        //按时间降序排列
//        Collections.sort(cruiseInspectResults, new Comparator<CruiseInspectResult>() {
//            @Override
//            public int compare(CruiseInspectResult o1, CruiseInspectResult o2) {
//                if (Objects.isNull(o1.getEndTime()) || Objects.isNull(o2.getEndTime())) {
//                    int flag = 1;
//                    return flag;
//                } else {
//                    int flag = o1.getEndTime().compareTo(o2.getEndTime());
//                    if (flag == -1) {
//                        flag = 1;
//                    } else if (flag == 1) {
//                        flag = -1;
//                    }
//                    return flag;
//                }
//
//            }
//        });

        //最新的巡视点在之前List的位置(查询发生产生结果点地索引)
        Integer index = cruiseInspectResults.indexOf(inspectResult);
        if(index==-1){
            index=0;
        }
        resultsMap.put("index", index);
        resultsMap.put("list", cruiseInspectResults);

        log.info("currentItem!!!!!!!!!!!!!"+inspectResult);
        log.info("list!!!!!!!!!!!!!!!!!!"+cruiseInspectResults);

        completeResult.add(resultsMap);
        return completeResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public String cruiseCameraStop(String taskId){
        String stopResult="失败";
         Set<String> cruiseVideos=redisScan("cruiseVideo:"+taskId);
         for(String cruiseVideo:cruiseVideos){
             Map<String,Object>videoInfo=redisTemplate.opsForHash().entries(cruiseVideo);

             HashMap<String, Object> camera = new HashMap<>();
             camera.put("cameraId", Long.valueOf(videoInfo.get("cameraId").toString()));
             camera.put("rtmpUrl",videoInfo.get("rtmpUrl").toString());
             Result result = sendStopRequest(Constant.STOP_CAMERA_URL, camera);
             stopResult=result.getData().toString();
         }
       return stopResult;
    }



    @Transactional(rollbackFor = Exception.class)
    public List<RealTimeWarn> realTimeWarnInfo(String taskId) throws ParseException {
        List<RealTimeWarn> realTimeWarns = new ArrayList<>();

        Set<String> warnKeys = redisScan("warnInfo:" + taskId);
        Set<String> defectKeys=redisScan("defectInfo:"+ taskId);
        //表计告警
        for (String warnKey : warnKeys) {
            Map<String, Object> warnMap = redisTemplate.opsForHash().entries(warnKey);
            String instanceId = warnMap.get("instanceId").toString();
            Map<String, Object> cruiseMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            RealTimeWarn realTimeWarn = new RealTimeWarn();
            realTimeWarn.setDeviceName(cruiseMap.get("deviceName").toString());
            realTimeWarn.setInstanceName(cruiseMap.get("instanceName").toString());
            realTimeWarn.setCruiseTypeName(tDictBusinessDao.selectDictNoteByDictCode(cruiseMap.get("cruiseType").toString()));
            realTimeWarn.setWarnLevelName(tDictBusinessDao.selectDictNoteByDictCode(warnMap.get("warnLevel").toString()));
            realTimeWarn.setCruiseTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cruiseMap.get("cruiseTime").toString()));
            realTimeWarn.setInstanceId(Long.valueOf(cruiseMap.get("instanceId").toString()));
            realTimeWarn.setAlarmContent(warnMap.get("warnContent").toString());
            realTimeWarns.add(realTimeWarn);
        }

        //缺陷告警
        for(String defectKey:defectKeys){
            Map<String,Object> defectMap=redisTemplate.opsForHash().entries(defectKey);
            String instanceId = defectMap.get("instanceId").toString();
            Map<String, Object> cruiseMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            RealTimeWarn realTimeWarn = new RealTimeWarn();
            realTimeWarn.setDeviceName(cruiseMap.get("deviceName").toString());
            realTimeWarn.setInstanceName(cruiseMap.get("instanceName").toString());
            realTimeWarn.setCruiseTypeName(tDictBusinessDao.selectDictNoteByDictCode(cruiseMap.get("cruiseType").toString()));
            realTimeWarn.setWarnLevelName(tDictBusinessDao.selectDictNoteByDictCode(defectMap.get("defectLevel").toString()));
            realTimeWarn.setCruiseTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cruiseMap.get("cruiseTime").toString()));
            realTimeWarn.setInstanceId(Long.valueOf(cruiseMap.get("instanceId").toString()));
            realTimeWarn.setAlarmContent(defectMap.get("defectContent").toString());
            realTimeWarns.add(realTimeWarn);
        }

        return realTimeWarns;

    }

//    @Transactional(rollbackFor = Exception.class)
//    public List<String> selectImagePosition(String taskId) {
//        List<String> imageArray = new ArrayList<>();
//        Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + taskId);
//        Long instanceCount = tCruiseResultDao.cruiseInspectCount(taskId).get(3);
//
//        for (int i = 0; i < instanceCount; i++) {
//            imageArray.add(null);
//        }
//        if(cruiseKeys.size() !=0){
//            int i=0;
//            for(String key:cruiseKeys){
//                Map<String,Object>cruiseMap=redisTemplate.opsForHash().entries(key);
//                String imagePath=cruiseMap.get("picpath").toString();
//                imageArray.set(i,imagePath);
//                i++;
//
//            }
//        }
//
//
//        return imageArray;
//    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectCruiseAdvance(String taskId) {
        Set<String> keyResult = redisScan("t_cruise_task_result:" + taskId);

        String cruiseExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "已执行");
        String cruiseNotExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未执行");
        String cruiseExecuteFailed = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "执行失败");
        String cruiseUnknown = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未知");

        Long cruiseCount = tCruiseTaskResultDao.selectCruiseCountsByTaskId(taskId);//总巡检点数量
        float cruiseComCount = 0;//执行完成的巡检点数量

        for (String cruiseKey : keyResult) {
            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(cruiseKey);
            if (!(resultMap.get("cruiseStatus").equals(cruiseNotExecuted))) {
                cruiseComCount = cruiseComCount + 1;
            }
        }
        log.info("执行完成点：" + cruiseComCount + "个");
        Map<String, Object> rateAndTaskInfo = new HashMap<>();
        if (cruiseCount == 0 || cruiseComCount == 0) {
            rateAndTaskInfo.put("rate", Float.valueOf("0"));
        } else {
            rateAndTaskInfo.put("rate", cruiseComCount / cruiseCount);
        }
        rateAndTaskInfo.put("taskState", tCruiseTaskResultDao.selectTaskStateByTaskId(taskId).getTaskState());
        rateAndTaskInfo.put("taskStateName", tCruiseTaskResultDao.selectTaskStateByTaskId(taskId).getTaskStateName());
        return rateAndTaskInfo;
    }


    @Transactional(rollbackFor = Exception.class)
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
        Set<String> keyResult = redisScan("t_cruise_task_result:" + taskId);




            Set<Long> instanceIds = tCruiseTaskAttrDao.selectInstanceIdByTask(taskId);
            for (Long instanceId : instanceIds) {
                deviceMete.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(instanceId));
                deviceMeteIds.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(instanceId));
            }


            log.info("Set测点数量---------"+deviceMete.size());
            log.info("List测点数量--------"+deviceMeteIds.size());
            log.info("redisSet长度-------"+keyResult.size());
//        for(String keys:keyResult){
//            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
//            deviceMete.add(Long.valueOf(resultMap.get("device_mete_id").toString()));
//            deviceMeteIds.add(Long.valueOf(resultMap.get("device_mete_id").toString()));
//        }
        if(keyResult.size()!=0) {
            for (String keys : keyResult) {
                Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
//            Long a = Long.valueOf(resultMap.get("cruiseId").toString());
//            Long deviceMeteId=tStdDevicemeteDao.getdeviceMeteByPointinstance(a);//当前点对应的标准测点
                Long deviceMeteId = Long.valueOf(resultMap.get("device_mete_id").toString());
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

            Integer cruisedNotCount = deviceMete.size() - deviceMeteComp.size();//已执行的标准测点数量
            cruiseResultCounter.setAlarmCount(deviceMeteAbnormal.size());//异常点数
            cruiseResultCounter.setCruisedCount(deviceMeteComp.size());//已执行的标准测点数量
            cruiseResultCounter.setCruiseNotCount(cruisedNotCount);//未执行点数

        }else {
            cruiseResultCounter.setAlarmCount(0);//异常点数
            cruiseResultCounter.setCruisedCount(0);//已执行的标准测点数量
            cruiseResultCounter.setCruiseNotCount(deviceMete.size());//未执行点数
        }


        //计算运行时间

            Map<String, Object> countResult = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);

        if(Objects.nonNull(countResult)) {
            //获取任务开始时间
            String startTime = countResult.get("taskStart").toString();
            //将两个时间字符串转为日期类型
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = simpleDateFormat.parse(startTime);
            String d2String = simpleDateFormat.format(new Date());
            Date d2 = simpleDateFormat.parse(d2String);
            cruiseResultCounter.setRunningTime((d2.getTime() - d1.getTime()) / (60 * 1000));
        }else {
            cruiseResultCounter.setRunningTime(Long.valueOf("0"));
        }




        log.info("总点数：" + deviceMete.size());
        log.info("已执行点数：" + deviceMeteComp.size());


        return cruiseResultCounter;
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


        Set<String> cruiseKeys = redisScan("t_cruise_task_result*");
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
        String collectPic = null;
        String preImg = tCameraPresetDao.selectPreImgByCruiseId(instanceId);
        System.out.println(preImg);
        Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + taskId);
        for (String key : cruiseKeys) {
            Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(key);
            String InstanceId = instanceId.toString();
            if (cruiseInfo.get("cruiseId").equals(InstanceId)) {
                Object collectedPic = cruiseInfo.get("picpath");
                collectPic = collectedPic.toString();
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("preImg", preImg);
        map.put("collectPic", collectPic);
        return map;
    }


    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cameraInfoByRedis() {
        Set<String> cruiseKeys = redisScan("t_cruise_task_result*");
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
        }
        return response;

    }


    public Result sendStopRequest(String url, HashMap<String, Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class, params.get("cameraId"),params.get("rtmpUrl"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;

    }
}

