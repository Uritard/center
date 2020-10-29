package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.task.controller.HelloController;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.CameraInfo;
import com.yjh.platform.module.user.entity.CameraOfRobotInfo;
import com.yjh.platform.module.task.entity.CruiseInspectResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger log = LoggerFactory.getLogger(HelloController.class);
    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.insert(tCruiseTaskResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.deleteByPrimaryId(taskResultId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskResult tCruiseTaskResult) {
        return this.tCruiseTaskResultDao.update(tCruiseTaskResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseTaskResultDao.selectByPrimaryId(taskResultId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> select(String taskResultId, String taskId, Integer taskAbnormal, Integer taskAlarm, String runExecute, Date cruiseTaskTime, Integer taskStatus, Integer cruiseResult, String remark) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.select(taskResultId, taskId, taskAbnormal, taskAlarm, runExecute, cruiseTaskTime, taskStatus, cruiseResult, remark);
        return tCruiseTaskResultList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResult> selectByPage(TCruiseTaskResult tCruiseTaskResult) {
        List<TCruiseTaskResult> tCruiseTaskResultList = tCruiseTaskResultDao.selectByPage(tCruiseTaskResult);
        return tCruiseTaskResultList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskResult> list) {
        return this.tCruiseTaskResultDao.batchInsert(list);
    }

    @Logs(title = "Redis数据库批量查询Key值游标", code = "module")
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

    @Logs(title = "获取当前任务的巡检点全量信息 ", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseInspectResult> selectCruiseTaskResult(String taskId) throws ParseException {
        List<CruiseInspectResult> cruiseInspectResults = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取数据库键名列表
        Set<String> keyResult = redisScan("t_cruise_task_result*");
        for (String key : keyResult) {
            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(key);//循环每个键名取相应的键值对数据

            String value = resultMap.get("taskId").toString();//取出每条数据的key值为“taskId”的value值
            String TaskId = taskId.toString(); //转化成统一格式进行比较筛选
            if (TaskId.equals(value)) {
                CruiseInspectResult cruiseInspectResult = new CruiseInspectResult();
//                cruiseInspectResult.setCruiseResultName(resultMap.get("cruiseResultName").toString());//巡检结果名称
                cruiseInspectResult.setInstanceId(Long.valueOf(resultMap.get("cruiseId").toString()));//instanceId
                cruiseInspectResult.setDeviceId(Long.valueOf(resultMap.get("deviceId").toString()));//设备ID
                TStdDevice tStdDevice = stdDeviceDao.selectByPrimaryId(Long.valueOf(resultMap.get("deviceId").toString()));
                if(tStdDevice.equals(null)){
                    cruiseInspectResult.setDeviceName("");
                }else {
                    cruiseInspectResult.setDeviceName(tStdDevice.getDeviceName());//设备名称
                }
                // instanceName为表中的cruiseName
                if(tCruisePointInstanceDao.selectInstancename(Long.valueOf(resultMap.get("cruiseId").toString())).equals(null)){
                    cruiseInspectResult.setInstanceName("");
                }else {
                    cruiseInspectResult.setInstanceName(tCruisePointInstanceDao.selectInstancename(Long.valueOf(resultMap.get("cruiseId").toString())));//巡检点名称
                }
                CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(resultMap.get("cruiseId").toString()));
                if(cruiseTypeInfo.equals(null)){
                    cruiseInspectResult.setCruiseType(null);
                    cruiseInspectResult.setCruiseTypeName(null);
                }else {
                    cruiseInspectResult.setCruiseType(cruiseTypeInfo.getCruiseType());//巡视方式
                    cruiseInspectResult.setCruiseTypeName(cruiseTypeInfo.getCruiseTypeName());//巡视方式类型
                }
                cruiseInspectResult.setCruiseResultName(resultMap.get("resultNum").toString());//巡检结果
                //Integer和Date类型判空
                if (resultMap.get("endTime").equals("")) {
                    cruiseInspectResult.setEndTime(null);
                } else {
//                    // TODO: 2020/10/9 巡视结果是否为该巡视点完成后采集到的数据
                    cruiseInspectResult.setEndTime(simpleDateFormat.parse(resultMap.get("endTime").toString()));//巡检时间
                }

                cruiseInspectResults.add(cruiseInspectResult);
            }

        }
        return cruiseInspectResults;
    }


    @Logs(title = "统计获取当前任务的执行进度", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Float> selectCruiseAdvance(String taskId) {
        Set<String> keyResult = redisScan("t_cruise_task_result*");

        String cruiseExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "已执行");
        String cruiseNotExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未执行");
        String cruiseExecuteFailed = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "执行失败");
        String cruiseUnknown = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未知");

        float cruiseCount = 0;//总巡检点数量
        float cruiseComCount = 0;//执行完成的巡检点数量
        for (String keys : keyResult) {
            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
            Object values = resultMap.get("taskId");//取出每条数据的key值为“taskId”的value值
            String value = values.toString();
            String TaskId = taskId.toString(); //转化成统一格式进行比较筛选
            if (TaskId.equals(value)) {
                cruiseCount = cruiseCount + 1;
                if (resultMap.get("cruiseStatus").equals(cruiseExecuted)) {   //执行成功
                    cruiseComCount = cruiseComCount + 1;
                } else if (resultMap.get("cruiseStatus").equals(cruiseExecuteFailed)) {  //执行失败
                    cruiseComCount = cruiseComCount + 1;
                } else if (resultMap.get("cruiseStatus").equals(cruiseUnknown)) { //未知
                    cruiseComCount = cruiseComCount + 1;
                }
            }
        }
        Map<String, Float> Rate = new HashMap<>();
        if(cruiseComCount==0 || cruiseComCount==0){
            Rate.put("rate",Float.valueOf("0"));
        }else {
            Rate.put("rate", cruiseComCount / cruiseCount);
        }
        return Rate;
    }


    @Logs(title = "查询当前任务下巡检点关联的标准测点各个状态下的数量", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public CruiseResultCounter selectCruiseStatusCount(String taskId) throws ParseException {

        String cruiseExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "已执行");
        String cruiseNotExecuted = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未执行");
        String cruiseExecuteFailed = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "执行失败");
        String cruiseUnknown = tDictBusinessDao.selectCameraTypeAndRobotPosition("cruise_data_state", "未知");

        Set<String> keyResult = redisScan("t_cruise_task_result*");
        CruiseResultCounter cruiseResultCounter = new CruiseResultCounter();
        Set<Long> deviceMete = new HashSet<>();//全部标准测点
        Set<Long> deviceMeteAbnormal = new HashSet<>();//结果异常的标准测点
        Set<Long> deviceMeteNotComp = new HashSet<>();//未执行的标准测点
        for (String keys : keyResult) {
            Map<String, Object> resultMap = redisTemplate.opsForHash().entries(keys);
            String value = (resultMap.get("taskId")).toString();
            String TaskId = taskId.toString();
            Long a = Long.valueOf(resultMap.get("cruiseId").toString());
            if (TaskId.equals(value)) {
                if(tStdDevicemeteDao.getdeviceMeteByPointinstance(a).equals(null)){
                    deviceMete.add(null);
                }else {
                    deviceMete.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
                }
                if (resultMap.get("cruiseStatus").equals(cruiseExecuteFailed) || resultMap.get("cruiseStatus").equals(cruiseUnknown)) {
                    if(tStdDevicemeteDao.getdeviceMeteByPointinstance(a).equals(null)){
                        deviceMeteAbnormal.add(null);
                    }else {
                        deviceMeteAbnormal.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
                    }
                } else if (resultMap.get("cruiseStatus").equals(cruiseNotExecuted)) {
                    if(tStdDevicemeteDao.getdeviceMeteByPointinstance(a).equals(null)){
                        deviceMeteNotComp.add(null);
                    }else {
                        deviceMeteNotComp.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
                    }
                }
            }
        }

        //计算运行时间
        Set<String> keyResult1 = redisScan("t_cruise_task_result*");
        //获取任务开始时间
        for (String keys : keyResult1) {
            Map<String, Object> mapResult = redisTemplate.opsForHash().entries(keys);

            String value = mapResult.get("taskId").toString();
            if (taskId.equals(value)) {
                String startTime = mapResult.get("startTime").toString();
                //将两个时间字符串转为日期类型
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d1 = simpleDateFormat.parse(startTime);
                Date d2 = new Date("yyyy-MM-dd HH:mm:ss");
                cruiseResultCounter.setRunningTime((d2.getTime() - d1.getTime()) / (60 * 1000));
            }
        }

        Integer cruisedCount = deviceMete.size() - deviceMeteNotComp.size();//已执行的标准测点数量
        cruiseResultCounter.setAlarmCount(deviceMeteAbnormal.size());
        cruiseResultCounter.setCruisedCount(cruisedCount);
        cruiseResultCounter.setCruiseNotCount(deviceMeteNotComp.size());


        return cruiseResultCounter;
    }


    @Logs(title = "查询当前任务下机器人/摄像头的基本信息和与之关联的巡检点执行进度", code = "module")
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


    @Logs(title = "图片比较", code = "module")//结果集仍需优化、只获取了摄像头原始图片
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> PictureCompare(String taskId, Long instanceId) {
//     List<String> pictureResult=new ArrayList<>();
        String collectPic = null;
        String preImg = tCameraPresetDao.selectPreImgByCruiseId(instanceId);
        System.out.println(preImg);
        Set<String> cruiseKeys = redisScan("t_cruise_task_result*");
        for (String key : cruiseKeys) {
            Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(key);
            String TaskId = taskId;
            String InstanceId = instanceId.toString();
            if (cruiseInfo.get("taskId").equals(TaskId) && cruiseInfo.get("cruiseId").equals(InstanceId)) {
                Object collectedPic = cruiseInfo.get("picpath");
                collectPic = collectedPic.toString();
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("preImg", preImg);
        map.put("collectPic", collectPic);
        return map;
    }

    @Logs(title = "获取机器人巡视画面", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CameraOfRobotInfo> selectRobotScreen(String taskId) {
        List<CameraOfRobotInfo> cameraOfRobotInfos = tRobotInfoDao.selectRobotScreen(taskId);
        return cameraOfRobotInfos;
    }

    @Logs(title = "获取摄像头缓存信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cameraInfoByRedis(){
        Set<String> cruiseKeys = redisScan("t_cruise_task_result*");
        for(String cruiseKey:cruiseKeys){
            Map<String,Object>cruiseInfo=redisTemplate.opsForHash().entries(cruiseKey);
            CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
            cruiseTypeInfo.getCruiseId().toString();

        }
        Map<String,Object> cameraInfo=redisTemplate.opsForHash().entries("camera_info:21000000015");
        System.out.print(cameraInfo.getClass());
        return  cameraInfo;
    }

}

