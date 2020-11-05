package com.yjh.accessvideo.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.websocket.WebSocketServer;
import com.yjh.accessvideo.module.device.entity.TCruiseDataResult;
import com.yjh.accessvideo.module.device.entity.TCruiseResult;
import com.yjh.accessvideo.module.device.entity.TCruiseTaskResult;
import com.yjh.accessvideo.module.device.entity.TCruiseTaskResultDetail;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.yjh.accessvideo.common.Constant.*;
import static com.yjh.accessvideo.common.Constant.NORMAL;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private String body;
    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;

    public DataDealThread(String body,RedisTemplate redisTemplate,AnalyseDataOperateService analyseDataOperateService) {
        this.analyseDataOperateService=analyseDataOperateService;
        this.redisTemplate=redisTemplate;
        this.body = body;
    }


    @SneakyThrows
    @Override
    public void run() {
        //TODO 添加线程池
        JSONObject jsonObject = JSON.parseObject(body);
        log.info("JSON对象1：" + jsonObject);
        if (jsonObject.get("msgType").toString().equals("2")) {
            JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.get("msgData").toString()).get("data").toString()); //全量数据结果集
            log.info("原生数据****：" + jsonObjectData);
            Iterator iterator = jsonObjectData.entrySet().iterator();//迭代器取出data中的每一个resultInfo
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                //遍历每一个结果子集
                JSONObject jsonObjectResult = JSON.parseObject(entry.getValue().toString());
                log.info("数据****：" + jsonObjectResult);//打印resultInfo
                //初始化TASKID和INSTANCEID


                TASKID = jsonObjectResult.get("taskId").toString();
                INSTANCEID = jsonObjectResult.get("instanceId").toString();
                String name = "t_cruise_task_result:" + TASKID + INSTANCEID;
                log.info("keyName" + name);
                cruiseKeys.add(name);


                log.info("数据初始化");
