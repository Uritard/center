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
import com.yjh.platform.module.task.entity.TCruiseResult;
import com.yjh.platform.module.task.entity.TCruiseTaskResult;
import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
import com.yjh.platform.module.task.service.TCruiseResultService;
import com.yjh.platform.module.task.service.TCruiseTaskResultService;
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
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@Api("HelloController")
@RequestMapping("/hello")
public class HelloController {

    private Logger log = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private RedisTemplate redisTemplate;

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";

    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";


    @ApiOperation("说hello")
    @PostMapping("/admin")
    @ResponseBody
    public String sayHello(@ApiParam(value = "ceshi ", required = true) @RequestParam String name,@ApiParam(value = "ceshi ", required = true)@RequestParam String name2) {
        WebSocketServer.sendMsg(name + "hello！");
        JSONObject jsonObject = JSONObject.fromObject(WebSocketResult.builder().type("1").info(name + "  很好!").build());
        WebSocketServer.sendMsg(jsonObject.toString());
        log.info("转换时间：" + Math.random() * 100);

//       log.info("RedisList:"+redisTemplate.opsForList().leftPushIfPresent("AnalysisList：9000001","1000003"));
//       redisTemplate.opsForList().leftPushAll("AnalysisList：9000001","1000003");
//       redisTemplate.opsForList().remove("AnalysisList：9000001",0,"1000003");

        if(name2.equals("2")){
            log.info("1"+name2);
            redisTemplate.opsForList().remove("AnalysisList：9000001", 0, "1000001");
            redisTemplate.opsForList().rightPush("AnalysisList：9000001","0");
        }else if(name2.equals("6")){
            log.info("2"+name2);
            redisTemplate.opsForList().leftPush("AnalysisList：9000001","-1");
        }
        if (redisTemplate.opsForList().index("AnalysisList：9000001", 0).equals("-1") && redisTemplate.opsForList().index("AnalysisList：9000001",1).equals("0")) {
            log.info("执行数据操作");
        }
//
//        String str="{\n\"msgType\": \"5\", \n\"msgData\": {\n\"instanceId\": " + "\"" + 111 + "\", \n\"taskId\": \"" + 222 + "\"\n}\n}\n";
//        log.info("JsonObject:"+ JSON.parseObject(JSON.parseObject(str).get("msgData").toString()).get("taskId").toString());
        return name + "你好!";
    }


    @ApiOperation("发任务")
    @PostMapping("/task")
    @ResponseBody
    public String task() throws Exception {
        Analysis analysis = new Analysis();
        analysis.setTaskId("9000009");
        analysis.setInstanceId(1001L);
        analysis.setPicPath("/home/yjh/iot-picture/resultImg/281020201444030724895.jpg");//相机拍摄图片
        //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
        analysis.setAnalyseType("1");
        analysis.setPicModelPath("/home/yjh/iot-picture/sync/Template/BigImg/21000000096");//模板图片
        List<Analysis> analysisList = new ArrayList<>();
        analysisList.add(analysis);
        Map<String, List<Analysis>> analysisMap = new HashMap<>();
        analysisMap.put("list", analysisList);
        log.info("发任务" + analysisMap);
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


    //读批量redis
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
