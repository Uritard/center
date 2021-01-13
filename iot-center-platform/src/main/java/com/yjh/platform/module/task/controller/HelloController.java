package com.yjh.platform.module.task.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.collect.Sets;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.QrCodeUtils;
import com.yjh.platform.common.utils.ResultHandleUtils;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TWarnInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@Api("HelloController")
@RequestMapping("/hello")
public class HelloController {

    private Logger log = LoggerFactory.getLogger(HelloController.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    TStdMetemodelDetailDao tStdMetemodelDetailDao;
    @Autowired

    TCruisePointInstanceDao tCruisePointInstanceDao;


    //相机抓图selectForDictNote
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";

    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    QrCodeUtils qrCodeUtils = new QrCodeUtils();

    @ApiOperation(value = "测试一切")
    @RequestMapping(value = "/testAnything", method = RequestMethod.GET)
    public Result QrDecode() {
        Result result = new Result();
//        //List<Long> list = tCruiseTaskDao.selectTimeIsIn(new Date());
//        tCruisePointInstanceDao.selectForTask(list);
//        result.setData(list);
        return result;
    }

    @ApiOperation(value = "webSocketBroadcast测试")
    @RequestMapping(value = "/webSocketBroadcast", method = RequestMethod.POST)
    public Result webSocketBroadcast(@RequestParam(value = "type") String type,
                                     @RequestParam(value = "message") String message) {
        Result result = new Result();
        try {
            Map<String, Object> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", type);
            jasonMaps2.put("message", message);
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            ConcurrentHashMap<String,WebSocketServer> webSocketMap = WebSocketServer.getInstance().getWebSocketMap();
            for (String userId :webSocketMap.keySet()) {
                webSocketMap.get(userId).sendMessage(json);
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("webSocketBroadcast测试失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "webSocketgetUserId测试")
    @RequestMapping(value = "/webSocketgetUserId", method = RequestMethod.POST)
    public Result webSocketgetUserId() {
        Result result = new Result();
        try {
            ConcurrentHashMap<String,WebSocketServer> webSocketMap = WebSocketServer.getInstance().getWebSocketMap();
            log.info("webSocketMap：" + webSocketMap);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("webSocketgetUserId测试失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "二维码识别")
    @RequestMapping(value = "/QrDecode", method = RequestMethod.GET)
    public Result QrDecode(@RequestParam(value = "filePath") String filePath) {
        Result result = new Result();
        try {
            result.setData(qrCodeUtils.decodeQrCode(filePath));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("二维码识别:", e);
        }
        return result;
    }



    @ApiOperation("说hello")
    @PostMapping("/admin")
    @ResponseBody
    public Result sayHello(@RequestParam(value = "filePath") String filePath,@RequestParam String testString)  throws ParseException {
        Result result = new Result();
        ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
        tStdMetemodelDetailDao.selectForDictNote(resultHandler);
        Map<String, String> map = resultHandler.getMappedResults();
        System.out.println("map = " + map);
        result.setData(map);


        List<String> answer=new ArrayList<>();
        answer=DateTimeUtil.getDayDateList(10);
        log.info("Date："+answer);


        Long beginTime=System.currentTimeMillis();

        Long endTime=System.currentTimeMillis();

//        Map<String,Object> maps=redisTemplate.opsForHash().entries("t_cruise_task_result:fc7a466fa9d24ab3aeb0669f9c345c86:11000000436");
//        log.info("djkhwqedkfe:"+maps.get("remark"));
//        TCruiseTask tCruiseTask=new TCruiseTask();
//        tCruiseTask.setTaskId(maps.get("remark").toString());
//        log.info("class.....:"+testString.getClass());
//        log.info("content-length:"+testString.length());
//
//        log.info("Content表计:"+testString.matches("\\{\"msgData.*?\"2\"}"));
//        log.info("Content缺陷:"+testString.matches("\\{\"msgType.*?}}}}"));

//        log.info("Content前半:"+testString.matches());
//        log.info("Content后半:"+testString.matches());



//        String l1="{\"msgType\": \"4\", \"msgData\": {\"desNode\": \"clientSocket001\", \"srcNode\": \"serverSocket\", \"errorCode\": \"0\"}}{\"msgType\": \"2\", \"msgID\": \"100000003\", \"msgData\": {\"desNode\": \"clientSocket001\", \"srcNode\": \"serverSocket\", \"data\": {\"resultInfo1\": {\"taskId\": \"6d05cd9969d14a22a71c03d7ff10b94a\", \"instanceId\": \"11000000505\", \"analyseType\": \"3\", \"resultValue\": \"\"}}}}";
//        String l3=l1.replaceAll("\\s+","");
//        String l2=l3.replaceAll("\\{.*?\\}\\{","{");
//        log.info("String:"+l2);
//        log.info("String3:"+l3);
//
//
//
//        String meter="{9541}{\"msgData\":{\"data\":{\"resultInfo1\":{\"analyseType\":\"3\",\"instanceId\":\"11000000505\",\"resultValue\":\"--.7--\",\"taskId\":\"6d05cd9969d14a22a71c03d7ff10b94a\"}},\"desNode\":\"clientSocket001\",\"srcNode\":\"clientSocket001\"},\"msgID\":\"100000004\",\"msgType\":\"2\"}";
//        String defect="{\"msgType\": \"2\", \"msgID\": \"100000003\", \"msgData\": {\"desNode\": \"clientSocket001\", \"srcNode\": \"serverSocket\", \"data\": {\"resultInfo1\": {\"taskId\": \"6d05cd9969d14a22a71c03d7ff10b94a\", \"instanceId\": \"11000000505\", \"analyseType\": \"3\", \"resultValue\": \"\"}}}}";
////        String testString=",\"srcNode\":\"clientSocket001\"},\"msgID\":\"100000136\",\"msgType\":\"2\"}\n";
//
//
//
//        log.info("meter:"+meter.replaceAll("\\{\"msgData.*?\"2\"}","hahaha"));
//        log.info("----+++:"+meter.split("\\{\"msgData.*?\"2\"}").length);
//        log.info("defect:"+defect.replaceAll("\\{\"msgType.*?}}}}","123"));
//
////        log.info("Judge---"+defect.matches("^\\{\"msgType\"+.*?"));
//
//        log.info("Trans1:"+meter.matches("\\{\"msgData.*?\"2\"}"));
//        log.info("Trans2:"+defect.matches("\\{\"msgType.*?}}}}"));
//
//        log.info("Rule1:"+meter.matches("\\{\"msgData.*?"));
//        log.info("Rule2:"+meter.matches(".*?\"2\"}"));
//
//        log.info("result:"+testString.matches(".*?\"2\"}"));
//
//        log.info("testString:"+testString);
//
//
//        String trulyMessage="";
//        log.info("testString:"+testString);
//        if(testString.matches("\\{\"msgData.*?\"2\"}")){ //表计整包
//               log.info("整包数据1");
//        }else if (testString.matches("\\{\"msgType.*?}}}}")){ //缺陷整包
//               log.info("整包数据2");
//        }else { //拆包
//            if(testString.matches("\\{\"msgData.*?") || testString.matches("\\{\"msgType.*?")){ //拆包A
//
//                redisTemplate.opsForHash().put("algoResponse","A",testString);
//                log.info("获取上半包数据");
//            }else if (testString.matches(".*?\"2\"}") || testString.matches(".*?}}}}") |testString.matches("}")){ //拆包B
//
//                redisTemplate.opsForHash().put("algoResponse","B",testString);
//                String success=(redisTemplate.opsForHash().entries("algoResponse")).get("A").toString().concat(testString);
////                redisTemplate.delete("algoResponse");
//                log.info("success:"+success);
//                trulyMessage=success;
//            }
//        }
//
//      if(trulyMessage!="") {
//          JSONObject jsonObject = JSON.parseObject(trulyMessage);
//          log.info("JSON对象1：" + jsonObject);
//          if (jsonObject.get("msgType").toString().equals("2")) {
//              JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.get("msgData").toString()).get("data").toString()); //全量数据结果集
//              log.info("原生数据****：" + jsonObjectData);
//              Iterator iterator = jsonObjectData.entrySet().iterator();//迭代器取出data中的每一个resultInfo
//              while (iterator.hasNext()) {
//                  Map.Entry entry = (Map.Entry) iterator.next();
//                  //遍历每一个结果子集
//                  JSONObject jsonObjectResult = JSON.parseObject(entry.getValue().toString());
//                  log.info("数据****：" + jsonObjectResult);//打印resultInfo
//              }
//          }
//      }
//

//        redisTemplate.opsForList().leftPush("constant","helloWorld");
//        redisTemplate.opsForList().leftPush("constant","JAVA");
//
//        List<String> list=redisTemplate.opsForList().range("constant",0,redisTemplate.opsForList().size("constant")-1);
//        log.info("list:"+list);


//        Float value=Float.valueOf("17.612");
//        Float highLimit1=Float.valueOf("20");
//        Float lowLimit1=Float.valueOf("10");
//        Float highLimit2=Float.valueOf("25");
//        Float lowLimit2=Float.valueOf("5");
//        Float highLimit3=Float.valueOf("30");
//        Float lowLimit3=Float.valueOf("4");
//        Float highLimit4=Float.valueOf("40");
//        Float lowLimit4=Float.valueOf("1");
//
//        Boolean emergency1 = false;
//        Boolean emergency2 = false;
//        Boolean worse1 = false;
//        Boolean worse2 = false;
//        Boolean general1 = false;
//        Boolean general2 = false;
//        Boolean warns1 = false;
//        Boolean warns2 = false;
//
//        if (Objects.nonNull(lowLimit4))
//            emergency1 = value <= lowLimit4;
//        if (Objects.nonNull(highLimit4))
//            emergency2 = value >= highLimit4;
//        if (Objects.nonNull(lowLimit3))
//            worse1 = value <= lowLimit3;
//        if (Objects.nonNull(highLimit3))
//            worse2 = value >= highLimit3;
//        if (Objects.nonNull(lowLimit2))
//            general1 = value <= lowLimit2;
//        if (Objects.nonNull(highLimit2))
//            general2 = value >= highLimit2;
//        if (Objects.nonNull(lowLimit1))
//            warns1 = value <= lowLimit1;
//        if (Objects.nonNull(highLimit1))
//            warns2 = value >= highLimit1;
//
//
//        if (emergency1 || emergency2) {
//            log.info("危急");
//        } else if (worse1 || worse2) {
//            log.info("严重");
//        } else if (general1 || general2) {
//            log.info("一般");
//        } else if (warns1 || warns2) {
//            log.info("预警");
//        } else {
//            log.info("正常");
//        }

        return result;
    }


    @ApiOperation("发任务")
    @PostMapping("/task")
    @ResponseBody
    public Result task() throws Exception {
        Result result = new Result();
        Map<String,Object> map = new HashMap<>();
        List<Long> list = new ArrayList<>();
        map.put("list",list);
        result.setData(map);
        return result;
    }


    @ApiOperation(value = "联动webSocket测试")
    @RequestMapping(value = "/linkWebSocket", method = RequestMethod.POST)
    public Result linkWebSocket(@RequestParam String taskId) {
        Result result = new Result();
        try {
            Map<String, Object> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "linkagePopUp");
            jasonMaps2.put("unionId", taskId);
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            WebSocketServer.sendMsg(json);
//            result.setData(tCfgDataCurrentService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
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
