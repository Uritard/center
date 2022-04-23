package com.yjh.accessvideo.netty.client;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import lombok.SneakyThrows;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

//import static com.yjh.accessvideo.common.Constant.INSTANCEID;
//import static com.yjh.accessvideo.common.Constant.TASKID;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private String body;
    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    private String TASKID;
    private String INSTANCEID;
    private String syncWebsocketUrl;
    /**
     * 服务端口
     */
    private final int remotePort;

    public DataDealThread(String body, int remotePort, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService, String syncWebsocketUrl) {
        this.analyseDataOperateService = analyseDataOperateService;
        this.redisTemplate = redisTemplate;
        this.body = body;
        this.syncWebsocketUrl=syncWebsocketUrl;
        this.remotePort = remotePort;
    }

    @SneakyThrows
    @Override
    public void run() {
        // 反拆包解析
        String usefulBody = body;
//        String bodyTemp=body.replaceAll("\\S+","");
//        String usefulBody=bodyTemp.replaceAll("\\{.*?\\}\\{","{");
        if (usefulBody != "") {
            JSONObject jsonObject = JSON.parseObject(usefulBody);
            log.info("JSON对象1：" + jsonObject);

            if ("2".equals(jsonObject.getString("msgType"))) {
                JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.getString("msgData")).getString("data")); //全量数据结果集
                log.info("原生数据****：" + jsonObjectData);
                Iterator iterator = jsonObjectData.entrySet().iterator();
                // 迭代器取出data中的每一个resultInfo
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    // 遍历每一个结果子集
                    JSONObject jsonObjectResult = JSON.parseObject(String.valueOf(entry.getValue()));
                    log.info("数据****：" + jsonObjectResult);



                    //初始化TASKID和INSTANCEID
                    TASKID = jsonObjectResult.getString("taskId");
                    INSTANCEID = jsonObjectResult.getString("instanceId");
                    String name = "t_cruise_task_result:" + TASKID + ":" + INSTANCEID;
                    //将每个任务下所需的正常点数、异常点数放入"任务常量Map"中
                    Map<String, String> taskConstant = new HashMap<>();
                    //把巡视点redis名称放入任务算法巡视点List中--单点双算法需要判重
                    if (redisTemplate.opsForList().size("cruiseKeys:" + TASKID) != 0) {
                        redisTemplate.opsForList().remove("cruiseKeys:" + TASKID, 0, name);
                        redisTemplate.opsForList().leftPush("cruiseKeys:" + TASKID, name);
                    } else {
                        redisTemplate.opsForList().leftPush("cruiseKeys:" + TASKID, name);
                    }

                    Integer tAbnormal = 0;
                    Integer tNormal = 0;
                    taskConstant.put("taskId", TASKID);
                    log.info("keyName" + name);

                    String alarm_level = "";



                    log.info("数据初始化");

                    //redis数据键名由taskId+instanceId命名
                    log.info("数据Redis业务开启");
                    String redisName = jsonObjectResult.getString("taskId") + ":" + jsonObjectResult.getString("instanceId");
                    log.info("template:" + redisTemplate);
                    log.info("redisName:" + redisName);
                    //巡视点信息存储Redis KeyName
                    String cruiseRedisName = "t_cruise_task_result:" + redisName;
                    Map<String, String> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + redisName);//读redis
                    log.info("读取到的redis：" + cruiseResult);
                    String recognitionMode = String.valueOf(cruiseResult.get("recognitionMode"));
                    log.info("recognitionMode----识别模式 ：-------" + recognitionMode);
                    Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                    //处理判别结果
                    //获取分析结果图片地址
                    String taskId = jsonObjectResult.getString("taskId");
                    Long instanceId = NumberUtils.toLong(jsonObjectResult.getString("instanceId"));
                    String resultImage=cruiseResult.get("picpath");
                    if(Objects.nonNull(jsonObjectResult.get("analyseResultImg"))) {
                        resultImage = jsonObjectResult.getString("analyseResultImg");
                    }
                    String analyseType = jsonObjectResult.getString("analyseType");
                    if ("11".equals(analyseType)) {
                        if ("".equals(resultImage)) {
                            continue;
                        }
                    }


                    log.info("端口号：" + remotePort);

                    try {
                        switch (remotePort) {
                            case 13668:

                                String analyseResultPic =cruiseResult.get("picpath");
                                if ("0".equals(recognitionMode)) {
                                    redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "recognitionMode", "-2");
                                }
                                //表计识别图片放入缓存(已考虑双算法)
                                if(Objects.nonNull(jsonObjectResult.get("analyseResultImg")) && !("".equals(
                                    jsonObjectResult.getString("analyseResultImg"))) ){
                                     analyseResultPic = jsonObjectResult.getString("analyseResultImg").replaceAll(
                                         (String)redisTemplate.opsForHash().get("t_sys_param:meterResultImg", "content"), (String)redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg", "content"));
                                    log.info("表计识别图片-------------------------------"+analyseResultPic);
                                    log.info("缓存地址-----------------------"+redisName);
                                    if("-1".equals(recognitionMode) || "-2".equals(recognitionMode)) {
                                        log.info("双算法-第二算法图片生成-M");
                                        redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "picpath", redisTemplate.opsForHash().get("t_cruise_task_result:" + redisName, "picpath") + "," + analyseResultPic);
                                    }else {
                                        redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "picpath", analyseResultPic);
                                    }
                                }




                                if ("NULL_Model".equals(jsonObjectResult.get("resultValue"))) {
                                    Map<String, String> doubleHandelMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "数据异常", "缺少标定文件");
                                    cruiseResultMap.put("resultNum", doubleHandelMap.get("resultNum"));
                                    cruiseResultMap.put("cruiseResult", doubleHandelMap.get("cruiseResult"));
                                    cruiseResultMap.put("cruiseAbnormal", doubleHandelMap.get("cruiseAbnormal"));
                                    tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                    tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
                                } else if ("识别失败".equals(jsonObjectResult.get("resultValue"))) {
                                    Map<String, String> doubleHandelMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "数据异常", jsonObjectResult.getString("resultValue"));
                                    cruiseResultMap.put("resultNum", doubleHandelMap.get("resultNum"));
                                    cruiseResultMap.put("cruiseResult", doubleHandelMap.get("cruiseResult"));
                                    cruiseResultMap.put("cruiseAbnormal", doubleHandelMap.get("cruiseAbnormal"));
                                    tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                    tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
                                } else {
                                    if (jsonObjectResult.getString("resultValue").matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|^(-[0-9]{1,})$|^(-[0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]+")) {

                                        String alarmValue = jsonObjectResult.getString("resultValue");
                                        //判断是否为红外识别且获取FIR文件
                                        //  JsonObject中存在 firDocPath 则放入缓存中
                                        if(Objects.nonNull(jsonObjectResult.get("firDocPath"))){
                                            if(!("".equals(jsonObjectResult.get("firDocPath")))){
                                                String firDocPath=jsonObjectResult.getString("firDocPath").replaceAll(
                                                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredStorePath", "content")), String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredRealPath", "content")));
                                                cruiseResultMap.put("firDocPath",firDocPath);
                                                File file=new File(firDocPath);
                                                cruiseResultMap.put("firName",file.getName().substring(0,file.getName().lastIndexOf(".")));//放入文件名
                                                log.info("FIR-Doc-----:"+jsonObjectResult.get("firDocPath"));
                                            }
                                        }

                                        log.info("开始告警预处理");
                                        TStdDevicemete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));
                                        //单个数值表计识别结果的告警判断与处理
                                        TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));
                                        log.info("数据查询初始化");
                                        //满足告警数据结构-进行告警判断
                                        //表计测点未配置告警规则
                                        if (analyseDataOperateService.warnSettings(tStdDevicemeteM.getMeteKind(),
                                                tStdDevicemeteM.getStateZero(),
                                                tStdDevicemeteM.getAlarmState(),
                                                tStdDevicemeteM.getHighLimit1(),
                                                tStdDevicemeteM.getLowLimit1(),
                                                tStdDevicemeteM.getHighLimit2(),
                                                tStdDevicemeteM.getLowLimit2(),
                                                tStdDevicemeteM.getHighLimit3(),
                                                tStdDevicemeteM.getLowLimit3(),
                                                tStdDevicemeteM.getHighLimit4(),
                                                tStdDevicemeteM.getLowLimit4()) == 1) {
                                            //初始化告警信息redis表
                                            String warnName = "warnInfo:" + TASKID + String.valueOf(UUID.randomUUID()).replace("-", "");
                                            Map<String, String> warnMap = new HashMap<>();
//                                            warnMap.put("warnType", tStdDevicemeteM.getAlarmType());
                                            warnMap.put("deviceId", String.valueOf(tCruisePointInstance.getDeviceId()));
                                            warnMap.put("customId", tCruisePointInstance.getCustomId());
                                            warnMap.put("instanceId", jsonObjectResult.getString("instanceId"));
                                            warnMap.put("stdMeteId", String.valueOf(tCruisePointInstance.getDeviceMeteId()));
                                            warnMap.put("taskId", jsonObjectResult.getString("taskId"));
                                            warnMap.put("value", jsonObjectResult.getString("resultValue"));
                                            warnMap.put("imagePath", analyseResultPic);
                                            warnMap.put("confMode", "276");
                                            warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                            warnMap.put("defectModel", analyseDataOperateService.selectDictCode("defect_model", "其他"));
                                            log.info("开始告警判断");
                                            log.info("测点种类:" + tStdDevicemeteM.getMeteKind());
                                            switch (tStdDevicemeteM.getMeteKind()) {
                                                case "1":
                                                    String resultValue = jsonObjectResult.getString("resultValue");
                                                    int warnRuleTeleSigning = analyseDataOperateService.warnJudgementTelesignaling(resultValue,
                                                            tStdDevicemeteM.getStateZero(),
                                                            tStdDevicemeteM.getStateOne(),
                                                            tStdDevicemeteM.getAlarmState()
                                                    );
                                                    log.info("告警结果：" + warnRuleTeleSigning);
                                                    if (warnRuleTeleSigning == 1) {
                                                        log.info("告警信息生成");
                                                        warnMap.put("warnLevel", String.valueOf(tStdDevicemeteM.getAlarmLevel()));
                                                        warnMap.put("warnName", tStdDevicemeteM.getMeteName() + "状态异常");
                                                        warnMap.put("warnTime", simpleDateFormat.format(new Date()));
                                                        if (tStdDevicemeteM.getAlarmState() == 0) {
                                                            warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + tStdDevicemeteM.getStateZero() + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemeteM.getAlarmLevel()), "alarm_level"));
                                                        } else {
                                                            warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + tStdDevicemeteM.getStateOne() + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemeteM.getAlarmLevel()), "alarm_level"));
                                                        }
                                                        warnMap.put("outRange", "--");

                                                        log.info("告警MAP：" + warnMap);
                                                        try {
                                                            redisTemplate.opsForHash().putAll(warnName, warnMap);
                                                            cruiseResultMap.put("isWarn", "1");
                                                        } catch (Exception e) {
                                                            log.info("生成错误", e);
                                                        }

                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "异常告警", alarmValue);
                                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                                        tAbnormal++;
                                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);

                                                    } else {
                                                        //未达到告警值(遥信)
                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", alarmValue);
                                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                                        tNormal++;
                                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
                                                    }
                                                    break;
                                                case "2":
                                                    Float resultValueMeter = NumberUtils.toFloat(jsonObjectResult.getString("resultValue"));
                                                    int warnRuleMeter = analyseDataOperateService.warnJudgement(resultValueMeter,
                                                            tStdDevicemeteM.getHighLimit1(),
                                                            tStdDevicemeteM.getLowLimit1(),
                                                            tStdDevicemeteM.getHighLimit2(),
                                                            tStdDevicemeteM.getLowLimit2(),
                                                            tStdDevicemeteM.getHighLimit3(),
                                                            tStdDevicemeteM.getLowLimit3(),
                                                            tStdDevicemeteM.getHighLimit4(),
                                                            tStdDevicemeteM.getLowLimit4());
                                                    if (warnRuleMeter > 0) {
                                                        warnMap.put("warnName", tStdDevicemeteM.getMeteName() + "数据异常");
                                                        warnMap.put("warnTime", simpleDateFormat.format(new Date()));
                                                        log.info("数字结果告警判断结果：" + warnRuleMeter);

                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "异常告警", alarmValue);
                                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));

                                                        switch (warnRuleMeter) {
                                                            case 1:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + cruiseResultMap.get("resultNum") + "--" + "预警");
                                                                alarm_level = "1";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit1()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit1()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit1() - resultValueMeter));
                                                                }
                                                                break;
                                                            case 2:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + cruiseResultMap.get("resultNum") + "--" + "一般告警");
                                                                alarm_level = "2";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit2()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit2()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit2() - resultValueMeter));
                                                                }
                                                                break;
                                                            case 3:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + cruiseResultMap.get("resultNum") + "--" + "严重告警");
                                                                alarm_level = "3";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit3()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit3()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit3() - resultValueMeter));
                                                                }
                                                                break;

                                                            case 4:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":" + cruiseResultMap.get("resultNum") + "--" + "危急告警");
                                                                alarm_level = "4";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit4()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit4()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit4() - resultValueMeter));
                                                                }
                                                                break;

                                                            default:
                                                                break;
                                                        }

                                                        redisTemplate.opsForHash().putAll(warnName, warnMap);
                                                        cruiseResultMap.put("isWarn", "1");
                                                        log.info("告警Map:" + warnMap);

