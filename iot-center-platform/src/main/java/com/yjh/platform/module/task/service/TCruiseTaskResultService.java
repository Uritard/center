package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.module.task.entity.CruiseResultCounter;
import com.yjh.platform.module.task.entity.CruiseTaskRunningTime;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Logs(title = "获取当前任务的巡检点全量信息 (巡检点初始化查询)")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> selectCruiseTaskResult(Long taskId){
        //获取数据库键名列表
        Set<String> keyResult=redisScan("t_cruise_task_result*");
        //创建容纳结果信息的容器
        List<Map> dataResult=new ArrayList<>();
        for(String key:keyResult){
            Map<String,Object> resultMap=redisTemplate.opsForHash().entries(key);//循环每个键名取相应的键值对数据
            Object values=resultMap.get("taskId");//取出每条数据的key值为“taskId”的value值
            String value=values.toString();
            String TaskId=taskId.toString(); //转化成统一格式进行比较筛选
            if(TaskId.equals(value)&&resultMap.get("cruiseStatus").equals("1")){
                dataResult.add(resultMap);
            }

        }
        return dataResult;
    }


    @Logs(title = "获取当前任务的巡检点结果信息(巡检点结果)")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> selectCurrentCruiseTaskResult(Long taskId,Long cruiseId,List<Map> cruiseResult){
        //获取数据库键名列表
        Set<String> keyResult=redisScan("t_cruise_task_result*");
        //创建容纳结果信息的容器
        List<Map> dataResult=cruiseResult;
         for(String key:keyResult){
             Map<String,Object> resultMap=redisTemplate.opsForHash().entries(key);//循环每个键名取相应的键值对数据
             String value1=(resultMap.get("taskId")).toString();//取出每条数据的key值为“taskId”的value值
             String value2=(resultMap.get("cruiseId").toString());
             String TaskId=taskId.toString();
             String CruiseId=cruiseId.toString();//转化成统一格式进行比较筛选
             if(TaskId.equals(value1)&&CruiseId.equals(value2)&&resultMap.get("cruiseStatus").equals("0")){  //巡检完成数据插入结果集
                 dataResult.add(resultMap);
             }else if (TaskId.equals(value1)&&CruiseId.equals(value2)&&resultMap.get("cruiseStatus").equals("1")){ //删除对应的未完成信息
                 dataResult.remove(resultMap);
             }

         }


        return dataResult;
    }


    @Logs(title = "统计获取当前任务的执行进度")
    @Transactional(rollbackFor = Exception.class)
    public float selectCruiseAdvance(Long taskId){
        Set<String> keyResult=redisScan("t_cruise_task_result*");
        float cruiseCount=0;//总巡检点数量
        float cruiseComCount=0;//执行完成的巡检点数量
       for (String keys:keyResult){
           Map<String,Object> resultMap=redisTemplate.opsForHash().entries(keys);
           Object values=resultMap.get("taskId");//取出每条数据的key值为“taskId”的value值
           String value=values.toString();
           String TaskId=taskId.toString(); //转化成统一格式进行比较筛选
           if(TaskId.equals(value)&&resultMap.get("cruiseStatus").equals("1")){
              cruiseCount=cruiseCount+1;
           }else if (TaskId.equals(value)&&resultMap.get("cruiseStatus").equals("0")){
               cruiseComCount=cruiseComCount+1;
           }
       }
       return cruiseComCount/cruiseCount;
    }


    @Logs(title = "查询当前任务下巡检点各个状态下的数量")
    @Transactional(rollbackFor = Exception.class)
    public CruiseResultCounter selectCruiseStatusCount(Long taskId){
        Set<String>keyResult=redisScan("t_cruise_task_result*");
        CruiseResultCounter cruiseResultCounter=new CruiseResultCounter();
        Integer abnormalCount=0;
        Integer cruisedCount=0;
        Integer cruiseCount=0;
        for(String keys:keyResult){
            Map<String,Object> resultMap=redisTemplate.opsForHash().entries(keys);
            String value=(resultMap.get("taskId")).toString();
            String TaskId=taskId.toString();
            if(TaskId.equals(value)&&resultMap.get("cruiseResult").equals("1")){
                    abnormalCount=abnormalCount+1;
                    cruisedCount=cruisedCount+1;
            }else if(TaskId.equals(value)&&resultMap.get("cruiseStatus").equals("0")){
                   cruisedCount=cruisedCount+1;
            }else if (TaskId.equals(value)&&resultMap.get("cruiseStatus").equals("1")){
                cruiseCount=cruiseCount+1;
            }

            }
        Integer cruiseNotCount=cruiseCount-cruisedCount;
        cruiseResultCounter.setAbnormalCount(abnormalCount);
        cruiseResultCounter.setCruisedCount(cruisedCount);
        cruiseResultCounter.setCruiseNotCount(cruiseNotCount);

        return cruiseResultCounter;
    }

    @Logs(title = "获取当前任务已运行的时间")
    @Transactional(rollbackFor = Exception.class)
    public CruiseTaskRunningTime selectTaskRunningTime(Long taskId) throws ParseException {

        Set<String>keyResult=redisScan("t_cruise_task_result*");
        CruiseTaskRunningTime cruiseTaskRunningTime=new CruiseTaskRunningTime();//创建时间对象
        String startTime="yyyy-mm-dd HH:MM:SS";
        //获取任务开始时间
        for(String keys:keyResult){
           Map<String,Object>mapResult=redisTemplate.opsForHash().entries(keys);
            String value=(mapResult.get("taskId")).toString();
            String TaskId=taskId.toString();
            if(TaskId.equals(value)&&mapResult.get("cruiseStatus").equals("1")){
                startTime=mapResult.get("cruiseTaskTime").toString();
            }

        }
        //获取当前实时时间
        Date date=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime =simpleDateFormat.format(date);
        Date d1 =simpleDateFormat.parse(startTime);
        Date d2=simpleDateFormat.parse(nowTime);//将两个时间字符串转为日期类型
        Long RunningTime=d2.getTime()-d1.getTime();//计算时间差
        Long Day=RunningTime/(24*60*60*1000);
        Long Hour=(RunningTime/(60*60*1000)-Day*24);
        Long Minute=((RunningTime/(60*1000))-Day*24*60-Hour*60);
        Long Second=(RunningTime/1000-Day*24*60*60-Hour*60*60-Minute*60);//将时间差转化为天时分秒格式

        cruiseTaskRunningTime.setDay(Day);
        cruiseTaskRunningTime.setHour(Hour);
        cruiseTaskRunningTime.setMinute(Minute);
        cruiseTaskRunningTime.setSeconds(Second);

        return cruiseTaskRunningTime;
    }

    @Logs(title = "获取执行当前任务的机器人信息及相应巡检点结果状态")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> selectRobotAndCruiseResult(Long taskId){
        Set<String> robotKeys=redisScan("robot_info");
        List<Map> robotResult=new ArrayList<>();
        for(String key:robotKeys){
            Map<String,Object> robotInfo=redisTemplate.opsForHash().entries(key);
            String value=robotInfo.get("taskId").toString();
            String TaskId=taskId.toString();
            if(TaskId.equals(value)){
                robotResult.add(robotInfo);
            }
        }
        return robotResult;
    }
}

