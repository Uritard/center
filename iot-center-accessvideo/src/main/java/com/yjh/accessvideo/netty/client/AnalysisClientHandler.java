package com.yjh.accessvideo.netty.client;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessvideo.common.websocket.WebSocketServer;
import com.yjh.accessvideo.commons.utils.ByteUtil;
import com.yjh.accessvideo.commons.utils.StaticContextAccessor;
import com.yjh.accessvideo.module.device.entity.*;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import com.yjh.accessvideo.module.device.service.AnalysisService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.yjh.accessvideo.common.Constant.TYPET3;
import static com.yjh.accessvideo.common.Constant.instanceIds;

/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientHandler extends ChannelInboundHandlerAdapter {

    private Logger log = LoggerFactory.getLogger(AnalysisClientHandler.class);

    private long hisT3 = System.currentTimeMillis();
    private long T3 = 20000;
    public boolean isThreadStart;
    private ChannelHandlerContext ctx;

    private  RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    public AnalysisClientHandler (RedisTemplate redisTemplate,AnalyseDataOperateService analyseDataOperateService) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService=analyseDataOperateService;
    }

    private static Map<Integer, AnalysisClientHandler> analysisClientHandlerHashMap = new HashMap<>();
    public static Map<Integer, AnalysisClientHandler> getAnalysisClientHandlerHashMap() { return analysisClientHandlerHashMap; }

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
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":")+1));
        if (analysisClientHandlerHashMap.get(remotePort) == null) { analysisClientHandlerHashMap.put(remotePort, this); }
        log.info("客户端注册成功: "+ctx.channel().remoteAddress());
        log.info("analysisClientHandlerHashMap: "+analysisClientHandlerHashMap);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,服务端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        isThreadStart = false;
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":")+1));
        analysisClientHandlerHashMap.remove(remotePort);
        log.error("服务端主动断开连接！");
        log.info("analysisClientHandlerHashMap: "+analysisClientHandlerHashMap);

        //使用过程中断线重连
