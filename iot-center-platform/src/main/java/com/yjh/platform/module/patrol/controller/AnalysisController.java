package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.FtpsService;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.configuration.ApplicationProperties;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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
    private final AnalysisService analysisService;
    @Autowired
    private final IntelAnalysisService intelAnalysisService;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    private final AbstractVideoCruise abstractVideoCruise;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AlarmService alarmService;
    @Autowired
    private FtpsService ftpsService;
    @Autowired
    private ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    public AnalysisController(AnalysisService analysisService, IntelAnalysisService intelAnalysisService, AbstractVideoCruise abstractVideoCruise) {
        this.analysisService = analysisService;
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
            if (analysisList.get(0).getInstanceId() == -1) {
                result.setData(analysisService.feignAlgorithm(analysisList, applicationProperties.getAlgorithmServerConfig().getNettyRecognizePort()));
            }else {
                // 开关
                String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
                if (StringUtils.equals(FLAG, flag)){
                    // 原来的socket协议
                    result.setData(analysisService.feignAlgorithm(analysisList, applicationProperties.getAlgorithmServerConfig().getNettyRecognizePort()));
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
            log.info("端口号：{}", applicationProperties.getAlgorithmServerConfig().getNettyAiPort());
            // 开关
            String flag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis","content"));
            if (StringUtils.equals(FLAG, flag)){
                // 原来的socket协议
                result.setData(analysisService.feignDefect(analysisList, applicationProperties.getAlgorithmServerConfig().getNettyAiPort()));
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

    @ApiOperation(value = "告警判断处理接口")
    @GetMapping(value = "/warnInfo")
    public Result alarmJudge(@RequestParam String value, @RequestParam(value = "valueDesc", required = false) String valueDesc, @RequestParam String stdDeviceMeteName,
                           @RequestParam String meteKind, @RequestParam Integer alarmState,
                           @RequestParam String stateZero, @RequestParam String stateOne,
                           @RequestParam Integer alarmLevel,
                           @RequestParam Float highLimit1, @RequestParam Float lowLimit1,
                           @RequestParam Float highLimit2, @RequestParam Float lowLimit2,
                           @RequestParam Float highLimit3, @RequestParam Float lowLimit3,
                           @RequestParam Float highLimit4, @RequestParam Float lowLimit4) {
        Result result = new Result();
        try {
            result.setData(analyseDataOperateService.alarmJudge(value, valueDesc, stdDeviceMeteName, meteKind, alarmState, stateZero, stateOne, alarmLevel,
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

            String remoteImgPath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "原图.jpg";
            String remoteBaseImgPath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/" + picF + "判别基准.jpg";
            String remoteDifResultPath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "判别" + "/" + yearMonth + "/" + picF + "判别告警.jpg";
            String remoteDefectFilepath = applicationProperties.getManagerMqttConfig().getManagerServerFtpsRemotePath() + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "缺陷告警.jpg";

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
            alarmService.PushMsg(alarmDetail);
            result.setMessage("success");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "与算法管理平台交互失败");
            log.info("与算法管理平台交互失败" + e);
        }
        return result;
    }

}
