package com.yjh.accessvideo.netty.client;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.utils.ByteUtil;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import com.yjh.accessvideo.thread.TaskExecutePool;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessvideo.common.Constant.TYPET3;
import static com.yjh.accessvideo.common.Constant.TASKID;
import static com.yjh.accessvideo.common.Constant.INSTANCEID;



/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientHandler extends ChannelInboundHandlerAdapter {

    private Logger log = LoggerFactory.getLogger(AnalysisClientHandler.class);

    private long hisT3 = System.currentTimeMillis();
    private long T3 = 20000;
    public boolean isThreadStart;
    private ChannelHandlerContext ctx;

    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    private String syncWebsocketUrl;

    public AnalysisClientHandler(RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService,String syncWebsocketUrl) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
        this.syncWebsocketUrl=syncWebsocketUrl;
    }

    private static Map<Integer, AnalysisClientHandler> analysisClientHandlerHashMap = new HashMap<>();

    public static Map<Integer, AnalysisClientHandler> getAnalysisClientHandlerHashMap() {
        return analysisClientHandlerHashMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        sendStartRegister(ctx);
        Thread.sleep(3000);
        this.ctx = ctx;
        isThreadStart = true;
        //启动心跳检测
//        HeartBeatThread heartBeatThread = new HeartBeatThread(this, true);
//        Thread thread = new Thread(heartBeatThread);
//        thread.setDaemon(true);
//        thread.start();
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        if (analysisClientHandlerHashMap.get(remotePort) == null) {
            analysisClientHandlerHashMap.put(remotePort, this);
        }
        log.info("客户端注册成功: " + ctx.channel().remoteAddress());
        log.info("analysisClientHandlerHashMap: " + analysisClientHandlerHashMap);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,服务端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        isThreadStart = false;
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        analysisClientHandlerHashMap.remove(remotePort);
        log.error("服务端主动断开连接！");
        log.info("analysisClientHandlerHashMap: " + analysisClientHandlerHashMap);
        String serverUrl = remoteAdds.substring(0, remoteAdds.indexOf(":"));
        log.info("serverUrl: " + serverUrl.substring(1));
        InetSocketAddress remoteAddress = new InetSocketAddress(serverUrl.substring(1), remotePort);
        //使用过程中断线重连
        if (Objects.nonNull(Constant.bootstrapHashMap.get(1))) {
            doConnect(remoteAddress, Constant.bootstrapHashMap.get(1));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        try {
            String body = new String(bytes, "UTF-8");
            log.info("接收服务端数据:" + body);
            String usefulBody = analyseDataOperateService.nonUnpacking(body);//反拆包解析
            if(usefulBody !="") {
                //线程池数据处理
                DataDealThread dataDealThread = new DataDealThread(usefulBody, redisTemplate, analyseDataOperateService, ctx,syncWebsocketUrl);
                TaskExecutePool.getInstance().execute(dataDealThread);
//            handlerData(body); //单线程数据处理
            }
        } catch (Exception e) {
            e.getMessage();
        }

        ReferenceCountUtil.release(byteBuf);
    }

//    private void handlerData(String body) throws ParseException, InterruptedException {
//          analyseDataOperateService.nonUnpacking(body);
//    }
//        //TODO 添加线程池
//        String remoteAdds = ctx.channel().remoteAddress().toString();
//        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));//Port:13668-表计识别,Port:13669-缺陷识别
//        String usefulBody=body.replaceAll("\\{.*?\\}\\{","{");
//        JSONObject jsonObject = JSON.parseObject(usefulBody);
//        log.info("JSON对象1：" + jsonObject);
//        if (jsonObject.get("msgType").toString().equals("2")) {
//            JSONObject jsonObjectData = JSON.parseObject(JSON.parseObject(jsonObject.get("msgData").toString()).get("data").toString()); //全量数据结果集
//            log.info("原生数据****：" + jsonObjectData);
//            Iterator iterator = jsonObjectData.entrySet().iterator();//迭代器取出data中的每一个resultInfo
//            while (iterator.hasNext()) {
//                Map.Entry entry = (Map.Entry) iterator.next();
//                //遍历每一个结果子集
//                JSONObject jsonObjectResult = JSON.parseObject(entry.getValue().toString());
//                log.info("数据****：" + jsonObjectResult);//打印resultInfo
//                //初始化TASKID和INSTANCEID
//            }
//        }
//    }
//
//                TASKID = jsonObjectResult.get("taskId").toString();
//                INSTANCEID = jsonObjectResult.get("instanceId").toString();
//                String name = "t_cruise_task_result:" + TASKID + INSTANCEID;
//                log.info("keyName" + name);
//                cruiseKeys.add(name);
//
//
//                log.info("数据初始化");
////                    String analyseType=jsonObjectResult.get("analyseType").toString();
////                    log.info(analyseType);
////                    String taskId=jsonObjectResult.get("taskId").toString();
////                    log.info(taskId);
////                    String resultValue1=jsonObjectResult.get("resultValue").toString();
////                    log.info(resultValue1);
////                    String instanceId=jsonObjectResult.get("instanceId").toString();
////                    log.info(instanceId);
//
//                //redis数据键名由taskId+instanceId命名
//                log.info("数据Redis业务开启");
//                String redisName = jsonObjectResult.get("taskId").toString() + jsonObjectResult.get("instanceId").toString();
//                log.info("template:" + redisTemplate);
//                log.info("redisName:" + redisName);
//                Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + redisName);//读redis
//                log.info("读取到的redis：" + cruiseResult);
//                Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map
//
//
//                log.info("端口号：" + remotePort);
//                switch (remotePort) {
//                    case 13668:
//                        if (jsonObjectResult.get("resultValue").equals("NULL_Model")) {
//                            cruiseResultMap.put("resultNum", "缺少标定文件");
//                            cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "未识别"));
//                            ABNORMAL = ABNORMAL + 1;
//                        } else if (jsonObjectResult.get("resultValue").equals("识别失败")) {
//                            cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
//                            cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "异常"));
//                            ABNORMAL = ABNORMAL + 1;
//                        } else {
//                            if (jsonObjectResult.get("resultValue").toString().matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]")) {
//                                cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
//                                cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "正常"));
//                                NORMAL = NORMAL + 1;
//
//                                log.info("开始告警预处理");
//                                TStdDevicemete tStdDevicemeteM = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
//                                //单个数值表计识别结果的告警判断与处理
//                                TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
//                                log.info("数据查询初始化");
//                                //满足告警数据结构-进行告警判断
//                                if (Objects.isNull(tStdDevicemeteM.getAlarmState()) && Objects.nonNull(tStdDevicemeteM.getHighLimit1()) || Objects.isNull(tStdDevicemeteM.getAlarmState()) && Objects.isNull(tStdDevicemeteM.getHighLimit1())) {
//                                    //初始化告警信息redis表
//                                    String warnName = "warnInfo:" + TASKID + String.valueOf(UUID.randomUUID()).replace("-", "");
//                                    Map<String, String> warnMap = new HashMap<>();
//                                    warnMap.put("warnLevel", tStdDevicemeteM.getAlarmLevel().toString());
//                                    warnMap.put("warnType", tStdDevicemeteM.getAlarmType());
//                                    warnMap.put("deviceId", tCruisePointInstance.getDeviceId().toString());
//                                    warnMap.put("customId", tCruisePointInstance.getCustomId());
//                                    warnMap.put("instanceId", jsonObjectResult.get("instanceId").toString());
//                                    warnMap.put("stdMeteId", tCruisePointInstance.getDeviceMeteId().toString());
//                                    warnMap.put("taskId", jsonObjectResult.get("taskId").toString());
//                                    warnMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未处理"));
//                                    warnMap.put("ifWarnDisable", String.valueOf(0));
//                                    warnMap.put("value", jsonObjectResult.get("resultValue").toString());
//                                    warnMap.put("imagePath", cruiseResult.get("picpath").toString());
//                                    warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
//                                    log.info("开始告警判断");
//                                    log.info("测点种类:" + tStdDevicemeteM.getMeteKind());
//                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                    switch (tStdDevicemeteM.getMeteKind()) {
//                                        case "1":
//                                            String resultValue = jsonObjectResult.get("resultValue").toString();
//                                            int warnRuleTeleSigning = analyseDataOperateService.warnJudgementTelesignaling(resultValue,
//                                                    tStdDevicemeteM.getStateZero(),
//                                                    tStdDevicemeteM.getStateOne(),
//                                                    tStdDevicemeteM.getAlarmState()
//                                            );
//                                            log.info("告警结果：" + warnRuleTeleSigning);
//                                            if (warnRuleTeleSigning == 1) {
//                                                log.info("告警信息生成");
//                                                warnMap.put("warnName", tStdDevicemeteM.getMeteName() + "状态异常");
//                                                warnMap.put("warnTime", simpleDateFormat.format(new Date()));
//                                                if (tStdDevicemeteM.getAlarmState() == 0) {
//                                                    warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + tStdDevicemeteM.getStateZero() + "状态触发告警");
//                                                } else {
//                                                    warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + tStdDevicemeteM.getStateOne() + "状态触发告警");
//                                                }
//                                                warnMap.put("outRang", "");
//
//                                                log.info("告警MAP：" + warnMap);
//                                                try {
//                                                    redisTemplate.opsForHash().putAll(warnName, warnMap);
//                                                    cruiseResultMap.put("isWarn", "1");
//                                                    // webSocket通知前端调用巡视监控的接口
//                                                    Map<String, Object> jasonMap = new HashMap<>();
//                                                    jasonMap.put("type", "newAlarm");
//                                                    jasonMap.put("alarmName", warnMap.get("warnName"));
//                                                    jasonMap.put("alarmTime", warnMap.get("warnTime"));
//                                                    jasonMap.put("alarmContent", warnMap.get("warnContent"));
//                                                    String json = JSON.toJSONString(jasonMap);
//                                                    log.info("发送给前端的消息：" + json);
//                                                    WebSocketServer.sendMsg(json);
//                                                } catch (Exception e) {
//                                                    log.info("生成错误", e);
//                                                }
//
//                                            }
//
//                                            break;
//                                        case "2":
//                                            Float resultValueMeter = Float.valueOf(jsonObjectResult.get("resultValue").toString());
//                                            int warnRuleMeter = analyseDataOperateService.warnJudgement(resultValueMeter,
//                                                    tStdDevicemeteM.getHighLimit1(),
//                                                    tStdDevicemeteM.getLowLimit1(),
//                                                    tStdDevicemeteM.getHighLimit2(),
//                                                    tStdDevicemeteM.getLowLimit2());
//                                            if (warnRuleMeter > 0) {
//                                                warnMap.put("warnName", tStdDevicemeteM.getMeteName() + "数据异常");
//                                                warnMap.put("warnTime", simpleDateFormat.format(new Date()));
//                                                log.info("数字结果告警判断结果：" + warnRuleMeter);
//                                                switch (warnRuleMeter) {
//                                                    case 1:
//                                                        warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + "过高");
//                                                        warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit1()));
//                                                        break;
//                                                    case 2:
//                                                        warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + "过低");
//                                                        warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit1() - resultValueMeter));
//                                                        break;
//                                                    case 3:
//                                                        warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + "超高");
//                                                        warnMap.put("outRange", String.valueOf(resultValueMeter - tStdDevicemeteM.getHighLimit2()));
//                                                        break;
//
//                                                    case 4:
//                                                        warnMap.put("warnContent", "主设备告警:" + tStdDevicemeteM.getMeteName() + "超低");
//                                                        warnMap.put("outRange", String.valueOf(tStdDevicemeteM.getLowLimit2() - resultValueMeter));
//                                                        break;
//                                                }
//
//                                                redisTemplate.opsForHash().putAll(warnName, warnMap);
//                                                cruiseResultMap.put("isWarn", "1");
//                                                log.info("告警Map:" + warnMap);
//
//                                                // webSocket通知前端调用巡视监控的接口
//                                                Map<String, Object> jasonMaps = new HashMap<>();
//                                                jasonMaps.put("type", "newAlarm");
//                                                jasonMaps.put("alarmName", warnMap.get("warnName"));
//                                                jasonMaps.put("alarmTime", warnMap.get("warnTime"));
//                                                jasonMaps.put("alarmContent", warnMap.get("warnContent"));
//                                                String jsons = JSON.toJSONString(jasonMaps);
//                                                log.info("发送给前端的消息：" + jsons);
//                                                WebSocketServer.sendMsg(jsons);
//                                            }
//                                            break;
//                                        default:
//                                            break;
//                                    }
//                                }
//                            } else {
//                                cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
//                                cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "数据异常"));
//                                ABNORMAL = ABNORMAL + 1;
//                            }
//                        }
//
//                        break;
//                    case 13669:
//                        String originResult = jsonObjectResult.get("resultValue").toString();
//                        String resultValue = analyseDataOperateService.resolveDefectResult(jsonObjectResult.get("resultValue").toString());
//                        if (resultValue != "null") {
//                            NORMAL = NORMAL + 1;
//                            cruiseResultMap.put("resultNum", resultValue);
//                            cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "正常"));
//                            //生成缺陷缓存信息（单一缺陷和多元缺陷）
//                            log.info("--------____--------生成缺陷缓存");
//                            String[] resultArr = resultValue.split("\\s+");
//                            if (resultArr.length == 1) {
//                                log.info("--------____--------单一缺陷");
//                                Map<String, String> defectMap = new HashMap<>();
//                                String defectRedisName = "defectInfo:" + jsonObjectResult.get("taskId").toString() + String.valueOf(UUID.randomUUID()).replace("-", "");
//                                defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("defect_level", "一般"));
//                                defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_type", resultValue));
//                                defectMap.put("defectContent", resultValue);
//                                defectMap.put("deviceId", cruiseResult.get("deviceId").toString());
//                                defectMap.put("instanceId", cruiseResult.get("instanceId").toString());
//                                TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(cruiseResult.get("instanceId").toString()));
//                                defectMap.put("customId", tStdDevicemete.getCustomId());
//                                defectMap.put("stdMeteId", tStdDevicemete.getDeviceMeteId().toString());
//                                defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未处理"));
//                                defectMap.put("imagePath", cruiseResult.get("picpath").toString());
//                                redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
//                            } else if (resultArr.length > 1) {
//                                log.info("--------____--------多元缺陷");
//                                for (int i = 0; i < resultArr.length; i++) {
//                                    log.info("缺陷处理方法：" + resultArr[i]);
//                                    Map<String, String> defectMap = new HashMap<>();
//                                    String defectRedisName = "defectInfo:" + jsonObjectResult.get("taskId").toString() + String.valueOf(UUID.randomUUID()).replace("-", "");
//                                    defectMap.put("defectLevel", analyseDataOperateService.selectDictCode("defect_level", "一般"));
//                                    defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_type", resultArr[i]));
//                                    defectMap.put("defectContent", resultArr[i]);
//                                    defectMap.put("deviceId", cruiseResult.get("deviceId").toString());
//                                    defectMap.put("instanceId", cruiseResult.get("instanceId").toString());
//                                    TStdDevicemete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(cruiseResult.get("instanceId").toString()));
//                                    defectMap.put("customId", tStdDevicemete.getCustomId());
//                                    defectMap.put("stdMeteId", tStdDevicemete.getDeviceMeteId().toString());
//                                    defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未处理"));
//                                    defectMap.put("imagePath", cruiseResult.get("picpath").toString());
//                                    redisTemplate.opsForHash().putAll(defectRedisName, defectMap);
//                                }
//
//                            }
//
//                        } else {
//                            cruiseResultMap.put("resultNum", "--");
//                            cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "未识别"));
//                            ABNORMAL = ABNORMAL + 1;
//                        }
//
//                        break;
//                }
//
//                cruiseResultMap.put("cruiseStatus", analyseDataOperateService.selectDictCode("cruise_data_state", "已执行"));
//                if (Objects.isNull(cruiseResultMap.get("isWarn"))) {
//                    cruiseResultMap.put("isWarn", "0");
//                }
//                // TODO: 2020/11/4 对算法识别结果进行判断并决定再redis中插入哪个值：identifyState- 识别正常&识别异常
//                // TODO: 2020/11/4 对实际结果进行判断并决定填入哪个初始值：identifyResult-正常&未采集图片&未识别图片&识别缺陷警告（加入IF判断）
//
//                redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);//修改redis
////                NORMAL = NORMAL + 1;
//                // webSocket通知前端调用巡视监控的接口
//                Map<String, Object> jasonMap = new HashMap<>();
//                jasonMap.put("type", "finishedOneInstance");
//                jasonMap.put("taskId", cruiseResult.get("taskId").toString());
//                String json = JSON.toJSONString(jasonMap);
//                log.info("发送给前端的消息：" + json);
//                WebSocketServer.sendMsg(json);
//
//                //修改缓存中任务算法巡视点List
//                redisTemplate.opsForList().remove("analysisList:" + cruiseResult.get("taskId").toString(), 0, cruiseResult.get("instanceId").toString());
//                redisTemplate.opsForList().leftPush("analysisList:" + cruiseResult.get("taskId").toString(), "0");
//
//                log.info("RedisList修改成功");
//
//
//            }
//
//        } else if (jsonObject.get("msgType").toString().equals("6")) { //任务结束后发来的心跳信息
//            String taskId = JSON.parseObject(jsonObject.get("msgData").toString()).get("taskId").toString();
//            redisTemplate.opsForList().leftPush("analysisList:" + taskId, "-1");
//            log.info("心跳处理结束" + taskId);
//        }
//
//        if (redisTemplate.opsForList().index("analysisList:" + TASKID, 0).equals("-1") && redisTemplate.opsForList().index("analysisList:" + TASKID, 1).equals("0")) {  //满足插库条件
//            log.info("任务结束-开始数据存储");
//            //满足条件先插巡视点数据
//            List<TCruiseTaskResultDetail> detailList = new ArrayList<>();//TCTRD List对象
//            List<TCruiseDataResult> dataList = new ArrayList<>();//TCDR List对象
//            List<TDefectInfo> defectList = new ArrayList<>();//TDI List对象
//            List<TWarnInfo> warnList = new ArrayList<>();//IWI List对象
//            Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + TASKID);
//            log.info("cruisekeys:" + cruiseKeys);
//            for (String cruiseKey : cruiseKeys) {
//                Map<String, Object> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
//                //TCTRD
//                log.info("TCTRD开始");
//                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
//                tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
//                tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId").toString());
//                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(cruiseWorkedMap.get("deviceId").toString()));
//                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime").toString()));
//                tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime").toString()));
//                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(cruiseWorkedMap.get("cruiseStatus").toString()));
//                tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark").toString());
//                detailList.add(tCruiseTaskResultDetail);
//
//                //TCDR
//                log.info("TCRD开始");
//                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
//                tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
//                tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
//                tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
//                tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
//                tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
//                tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
//                tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
//                tCruiseDataResult.setEvaluationState(Integer.valueOf(analyseDataOperateService.selectDictCode("evaluation_state", "未审核")));
//                tCruiseDataResult.setState(Integer.valueOf(cruiseWorkedMap.get("state").toString()));
//                tCruiseDataResult.setCreatetime(new Date());
//                tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
//                tCruiseDataResult.setIsWarn(Integer.valueOf(cruiseWorkedMap.get("isWarn").toString()));
//                dataList.add(tCruiseDataResult);
//
//            }
//
//            try {
//                Set<String> warnKey = redisScan("warnInfo:" + TASKID);
//                for (String key : warnKey) {
//                    Map<String, Object> warnMap = redisTemplate.opsForHash().entries(key);
//                    log.info("TWI开始");
//                    TWarnInfo tWarnInfo = new TWarnInfo();
//                    tWarnInfo.setWarnLevel(Integer.valueOf(warnMap.get("warnLevel").toString()));
//                    tWarnInfo.setWarnType(Integer.valueOf(warnMap.get("warnType").toString()));
//                    tWarnInfo.setDeviceId(Long.valueOf(warnMap.get("deviceId").toString()));
//                    tWarnInfo.setCunstomId(warnMap.get("customId").toString());
//                    tWarnInfo.setInstanceId(Long.valueOf(warnMap.get("instanceId").toString()));
//                    tWarnInfo.setStdMeteId(Long.valueOf(warnMap.get("stdMeteId").toString()));
//                    tWarnInfo.setTaskId(warnMap.get("taskId").toString());
//                    tWarnInfo.setConfMode(Integer.valueOf(warnMap.get("confMode").toString()));
//                    tWarnInfo.setIfWarnDisable(Integer.valueOf(warnMap.get("ifWarnDisable").toString()));
//                    tWarnInfo.setValue(warnMap.get("value").toString());
//                    tWarnInfo.setImagePath(warnMap.get("imagePath").toString());
//                    tWarnInfo.setAlarmSource(Integer.valueOf(warnMap.get("alarmSource").toString()));
//                    tWarnInfo.setWarnName(warnMap.get("warnName").toString());
//                    tWarnInfo.setOutRange(warnMap.get("outRange").toString());
//                    tWarnInfo.setWarnContent(warnMap.get("warnContent").toString());
//                    tWarnInfo.setWarnTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(warnMap.get("warnTime").toString()));
//                    log.info("告警数据对象：" + tWarnInfo);
//                    warnList.add(tWarnInfo);
//                }
//            } catch (Exception e) {
//                log.error("告警信息入库失败" + e);
//            }
//
//
//            Set<String> defectKey = redisScan("defectInfo:" + TASKID);
//            for (String key : defectKey) {
//                Map<String, Object> defectMap = redisTemplate.opsForHash().entries(key);
//                log.info("TDI开始");
//                TDefectInfo tDefectInfo = new TDefectInfo();
//                tDefectInfo.setDefectLevel(Integer.valueOf(defectMap.get("defectLevel").toString()));
//                tDefectInfo.setDefectTime(new Date());
//                tDefectInfo.setDefectType(Integer.valueOf(defectMap.get("defectType").toString()));
//                tDefectInfo.setDefectContent(defectMap.get("defectContent").toString());
//                tDefectInfo.setDeviceId(Long.valueOf(defectMap.get("deviceId").toString()));
//                tDefectInfo.setInstanceId(Long.valueOf(defectMap.get("instanceId").toString()));
//                tDefectInfo.setCunstomId(defectMap.get("customId").toString());
//                tDefectInfo.setStdMeteId(Long.valueOf(defectMap.get("stdMeteId").toString()));
//                tDefectInfo.setConfMode(Integer.valueOf(defectMap.get("confMode").toString()));
//                tDefectInfo.setImagePath(defectMap.get("imagePath").toString());
//                defectList.add(tDefectInfo);
//            }
//
//            //批量插入两表
//
//            log.info("两表开始插入");
//            analyseDataOperateService.batchInsertCruiseTaskResultDetail(detailList);
//            analyseDataOperateService.batchInsertCruiseDataResult(dataList);
//            if (warnList.size() > 0) {
//                analyseDataOperateService.batchInsertWarnInfo(warnList);
//                //清空redis中的告警信息
//                Set<String> warnKeys = redisScan("warnInfo:");
//                for (String key : warnKeys) {
//                    redisTemplate.delete(key);
//                }
//            }
//            if (defectList.size() > 0) {
//                analyseDataOperateService.batchInsertDefectInfo(defectList);
//            }
//            log.info("两表结束插入");
//            cruiseKeys.clear();
//
//
//            // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
//            log.info("abnormal:" + ABNORMAL);
//            log.info("normal:" + NORMAL);
//            Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + TASKID + INSTANCEID);
//            String strForCountAbnormal = "countForAbnormal:" + TASKID;
//            Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
//            Integer total = Integer.valueOf(abnormalCount.get("all").toString());
//            Integer abnormal = Integer.valueOf(abnormalCount.get("abnormal").toString());
//            Integer normal = Integer.valueOf(abnormalCount.get("normal").toString());
//            //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
//            if (abnormal + ABNORMAL + normal + NORMAL == total) {
//                //TCTR开始
//
//                Thread.sleep(15000);
//
//                log.info("TCTR开始");
//                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
//                tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
//                tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
//                tCruiseTaskResult.setTaskAbnormal(abnormal + ABNORMAL);
//                tCruiseTaskResult.setTaskAlarm(0);
//                tCruiseTaskResult.setRunExecute(cruiseResult.get("if_run").toString());
////                                tCruiseTaskResult.setCruiseTaskTime();
//                analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);
//
//
//                //TCR开始
//                log.info("TCR开始");
//                TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId").toString());
//                tCruiseResult.setCState(Integer.valueOf(analyseDataOperateService.selectDictCode("task_state", "执行完成").toString()));
//                tCruiseResult.setTaskWait(0);
//                analyseDataOperateService.updateCruiseResult(tCruiseResult);
//
//                // webSocket通知前端调用巡视监控的接口（任务完成）
//                Map<String, Object> jasonMap = new HashMap<>();
//                jasonMap.put("type", "lastOneInstance");
//                jasonMap.put("taskId", cruiseResult.get("taskId").toString());
//                String json = JSON.toJSONString(jasonMap);
//                log.info("发送给前端的消息：" + json);
//                WebSocketServer.sendMsg(json);
//
//            } else {       // 修改异常点数缓存
//                Map<String, String> mapForAbnormal = new HashMap<>();
//                Integer totAbnormal = abnormal + ABNORMAL;
//                Integer totNormal = normal + NORMAL;
//                mapForAbnormal.put("abnormal", totAbnormal.toString());
//                mapForAbnormal.put("normal", totNormal.toString());
//                redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
//            }
//            ABNORMAL = 0;
//            NORMAL = 0;
//
//        }
//
//
//    }

    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            if (isTimeout(TYPET3, hisT3, false)) {
//                SendHeartBeat();
                hisT3 = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isTimeout(byte type, long value, boolean set) {
        if (type == TYPET3) {
            //log.info("commandSend:68 04 43 00 00 00 "+System.currentTimeMillis() + ":" + value +":" +(System.currentTimeMillis() - value));
            if ((System.currentTimeMillis() - value) > T3) {
                if (set) hisT3 = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

    public void sendString(ChannelHandlerContext ctx, String msg) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(msg.getBytes());
        log.info("客户端发送报文:" + msg);
        ctx.channel().writeAndFlush(byteBuf);
        log.info("客户端发送报文成功！");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    private void sendStartRegister(ChannelHandlerContext ctx) {
        // 发送json字符串
        String registerMsg = "{\n\"msgType\": \"3\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\",\n\"registerKey\": \"yijiahe\"\n}\n}\n";
        sendString(ctx, registerMsg);
//        String fakeData="";
//        sendString(ctx,fakeData);
        // TODO: 2020/10/22
        //  String registerMsg = "发送假数据";
        //        sendString(ctx, registerMsg);
    }

    public void SendHeartBeat(Analysis analysis) throws InterruptedException {
        log.info("analysis-INFO-----:"+analysis);
        String instanceId = analysis.getInstanceId().toString();
        String taskId = analysis.getTaskId();
        Thread.sleep(3000);
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"instanceId\": " + "\"" + instanceId + "\", \n\"taskId\": \"" + taskId + "\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    //发送请求数据
    public void sendDataReguest(JSONObject analysisObject) throws InterruptedException {
        String msg = analysisObject.toString();
        Thread.sleep(3000);//延迟发送数据
        sendString(ctx, msg);
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

    //重新连接tcp服务端
    private void doConnect(InetSocketAddress remoteAddress, Bootstrap bootstrap) {
        try {
            if (bootstrap != null) {
                bootstrap.remoteAddress(remoteAddress);
                ChannelFuture f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        //重连
                        log.info("与服务端" + remoteAddress + "连接失败!主动尝试重连!");
                        eventLoop.schedule(() -> doConnect(remoteAddress, bootstrap), 60, TimeUnit.SECONDS);
                    }
                });
            }
        } catch (Exception e) {
            log.info("------主动连接服务端连接失败------" + e.getMessage());
        }
    }

}
