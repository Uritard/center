package com.yjh.accessvideo.module.device.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.netty.client.AnalysisClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class AnalysisService {

    private Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private static long algorithmMsgId = 100000001;
    private static long defectMsgId = 200000001;

    @Logs(title = "算法调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String feignAlgorithm(List<Analysis> analysisList, int recognizePort) throws InterruptedException {

        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        log.info("任务结束心跳发送");
        if (analysisList.get(0).getInstanceId() == -1) {
            AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(recognizePort).SendHeartBeat(analysisList.get(0));
        } else {
            algorithmMsgId = algorithmMsgId+1;
            analysisObject.put("msgID", String.valueOf(algorithmMsgId));
            log.info("algorithmMsgId: "+algorithmMsgId);
            analysisObject.put("msgType", "1");
            msgDataObject.put("desNode", "serverSocket");
            msgDataObject.put("srcNode", "clientSocket001");
            int i=1;
            for (Analysis analysis: analysisList) {
                if (analysis.getIsAi()==1) {
                    JSONObject pictureInfoObject = new JSONObject();
                    JSONObject pictureDataObject = new JSONObject();
                    pictureDataObject.put("analyseType", analysis.getAnalyseType());
                    pictureDataObject.put("imagePath", analysis.getPicPath());
                    pictureDataObject.put("modelPath", analysis.getPicModelPath());
                    pictureDataObject.put("taskId", analysis.getTaskId());
                    pictureDataObject.put("instanceId", analysis.getInstanceId().toString());
                    pictureInfoObject.put("pictureInfo"+i, pictureDataObject);
                    msgDataObject.put("data", pictureInfoObject);
                }
                i++;
            }
            analysisObject.put("msgData", msgDataObject);
            AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(recognizePort).sendDataReguest(analysisObject);
        }
        log.info("analysisList.get(0).getInstanceId():"+analysisList.get(0).getInstanceId());
        return "success";
    }

    @Logs(title = "缺陷调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String feignDefect(List<Analysis> analysisList, int aiPort) throws InterruptedException {
        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        defectMsgId = defectMsgId+1;
        analysisObject.put("msgID", String.valueOf(defectMsgId));
        log.info("defectMsgId: "+defectMsgId);
        analysisObject.put("msgType", "1");
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");
        log.info("进入算法方法----------");
        int i=1;
        for (Analysis analysis: analysisList) {
            if (analysis.getIsAi()==0) {
                JSONObject pictureDataObject = new JSONObject();
                JSONObject pictureInfoObject = new JSONObject();
                pictureDataObject.put("analyseType", analysis.getAnalyseType());
                pictureDataObject.put("imagePath", analysis.getPicPath());
                pictureDataObject.put("modelPath", analysis.getPicModelPath());
                pictureDataObject.put("taskId", analysis.getTaskId());
                pictureInfoObject.put("pictureInfo"+i, pictureDataObject);
                pictureDataObject.put("instanceId", analysis.getInstanceId().toString());
                msgDataObject.put("data", pictureInfoObject);
                log.info("DATA内容:"+pictureInfoObject);
            }
            i++;
        }
        analysisObject.put("msgData", msgDataObject);
        AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(aiPort).sendDataReguest(analysisObject);
        log.info("缺陷算法结束------------"+msgDataObject);
        return "success";
    }

}

