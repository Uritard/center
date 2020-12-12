package com.yjh.accessrobot.netty.server;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YC
 * @date 2020/12/8 9:58
 */
@lombok.extern.slf4j.Slf4j
public class CruiseResultDealThread implements Runnable{

    private static final String DATETIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATTPL);

//    @Autowired
//    private RobotService robotService;
//    @Autowired
    private RedisTemplate redisTemplate;

    private Map<String,String> cruiseResultMap;

    public CruiseResultDealThread(Map<String,String> cruiseResultMap,RedisTemplate redisTemplate){
        this.cruiseResultMap = cruiseResultMap;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(){
        try {
            log.info("处理巡视结果的线程进来了！！！！！！！！！！！！！！");
            log.info("传进来的cruiseResultMap是==="+cruiseResultMap);
            //根据taskId查询相关内容

            String taskId = cruiseResultMap.get("taskCode");
            log.info("taskId是==="+taskId);
           TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);//获取任务
            log.info("tCruiseTask是==="+tCruiseTask);
            String taskResultId = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
            log.info("taskResultId是==="+taskResultId);

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+cruiseResultMap.get("robotCode"));

            Map<String,String> tCruiseTaskResultMap = new HashMap<>();

            String str = null;

            for (String key : robotInfoKeys){
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);//读缓存
                if (cruiseResultMap.get("robotCode").equals(redisInfoMap.get("robotCode"))
                        && cruiseResultMap.get("taskCode").equals(redisInfoMap.get("taskId"))
                        &&cruiseResultMap.get("deviceId").equals(redisInfoMap.get("inspectionCode"))) {
                    String instanceId = redisInfoMap.get("instanceId");
                    String cruiseTime = redisInfoMap.get("cruiseTime");
                    log.info("该deviceId对应的instanceId是===" + instanceId);

                    str = "t_cruise_task_result:"+tCruiseTask.getTaskId() + instanceId;//redis缓存名称

                    tCruiseTaskResultMap.put("instanceId",instanceId);
                    tCruiseTaskResultMap.put("cruiseResultId",taskResultId + instanceId);
                    tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));
                    tCruiseTaskResultMap.put("cruiseId",instanceId);
                    tCruiseTaskResultMap.put("cruiseTaskTime",cruiseTime);

                }
            }

            Map<String,String> map = redisTemplate.opsForHash().entries("countForAbnormal:"+taskId);
            tCruiseTaskResultMap.put("startTime",map.get("taskStart"));

            tCruiseTaskResultMap.put("taskResultId",taskResultId);
            tCruiseTaskResultMap.put("endTime",cruiseResultMap.get("time"));
            tCruiseTaskResultMap.put("cruiseStatus","252");

            tCruiseTaskResultMap.put("cruiseType","228");
            tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("valueUnit"));
            if (cruiseResultMap.get("fileType").equals("1") || cruiseResultMap.get("fileType").equals("2")){
                tCruiseTaskResultMap.put("picpath",cruiseResultMap.get("relativePath"));
            }
//
            tCruiseTaskResultMap.put("origpic",cruiseResultMap.get("absolutePath"));
//            tCruiseTaskResultMap.put("cruiseResult",);
            tCruiseTaskResultMap.put("evaluationState","257");
            tCruiseTaskResultMap.put("createtime",sdf.format(new Date()));
//            tCruiseTaskResultMap.put("is_warn",);

            tCruiseTaskResultMap.put("taskId",taskId);
//                    tCruiseTaskResultMap.put("task_abnormal",);//做完后
            tCruiseTaskResultMap.put("runExecute",tCruiseTask.getIfRun().toString());

            if(tCruiseTask.getAreaId() == null){
                tCruiseTaskResultMap.put("areaId","null");
            }else {
                tCruiseTaskResultMap.put("areaId",tCruiseTask.getAreaId());
            }
            tCruiseTaskResultMap.put("cType",tCruiseTask.getType().toString());
//                    tCruiseTaskResultMap.put("cState",);
            tCruiseTaskResultMap.put("taskCount",robotInfoKeys.size() + "");
//                    tCruiseTaskResultMap.put("taskWait",robotInfoKeys.size() - normal - abnormal);
            tCruiseTaskResultMap.put("taskCode",taskId);
            log.info("tCruiseTaskResultMap是==="+tCruiseTaskResultMap);


            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);//塞进缓存

            //读任务状态缓存
//            Map<String, String> taskStatusMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode"));
//            String taskStatus = taskStatusMap.get("taskState");
//            log.info("当前任务的状态是==="+taskStatus);

            Integer abnormal = 1;
            Integer normal = 2;