//                                                        tAbnormal++;
                                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
                                                    } else {
                                                        //未达到告警值（遥测）
                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", alarmValue);
                                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                                        tNormal++;
                                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
                                                    }
                                                    break;
                                                default:
                                                    break;
                                            }
                                            try {
                                                // TODO: 2020/12/15 告警信息插库 并推送
                                                Map<String, String> warningMsg = redisTemplate.opsForHash().entries(warnName);
                                                log.info("warnMsg:" + warningMsg);
                                                if (warningMsg.size() != 0) {
                                                    TWarnInfo tWarnInfo = new TWarnInfo();
                                                    tWarnInfo.setWarnLevel(NumberUtils.toInt(warningMsg.get("warnLevel")));
//                                                    tWarnInfo.setWarnType(NumberUtils.toInt(warningMsg.get("warnType")));
                                                    tWarnInfo.setDeviceId(NumberUtils.toLong(warningMsg.get("deviceId")));
                                                    tWarnInfo.setCunstomId(warningMsg.get("customId"));
                                                    tWarnInfo.setInstanceId(NumberUtils.toLong(warningMsg.get("instanceId")));
                                                    tWarnInfo.setStdMeteId(NumberUtils.toLong(warningMsg.get("stdMeteId")));
                                                    tWarnInfo.setTaskId(warningMsg.get("taskId"));
                                                    tWarnInfo.setValue(warningMsg.get("value"));
                                                    tWarnInfo.setConfMode(NumberUtils.toInt(warningMsg.get("confMode")));
                                                    tWarnInfo.setImagePath(warningMsg.get("imagePath"));
                                                    tWarnInfo.setAlarmSource(NumberUtils.toInt(warningMsg.get("alarmSource")));
                                                    tWarnInfo.setWarnName(warningMsg.get("warnName"));
                                                    tWarnInfo.setOutRange(warningMsg.get("outRange"));
                                                    tWarnInfo.setWarnContent(warningMsg.get("warnContent"));
                                                    tWarnInfo.setDefectModel(NumberUtils.toInt(warningMsg.get("defectModel")));
                                                    tWarnInfo.setWarnTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(warningMsg.get("warnTime")));
                                                    log.info("------------------------------------------------------------");
                                                    analyseDataOperateService.insertWarnInfo(tWarnInfo);

                                                    // TODO:最新告警信息获取
                                                    Map<String,String> currentWarnInfo=new HashMap<>();
                                                    currentWarnInfo.put("warnId", String.valueOf(analyseDataOperateService.selectCurrentWarn()));
                                                    currentWarnInfo.put("defectModel","450");
                                                    currentWarnInfo.put("isPop","false");

                                                    try{

                                                        //告警上报站端
                                                        XMLBaseModel xmlBaseModel = new XMLBaseModel();
                                                        List<Map<String, Object>> xmlItems = new ArrayList<>();
                                                        Map<String, Object> xmlItem = new HashMap<>();
                                                        xmlBaseModel.setType("62");
                                                        xmlItem.put("patroldevice_code", cruiseResult.get("instanceId"));
                                                        xmlItem.put("task_name", cruiseResult.get("taskName"));
                                                        xmlItem.put("task_code", cruiseResult.get("taskCode"));
                                                        xmlItem.put("device_name", cruiseResult.get("instanceName"));
                                                        xmlItem.put("device_id", cruiseResult.get("instanceId"));
                                                        xmlItem.put("alarm_level", alarm_level);
                                                        String deviceMeteId = cruiseResult.get("device_mete_id");
                                                        Map<String, Object> info = analyseDataOperateService.selectWarnInfo(NumberUtils.toLong(deviceMeteId));
                                                        xmlItem.put("alarm_type", (null == info.get("alarm_type") ? "" : info.get("alarm_type")));
                                                        xmlItem.put("recognition_type", (null == info.get("recognition_type") ? "" : info.get("recognition_type")));
                                                        xmlItem.put("value", tWarnInfo.getValue());
                                                        xmlItem.put("unit", (null == info.get("unit") ? "" : info.get("unit")));
                                                        xmlItem.put("value_unit", tWarnInfo.getValue() + xmlItem.get("unit"));
                                                        xmlItem.put("time", simpleDateFormat.format(new Date()));
                                                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                                                        xmlItem.put("task_patrolled_id", cruiseResult.get("taskId")+"_"+simpleDateFormat2.format(simpleDateFormat2.parse(cruiseResult.get("cruiseTime"))));
                                                        xmlItem.put("content", tWarnInfo.getWarnContent());


                                                        xmlItems.add(xmlItem);
                                                        xmlBaseModel.setItems(xmlItems);
                                                        List<XMLBaseModel> list = new ArrayList<>();
                                                        list.add(xmlBaseModel);
                                                        Map<String, List<XMLBaseModel>> map = new HashMap<>();
                                                        map.put("list", list);
                                                        log.info("告警上报：-" + map);
                                                        Constant.otherServer(map, Constant.TCP_URL);//江苏要求
                                                    }catch (Exception e){
                                                        log.error("告警上报出错：", e);
                                                    }





                                                    // webSocket通知前端刷新告警统计数量
                                                    Map<String, Object> jasonMaps = new HashMap<>();
                                                    jasonMaps.put("type", "newAlarm");
                                                    jasonMaps.put("alarmName", warningMsg.get("warnName"));
                                                    jasonMaps.put("alarmTime", warningMsg.get("warnTime"));
                                                    jasonMaps.put("alarmContent", warningMsg.get("warnContent"));
                                                    String jsons = JSON.toJSONString(jasonMaps);
                                                    log.info("告警生成-前端推送：" + jsons);
                                                    postUrl(syncWebsocketUrl,jsons);

                                                    //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
                                                    String alarmNote = tStdDevicemeteM.getAlarmNote();
                                                    Integer alarmLevel = tStdDevicemeteM.getAlarmLevel();
                                                    Integer warnLevel = tWarnInfo.getWarnLevel();
                                                    log.info("该测点是否配置了告警提示是===" + alarmNote);
                                                    log.info("该测点告警推送配置的告警等级是===" + alarmLevel);
                                                    log.info("产生的该条告警等级是===" + warnLevel);
                                                    boolean one = (Objects.nonNull(alarmNote) && "1".equals(alarmNote));
                                                    boolean two = (Objects.nonNull(alarmLevel) && (warnLevel.compareTo(alarmLevel) == 0 || warnLevel > alarmLevel));
                                                    log.info("一层判断" + one + "二层判断"+two);


                                                    if (one) {
                                                        if (two) {
                                                            //webSocket通知前端调用查询告警弹框的接口
                                                            currentWarnInfo.put("isPop","true");
                                                            Map<String, Object> jasonMaps2 = new HashMap<>();
                                                            jasonMaps2.put("type", "alarmPopUp");
                                                            jasonMaps2.put("warnId", tWarnInfo.getWarnId());
                                                            jasonMaps2.put("defectModel", tWarnInfo.getDefectModel());
                                                            String json = JSON.toJSONString(jasonMaps2);
                                                            log.info("发送给前端的消息：" + json);
                                                            postUrl(syncWebsocketUrl,json);
                                                        }
                                                    }

                                                    //最近一条告警信息 入缓存
                                                    redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3, TimeUnit.MINUTES);
                                                    log.info("currentWarnInfo666"+currentWarnInfo);