//        (ChannelFuture futureListener) -> {
//            log.info("主动连接服务端"+ctx.channel().remoteAddress());
//            final EventLoop eventLoop = futureListener.channel().eventLoop();
//            if (!futureListener.isSuccess()) {
//                log.info("和服务端"+ctx.channel().remoteAddress()+"连接失败!");
//                //10秒后重连
//                eventLoop.schedule(() -> doConnect(bootstrap, ctx.channel().remoteAddress()), 60, TimeUnit.SECONDS);
//            } else { log.info("与"+ctx.channel().remoteAddress()+"连接成功!"); }
//        };
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        try {
            String body = new String(bytes, "UTF-8");
            log.info("接收服务端数据:"+body);
            handlerData(body);
        } catch (Exception e) { e.getMessage(); }

        ReferenceCountUtil.release(byteBuf);
    }

    private void handlerData(String body) throws ParseException {
        //TODO 具体转化逻辑
        JSONObject jsonObject=JSON.parseObject(body);
        log.info("JSON对象1："+jsonObject);
        switch (jsonObject.get("msgType").toString()){
            case "2":
                JSONObject jsonObjectData=JSON.parseObject(JSON.parseObject(jsonObject.get("msgData").toString()).get("data").toString()); //全量数据结果集
                log.info("原生数据****："+jsonObjectData);
                Iterator iterator=jsonObjectData.entrySet().iterator();//迭代器取出data中的每一个resultInfo
                while (iterator.hasNext()){
                    Map.Entry entry=(Map.Entry)iterator.next();
                    //遍历每一个结果子集
                    JSONObject jsonObjectResult=JSON.parseObject(entry.getValue().toString());
                    log.info("数据****："+jsonObjectResult);//打印resultInfo
//                    String analyseType=jsonObjectResult.get("analyseType").toString();
//                    log.info(analyseType);
//                    String taskId=jsonObjectResult.get("taskId").toString();
//                    log.info(taskId);
//                    String resultValue1=jsonObjectResult.get("resultValue").toString();
//                    log.info(resultValue1);
//                    String instanceId=jsonObjectResult.get("instanceId").toString();
//                    log.info(instanceId);

                    //redis数据键名由taskId+instanceId命名
                    // TODO: 2020/10/25 下任务时插redis库名需改
                    String redisName=jsonObjectResult.get("taskId").toString()+jsonObjectResult.get("instanceId").toString();
                    log.info("template:"+ redisTemplate);
                    log.info("redisName:"+redisName);
                    Map<String,Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:"+redisName);//读redis
                    log.info("读取到的redis："+cruiseResult);
                    Map<String,String> cruiseResultMap=new HashMap<>();//修改redis的巡检点结果map
                    cruiseResultMap.put("resultNum",jsonObjectResult.get("resultValue").toString());
                    cruiseResultMap.put("state",analyseDataOperateService.selectDictCode("data_state","正常"));
                    cruiseResultMap.put("cruiseStatus",analyseDataOperateService.selectDictCode("cruise_data_state","已执行"));
                    redisTemplate.opsForHash().putAll("t_cruise_task_result:"+redisName,cruiseResultMap);//修改redis
                    // webSocket通知前端调用巡视监控的接口
                    Map<String,Object> jasonMap=new HashMap<>();
                    jasonMap.put("type","newTask");
                    jasonMap.put("taskId",cruiseResult.get("taskId").toString());
                    String json=JSON.toJSONString(jasonMap);
                    log.info("发送给前端的消息："+json);
                    WebSocketServer.sendMsg(json);


                    

                    //判断该任务下的巡视点是否均已执行完成

                    //当前任务下所有的巡视点
                    if(instanceIds.size()==0){
                        for(TCruisePointInstance tCruisePointInstance1:analyseDataOperateService.selectCruiseByTask(jsonObjectResult.get("taskId").toString())){
                            instanceIds.add(tCruisePointInstance1.getInstanceId());
                        }
                    }
                    log.info("instanceIds:"+instanceIds);
                    instanceIds.remove(Long.valueOf(jsonObjectResult.get("instanceId").toString()));//当一个巡视点有值时，从总巡视点集中删除
                    log.info("instanceIds删除后:"+instanceIds);
                    //所有巡视点都有了结果
                    if(instanceIds.size()==0){
                        // TODO: 2020/10/25 修改任务状态为已完成

                        //TCR
                        TCruiseResult tCruiseResult=analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId").toString());
                        tCruiseResult.setCState(Integer.valueOf(analyseDataOperateService.selectDictCode("task_state","执行完成").toString()));
                        tCruiseResult.setTaskWait(0);
                        analyseDataOperateService.updateCruiseResult(tCruiseResult);

                        //TCTR

                        TCruiseTaskResult tCruiseTaskResult=new TCruiseTaskResult();
                        tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
                        tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
//                                tCruiseTaskResult.setTaskAbnormal();
//                                tCruiseTaskResult.getTaskAlarm();
                        tCruiseTaskResult.setRunExecute(cruiseResult.get("ifRun").toString());
//                                tCruiseTaskResult.setCruiseTaskTime();
                        analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);


                        Set<String> cruiseKeys=redisScan("t_cruise_task_result:"+(jsonObjectResult.get("taskId").toString()));
                        for(String cruiseKey:cruiseKeys){
                            Map<String,Object> cruiseWorkedMap=redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
                            //创建四张结果表对象



                            //TCTRD
                            TCruiseTaskResultDetail tCruiseTaskResultDetail=new TCruiseTaskResultDetail();
                            tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString()+cruiseWorkedMap.get("instanceId").toString());
                            tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId").toString());
                            tCruiseTaskResultDetail.setDeviceId(Long.valueOf(cruiseWorkedMap.get("deviceId").toString()));
                            tCruiseTaskResultDetail.setInstanceId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime").toString()));
                            tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime").toString()));
                            tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(cruiseWorkedMap.get("cruiseStatus").toString()));
                            tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark").toString());
                            analyseDataOperateService.insertCruiseTaskResultDetail(tCruiseTaskResultDetail);

                            //TCDR
                            TCruiseDataResult tCruiseDataResult=new TCruiseDataResult();
                            tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                            tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
                            tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
                            tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
                            tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
                            tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
                            tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
                            tCruiseDataResult.setState(Integer.valueOf(cruiseWorkedMap.get("state").toString()));
                            tCruiseDataResult.setIdentifyState(Integer.valueOf(cruiseWorkedMap.get("identifyState").toString()));
                            tCruiseDataResult.setIdentifyResult(Integer.valueOf(cruiseWorkedMap.get("identifyResult").toString()));
                            tCruiseDataResult.setCreatetime(new Date());
                            tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString()+cruiseWorkedMap.get("instanceId").toString());
                            analyseDataOperateService.insertCruiseDataResult(tCruiseDataResult);
                        }

                    }



