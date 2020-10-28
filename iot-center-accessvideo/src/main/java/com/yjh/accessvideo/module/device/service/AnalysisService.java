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
    public String feignAlgorithm(List<Analysis> analysisList, int recognizePort) {

        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();

        algorithmMsgId = algorithmMsgId+1;
        analysisObject.put("msgID", String.valueOf(algorithmMsgId));
        log.info("algorithmMsgId: "+algorithmMsgId);
        analysisObject.put("msgType", "1");
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");
        int i=1;
        for (Analysis analysis: analysisList) {
            if (analysis.getIsAi()==0) {
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
        return "success";
    }

    @Logs(title = "缺陷调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String feignDefect(List<Analysis> analysisList, int aiPort) {
        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        defectMsgId = defectMsgId+1;
        analysisObject.put("msgID", String.valueOf(defectMsgId));
        log.info("defectMsgId: "+defectMsgId);
        analysisObject.put("msgType", "1");
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");
        int i=1;
        for (Analysis analysis: analysisList) {
            if (analysis.getIsAi()==1) {
                JSONObject pictureDataObject = new JSONObject();
                JSONObject pictureInfoObject = new JSONObject();
                pictureDataObject.put("analyseType", analysis.getAnalyseType());
                pictureDataObject.put("imagePath", analysis.getPicPath());
                pictureDataObject.put("modelPath", analysis.getPicModelPath());
                pictureDataObject.put("taskId", analysis.getTaskId());
                pictureInfoObject.put("pictureInfo"+i, pictureDataObject);
                pictureDataObject.put("instanceId", analysis.getInstanceId().toString());
                msgDataObject.put("data", pictureInfoObject);
            }
            i++;
        }
        analysisObject.put("msgData", msgDataObject);
        AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(aiPort).sendDataReguest(analysisObject);
        return "success";
    }

}

