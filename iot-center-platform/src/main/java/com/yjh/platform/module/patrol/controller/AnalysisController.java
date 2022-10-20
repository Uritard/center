package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.patrol.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.UpdateRequest;
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

    private final Logger log = LoggerFactory.getLogger(AnalysisController.class);

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

    private static final String FLAG = "false";

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
                        log.error("调用智能分析主机进行缺陷分析异常：{}", e.getMessage());
                        log.error("exceptionDetails:{}", e.getStackTrace()[0]);
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
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis","content"));
            if (StringUtils.equals(FLAG, flag)){
                // 原来的socket协议
                result.setData(analysisService.feignDefect(analysisList, aiPort));
            }else {
                // 调用智能分析主机接口进行分析
                try {
                    intelAnalysisService.picAnalyseNoDetection(analysisList);
                }catch (Exception e){
                    log.error("调用智能分析主机进行缺陷分析异常：{}", e.getMessage());
                    log.error("exceptionDetails:{}", e.getStackTrace()[0]);
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
        Map<String, List<Analysis>> analysisMap  = new HashMap<>();
        analysisMap.put("list",analysisList);
        log.info("算法信息：    "+analysisMap);
        Constant.algorithmTestPicPath = picPath;
        if (Objects.equals("-1", type)){
            feignAlgorithm(analysisMap);
        }else{
         this.feignDefect(analysisMap);
        }
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
    public Result warnInfo(@RequestParam String value, @RequestParam String stdDeviceMeteName,
                           @RequestParam String meteKind, @RequestParam Integer alarmState,
                           @RequestParam String stateZero, @RequestParam String stateOne,
                           @RequestParam Integer alarmLevel,
                           @RequestParam Float highLimit1, @RequestParam Float lowLimit1,
                           @RequestParam Float highLimit2, @RequestParam Float lowLimit2,
                           @RequestParam Float highLimit3, @RequestParam Float lowLimit3,
                           @RequestParam Float highLimit4, @RequestParam Float lowLimit4){
        Result result=new Result();
        try{
            //是否告警
            Boolean isWarn=false;
            //告警级别
            Integer warnLevel=0;
            //告警名称
            String warnName=null;
            //告警内容
            String warnContent=null;
            //超越浮动值
            String outRange=null;
            //告警时间
            Date warnTime=null;
            int flag=analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
            if(flag==1){
                switch (meteKind){
                    case "1":
                        warnLevel=alarmLevel;
                        if(analyseDataOperateService.warnJudgementTelesignaling(value,stateZero,stateOne,alarmState)==1){
                            isWarn=true;
                            warnName=stdDeviceMeteName;
                            switch (alarmState){
                                case 0:
                                    warnContent=stdDeviceMeteName+":"+stateZero+"--"+"状态"+analyseDataOperateService.selectDictNote(warnLevel.toString(), "alarm_level");
                                    break;
                                case 1:
                                    warnContent=stdDeviceMeteName+":"+stateOne+"--"+"状态"+analyseDataOperateService.selectDictNote(warnLevel.toString(), "alarm_level");
                            }
                            warnTime=new Date();
                        }
                        break;
                    case "2":
                        if(value.matches("^[a-zA-Z_\\u4e00-\\u9fa5_\\--]+$")){
                            break;
                        }else {
                            int level = analyseDataOperateService.warnJudgement(Float.valueOf(value), highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                            log.info("level-------------:" + level);
                            if (level > 0) {
                                isWarn = true;
                                warnName = stdDeviceMeteName + "数据异常";
                                warnTime = new Date();
                                Float resultValueMeter = Float.valueOf(value);
                                switch (level) {
                                    case 1:
                                        warnLevel = Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                        warnContent =stdDeviceMeteName+":"+value+ "--" + "预警";
                                        if (resultValueMeter >= highLimit1) {
                                            outRange = String.valueOf(resultValueMeter - highLimit1);
                                        } else {
                                            outRange = String.valueOf(lowLimit1 - resultValueMeter);
                                        }
                                        break;
                                    case 2:
                                        warnLevel = Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                        warnContent =stdDeviceMeteName +":"+value+ "--" + "一般告警";
                                        if (resultValueMeter >= highLimit2) {
                                            outRange = String.valueOf(resultValueMeter - highLimit2);
                                        } else {
                                            outRange = String.valueOf(lowLimit2 - resultValueMeter);
                                        }
                                        break;
                                    case 3:
                                        warnLevel = Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                        warnContent = stdDeviceMeteName +":"+value+ "-" + "严重告警";
                                        if (resultValueMeter >= highLimit3) {
                                            outRange = String.valueOf(resultValueMeter - highLimit3);
                                        } else {
                                            outRange = String.valueOf(lowLimit3 - resultValueMeter);
                                        }
                                        break;
                                    case 4:
                                        warnLevel = Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                        warnContent =stdDeviceMeteName +":"+value+ "-" + "危急告警";
                                        if (resultValueMeter >= highLimit4) {
                                            outRange = String.valueOf(resultValueMeter - highLimit4);
                                        } else {
                                            outRange = String.valueOf(lowLimit4 - resultValueMeter);
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put("isWarn",isWarn);
            resultMap.put("warnLevel",warnLevel);
            resultMap.put("warnName",warnName);
            resultMap.put("warnContent",warnContent);
            resultMap.put("outRange",outRange);
            resultMap.put("warnTime",warnTime);
            result.setData(resultMap);
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