//                    //告警处理
//                    String remoteAdds = ctx.channel().remoteAddress().toString();
//                    int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":")+1));//Port:13668-表计识别,Port:13669-缺陷识别
//                    //告警判断并组合数据插库
//                    switch (remotePort){
//                        //表计识别算法结果处理
//                        case 13668:
//                            TStdDevicemete tStdDevicemete=analyseDataOperateService.selectDeviceMeteByInstanceId(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
//                            TCruisePointInstance tCruisePointInstance=analyseDataOperateService.selectPointInstance(Long.valueOf(jsonObjectResult.get("instanceId").toString()));
//                            Float resultValue=Float.valueOf(jsonObjectResult.get("resultValue").toString());
//                            int warnRule=analyseDataOperateService.warnJudgement(resultValue,
//                                    tStdDevicemete.getHighLimit1(),
//                                    tStdDevicemete.getLowLimit1(),
//                                    tStdDevicemete.getHighLimit2(),
//                                    tStdDevicemete.getLowLimit2());
//                            if(warnRule>0){
//                                // TODO: 2020/10/20 组装TWarnInfo数据并插库
//                                TWarnInfo tWarnInfo=new TWarnInfo();
//                                tWarnInfo.setWarnLevel(tStdDevicemete.getAlarmLevel());
//                                tWarnInfo.setWarnTime(new Date());//时间尚未格式化
//                                tWarnInfo.setWarnType(Integer.valueOf(tStdDevicemete.getAlarmType()));
//                                switch (warnRule){
//                                    case 1:
//                                        tWarnInfo.setWarnName(tStdDevicemete.getMeteName()+"过高");
//                                        tWarnInfo.setOutRange(String.valueOf(resultValue-tStdDevicemete.getHighLimit1()));//实际量超出上下限的差值
//                                    case 2:
//                                        tWarnInfo.setWarnName(tStdDevicemete.getMeteName()+"过低");
//                                        tWarnInfo.setOutRange(String.valueOf(tStdDevicemete.getLowLimit1()-resultValue));
//                                    case 3:
//                                        tWarnInfo.setWarnName(tStdDevicemete.getMeteName()+"超高");
//                                        tWarnInfo.setOutRange(String.valueOf(resultValue-tStdDevicemete.getHighLimit2()));
//                                    case 4:
//                                        tWarnInfo.setWarnName(tStdDevicemete.getMeteName()+"超低");
//                                        tWarnInfo.setOutRange(String.valueOf(tStdDevicemete.getLowLimit2()-resultValue));
//                                        break;
//                                }
//                                tWarnInfo.setWarnContent(tWarnInfo.getWarnName());
//                                tWarnInfo.setDeviceId(tCruisePointInstance.getDeviceId());//设备ID
//                                tWarnInfo.setCunstomId(tCruisePointInstance.getCustomId());//部位ID
//                                tWarnInfo.setInstanceId(Long.valueOf(jsonObjectResult.get("instanceId").toString()));//巡视点ID
//                                tWarnInfo.setStdMeteId(tCruisePointInstance.getDeviceMeteId());//标准测点ID
//                                tWarnInfo.setConfMode(276);//未处理
//                                switch (tCruisePointInstance.getCruiseTypeName()){
//                                    case("机器人"):
//                                        tWarnInfo.setAlarmSource(Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_source","机器人")));//判断巡视点的巡视方式再决定
//                                    case ("视频"):
//                                        tWarnInfo.setAlarmSource(Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_source","可见光")));//判断巡视点的巡视方式再决定
//                                    case ("红外"):
//                                        tWarnInfo.setAlarmSource(Integer.valueOf(analyseDataOperateService.selectDictCode("alarm_source","红外")));//判断巡视点的巡视方式再决定
//                                        break;
//                                }
//                                tWarnInfo.setDeviceCode(tCruisePointInstance.getDeviceCode());//设备编码
////                        tWarnInfo.setImagePath();//该巡视点所拍摄的图片
////                        tWarnInfo.setValue(jsonObjectResult.get("resultValue").toString());//resultValue
////                        tWarnInfo.setLinkMessage();//若该巡视点属于联动任务，则赋值
//                                analyseDataOperateService.insertWarnInfo(tWarnInfo);//组装完成的告警信息插库
//
//                            }
//                            //缺陷识别算法结果处理
//                        case 13669:
//
//                            break;
//                    }
//
//
                }



        }
    }

    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            if (isTimeout(TYPET3, hisT3, false)) {
                SendHeartBeat(ctx);
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
        // TODO: 2020/10/22
        //  String registerMsg = "发送假数据";
        //        sendString(ctx, registerMsg);
    }

    private void SendHeartBeat(ChannelHandlerContext ctx) {
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    public void sendDataReguest(JSONObject analysisObject) {
        String msg = analysisObject.toString();
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

}
