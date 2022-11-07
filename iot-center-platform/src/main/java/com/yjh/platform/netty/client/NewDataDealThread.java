package com.yjh.platform.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.entity.AnalysePatrolTaskResult;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


@lombok.extern.slf4j.Slf4j
public class NewDataDealThread implements Runnable {

    private String body;
    private UPatrolTaskService uPatrolTaskService;

    public NewDataDealThread(String body) {
        this.body = body;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
    }

    @SneakyThrows
    @Override
    public void run() {
        // 反拆包解析
        String usefulBody = body;
        JSONObject jsonObject = JSON.parseObject(usefulBody);
        log.info("JSON对象1：" + jsonObject);
        if (!StringUtils.equals("2", jsonObject.getString("msgType"))){
            return;
        }

        JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.getString("msgData")).getString("data"));
        log.info("原生数据****：" + jsonObjectData);
        Iterator iterator = jsonObjectData.entrySet().iterator();

        LinkedList<AnalysePatrolTaskResult> resultList = new LinkedList<AnalysePatrolTaskResult>();
        // 迭代器取出data中的每一个resultInfo
        try {
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                // 遍历每一个结果子集
                JSONObject jsonObjectResult = JSON.parseObject(String.valueOf(entry.getValue()));
                log.info("数据****：" + jsonObjectResult);
                String taskId = jsonObjectResult.getString("taskId");
                String instanceId = jsonObjectResult.getString("instanceId");
                String resultValue = jsonObjectResult.getString("resultValue");
                String analyseType = jsonObjectResult.getString("analyseType");
                String analyseResultImg = jsonObjectResult.getString("analyseResultImg");
                String firDocPath = jsonObjectResult.getString("firDocPath");

                AnalysePatrolTaskResult taskResult = new AnalysePatrolTaskResult();
                taskResult.setTaskId(taskId);
                taskResult.setInstanceId(instanceId);
                taskResult.setAnalyseType(analyseType);
                if (StringUtils.equals("NULL_Model", resultValue)) {
                    resultValue = "数据错误";
                } else if (StringUtils.equals("识别失败", resultValue)) {
                    resultValue = "未获得读数";
                }
                taskResult.setResultValue(resultValue);
                taskResult.setAnalyseResultImg(analyseResultImg);
                taskResult.setFirDocPath(firDocPath);

                resultList.add(taskResult);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        uPatrolTaskService.analysePatrolTaskResult(resultList);
    }
}



