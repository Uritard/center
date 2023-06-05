package com.yjh.platform.module.patrol.controller;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.*;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 调用智能分析主机接口
 *
 * @author 丫C
 * @date 2022/4/11
 */
@RestController
@Api(value = "/intel-analysis", tags = "调用智能分析主机接口")
public class IntelAnalysisController {

    @Autowired
    private final IntelAnalysisService intelAnalysisService;

    private final Logger log = LoggerFactory.getLogger(IntelAnalysisController.class);

    public IntelAnalysisController(IntelAnalysisService intelAnalysisService) {
        this.intelAnalysisService = intelAnalysisService;
    }

    @PostMapping(value = "/picAnalyseNoDetection")
    public List<Response> picAnalyseNoDetection(@Valid @RequestBody List<Analysis> analysisList) {
        return intelAnalysisService.picAnalyseNoDetection(analysisList);
    }

    @PostMapping("/algorithmUpdate")
    public Response algorithmUpdate(@Valid @RequestBody UpdateRequest request) {
        return intelAnalysisService.algorithmUpdate(request);
    }

    @PostMapping(value = "/picAnalyseRetNotify")
    public Response picAnalyseRetNotify(@Valid @RequestBody PicAnalyseResponse response) {
        log.info("< < < < < < 收到分析结果反馈：{}", JSON.toJSONString(response));
        try {
            intelAnalysisService.picAnalyseRetNotify(response);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return Response.serverError();
        }
        return Response.ok();
    }

    /**
     *  巡视主机收到算法更新结果反馈
     *
     * @param response 参数
     * @return ResponseEntity
     */
    @PostMapping(value = "/algorithmUpdateResult")
    public Response algorithmUpdateResult(@Valid @RequestBody UpdateResponse response) {
        try {
            log.info("< < < < < < 收到算法更新结果反馈：{}", JSON.toJSONString(response));
            if (StringUtils.equals("test666", response.getRequestId())){
                intelAnalysisService.algorithmUpdateResult(response);
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
            Response.serverError();
        }
        return Response.ok();
    }

}
