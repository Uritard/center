package com.yjh.platform.module.task.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Sets;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.CruiseTaskJob;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.websocket.WebSocketResult;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@Api("HelloController")
@RequestMapping("/hello")
public class HelloController {

    private Logger log = LoggerFactory.getLogger(HelloController.class);


    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";

    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    @Value("${picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${waitTime}")
    private Long waitTime;

    @ApiOperation("说hello")
    @PostMapping("/admin")
    @ResponseBody
    public String sayHello(@ApiParam(value = "ceshi ",required = true) @RequestParam String name){
        WebSocketServer.sendMsg(name+"hello！");
        JSONObject jsonObject = JSONObject.fromObject(WebSocketResult.builder().type("1").info(name +"  很好!").build());
        WebSocketServer.sendMsg(jsonObject.toString());
        log.info("转换时间："+Math.random()*100);
        return name +"你好!";
    }


    @ApiOperation("发任务")
    @PostMapping("/task")
    @ResponseBody
    public String task()throws Exception{
        Analysis analysis = new Analysis();
        analysis.setTaskId("9000009");
        analysis.setInstanceId(1001L);
        analysis.setPicPath("/home/yjh/iot-picture/model-picture/Template/Infrared/2AFE48DF21F64F78AB57396F6DCCAC06/bigi_0.jpg");//相机拍摄图片
        //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
        analysis.setAnalyseType("1");
        analysis.setPicModelPath("/home/yjh/iot-picture/model-picture/Template/Infrared/2AFE48DF21F64F78AB57396F6DCCAC06");//模板图片
        List<Analysis> analysisList = new ArrayList<>();
        analysisList.add(analysis);
        Map<String, List<Analysis>> analysisMap  = new HashMap<>();
        analysisMap.put("list",analysisList);
        log.info("发任务"+analysisMap);
        analysis(analysisMap);
        return "ok";
    }


    //表记分析
    private void analysis(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(ALGORITHM_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    //缺陷分析
    private void defect(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(DEFECT_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
