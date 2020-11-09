package com.yjh.accessvideo.netty.client;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.websocket.WebSocketServer;
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
import static com.yjh.accessvideo.common.Constant.ABNORMAL;
import static com.yjh.accessvideo.common.Constant.NORMAL;
import static com.yjh.accessvideo.common.Constant.cruiseKeys;


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

    public AnalysisClientHandler(RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
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
            // TODO: 2020/11/4 构造线程池对象
            //线程池数据处理
//            DataDealThread dataDealThread=new DataDealThread(body,redisTemplate,analyseDataOperateService);
//            TaskExecutePool.getInstance().execute(dataDealThread);
            handlerData(body); //单线程数据处理
        } catch (Exception e) {
            e.getMessage();
        }

        ReferenceCountUtil.release(byteBuf);
    }

    private void handlerData(String body) throws ParseException {
        //TODO 添加线程池
        JSONObject jsonObject = JSON.parseObject(body);
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


                TASKID=jsonObjectResult.get("taskId").toString();
                INSTANCEID=jsonObjectResult.get("instanceId").toString();
                String name="t_cruise_task_result:"+TASKID+INSTANCEID;
                log.info("keyName"+name);
                cruiseKeys.add(name);



                log.info("数据初始化");
//                    String analyseType=jsonObjectResult.get("analyseType").toString();
//                    log.info(analyseType);
//                    String taskId=jsonObjectResult.get("taskId").toString();
//                    log.info(taskId);
//                    String resultValue1=jsonObjectResult.get("resultValue").toString();
//                    log.info(resultValue1);
//                    String instanceId=jsonObjectResult.get("instanceId").toString();
//                    log.info(instanceId);

                //redis数据键名由taskId+instanceId命名
                log.info("数据Redis业务开启");
                String redisName = jsonObjectResult.get("taskId").toString() + jsonObjectResult.get("instanceId").toString();
                log.info("template:" + redisTemplate);
                log.info("redisName:" + redisName);
                Map<String, Object> cruiseResult = redisTemplate.opsForHash().entries("t_cruise_task_result:" + redisName);//读redis
                log.info("读取到的redis：" + cruiseResult);
                Map<String, String> cruiseResultMap = new HashMap<>();//修改redis的巡检点结果map
                cruiseResultMap.put("resultNum", jsonObjectResult.get("resultValue").toString());
                cruiseResultMap.put("state", analyseDataOperateService.selectDictCode("data_state", "正常"));
                cruiseResultMap.put("cruiseStatus", analyseDataOperateService.selectDictCode("cruise_data_state", "已执行"));
                redisTemplate.opsForHash().putAll("t_cruise_task_result:" + redisName, cruiseResultMap);//修改redis
                NORMAL=NORMAL+1;
                // webSocket通知前端调用巡视监控的接口
                Map<String, Object> jasonMap = new HashMap<>();
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", cruiseResult.get("taskId").toString());
                String json = JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：" + json);
                WebSocketServer.sendMsg(json);

                
                //修改缓存中任务算法巡视点List
                redisTemplate.opsForList().remove(cruiseResult.get("taskId").toString(),0,cruiseResult.get("instanceId").toString());
                redisTemplate.opsForList().leftPush(cruiseResult.get("taskId").toString(),"0");

                log.info("RedisList修改成功");



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

        }else if(jsonObject.get("msgType").toString().equals("6")){ //任务结束后发来的心跳信息
            String taskId=JSON.parseObject(jsonObject.get("msgData").toString()).get("taskId").toString();
            redisTemplate.opsForList().leftPush(taskId,"-1");
            log.info("心跳处理结束"+taskId);
        }

        if(redisTemplate.opsForList().index(TASKID,0).equals("-1") && redisTemplate.opsForList().index(TASKID,1).equals("0")){  //满足插库条件

            //满足条件先插巡视点数据
            List<TCruiseTaskResultDetail> detailList=new ArrayList<>();//TCTRD List对象
            List<TCruiseDataResult> dataList=new ArrayList<>();//TCDR List对象
//            Set<String>cruiseKeys=redisScan("t_cruise_task_result:"+TASKID);
            log.info("cruisekeys:"+cruiseKeys);
            for(String cruiseKey:cruiseKeys){
                Map<String, Object> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
                //TCTRD
                log.info("TCTRD开始");
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                tCruiseTaskResultDetail.setTaskResultId(cruiseWorkedMap.get("taskResultId").toString());
                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(cruiseWorkedMap.get("deviceId").toString()));
                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseWorkedMap.get("cruiseTime").toString()));
                tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(cruiseWorkedMap.get("endTime").toString()));
                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(cruiseWorkedMap.get("cruiseStatus").toString()));
                tCruiseTaskResultDetail.setRemark(cruiseWorkedMap.get("remark").toString());
                detailList.add(tCruiseTaskResultDetail);

                //TCDR
                log.info("TCRD开始");
                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
                tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
                tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
                tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
                tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
                tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
                tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
                tCruiseDataResult.setEvaluationState(Integer.valueOf(analyseDataOperateService.selectDictCode("evaluation_state","未审核")));
                tCruiseDataResult.setState(Integer.valueOf(cruiseWorkedMap.get("state").toString()));
                tCruiseDataResult.setCreatetime(new Date());
                tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
                dataList.add(tCruiseDataResult);
            }
            //批量插入两表

            log.info("两表开始插入");
            analyseDataOperateService.batchInsertCruiseTaskResultDetail(detailList);
            analyseDataOperateService.batchInsertCruiseDataResult(dataList);
            log.info("两表结束插入");
            cruiseKeys.clear();




            // 判断异常点缓存，算法是否为最后一点，决定是否执行TCTR插库操作和TCR库修改操作
            Map<String,Object> cruiseResult=redisTemplate.opsForHash().entries("t_cruise_task_result:"+TASKID+INSTANCEID);
            String strForCountAbnormal = "countForAbnormal:"+TASKID;
            Map<String,Object>abnormalCount=redisTemplate.opsForHash().entries(strForCountAbnormal);
            Integer total=Integer.valueOf(abnormalCount.get("all").toString());
            Integer abnormal=Integer.valueOf(abnormalCount.get("abnormal").toString());
            Integer normal=Integer.valueOf(abnormalCount.get("normal").toString());
            //判断最后一个执行完成的巡视点是否是算法点--T:插TCTR库表和修改TCR库表；F：更新异常、正常点数量
            if(abnormal+ABNORMAL+normal+NORMAL==total){
                //TCTR开始
                log.info("TCTR开始");
                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
                tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
                tCruiseTaskResult.setTaskAbnormal(abnormal+ABNORMAL);
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
            }else {       // 修改异常点数缓存
                Map<String,String> mapForAbnormal = new HashMap<>();
                Integer totAbnormal=abnormal+ABNORMAL;
                Integer totNormal=normal+NORMAL;
                mapForAbnormal.put("abnormal",totAbnormal.toString());
                mapForAbnormal.put("normal",totNormal.toString());
                log.info("Normal"+NORMAL.toString());
                log.info("normal:"+totNormal.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);
            }
            ABNORMAL=0;
            NORMAL=0;

        }




        //判断该任务下的巡视点是否均已执行完成

        //当前任务下所有的巡视点
