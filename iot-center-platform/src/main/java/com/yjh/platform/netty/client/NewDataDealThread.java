package com.yjh.platform.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.entity.AnalysePatrolTaskResult;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


/**
 * 巡视主机算法分析结果处理
 *
 * @author 丫C
 * @date 2022/5/31
 */
@lombok.extern.slf4j.Slf4j
public class NewDataDealThread implements Runnable {

    private final String body;
    private final PatrolResultHandler patrolResultHandler;

    public NewDataDealThread(String body) {
        this.body = body;
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
    }

    @SneakyThrows
    @Override
    public void run() {
        // 反拆包解析
        JSONObject jsonObject = JSON.parseObject(body);
        log.info("JSON对象1：" + jsonObject);
        if (!StringUtils.equals("2", jsonObject.getString("msgType"))){
            return;
        }

        JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.getString("msgData")).getString("data"));
        log.info("原生数据****：{}", jsonObjectData);
        Iterator iterator = jsonObjectData.entrySet().iterator();

        LinkedList<AnalysePatrolTaskResult> resultList = new LinkedList<AnalysePatrolTaskResult>();
        // 迭代器取出data中的每一个resultInfo
        try {
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                // 遍历每一个结果子集
                JSONObject jsonObjectResult = JSON.parseObject(String.valueOf(entry.getValue()));
                log.info("数据****：{}", jsonObjectResult);
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
        log.info("resultList=={}", resultList);
        patrolResultHandler.analysePatrolTaskResult(resultList);
    }
}



