package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.entity.MessageEntity;
import com.yjh.accessrobot.module.device.service.SysLogsService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.bcel.generic.LineNumberGen;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.sql.DataTruncation;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accessrobot.common.Constant.*;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class RobotServerHandler extends ChannelInboundHandlerAdapter {

    public RobotServerHandler() {
        hisT3 = System.currentTimeMillis();
        T3 = 15000;
    }

    private SysLogsService sysLogsService;
    private RobotService robotService;

    public void setSysLogsService(SysLogsService sysLogsService) {
        this.sysLogsService = sysLogsService;
    }

    public void setRobotService(RobotService robotService) {
        this.robotService = robotService;
    }

    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String serverName;

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    //场站号
    private String strRobotCode = "TT";
    private boolean isThreadStart = true;

    public boolean getIsThreadStart() {
        return isThreadStart;
    }

    private long hisT3;
    private long T3;

    private byte[] bufBytes = new byte[1024];
    private ChannelHandlerContext ctx;
    //遥调遥控
    private static Map<Object, RobotServerHandler> robotServerHandlerMap = new HashMap<>();

    public static Map<Object, RobotServerHandler> getRobotServerHandlerMap() {
        return robotServerHandlerMap;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //TO DO: channel 和 ChannelPipeline 是否需要关闭？？
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        isThreadStart = false;
        Channel channel = ctx.channel();
        if (channel.id() != null) {
            maps.remove(channel.id().toString());
            log.info("id: " + channel.id() + ", strRobotCode: " + strRobotCode + " left," + "Onlinesize: " + maps.size());
        }
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            isThreadStart = false;
            log.error("clientDisconnect: " + e.getMessage());
        }
        log.info("mapsAfterRemoved: " + maps);
        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strRobotCode == null || strRobotCode.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            robotServerHandlerMap.remove(strRobotCode);
            strRobotCode = strRobotCode.replace("\0", "");
            Object strid2 = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strRobotCode)).get("deviceId");
            if (strid2 != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dataTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
                log.info("offlineClientId:" + strid2 + "  channel.isActive(): " + channel.isActive());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        this.ctx = ctx;
        maps.put(ctx.channel().id().toString(), ctx);
        log.info("mapsAfterAdded: " + maps);
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: " + maps.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if (cause.toString().equals("java.io.IOException: 远程主机强迫关闭了一个现有的连接。") || cause.toString().equals("java.io.IOException: Connection reset by peer")) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }
//        ctx.close().sync();
//        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收机器人发送的指令
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = new String(bytes, "UTF-8");
        log.info("还没处理的xml是|" + body + "|");
        int flag = 0;
        String bodyTem = null;
        if (flag == 0) {
            bodyTem = body.substring(22);//21,60
            flag++;
        } else {
            bodyTem = body;
        }
        String xmlContext = null;
        String bdy = bodyTem.replace("</Robot>", "</Robot>flag");
        String[] res = bdy.split("flag");
        for (int i = 0; i < res.length; i++) {
            log.info("截取的内容是:" + res[i]);
            if (res[i].startsWith("<?xml") && res[i].endsWith("</Robot>")) {
                xmlContext = res[i];
                log.info("处理过的xml是|" + xmlContext + "|");
                zuoJueDing(bytes,xmlContext);
            }
//            else{
//                xmlContext = res[i].substring(23);
//                log.info("处理过的xml是|" + xmlContext + "|");
//                zuoJueDing(bytes,xmlContext);
//            }
//            else {
//                log.info("临时文件是:"+res[i]);
//                String linShi = "";
//                res[i] = linShi+res[i];
//                log.info("组装的文件是:"+res[i]);
//            }
        }
        ReferenceCountUtil.release(byteBuf);
    }
    private void zuoJueDing(byte[] bytes,String xmlContext)throws Exception{
        Document document = DocumentHelper.parseText(xmlContext);//String转XML
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
        byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
        byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号

        log.info("解析出来的xml是：" + xmlRes);

//        redisTemplate.opsForHash().putAll("RobotXML", xmlToMap);//将Map放缓存
//        Map<String, Object> RobotXMLMap = redisTemplate.opsForHash().entries("RobotXML");//读redis
        if (xmlRes.getSendCode() == null) {
            log.info("连接可能断了，等待重连.....");
        } else {
            doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
        }
    }
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {
        if ("251".equals(xmlBaseModel.getType())){
            switch (xmlBaseModel.getType()+xmlBaseModel.getCommand()){
                //注册指令(发送响应)
                case "2511":
                    log.info("巡视主机收到注册指令了");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    List<Map<String, Object>> ItemsList = new ArrayList<>();
                    Map<String, Object> Items = new HashMap<>();
                    Items.put("heart_beat_interval", "6");
                    Items.put("robot_run_interval", "6");
                    Items.put("weather_interval", "6");
                    ItemsList.add(Items);
                    XMLBaseModel xmlBaseModelTemp = new XMLBaseModel()
                            .setSendCode("巡视主机")
                            .setReceiveCode("Client01")
                            .setType("251")
                            .setCode("200")
                            .setCommand("4")
                            .setTime(sdf.format(new Date()))
                            .setItems(ItemsList);
                    String registerXmlString = PlatformXMLUtil.generateXml(xmlBaseModelTemp);//生成xml
                    log.info("生成的注册xml是<start>" + registerXmlString + "<end>");
                    byte[] registerProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, registerXmlString);

                    StringBuilder Str = new StringBuilder();
                    for (byte byteitem : registerProtocol) {
                        Str.append(String.format("%02x ", byteitem));
                    }
                    log.info("发送给机器人的注册指令是<start>" + Str + "<end>");
                    SendHeartBeat(registerProtocol);
                    //启动线程
//                    DataDealThread dataDealThread = new DataDealThread(this, isThreadStart);
//                    Thread thread = new Thread(dataDealThread);
//                    thread.setDaemon(true);
//                    thread.start();
                    robotServerHandlerMap.put(strRobotCode, this);
                    break;  
                //心跳指令(发送响应)
                case "2512":
                    log.info("巡视主机收到心跳指令了");
                    String heartXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的心跳xml是<start>" + heartXmlString + "<end>");
                    byte[] heartProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, heartXmlString);

                    StringBuilder Str2 = new StringBuilder();
                    for (byte byteitem : heartProtocol) {
                        Str2.append(String.format("%02x ", byteitem));
                    }
                    log.info("发送给机器人的心跳指令是<start>" + Str2 + "<end>");
                    send(ctx, heartProtocol);
                    break;
                //模型同步指令and任务控制指令(接收响应)
                case "2514":
                    if (xmlBaseModel.getItems().get(0).size() == 1) {//任务控制指令
                        log.info("机器人收到任务控制指令了,这是机器人的响应");
                        //处理任务控制的响应
                        String taskId = xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString();
                        log.info("巡视任务执行Id是："+taskId);

                    }else if (xmlBaseModel.getItems().get(0).size() == 2){//模型同步指令
                        log.info("机器人收到模型指令了,这是机器人的响应");
                        //处理模型同步的响应
                        String deviceFile = xmlBaseModel.getItems().get(0).get("device_file_path").toString();
                        String robotFile = xmlBaseModel.getItems().get(0).get("robot_file_path").toString();
                        XMLBaseModel deviceModel = getXmlMessage(Constant.filePath+deviceFile);
                        List<Map<String,Object>> deviceMap = deviceModel.getItems();
                        log.info("deviceMap是："+deviceMap);
                        XMLBaseModel robotModel = getXmlMessage(Constant.filePath+robotFile);
                        List<Map<String,Object>> robotMap = robotModel.getItems();
                        log.info("robotMap是："+robotMap);
                        robotService.robotFileIntoDB(deviceMap,robotMap);
                    }
                    break;
                //任务下发指令and控制指令(接收响应)
                case "2513":
                    log.info("机器人收到下发任务指令/控制指令了,这是机器人的响应");
                    //处理任务下发/控制的响应
                    log.info("巡视主机收到的响应是："+xmlBaseModel.toString());
                    break;
            }
        }else {
            switch (xmlBaseModel.getType()){
                //机器人状态数据(接收并发送响应)
                case "1":
                    log.info("巡视主机收到机器人状态数据了");
                    //处理机器人状态数据
                    List<Map<String,String>> robotStatusList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res->{
                        Map<String, String> robotStatusMap = new HashMap<>();
                        robotStatusMap.put("robotName",res.get("robot_name").toString());
                        robotStatusMap.put("robotCode",xmlBaseModel.getSendCode());
                        robotStatusMap.put("time",res.get("time").toString());
                        //1.电量状态2.通信状态3.超声4.驱动21.故障41.运行状态61控制模式81.控制权状态101.轮转状态
                        robotStatusMap.put("type",res.get("type").toString());
                        robotStatusMap.put("value",res.get("value").toString());
                        robotStatusMap.put("valueUnit",res.get("value_unit").toString());
                        robotStatusMap.put("unit",res.get("unit").toString());
                        robotStatusList.add(robotStatusMap);
                    });
                    log.info("机器人状态数据是："+robotStatusList);
                    //放缓存(还没写)
                    String statusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + statusXmlString + "<end>");
                    byte[] statusProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, statusXmlString);
                    send(ctx, statusProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人运行数据(接收并发送响应)
                case "2":
                    log.info("巡视主机收到机器人运行数据了");
                    //处理机器人运行数据
                    List<Map<String,String>> robotOperationList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> robotOperationMap = new HashMap<>();
                        robotOperationMap.put("robotName", res.get("robot_name").toString());
                        robotOperationMap.put("robotCode", xmlBaseModel.getSendCode());
                        robotOperationMap.put("time", res.get("time").toString());
                        robotOperationMap.put("type",res.get("type").toString());//1.运动速度2.行驶里程3.电池电量
                        robotOperationMap.put("value", res.get("value").toString());
                        robotOperationMap.put("valueUnit", res.get("valueUnit").toString());
                        robotOperationMap.put("unit", res.get("unit").toString());
                        robotOperationList.add(robotOperationMap);
                    });
                    log.info("机器人运行数据是："+robotOperationList);
                    //放缓存(还没写)
                    String operationXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + operationXmlString + "<end>");
                    byte[] operationProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, operationXmlString);
                    send(ctx, operationProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人坐标(接收并发送响应)
                case "3":
                    log.info("巡视主机收到机器人坐标数据了");
                    //处理机器人坐标数据
                    List<Map<String,String>> robotCoordinateList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> robotCoordinateMap = new HashMap<>();
                        robotCoordinateMap.put("robotName", res.get("robot_name").toString());
                        robotCoordinateMap.put("filePath", res.get("file_path").toString());
                        robotCoordinateMap.put("robotCode", xmlBaseModel.getSendCode());
                        robotCoordinateMap.put("time",res.get("time").toString());
                        robotCoordinateMap.put("coordinatePixel", res.get("coordinate_pixel").toString());//坐标框(像素点)
                        robotCoordinateMap.put("coordinateGeography", res.get("coordinate_geography").toString());//坐标框(经纬度)
                        robotCoordinateList.add(robotCoordinateMap);
                    });
                    log.info("机器人坐标数据是："+robotCoordinateList);
                    //放缓存(还没写)
                    String coordinateXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + coordinateXmlString + "<end>");
                    byte[] coordinateProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, coordinateXmlString);
                    send(ctx, coordinateProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人巡视路线(接收并发送响应)
                case "4":
                    log.info("巡视主机收到机器人巡视路线数据了");
                    //处理机器人巡视路线数据
                    List<Map<String,String>> robotRoadList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> robotRoadMap = new HashMap<>();
                        robotRoadMap.put("robotName", res.get("robot_name").toString());
                        robotRoadMap.put("filePath", res.get("file_path").toString());
                        robotRoadMap.put("robotCode", xmlBaseModel.getSendCode());
                        robotRoadMap.put("time",res.get("time").toString());
                        robotRoadMap.put("coordinatePixel", res.get("coordinate_pixel").toString());//坐标框(像素点)
                        robotRoadMap.put("coordinateGeography", res.get("coordinate_geography").toString());//坐标框(经纬度)
                        robotRoadList.add(robotRoadMap);
                    });
                    log.info("机器人巡视路线数据是："+robotRoadList);
                    //放缓存(还没写)
                    String roadXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + roadXmlString + "<end>");
                    byte[] roadProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, roadXmlString);
                    send(ctx, roadProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人异常告警数据(接收并发送响应)
                case "5":
                    log.info("巡视主机收到机器人异常告警数据了");
                    //处理机器人异常告警数据
                    List<Map<String,String>> robotAlarmList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> robotAlarmMap = new HashMap<>();
                        robotAlarmMap.put("robotName", res.get("robot_name").toString());
                        robotAlarmMap.put("robot_code", xmlBaseModel.getSendCode());
                        robotAlarmMap.put("time", res.get("time").toString());
                        robotAlarmMap.put("content", res.get("content").toString());//告警内容
                        robotAlarmList.add(robotAlarmMap);
                    });
                    log.info("机器人异常告警数据是："+robotAlarmList);
                    //入库(还没写)
                    String alarmXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + alarmXmlString + "<end>");
                    byte[] alarmProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, alarmXmlString);
                    send(ctx, alarmProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //微气象数据(接收并发送响应)
                case "21":
                    log.info("巡视主机收到微气象数据了");
                    //处理微气象数据
                    List<Map<String,String>> weatherList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> weatherMap = new HashMap<>();
                        weatherMap.put("robotName", res.get("robot_name").toString());
                        weatherMap.put("robotCode", xmlBaseModel.getSendCode());
                        weatherMap.put("time", res.get("robot_name").toString());
                        weatherMap.put("type", res.get("robot_name").toString());//1.环境温度2.环境适度3.风速4.大气压5.雨量
                        weatherMap.put("value", res.get("robot_name").toString());
                        weatherMap.put("value_unit", res.get("robot_name").toString());
                        weatherMap.put("unit", res.get("robot_name").toString());
                        weatherList.add(weatherMap);
                    });
                    log.info("微气象数据是："+weatherList);
                    //放缓存(还没写)
                    String weatherXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + weatherXmlString + "<end>");
                    byte[] weatherProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, weatherXmlString);
                    send(ctx, weatherProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //任务状态数据(接收并发送响应)
                case "41":
                    log.info("巡视主机收到任务状态数据了");
                    //处理任务状态数据
                    List<Map<String,String>> taskStatusList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> taskStatusMap = new HashMap<>();
                        taskStatusMap.put("taskPatrolled_id", res.get("task_patrolled_id").toString());
                        taskStatusMap.put("taskName", res.get("task_name").toString());
                        taskStatusMap.put("taskCode", res.get("task_code").toString());
                        //1.已执行2.正在执行3.暂停4.终止5.未执行6.超期
                        taskStatusMap.put("taskState", res.get("task_state").toString());//任务状态
                        taskStatusMap.put("planStartTime", res.get("plan_start_time").toString());//计划开始时间
                        taskStatusMap.put("startTime", res.get("start_time").toString());//开始时间
                        taskStatusMap.put("taskProgress", res.get("task_progress").toString());//任务进度
                        taskStatusMap.put("taskEstimatedTime", res.get("task_estimated_time").toString());//任务预计剩余时间
                        taskStatusMap.put("description", res.get("description").toString());//描述
                        taskStatusList.add(taskStatusMap);
                    });
                    log.info("机器人任务状态数据是："+taskStatusList);
                    //入库或者放缓存(还没写)
                    String taskStatusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + taskStatusXmlString + "<end>");
                    byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, taskStatusXmlString);
                    send(ctx, taskStatusProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //巡视结果
                case "61":
                    log.info("巡视主机收到巡视结果了");
                    //处理巡视结果
                    List<Map<String,String>> cruiseResultList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res-> {
                        Map<String, String> cruiseResultMap = new HashMap<>();
                        cruiseResultMap.put("robotCode",xmlBaseModel.getSendCode());
                        cruiseResultMap.put("taskName",res.get("task_name").toString());
                        cruiseResultMap.put("taskCode",res.get("task_code").toString());
                        cruiseResultMap.put("deviceName",res.get("device_name").toString());//巡检点名称
                        cruiseResultMap.put("deviceId",res.get("device_id").toString());
                        cruiseResultMap.put("value",res.get("value").toString());
                        cruiseResultMap.put("valueUnit",res.get("value_unit").toString());
                        cruiseResultMap.put("unit",res.get("unit").toString());
                        cruiseResultMap.put("time",res.get("time").toString());
                        //1.表计读取2.位置状态识别3.设备外观查看4.红外测温5.声音检测6.闪烁检测
                        cruiseResultMap.put("recognitionType",res.get("recognition_type").toString());//识别类型
                        //1.红外图谱2.可见光照片3.音频
                        cruiseResultMap.put("fileType",res.get("file_type").toString());//采集文件类型
                        cruiseResultMap.put("filePath",res.get("file_path").toString());//文件名称
                        cruiseResultMap.put("rectangle",res.get("rectangle").toString());//图像框
                        cruiseResultMap.put("taskPatrolledId",res.get("task_patrolled_id").toString());
                        cruiseResultList.add(cruiseResultMap);
                    });
                    log.info("机器人巡视结果数据是："+cruiseResultList);
                    //入库(还没写)
                    String cruiseResultXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true));
                    log.info("生成的响应xml是<start>" + cruiseResultXmlString + "<end>");
                    byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, cruiseResultXmlString);
                    send(ctx, cruiseResultProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
            }
        }
    }
    //快速创建command=3 的消息体
    private XMLBaseModel sendMessageForCommandThree(boolean flag){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XMLBaseModel xmlBaseModelEmpty = new XMLBaseModel();
        xmlBaseModelEmpty.setCommand("3");
        xmlBaseModelEmpty.setTime(sdf.format(new Date()));
        xmlBaseModelEmpty.setType("251");
        xmlBaseModelEmpty.setCode(flag?"200":"500");
        xmlBaseModelEmpty.setSendCode("巡视主机");
        xmlBaseModelEmpty.setReceiveCode("Client01");
        return xmlBaseModelEmpty;
    }
    //通过文件路径解析XML
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws Exception {
        //创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        //通过reader对象的read方法加载文件,获取docuemnt对象。
        Document document = reader.read(new File(filePathAndName));
        XMLBaseModel xmlRM = PlatformXMLUtil.readStringXmlOut(document);
        return xmlRM;
    }

    private void handlerData() { }

    void ProcSend() {
        // 心跳报文(客户端,服务端均可发起测试);
//        if (istimeout(HEARTBEAT, hisT3, false)) {
//            SendHeartBeat();
//            hisT3 = System.currentTimeMillis();
//        }
        if (istimeout(hisT3)){
            //发送心跳响应报文
        }else {
            //没有接收到心跳报文的处理方法
            log.info("没有收到心跳报文......");
//                try {
//                    ctx.close().sync();
//                    ctx.flush();
//                    super.channelInactive(ctx);
//                } catch (Exception e) {
//                    isThreadStart = false;
//                    log.error("clientDisconnect: " + e.getMessage());
//                }
        }

    }

    private void send(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSendToRobot:" + Str + " : " + strRobotCode);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendToRobotSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    private boolean istimeout(long value) {
        if ((System.currentTimeMillis() - value) < T3) {
            hisT3 = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public void SendHeartBeat (byte[] protocolBytes) {
        send(ctx,protocolBytes);
    }

    public static Map<String, Object> getMessage(String messageXML) throws Exception {
        Map<String, Object> message = new HashMap<String, Object>();
        //将报文XML交给DocumentHelper解析为Document
        Document document = DocumentHelper.parseText(messageXML);
        //获取根节点
        Element root = document.getRootElement();

        //将报文头信息添加到返回内容中
        Element head = root.element("head");
        String ver = head == null || head.attributeValue("ver") == null ? "" : head.attributeValue("ver");
        String MsgType = head == null || head.attributeValue("MsgType") == null ? "" : head.attributeValue("MsgType");
        String SessionId = head == null || head.attributeValue("SessionId") == null ? "" : head.attributeValue("SessionId");
        String Result = root.element("Result") == null || root.element("Result").attributeValue("val") == null ? "" : root.element("Result").attributeValue("val");
        if (isEmpty(Result)) {
            Result = root.element("result") == null || root.element("result").attributeValue("val") == null ? "" : root.element("result").attributeValue("val");
        }
        message.put("ver", ver);
        message.put("MsgType", MsgType);
        message.put("SessionId", SessionId);
        message.put("RootResult", Result);

        //从根节点中获取body标签
        Element body = root.element("body");
        if (body == null || body.elements().size() == 0) {
            return message;
        }
        //获取body下所有元素
        List<Element> elements = body.elements();
        Map<String, Object> node = null;
        List<Map<String, Object>> listGroup = null;
        List<MessageEntity> elementGroup = null;
        //container用于记录重复名称的列表，方便归类
        Map<String, List> container = new HashMap<String, List>();
        for (Element element : elements) {
            //如果获取的元素非最基层元素，则进行列表解析
            if (element.hasContent()) {
                node = new HashMap<String, Object>();
                node = readList(element);
                //如果列表存在重复，则使用container记录并存入List，后再put到返回对象中
                if (element.attribute("val") == null || body.elements(element.getName()).size() > 1) {
                    if (container.get(element.getName()) == null) {
                        listGroup = new LinkedList<Map<String, Object>>();
                    } else {
                        listGroup = container.get(element.getName());
                    }

                    listGroup.add(node);

                    container.put(element.getName(), listGroup);
                } else {
                    message.put(element.getName(), node);
                }

                continue;
            }
            //如果不是列表元素，则将数据转为对象存储进Map
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setProperty(element.getName());
            messageEntity.setValue(element.attributeValue("val"));

            if (body.elements(element.getName()).size() > 1) {
                if (container.get(element.getName()) == null) {
                    elementGroup = new LinkedList<MessageEntity>();
                } else {
                    elementGroup = container.get(element.getName());
                }

                elementGroup.add(messageEntity);

                container.put(element.getName(), elementGroup);
            } else {
                message.put(element.getName(), messageEntity);
            }
        }
        //将最后整理的列表附加到返回Map中
        for (String ListName : container.keySet()) {
            message.put(ListName, container.get(ListName));
        }

        return message;
    }

    /**
     * 检测字符串是否为空(null,"","null")
     *
     * @param s
     * @return 为空则返回true，不否则返回false
     */
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s) || "null".equals(s);
    }

    /**
     * 列表解析方法
     *
     * @param param 列表元素
     * @return 报文Map对象
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> readList(Element param) {
        if (param == null || param.elements().size() == 0) {
            return null;
        }
        List<Element> elements = param.elements();
        Map<String, Object> message = new HashMap<String, Object>();
        Map<String, Object> node = null;
        List<Map<String, Object>> listGroup = null;
        List<MessageEntity> elementGroup = null;
        Map<String, List> container = new HashMap<String, List>();
        for (Element element : elements) {
            if (element.hasContent()) {
                node = new HashMap<String, Object>();
                node = readList(element);

                if (element.attribute("val") == null || param.elements(element.getName()).size() > 0) {
                    if (container.get(element.getName()) == null) {
                        listGroup = new LinkedList<Map<String, Object>>();
                    } else {
                        listGroup = container.get(element.getName());
                    }

                    listGroup.add(node);

                    container.put(element.getName(), listGroup);
                } else {
                    message.put(element.getName(), node);
                }

                continue;
            }

            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setProperty(element.getName());
            messageEntity.setValue(element.attributeValue("val"));
            if (param.elements(element.getName()).size() > 1) {
                if (container.get(element.getName()) == null) {
                    elementGroup = new LinkedList<MessageEntity>();
                } else {
                    elementGroup = container.get(element.getName());
                }

                elementGroup.add(messageEntity);

                container.put(element.getName(), elementGroup);
            } else {
                message.put(element.getName(), messageEntity);
            }
        }
        for (String ListName : container.keySet()) {
            message.put(ListName, container.get(ListName));
        }
        return message;
    }

}