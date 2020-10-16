package com.yjh.accessvideo.module.device.controller;

import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/analysis/v1")
@Api(value = "/analysis", description = "算法调用操作接口")
public class AnalysisController {

    @Autowired
    private final AnalysisService analysisService;

    private Logger log = LoggerFactory.getLogger(AnalysisController.class);

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @ApiOperation(value = "算法接口")
    @RequestMapping(value = "/algorithm", method = RequestMethod.POST)
    public Result feignAlgorithm(@RequestBody List<Analysis> analysisList) {
        Result result = new Result();
        try {
            result.setData(analysisService.feignAlgorithm(analysisList));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "缺陷接口")
    @RequestMapping(value = "/defect", method = RequestMethod.POST)
    public Result feignDefect(@RequestBody List<Analysis> analysisList) {
        Result result = new Result();
        try {
            result.setData(analysisService.feignDefect(analysisList));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("缺陷接口调用错误:", e);
        }
        return result;
    }

}
