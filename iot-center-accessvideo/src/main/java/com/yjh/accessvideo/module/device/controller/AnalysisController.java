package com.yjh.accessvideo.module.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.mqtt.AlarmService;
import com.yjh.accessvideo.common.mqtt.GetSpringUtil;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Defect;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Different;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.entity.interlanalysis.Response;
import com.yjh.accessvideo.module.device.entity.interlanalysis.UpdateRequest;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import com.yjh.accessvideo.netty.client.DataDealThread;
import com.yjh.accessvideo.service.ftpsservice;
import com.yjh.accessvideo.thread.TaskExecutePool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    @Autowired
    private com.yjh.accessvideo.service.ftpsservice ftpsservice;


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

    /**
     * webSocket请求地址
     */
    @Value("${system.webSocket.url}")
    private String syncWebsocketUrl;

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

    /**
     *  算法主机-判别告警
     *
     * @return ResponseEntity
     */
    @GetMapping(value = "/PBtTest")
    public void PBTest(@RequestParam(value = "间隔名称",required = false) String bayName,
                       @RequestParam(value = "设备名称", required = false) String devicename,
                       @RequestParam(value = "测点名称", required = false) String pointName,
                       @RequestParam(value = "原图", required = false) String origpicpath,
                       @RequestParam(value = "判别基准图路径", required = false) String judgeBaseImagepath,
                       @RequestParam(value = "判别结果图", required = false) String resultImagebak
    ) {
        try {
            String year=Integer.toString(LocalDate.now().getYear());
            String month=Integer.toString(LocalDate.now().getMonthValue());
            String[] str2=origpicpath.split("/");
            String origpcimagename=str2[str2.length-1];
            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+year+"/"+month+"/"+origpcimagename;

            String[] str=judgeBaseImagepath.split("/");
            String imagename=str[str.length-1];
            String remotebaseimagicpath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+year+"/"+month+"/"+imagename;

            str=resultImagebak.split("/");
            imagename=str[str.length-1];
            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+year+"/"+month+"/"+imagename;


            Alarm alarmDetail=new Alarm();

            List<Different> defectList1=new ArrayList<>();
            Different different= new Different();
            different.setX1(11);
            different.setY1(11);
            different.setX2(22);
            different.setY2(22);
            defectList1.add(different);
            alarmDetail.setBay_name(bayName);
            alarmDetail.setDevice_name(devicename);
            alarmDetail.setPoint_name(pointName);
            alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
            alarmDetail.setPic_diff_base(remotebaseimagicpath);               //判别基准图路径
            alarmDetail.setPic_different(remotefilepath);               //判别告警图路径,判别结果图
            alarmDetail.setPic_defect("");
            alarmDetail.setPic_height(1080);
            alarmDetail.setPic_width(1920);
            alarmDetail.setDifferent(defectList1);
            log.info("判别消息：{}",alarmDetail);
            ftpsservice.uploadFile("判别告警",origpicpath,remoteorigfilepath);  //原始图片上传
            ftpsservice.uploadFile("判别告警",judgeBaseImagepath,remotebaseimagicpath);  //判别基准图片上传
            ftpsservice.uploadFile("判别告警",resultImagebak,remotefilepath);

            AlarmService alarmService= GetSpringUtil.getBean("alarmService");
            alarmService.PushMsg(alarmDetail);
            log.info("发送算法管理平台结束");
        }catch (Exception e){
            log.error(e.getMessage());
            Response.serverError();
        }
    }

    /**
     *  算法主机-缺陷告警
     *
     * @return ResponseEntity
     */
    @GetMapping(value = "/QXtTest")
    public void QXTest(@RequestParam(value = "间隔名称",required = false) String bayName,
                       @RequestParam(value = "设备名称", required = false) String devicename,
                       @RequestParam(value = "测点名称", required = false) String pointName,
                       @RequestParam(value = "原图", required = false) String origpicpath,
                       @RequestParam(value = "缺陷名称", required = false) String defectValue,
                       @RequestParam(value = "缺陷名称拼音首字母", required = false) String defectValueF,
                       @RequestParam(value = "置信度", required = false) int confdence,
                       @RequestParam(value = "缺陷结果图", required = false) String resultImagebak
    ) {
        try {

            String year=Integer.toString(LocalDate.now().getYear());
            String month=Integer.toString(LocalDate.now().getMonthValue());

            String[] str2=origpicpath.split("/");
            String origpcimagename=str2[str2.length-1];
            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+year+"/"+month+"/"+origpcimagename;

            String[] str=resultImagebak.split("/");
            String imagename=str[str.length-1];
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+year+"/"+month+"/"+imagename;

            Alarm alarmDetail=new Alarm();

            List<Defect> defectList1=new ArrayList<>();
            Defect defect= new Defect();
            defect.setX1(11);
            defect.setY1(11);
            defect.setX2(22);
            defect.setY2(22);
            defect.setDesc(defectValue);
            defect.setConfidence(confdence);
            defect.setType(defectValueF);
            defectList1.add(defect);
            alarmDetail.setBay_name(bayName);
            alarmDetail.setDevice_name(devicename);
            alarmDetail.setPoint_name(pointName);
            alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
            alarmDetail.setPic_defect(remotefilepath);
            alarmDetail.setDefect(defectList1);
            alarmDetail.setPic_height(1080);
            alarmDetail.setPic_width(1920);
            log.info("判别消息：{}",alarmDetail);

            ftpsservice.uploadFile("遥信告警",origpicpath,remoteorigfilepath);
            ftpsservice.uploadFile("遥信告警",resultImagebak,remotefilepath);

            AlarmService alarmService= GetSpringUtil.getBean("alarmService");
            alarmService.PushMsg(alarmDetail);
            log.info("发送算法管理平台结束");
        }catch (Exception e){
            log.error(e.getMessage());
            Response.serverError();
        }
    }

    /**
     *  算法主机-缺陷告警
     *
     * @return ResponseEntity
     */
    @GetMapping(value = "/algorithm-test")
    public void algorithmTest(@RequestParam(value = "picPath",required = false) String picPath,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "instanceId", required = false) Long instanceId

    ) {
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

    /**
     *  算法主机-算法更新
     */
    @GetMapping(value = "/algorithm-update")
    public void algorithmUpdateTest(@RequestParam(value = "filePath") String filePath){
        UpdateRequest request = new UpdateRequest()
                .setRequestHostIp("192.168.1.66")
                .setRequestHostPort("18715")
                .setRequestId(String.valueOf(UUID.randomUUID()))
                .setAlgorithmPath(filePath);
        StaticContextAccessor.getBean(IntelAnalysisService.class).algorithmUpdate(request);
    }

}