//        if (instanceIds.size() == 0) {
//            for (TCruisePointInstance tCruisePointInstance1 : analyseDataOperateService.selectCruiseByTask(jsonObjectResult.get("taskId").toString())) {
//                instanceIds.add(tCruisePointInstance1.getInstanceId());
//            }
//        }
//        log.info("instanceIds:" + instanceIds);
//        instanceIds.remove(Long.valueOf(jsonObjectResult.get("instanceId").toString()));//当一个巡视点有值时，从总巡视点集中删除
//        log.info("instanceIds删除后:" + instanceIds);
        //所有巡视点都有了结果
//        if (instanceIds.size() == 0) {
//            //  修改任务状态为已完成
//            log.info("TCR开始");
//            //TCR
//            TCruiseResult tCruiseResult = analyseDataOperateService.selectByPrimaryIdCruiseResult(cruiseResult.get("taskResultId").toString());
//            tCruiseResult.setCState(Integer.valueOf(analyseDataOperateService.selectDictCode("task_state", "执行完成").toString()));
//            tCruiseResult.setTaskWait(0);
//            analyseDataOperateService.updateCruiseResult(tCruiseResult);
//
//            log.info("TCTR开始");
//            //TCTR
//
//            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
//            tCruiseTaskResult.setTaskResultId(cruiseResult.get("taskResultId").toString());
//            tCruiseTaskResult.setTaskId(cruiseResult.get("taskId").toString());
//            tCruiseTaskResult.setTaskAbnormal(0);
//            tCruiseTaskResult.setTaskAlarm(0);
//            tCruiseTaskResult.setRunExecute(cruiseResult.get("if_run").toString());
////                                tCruiseTaskResult.setCruiseTaskTime();
//            analyseDataOperateService.insertCruiseTaskResult(tCruiseTaskResult);
//
//
//            Set<String> cruiseKeys = redisScan("t_cruise_task_result:" + (jsonObjectResult.get("taskId").toString()));
//            for (String cruiseKey : cruiseKeys) {
//                Map<String, Object> cruiseWorkedMap = redisTemplate.opsForHash().entries(cruiseKey);//取出缓存中该任务下的巡视点
//                //创建四张结果表对象
//
//                log.info("TCTRD开始");
//                //TCTRD
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
//                analyseDataOperateService.insertCruiseTaskResultDetail(tCruiseTaskResultDetail);
//
//                log.info("TCDR开始");
//                //TCDR
//                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
//                tCruiseDataResult.setCruiseId(Long.valueOf(cruiseWorkedMap.get("instanceId").toString()));
//                tCruiseDataResult.setPicpath(cruiseWorkedMap.get("picpath").toString());
//                tCruiseDataResult.setResultNum(cruiseWorkedMap.get("resultNum").toString());
//                tCruiseDataResult.setResultDesc(cruiseWorkedMap.get("resultDesc").toString());
//                tCruiseDataResult.setCruiseType(Integer.valueOf(cruiseWorkedMap.get("cruiseType").toString()));
//                tCruiseDataResult.setModifyNum(cruiseWorkedMap.get("modifyNum").toString());
//                tCruiseDataResult.setOrigpic(cruiseWorkedMap.get("origpic").toString());
//                tCruiseDataResult.setState(Integer.valueOf(cruiseWorkedMap.get("state").toString()));
//                tCruiseDataResult.setCreatetime(new Date());
//                tCruiseDataResult.setCruiseResultId(cruiseWorkedMap.get("taskResultId").toString() + cruiseWorkedMap.get("instanceId").toString());
//                analyseDataOperateService.insertCruiseDataResult(tCruiseDataResult);
//            }
//
//        }


    }

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
        // TODO: 2020/10/22
        //  String registerMsg = "发送假数据";
        //        sendString(ctx, registerMsg);
    }

    public void SendHeartBeat(Analysis analysis) throws InterruptedException {
        String instanceId = analysis.getInstanceId().toString();
        String taskId = analysis.getTaskId();
        Thread.sleep(3000);
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"instanceId\": " + "\"" + instanceId + "\", \n\"taskId\": \"" + taskId + "\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

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
