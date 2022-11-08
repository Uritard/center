package com.yjh.accessvideo.module.device.controller;

import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import com.yjh.accessvideo.service.ProcessResultToUpSystem;
import com.yjh.accessvideo.thread.TaskExecutePool;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
@Deprecated
@Api(value = "/intel-analysis/Deprecated", tags = "调用智能分析主机接口")
public class IntelAnalysisController {

    @Autowired
    private final IntelAnalysisService intelAnalysisService;

    private final Logger log = LoggerFactory.getLogger(IntelAnalysisController.class);

    public IntelAnalysisController(IntelAnalysisService intelAnalysisService) {
        this.intelAnalysisService = intelAnalysisService;
    }

    @PostMapping("/picAnalyse")
    public Response picAnalyse(@Valid @RequestBody PicAnalyseRequest picAnalyseRequest) {
        return intelAnalysisService.picAnalyse(picAnalyseRequest);
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
        log.info("< < < < < < 收到分析结果反馈：{}", response);
        try {
            intelAnalysisService.picAnalyseRetNotify(response);
        }catch (Exception e){
            log.error(e.getMessage());
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
            log.info("< < < < < < 收到算法更新结果反馈：{}", response);
            if (StringUtils.equals("test666", response.getRequestId())){
                intelAnalysisService.algorithmUpdateResult(response);
            }
        }catch (Exception e){
            log.error(e.getMessage());
            Response.serverError();
        }
        return Response.ok();
    }

}