//            判断任务执行情况：暂停、终止、完成、超期状态,批量插入TCDR和TCTRD、更新TCR和TCTR
            Map<String, String> taskStatusMap = redisTemplate.opsForHash().entries("taskStatusRedis:"+cruiseResultMap.get("robotCode")
                    +":"+cruiseResultMap.get("taskCode"));
            String taskStatus = taskStatusMap.get("taskStatus");
            log.info("当前任务的状态是==="+taskStatus);

            //统计机器人返回任务结果的大小
            List<String> resultList = new ArrayList<>();
            Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
            for (String key : cruiseKey) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                resultList.add(redisInfoMap.get("instanceId"));
            }
            log.info("resultList的大小===="+resultList.size());

            //统计巡视主机下发给机器人的巡检点大小
            Map<String, String> redisInfoMap2 = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode"));
            String instanceList = (String)redisInfoMap2.get("instanceIdList");
            instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
            String[] instanceIdList = instanceList.split(", ");
            log.info("instanceIdList的大小===="+instanceIdList.length);

            //比较任务相关时间(开始时间，超期时间)
            Map<String, String> redisInfoMap3 = redisTemplate.opsForHash().entries("countForAbnormal:"+cruiseResultMap.get("taskCode"));
            String taskStart = redisInfoMap3.get("taskStart");//开始时间
            Integer overDay = Integer.valueOf(redisInfoMap3.get("overDay"));//超期时间

            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("开始的日期："+df.format(df.parse(taskStart)));
            String overDayTime = df.format(new Date(df.parse(taskStart).getTime() + overDay * 24 * 60 * 60 * 1000));
            log.info("overDay天后的日期：" + overDayTime );
            log.info("是否超期==="+(df.parse(overDayTime).getTime() > new Date().getTime()));

            if (taskStatus.equals("2") || taskStatus.equals("4")
                    || instanceIdList.length == resultList.size() ||
                    df.parse(overDayTime).getTime() > new Date().getTime()){
                log.info("任务执行暂停/终止/完成/超期！！！");

                //更新TCR
                //更新TCTR
                List<TCruiseDataResult> tCDRList = new ArrayList<>();//巡检点数据表tCDRList
                List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();//巡检点状态详细表tCTRDList
                Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + taskId);
                for (String key : cruiseKeys) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);

                    TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
                            .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                            .setTaskResultId(redisInfoMap.get("taskResultId"))
                            .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
                            .setCruiseTime(sdf.parse(redisInfoMap.get("cruiseTime")))
                            .setEndTime(sdf.parse(redisInfoMap.get("endTime")))
                            .setCruiseStatus(252);//252.已执行253.未执行254.执行失败255.未知
//                    log.info("TCTRD的内容==="+tCruiseTaskResultDetail);
                    tCTRDList.add(tCruiseTaskResultDetail);

                    TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
                            .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                            .setCruiseId(Long.valueOf(redisInfoMap.get("instanceId")))
                            .setCruiseType(228)//机器人
//                            .setCruiseResult()//246.正常247.异常
//                            .setCruiseAbnormal()//248.抓图失败249.数据异常250.异常警告251算法超时
                            .setResultNum(redisInfoMap.get("resultNum"))
                            .setPicpath(redisInfoMap.get("picpath"))
                            .setOrigpic(redisInfoMap.get("origpic"))
//                            .setIsWarn(1)
                            .setEvaluationState(257)
                            .setCreatetime(new Date());
//                    log.info("TCDR的内容是==="+tCruiseDataResult);
                    tCDRList.add(tCruiseDataResult);

                }
                log.info("tCTRDList的内容是==="+tCTRDList);
                log.info("tCDRList的内容是==="+tCDRList);
                /*//批量插入TCTRD库
                int res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tCTRDList);//批量插tCTRDList
                log.info("res1的内容是==="+res1);
                //批量插入TCDR库
                int res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tCDRList);//批量插tCDRList
                log.info("res2的内容是==="+res2);*/

                //判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表

                //读异常点缓存表巡检点
               String strForCountAbnormal = "countForAbnormal:" + taskId;
                Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

                Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
                //机器人异常点+缓存中的异常点
                Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString()) + abnormal;
                log.info("总异常点数是==="+abnormalCheckPoint);
                //机器人正常点+缓存中的正常点
                Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString()) + normal;
                log.info("总正常点数是==="+normalCheckPoint);

                /*if (abnormalCheckPoint + normalCheckPoint == totalCheckPoint){//机器人任务是最后一个

                    Thread.sleep(15000);

                    TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                            .setTaskId(tCruiseTaskResultMap.get("taskId"))
                            .setTaskAlarm(0)
                            .setTaskAbnormal(abnormal)
                            .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
                    log.info("tCruiseTaskResult的内容是==="+tCruiseTaskResult);
                    //更新TCTR表
                    StaticContextAccessor.getBean(RobotService.class).updateTCruiseTaskResult(tCruiseTaskResult);

                    Integer taskWait = totalCheckPoint - tCDRList.size();
                    log.info("待测点数是==="+taskWait);
                    Integer cState  = null;
                    if (taskStatus.equals("2")){
                        cState = 241;//暂停
                    } else if (taskStatus.equals("4")){
                        cState = 242;//终止
                    }else if (instanceIdList.length == resultList.size()){
                        cState = 240;//完成
                    }else if (df.parse(overDayTime).getTime() > new Date().getTime()){
                        cState = 244;//超期
                    }
                    TCruiseResult tCruiseResult = new TCruiseResult()
                            .setTaskWait(taskWait)//待测点数
                            .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"))
                            .setCState(cState);//任务状态
                    log.info("tCruiseResult的内容是==="+tCruiseResult);
                    //更新TCR表
                    StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);

                    // webSocket通知前端调用巡视监控的接口（任务完成）
                    *//*Map<String, Object> jasonMap = new HashMap<>();
                    jasonMap.put("type", "lastOneInstance");
                    jasonMap.put("taskId",taskId);
                    String json = JSON.toJSONString(jasonMap);
                    log.info("发送给前端的消息：" + json);
                    WebSocketServer.sendMsg(json);*//*

                }else{//不是最后一个
                    Map<String, String> mapForAbnormal = new HashMap<>();
                    mapForAbnormal.put("abnormal", abnormalCheckPoint.toString());
                    mapForAbnormal.put("normal", normalCheckPoint.toString());
                    //更新异常点缓存的数据
                    redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
                }*/
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    //Redis数据库批量查询Key值游标
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
}