//                    String analyseType=jsonObjectResult.get("analyseType").toString();
//                    log.info(analyseType);
//                    String taskId=jsonObjectResult.get("taskId").toString();
//                    log.info(taskId);
//                    String resultValue1=jsonObjectResult.get("resultValue").toString();
//                    log.info(resultValue1);
//                    String instanceId=jsonObjectResult.get("instanceId").toString();
//                    log.info(instanceId);

                //redis数据键名由taskId+instanceId命名
                log.info("数据Redis业务开启");
                String redisName = jsonObjectResult.get("taskId").toString() + jsonObjectResult.get("instanceId").toString();
                log.info("template:" + redisTemplate);
                log.info("redisName:" + redisName);
                Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + redisName);//读redis
                log.info("读取到的redis：" + cruiseResult);
                Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map
                cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
                cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "正常"));
                cruiseResultMap.put("cruiseStatus", analyseDataOperateService.selectDictCode("cruise_data_state", "已执行"));
                // TODO: 2020/11/4 对算法识别结果进行判断并决定再redis中插入哪个值：identifyState- 识别正常&识别异常
                // TODO: 2020/11/4 对实际结果进行判断并决定填入哪个初始值：identifyResult-正常&未采集图片&未识别图片&识别缺陷警告（加入IF判断）
                cruiseResultMap.put("identifyState",analyseDataOperateService.selectDictCode("identify_state","识别正常"));
                cruiseResultMap.put("identifyResult",analyseDataOperateService.selectDictCode("identify_result","正常"));
                redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);//修改redis
                NORMAL = NORMAL + 1;
                // webSocket通知前端调用巡视监控的接口
                Map<String, Object> jasonMap = new HashMap<>();
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", cruiseResult.get("taskId").toString());
                String json = JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：" + json);
                WebSocketServer.sendMsg(json);


                //修改缓存中任务算法巡视点List
                redisTemplate.opsForList().remove(cruiseResult.get("taskId").toString(), 0, cruiseResult.get("instanceId").toString());
                redisTemplate.opsForList().leftPush(cruiseResult.get("taskId").toString(), "0");

                log.info("RedisList修改成功");

            }

        } else if (jsonObject.get("msgType").toString().equals("6")) { //任务结束后发来的心跳信息
            String taskId = JSON.parseObject(jsonObject.get("msgData").toString()).get("taskId").toString();
            redisTemplate.opsForList().leftPush(taskId, "-1");
            log.info("心跳处理结束" + taskId);
        }

        if (redisTemplate.opsForList().index(TASKID, 0).equals("-1") && redisTemplate.opsForList().index(TASKID, 1).equals("0")) {  //满足插库条件

            //满足条件先插巡视点数据
            List<TCruiseTaskResultDetail> detailList = new ArrayList<>();//TCTRD List对象
            List<TCruiseDataResult> dataList = new ArrayList<>();//TCDR List对象
//            Set<String>cruiseKeys=redisScan("t_cruise_task_result:"+TASKID);
            log.info("cruisekeys:" + cruiseKeys);
            for (String cruiseKey : cruiseKeys) {
                Map<String, Object> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
                //TCTRD
                log.info("TCTRD开始");
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId").toString());
                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(cruiseWorkedMap.get("deviceId").toString()));
                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime").toString()));
                tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime").toString()));
                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(cruiseWorkedMap.get("cruiseStatus").toString()));
                tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark").toString());
                detailList.add(tCruiseTaskResultDetail);

                //TCDR
                log.info("TCRD开始");
                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
                tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
                tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
                tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
                tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
                tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
                tCruiseDataResult.setEvaluationState(Integer.valueOf(analyseDataOperateService.selectDictCode("evaluation_state", "未审核")));
                tCruiseDataResult.setState(Integer.valueOf(cruiseWorkedMap.get("state").toString()));
                tCruiseDataResult.setIdentifyState(Integer.valueOf(cruiseWorkedMap.get("identifyState").toString()));//根据获取的算法识别结果对缓存中相应字段进行修改
                tCruiseDataResult.setIdentifyResult(Integer.valueOf(cruiseWorkedMap.get("identifyResult").toString()));//根据巡视点执行结果对 缓存中相应字段进行修改
                tCruiseDataResult.setCreatetime(new Date());
                tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                dataList.add(tCruiseDataResult);
            }
            //批量插入两表

            log.info("两表开始插入");
            analyseDataOperateService.batchInsertCruiseTaskResultDetail(detailList);
            analyseDataOperateService.batchInsertCruiseDataResult(dataList);
            log.info("两表结束插入");
            cruiseKeys.clear();


            // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
            Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + TASKID + INSTANCEID);
            String strForCountAbnormal = "countForAbnormal:" + TASKID;
            Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
            Integer total = Integer.valueOf(abnormalCount.get("all").toString());
            Integer abnormal = Integer.valueOf(abnormalCount.get("abnormal").toString());
            Integer normal = Integer.valueOf(abnormalCount.get("normal").toString());
            //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
            if (abnormal + ABNORMAL + normal + NORMAL == total) {
                //TCTR开始
                log.info("TCTR开始");
                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
                tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
                tCruiseTaskResult.setTaskAbnormal(abnormal + ABNORMAL);
                tCruiseTaskResult.setTaskAlarm(0);
                tCruiseTaskResult.setRunExecute(cruiseResult.get("if_run").toString());
//                                tCruiseTaskResult.setCruiseTaskTime();
                analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);


                //TCR开始
                log.info("TCR开始");
                TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId").toString());
                tCruiseResult.setCState(Integer.valueOf(analyseDataOperateService.selectDictCode("task_state", "执行完成").toString()));
                tCruiseResult.setTaskWait(0);
                analyseDataOperateService.updateCruiseResult(tCruiseResult);
            } else {       // 修改异常点数缓存
                Map<String, String> mapForAbnormal = new HashMap<>();
                Integer totAbnormal = abnormal + ABNORMAL;
                Integer totNormal = normal + NORMAL;
                mapForAbnormal.put("abnormal", totAbnormal.toString());
                mapForAbnormal.put("normal", totNormal.toString());
                log.info("Normal" + NORMAL.toString());
                log.info("normal:" + totNormal.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
            }
            ABNORMAL = 0;
            NORMAL = 0;

        }


    }

}



