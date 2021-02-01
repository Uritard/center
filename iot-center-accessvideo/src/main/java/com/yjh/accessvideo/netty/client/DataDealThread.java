package com.yjh.accessvideo.netty.client;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.websocket.WebSocketServer;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;

//import static com.yjh.accessvideo.common.Constant.INSTANCEID;
//import static com.yjh.accessvideo.common.Constant.TASKID;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private String body;
    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    private ChannelHandlerContext ctx;
    private String TASKID;
    private String INSTANCEID;

    public DataDealThread(String body, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService, ChannelHandlerContext ctx) {
        this.analyseDataOperateService = analyseDataOperateService;
        this.redisTemplate = redisTemplate;
        this.body = body;
        this.ctx = ctx;
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


    @SneakyThrows
    @Override
    public void run() {
        //TODO 添加线程池
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));//Port:13668-表计识别,Port:13669-缺陷识别
        String usefulBody = analyseDataOperateService.nonUnpacking(body);//反拆包解析
//        String bodyTemp=body.replaceAll("\\S+","");
//        String usefulBody=bodyTemp.replaceAll("\\{.*?\\}\\{","{");
        if (usefulBody != "") {
            JSONObject jsonObject = JSON.parseObject(usefulBody);
            log.info("JSON对象1：" + jsonObject);
            if (jsonObject.get("msgType").toString().equals("2")) {
                JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.get("msgData").toString()).get("data").toString()); //全量数据结果集
                log.info("原生数据****：" + jsonObjectData);
                Iterator iterator = jsonObjectData.entrySet().iterator();//迭代器取出data中的每一个resultInfo
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    //遍历每一个结果子集
                    JSONObject jsonObjectResult = JSON.parseObject(entry.getValue().toString());
                    log.info("数据****：" + jsonObjectResult);//打印resultInfo
                    //初始化TASKID和INSTANCEID
                    TASKID = jsonObjectResult.get("taskId").toString();
                    INSTANCEID = jsonObjectResult.get("instanceId").toString();
                    String name = "t_cruise_task_result:" + TASKID + ":" + INSTANCEID;
                    //将每个任务下所需的正常点数、异常点数放入"任务常量Map"中
                    Map<String, String> taskConstant = new HashMap<>();
                    //把巡视点redis名称放入任务算法巡视点List中
                    redisTemplate.opsForList().leftPush("cruiseKeys:" + TASKID, name);
                    Integer tAbnormal = 0;
                    Integer tNormal = 0;
                    taskConstant.put("taskId", TASKID);
                    log.info("keyName" + name);

                    String alarm_level = "";


                    log.info("数据初始化");

                    //redis数据键名由taskId+instanceId命名
                    log.info("数据Redis业务开启");
                    String redisName = jsonObjectResult.get("taskId").toString() + ":" + jsonObjectResult.get("instanceId").toString();
                    log.info("template:" + redisTemplate);
                    log.info("redisName:" + redisName);
                    Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + redisName);//读redis
                    log.info("读取到的redis：" + cruiseResult);
                    Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map

                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                    log.info("端口号：" + remotePort);

                    try {
                        switch (remotePort) {
                            case 13668:
                                if (jsonObjectResult.get("resultValue").equals("NULL_Model")) {
                                    cruiseResultMap.put("resultNum", "缺少标定文件");
                                    cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                    cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "数据异常"));
                                    tAbnormal++;
                                } else if (jsonObjectResult.get("resultValue").equals("识别失败")) {
                                    cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
                                    cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                    cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "数据异常"));
                                    tAbnormal++;
                                } else {
                                    if (jsonObjectResult.get("resultValue").toString().matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]")) {
                                        cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());

                                        log.info("开始告警预处理");
                                        TStdDevicemete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
                                        //单个数值表计识别结果的告警判断与处理
                                        TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
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
                                            warnMap.put("warnType", tStdDevicemeteM.getAlarmNote());
                                            warnMap.put("deviceId", tCruisePointInstance.getDeviceId().toString());
                                            warnMap.put("customId", tCruisePointInstance.getCustomId());
                                            warnMap.put("instanceId", jsonObjectResult.get("instanceId").toString());
                                            warnMap.put("stdMeteId", tCruisePointInstance.getDeviceMeteId().toString());
                                            warnMap.put("taskId", jsonObjectResult.get("taskId").toString());
                                            warnMap.put("value", jsonObjectResult.get("resultValue").toString());
                                            warnMap.put("imagePath", cruiseResult.get("picpath").toString());
                                            warnMap.put("confMode","276");
                                            warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                                            warnMap.put("defectModel",analyseDataOperateService.selectDictCode("defect_model","其他"));
                                            log.info("开始告警判断");
                                            log.info("测点种类:" + tStdDevicemeteM.getMeteKind());
                                            switch (tStdDevicemeteM.getMeteKind()) {
                                                case "1":
                                                    String resultValue = jsonObjectResult.get("resultValue").toString();
                                                    int warnRuleTeleSigning = analyseDataOperateService.warnJudgementTelesignaling(resultValue,
                                                            tStdDevicemeteM.getStateZero(),
                                                            tStdDevicemeteM.getStateOne(),
                                                            tStdDevicemeteM.getAlarmState()
                                                    );
                                                    log.info("告警结果：" + warnRuleTeleSigning);
                                                    if (warnRuleTeleSigning == 1) {
                                                        log.info("告警信息生成");
                                                        warnMap.put("warnLevel", tStdDevicemeteM.getAlarmLevel().toString());
                                                        warnMap.put("warnName", tStdDevicemeteM.getMeteName() + "状态异常");
                                                        warnMap.put("warnTime", simpleDateFormat.format(new Date()));
                                                        if (tStdDevicemeteM.getAlarmState() == 0) {
                                                            warnMap.put("warnContent",  tStdDevicemeteM.getMeteName() +":"+ tStdDevicemeteM.getStateZero() + "--"+analyseDataOperateService.selectDictNote(tStdDevicemeteM.getAlarmLevel().toString(),"alarm_level"));
                                                        } else {
                                                            warnMap.put("warnContent", tStdDevicemeteM.getMeteName() +":"+ tStdDevicemeteM.getStateOne() + "--"+analyseDataOperateService.selectDictNote(tStdDevicemeteM.getAlarmLevel().toString(),"alarm_level"));
                                                        }
                                                        warnMap.put("outRange", "--");

                                                        log.info("告警MAP：" + warnMap);
                                                        try {
                                                            redisTemplate.opsForHash().putAll(warnName, warnMap);
                                                            cruiseResultMap.put("isWarn", "1");
                                                        } catch (Exception e) {
                                                            log.info("生成错误", e);
                                                        }

                                                        cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                                        cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "异常告警"));
                                                        tAbnormal++;

                                                    } else {
                                                        //未达到告警值(遥信)
                                                        cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "正常"));
                                                        cruiseResultMap.put("cruiseAbnormal", "--");
                                                        tNormal++;
                                                    }
                                                    break;
                                                case "2":
                                                    Float resultValueMeter = Float.valueOf(jsonObjectResult.get("resultValue").toString());
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
                                                        switch (warnRuleMeter) {
                                                            case 1:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                                                warnMap.put("warnContent",  tStdDevicemeteM.getMeteName() + ":"+cruiseResultMap.get("resultNum")+"--"+"预警");
                                                                alarm_level = "1";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit1()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit1()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit1() - resultValueMeter));
                                                                }
                                                                break;
                                                            case 2:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":"+cruiseResultMap.get("resultNum")+"--" + "一般");
                                                                alarm_level = "2";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit2()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit2()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit2() - resultValueMeter));
                                                                }
                                                                break;
                                                            case 3:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":"+cruiseResultMap.get("resultNum")+"--" + "严重");
                                                                alarm_level = "3";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit3()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit3()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit3() - resultValueMeter));
                                                                }
                                                                break;

                                                            case 4:
                                                                warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                                                warnMap.put("warnContent", tStdDevicemeteM.getMeteName() + ":"+cruiseResultMap.get("resultNum")+"--"+ "危急");
                                                                alarm_level = "4";
                                                                if (resultValueMeter >= tStdDevicemeteM.getHighLimit4()) {
                                                                    warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit4()));
                                                                } else {
                                                                    warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit4() - resultValueMeter));
                                                                }
                                                                break;
                                                        }

                                                        redisTemplate.opsForHash().putAll(warnName, warnMap);
                                                        cruiseResultMap.put("isWarn", "1");
                                                        log.info("告警Map:" + warnMap);

                                                        cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                                        cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "异常告警"));
                                                        tAbnormal++;
                                                    } else {
                                                        //未达到告警值（遥测）
                                                        cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "正常"));
                                                        cruiseResultMap.put("cruiseAbnormal", "--");
                                                        tNormal++;
                                                    }
                                                    break;
                                                default:
                                                    break;
                                            }
                                            try {
                                                // TODO: 2020/12/15 告警信息插库 并推送
                                                Map<String, Object> warningMsg = redisTemplate.opsForHash().entries(warnName);
                                                log.info("warnMsg:"+warningMsg);
                                                if (warningMsg.size() != 0) {
                                                    TWarnInfo tWarnInfo = new TWarnInfo();
                                                    tWarnInfo.setWarnLevel(Integer.valueOf(warningMsg.get("warnLevel").toString()));
                                                    tWarnInfo.setWarnType(Integer.valueOf(warningMsg.get("warnType").toString()));
                                                    tWarnInfo.setDeviceId(Long.valueOf(warningMsg.get("deviceId").toString()));
                                                    tWarnInfo.setCunstomId(warningMsg.get("customId").toString());
                                                    tWarnInfo.setInstanceId(Long.valueOf(warningMsg.get("instanceId").toString()));
                                                    tWarnInfo.setStdMeteId(Long.valueOf(warningMsg.get("stdMeteId").toString()));
                                                    tWarnInfo.setTaskId(warningMsg.get("taskId").toString());
                                                    tWarnInfo.setValue(warningMsg.get("value").toString());
                                                    tWarnInfo.setConfMode(Integer.valueOf(warningMsg.get("confMode").toString()));
                                                    tWarnInfo.setImagePath(warningMsg.get("imagePath").toString());
                                                    tWarnInfo.setAlarmSource(Integer.valueOf(warningMsg.get("alarmSource").toString()));
                                                    tWarnInfo.setWarnName(warningMsg.get("warnName").toString());
                                                    tWarnInfo.setOutRange(warningMsg.get("outRange").toString());
                                                    tWarnInfo.setWarnContent(warningMsg.get("warnContent").toString());
                                                    tWarnInfo.setDefectModel(Integer.valueOf(warningMsg.get("defectModel").toString()));
                                                    tWarnInfo.setWarnTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(warningMsg.get("warnTime").toString()));
                                                    log.info("------------------------------------------------------------");
                                                    analyseDataOperateService.insertWarnInfo(tWarnInfo);

                                                    {
                                                        //告警上报站端
                                                        XMLBaseModel xmlBaseModel = new XMLBaseModel();
                                                        List<Map<String,Object>> xmlItems = new ArrayList<>();
                                                        Map<String,Object> xmlItem = new HashMap<>();
                                                        xmlBaseModel.setType("62");
                                                        xmlItem.put("patroldevice_code",cruiseResult.get("instanceId"));
                                                        xmlItem.put("task_name",cruiseResult.get("taskName"));
                                                        xmlItem.put("task_code",cruiseResult.get("taskCode"));
                                                        xmlItem.put("device_name",cruiseResult.get("instanceName"));
                                                        xmlItem.put("device_id",cruiseResult.get("instanceId"));
                                                        xmlItem.put("alarm_level",alarm_level);
                                                        String deviceMeteId = cruiseResult.get("device_mete_id").toString();
                                                        Map<String,Object> info = analyseDataOperateService.selectWarnInfo(Long.valueOf(deviceMeteId));
                                                        xmlItem.put("alarm_type",(null== info.get("alarm_type") ? "": info.get("alarm_type")));
                                                        xmlItem.put("recognition_type",(null== info.get("recognition_type") ? "": info.get("recognition_type")));
                                                        xmlItem.put("value",tWarnInfo.getValue());
                                                        xmlItem.put("unit",(null== info.get("unit") ? "": info.get("unit")));
                                                        xmlItem.put("value_unit",tWarnInfo.getValue()+xmlItem.get("unit"));
                                                        xmlItem.put("time",simpleDateFormat.format(new Date()));
                                                        xmlItem.put("task_patrolled_id",cruiseResult.get("taskId"));
                                                        xmlItem.put("content",tWarnInfo.getWarnContent());


                                                        xmlItems.add(xmlItem);
                                                        xmlBaseModel.setItems(xmlItems);
                                                        List<XMLBaseModel> list = new ArrayList<>();
                                                        list.add(xmlBaseModel);
                                                        Map<String,List<XMLBaseModel>> map = new HashMap<>();
                                                        map.put("list",list);
                                                        log.info("告警上报：-"+map);
                                                        //Constant.otherServer(map,Constant.TCP_URL);
                                                    }

                                                    // webSocket通知前端刷新告警统计数量
                                                    Map<String, Object> jasonMaps = new HashMap<>();
                                                    jasonMaps.put("type", "newAlarm");
                                                    jasonMaps.put("alarmName", warningMsg.get("warnName"));
                                                    jasonMaps.put("alarmTime", warningMsg.get("warnTime"));
                                                    jasonMaps.put("alarmContent", warningMsg.get("warnContent"));
                                                    String jsons = JSON.toJSONString(jasonMaps);
                                                    log.info("告警生成-前端推送：" + jsons);
                                                    WebSocketServer.sendMsg(jsons);

                                                    //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
                                                    String alarmNote = tStdDevicemeteM.getAlarmNote();
                                                    Integer alarmLevel = tStdDevicemeteM.getAlarmLevel();
                                                    Integer warnLevel = tWarnInfo.getWarnLevel();
                                                    log.info("该测点是否配置了告警提示是===" + alarmNote);
                                                    log.info("该测点告警推送配置的告警等级是===" + alarmLevel);
                                                    log.info("产生的该条告警等级是===" + warnLevel);
                                                    log.info("一层判断" + (alarmNote != null && "1".equals(alarmNote)));
                                                    log.info("二层判断" + (warnLevel == alarmLevel || warnLevel > alarmLevel));

                                                    if (alarmNote != null && "1".equals(alarmNote)) {
                                                        if (warnLevel.compareTo(alarmLevel) == 0 || warnLevel > alarmLevel) {
                                                            //webSocket通知前端调用查询告警弹框的接口
                                                            Map<String, Object> jasonMaps2 = new HashMap<>();
                                                            jasonMaps2.put("type", "alarmPopUp");
                                                            jasonMaps2.put("warnId", tWarnInfo.getWarnId());
                                                            jasonMaps2.put("defectModel", tWarnInfo.getDefectModel());
                                                            String json = JSON.toJSONString(jasonMaps2);
                                                            log.info("发送给前端的消息：" + json);
                                                            WebSocketServer.sendMsg(json);
                                                        }
                                                    }

                                                    //删除告警redis
                                                    redisTemplate.delete(warnName);
                                                }
                                            } catch (Exception e) {
                                                log.error("告警入库失败" + e);
                                            }

                                        } else {
                                            cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "正常"));
                                            cruiseResultMap.put("cruiseAbnormal", "--");
                                            tNormal++;
                                        }
                                    } else {
                                        cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
                                        cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                        cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "数据异常"));
                                        tAbnormal++;
                                    }
                                }

                                break;
                            case 13669:
                                String originResult = jsonObjectResult.get("resultValue").toString();
                                String resultValue = analyseDataOperateService.resolveDefectResult(jsonObjectResult.get("resultValue").toString());
                                log.info("解析的缺陷数据："+resultValue);
                                // TODO: 2021/1/11 算法服务端需要区分数据异常和未识别出缺陷的情形 
                                if (resultValue != "null") {
                                   //巡视点被识别出缺陷就会被判定为异常点，异常类型为--缺陷异常
                                    cruiseResultMap.put("resultNum", resultValue);
                                    cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "异常"));
                                    cruiseResultMap.put("cruiseAbnormal", analyseDataOperateService.selectDictCode("abnormal_type", "缺陷异常"));
                                    tAbnormal++;

                                    //生成缺陷缓存信息（单一缺陷和多元缺陷）
                                    log.info("--------____--------生成缺陷缓存");
                                    String[] resultArr = resultValue.split("\\s+");
                                    if (resultArr.length == 1) {
                                        log.info("--------____--------单一缺陷");
                                        Map<String, String> defectMap = new HashMap<>();
                                        String defectRedisName = "defectInfo:" + jsonObjectResult.get("taskId").toString() + String.valueOf(UUID.randomUUID()).replace("-", "");
                                        defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                        defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
                                        defectMap.put("defectContent", resultValue);
                                        defectMap.put("deviceId", cruiseResult.get("deviceId").toString());
                                        defectMap.put("instanceId", cruiseResult.get("instanceId").toString());
                                        TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(cruiseResult.get("instanceId").toString()));
                                        defectMap.put("customId", tStdDevicemete.getCustomId());
                                        defectMap.put("stdMeteId", tStdDevicemete.getDeviceMeteId().toString());
                                        defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
                                        defectMap.put("imagePath", cruiseResult.get("picpath").toString());
                                        defectMap.put("alarmSource",analyseDataOperateService.selectDictCode("alarm_source","主辅设备"));
                                        defectMap.put("defectTime",simpleDateFormat.format(new Date()));
                                        redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
                                    } else if (resultArr.length > 1) {
                                        log.info("--------____--------多元缺陷");
                                        String defectTime=simpleDateFormat.format(new Date());
                                        for (int i = 0; i < resultArr.length; i++) {
                                            log.info("缺陷处理方法：" + resultArr[i]);
                                            Map<String, String> defectMap = new HashMap<>();
                                            String defectRedisName = "defectInfo:" + jsonObjectResult.get("taskId").toString() + String.valueOf(UUID.randomUUID()).replace("-", "");
                                            defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultArr[i]));
                                            defectMap.put("defectContent", resultArr[i]);
                                            defectMap.put("deviceId", cruiseResult.get("deviceId").toString());
                                            defectMap.put("instanceId", cruiseResult.get("instanceId").toString());
                                            TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(cruiseResult.get("instanceId").toString()));
                                            defectMap.put("customId", tStdDevicemete.getCustomId());
                                            defectMap.put("stdMeteId", tStdDevicemete.getDeviceMeteId().toString());
                                            defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未处理"));
                                            defectMap.put("imagePath", cruiseResult.get("picpath").toString());
                                            defectMap.put("alarmSource",analyseDataOperateService.selectDictCode("alarm_source","主辅设备"));
                                            defectMap.put("defectTime",defectTime);
                                            redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
                                        }

                                    }

                                } else {  //未产生缺陷
                                    tNormal++;
                                    cruiseResultMap.put("resultNum", "--");
                                    cruiseResultMap.put("cruiseResult", analyseDataOperateService.selectDictCode("cruise_result", "正常"));
                                    cruiseResultMap.put("cruiseAbnormal", "--");
                                }

                                break;
                        }
                    } catch (Exception e) {
                        log.error("告警/缺陷处理失败：" + e);
                    }

                    cruiseResultMap.put("cruiseStatus", analyseDataOperateService.selectDictCode("cruise_data_state", "已执行"));
                    if (Objects.isNull(cruiseResultMap.get("isWarn"))) {
                        cruiseResultMap.put("isWarn", "0");
                    }
                    cruiseResultMap.put("evaluationState", analyseDataOperateService.selectDictCode("evaluation_state", "未审核"));
                    // TODO: 2020/11/4 对算法识别结果进行判断并决定再redis中插入哪个值：identifyState- 识别正常&识别异常
                    // TODO: 2020/11/4 对实际结果进行判断并决定填入哪个初始值：identifyResult-正常&未采集图片&未识别图片&识别缺陷警告（加入IF判断）

                    redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);//修改redis

                    {
                        //巡视点结果上报站端
                        XMLBaseModel xmlBaseModel = new XMLBaseModel();
                        List<Map<String,Object>> xmlItems = new ArrayList<>();
                        Map<String,Object> xmlItem = new HashMap<>();
                        xmlBaseModel.setType("61");
                        xmlItem.put("patroldevice_code",cruiseResult.get("instanceId"));
                        xmlItem.put("task_name",cruiseResult.get("taskName"));
                        xmlItem.put("task_code",cruiseResult.get("taskCode"));
                        xmlItem.put("device_name",cruiseResult.get("instanceName"));
                        xmlItem.put("device_id",cruiseResult.get("instanceId"));
                        xmlItem.put("material_id",cruiseResult.get("realCode"));
                        xmlItem.put("value","");
                        xmlItem.put("value_unit",cruiseResultMap.get("resultNum"));
                        xmlItem.put("unit","");
                        xmlItem.put("time",simpleDateFormat.format(new Date()));
                        //todo
                        xmlItem.put("recognition_type","");
                        xmlItem.put("file_type","2");
                        xmlItem.put("file_path",cruiseResult.get("picpath"));
                        xmlItem.put("rectangle","");
                        xmlItem.put("task_patrolled_id",cruiseResult.get("taskId"));
                        xmlItem.put("data_type","0x01");
                        String valid = "";
                        if("--".equals(cruiseResultMap.get("resultNum")) || "null".equals(cruiseResultMap.get("resultNum"))){
                            valid = "0";
                        }else {
                            valid = "1";
                        }
                        xmlItem.put("valid",valid);

                        xmlItems.add(xmlItem);
                        xmlBaseModel.setItems(xmlItems);
                        List<XMLBaseModel> list = new ArrayList<>();
                        list.add(xmlBaseModel);
                        Map<String,List<XMLBaseModel>> map = new HashMap<>();
                        map.put("list",list);
                        log.info("结果信息上报：-"+map);
                        //Constant.otherServer(map,Constant.TCP_URL);
                    }

                    log.info("Border_______---------______________________________________________________________________________________________");
                    //若本任务上一次有巡视数据，则正常或异常点数要进行加和
                    if (redisTemplate.opsForHash().entries("taskConstant:" + TASKID).size() != 0) {
                        tNormal = Integer.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tNormal").toString()) + tNormal;
                        tAbnormal = Integer.valueOf(redisTemplate.opsForHash().entries("taskConstant:" + TASKID).get("tAbnormal").toString()) + tAbnormal;
                    }
                    taskConstant.put("tNormal", tNormal.toString());
                    taskConstant.put("tAbnormal", tAbnormal.toString());
                    redisTemplate.opsForHash().putAll("taskConstant:" + TASKID, taskConstant);//插入任务常量Map
                    log.info("任务常量配置完成");

