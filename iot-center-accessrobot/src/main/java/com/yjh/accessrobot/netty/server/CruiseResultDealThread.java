package com.yjh.accessrobot.netty.server;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.common.websocket.WebSocketServer;
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

            List<TCruiseDataResult> tCDRList = new ArrayList<>();//巡检点数据表tCDRList
            List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();//巡检点状态详细表tCTRDList

            String taskId = cruiseResultMap.get("taskCode");

            //读异常点缓存表巡检点
            String strForCountAbnormal = "countForAbnormal:" + taskId;
            Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

            //缓存中的总检测点
            Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
            log.info("总检测点数时==="+totalCheckPoint);
            //缓存中的异常点
            Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
            log.info("总异常点数是===" + abnormalCheckPoint);
            //缓存中的正常点
            Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString()) ;
            log.info("总正常点数是===" + normalCheckPoint);
            //总待测点数
            Integer taskWait = totalCheckPoint - abnormalCheckPoint - normalCheckPoint;
            log.info("待测点数是==="+taskWait);

            Integer abnormal = abnormalCheckPoint;//异常
            Integer normal = normalCheckPoint;//正常

            //根据taskId查询相关内容
            log.info("taskId是==="+taskId);
            TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);//获取任务
            log.info("tCruiseTask是==="+tCruiseTask);
            TCruiseResult tCruiseResult = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
            log.info("taskResultId是==="+tCruiseResult.getTaskResultId());

             //读缓存
            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+cruiseResultMap.get("robotCode"));

            Map<String,String> tCruiseTaskResultMap = new HashMap<>();

            String str = null;
            //遍历在下任务时提前组装好的巡检点信息
            for (String key : robotInfoKeys){
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                if (cruiseResultMap.get("robotCode").equals(redisInfoMap.get("robotCode"))
                        && cruiseResultMap.get("taskCode").equals(redisInfoMap.get("taskId"))
                        &&cruiseResultMap.get("deviceId").equals(redisInfoMap.get("inspectionCode"))) {
                    String instanceId = redisInfoMap.get("instanceId");
                    String cruiseTime = redisInfoMap.get("cruiseTime");
                    log.info("该deviceId对应的instanceId是===" + instanceId);

                    str = "t_cruise_task_result:"+tCruiseTask.getTaskId() + instanceId;//redis缓存名称

                    tCruiseTaskResultMap.put("instanceId",instanceId);
                    tCruiseTaskResultMap.put("cruiseResultId",tCruiseResult.getTaskResultId() + instanceId);
                    tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));
                    tCruiseTaskResultMap.put("cruiseId",instanceId);
                    tCruiseTaskResultMap.put("cruiseTaskTime",cruiseTime);

                    Map<String,String> map = redisTemplate.opsForHash().entries("countForAbnormal:"+taskId);
                    tCruiseTaskResultMap.put("startTime",map.get("taskStart"));

                    tCruiseTaskResultMap.put("taskResultId",tCruiseResult.getTaskResultId());
                    tCruiseTaskResultMap.put("endTime",cruiseResultMap.get("time"));
                    tCruiseTaskResultMap.put("cruiseStatus","252");

                    tCruiseTaskResultMap.put("cruiseType","228");


                    if (!"".equals(cruiseResultMap.get("value"))){
                        tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("valueUnit"));
                        tCruiseTaskResultMap.put("cruiseResult","246");//正常
                        tCruiseTaskResultMap.put("cruiseAbnormal","null");
//                        normal = normal + 1;
                    }else {
                        tCruiseTaskResultMap.put("resultNum","--");
                        tCruiseTaskResultMap.put("cruiseResult","247");//异常
                        tCruiseTaskResultMap.put("cruiseAbnormal","250");//异常告警
//                        abnormal = abnormal + 1;
                    }
                    if (cruiseResultMap.get("fileType").equals("1") || cruiseResultMap.get("fileType").equals("2")){
                        tCruiseTaskResultMap.put("picpath",cruiseResultMap.get("relativePath"));
                    }
                    tCruiseTaskResultMap.put("origpic",cruiseResultMap.get("absolutePath"));
                    tCruiseTaskResultMap.put("evaluationState","257");
                    tCruiseTaskResultMap.put("createtime",sdf.format(new Date()));
