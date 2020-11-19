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


//        String defectValue="";
//        String value = "ywzt_yfyc 998 787 bj_bpmh 765 bj_bpps 765 9876 bj_wkps 987 mcqdmsh 876 gbps 765 gjptwss 7898 xmbhyc 87 998 jsxs 887 564";
//        String finalValue = value.replaceAll("[0-9]", "");
//        log.info("final:"+finalValue);
//        String[] str2 = finalValue.split("\\s+");
//        log.info("length:"+str2.length);
//        for (int i = 0; i < str2.length; i++) {
//            log.info("str[]:"+str2[i]);
//            switch (str2[i]) {
//                case "wcaqm":
//                    defectValue = defectValue + "未穿安全帽" + " ";
//                    break;
//                case "wcgz":
//                    defectValue = defectValue + "未穿工装" + " ";
//                    break;
//                case "rydd":
//                    defectValue = defectValue + "人员倒地" + " ";
//                    break;
//                case "xy":
//                    defectValue = defectValue + "吸烟" + " ";
//                    break;
//                case "sly_dmyw":
//                    defectValue = defectValue + "地面油污" + " ";
//                    break;
//                case "yw_nc":
//                    defectValue = defectValue + "鸟窝" + " ";
//                    break;
//                case "yw_gkxfw":
//                    defectValue = defectValue + "飘挂物" + " ";
//                    break;
//                case "jyz_bmwh":
//                    defectValue = defectValue + "绝缘子-表面污秽" + " ";
//                    break;
//                case "jyz_pl":
//                    defectValue = defectValue + "绝缘子-破裂" + " ";
//                    break;
//                case "jyz_lw":
//                    defectValue = defectValue + "绝缘子-裂纹" + " ";
//                    break;
//                case "hxq_gjbs":
//                    defectValue = defectValue + "呼吸器-硅胶变色" + " ";
//                    break;
//                case "hxq_gjtps":
//                    defectValue = defectValue + "呼吸器-硅胶筒破损" + " ";
//                    break;
//                case "ywzt_yfyc":
//                    defectValue = defectValue + "油位状态-油位异常" + " ";
//                    break;
//                case "bj_bpmh":
//                    defectValue = defectValue + "表计-表盘模糊" + " ";
//                    break;
//                case "bj_bpps":
//                    defectValue = defectValue + "表计-表盘破损" + " ";
//                    break;
//                case "bj_wkps":
//                    defectValue = defectValue + "表计-外壳破损" + " ";
//                    break;
//                case "mcqdmsh":
//                    defectValue = defectValue + "门窗墙地面损坏" + " ";
//                    break;
//                case "gbps":
//                    defectValue = defectValue + "盖板破损" + " ";
//                    break;
//                case "gjptwss":
//                    defectValue = defectValue + "构架爬梯未上锁" + " ";
//                    break;
//                case "xmbhyc":
//                    defectValue = defectValue + "箱门闭合异常" + " ";
//                    break;
//                case "jsxs":
//                    defectValue = defectValue + "金属锈蚀" + " ";
//                    break;
//                default:
//                    log.info("缺陷："+"null");
//            }
//        }
//        log.info("缺陷："+defectValue);


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
