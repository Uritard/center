package com.yjh.accessvideo.module.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * 调用智能分析主机接口
 *
 * @author 丫C
 * @date 2022/4/11
 */
@RestController
@Api(value = "/intel-analysis", tags = "调用智能分析主机接口")
public class IntelAnalysisController {

    private Logger log = LoggerFactory.getLogger(IntelAnalysisController.class);

    @Autowired
    private final IntelAnalysisService intelAnalysisService;

    public IntelAnalysisController(IntelAnalysisService intelAnalysisService) {
        this.intelAnalysisService = intelAnalysisService;
    }

    @PostMapping("/picAnalyse")
    public ResponseEntity<Response> picAnalyse(@Valid @RequestBody PicAnalyseRequest picAnalyseRequest) {
        return intelAnalysisService.picAnalyse(picAnalyseRequest);
    }

    @PostMapping(value = "/picAnalyseNoDetection")
    public ResponseEntity<Response> picAnalyseNoDetection() {
        JSONObject analysisObject = new JSONObject();
        analysisObject.put("msgID", String.valueOf(100000001));
        analysisObject.put("msgType", "1");
        JSONObject msgDataObject = new JSONObject();
        msgDataObject.put("desNode", "serverSocket");
        msgDataObject.put("srcNode", "clientSocket001");

        JSONObject pictureInfoObject = new JSONObject();

        JSONObject pictureDataObject = new JSONObject();
        pictureDataObject.put("analyseType", "11");
        pictureDataObject.put("imagePath", "imagePath.jpg");
        pictureDataObject.put("modelPath", "modelPath.jpg");
        pictureDataObject.put("taskId", String.valueOf(UUID.randomUUID()));
        pictureDataObject.put("instanceId", "123456");
        pictureInfoObject.put("pictureInfo1", pictureDataObject);

        JSONObject pictureDataObject2 = new JSONObject();
        pictureDataObject2.put("analyseType", "11");
        pictureDataObject2.put("imagePath", "imagePath.jpg");
        pictureDataObject2.put("modelPath", "modelPath.jpg");
        pictureDataObject2.put("taskId", String.valueOf(UUID.randomUUID()));
        pictureDataObject2.put("instanceId", "123456");
        pictureInfoObject.put("pictureInfo2", pictureDataObject2);

        msgDataObject.put("data", pictureInfoObject);
        analysisObject.put("msgData", msgDataObject);
        return intelAnalysisService.picAnalyseNoDetection(analysisObject);
    }


    @PostMapping("/algorithmUpdate")
    public ResponseEntity<Response> algorithmUpdate(@Valid @RequestBody UpdateRequest request) {
        return intelAnalysisService.algorithmUpdate(request);
    }

    @PostMapping(value = "/algorithmUpdateNoDetection")
    public ResponseEntity<Response> algorithmUpdateNoDetection(@Valid @RequestBody UpdateRequest request) {
        return intelAnalysisService.algorithmUpdateNoDetection(request);
    }

    @PostMapping(value = "/picAnalyseRetNotify")
    public ResponseEntity<Response> picAnalyseRetNotify(@Valid @RequestBody PicAnalyseResponse response) {
        log.info("< < < < < < 开始处理分析结果 > > > > > >");
        return intelAnalysisService.picAnalyseRetNotify(response);
    }

    /**
     *  巡视主机收到算法更新结果反馈
     *
     * @param response 参数
     * @return ResponseEntity
     */
    @PostMapping(value = "/algorithmUpdateResult")
    public ResponseEntity<Response> algorithmUpdateResult(@Valid @RequestBody UpdateResponse response) {
        log.info("< < < < < < 开始处理算法更新结果 > > > > > >");
        return ResponseEntity.ok(Response.ok());
    }



}
