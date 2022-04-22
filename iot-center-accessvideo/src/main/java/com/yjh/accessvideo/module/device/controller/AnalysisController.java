package com.yjh.accessvideo.module.device.controller;

import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/analysis/v1")
@Api(value = "/analysis", tags = "算法调用操作接口")
public class AnalysisController {

    @Autowired
    private final AnalysisService analysisService;
    @Autowired
    private final IntelAnalysisService intelAnalysisService;

    private Logger log = LoggerFactory.getLogger(AnalysisController.class);

    public AnalysisController(AnalysisService analysisService, IntelAnalysisService intelAnalysisService) {
        this.analysisService = analysisService;
        this.intelAnalysisService = intelAnalysisService;
    }

    /**
     * 识别算法端口
     */
    @Value("${netty.recognize.port}")
    private int recognizePort;

    /**
     * 缺陷算法端口
     */
    @Value("${netty.ai.port}")
    private int aiPort;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "算法接口")
    @PostMapping(value = "/algorithm")
    public Result feignAlgorithm(@RequestBody Map<String, List<Analysis>> analysisMap) {
        Result result = new Result();
        try {
            if (Objects.isNull(analysisMap.get("list"))) {
                result.setMessage("参数为空");
                return result;
            }
            List<Analysis> analysisList = analysisMap.get("list");
            log.info("---------发送算法信息中");
            log.info("算法数据列表：{}", analysisList);
            log.info("端口号：{}", recognizePort);
            result.setData(analysisService.feignAlgorithm(analysisList, recognizePort));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "缺陷接口")
    @PostMapping(value = "/defect")
    public Result feignDefect(@RequestBody Map<String, List<Analysis>> analysisMap) {
        Result result = new Result();
        try {
            if (Objects.isNull(analysisMap.get("list"))) {
                result.setMessage("参数为空");
                return result;
            }
            List<Analysis> analysisList = analysisMap.get("list");
            log.info("---------发送算法信息中");
            log.info("缺陷接口数据列表：{}", analysisList);
            log.info("端口号：{}", aiPort);
            // 开关
            String flag = redisTemplate.opsForHash().get("t_sys_param:isIntelAnalysis","content").toString();
            if (StringUtils.equals("false", flag)){
                // 原来的socket协议
                result.setData(analysisService.feignDefect(analysisList, aiPort));
            }else {
                // 调用智能分析主机接口进行分析
                try {
                    intelAnalysisService.picAnalyseNoDetection(analysisList);
                }catch (Exception e){
                    log.error("调用智能分析主机进行缺陷分析异常："+e);
                    log.error("exceptionDetails:"+e.getStackTrace()[0]);
                }
            }


        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("缺陷接口调用错误:", e);
        }
        return result;
    }

}
