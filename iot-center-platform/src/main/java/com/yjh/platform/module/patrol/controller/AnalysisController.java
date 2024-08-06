package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.algorithm.AlgorithmService;
import com.yjh.platform.common.Constant;
import com.yjh.platform.algorithm.FtpsService;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.interlanalysis.UpdateRequest;
import com.yjh.platform.module.patrol.service.AbstractVideoCruise;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
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
    private final IntelAnalysisService intelAnalysisService;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    private final AbstractVideoCruise abstractVideoCruise;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AlgorithmService algorithmService;
    @Autowired
    private FtpsService ftpsService;
    @Autowired
    private ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    public AnalysisController(IntelAnalysisService intelAnalysisService, AbstractVideoCruise abstractVideoCruise) {
        this.intelAnalysisService = intelAnalysisService;
        this.abstractVideoCruise = abstractVideoCruise;
    }

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
            log.info("端口号：{}", applicationProperties.getAlgorithmServerConfig().getNettyRecognizePort());
            abstractVideoCruise.analysis(analysisList);
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
            log.info("端口号：{}", applicationProperties.getAlgorithmServerConfig().getNettyAiPort());
            // 开关
            abstractVideoCruise.defect(analysisList);
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
    public Result algorithmTest(@RequestParam(value = "picPath",required = false) String picPath,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "instanceId", required = false) Long instanceId) {

        if ("11398".equals(type)){
            return upToAlgorithm(instanceId,picPath);
        }

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
            String analyseType = analyseDataOperateService.selectAnalyseType(instanceId);
            analysis.setAnalyseType(analyseType);
        }else {
            // 缺陷和判别
            analysis.setAnalyseType(type);
        }
        // 判别该点为本级系统的点还是下级系统的
        TCruisePointInstance instance = analyseDataOperateService.selectPointInstance(analysis.getInstanceId());
        Long presetId = instance.getCruiseid();
        String presetImgPath = SysParamConfig.getSysContent("presetImgPath");
        if (StringUtils.isNotEmpty(instance.getEdgeCode())) {
            String stationId = (String)redisTemplate.opsForHash().get("region:" + instance.getEdgeCode(), "stationId");
            analysis.setReferenceImage(presetImgPath + "/" + stationId + "/" + presetId + "/" + presetId + ".jpg");
        } else {
            analysis.setReferenceImage(presetImgPath + "/" + presetId + "/" + presetId + ".jpg");
        }

        analysis.setPicModelPath(SysParamConfig.getSysContent("picModelPath") + "/" + presetId);
        analysis.setIsAi(0);
        List<Analysis> analysisList = new ArrayList<>();
        analysisList.add(analysis);
        Map<String, List<Analysis>> analysisMap  = new HashMap<>(2);
        analysisMap.put("list",analysisList);
        log.info("算法信息:{}", analysisMap);
        Constant.algorithmTestPicPath = picPath;
        if (Objects.equals("-1", type)){
            return feignAlgorithm(analysisMap);
        }

        return feignDefect(analysisMap);
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

    @ApiOperation(value = "请求算法资源信息接口")
    @GetMapping(value = "/algorithmResource")
    @Logs(title = "查询算法资源信息",content = "根据用户传递的参数查询算法资源信息",logType = 1,authority = "1234")
    public Result algorithmResource(){
        Result result = new Result();
        try {
            result.setData(intelAnalysisService.algorithmResource());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法资源信息接口错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "请求系统自检信息接口")
    @GetMapping(value = "/systemCheck")
    @Logs(title = "查询系统自检信息",content = "根据用户传递的参数查询系统自检信息",logType = 1,authority = "1234")
    public Result systemCheck(HttpServletRequest request){
        Result result = new Result();
        try {
            Long userId=Long.valueOf(StringUtils.isBlank(request.getHeader("userId")) ? "10001" : request.getHeader("userId"));
            result.setData(intelAnalysisService.systemCheck(request, userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统自检信息接口错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "告警判断处理接口")
    @GetMapping(value = "/warnInfo")
    public Result alarmJudge(@RequestParam String value, @RequestParam(value = "valueDesc", required = false) String valueDesc, @RequestParam String stdDeviceMeteName,
                           @RequestParam String meteKind, @RequestParam Integer alarmState,
                           @RequestParam String stateZero, @RequestParam String stateOne,
                           @RequestParam Integer alarmLevel,
                           @RequestParam Float highLimit1, @RequestParam Float lowLimit1,
                           @RequestParam Float highLimit2, @RequestParam Float lowLimit2,
                           @RequestParam Float highLimit3, @RequestParam Float lowLimit3,
                           @RequestParam Float highLimit4, @RequestParam Float lowLimit4,
                           @RequestParam(defaultValue = "0") int alarmRuleType) {
        Result result = new Result();
        try {
            result.setData(analyseDataOperateService.alarmJudge(value, valueDesc, stdDeviceMeteName, meteKind, alarmState, stateZero, stateOne, alarmLevel,
                    highLimit1, lowLimit1, highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4, alarmRuleType));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("告警判断处理异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("告警判断处理错误:", e);
        }
        return result;
    }

    private Result upToAlgorithm(Long instanceId, String path) {
        Result result = new Result();
        try {
            Alarm alarmDetail = new Alarm();
            HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(instanceId);

            alarmDetail.setBay_name(nameMap.get("upRegionName"));
            alarmDetail.setDevice_name(nameMap.get("deviceName"));
            alarmDetail.setPoint_name(nameMap.get("meteName"));
            alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String yearMonth = ym.format(new Date());
            String nowTime = timeFormat.format(new Date());

            List<Different> differentList = new ArrayList<>();

            Different different = new Different();
            different.setX1(68);
            different.setY1(576);
            different.setX2(1848);
            different.setY2(1028);
            differentList.add(different);

            List<Defect> defectList = new ArrayList<>();
            Defect defect = new Defect();
            defect.setX1(512);
            defect.setY1(644);
            defect.setX2(679);
            defect.setY2(1125);
            defect.setType("gbps");

            int confidence = 86;
            defect.setConfidence(confidence);
            defect.setDesc("盖板破损或缺失" + "(坐标位置 " + defect.getX1() + "," +
                defect.getY1() + "," +
                defect.getX2() + "," +
                defect.getY2() + ";" +
                "置信度 " + confidence
                + "%)");
            defectList.add(defect);

            String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

            String remoteImgPath = applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "原图.jpg";
            String remoteBaseImgPath = applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/" + picF + "判别基准.jpg";
            String remoteDifResultPath = applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/" + picF + "判别告警.jpg";
            String remoteDefectFilepath = applicationProperties.getManagerAlgorithmConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "缺陷告警.jpg";

            ftpsService.uploadFile("原始图片",path+"/img.jpg",remoteImgPath);  //原始图片上传
            ftpsService.uploadFile("缺陷告警结果图片",path+"/defectResultImg.jpg",remoteDefectFilepath);  //缺陷告警
            ftpsService.uploadFile("判别基准图片",path+"/diffBaseImg.jpg",remoteBaseImgPath);  //判别基准图片上传
            ftpsService.uploadFile("判别结果图片",path+"/differentResulImg.jpg",remoteDifResultPath);  //判别结果图片

            alarmDetail.setPic_raw(remoteImgPath);         //图片原图
            alarmDetail.setPic_diff_base(remoteBaseImgPath);               //判别基准图路径
            alarmDetail.setPic_different(remoteDifResultPath);               //判别告警图路径,判别结果图
            alarmDetail.setPic_defect(remoteDefectFilepath);      //缺陷告警图路径//
            alarmDetail.setDifferent(differentList);
            alarmDetail.setDefect(defectList);

            log.info("算法发送测试消息：{} ", alarmDetail);
            algorithmService.pushAlarmMsg(alarmDetail);
            result.setMessage("success");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "与算法管理平台交互失败");
            log.info("与算法管理平台交互失败" + e);
        }
        return result;
    }

    /**
     * 算法参数获取
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @return 算法参数
     */
    @ApiOperation(value = "算法参数获取接口")
    @GetMapping(value = "/getAlgorithmParams")
    @Logs(title = "查询算法参数信息",content = "根据用户传递的参数查询算法参数信息",authority = "1234")
    public Result getAlgorithmParams(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
            result.setData(intelAnalysisService.getAlgorithmParams(type));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法参数获取接口错误:", e);
        }
        return result;
    }

    /**
     * 算法版本获取接口
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @param algorithmManufacturer 算法厂商 当值为空时， 代表获取所有厂商的算法历史版本
     * @return 算法版本信息
     */
    @ApiOperation(value = "算法版本获取接口")
    @GetMapping(value = "/getAlgorithmVersion")
    @Logs(title = "查询算法版本信息",content = "根据用户传递的参数查询算法版本信息",authority = "1234")
    public Result getAlgorithmVersion(@RequestParam(value = "type") String type,
                                      @RequestParam(value = "algorithmManufacturer") String algorithmManufacturer) {
        Result result = new Result();
        try {
            result.setData(intelAnalysisService.getAlgorithmVersion(type, algorithmManufacturer));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法版本获取接口错误:", e);
        }
        return result;
    }

    /**
     * 算法版本切换接口
     * @param type <1>:=状态类型 <2>:=缺陷类型
     * @param version 算法版本号
     * @return
     */
    @ApiOperation(value = "算法版本切换接口")
    @PostMapping(value = "/algorithmVersionChange")
    @Logs(title = "算法版本切换",content = "算法版本切换", logType = 5, authority = "1234")
    public Result algorithmVersionChange(@RequestParam(value = "type") String type,
                                         @RequestParam(value = "version") String version) {
        Result result = new Result();
        try {
            intelAnalysisService.algorithmVersionChange(type, version);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法版本切换接口错误:", e);
        }
        return result;
    }

}
