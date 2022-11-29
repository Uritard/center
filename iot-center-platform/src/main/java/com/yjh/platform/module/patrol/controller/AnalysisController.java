package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.UpdateRequest;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.AnalysisService;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    private final AbstractVideoCruise abstractVideoCruise;
    @Autowired
    private RedisTemplate redisTemplate;

    private final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    public AnalysisController(AnalysisService analysisService, IntelAnalysisService intelAnalysisService, AbstractVideoCruise abstractVideoCruise) {
        this.analysisService = analysisService;
        this.intelAnalysisService = intelAnalysisService;
        this.abstractVideoCruise = abstractVideoCruise;
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

    private static final String FLAG = "false";

    @ApiOperation(value = "表计算法接口")
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
            if (analysisList.get(0).getInstanceId() == -1) {
                result.setData(analysisService.feignAlgorithm(analysisList, recognizePort));
            }else {
                // 开关
                String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
                if (StringUtils.equals(FLAG, flag)){
                    // 原来的socket协议
                    result.setData(analysisService.feignAlgorithm(analysisList, recognizePort));
                }else {
                    // 调用智能分析主机接口进行分析
                    try {
                        intelAnalysisService.picAnalyseNoDetection(analysisList);
                    }catch (Exception e){
                        log.error("调用智能分析主机进行缺陷分析异常：", e);
                    }
                }
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "缺陷算法接口")
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
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis","content"));
            if (StringUtils.equals(FLAG, flag)){
                // 原来的socket协议
                result.setData(analysisService.feignDefect(analysisList, aiPort));
            }else {
                // 调用智能分析主机接口进行分析
                try {
                    intelAnalysisService.picAnalyseNoDetection(analysisList);
                }catch (Exception e){
                    log.error("调用智能分析主机进行缺陷分析异常：", e);
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

    @ApiOperation(value = "算法分析接口测试")
    @GetMapping(value = "/algorithm-test")
    public void algorithmTest(@RequestParam(value = "picPath",required = false) String picPath,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "instanceId", required = false) Long instanceId) {
        Analysis analysis = new Analysis();
        analysis.setTaskId("666666");
        if (instanceId == null){
            analysis.setInstanceId(666666L);
        }else {
            analysis.setInstanceId(instanceId);
        }

        analysis.setPicPath(picPath);
        if (Objects.equals("-1", type) && Objects.nonNull(instanceId)){
            // 设备状态识别
            String analyseType = analysisService.selectAnalyseType(instanceId);
            analysis.setAnalyseType(analyseType);
        }else {
            // 缺陷和判别
            analysis.setAnalyseType(type);
        }

        analysis.setPicModelPath("");
        analysis.setIsAi(0);
        List<Analysis> analysisList = new ArrayList<>();
        analysisList.add(analysis);
        Map<String, List<Analysis>> analysisMap  = new HashMap<>(2);
        analysisMap.put("list",analysisList);
        log.info("算法信息:{}", analysisMap);
        Constant.algorithmTestPicPath = picPath;
        if (Objects.equals("-1", type)){
            feignAlgorithm(analysisMap);
            return;
        }

        feignDefect(analysisMap);
    }

    @ApiOperation(value = "算法更新接口测试")
    @GetMapping(value = "/algorithm-update")
    public void algorithmUpdateTest(@RequestParam(value = "filePath") String filePath){
        UpdateRequest request = new UpdateRequest()
                .setRequestHostIp("192.168.1.66")
                .setRequestHostPort("18715")
                .setRequestId(String.valueOf(UUID.randomUUID()))
                .setAlgorithmPath(filePath);
        intelAnalysisService.algorithmUpdate(request);
    }

    @ApiOperation(value = "告警判断处理接口")
    @GetMapping(value = "/warnInfo")
    public Result alarmJudge(@RequestParam String value, @RequestParam String stdDeviceMeteName,
                           @RequestParam String meteKind, @RequestParam Integer alarmState,
                           @RequestParam String stateZero, @RequestParam String stateOne,
                           @RequestParam Integer alarmLevel,
                           @RequestParam Float highLimit1, @RequestParam Float lowLimit1,
                           @RequestParam Float highLimit2, @RequestParam Float lowLimit2,
                           @RequestParam Float highLimit3, @RequestParam Float lowLimit3,
                           @RequestParam Float highLimit4, @RequestParam Float lowLimit4) {
        Result result = new Result();
        try {
            result.setData(analyseDataOperateService.alarmJudge(value, stdDeviceMeteName, meteKind, alarmState, stateZero, stateOne, alarmLevel,
                    highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("告警判断处理异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("告警判断处理错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "test")
    @GetMapping(value = "/test")
    public Result test(){
        Result result = new Result();
        try {
            List<Analysis> analysisList = new ArrayList<>();
            Analysis analysis = new Analysis();
            analysis.setAnalyseType("11");
            analysis.setTaskId("123");
            analysis.setInstanceId(111L);
            analysis.setIsAi(0);
            analysis.setPicPath("/home/xx");
            analysis.setDevicePointId("3");
            analysis.setReferenceImage("/home/yjh_iot_center/iot-picture/specimens/123/123.jpg");
            analysisList.add(analysis);
            abstractVideoCruise.defect(analysisList);
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("告警判断处理异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("告警判断处理错误:", e);
        }
        return result;
    }
}
