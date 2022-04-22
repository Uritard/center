package com.yjh.accessvideo.module.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired
    private RedisTemplate redisTemplate;

    public IntelAnalysisController(IntelAnalysisService intelAnalysisService) {
        this.intelAnalysisService = intelAnalysisService;
    }

    @PostMapping("/picAnalyse")
    public ResponseEntity<Response> picAnalyse(@Valid @RequestBody PicAnalyseRequest picAnalyseRequest) {
        return intelAnalysisService.picAnalyse(picAnalyseRequest);
    }

    @PostMapping(value = "/picAnalyseNoDetection")
    public ResponseEntity<Response> picAnalyseNoDetection() {
        // 测试数据
        List<Analysis> analysisList = new ArrayList<>();
        Analysis analysis = new Analysis();
        analysis.setTaskId("task666");
        analysis.setInstanceId(11000003566L);
        analysis.setPicPath("/home/yjh_iot_center/iot-picture/resultImg/11.jpg");
        analysis.setAnalyseType("11");
        analysisList.add(analysis);

        Analysis analysis2 = new Analysis();
        analysis2.setTaskId("task666");
        analysis2.setInstanceId(11000003566L);
        analysis2.setPicPath("/home/yjh_iot_center/iot-picture/resultImg/398.jpg");
        analysis2.setAnalyseType("398");
        analysisList.add(analysis2);
        return intelAnalysisService.picAnalyseNoDetection(analysisList);
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
        log.info("< < < < < < 收到的处理分析结果：{}", response);
        // 开关
        String flag = redisTemplate.opsForHash().get("t_sys_param:isIntelAnalysis","content").toString();
        if (StringUtils.equals("true", flag)){
            intelAnalysisService.picAnalyseRetNotify(response);
        }
        return ResponseEntity.ok(Response.ok());
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