//                NORMAL = NORMAL + 1;
                    // webSocket通知前端调用巡视监控的接口
                    Map<String, Object> jasonMap = new HashMap<>();
                    jasonMap.put("type", "finishedOneInstance");
                    jasonMap.put("taskId", cruiseResult.get("taskId").toString());
                    String json = JSON.toJSONString(jasonMap);
                    log.info("发送给前端的消息：" + json);
                    WebSocketServer.sendMsg(json);

                    //修改缓存中任务算法巡视点List
                    redisTemplate.opsForList().remove("analysisList:" + cruiseResult.get("taskId").toString(), 0, cruiseResult.get("instanceId").toString());
                    redisTemplate.opsForList().leftPush("analysisList:" + cruiseResult.get("taskId").toString(), "0");

                    log.info("RedisList修改成功");

                }

            } else if (jsonObject.get("msgType").toString().equals("6")) { //任务结束后发来的心跳信息
                String taskId = JSON.parseObject(jsonObject.get("msgData").toString()).get("taskId").toString();
                String instanceId = JSON.parseObject(jsonObject.get("msgData").toString()).get("instanceId").toString();
                redisTemplate.opsForList().leftPush("analysisList:" + taskId, "-1");
                log.info("心跳处理结束" + taskId);
                //处理心跳线程私有变量 taskId赋值
                TASKID = taskId;
            }

            if (redisTemplate.opsForList().index("analysisList:" + TASKID, 0).equals("-1")) {  //满足插库条件
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String finalCruiseKey = "instanceId";//最后修改任务表信息所需的redis KEY名
                log.info("任务结束-开始数据存储");
                //插库时从redis中取出正常、异常点数
                Map<String, Object> taskConstant = redisTemplate.opsForHash().entries("taskConstant:" + TASKID);
                Integer tAbnormal = Integer.valueOf(taskConstant.get("tAbnormal").toString());
                Integer tNormal = Integer.valueOf(taskConstant.get("tNormal").toString());
                log.info("正常点数：" + tNormal);
                log.info("异常点数：" + tAbnormal);
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
                        Map<String, Object> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
                        //TCTRD
                        log.info("TCTRD开始");
                        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                        tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                        tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId").toString());
                        tCruiseTaskResultDetail.setDeviceId(Long.valueOf(cruiseWorkedMap.get("deviceId").toString()));
                        tCruiseTaskResultDetail.setDeviceName(cruiseWorkedMap.get("deviceName").toString());
                        tCruiseTaskResultDetail.setInstanceId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                        tCruiseTaskResultDetail.setInstanceName(cruiseWorkedMap.get("instanceName").toString());
                        tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime").toString()));
                        tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime").toString()));
                        tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(cruiseWorkedMap.get("cruiseStatus").toString()));
                        tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark").toString());
                        log.info("TCDRD内容：" + tCruiseTaskResultDetail);
                        detailList.add(tCruiseTaskResultDetail);

                        //TCDR
                        log.info("TCRDR开始");
                        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                        tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                        tCruiseDataResult.setCruiseName(cruiseWorkedMap.get("cruiseName").toString());
                        tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
                        tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
                        tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
                        tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
                        tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
                        tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
                        tCruiseDataResult.setEvaluationState(Integer.valueOf(analyseDataOperateService.selectDictCode("evaluation_state", "未审核")));
                        tCruiseDataResult.setCruiseResult(Integer.valueOf(cruiseWorkedMap.get("cruiseResult").toString()));
                        if (cruiseWorkedMap.get("cruiseAbnormal").toString().equals("--")) {
                            tCruiseDataResult.setCruiseAbnormal(null);
                        } else {
                            tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(cruiseWorkedMap.get("cruiseAbnormal").toString()));
                        }
                        tCruiseDataResult.setEvaluationState(Integer.valueOf(cruiseWorkedMap.get("evaluationState").toString()));
                        tCruiseDataResult.setCreatetime(new Date());
                        tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                        tCruiseDataResult.setIsWarn(Integer.valueOf(cruiseWorkedMap.get("isWarn").toString()));
                        log.info("TCDR内容：" + tCruiseDataResult);
                        dataList.add(tCruiseDataResult);

                    }


                    Set<String> defectKey = redisScan("defectInfo:" + TASKID);
                    for (String key : defectKey) {
                        Map<String, Object> defectMap = redisTemplate.opsForHash().entries(key);
                        log.info("TDI开始");
                        TDefectInfo tDefectInfo = new TDefectInfo();
                        tDefectInfo.setDefectLevel(Integer.valueOf(defectMap.get("defectLevel").toString()));
                        tDefectInfo.setDefectTime(simpleDateFormat.parse(defectMap.get("defectTime").toString()));//缺陷识别时间
                        tDefectInfo.setDefectType(Integer.valueOf(defectMap.get("defectType").toString()));
                        tDefectInfo.setDefectContent(defectMap.get("defectContent").toString());
                        tDefectInfo.setDeviceId(Long.valueOf(defectMap.get("deviceId").toString()));
                        tDefectInfo.setInstanceId(Long.valueOf(defectMap.get("instanceId").toString()));
                        tDefectInfo.setCunstomId(defectMap.get("customId").toString());
                        tDefectInfo.setStdMeteId(Long.valueOf(defectMap.get("stdMeteId").toString()));
                        tDefectInfo.setConfMode(Integer.valueOf(defectMap.get("confMode").toString()));
                        tDefectInfo.setImagePath(defectMap.get("imagePath").toString());
                        tDefectInfo.setAlarmSource(Integer.valueOf(defectMap.get("alarmSource").toString()));
                        defectList.add(tDefectInfo);
                    }

                    //批量插入两表

                    log.info("两表开始插入");
                    analyseDataOperateService.batchInsertCruiseTaskResultDetail(detailList);
                    analyseDataOperateService.batchInsertCruiseDataResult(dataList);
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
                    if (defectList.size() > 0) {
                        analyseDataOperateService.batchInsertDefectInfo(defectList);
                    }
                    log.info("两表结束插入");
                    cruiseKeys.clear();
                } catch (Exception e) {
                    log.error("插表错误" + e);
                }

                // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
                Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries(finalCruiseKey);
                String strForCountAbnormal = "countForAbnormal:" + TASKID;
                Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
                Integer total = Integer.valueOf(abnormalCount.get("all").toString());
                Integer abnormal = Integer.valueOf(abnormalCount.get("abnormal").toString());
                Integer normal = Integer.valueOf(abnormalCount.get("normal").toString());
                log.info("All：" + total);
                //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
                tAbnormal = tAbnormal + abnormal;
                tNormal = tNormal + normal;
                if (tAbnormal + tNormal == total) {
                    //TCTR开始

                    Thread.sleep(15000);

                    try {
                        log.info("TCTR开始");
                        TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                        tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
                        tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
                        tCruiseTaskResult.setTaskName(cruiseResult.get("taskName").toString());
                        tCruiseTaskResult.setTaskAbnormal(tAbnormal);
                        tCruiseTaskResult.setTaskAlarm(0);
                        tCruiseTaskResult.setRunExecute(cruiseResult.get("if_run").toString());
//                                tCruiseTaskResult.setCruiseTaskTime();
                        analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);


                        //TCR开始
                        log.info("TCR开始");
                        TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId").toString());
                        tCruiseResult.setCState(Integer.valueOf(analyseDataOperateService.selectDictCode("task_state", "执行完成").toString()));
                        tCruiseResult.setTaskWait(0);
                        analyseDataOperateService.updateCruiseResult(tCruiseResult);

                        // webSocket通知前端调用巡视监控的接口（任务完成）
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "lastOneInstance");
                        jasonMap.put("taskId", cruiseResult.get("taskId").toString());
                        String json = JSON.toJSONString(jasonMap);
                        log.info("发送给前端的消息：" + json);
                        WebSocketServer.sendMsg(json);

                        //任务执行完成 删除当前任务的缓存
                        redisTemplate.delete("taskConstant:" + TASKID);
                    } catch (Exception e) {
                        log.error("任务信息修改失败" + e);
                    }

                } else {       // 修改异常点数缓存
                    log.info("不满足修改任务条件，对正常异常点数进行修改...............");
                    redisTemplate.delete("taskConstant:" + TASKID);//清楚当前-1阶段所有点数
                    Map<String, String> mapForAbnormal = new HashMap<>();
                    mapForAbnormal.put("abnormal", tAbnormal.toString());
                    mapForAbnormal.put("normal", tNormal.toString());
                    redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
                    log.info("修改后的abnormal：" + tAbnormal);
                    log.info("修改后的normal：" + tNormal);
                }

            }


        }
    }

}



