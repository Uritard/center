package com.yjh.platform.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.GetSpringUtil;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.mqtt.ftpsService;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.ProcessResultToUpSystem;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.locks.ReentrantLock;

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
    private String stationCode;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 服务端口
     */
    private final int remotePort;

    private static ReentrantLock lock = new ReentrantLock();

    public DataDealThread(String body, int remotePort, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService,
                          String syncWebsocketUrl) {
        this.analyseDataOperateService = analyseDataOperateService;
        this.redisTemplate = redisTemplate;
        this.body = body;
        this.syncWebsocketUrl=syncWebsocketUrl;
        this.remotePort = remotePort;
    }

    private void yjskHandle(JSONObject jsonObjectResult){
        try{
            Map<String,String> recBack = new HashMap<>();
            recBack.put("meteId", jsonObjectResult.getString("instanceId"));
            recBack.put("code","200");
            recBack.put("desc",jsonObjectResult.getString("resultValue"));
            Constant.otherServerMap(recBack,Constant.picRecBack);
            log.info("一键顺控services：" + recBack);
        }catch (Exception e){
            log.error("一键顺控-变相信号-分析主机返回处理失败", e);
        }
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
            String reponseMessage=jsonObject.getString("msgID");
            log.info("reponseMessage:{}",reponseMessage);
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

                    if (TASKID != null && TASKID.contains("yjsk")){
                        //一键顺控结果处理
                        yjskHandle(jsonObjectResult);
                        return;
                    }
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
                    log.info("keyName: " + name);

                    String alarm_level = "";



                    log.info("数据初始化");

                    //redis数据键名由taskId+instanceId命名
                    log.info("数据Redis业务开启");
                    String redisName = TASKID + ":" + INSTANCEID;
                    log.info("template:" + redisTemplate);
                    log.info("redisName:" + redisName);
                    //巡视点信息存储Redis KeyName
                    String cruiseRedisName = "t_cruise_task_result:" + redisName;
                    Map<String, String> cruiseResult = redisTemplate.opsForHash().entries(cruiseRedisName);//读redis
                    log.info("读取到的redis：{}", JSON.toJSONString(cruiseResult));
                    String recognitionMode = cruiseResult.get("recognitionMode");
                    log.info("recognitionMode----识别模式 ：-------" + recognitionMode);
                    boolean dltFlag = NumberUtils.toInt(cruiseResult.get("cruiseType")) == 230 &&
                            Objects.nonNull(cruiseResult.get("resultNum")) && !Objects.equals("null", cruiseResult.get("resultNum"));
                    Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map




                    //处理判别结果
                    //获取分析结果图片地址
                    String taskId = TASKID;
                    Long instanceId = NumberUtils.toLong(INSTANCEID);
                    String resultImage=cruiseResult.get("picpath");
                    if(Objects.nonNull(jsonObjectResult.get("analyseResultImg"))) {
                        resultImage = jsonObjectResult.getString("analyseResultImg");
                    }
                    String analyseType = jsonObjectResult.getString("analyseType");
                    if ("11".equals(analyseType) && "".equals(resultImage)) {
                        continue;
                    }

                    log.info("端口号：" + remotePort);

                    try {
                        switch (remotePort) {
                            case 13668:

                                String analyseResultPic =cruiseResult.get("picpath");
                                if ("0".equals(recognitionMode)) {
                                    redisTemplate.opsForHash().put(cruiseRedisName, "recognitionMode", "-2");
                                }
                                //表计识别图片放入缓存(已考虑双算法)
                                if(StringUtils.isNotEmpty(jsonObjectResult.getString("analyseResultImg"))){
                                     analyseResultPic = jsonObjectResult.getString("analyseResultImg").replaceAll(
                                         (String)redisTemplate.opsForHash().get("t_sys_param:meterResultImg", "content"), (String)redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg", "content"));
                                    log.info("表计识别图片-------------------------------"+analyseResultPic);
                                    log.info("缓存地址-----------------------"+redisName);

                                    if (dltFlag){
                                        log.info("dltFlag {}, dlt664抓图", dltFlag);
                                    }else {
                                        if ("-1".equals(recognitionMode) || "-2".equals(recognitionMode)) {
                                            log.info("双算法-第二算法图片生成-M");
                                            redisTemplate.opsForHash().put(cruiseRedisName, "picpath", redisTemplate.opsForHash().get(cruiseRedisName, "picpath") + "," + analyseResultPic);
                                        } else {
                                            redisTemplate.opsForHash().put(cruiseRedisName, "picpath", analyseResultPic);
                                        }
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
                                    String resultString;
                                    if (dltFlag) {
                                        resultString = String.valueOf(cruiseResult.get("resultNum"));
                                    } else {
                                        resultString = jsonObjectResult.getString("resultValue");
                                    }
                                    // 红外会返回两个温度(如：12.3,2.3),所以需要这样取值
                                    resultString = resultString.split(",")[0];
                                    if (resultString.matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|^(-[0-9]{1,})$|^(-[0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]+")) {

                                        String alarmValue = resultString;
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
                                        TStdDeviceMete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));
                                        //单个数值表计识别结果的告警判断与处理
                                        TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));
                                        log.info("数据查询初始化");
                                        //满足告警数据结构-进行告警判断
                                        //表计测点未配置告警规则
                                        int warnFlag = analyseDataOperateService.warnSettings(tStdDevicemeteM.getMeteKind(),
                                                tStdDevicemeteM.getStateZero(),
                                                tStdDevicemeteM.getAlarmState(),
                                                tStdDevicemeteM.getHighLimit1(),
                                                tStdDevicemeteM.getLowLimit1(),
                                                tStdDevicemeteM.getHighLimit2(),
                                                tStdDevicemeteM.getLowLimit2(),
                                                tStdDevicemeteM.getHighLimit3(),
                                                tStdDevicemeteM.getLowLimit3(),
                                                tStdDevicemeteM.getHighLimit4(),
                                                tStdDevicemeteM.getLowLimit4());
                                        if ( warnFlag == 1 ) {
                                            //初始化告警信息redis表
                                            String warnName = "warnInfo:" + TASKID + String.valueOf(UUID.randomUUID()).replace("-", "");
                                            Map<String, String> warnMap = new HashMap<>();
//                                            warnMap.put("warnType", tStdDevicemeteM.getAlarmType());
                                            warnMap.put("deviceId", String.valueOf(tCruisePointInstance.getDeviceId()));
                                            warnMap.put("customId", tCruisePointInstance.getCustomId());
                                            warnMap.put("instanceId", jsonObjectResult.getString("instanceId"));
                                            warnMap.put("stdMeteId", String.valueOf(tCruisePointInstance.getDeviceMeteId()));
                                            warnMap.put("taskId", jsonObjectResult.getString("taskId"));
                                            warnMap.put("value", resultString);
                                            warnMap.put("imagePath", analyseResultPic);
                                            warnMap.put("confMode", "276");
                                            warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                            warnMap.put("defectModel", analyseDataOperateService.selectDictCode("defect_model", "其他"));

                                            log.info("开始告警判断");
                                            log.info("测点种类:" + tStdDevicemeteM.getMeteKind());

                                            switch (tStdDevicemeteM.getMeteKind()) {
                                                case "1":
                                                    String resultValue = resultString;
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
                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", jsonObjectResult.getString("resultValue"));
                                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                                        tNormal++;
                                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(0);
                                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 1, cruiseRedisName).get(1);
                                                    }
                                                    break;
                                                case "2":
                                                    Float resultValueMeter = NumberUtils.toFloat(resultString);
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

                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "异常告警", jsonObjectResult.getString("resultValue"));
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
                                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", jsonObjectResult.getString("resultValue"));
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

                                                    // 告警上报站端
                                                    StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem(cruiseResultMap, alarm_level, tWarnInfo);

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


                                                    if (one && two) {
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

                                                    //最近一条告警信息 入缓存
                                                    redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3, TimeUnit.MINUTES);
                                                    log.info("currentWarnInfo666: {}", JSON.toJSONString(currentWarnInfo));

//                                                    redisTemplate.opsForValue().set("currentWarn",currentWarnInfo,3,TimeUnit.MINUTES);

//
//                                                    //删除告警redis
//                                                    redisTemplate.delete(warnName);
                                                }
                                            } catch (Exception e) {
                                                log.error("告警入库失败", e);
                                            }

                                        } else {
                                            Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "正常", "--", jsonObjectResult.getString("resultValue"));
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

                                log.info("初始redis===={}", cruiseResult);
                                String analyseResultImg =cruiseResult.get("picpath");//图片路径初始化
                                if ("0".equals(recognitionMode)) {
                                    redisTemplate.opsForHash().put(cruiseRedisName, "recognitionMode", "-1");
                                }

                                //判别结果处理逻辑
                                if("11".equals(analyseType)){
                                    log.info("判别");

                                    if (resultImage.contains("analyseResultImg")){
                                        analyseResultImg = resultImage.replaceAll(
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content")),
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content")));
                                    }else {
                                        analyseResultImg = resultImage.replaceAll(
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
                                    }
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

                                        //判别异常
                                        String msgName = "msg:" + reponseMessage + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
                                        Map<String,String> map = new HashMap<>();
                                        map.put("value",jsonObjectResult.getString("resultValue"));
                                        redisTemplate.opsForHash().putAll(msgName,map);

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
                                    String objectResultString = jsonObjectResult.getString("analyseResultImg");
                                    if (objectResultString.contains("analyseResultImg")){
                                        analyseResultImg = objectResultString.replaceAll(
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")),
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")));
                                    }else {
                                        analyseResultImg = objectResultString.replaceAll(
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                                                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
                                    }
                                    if ("-1".equals(recognitionMode) || "-2".equals(recognitionMode)) {
                                        log.info("双算法-第二算法图片生成-D");
                                        redisTemplate.opsForHash().put(cruiseRedisName, "picpath", redisTemplate.opsForHash().get(cruiseRedisName, "picpath") + "," + analyseResultImg);
                                    } else {
                                        redisTemplate.opsForHash().put(cruiseRedisName, "picpath", analyseResultImg);
                                    }
                                }

                                // TODO: 2021/1/11 算法服务端需要区分数据异常和未识别出缺陷的情形
                                if (!"null".equals(resultValue ) && !(resultValue.contains("device"))) {
                                    //巡视点被识别出缺陷就会被判定为异常点，异常类型为--缺陷异常
                                    TStdDeviceMete tStdDevicemeteM = new TStdDeviceMete();
                                    try {
                                        Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "缺陷异常", resultValue);
                                        cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
                                        cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
                                        cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
//                                    tAbnormal++;
                                        tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
                                        tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);

                                         tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));

                                        //生成缺陷缓存信息（单一缺陷和多元缺陷）
                                        log.info("--------____--------生成缺陷缓存");

                                    }catch (Exception e){
                                        log.error(e.getMessage(), e);
                                    }
//                                    Map<String, String> doubleResultMap = analyseDataOperateService.doubleResultHandle(recognitionMode, cruiseRedisName, "异常", "缺陷异常", resultValue);
//                                    cruiseResultMap.put("resultNum", doubleResultMap.get("resultNum"));
//                                    cruiseResultMap.put("cruiseResult", doubleResultMap.get("cruiseResult"));
//                                    cruiseResultMap.put("cruiseAbnormal", doubleResultMap.get("cruiseAbnormal"));
////                                    tAbnormal++;
//                                    tNormal = tNormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(0);
//                                    tAbnormal = tAbnormal + analyseDataOperateService.mutiAlgoCount(recognitionMode, 0, cruiseRedisName).get(1);
//
//                                    TStdDeviceMete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(jsonObjectResult.getString("instanceId")));
//
//                                    //生成缺陷缓存信息（单一缺陷和多元缺陷）
//                                    log.info("--------____--------生成缺陷缓存");
                                    String[] resultArr = resultValue.split("\\s+");
                                    if (resultArr.length == 1) {
                                        log.info("--------____--------单一缺陷");
                                        TDefectInfo tDefectInfo=new TDefectInfo();//实时入库
                                        Map<String, String> defectMap = new HashMap<>();
                                        String redisFlag = jsonObjectResult.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");
                                        String defectRedisName = "defectInfo:" + redisFlag;
                                        String defectAlarmMsg="defect:" + reponseMessage + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
                                        // TODO: 2021/2/19 判别并获取对应缺陷的缺陷算法等级
                                        defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
                                        log.info("defectMap:"+defectMap);
                                        defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
                                        defectMap.put("defectContent", resultValue);
                                        defectMap.put("deviceId", cruiseResult.get("deviceId"));
                                        defectMap.put("instanceId", cruiseResult.get("instanceId"));
                                        TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResult.get("instanceId")));
                                        defectMap.put("customId", tStdDevicemete.getCustomId());
                                        defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                                        defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
                                        defectMap.put("imagePath", analyseResultImg);
                                        defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                        defectMap.put("defectTime", simpleDateFormat.format(new Date()));
                                        defectMap.put("value", jsonObjectResult.get("resultValue").toString());
                                        redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
                                        redisTemplate.opsForHash().putAll(defectAlarmMsg, defectMap);

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
                                            String defectAlarmMsg="defect:" + reponseMessage + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
