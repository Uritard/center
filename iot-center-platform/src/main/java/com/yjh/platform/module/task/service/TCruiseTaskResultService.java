package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.CruiseTypeInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TStdDevice;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class TCruiseTaskResultService{

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

    @Logs(title = "Redis数据库批量查询Key值游标",code = "module")
    @Transactional(rollbackFor =Exception.class)
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

    @Logs(title = "获取当前任务的巡检点全量信息 " ,code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseInspectResult> selectCruiseTaskResult(Long taskId) throws ParseException {
        List<CruiseInspectResult> cruiseInspectResults=new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取数据库键名列表
        Set<String> keyResult=redisScan("t_cruise_task_result*");
        for(String key:keyResult){
            Map<String,Object> resultMap=redisTemplate.opsForHash().entries(key);//循环每个键名取相应的键值对数据
            Object values=resultMap.get("taskId");//取出每条数据的key值为“taskId”的value值
            String value=values.toString();
            String TaskId=taskId.toString(); //转化成统一格式进行比较筛选
            if(TaskId.equals(value)){
                CruiseInspectResult cruiseInspectResult=new CruiseInspectResult();
                //todo 增加websocket
                cruiseInspectResult.setCruiseResultName(resultMap.get("cruiseResultName").toString());//巡检结果名称
                cruiseInspectResult.setInstanceId(Long.valueOf(resultMap.get("cruiseId").toString()));//instanceId
                cruiseInspectResult.setDeviceId(Long.valueOf(resultMap.get("deviceId").toString()));//设备ID
                TStdDevice tStdDevice=stdDeviceDao.selectByPrimaryId(Long.valueOf(resultMap.get("deviceId").toString()));
                cruiseInspectResult.setDeviceName(tStdDevice.getDeviceName());//设备名称
                cruiseInspectResult.setInstanceName(tCruisePointInstanceDao.selectInstancename(Long.valueOf(resultMap.get("cruiseId").toString())));//巡检点名称
                CruiseTypeInfo cruiseTypeInfo=tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(resultMap.get("cruiseId").toString()));
                cruiseInspectResult.setCruiseType(cruiseTypeInfo.getCruiseType());//巡视方式
                cruiseInspectResult.setCruiseTypeName(cruiseTypeInfo.getCruiseTypeName());//巡视方式类型
                //Integer和Date类型判空
                if(resultMap.get("cruiseResult").equals("")&&resultMap.get("endTime").equals("")){
                    cruiseInspectResult.setCruiseResult(null);
                    cruiseInspectResult.setEndTime(null);
                }else {
                    cruiseInspectResult.setCruiseResult(Integer.valueOf(resultMap.get("cruiseResult").toString()));//巡检结果
                    cruiseInspectResult.setEndTime(simpleDateFormat.parse(resultMap.get("endTime").toString()));//巡检时间
                }

                cruiseInspectResults.add(cruiseInspectResult);
            }

        }

        return cruiseInspectResults;
    }


    @Logs(title = "统计获取当前任务的执行进度", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Float> selectCruiseAdvance(Long taskId){
        Set<String> keyResult=redisScan("t_cruise_task_result*");
        float cruiseCount=0;//总巡检点数量
        float cruiseComCount=0;//执行完成的巡检点数量
       for (String keys:keyResult){
           Map<String,Object> resultMap=redisTemplate.opsForHash().entries(keys);
           Object values=resultMap.get("taskId");//取出每条数据的key值为“taskId”的value值
           String value=values.toString();
           String TaskId=taskId.toString(); //转化成统一格式进行比较筛选
           if(TaskId.equals(value)){
              cruiseCount=cruiseCount+1;
              if(resultMap.get("cruiseStatus").equals("0")){   //执行成功
                  cruiseComCount=cruiseComCount+1;
              }else if(resultMap.get("cruiseStatus").equals("2")){  //执行失败
                  cruiseComCount=cruiseComCount+1;
              }else if(resultMap.get("cruiseStatus").equals("255")){
                  cruiseComCount=cruiseComCount+1;
              }
           }
       }
       Map<String,Float> Rate=new HashMap<>();
       Rate.put("rate",cruiseComCount/cruiseCount);
       return Rate;
    }


    @Logs(title = "查询当前任务下巡检点关联的标准测点各个状态下的数量", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public CruiseResultCounter selectCruiseStatusCount(Long taskId) throws ParseException {
        Set<String>keyResult=redisScan("t_cruise_task_result*");
        CruiseResultCounter cruiseResultCounter=new CruiseResultCounter();
        Set<Long>deviceMete=new HashSet<>();//全部标准测点
        Set<Long>deviceMeteAbnormal=new HashSet<>();//结果异常的标准测点
        Set<Long>deviceMeteNotComp=new HashSet<>();//未执行的标准测点
        for(String keys:keyResult){
            Map<String,Object> resultMap=redisTemplate.opsForHash().entries(keys);
            String value=(resultMap.get("taskId")).toString();
            String TaskId=taskId.toString();
            Long a=Long.valueOf(resultMap.get("cruiseId").toString());
            if(TaskId.equals(value)){
               deviceMete.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
               if(resultMap.get("cruiseStatus").equals("2")){
                   deviceMeteAbnormal.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
               }else if (resultMap.get("cruiseStatus").equals("1")){
                   deviceMeteNotComp.add(tStdDevicemeteDao.getdeviceMeteByPointinstance(a));
               }
            }
        }

       //计算运行时间
        Set<String>keyResult1=redisScan("TaskExcutedTime*");
        String startTime="yyyy-mm-dd HH:MM:SS";
        //获取任务开始时间
        for(String keys:keyResult1){
            Map<String,Object>mapResult=redisTemplate.opsForHash().entries(keys);

            String value=(mapResult.get("taskId")).toString();
            String TaskId=taskId.toString();
            if(TaskId.equals(value)){
                startTime=mapResult.get("excutedTime").toString();
            }

        }
        //获取当前实时时间
        Date date=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime =simpleDateFormat.format(date);
        Date d1 =simpleDateFormat.parse(startTime);
        Date d2=simpleDateFormat.parse(nowTime);//将两个时间字符串转为日期类型
        Long RunningTime=d2.getTime()-d1.getTime();//计算时间差

        Long Minute=((RunningTime/(60*1000)));
        Integer cruisedCount=deviceMete.size()-deviceMeteNotComp.size();//已执行的标准测点数量
        cruiseResultCounter.setAlarmCount(deviceMeteAbnormal.size());
        cruiseResultCounter.setCruisedCount(cruisedCount);
        cruiseResultCounter.setCruiseNotCount(deviceMeteNotComp.size());
        cruiseResultCounter.setRunningTime(Minute);

        return cruiseResultCounter;
    }


    @Logs(title = "查询当前任务下机器人/摄像头的基本信息和与之关联的巡检点执行进度", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Object> selectCruiseDeviceAndCruiseAdvance(Long taskId) {
        Log cLogger = LogFactory.getLog(this.getClass());

        Integer cameraType = Integer.valueOf(tDictBusinessDao.selectCameraTypeAndRobotPosition("camera_type", "可见光摄像机"));//可见光摄像头TypeId
        Integer Inferad = Integer.valueOf(tDictBusinessDao.selectCameraTypeAndRobotPosition("camera_type", "红外摄像机"));//红外摄像头TypeId
        List<Object> finalResult = new ArrayList<>();//最终结果集


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
                        if (cruiseInfo.get("cruiseStatus").equals("1")) {
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

        float cruiseCount = 0;
        float cruisedCount = 0;
        float cruisedFailCount = 0;
        int cameraFault = 0;
        int cameraNotFault = 0;
        CameraCruiseInfo cameraCruiseInfo = new CameraCruiseInfo();
        for (String cameraKey : cameraKeys) {
            Map<String, Object> cameraInfo = redisTemplate.opsForHash().entries(cameraKey);
            String TaskId = taskId.toString();
            String CameraType = cameraType.toString();
            if (cameraInfo.get("cameraType").equals(CameraType)) {//从数据库或缓存中获取相机类型
                for (String cruiseKey : cruiseKeys) {
                    Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
                    if (cruiseInfo.get("taskId").equals(TaskId)) {
                        CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
                        if (cameraInfo.get("presetId").equals(cruiseTypeInfo.getCruiseId().toString())) {
//                                result.add(cameraInfo);
                            cruiseCount = cruiseCount + 1;//摄像头/巡检点总数
                            if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("0")) { //摄像头正常-巡检点正常
                                cameraNotFault = cameraNotFault + 1;//可运行数量
                                cruisedCount = cruisedCount + 1; //巡检点已执行个数
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("2")) {  //摄像头正常-巡检点异常
                                cameraNotFault = cameraNotFault + 1;//可运行数量
                                cruisedFailCount = cruisedFailCount + 1;//执行失败个数
                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("2")) {  //摄像头故障-巡检点异常
                                cameraFault = cameraFault + 1;//故障数量
                                cameraFault = cameraFault + 1;//故障数量
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("1")) {
                                cameraNotFault = cameraNotFault + 1;//可运行数量
                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("1")) {
                                cameraFault = cameraFault + 1;//故障数量
                            }
                        }

                    }
                }

            }
        }
        //防止出现NaN
        if(cruiseCount==0&&(cruisedCount + cruisedFailCount)==0){
            cameraCruiseInfo.setRate(Float.valueOf("0.0"));
        }else {
            cameraCruiseInfo.setRate((cruisedCount + cruisedFailCount) / cruiseCount);
        }

        cameraCruiseInfo.setCameraNotFault(cameraNotFault);
        cameraCruiseInfo.setCameraFault(cameraFault);
        cameraCruiseInfo.setCameraType(cameraType);
        cameraCruiseInfo.setCameraTypeName("可见光");
        finalResult.add(cameraCruiseInfo);


        float cruiseCountIn = 0;
        float cruisedCountIn = 0;
        float cruisedFailCountIn = 0;
        int cameraFaultIn = 0;
        int cameraNotFaultIn = 0;
        CameraCruiseInfo inferadCruiseInfo = new CameraCruiseInfo();
        for (String cameraKey : cameraKeys) {
            Map<String, Object> cameraInfo = redisTemplate.opsForHash().entries(cameraKey);
            String TaskId = taskId.toString();
            String InferadS = Inferad.toString();
            if (cameraInfo.get("cameraType").equals(InferadS)) {//从数据库或缓存中获取相机类型
                for (String cruiseKey : cruiseKeys) {
                    Map<String, Object> cruiseInfo = redisTemplate.opsForHash().entries(cruiseKey);
                    if (cruiseInfo.get("taskId").equals(TaskId)) {
                        CruiseTypeInfo cruiseTypeInfo = tCruisePointInstanceDao.selectCruiseCommonInfoByInstanceId(Long.valueOf(cruiseInfo.get("cruiseId").toString()));
                        if (cameraInfo.get("presetId").equals(cruiseTypeInfo.getCruiseId().toString())) {
                            cruiseCountIn = cruiseCountIn + 1;//摄像头/巡检点总数
                            if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("0")) { //摄像头正常-巡检点正常
                                cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                                cruisedCountIn = cruisedCountIn + 1; //巡检点已执行个数
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("2")) {  //摄像头正常-巡检点异常
                                cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                                cruisedFailCountIn = cruisedFailCountIn + 1;//执行失败个数
                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("2")) {  //摄像头故障-巡检点异常
                                cameraFaultIn = cameraFaultIn + 1;//故障数量
                                cameraFaultIn = cameraFaultIn + 1;//故障数量
                            } else if (cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("1")) {
                                cameraNotFaultIn = cameraNotFaultIn + 1;//可运行数量
                            } else if (!cameraInfo.get("isFault").equals("0") && cruiseInfo.get("cruiseStatus").equals("1")) {
                                cameraFaultIn = cameraFaultIn + 1;//故障数量
                            }
                        }

                    }
                }

            }
        }
        if(cruiseCountIn==0.0&&(cruisedCountIn + cruisedFailCountIn)==0.0){
            inferadCruiseInfo.setRate(Float.valueOf("0.0"));
        }else {
            inferadCruiseInfo.setRate((cruisedCountIn + cruisedFailCountIn) / cruiseCountIn);
        }
        inferadCruiseInfo.setCameraNotFault(cameraNotFaultIn);
        inferadCruiseInfo.setCameraFault(cameraFaultIn);
        inferadCruiseInfo.setCameraType(Inferad);
        inferadCruiseInfo.setCameraTypeName("红外");


        finalResult.add(inferadCruiseInfo);


        return finalResult;

    }




    @Logs(title = "图片比较", code = "module")//结果集仍需优化、只获取了摄像头原始图片
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> PictureCompare(Long taskId, Long instanceId){
//     List<String> pictureResult=new ArrayList<>();
     String collectPic=null;
     String preImg=tCameraPresetDao.selectPreImgByCruiseId(instanceId);
     Set<String>cruiseKeys=redisScan("t_cruise_task_result*");
     for(String key:cruiseKeys){
         Map<String,Object>cruiseInfo=redisTemplate.opsForHash().entries(key);
         String TaskId=taskId.toString();
         String InstanceId=instanceId.toString();
         if(cruiseInfo.get("taskId").equals(TaskId)&&cruiseInfo.get("cruiseId").equals(InstanceId)){
             Object collectedPic=cruiseInfo.get("colletcPic");
             collectPic=collectedPic.toString();
         }
     }
     Map<String,String> map=new HashMap<>();
     map.put("preImg",preImg);
     map.put("collectPic",collectPic);
//     pictureResult.add(preImg);
//     pictureResult.add(collectPic);

     return map;
    }

    @Logs(title = "获取机器人巡视画面", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CameraOfRobotInfo> selectRobotScreen(Long taskId){
         List<CameraOfRobotInfo> cameraOfRobotInfos=tRobotInfoDao.selectRobotScreen(taskId);
         return cameraOfRobotInfos;
    }
}

