package com.yjh.accessvideo.module.device.controller;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.mqtt.AlarmService;
import com.yjh.accessvideo.common.mqtt.GetSpringUtil;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Defect;
import com.yjh.accessvideo.common.mqtt.alarmMsgBody.Different;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.entity.interlanalysis.*;
import com.yjh.accessvideo.module.device.service.IntelAnalysisService;
import com.yjh.accessvideo.service.ftpsservice;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

    @Autowired
    private final IntelAnalysisService intelAnalysisService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private com.yjh.accessvideo.service.ftpsservice ftpsservice;
    @Autowired
    private  AnalysisController analysisController;

    private static final String FLAG = "true";

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
//            String defectFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelDefectAnalysis","content"));
//            if (StringUtils.equals(FLAG, defectFlag)){
                intelAnalysisService.picAnalyseRetNotify(response);
//            }
//
//            String algorithmFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isIntelAlgorithmAnalysis","content"));
//            if (StringUtils.equals(FLAG, algorithmFlag)){
//                log.info("response==={}", response);
//            }
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
        }catch (Exception e){
            log.error(e.getMessage());
            Response.serverError();
        }
        return Response.ok();
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
            different.setX1("11");
            different.setY1("11");
            different.setX2("22");
            different.setY2("22");
            defectList1.add(different);
            alarmDetail.setBay_name(bayName);
            alarmDetail.setDevice_name(devicename);
            alarmDetail.setPoint_name(pointName);
            alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
            alarmDetail.setPic_diff_base(remotebaseimagicpath);               //判别基准图路径
            alarmDetail.setPic_different(remotefilepath);               //判别告警图路径,判别结果图
            alarmDetail.setPic_defect("");
            alarmDetail.setPic_height("1080");
            alarmDetail.setPic_width("1920");
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
                     @RequestParam(value = "置信度", required = false) String confdence,
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
            defect.setX1("11");
            defect.setY1("11");
            defect.setX2("22");
            defect.setY2("22");
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
            alarmDetail.setPic_height("1080");
            alarmDetail.setPic_width("1920");
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
    public void algorithmTest(@RequestParam(value = "原图路径",required = false) String picPath,
                       @RequestParam(value = "分析类型", required = false) String type,
                       @RequestParam(value = "基准图片", required = false) String basePath

    ) {
        Analysis analysis = new Analysis();
        analysis.setTaskId("666666");
        analysis.setInstanceId(666666L);
        analysis.setPicPath(picPath);
        analysis.setAnalyseType(type);
        analysis.setPicModelPath("");
        analysis.setIsAi(0);
        List<Analysis> analysisList = new ArrayList<>();
        analysisList.add(analysis);
        Map<String, List<Analysis>> analysisMap  = new HashMap<>();
        analysisMap.put("list",analysisList);
        log.info("算法信息：    "+analysisMap);
        Constant.algorithmTestPicPath = picPath;
        Constant.algorithmTestBasePicPath = basePath;
        analysisController.feignDefect(analysisMap);
    }

}
