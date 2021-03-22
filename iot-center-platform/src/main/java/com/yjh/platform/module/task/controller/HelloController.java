package com.yjh.platform.module.task.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
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
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TWarnInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

import java.io.IOException;
import java.net.InetAddress;
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
        Map<String,Object> jasonMap=new HashMap<>();
        jasonMap.put("type","noTask");
        //jasonMap.put("taskId",tCruiseTask.getTaskId());
        String json= JSON.toJSONString(jasonMap);
        try{
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,json);
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
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,json);
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
    public Result sayHello(@RequestParam(value = "filePath") String filePath,@RequestParam String testString)  throws ParseException {
        Result result = new Result();
        ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
        tStdMetemodelDetailDao.selectForDictNote(resultHandler);
        Map<String, String> map = resultHandler.getMappedResults();
        System.out.println("map = " + map);
        result.setData(map);
        Long beginTime=System.currentTimeMillis();




        Map<String,List<Analysis>> analysisInfo=new HashMap<>();
        List<Analysis> analysisList=new ArrayList<>();



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
            try{
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,json);
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
}
