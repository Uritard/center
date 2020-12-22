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
import com.yjh.platform.common.utils.ResultHandleUtils;
import com.yjh.platform.common.websocket.WebSocketResult;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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

import java.awt.geom.FlatteningPathIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.QrCodeUtils;


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


    //相机抓图selectForDictNote
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";

    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    QrCodeUtils qrCodeUtils = new QrCodeUtils();


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
    public Result sayHello(@RequestParam(value = "filePath") String filePath) throws ParseException {
        Result result = new Result();
        ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
        tStdMetemodelDetailDao.selectForDictNote(resultHandler);
        Map<String, String> map = resultHandler.getMappedResults();
        System.out.println("map = " + map);
        result.setData(map);

        Long beginTime=System.currentTimeMillis();

        Long endTime=System.currentTimeMillis();

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