//            tCruiseTaskResultMap.put("is_warn",);

                    tCruiseTaskResultMap.put("taskId",taskId);
                    tCruiseTaskResultMap.put("runExecute",tCruiseTask.getIfRun().toString());

                    if(tCruiseTask.getAreaId() == null){
                        tCruiseTaskResultMap.put("areaId","null");
                    }else {
                        tCruiseTaskResultMap.put("areaId",tCruiseTask.getAreaId());
                    }
                    tCruiseTaskResultMap.put("cType",tCruiseTask.getType().toString());
                    tCruiseTaskResultMap.put("taskCount",robotInfoKeys.size() + "");
                    tCruiseTaskResultMap.put("taskCode",taskId);
                    log.info("tCruiseTaskResultMap是==="+tCruiseTaskResultMap);
                    redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);//塞进缓存

                    //做完一个点给前端推一次webSocket
                    Map<String, Object> jasonMap = new HashMap<>();
                    jasonMap.put("type", "finishedOneInstance");
                    jasonMap.put("taskId", taskId);
                    String json = JSON.toJSONString(jasonMap);
                    log.info("发送给前端的消息：" + json);
                    WebSocketServer.sendMsg(json);
                }
            }

            //统计机器人返回任务结果的大小
            List<String> resultList = new ArrayList<>();
            Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
            for (String key : cruiseKey) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                if (redisInfoMap.get("cruiseResult").equals("246") || redisInfoMap.get("cruiseResult").equals("247")){
                    resultList.add(redisInfoMap.get("instanceId"));
                }
            }
            log.info("resultList的大小===="+resultList.size());

            //统计巡视主机下发给机器人的巡检点大小
            Map<String, String> redisInfoMap2 = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode"));
            String instanceList = (String)redisInfoMap2.get("instanceIdList");
            instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
            String[] instanceIdArray = instanceList.split(", ");
            List<String> instanceIdList = new ArrayList<>();
            for (String i : instanceIdArray){
                instanceIdList.add(i);
            }
            log.info("instanceIdList的大小===="+instanceIdList.size());

            //比较任务相关时间(开始时间，超期时间)
            /*Map<String, String> redisInfoMap3 = redisTemplate.opsForHash().entries("countForAbnormal:"+cruiseResultMap.get("taskCode"));
            String taskStart = redisInfoMap3.get("taskStart");//开始时间
            Integer overDay = Integer.valueOf(redisInfoMap3.get("overDay"));//超期时间
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("开始的日期："+df.format(df.parse(taskStart)));
            String overDayTime = df.format(new Date(df.parse(taskStart).getTime() + overDay * 24 * 60 * 60 * 1000));
            log.info("overDay天后的日期：" + overDayTime );
            boolean taskFlag = (df.parse(overDayTime).getTime() < new Date().getTime());
            log.info("是否超期==="+taskFlag);*/

            /*if (taskStatus == null) {
                taskStatus = "";
            }*/
            /*if (taskStatus.equals("2") || taskStatus.equals("4")
                    || */
            if(instanceIdList.size() == resultList.size() ) {
                log.info("完成！！！");

                List<Long> instanceIDList = Constant.flagMap.get(taskId);

                if (instanceIDList != null && instanceIDList.size() > 0){
                    for (Long instanceId : instanceIDList){
                        instanceIdList.remove(instanceId.toString());
                    }
                }

                for (String instanceId : instanceIdList) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + instanceId);
                    if (redisInfoMap.get("cruiseResult").equals("246") ||
                            redisInfoMap.get("cruiseResult").equals("247")) {//缓存中该巡检点有结果
                        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
                                .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                                .setTaskResultId(redisInfoMap.get("taskResultId"))
                                .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
                                .setCruiseTime(sdf.parse(redisInfoMap.get("cruiseTime")))
                                .setEndTime(sdf.parse(redisInfoMap.get("endTime")))
                                .setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")))
                                .setCruiseStatus(252);//252.已执行253.未执行254.执行失败255.未知
                        tCTRDList.add(tCruiseTaskResultDetail);

                        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
                                .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                                .setCruiseId(Long.valueOf(redisInfoMap.get("instanceId")))
                                .setCruiseType(228)//机器人
                                .setPicpath(redisInfoMap.get("picpath"))
                                .setOrigpic(redisInfoMap.get("origpic"))
                                .setIsWarn(0)
                                .setEvaluationState(257)
                                .setCreatetime(new Date())
                                .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                        if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                            tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                        } else {
                            tCruiseDataResult.setCruiseAbnormal(null);
                        }
                        if (!"--".equals(redisInfoMap.get("resultNum"))) {
                            tCruiseDataResult.setResultNum(redisInfoMap.get("resultNum"));
                        } else {
                            tCruiseDataResult.setResultNum(null);
                        }
                        tCDRList.add(tCruiseDataResult);
                    }

                    if ("null".equals(redisInfoMap.get("cruiseAbnormal"))){
                        normal = normal + 1;
                    }else if ("250".equals(redisInfoMap.get("cruiseAbnormal"))){
                        abnormal = abnormal + 1;
                    }

                }
                log.info("tCTRDList的内容是===" + tCTRDList);
                log.info("tCDRList的内容是===" + tCDRList);
                //批量插入TCTRD库
                int res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tCTRDList);//批量插tCTRDList
                log.info("res1的内容是===" + res1);
                //批量插入TCDR库
                int res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tCDRList);//批量插tCDRList
                log.info("res2的内容是===" + res2);

                /*
                判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
                */

                log.info("机器人巡检任务产生的异常数是===" + abnormal);
                log.info("机器人巡检任务产生的正常数是===" + normal);


                /*Integer cState  = null;
                if (taskStatus.equals("2")){
                    cState = 241;//任务暂停
                } else if (taskStatus.equals("4")){
                    cState = 242;//任务终止
                }else if (instanceIdList.size() == resultList.size()){
                    cState = 240;//执行完成
                } else {
                    cState = 243;//未执行
                }*/

                //将公共类的instanceIdList清空
                if (Constant.flagMap.get(taskId) != null && Constant.flagMap.get(taskId).size() > 0){
                    for (Long instancedId : Constant.flagMap.get(taskId)){
                        instanceIdList.remove(instancedId.toString());
                    }
                }

                if (abnormalCheckPoint + normalCheckPoint == totalCheckPoint){//机器人任务是最后一个

                    Thread.sleep(15000);

                    TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                            .setTaskId(tCruiseTaskResultMap.get("taskId"))
                            .setTaskAlarm(0)
                            .setTaskAbnormal(abnormalCheckPoint)
                            .setRunExecute(String.valueOf(tCruiseTask.getIfRun()))
                            .setCruiseTaskTime(sdf.parse(tCruiseTaskResultMap.get("createtime")))
                            .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
                    log.info("tCruiseTaskResult的内容是==="+tCruiseTaskResult);
                    //插TCTR表
                    StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);
                    log.info("taskWait是==="+taskWait);

                    tCruiseResult.setTaskWait(taskWait);//待测点数
                    tCruiseResult.setCState(240);//任务状态
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
                    mapForAbnormal.put("abnormal", abnormalCheckPoint.toString());
                    mapForAbnormal.put("normal", normalCheckPoint.toString());
                    //更新异常点缓存的数据
                    redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

                    log.info("taskWait是==="+taskWait);
                    tCruiseResult.setTaskWait(taskWait);//待测点数
                    tCruiseResult.setCState(239);//正在执行
                    log.info("tCruiseResult的内容是==="+tCruiseResult);
                    //更新TCR表
                    StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
                }
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
