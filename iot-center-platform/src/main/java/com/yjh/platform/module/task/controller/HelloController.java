package com.yjh.platform.module.task.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.HttpClientUtils;
import com.yjh.platform.common.utils.QrCodeUtils;
import com.yjh.platform.common.utils.ResultHandleUtils;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TWarnInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


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

    @Value("http://192.168.33.241:800/PSIA/Custom/SelfExt/AS/VQDDiagnose/queryStatus")
    private String URL;


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
        try{
//        Map<String,String> jasonMap=new HashMap<>();
//        jasonMap.put("type","noTask");
//        //jasonMap.put("taskId",tCruiseTask.getTaskId());
//        String json= JSON.toJSONString(jasonMap);
        Map<String,String> jsonMap=new HashMap<>();
        jsonMap.put("type","taskAre");
        jsonMap.put("taskId","taskId");
        String jsonForTaskAre= JSON.toJSONString(jsonMap);
        log.info("任务超期的消息：   "+jsonForTaskAre);
        //Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jsonMap);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jsonMap);

        // webSocket通知前端调用巡视监控的接口
        Map<String,String> jasonMapOnFinished=new HashMap<>();
        jasonMapOnFinished.put("type","finishedOneInstance");
        jasonMapOnFinished.put("taskId","tCruiseTask.getTaskId()");
        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息："+jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
        }catch (Exception e){
            System.out.println("发送websocket出错");
        }
        return result;
    }

    @ApiOperation(value = "webSocketBroadcast测试")
    @RequestMapping(value = "/webSocketBroadcast", method = RequestMethod.POST)
    public Result webSocketBroadcast(@RequestParam(value = "type") String type,
                                     @RequestParam(value = "message") String message,
                                     @RequestParam(value = "warnId") String warnId) {
        Result result = new Result();
        try {
            Map<String, Object> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", type);
            jasonMaps2.put("message", message);
            jasonMaps2.put("warnId", warnId);
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
//            ConcurrentHashMap<String,WebSocketServer> webSocketMap = WebSocketServer.getInstance().getWebSocketMap();
//            for (String userId :webSocketMap.keySet()) {
//                webSocketMap.get(userId).sendMessage(json);
//            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("webSocketBroadcast测试失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "webSocket测试")
    @RequestMapping(value = "/webSocketTestAnything", method = RequestMethod.POST)
    public Result webSocketTestAnything(@RequestBody Map<String,String> map) {
        Result result = new Result();
        try {
//            Map<String, Object> jasonMaps2 = new HashMap<>();
//            jasonMaps2.put("type", type);
//            jasonMaps2.put("message", message);
//            jasonMaps2.put("warnId", warnId);

            //结果
//            {"type": "newSequentialResult",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合",
//                    "identifyResult": "合"
//            }
            //顺控
//            {"type": "newSequential",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合"
//            }

            String json = JSON.toJSONString(map);
            log.info("发送给前端的消息：" + json);
            try{
                String url = Constant.WEBSOCKET_URL;
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,map);
            }catch (Exception e){
                System.out.println("发送websocket出错");
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
            //ConcurrentHashMap<String,WebSocketServer> webSocketMap = WebSocketServer.getInstance().getWebSocketMap();
            //log.info("webSocketMap：" + webSocketMap);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("webSocketgetUserId测试失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "二维码识别")
    @RequestMapping(value = "/QrDecode", method = RequestMethod.GET)
    @Logs(title = "插入",content = "根据用户传递的参数新增",logType = 2)
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
    public Result sayHello(@RequestParam(value = "filePath") String filePath, @RequestParam String testString) {
        Result result = new Result();
        ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
        tStdMetemodelDetailDao.selectForDictNote(resultHandler);
        Map<String, String> map = resultHandler.getMappedResults();
        System.out.println("map = " + map);
        result.setData(map);
        Long beginTime=System.currentTimeMillis();

//        Map<String,List<Analysis>> analysisInfo=new HashMap<>();
//        List<Analysis> analysisList=new ArrayList<>();
//        Analysis analysis=new Analysis();
//        analysis.setTaskId("110001");
//        analysis.setInstanceId(Long.valueOf("910009"));
//        analysis.setPicModelPath("/home/yjh/iot-picture/model-picture/sync/Template/BigImg/21000000017" +
//                " ");
//        analysis.setAnalyseType("9");
//        analysis.setPicPath("/home/yjh_iot_center/iot-picture/resultImg/20210325020936977.jpg");
//        analysis.setCsvPath("https://192.168.9.40:443/imgs/infrared/20210325020936977.data");
//        analysis.setDataPath("https://192.168.9.40:443/imgs/infrared/20210325020936977.data");
//        analysis.setIsAi(1);
//
//        analysisList.add(analysis);
//        analysisInfo.put("list",analysisList);
//        try {
//            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
//            if (null != serviceRestTemplate) {
//                String str=serviceRestTemplate.postForObject(ALGORITHM_URL, analysisInfo, String.class);
//                result.setData(str);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }



//        Date date=new SimpleDateFormat("yyyyMMddhhmmssSSS").parse(testString);
//        String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
//        Date date1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
//        result.setData(time);
//        log.info("data"+date);
//        log.info("data1"+date1);


        return result;
    }

//    @ApiOperation("红外测温")
//    @GetMapping("/hotData")
//    public String thermalDataRevise(@RequestParam String value){
//
//        Double longValue=Double.valueOf(value);
//        Double  longRevise=(longValue/10);
//        String[] stringRevise=longRevise.toString().split("\\.");
//        Double  finalValue=longValue-Long.valueOf(stringRevise[0]);
//
//        return finalValue.toString();
//
//    }

    @Autowired
    TCruiseResultDao tCruiseResultDao;
    @Autowired
    TCruiseTaskResultDao tCruiseTaskResultDao;


    private Result sendTaskStateToUp(TCruiseTask tCruiseTask, Integer state){
        //任务状态上报站端
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String,Object>> items= new ArrayList<>();
        Map<String,Object> item = new HashMap<>();
        xmlBaseModel.setType("41");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
        item.put("task_patrolled_id",tCruiseTask.getTaskId()+"_"+simpleDateFormat2.format(tCruiseTask.getStartTime()));
        item.put("task_name",tCruiseTask.getTaskName());
        item.put("task_code",tCruiseTask.getTaskCode());
        item.put("task_state",state);
        item.put("plan_start_time",tCruiseTask.getStartTime());
        if(tCruiseTask.getIfRun() == 172){
            try{
                CronExpression expression = new CronExpression(tCruiseTask.getDateType());
                item.put("start_time",expression.getNextValidTimeAfter(new Date()));
            }catch (Exception e){
                log.info("上报出错"+e.getMessage());
            }
        }else {
            item.put("start_time",tCruiseTask.getStartTime());
        }
        item.put("task_progress","0%");
        Integer i =0;
        Map<String,String> mapForGet = redisTemplate.opsForHash().entries("countForAbnormal:"+tCruiseTask.getTaskId());
        Integer all = Integer.valueOf(mapForGet.get("all"));
        Integer normal = Integer.valueOf(mapForGet.get("normal"));
        Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
        i = all -normal -abnormal;


        item.put("task_estimated_time",i*60*5);
        item.put("description","");
        items.add(item);
        xmlBaseModel.setItems(items);

        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String,List<XMLBaseModel>> map = new HashMap<>();
        map.put("list",list);
        Result re = null;
        try{
            log.info("信息上报：-"+map);
            re = Constant.otherServer(map,Constant.TCP_URL);//江苏要求
        }catch (Exception e){
            log.info("上报出错"+e.getMessage());
        }
        return re;
    }
    @ApiOperation("发任务")
    @PostMapping("/task")
    @ResponseBody
    public Result task() throws Exception {
        Result result = null;
        TCruiseResult tCruiseResult = tCruiseResultDao.selectByPrimaryId("0b4f4a0a1fe14338b93108756cea37eb");
        tCruiseResult.setCState(244);
        tCruiseResult.setTaskWait(0);
        tCruiseResultDao.update(tCruiseResult);

        TCruiseTask tCruiseTask =tCruiseTaskDao.selectByPrimaryId("6e3f89c71be741b390932ba457fbe1c6");
        String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
        Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
        //String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
        //任务状态
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TCruiseTaskResult tCruiseTaskResult ;
        tCruiseTaskResult = tCruiseTaskResultDao.selectByPrimaryId(tCruiseResult.getTaskResultId());
        if(tCruiseTaskResult == null){
            tCruiseTaskResult = new TCruiseTaskResult();
            tCruiseTaskResult.setTaskResultId(tCruiseResult.getTaskResultId());
            tCruiseTaskResult.setTaskId(tCruiseTask.getTaskId());
            tCruiseTaskResult.setTaskAbnormal(6);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
            tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(mapForGet.get("taskStart")));
            tCruiseTaskResult.setTaskStatus(244);
            tCruiseTaskResult.setCruiseResult(247);
            tCruiseTaskResultDao.insert(tCruiseTaskResult);
        }else {
            tCruiseTaskResult.setTaskResultId(tCruiseResult.getTaskResultId());
            tCruiseTaskResult.setTaskId(tCruiseTask.getTaskId());
            tCruiseTaskResult.setTaskAbnormal(6);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
            tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(mapForGet.get("taskStart")));
            tCruiseTaskResult.setTaskStatus(244);
            tCruiseTaskResult.setCruiseResult(247);
            tCruiseTaskResultDao.update(tCruiseTaskResult);
        }

        sendTaskStateToUp(tCruiseTask,6);

        log.info("超期完毕");
        return result;
    }



    @ApiOperation(value = "联动webSocket测试")
    @RequestMapping(value = "/linkWebSocket", method = RequestMethod.POST)
    public Result linkWebSocket(@RequestParam String taskId) {
        Result result = new Result();
        try {
            Map<String, String> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "linkagePopUp");
            jasonMaps2.put("unionId", taskId);
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMaps2);
            }catch (Exception e){
                System.out.println("发送websocket出错");
            }
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



    public Result sendPostRequest(String url,HashMap<String,Long> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
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
    @ApiOperation(value = "判断系统是哪个操作系统")
    @RequestMapping(value = "/testIsWhatOS", method = RequestMethod.GET)
    public Result testIsWhatOS() throws Exception{
        Result result = new Result();
        result.setData("此系统是： "+System.getProperty("os.name")+"; ip是:"+ InetAddress.getLocalHost().getHostAddress());
        return result;
    }
    @ApiOperation(value = "异常日志测试")
    @RequestMapping(value = "/testErrorLog", method = RequestMethod.GET)
    @Logs(title = "日志测试",content = "异常日志",logType = 2)
    public Result testErrorLog() throws Exception{
        Result result = new Result();
        Map<String, Object> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        String temTestString = String.valueOf(map.get("test"));
        if (Objects.isNull(temTestString)) { throw new BusinessException("is null"); }
        jsonObject.put("test", temTestString);
        result.setData(jsonObject);
        return result;
    }
}
