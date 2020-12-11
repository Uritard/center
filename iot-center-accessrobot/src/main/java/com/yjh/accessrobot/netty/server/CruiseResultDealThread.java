package com.yjh.accessrobot.netty.server;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.TCruiseDataResult;
import com.yjh.accessrobot.module.command.entity.TCruiseTask;
import com.yjh.accessrobot.module.command.entity.TCruiseTaskResultDetail;
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
//            tCruiseTaskResultMap.put("state",);
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
            Thread.sleep(5000);

            Map<String, String> taskStatusMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode"));
            String taskStatus = taskStatusMap.get("taskState");
            log.info("当前任务的状态是==="+taskStatus);

            Integer abnormal = 0;
            Integer normal = 0;
//            判断任务执行情况：暂停、终止、完成、超期状态,批量插入TCDR和TCTRD、更新TCR和TCTR
            if (taskStatus.equals("1") || taskStatus.equals("3")
                    || taskStatus.equals("4") || taskStatus.equals("6")){
                log.info("任务执行完成/暂停/终止/超期！！！");

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
                            .setResultNum(redisInfoMap.get("resultNum"))
                            .setPicpath(redisInfoMap.get("picpath"))
                            .setOrigpic(redisInfoMap.get("origpic"))
//                            .setState(999)
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
               /*String strForCountAbnormal = "countForAbnormal:" + taskId;
                Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

                Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
                Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString()) + abnormal;
                Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString()) + normal;

                if (abnormalCheckPoint + normalCheckPoint == totalCheckPoint){//机器人任务是最后一个

                    Thread.sleep(15000);

                    TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                            .setTaskAbnormal(abnormal);
                    log.info("tCruiseTaskResult的内容是==="+tCruiseTaskResult);
                    //更新TCTR表
                    StaticContextAccessor.getBean(RobotService.class).updateTCruiseTaskResult(tCruiseTaskResult);

                    Integer taskWait = tCruiseResult.getTaskWait()-1;
                    TCruiseResult tCruiseResult = new TCruiseResult()
                            .setTaskWait(taskWait)//待测点数
                            .setCState(1);//任务状态
                    log.info("tCruiseResult的内容是==="+tCruiseResult);
                    //更新TCR表
                    StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);

                    // webSocket通知前端调用巡视监控的接口（任务完成）
                    Map<String, Object> jasonMap = new HashMap<>();
                    jasonMap.put("type", "lastOneInstance");
                    jasonMap.put("taskId",taskId);
                    String json = JSON.toJSONString(jasonMap);
                    log.info("发送给前端的消息：" + json);
                    WebSocketServer.sendMsg(json);

                }else{//不是最后一个

                    Map<String, String> mapForAbnormal = new HashMap<>();
                    Integer totAbnormal = 1;
                    Integer totNormal = 1;
                    mapForAbnormal.put("abnormal", totAbnormal.toString());
                    mapForAbnormal.put("normal", totNormal.toString());
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
