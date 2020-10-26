package com.yjh.accessvideo.module.device.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.netty.client.AnalysisClientHandler;
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

    @Logs(title = "算法调用", code = "Analysis")
    @Transactional(rollbackFor = Exception.class)
    public String feignAlgorithm(List<Analysis> analysisList, int recognizePort) {

        JSONObject analysisObject = new JSONObject();
        JSONObject msgDataObject = new JSONObject();
        JSONObject pictureInfo = new JSONObject();
//        JSONArray pictureInfoArray = new JSONArray();

        analysisObject.put("msgID", "123456789");
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
                pictureDataObject.put("instanceId", analysis.getInstanceId());
                pictureInfoObject.put("pictureInfo"+i, pictureDataObject);
//                pictureInfoArray.add(pictureInfoObject);
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
        JSONObject pictureInfo = new JSONObject();
//        JSONArray pictureInfoArray = new JSONArray();
        analysisObject.put("msgID", "123567");
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
                pictureDataObject.put("instanceId", analysis.getInstanceId());
                msgDataObject.put("data", pictureInfoObject);
            }
            i++;
        }
        analysisObject.put("msgData", msgDataObject);
        AnalysisClientHandler.getAnalysisClientHandlerHashMap().get(aiPort).sendDataReguest(analysisObject);
        return "success";
    }

}

