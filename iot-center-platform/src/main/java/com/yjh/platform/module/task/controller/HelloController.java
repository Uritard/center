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
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.*;
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
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;


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
    public String sayHello(@ApiParam(value = "ceshi ", required = true) @RequestParam String name, @ApiParam(value = "ceshi ", required = true) @RequestParam String name2) {
        WebSocketServer.sendMsg(name + "hello！");
        JSONObject jsonObject = JSONObject.fromObject(WebSocketResult.builder().type("1").info(name + "  很好!").build());
        WebSocketServer.sendMsg(jsonObject.toString());
        log.info("转换时间：" + Math.random() * 100);

//       log.info("RedisList:"+redisTemplate.opsForList().leftPushIfPresent("AnalysisList：9000001","1000003"));
//       redisTemplate.opsForList().leftPushAll("AnalysisList：9000001","1000003");
//       redisTemplate.opsForList().remove("AnalysisList：9000001",0,"1000003");

//        if(name2.equals("2")){
//            log.info("1"+name2);
//            redisTemplate.opsForList().remove("AnalysisList：9000001", 0, "1000001");
//            redisTemplate.opsForList().rightPush("AnalysisList：9000001","0");
//        }else if(name2.equals("6")){
//            log.info("2"+name2);
//            redisTemplate.opsForList().leftPush("AnalysisList：9000001","-1");
//        }
//        if (redisTemplate.opsForList().index("AnalysisList：9000001", 0).equals("-1") && redisTemplate.opsForList().index("AnalysisList：9000001",1).equals("0")) {
//            log.info("执行数据操作");
//        }
//        String value = "wcaqm 655 68 872 314 wcgz 529 272 1022 1078";
//        String[]value1 = value.split("[^a-z^A-Z]");
//        log.info("lenth:"+value1.length);
//        for(int i=0;i<value1.length;i++){
//            log.info("result:"+value1[i]);
//        }


//        Set<Integer> index = new HashSet<>();
//        index.add(value1.indexOf("wcaqm"));
//        index.add(value1.indexOf("wcgz"));
//        index.add(value1.indexOf("yydd"));
//        index.add(value1.indexOf("xy"));
//        index.add(value1.indexOf("slydmyw"));
//        index.add(value1.indexOf("ywnc"));
//        index.add(value1.indexOf("ywgkxfw"));
//        index.add(value1.indexOf("jyzbmwh"));
//        index.add(value1.indexOf("jyzpl"));
//        index.add(value1.indexOf("jyzlw"));
//        index.add(value1.indexOf("hxqgjbs"));
//        index.add(value1.indexOf("hxqgjtps"));
//        index.add(value1.indexOf("ywztyfyc"));
//        index.add(value1.indexOf("bjbpmh"));
//        index.add(value1.indexOf("bjbpps"));
//        index.add(value1.indexOf("bjwkps"));
//        index.add(value1.indexOf("mcqdmsh"));
//        index.add(value1.indexOf("gbps"));
//        index.add(value1.indexOf("gjptwss"));
//        index.add(value1.indexOf("xmbhyc"));
//        index.add(value1.indexOf("jsxs"));
//        log.info("Set:"+index);
//
//        String[] str1 = value.split("[0-9]");
//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < str1.length; i++) {
//            stringBuffer.append(str1[i]);
//        }
//        String value2 = stringBuffer.toString();
//        String[] str2 = value2.split(" ");
//        stringBuffer.delete(0, stringBuffer.length() - 1);
//        for (int i = 0; i < str2.length; i++) {
//            stringBuffer.append(str2[i]);
//        }
//
//
//        for(Integer ind:index){
//            if (ind.equals(-1)){
//                continue;
//            }else {
//                switch (str2[ind]){
//                    case "wcaqm":
//                        defectValue=defectValue+"未穿安全帽"+" ";
//                       break;
//                    case "wcgz":
//                        defectValue=defectValue+"未穿工装"+" ";
//                       break;
//                    case "rydd":
//                        defectValue=defectValue+"人员倒地"+" ";
//                        break;
//
//                }
//            }
//        }
//        log.info("结果---:" + defectValue);


//        String str="0.32";
//        log.info("Result:"+str.matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$"));
      
        return name + "你好!";
    }


    @ApiOperation("发任务")
    @PostMapping("/task")
    @ResponseBody
    public String task() throws Exception {
        TCruiseTask newTask = tCruiseTaskDao.selectByPrimaryId("c1ce5bf2a4c44d06a5635c847f9e3e66");//获取任务
        newTask.setTaskId(null);
        tCruiseTaskDao.insert(newTask);
        System.out.println(newTask.getTaskId());
        return newTask.getTaskId();
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