//                                                    redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3,TimeUnit.MINUTES);

//
//                                                    //删除告警redis
//                                                    redisTemplate.delete(warnName);
                                                }
                                            } catch (Exception e) {
                                                log.error("告警入库失败", e);
                                                log.error("Exception--Detail:{}", e.getStackTrace()[0]);
                                            }

                                        } else {
                                            Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", alarmValue);
                                            cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                            cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                            cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                            tNormal++;
                                            tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                            tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
                                        }
                                    } else {
                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "数据异常", jsonObjectResult.getString("resultValue"));
                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                        tAbnormal++;
                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
                                    }
                                }

                                break;
                            case 13669:

                                String analyseResultImg =cruiseResult.get("picpath");//图片路径初始化
                                if ("0".equals(recognitionMode)) {
                                    redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "recognitionMode", "-1");
                                }

                                //判别结果处理逻辑
                                if("11".equals(analyseType)){

                                    analyseResultImg=resultImage.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")),String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")));
                                    String resultValue = analyseDataOperateService.resolveDefectResult(jsonObjectResult.getString("resultValue"));

                                    if(jsonObjectResult.getString("resultValue").contains("abnormal")){

                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
//                                    tNormal++;
                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "--", "--");
                                        cruiseResultMap.put("resultNum", resultValue);
                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
                                        cruiseResultMap.put("picpath",analyseResultImg);

                                    }else {

                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
//                                    tNormal++;
                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", "--");
                                        cruiseResultMap.put("resultNum", resultValue);
                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
                                        cruiseResultMap.put("picpath",analyseResultImg);
                                    }

                                    break;
                                }


                                List<TDefectInfo> defectInfos=new ArrayList<>();//缺陷信息List
                                String originResult = jsonObjectResult.getString("resultValue");
                                String resultValue = analyseDataOperateService.resolveDefectResult(jsonObjectResult.getString("resultValue"));
                                log.info("解析的缺陷数据：" + resultValue);
                                //获取算法标定结果图片并放入缓存（已考虑双算法情况）
                                if(Objects.nonNull(jsonObjectResult.get("analyseResultImg")) && !(jsonObjectResult.get("analyseResultImg").equals("")) ) {
                                     analyseResultImg = jsonObjectResult.getString("analyseResultImg").replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")), String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")));
                                    if ("-1".equals(recognitionMode) || "-2".equals(recognitionMode)) {
                                        log.info("双算法-第二算法图片生成-D");
                                        redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "picpath", redisTemplate.opsForHash().get("t_cruise_task_result:" + redisName, "picpath") + "," + analyseResultImg);
                                    } else {
                                        redisTemplate.opsForHash().put("t_cruise_task_result:" + redisName, "picpath", analyseResultImg);
                                    }
                                }

                                // TODO: 2021/1/11 算法服务端需要区分数据异常和未识别出缺陷的情形
                                if (resultValue != null && !(resultValue.contains("device"))) {
                                    //巡视点被识别出缺陷就会被判定为异常点，异常类型为--缺陷异常
                                    Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "缺陷异常", resultValue);
                                    cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                    cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                    cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                    tAbnormal++;
                                    tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                    tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);

                                    TStdDevicemete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));

                                    //生成缺陷缓存信息（单一缺陷和多元缺陷）
                                    log.info("--------____--------生成缺陷缓存");
                                    String[] resultArr = resultValue.split("\\s+");
                                    if (resultArr.length == 1) {
                                        log.info("--------____--------单一缺陷");
                                        TDefectInfo tDefectInfo=new TDefectInfo();//实时入库
                                        Map<String, String> defectMap = new HashMap<>();
                                        String redisFlag = jsonObjectResult.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");
                                        String defectRedisName = "defectInfo:" + redisFlag;
                                        // TODO: 2021/2/19 判别并获取对应缺陷的缺陷算法等级
                                        defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
                                        log.info("defectMap:"+defectMap);
                                        defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
                                        defectMap.put("defectContent", resultValue);
                                        defectMap.put("deviceId", cruiseResult.get("deviceId"));
                                        defectMap.put("instanceId", cruiseResult.get("instanceId"));
                                        TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResult.get("instanceId")));
                                        defectMap.put("customId", tStdDevicemete.getCustomId());
                                        defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                                        defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
                                        defectMap.put("imagePath", analyseResultImg);
                                        defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                        defectMap.put("defectTime", simpleDateFormat.format(new Date()));
                                        redisTemplate.opsForHash().putAll(defectRedisName, defectMap);

                                        //缺陷插库  To be continue。。。
                                        tDefectInfo.setDefectLevel(NumberUtils.toInt(defectMap.get("defectLevel")));
                                        tDefectInfo.setDefectTime(simpleDateFormat.parse(defectMap.get("defectTime")));//缺陷识别时间
                                        tDefectInfo.setDefectType(NumberUtils.toInt(defectMap.get("defectType")));
                                        tDefectInfo.setDefectContent(defectMap.get("defectContent"));
                                        tDefectInfo.setDeviceId(NumberUtils.toLong(defectMap.get("deviceId")));
                                        tDefectInfo.setInstanceId(NumberUtils.toLong(defectMap.get("instanceId")));
                                        tDefectInfo.setCunstomId(defectMap.get("customId"));
                                        tDefectInfo.setStdMeteId(NumberUtils.toLong(defectMap.get("stdMeteId")));
                                        tDefectInfo.setConfMode(NumberUtils.toInt(defectMap.get("confMode")));
                                        tDefectInfo.setImagePath(defectMap.get("imagePath"));
                                        tDefectInfo.setAlarmSource(NumberUtils.toInt(defectMap.get("alarmSource")));
                                        defectInfos.add(tDefectInfo);


                                        //缺陷弹框PlanB-推送-单缺陷
                                        Map<String,String> currentWarnInfo=new HashMap<>();
                                        currentWarnInfo.put("warnId", String.valueOf(analyseDataOperateService.selectCurrentDefect()));
                                        currentWarnInfo.put("defectModel",defectMap.get("defectType"));
                                        currentWarnInfo.put("isPop","false");


                                        // webSocket通知显示缺陷信息(单条推送)
                                        Map<String, Object> jasonMaps = new HashMap<>();
                                        jasonMaps.put("type", "newAlarm");
                                        jasonMaps.put("alarmName", resultValue);
                                        jasonMaps.put("alarmTime", defectMap.get("defectTime"));
                                        jasonMaps.put("alarmContent", tStdDevicemeteM.getMeteName() + "--" + resultValue);
                                        String jsons = JSON.toJSONString(jasonMaps);
                                        log.info("缺陷生成-前端推送：" + jsons);
                                        postUrl(syncWebsocketUrl,jsons);

                                        //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
                                        String alarmNote = tStdDevicemete.getAlarmNote();
                                        log.info("该测点是否配置了告警提示是===" + alarmNote);
                                        Integer defectLevel = NumberUtils.toInt(defectMap.get("defectLevel"));
                                        log.info("产生的该条缺陷的等级是===" + defectLevel);
                                        if (alarmNote != null && "1".equals(alarmNote)) {
                                            if (defectLevel == 133) {//危急
                                                //webSocket通知前端调用查询告警弹框的接口
                                                currentWarnInfo.put("isPop","true");
                                                Map<String, Object> jasonMaps2 = new HashMap<>();
                                                jasonMaps2.put("type", "alarmPopUp");
                                                jasonMaps2.put("warnId", redisFlag);
                                                jasonMaps2.put("defectModel", defectMap.get("defectType"));
                                                String json = JSON.toJSONString(jasonMaps2);
                                                log.info("发送给前端的消息：" + json);
                                                postUrl(syncWebsocketUrl,json);
                                            }
                                        }

                                        //最近一条缺陷信息-入缓存
                                        redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3, TimeUnit.MINUTES);
                                        log.info("currentWarnInfo666"+currentWarnInfo);

                                    } else if (resultArr.length > 1) {
                                        log.info("--------____--------多元缺陷");
                                        String defectTime = simpleDateFormat.format(new Date());
                                        String defectNames = "";
                                        for (int i = 0; i < resultArr.length; i++) {
                                            log.info("缺陷处理方法：" + resultArr[i]);
                                            Map<String, String> defectMap = new HashMap<>();
                                            TDefectInfo tDefectInfo=new TDefectInfo();//实时入库
                                            String redisFlag = jsonObjectResult.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");
                                            String defectRedisName = "defectInfo:" + redisFlag;
//                                            defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));

                                            log.info("redisFlag:"+redisFlag);
                                            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultArr[i]));
                                            defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
                                            defectMap.put("defectContent", resultArr[i]);
                                            defectMap.put("deviceId", cruiseResult.get("deviceId"));
                                            defectMap.put("instanceId", cruiseResult.get("instanceId"));
                                            TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResult.get("instanceId")));
                                            defectMap.put("customId", tStdDevicemete.getCustomId());
                                            defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                                            defectMap.put("confMode", "276");//确认状态--未核查
                                            defectMap.put("imagePath", analyseResultImg);
                                            defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                            defectMap.put("defectTime", defectTime);

                                            log.info("defectMap:"+defectMap);

                                            redisTemplate.opsForHash().putAll(defectRedisName, defectMap);

                                            //缺陷插库
                                            tDefectInfo.setDefectLevel(NumberUtils.toInt(defectMap.get("defectLevel")));
                                            tDefectInfo.setDefectTime(simpleDateFormat.parse(defectMap.get("defectTime")));//缺陷识别时间
                                            tDefectInfo.setDefectType(NumberUtils.toInt(defectMap.get("defectType")));
                                            tDefectInfo.setDefectContent(defectMap.get("defectContent"));
                                            tDefectInfo.setDeviceId(NumberUtils.toLong(defectMap.get("deviceId")));
                                            tDefectInfo.setInstanceId(NumberUtils.toLong(defectMap.get("instanceId")));
                                            tDefectInfo.setCunstomId(defectMap.get("customId"));
                                            tDefectInfo.setStdMeteId(NumberUtils.toLong(defectMap.get("stdMeteId")));
                                            tDefectInfo.setConfMode(NumberUtils.toInt(defectMap.get("confMode")));
                                            tDefectInfo.setImagePath(defectMap.get("imagePath"));
                                            tDefectInfo.setAlarmSource(NumberUtils.toInt(defectMap.get("alarmSource")));
                                            defectInfos.add(tDefectInfo);


                                            //缺陷弹框PlanB-推送-多缺陷
                                            Map<String,String> currentWarnInfo=new HashMap<>();
                                            currentWarnInfo.put("warnId", String.valueOf(analyseDataOperateService.selectCurrentWarn()));
                                            currentWarnInfo.put("defectModel",defectMap.get("defectType"));
                                            currentWarnInfo.put("isPop","false");



                                            //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
                                            String alarmNote = tStdDevicemete.getAlarmNote();
                                            log.info("该测点是否配置了告警提示是===" + alarmNote);
                                            Integer defectLevel = NumberUtils.toInt(defectMap.get("defectLevel"));
                                            log.info("产生的该条缺陷的等级是===" + defectLevel);
                                            if (alarmNote != null && "1".equals(alarmNote)) {
                                                if (defectLevel == 133) {//危急
                                                    //webSocket通知前端调用查询告警弹框的接口
                                                    currentWarnInfo.put("isPop","true");
                                                    Map<String, Object> jasonMaps2 = new HashMap<>();
                                                    jasonMaps2.put("type", "alarmPopUp");
                                                    jasonMaps2.put("warnId", redisFlag);
                                                    jasonMaps2.put("defectModel", defectMap.get("defectType"));
                                                    String json = JSON.toJSONString(jasonMaps2);
                                                    log.info("发送给前端的消息：" + json);
                                                    postUrl(syncWebsocketUrl,json);
                                                }
                                            }

                                            redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3, TimeUnit.MINUTES);
                                            log.info("currentWarnInfo666"+currentWarnInfo);


                                            defectNames = defectNames + resultArr[i] + " ";

                                        }


                                        // webSocket通知显示缺陷信息(多条推送)
                                        Map<String, Object> jasonMaps = new HashMap<>();
                                        jasonMaps.put("type", "newAlarm");
                                        jasonMaps.put("alarmName", defectNames);
                                        jasonMaps.put("alarmTime", defectTime);
                                        jasonMaps.put("alarmContent", tStdDevicemeteM.getMeteName() + "--" + defectNames);
                                        String jsons = JSON.toJSONString(jasonMaps);
                                        log.info("缺陷生成-前端推送：" + jsons);
                                        postUrl(syncWebsocketUrl,jsons);

                                    }

                                    //  缺陷批量实时入库
                                    if (defectInfos.size() > 0) {
                                        analyseDataOperateService.batchInsertDefectInfo(defectInfos);
                                    }
                                    log.info("--------缺陷入库完成-----");

                                } else {  //未产生缺陷
                                    tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                    tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
//                                    tNormal++;
                                    Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", "--");
                                    if(resultValue.contains("device")){
                                        cruiseResultMap.put("resultNum",resultValue.replaceAll("device",""));
                                    }else {
                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                    }
                                    cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                    cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
                                }

                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        log.error("告警/缺陷处理失败：" + e);
                        log.error("Exception-Detail:-------"+e.getStackTrace()[0]);
                    }


                    if ("0".equals(recognitionMode)) {
                        //双算法-第一算法处理
                        log.info("DoubleAlgorithmDealing......................");
                        redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);
                        log.info("--------------------End-----------------------");
                    } else {
                        //单算法处理/双算法-第二算法处理--（完整巡视点结束）
                        cruiseResultMap.put("cruiseStatus", analyseDataOperateService.selectDictCode("cruise_data_state", "已执行"));
                        if (Objects.isNull(cruiseResultMap.get("isWarn"))) {
                            cruiseResultMap.put("isWarn", "0");
                        }
                        cruiseResultMap.put("evaluationState", analyseDataOperateService.selectDictCode("evaluation_state", "未审核"));
                        // TODO: 2020/11/4 对算法识别结果进行判断并决定再redis中插入哪个值：identifyState- 识别正常&识别异常
                        // TODO: 2020/11/4 对实际结果进行判断并决定填入哪个初始值：identifyResult-正常&未采集图片&未识别图片&识别缺陷警告（加入IF判断）

                        redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);//修改redis

                        try{
                            //巡视点结果上报站端
                            XMLBaseModel xmlBaseModel = new XMLBaseModel();
                            List<Map<String, Object>> xmlItems = new ArrayList<>();
                            Map<String, Object> xmlItem = new HashMap<>();
                            xmlBaseModel.setType("61");
                            xmlItem.put("patroldevice_code", cruiseResult.get("instanceId"));
                            xmlItem.put("task_name", cruiseResult.get("taskName"));
                            xmlItem.put("task_code", cruiseResult.get("taskCode"));
                            xmlItem.put("device_name", cruiseResult.get("instanceName"));
                            xmlItem.put("device_id", cruiseResult.get("instanceId"));
                            xmlItem.put("material_id", cruiseResult.get("realCode"));
                            xmlItem.put("value", "");
                            xmlItem.put("value_unit", cruiseResultMap.get("resultNum"));
                            xmlItem.put("unit", "");
                            xmlItem.put("time", simpleDateFormat.format(new Date()));
                            //todo
                            xmlItem.put("recognition_type", "");
                            xmlItem.put("file_type", "2");
                            xmlItem.put("file_path", cruiseResult.get("picpath"));
                            xmlItem.put("rectangle", "");
                            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
//                            xmlItem.put("task_patrolled_id", cruiseResult.get("taskId")+"_"+simpleDateFormat2.format(cruiseResult.get("cruiseTime")));
                            xmlItem.put("data_type", "0x01");
                            String valid = "";
                            if ("--".equals(cruiseResultMap.get("resultNum")) || "null".equals(cruiseResultMap.get("resultNum"))) {
                                valid = "0";
                            } else {
                                valid = "1";
                            }
                            xmlItem.put("valid", valid);

                            xmlItems.add(xmlItem);
                            xmlBaseModel.setItems(xmlItems);
                            List<XMLBaseModel> list = new ArrayList<>();
                            list.add(xmlBaseModel);
                            Map<String, List<XMLBaseModel>> map = new HashMap<>();
                            map.put("list", list);
                            log.info("结果信息上报：-" + map);
                            //Constant.otherServer(map, Constant.TCP_URL);//江苏要求
                        }catch (Exception e){
                            log.error("结果上报失败"+e);
                            log.error("异常原因："+e.getStackTrace()[0]);
                        }

                        log.info("Border_______---------______________________________________________________________________________________________");
                        //若本任务上一次有巡视数据，则正常或异常点数要进行加和
                        if (redisTemplate.opsForHash().entries("taskConstant:" + TASKID).size() != 0) {
                            tNormal = NumberUtils.toInt(String.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tNormal"))) + tNormal;
                            tAbnormal = NumberUtils.toInt(String.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tAbnormal"))) + tAbnormal;
                        }
                        taskConstant.put("tNormal", String.valueOf(tNormal));
                        taskConstant.put("tAbnormal", String.valueOf(tAbnormal));
                        redisTemplate.opsForHash().putAll("taskConstant:" + TASKID, taskConstant);//插入任务常量Map
                        log.info("任务常量配置完成");

//                NORMAL = NORMAL + 1;

                        // webSocket通知前端调用巡视监控的接口
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "finishedOneInstance");
                        jasonMap.put("taskId", cruiseResult.get("taskId"));
                        String json = JSON.toJSONString(jasonMap);
                        log.info("发送给前端的消息：" + json);
                        postUrl(syncWebsocketUrl,json);

                        //修改缓存中任务算法巡视点List
                        redisTemplate.opsForList().remove("analysisList:" + cruiseResult.get("taskId"), 0, cruiseResult.get("instanceId"));
                        redisTemplate.opsForList().leftPush("analysisList:" + cruiseResult.get("taskId"), "0");

                        log.info("RedisList修改成功");
                    }

                }

            } else if ("6".equals(jsonObject.get("msgType"))) { //任务结束后发来的心跳信息
                String taskId = JSON.parseObject(jsonObject.getString("msgData")).getString("taskId");
                String instanceId = JSON.parseObject(jsonObject.getString("msgData")).getString("instanceId");
                redisTemplate.opsForList().leftPush("analysisList:" + taskId, "-1");
                log.info("心跳处理结束" + taskId);
                //处理心跳线程私有变量 taskId赋值
                TASKID = taskId;
            }

            if ("-1".equals(redisTemplate.opsForList().index("analysisList:" + TASKID, 0))) {  //满足插库条件
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String finalCruiseKey = "instanceId";//最后修改任务表信息所需的redis KEY名
                log.info("任务结束-开始数据存储");
                //插库时从redis中取出正常、异常点数
                Map<String, String> taskConstant = redisTemplate.opsForHash().entries("taskConstant:" + TASKID);
                Integer tAbnormal = NumberUtils.toInt(taskConstant.get("tAbnormal"));
                Integer tNormal = NumberUtils.toInt(taskConstant.get("tNormal"));
                log.info("正常点数：" + tNormal);
                log.info("异常点数：" + tAbnormal);
                List<String> cruiseResultIds = new ArrayList<>();//cruiseResultIds
                try {
                    //满足条件先插巡视点数据
                    List<TCruiseTaskResultDetail> detailList = new ArrayList<>();//TCTRD List对象
                    List<TCruiseDataResult> dataList = new ArrayList<>();//TCDR List对象
                    List<TDefectInfo> defectList = new ArrayList<>();//TDI List对象

                    //从redisList中取出目前为止本任务中执行算法的所有巡视点
                    List<String> cruiseKeys = redisTemplate.opsForList().range("cruiseKeys:" + TASKID, 0, redisTemplate.opsForList().size("cruiseKeys:" + TASKID) - 1);
                    log.info("cruisekeys:" + cruiseKeys);
                    finalCruiseKey = cruiseKeys.get(0);//挑选一名幸运Redis KEY值
                    for (String cruiseKey : cruiseKeys) {
                        Map<String, String> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
                        //TCTRD
                        log.info("TCTRD开始");
                        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                        tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId") + cruiseWorkedMap.get("instanceId"));
                        tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId"));
                        tCruiseTaskResultDetail.setDeviceId(NumberUtils.toLong(cruiseWorkedMap.get("deviceId")));
                        tCruiseTaskResultDetail.setDeviceName(cruiseWorkedMap.get("deviceName"));
                        tCruiseTaskResultDetail.setInstanceId(NumberUtils.toLong(cruiseWorkedMap.get("instanceId")));
                        tCruiseTaskResultDetail.setInstanceName(cruiseWorkedMap.get("instanceName"));
                        tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime")));
                        tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime")));
                        tCruiseTaskResultDetail.setCruiseStatus(NumberUtils.toInt(cruiseWorkedMap.get("cruiseStatus")));
                        tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark"));
                        log.info("TCDRD内容：" + tCruiseTaskResultDetail);
                        detailList.add(tCruiseTaskResultDetail);
                        cruiseResultIds.add(tCruiseTaskResultDetail.getCruiseResultId());

                        //TCDR
                        log.info("TCDR开始");
                        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                        tCruiseDataResult.setCruiseId(NumberUtils.toLong(cruiseWorkedMap.get("instanceId")));
                        tCruiseDataResult.setCruiseName(cruiseWorkedMap.get("cruiseName"));
                        tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath"));
                        tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum"));
                        tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc"));
                        tCruiseDataResult.setCruiseType(NumberUtils.toInt(cruiseWorkedMap.get("cruiseType")));
                        tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum"));
                        tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic"));
                        tCruiseDataResult.setEvaluationState(NumberUtils.toInt(analyseDataOperateService.selectDictCode("evaluation_state", "未审核")));
                        tCruiseDataResult.setCruiseResult(NumberUtils.toInt(cruiseWorkedMap.get("cruiseResult")));
                        if ("--".equals(cruiseWorkedMap.get("cruiseAbnormal"))) {
                            tCruiseDataResult.setCruiseAbnormal(null);
                        } else {
                            if (cruiseWorkedMap.get("cruiseAbnormal").contains(",")) {
                                String[] abnormalArr = cruiseWorkedMap.get("cruiseAbnormal").split(",");
                                tCruiseDataResult.setCruiseAbnormal(NumberUtils.toInt(abnormalArr[0]));
                                tCruiseDataResult.setRemark(abnormalArr[1]);
                            } else {
                                tCruiseDataResult.setCruiseAbnormal(NumberUtils.toInt(cruiseWorkedMap.get("cruiseAbnormal")));
                            }

                        }
                        tCruiseDataResult.setEvaluationState(NumberUtils.toInt(cruiseWorkedMap.get("evaluationState")));
                        tCruiseDataResult.setCreatetime(new Date());
                        tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId") + cruiseWorkedMap.get("instanceId"));
                        tCruiseDataResult.setIsWarn(NumberUtils.toInt(cruiseWorkedMap.get("isWarn")));
                        if(Objects.nonNull(cruiseWorkedMap.get("firDocPath"))){
                            tCruiseDataResult.setResultPic(cruiseWorkedMap.get("firDocPath"));
                        }else {
                            tCruiseDataResult.setResultPic("");
                        }
                        if(Objects.nonNull(cruiseWorkedMap.get("firName")) && !("null".equals(cruiseWorkedMap.get("firName")))){
                            tCruiseDataResult.setFirName(cruiseWorkedMap.get("firName"));
                            tCruiseDataResult.setFirDate(new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(tCruiseDataResult.getFirName()));
                            // TODO: 2021/3/25 FIR文件名与时间赋值
                        }
                        log.info("TCDR内容：" + tCruiseDataResult);
                        dataList.add(tCruiseDataResult);

                    }


//                    Set<String> defectKey = redisScan("defectInfo:" + TASKID);
//                    for (String key : defectKey) {
//                        Map<String, Object> defectMap = redisTemplate.opsForHash().entries(key);
//                        log.info("TDI开始");
//                        TDefectInfo tDefectInfo = new TDefectInfo();
//                        tDefectInfo.setDefectLevel(NumberUtils.toInt(defectMap.get("defectLevel")));
//                        tDefectInfo.setDefectTime(simpleDateFormat.parse(defectMap.get("defectTime")));//缺陷识别时间
//                        tDefectInfo.setDefectType(NumberUtils.toInt(defectMap.get("defectType")));
//                        tDefectInfo.setDefectContent(defectMap.get("defectContent"));
//                        tDefectInfo.setDeviceId(NumberUtils.toLong(defectMap.get("deviceId")));
//                        tDefectInfo.setInstanceId(NumberUtils.toLong(defectMap.get("instanceId")));
//                        tDefectInfo.setCunstomId(defectMap.get("customId"));
//                        tDefectInfo.setStdMeteId(NumberUtils.toLong(defectMap.get("stdMeteId")));
//                        tDefectInfo.setConfMode(NumberUtils.toInt(defectMap.get("confMode")));
//                        tDefectInfo.setImagePath(defectMap.get("imagePath"));
//                        tDefectInfo.setAlarmSource(NumberUtils.toInt(defectMap.get("alarmSource")));
//                        defectList.add(tDefectInfo);
//                    }

                    //批量插入两表

                    log.info("两表开始插入");
                    analyseDataOperateService.batchInsertCruiseTaskResultDetail(detailList);
                    analyseDataOperateService.batchInsertCruiseDataResult(dataList);
                    log.info("------------点结果插入后调用updateCruiseResultIds----------------------");
                    Constant.otherServerList(cruiseResultIds, Constant.TASK_FINISH);
                    log.info("----------------------------------");
                    redisTemplate.delete("cruiseKeys:" + TASKID);
                    log.info("Loading........清空本任务至此的巡视点");
//                if (warnList.size() > 0) {
//                    analyseDataOperateService.batchInsertWarnInfo(warnList);
//                    //清空redis中的告警信息
//                    Set<String> warnKeys = redisScan("warnInfo:");
//                    for (String key : warnKeys) {
//                        redisTemplate.delete(key);
//                    }
//                }
//                    if (defectList.size() > 0) {
//                        analyseDataOperateService.batchInsertDefectInfo(defectList);
//                    }
                    log.info("两表结束插入");
                    cruiseKeys.clear();
                } catch (Exception e) {
                    log.error("插表错误", e);
                }

                // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
                Map<String, String> cruiseResult = redisTemplate.opsForHash().entries(finalCruiseKey);
                String strForCountAbnormal = "countForAbnormal:" + TASKID;
                Map<String, String> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
                Integer total = NumberUtils.toInt(abnormalCount.get("all"));
                Integer abnormal = NumberUtils.toInt(abnormalCount.get("abnormal"));
                Integer normal = NumberUtils.toInt(abnormalCount.get("normal"));
                log.info("All：" + total);
                //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
                tAbnormal = tAbnormal + abnormal;
                tNormal = tNormal + normal;
                if (tAbnormal + tNormal == total) {
                    //TCTR开始

                    Thread.sleep(10000);

                    try {
                        log.info("TCTR开始");
                        TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                        tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId"));
                        tCruiseTaskResult.setTaskId(cruiseResult.get("taskId"));
                        tCruiseTaskResult.setTaskName(cruiseResult.get("taskName"));
                        tCruiseTaskResult.setTaskAbnormal(tAbnormal);
                        tCruiseTaskResult.setTaskAlarm(0);
                        tCruiseTaskResult.setRunExecute(cruiseResult.get("if_run"));
//                                tCruiseTaskResult.setCruiseTaskTime();

                        log.info("taskId-----------:" + tCruiseTaskResult.getTaskId());
                        if (Objects.nonNull(analyseDataOperateService.selectLaterTaskCruiseResult(tCruiseTaskResult.getTaskId()))) {
                            log.info("TCTR已存在");
                        } else {
                            analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);
                        }


                        //TCR开始
                        log.info("TCR开始");
                        TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId"));
                        tCruiseResult.setCState(NumberUtils.toInt(analyseDataOperateService.selectDictCode("task_state", "执行完成")));
                        tCruiseResult.setTaskWait(0);
                        analyseDataOperateService.updateCruiseResult(tCruiseResult);
                        //低优先级任务继续
                        analyseDataOperateService.lowTaskGoOn(tCruiseResult.getTaskId());


                        // webSocket通知前端调用巡视监控的接口（任务完成）
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "lastOneInstance");
                        jasonMap.put("taskId", cruiseResult.get("taskId"));
                        String json = JSON.toJSONString(jasonMap);
                        log.info("发送给前端的消息：" + json);
                        postUrl(syncWebsocketUrl,json);


                        //任务执行完成 删除当前任务的缓存
                        redisTemplate.delete("taskConstant:" + TASKID);
                    } catch (Exception e) {
                        log.error("任务信息修改失败" + e);
                    }

                } else {       // 修改异常点数缓存
                    log.info("不满足修改任务条件，对正常异常点数进行修改...............");
                    redisTemplate.delete("taskConstant:" + TASKID);//清楚当前-1阶段所有点数
                    Map<String, String> mapForAbnormal = new HashMap<>();
                    mapForAbnormal.put("abnormal", String.valueOf(tAbnormal));
                    mapForAbnormal.put("normal", String.valueOf(tNormal));
                    redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
                    log.info("修改后的abnormal：" + tAbnormal);
                    log.info("修改后的normal：" + tNormal);
                }

            }


        }
    }

    /**
     * 请求webSocket发送方法
     *
     * @param url 请求地址
     * @param json 发送内容
     * @return String
     */
    public String postUrl(String url, String json) throws IOException, URISyntaxException {
        log.info("webSocketUrl"+url);
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(url).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    /**
     * Redis数据库批量查询Key值游标
     * @param key redis的key
     * @return Set<String>
     */
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