//                                            defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                            log.info("redisFlag:"+redisFlag);
                                            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultArr[i]));
                                            defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
                                            defectMap.put("defectContent", resultArr[i]);
                                            defectMap.put("deviceId", cruiseResult.get("deviceId"));
                                            defectMap.put("instanceId", cruiseResult.get("instanceId"));
                                            TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResult.get("instanceId")));
                                            defectMap.put("customId", tStdDevicemete.getCustomId());
                                            defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                                            defectMap.put("confMode", "276");//确认状态--未核查
                                            defectMap.put("imagePath", analyseResultImg);
                                            defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                            defectMap.put("defectTime", defectTime);
                                            defectMap.put("value", jsonObjectResult.get("resultValue").toString());
                                            log.info("defectMap:"+defectMap);
                                            redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
                                            //jeff add
                                            redisTemplate.opsForHash().putAll(defectAlarmMsg, defectMap);

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
                                            String alarmNote = tStdDevicemete.getAlarmNote();  //jeff测试注释
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
                        log.error("告警/缺陷处理失败：", e);
                    }


                    if ("0".equals(recognitionMode)) {
                        //双算法-第一算法处理
                        log.info("DoubleAlgorithmDealing......................");
                        redisTemplate.opsForHash().putAll(cruiseRedisName, cruiseResultMap);
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

                        log.info("cruiseResultMap==={}", JSON.toJSONString(cruiseResultMap));
                        redisTemplate.opsForHash().putAll(cruiseRedisName, cruiseResultMap);//修改redis

                        // 巡视点结果上报站端
                        StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem( cruiseResultMap, null, null);
                        log.info("Border_______---------______________________________________________________________________________________________");
                        lock.lock();
                        try {
                            //若本任务上一次有巡视数据，则正常或异常点数要进行加和
                            if (redisTemplate.opsForHash().entries("taskConstant:" + TASKID).size() != 0) {
                                tNormal = NumberUtils.toInt(String.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tNormal"))) + tNormal;
                                tAbnormal = NumberUtils.toInt(String.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tAbnormal"))) + tAbnormal;
                            }
                            taskConstant.put("tNormal", String.valueOf(tNormal));
                            taskConstant.put("tAbnormal", String.valueOf(tAbnormal));
                            redisTemplate.opsForHash().putAll("taskConstant:" + TASKID, taskConstant);//插入任务常量Map
                            log.info("任务常量配置完成: {}", JSON.toJSONString(taskConstant));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        } finally {
                            lock.unlock();
                        }

                        // 巡视点结果上报站端
                        StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem(cruiseResultMap, null, null);


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
                        redisTemplate.opsForList().rightPush("analysisList:" + cruiseResult.get("taskId"), "0");

                        log.info("RedisList修改成功");
                    }

                }

                if(reponseMessage != null && !"".equals(reponseMessage)){
                    try{
                        //jeff: 每一次算法返回响应处理结束后，上传告警图片信息并发送告警信息到算法管理平台
                        Set<String> differentList= redisScan( "msg:" + reponseMessage);  //标记告警，也就是判别告警
                        Set<String> defectList=  redisScan("defect:" + reponseMessage);   //缺陷告警
                        Alarm alarmDetail=new Alarm();
                        ftpsService ftpsservice= GetSpringUtil.getBean("ftpsservice");
                        String flag= ftpsservice.getFlag();

                        SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String yearMonth = ym.format(new Date());
                        String nowTime = timeFormat.format(new Date());

                        if(differentList.size()>0&&("1".equals(flag))){
                            log.info("different类型:开始向算法管理平台发送图片和mqtt消息");


                            Map.Entry entrybak= jsonObjectData.entrySet().iterator().next();
                            JSONObject jsonObjectResultbak=JSON.parseObject(entrybak.getValue().toString());
                            String  instanceId=jsonObjectResultbak.getString("instanceId");
                            HashMap<String,String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
                            String picF = nowTime+"_"+nameMap.get("upRegionName")+"_"+nameMap.get("deviceName")+"_"+nameMap.get("meteName")+"_";

                            //,先取出算法平台返回的resultinfo中的结果图片路径
                            String resultImagebak=jsonObjectResultbak.getString("analyseResultImg");
                            String taskidbak=jsonObjectResultbak.getString("taskId");
                            Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskidbak+":"+instanceId);//读redis
                            String devicename= String.valueOf(cruiseResult2.get("deviceName"));
                            //,获取原始路径.并拼接算法管理平台对应远程文件路径
                            String origpicpath=String.valueOf(cruiseResult2.get("origpic").toString());
                            String[] str2=origpicpath.split("/");
                            String origpcimagename=str2[str2.length-1];
                            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+picF+"原图.jpg";
                            //,获取结果路径.并拼接算法管理平台对应远程文件路径
                            String[] str=resultImagebak.split("/");
                            String imagename=str[str.length-1];
                            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别告警.jpg";
                            //,获取基准路径.并拼接算法管理平台所需要的基准文件路径
                            TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(Long.valueOf(instanceId));
                            String Cruiseid=String.valueOf(tCruisePointInstance.getCruiseid());   //获取巡视点位id
                            String judgeBaseImagepath= redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content").toString();
                            judgeBaseImagepath=judgeBaseImagepath+"/"+Cruiseid+"/"+Cruiseid+".jpg"; //判定基准图路径位presetImgPath+巡视点+巡视点.jpg
                            //拼接算法管理平台分析告警结果图片地址
                            String remotebaseimagicpath=ftpsservice.getFtpsRemotePath() + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别基准.jpg";
                            Iterator it=differentList.iterator();
                            List<Different> defectList1=new ArrayList<>();
                            while (it.hasNext()){
                                String key=it.next().toString();//所有的key
                                Map<String, String>  differentlistmap= redisTemplate.opsForHash().entries(key);
                                String resultinfo=differentlistmap.get("value");      //获取返回的resultvalue值,判别类是一个数值，缺陷类是坐标
                                log.info("判别结果：{}",resultinfo);
                                Different different= new Different();
                                String[] re = resultinfo.split(",");
                                if(re != null && re.length > 4){
                                    different.setX1((int) NumberUtils.toDouble(re[1]));
                                    different.setY1((int) NumberUtils.toDouble(re[2]));
                                    different.setX2((int) NumberUtils.toDouble(re[3]));
                                    different.setY2((int) NumberUtils.toDouble(re[4]));
                                }else {
                                    different.setX1((int) NumberUtils.toDouble(resultinfo));
                                    different.setY1(0);
                                    different.setX2(0);
                                    different.setY2(0);
                                }
                                defectList1.add(different);
                                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                                alarmDetail.setDevice_name(devicename);
                                alarmDetail.setPoint_name(nameMap.get("meteName"));
                                alarmDetail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
                                alarmDetail.setPic_diff_base(remotebaseimagicpath);               //判别基准图路径
                                alarmDetail.setPic_different(remotefilepath);               //判别告警图路径,判别结果图
                                alarmDetail.setPic_defect("");      //缺陷告警图路径//
                            }
                            alarmDetail.setDifferent(defectList1);
                            ftpsservice.uploadFile("判别告警",origpicpath,remoteorigfilepath);  //原始图片上传
                            ftpsservice.uploadFile("判别告警",judgeBaseImagepath,remotebaseimagicpath);  //判别基准图片上传
                            ftpsservice.uploadFile("判别告警",resultImagebak,remotefilepath);            //判别结果图片
                            log.info("判别预算法主机origpicpath:{}， remoteorigfilepath:{}",origpicpath,remoteorigfilepath);
                            log.info("判别预算法主机judgeBaseImagepath:{}， remotebaseimagicpath:{}",judgeBaseImagepath,remotebaseimagicpath);
                            log.info("判别预算法主机resultImagebak:{}， remotefilepath:{}",resultImagebak,remotefilepath);
                            //可靠性 文件是否传输成功
                            if( !ftpsservice.fileExits(remoteorigfilepath)){
                                ftpsservice.uploadFile("判别告警",origpicpath,remoteorigfilepath);  //原始图片上传
                            }
                            if( !ftpsservice.fileExits(remotebaseimagicpath)){
                                ftpsservice.uploadFile("判别告警",judgeBaseImagepath,remotebaseimagicpath);  //判别基准图片上传
                            }
                            if( !ftpsservice.fileExits(remotefilepath)){
                                ftpsservice.uploadFile("判别告警",resultImagebak,remotefilepath);            //判别结果图片
                            }
                            AlarmService alarmService= GetSpringUtil.getBean("alarmService");
                            alarmService.PushMsg(alarmDetail);

                            // 判别上报站端
                            String taskId = jsonObjectResultbak.getString("taskId");

                            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
                            StaticContextAccessor.getBean(ProcessResultToUpSystem.class).defectAndDistinguishToUpSystem(cruiseResultMap, differentList);
                        }

                        if(defectList.size()>0&&("1".equals(flag))){
                            log.info("defect类型:开始向算法管理平台发送图片和mqtt消息");
                            Map.Entry entrybak= jsonObjectData.entrySet().iterator().next();
                            JSONObject jsonObjectResultbak=JSON.parseObject(entrybak.getValue().toString());
                            String  instanceId=jsonObjectResultbak.getString("instanceId");
                            HashMap<String,String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
                            String picF = nowTime+"_"+ nameMap.get("upRegionName")+"_"+nameMap.get("deviceName")+"_"+nameMap.get("meteName")+"_";

                            //,先取出算法平台返回的resultinfo中的结果图片路径
                            String resultImagebak=jsonObjectResultbak.getString("analyseResultImg"); //分析结果过
                            String taskidbak=jsonObjectResultbak.getString("taskId");
                            Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskidbak+":"+instanceId);//读redis
                            String devicename= String.valueOf(cruiseResult2.get("deviceName"));
                            // 获取原始图路径
                            String origpicpath=String.valueOf(cruiseResult2.get("origpic"));
                            String[] str2=origpicpath.split("/");
                            String origpcimagename=str2[str2.length-1];
                            //拼接算法管理平台原始图片推送地址
                            String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+picF+"原图.jpg";
                            log.info("开始向算法管理平台发送图片和mqtt消息");
                            String[] str=resultImagebak.split("/");
                            String imagename=str[str.length-1];
                            //拼接算法管理平台分析告警结果图片地址
                            String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+picF+"缺陷告警.jpg";
                            Iterator it=defectList.iterator();

                            while (it.hasNext()){
                                String key=it.next().toString();//所有的key
                                Map<String, String>  differenmap= redisTemplate.opsForHash().entries(key);
                                String resultinfo=differenmap.get("value");      //获取返回的resultvalue值，这个值就是缺陷和判别的x,y位置信息
                                log.info("缺陷结果：{}",resultinfo);
                                String[] arr1 = resultinfo.split(",");   //目前格式："sly_dmyw,0,171,502,667,bj_bpps,745,143,724,923"
                                List<Defect> defectList1=new ArrayList<>();
                                for(int i=0;i<arr1.length;){
                                    Defect defect=new Defect();
                                    defect.setX1((int)NumberUtils.toDouble(arr1[i+1]));
                                    defect.setY1((int)NumberUtils.toDouble(arr1[i+2]));
                                    defect.setX2((int)NumberUtils.toDouble(arr1[i+3]));
                                    defect.setY2((int)NumberUtils.toDouble(arr1[i+4]));
                                    defect.setType(arr1[i]);
                                    // DecimalFormat df =  new DecimalFormat("0%");
                                    // String confidence = df.format(Double.valueOf(arr1[i+5]));
                                    int confidence = (int)(NumberUtils.toDouble(arr1[i+5]) * 100);
                                    defect.setConfidence(confidence);
                                    defect.setDesc(differenmap.get("defectContent")+"(坐标位置 "+defect.getX1()+","+
                                            defect.getY1()+","+
                                            defect.getX2()+","+
                                            defect.getY2()+";"+
                                            "置信度 "+ confidence
                                            +"%)");
                                    defectList1.add(defect);
                                    i=i+6;
                                }
                                alarmDetail.setDefect(defectList1);
                                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                                alarmDetail.setDevice_name(devicename);  //需要修改位devicename
                                alarmDetail.setPoint_name(nameMap.get("meteName"));
                                alarmDetail.setTime(differenmap.get("defectTime"));
                                alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
                                alarmDetail.setPic_diff_base("");               //判别基准图路径
                                alarmDetail.setPic_different("");               //判别告警图路径,即分析结果图
                                alarmDetail.setPic_defect(remotefilepath);      //缺陷告警图路径//
                            }
                            ftpsservice.uploadFile("遥信告警",origpicpath,remoteorigfilepath);
                            ftpsservice.uploadFile("遥信告警",resultImagebak,remotefilepath);
                            if( !ftpsservice.fileExits(remoteorigfilepath)){
                                ftpsservice.uploadFile("遥信告警",origpicpath,remoteorigfilepath);  //原始图片上传
                            }
                            if( !ftpsservice.fileExits(remotefilepath)){
                                ftpsservice.uploadFile("遥信告警",resultImagebak,remotefilepath);
                            }

                            log.info("巡视主机与智能分析主机：origpicpath:{}",origpicpath);
                            log.info("巡视主机与智能分析主机：remoteorigfilepath:{}",remoteorigfilepath);
                            log.info("巡视主机与智能分析主机：resultImagebak:{}",resultImagebak);
                            log.info("巡视主机与智能分析主机：remotefilepath:{}",remotefilepath);
//                       ftp文件上传测试数据
//                      String localpath="D://信息化工作.jpg";
//                      ftpsservice.uploadFile("遥信告警",localpath,remotefilepath);
                            AlarmService alarmService= GetSpringUtil.getBean("alarmService");
                            alarmService.PushMsg(alarmDetail);
                            log.info("发送算法管理平台结束");

                            // 缺陷上报站端
                            String taskId = jsonObjectResultbak.getString("taskId");

                            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
                            StaticContextAccessor.getBean(ProcessResultToUpSystem.class).defectAndDistinguishToUpSystem(cruiseResultMap, defectList);
                        }
                    } catch (Exception e) {
                        log.info("与算法管理平台交互失败" + e);
                    }

                    //jeff: mqtt消息发个告警平台结束
                }


            } else if ("6".equals(jsonObject.getString("msgType"))) { //任务结束后发来的心跳信息

                    String taskId = JSON.parseObject(jsonObject.getString("msgData")).getString("taskId");
                    String instanceId = JSON.parseObject(jsonObject.getString("msgData")).getString("instanceId");
                    redisTemplate.opsForList().leftPush("analysisList:" + taskId, "-1");
                    log.info("心跳处理结束" + taskId);
                    //处理心跳线程私有变量 taskId赋值
                    TASKID = taskId;
            }

            log.info("TASKID============{}", TASKID);
            if(TASKID == null){
                TASKID = JSON.parseObject(jsonObject.getString("msgData")).getString("taskId");
                log.info("TASKID为空后重新复制{}", TASKID);
            }


            log.info("判断:{}", redisTemplate.opsForList().index("analysisList:" + TASKID, 0));
            if ("-1".equals(redisTemplate.opsForList().index("analysisList:" + TASKID, 0))) {  //满足插库条件
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String finalCruiseKey = "t_cruise_task_result:" + TASKID + ":" + INSTANCEID;//最后修改任务表信息所需的redis KEY名
                log.info("任务结束-开始数据存储");
                List<String> cruiseResultIds = new ArrayList<>();//cruiseResultIds
                try {
                    //满足条件先插巡视点数据
                    List<TCruiseTaskResultDetail> detailList = new ArrayList<>();//TCTRD List对象
                    List<TCruiseDataResult> dataList = new ArrayList<>();//TCDR List对象
                    List<TDefectInfo> defectList = new ArrayList<>();//TDI List对象

                    //从redisList中取出目前为止本任务中执行算法的所有巡视点
                    long size = redisTemplate.opsForList().size("cruiseKeys:" + TASKID) - 1;
                    log.info("cruiseKeys size==={}", size);
                    List<String> cruiseKeys = redisTemplate.opsForList().range("cruiseKeys:" + TASKID, 0, -1);
                    log.info("cruisekeys: {}", JSON.toJSONString(cruiseKeys));
                    if(CollectionUtils.isNotEmpty(cruiseKeys)) {
                        finalCruiseKey = cruiseKeys.get(0);//挑选一名幸运Redis KEY值
                        log.info("finalCruiseKey===={}", finalCruiseKey);
                        for (String cruiseKey : cruiseKeys) {
                            Map<String, String> cruiseWorkedMap = redisTemplate.opsForHash()
                                    .entries(cruiseKey);//取出缓存中该任务下的巡视点
                            log.info("cruiseWorkedMap==={}", cruiseWorkedMap);
                            //TCTRD
                            log.info("TCTRD开始");
                            TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                            tCruiseTaskResultDetail.setCruiseResultId(
                                    cruiseWorkedMap.get("taskResultId") + cruiseWorkedMap.get("instanceId"));
                            tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId"));
                            tCruiseTaskResultDetail.setDeviceId(NumberUtils.toLong(cruiseWorkedMap.get("deviceId")));
                            tCruiseTaskResultDetail.setDeviceName(cruiseWorkedMap.get("deviceName"));
                            tCruiseTaskResultDetail
                                    .setInstanceId(NumberUtils.toLong(cruiseWorkedMap.get("instanceId")));
                            tCruiseTaskResultDetail.setInstanceName(cruiseWorkedMap.get("instanceName"));
                            tCruiseTaskResultDetail
                                    .setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime")));
                            tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime")));
                            tCruiseTaskResultDetail
                                    .setCruiseStatus(NumberUtils.toInt(cruiseWorkedMap.get("cruiseStatus")));
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
                            tCruiseDataResult.setEvaluationState(NumberUtils
                                    .toInt(analyseDataOperateService.selectDictCode("evaluation_state", "未审核")));
                            tCruiseDataResult.setCruiseResult(NumberUtils.toInt(cruiseWorkedMap.get("cruiseResult")));
                            if ("--".equals(cruiseWorkedMap.get("cruiseAbnormal"))) {
                                tCruiseDataResult.setCruiseAbnormal(null);
                            } else {
                                if (cruiseWorkedMap.get("cruiseAbnormal").contains(",")) {
                                    String[] abnormalArr = cruiseWorkedMap.get("cruiseAbnormal").split(",");
                                    tCruiseDataResult.setCruiseAbnormal(NumberUtils.toInt(abnormalArr[0]));
                                    tCruiseDataResult.setRemark(abnormalArr[1]);
                                } else {
                                    tCruiseDataResult.setCruiseAbnormal(
                                            NumberUtils.toInt(cruiseWorkedMap.get("cruiseAbnormal")));
                                }

                            }
                            tCruiseDataResult
                                    .setEvaluationState(NumberUtils.toInt(cruiseWorkedMap.get("evaluationState")));
                            tCruiseDataResult.setCreatetime(new Date());
                            tCruiseDataResult.setCruiseResultId(
                                    cruiseWorkedMap.get("taskResultId") + cruiseWorkedMap.get("instanceId"));
                            tCruiseDataResult.setIsWarn(NumberUtils.toInt(cruiseWorkedMap.get("isWarn")));
                            if (Objects.nonNull(cruiseWorkedMap.get("firDocPath"))) {
                                tCruiseDataResult
                                        .setResultPic(cruiseWorkedMap.get("firDocPath").replace("dfir", "fir"));
                            } else {
                                tCruiseDataResult.setResultPic("");
                            }
                            if (Objects.nonNull(cruiseWorkedMap.get("firName")) && !("null"
                                    .equals(cruiseWorkedMap.get("firName")))) {
                                tCruiseDataResult.setFirName(cruiseWorkedMap.get("firName"));
                                tCruiseDataResult.setFirDate(new SimpleDateFormat("yyyyMMddHHmmssSSS")
                                        .parse(tCruiseDataResult.getFirName()));
                                // TODO: 2021/3/25 FIR文件名与时间赋值
                            }
                            log.info("TCDR内容：" + tCruiseDataResult);
                            dataList.add(tCruiseDataResult);
                            // 删除相应入库点位缓存
                            redisTemplate.opsForList().remove("cruiseKeys:" + TASKID, 0, cruiseKey);
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
//                    redisTemplate.delete("cruiseKeys:" + TASKID);
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
                    }
                } catch (Exception e) {
                    log.error("插表错误", e);
                }

                lock.lock();
                String strForCountAbnormal = "countForAbnormal:" + TASKID;
                Integer total = null;
                int tAbnormal = 0;
                int tNormal = 0;
                try {
                    //插库时从redis中取出正常、异常点数
                    Map<String, String> taskConstant = redisTemplate.opsForHash().entries("taskConstant:" + TASKID);
                    tAbnormal = NumberUtils.toInt(taskConstant.get("tAbnormal"));
                    tNormal = NumberUtils.toInt(taskConstant.get("tNormal"));
                    log.info("正常点数：" + tNormal);
                    log.info("异常点数：" + tAbnormal);

                    Map<String, String> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
                    total = NumberUtils.toInt(abnormalCount.get("all"));
                    Integer abnormal = NumberUtils.toInt(abnormalCount.get("abnormal"));
                    Integer normal = NumberUtils.toInt(abnormalCount.get("normal"));
                    log.info("All：" + total);
                    //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
                    tAbnormal = tAbnormal + abnormal;
                    tNormal = tNormal + normal;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                if (tAbnormal + tNormal == total) {
                    // 提前解锁
                    lock.unlock();
                    //TCTR开始
                    try {
                        Thread.sleep(10000);

                        // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
                        Map<String, String> cruiseResult = redisTemplate.opsForHash().entries(finalCruiseKey);
                        log.info("cruiseResult==={}", cruiseResult);

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
                        if (Objects.nonNull(analyseDataOperateService.selectLaterTaskCruiseResult(tCruiseTaskResult.getTaskId()))
                        || tCruiseTaskResult.getTaskResultId() == null) {
                            log.info("TCTR已存在, TaskResultId: {}", tCruiseTaskResult.getTaskResultId());
                        } else {
                            analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);
                        }


                        //TCR开始
                        log.info("TCR开始");
                        log.info("==="+cruiseResult.get("taskResultId"));
                        TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId"),TASKID);
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
                        log.error("任务信息修改失败", e);
                    }

                } else {       // 修改异常点数缓存
                    try {
                        log.info("不满足修改任务条件，对正常异常点数进行修改...............");
                        redisTemplate.delete("taskConstant:" + TASKID);//清楚当前-1阶段所有点数
                        Map<String, String> mapForAbnormal = new HashMap<>();
                        mapForAbnormal.put("abnormal", String.valueOf(tAbnormal));
                        mapForAbnormal.put("normal", String.valueOf(tNormal));
                        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
                        log.info("修改后的abnormal：" + tAbnormal);
                        log.info("修改后的normal：" + tNormal);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        lock.unlock();
                    }
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



