package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.ByteUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.entity.MessageEntity;
import com.yjh.accessrobot.module.device.service.SysLogsService;
import com.yjh.accessrobot.thread.TaskExecutePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accessrobot.common.Constant.Packet;
import static com.yjh.accessrobot.common.Constant.maps;

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
    private static final String DATETIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATTPL);
    String todayTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    ByteUtil byteUtil = new ByteUtil();

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
    //数组倒序
    public static byte[] reserve( byte[] arr ){
        byte[] arr1 = new byte[arr.length];
        for( int x=0;x<arr.length;x++ ){
            arr1[x] = arr[arr.length-x-1];
        }
        return arr1 ;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收机器人发送的指令
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("机器人发送的的指令是<start>" + Str + "<end>");

        lookByte(bytes);//看指令
////

//        System.arraycopy(bytes,bytes.length-2,endIdentifierByte,0,2);
//        StringBuilder endStr = new StringBuilder();
//        for (byte byteitem : endIdentifierByte) {
//            endStr.append(String.format("%02x ", byteitem));
//        }
//        if ("eb 90".equals(startStr.toString()) ){
//            System.arraycopy(bytes,19,xmlByteLengthByte,0,4);
//            long xmlByteLength2 = PlatformPacketUtil.reserve(xmlByteLengthByte);//xml的字节长度
//            log.info("xml的字节长度为："+xmlByteLength2);
//            long len = 2+8+8+1+4+xmlByteLength2+2;
//            byte[] total = new byte[(int)len];
//            System.arraycopy(bytes,0,total,0,(int)len);
//            byte[] shengYu = new byte[bytes.length-total.length];
//            System.arraycopy(bytes,(int)len,shengYu,0,bytes.length-total.length);
//        }else {
//        int xmlByteLength = ByteUtil.bytes2Int(xmlByteLengthByte);
//        long xmlByteLength4 = PlatformPacketUtil.bytesToLong(sendSessionIdByte);
//        log.info("xml的字节长度为:"+xmlByteLength);
//        byte[] xmlByte = new byte[(int)xmlByteLength];//xml的内容
//        System.arraycopy(bytes,23,xmlByte,0,(int)xmlByteLength);
//        StringBuilder Str1 = new StringBuilder();
//        for (byte byteitem : bytes) {
//            Str1.append(String.format("%02x ", byteitem));
//        }
//        log.info("发送的xml内容指令是<start>" + Str1 + "<end>");
//        int flag = 0;
//        String bodyTem = null;
//        if (flag == 0) {
//            bodyTem = body.substring(22);//21,60
//            flag++;
//        } else {
//            bodyTem = body;
//        }
//        String xmlContext = null;
//        String bdy = bodyTem.replace("</Robot>", "</Robot>flag");
//        String[] res = bdy.split("flag");
//        for (int i = 0; i < res.length; i++) {
//            log.info("截取的内容是:" + res[i]);
//            if (res[i].startsWith("<?xml") && res[i].endsWith("</Robot>")) {
//                xmlContext = res[i];
//                log.info("处理过的xml是|" + xmlContext + "|");
//                zuoJueDing(bytes,xmlContext);
//            }
//        }


        String body = new String(bytes, "UTF-8");

        log.info("还没处理的Packet="+Packet);
//        String zzbds = "^(?:(?=.*[0-9].*)(?=.*[A-Za-z].*)(?=.*[\\W].*))[\\W0-9A-Za-z]{0,}$";
//        if (!Packet.matches(zzbds)){
//            Packet = "";
//        }
//        log.info("处理过的Packet="+Packet);

        log.info("机器人发来的内容="+body);
        String temporaryBody = Packet + body ;//临时
        String temporaryBody2 = temporaryBody.replace("\"UTF-8\"","\'UTF-8\'");//临时
        String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body

        handlingMethod(bytes,finalBody);

        ReferenceCountUtil.release(byteBuf);
    }
    /* 看指令的工具
    *
    * */
    public void lookByte(byte[] bytes){
        byte[] startIdentifierByte = new byte[2];
        byte[] sendSessionIdByte = new byte[8];
        byte[] receiveSessionIdByte = new byte[8];
        byte[] sessionSourceIdByte = new byte[1];
        byte[] xmlByteLengthByte = new byte[4];
        byte[] endIdentifierByte = new byte[2];
        //1.起始标志符
        System.arraycopy(bytes,0,startIdentifierByte,0,2);
        StringBuilder startStr = new StringBuilder();
        for (byte byteitem : startIdentifierByte) {
            startStr.append(String.format("%02x ", byteitem));
        }
        log.info("起始标志符指令为："+startStr);
        //2.发送会话序列号
        System.arraycopy(bytes,2,sendSessionIdByte,0,8);
        StringBuilder sendSessionIdStr = new StringBuilder();
        for (byte byteitem : sendSessionIdByte) {
            sendSessionIdStr.append(String.format("%02x ", byteitem));
        }
        log.info("发送会话序列号指令为："+sendSessionIdStr);
        long ssi = PlatformPacketUtil.reserve(sendSessionIdByte);//发送会话序列号的字节长度
        log.info("发送会话序列号为："+ssi);
        //3.接收会话序列号
        System.arraycopy(bytes,10,receiveSessionIdByte,0,8);
        StringBuilder receiveSessionIdStr = new StringBuilder();
        for (byte byteitem : receiveSessionIdByte) {
            receiveSessionIdStr.append(String.format("%02x ", byteitem));
        }
        log.info("接收会话序列号指令为："+receiveSessionIdStr);
        //4.会话源标识
        System.arraycopy(bytes,18,sessionSourceIdByte,0,1);
        StringBuilder sessionSourceIdStr = new StringBuilder();
        for (byte byteitem : sessionSourceIdByte) {
            sessionSourceIdStr.append(String.format("%02x ", byteitem));
        }
        log.info("会话源标识指令为："+sessionSourceIdStr);
        //5.xml的字节长度
        System.arraycopy(bytes,19,xmlByteLengthByte,0,4);
        StringBuilder xmlByteLengthStr = new StringBuilder();
        for (byte byteitem : xmlByteLengthByte) {
            xmlByteLengthStr.append(String.format("%02x ", byteitem));
        }
        log.info("xml的字节长度指令为："+xmlByteLengthStr);
        long xmlByteLength = PlatformPacketUtil.reserve(xmlByteLengthByte);//xml的字节长度
        log.info("xml的字节长度为："+xmlByteLength);
        byte[] xmlByte = new byte[(int)xmlByteLength];
        //6.xml的内容
        System.arraycopy(bytes,23,xmlByte,0,(int)xmlByteLength);
        StringBuilder Str1 = new StringBuilder();
        for (byte byteitem : xmlByte) {
            Str1.append(String.format("%02x ", byteitem));
        }
        log.info("发送的xml内容指令是<start>" + Str1 + "<end>");
        //7.结束标志符号
        System.arraycopy(bytes,bytes.length-2,endIdentifierByte,0,2);
        StringBuilder endStr = new StringBuilder();
        for (byte byteitem : endIdentifierByte) {
            endStr.append(String.format("%02x ", byteitem));
        }
        log.info("结束标志符号指令是<start>" + endStr + "<end>");
    }

    /*拆包工具
    *
    * */
    private void handlingMethod(byte[] bytes,String parameter)throws Exception {
//        if (null != parameter) {
//            log.info("Packet+机器人发来的内容总和=：" + parameter);

            String hua = null;

            if (parameter.contains("开始") || parameter.contains("结束")) {
                hua = parameter;
            } else {
                String bian = parameter.replace("<?xml version='1.0' encoding='UTF-8'?>", "开始<?xml version='1.0' encoding='UTF-8'?>");
                hua = bian.replace("</Robot>", "</Robot>结束");
            }
            Pattern pattern1 = Pattern.compile("(\\<\\?xml version='1.0' encoding='UTF-8'?[^>])([\\s\\S]*?)(</Robot>)");
            Matcher matcher1 = pattern1.matcher(hua);
            String wanZheng = null;
            String shengYu = null;
            if (matcher1.find()) {
                wanZheng = matcher1.group();
//                log.info("匹配到一个完整包的内容=" + wanZheng);
                stringToXml(bytes, wanZheng);
//                if (null != parameter) {
                shengYu = parameter.replace(wanZheng, "");
//                log.info("除去一个完整包剩余的=" + shengYu);
                handlingMethod(bytes, shengYu);
//                }
            } else {
                Packet = parameter;
//                log.info("不足一个完整的包：" + Packet);
            }
//        }else {
//            log.info("xmlContext的值是null,啥也不是");
//            Packet = "";
//        }
    }
    private void stringToXml(byte[] bytes,String xmlContext)throws Exception{
//        if (null != xmlContext){
            Document document = DocumentHelper.parseText(xmlContext);//String转XML
            XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
            log.info("解析出来的xml是：" + xmlRes);
            byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
            byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
            System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
            System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
            long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
            long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
            if (xmlRes.getSendCode() == null) {
                log.info("连接可能断了，等待重连.....");
            } else {
                doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
            }
//        }else {
//            log.info("xmlContext的值是null,啥也不是");
//            Packet = "";
//        }

    }
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {

        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

        if ("251".equals(xmlBaseModel.getType())){
            switch (xmlBaseModel.getType()+xmlBaseModel.getCommand()){
                //注册指令(发送响应)
                case "2511":
                    log.info("巡视主机收到注册指令了");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    List<Map<String, Object>> ItemsList = new ArrayList<>();
                    Map<String, Object> Items = new HashMap<>();
                    Items.put("heart_beat_interval", "5");//心跳间隔
                    Items.put("robot_run_interval", "6");//机器人运行数据间隔
                    Items.put("weather_interval", "7");//微气象数据间隔
                    ItemsList.add(Items);
                    XMLBaseModel xmlBaseModelTemp = new XMLBaseModel()
                            .setSendCode("Server01")
                            .setReceiveCode(xmlBaseModel.getSendCode())//Client01
                            .setType("251")
                            .setCode("200")
                            .setCommand("4")
                            .setTime(sdf.format(new Date()))
                            .setItems(ItemsList);
                    String registerXmlString = PlatformXMLUtil.generateXml(xmlBaseModelTemp);//生成xml
//                    log.info("生成的注册xml是<start>" + registerXmlString + "<end>");
                    byte[] registerProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, registerXmlString);

//                    StringBuilder Str = new StringBuilder();
//                    for (byte byteitem : registerProtocol) {
//                        Str.append(String.format("%02x ", byteitem));
//                    }
//                    log.info("发送给机器人的注册指令是<start>" + Str + "<end>");
                    SendHeartBeat(registerProtocol);
                    //启动线程
//                    DataDealThread dataDealThread = new DataDealThread(this, isThreadStart);
//                    Thread thread = new Thread(dataDealThread);
//                    thread.setDaemon(true);
//                    thread.start();
                    robotServerHandlerMap.put(xmlBaseModel.getSendCode(), this);
                    break;  
                //心跳指令(发送响应)
                case "2512":
                    log.info("巡视主机收到心跳指令了");
                    String heartXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
//                    log.info("生成的心跳xml是<start>" + heartXmlString + "<end>");
                    byte[] heartProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, heartXmlString);

//                    StringBuilder Str2 = new StringBuilder();
//                    for (byte byteitem : heartProtocol) {
//                        Str2.append(String.format("%02x ", byteitem));
//                    }
//                    log.info("发送给机器人的心跳指令是<start>" + Str2 + "<end>");
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
                        robotService.robotFileIntoDB(deviceMap,robotMap,xmlBaseModel);
                    }
                    break;
                //任务下发指令and控制指令(接收响应)
                case "2513":
                    log.info("机器人收到下发任务指令/控制指令了,这是机器人的响应");
                    //处理任务下发/控制的响应
                    log.info("巡视主机收到的响应是："+xmlBaseModel.toString());
                    log.info("巡视主机收到响应的type是："+xmlBaseModel.getType());
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
                    //放缓存
                    for (int i = 0; i < robotStatusList.size(); i++) {
                        redisTemplate.opsForHash().putAll("RobotStatus:"+xmlBaseModel.getSendCode()+":"+ robotStatusList.get(i).get("type"), robotStatusList.get(i));//将Map放缓存
                    }
                    String statusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                    byte[] statusProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, statusXmlString);
                    send(ctx, statusProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人运行数据(接收并发送响应)
                case "2":
                    log.info("巡视主机收到机器人运行数据了");
                    //处理机器人运行数据
                    List<Map<String,String>> robotOperationList = new ArrayList<>();
                    xmlBaseModel.getItems().forEach(res->{
                        Map<String,String> robotOperationMap = new HashMap<>();
                        robotOperationMap.put("robotName",res.get("robot_name").toString());
                        robotOperationMap.put("robotCode",res.get("robot_code").toString());
                        robotOperationMap.put("time",res.get("time").toString());
                        robotOperationMap.put("type",res.get("type").toString());//1.运动速度2.行驶里程3.电池电量
                        robotOperationMap.put("value",res.get("value").toString());
                        robotOperationMap.put("valueUnit",res.get("value_unit").toString());
                        robotOperationMap.put("unit",res.get("unit").toString());
                        robotOperationList.add(robotOperationMap);
                    });
//                    log.info("机器人运行数据是；"+robotOperationList);
                    //放缓存
                    for (int i = 0; i < robotOperationList.size(); i++) {
                        redisTemplate.opsForHash().putAll("RobotOperation:"+xmlBaseModel.getSendCode()+":"+ robotOperationList.get(i).get("type"), robotOperationList.get(i));//将Map放缓存
                    }
                    String operationXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
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
//                    log.info("机器人坐标数据是："+robotCoordinateList);
                    //放缓存
                    for (int i = 0; i < robotCoordinateList.size(); i++) {
                        redisTemplate.opsForHash().putAll("RobotCoordinate:"+xmlBaseModel.getSendCode(), robotCoordinateList.get(i));//将Map放缓存
                    }
                    String coordinateXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
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
                        String filePath = res.get("file_path").toString();//文件名称
                        String fenGe[] = filePath.split("/");
                        String fileName = fenGe[fenGe.length - 1];
                        log.info("巡检路线图片名称=="+fileName);
                        String taskId = fenGe[fenGe.length - 3];
                        /*
                        将ftp服务器上的文件复制到开发环境
                        * */
                        String temporaryPath = Constant.filePath+filePath;//文件在ftp服务器上的绝对路径
                        log.info("temporaryPath是==="+temporaryPath);

                        //开发环境图片相对路径文件目录
                        String developRelativeUrl =  relativeImgMap.get("content") +  "/"+ todayTime+ "/"+ taskId + "/Road";
                        //开发环境图片绝对路径文件目录
                        String developAbsoluteUrl = absoluteImgMap.get("content") + "/"+ todayTime+ "/"+taskId + "/Road";
                        log.info("developAbsoluteUrl是==="+developAbsoluteUrl);
                        File f=new File(developAbsoluteUrl);
                        if (!f.exists()){
                            f.setWritable(true, false);
                            f.mkdirs();
                        }

                        try {
                            String url = "cp " + temporaryPath + " " + developAbsoluteUrl;
                            log.info("url是==="+url);
                            Runtime.getRuntime().exec(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        robotRoadMap.put("relativePath",developRelativeUrl + "/" +fileName);//相对路径
                        robotRoadMap.put("absolutePath",developAbsoluteUrl + "/"+fileName);//绝对路径
                        robotRoadMap.put("robotCode", xmlBaseModel.getSendCode());
                        robotRoadMap.put("time",res.get("time").toString());
                        robotRoadMap.put("coordinatePixel", res.get("coordinate_pixel").toString());//坐标框(像素点)
                        robotRoadMap.put("coordinateGeography", res.get("coordinate_geography").toString());//坐标框(经纬度)
                        robotRoadList.add(robotRoadMap);
                    });
//                    log.info("机器人巡视路线数据是："+robotRoadList);
                    //放缓存
                    for (int i = 0; i < robotRoadList.size(); i++) {
                        redisTemplate.opsForHash().putAll("RobotRoad:"+xmlBaseModel.getSendCode(), robotRoadList.get(i));//将Map放缓存
                    }
                    String roadXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                    byte[] roadProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, roadXmlString);
                    send(ctx, roadProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //机器人异常告警数据(接收并发送响应)
                case "5":
                    log.info("巡视主机收到机器人异常告警数据了");
                    //处理机器人异常告警数据
                    Map<String, String> robotAlarmMap = new HashMap<>();
                    robotAlarmMap.put("robotName",xmlBaseModel.getItems().get(0).get("robot_name").toString());
                    robotAlarmMap.put("robotCode",xmlBaseModel.getSendCode());
                    robotAlarmMap.put("time",xmlBaseModel.getItems().get(0).get("time").toString());
                    robotAlarmMap.put("content",xmlBaseModel.getItems().get(0).get("content").toString());//告警内容
                    log.info("机器人异常告警数据是："+robotAlarmMap);

                    //处理告警数据
                    AlarmResultDealThread alarmResultDealThread = new AlarmResultDealThread(robotAlarmMap,redisTemplate);
                    TaskExecutePool.getInstance().execute(alarmResultDealThread);

                    String alarmXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
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
                        weatherMap.put("time", res.get("time").toString());
                        weatherMap.put("type", res.get("type").toString());//1.环境温度2.环境适度3.风速4.大气压5.雨量
                        weatherMap.put("value", res.get("value").toString());
                        weatherMap.put("valueUnit", res.get("value_unit").toString());
                        weatherMap.put("unit", res.get("unit").toString());
                        weatherList.add(weatherMap);
                    });
//                    log.info("微气象数据是："+weatherList);
                    //放缓存
                    for (int i = 0; i < weatherList.size(); i++) {
                        redisTemplate.opsForHash().putAll("RobotWeather:"+xmlBaseModel.getSendCode()+":"+ weatherList.get(i).get("type"), weatherList.get(i));//将Map放缓存
                    }
                    String weatherXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                    byte[] weatherProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, weatherXmlString);
                    send(ctx, weatherProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //任务状态数据(接收并发送响应)
                case "41":
                    log.info("巡视主机收到任务状态数据了");
                    //处理任务状态数据
                        Map<String, Object> taskStatusMap = new HashMap<>();
                        taskStatusMap.put("taskPatrolled_id", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
                        taskStatusMap.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
                        taskStatusMap.put("taskCode", xmlBaseModel.getItems().get(0).get("task_code").toString());
                        //1.已执行2.正在执行3.暂停4.终止5.未执行6.超期
                        taskStatusMap.put("taskState", xmlBaseModel.getItems().get(0).get("task_state").toString());//任务状态
                        taskStatusMap.put("planStartTime", xmlBaseModel.getItems().get(0).get("plan_start_time").toString());//计划开始时间
                        taskStatusMap.put("startTime", xmlBaseModel.getItems().get(0).get("start_time").toString());//开始时间
                        taskStatusMap.put("taskProgress", xmlBaseModel.getItems().get(0).get("task_progress").toString());//任务进度
                        taskStatusMap.put("taskEstimatedTime", xmlBaseModel.getItems().get(0).get("task_estimated_time").toString());//任务预计剩余时间
                        taskStatusMap.put("description", xmlBaseModel.getItems().get(0).get("description").toString());//描述
//                    log.info("机器人任务状态数据是："+taskStatusMap);

                    //先读缓存，进行修改
                    Map<String, Object> listMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+xmlBaseModel.getSendCode());
                    taskStatusMap.put("instanceList",listMap.get("instanceIdList"));
                    //再将taskStatusMap放进缓存
                    redisTemplate.opsForHash().putAll("RobotTaskStatus:"+xmlBaseModel.getSendCode(), taskStatusMap);

                    String taskStatusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                    byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, taskStatusXmlString);
                    send(ctx, taskStatusProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
                //巡视结果
                case "61":
                    log.info("巡视主机收到巡视结果了");
                    //处理巡视结果

//                    Constant.flagMap.put(xmlBaseModel.getItems().get(0).get("task_code").toString(),1);

                    Map<String, String> cruiseResultMap = new HashMap<>();
                    cruiseResultMap.put("robotCode",xmlBaseModel.getSendCode());
                    cruiseResultMap.put("taskName",xmlBaseModel.getItems().get(0).get("task_name").toString());
                    cruiseResultMap.put("taskCode",xmlBaseModel.getItems().get(0).get("task_code").toString());
                    cruiseResultMap.put("deviceName",xmlBaseModel.getItems().get(0).get("device_name").toString());//巡检点名称
                    cruiseResultMap.put("deviceId",xmlBaseModel.getItems().get(0).get("device_id").toString());
                    cruiseResultMap.put("value",xmlBaseModel.getItems().get(0).get("value").toString());
                    cruiseResultMap.put("valueUnit",xmlBaseModel.getItems().get(0).get("value_unit").toString());
                    cruiseResultMap.put("unit",xmlBaseModel.getItems().get(0).get("unit").toString());
                    cruiseResultMap.put("time",xmlBaseModel.getItems().get(0).get("time").toString());
                    //1.表计读取2.位置状态识别3.设备外观查看4.红外测温5.声音检测6.闪烁检测
                    cruiseResultMap.put("recognitionType",xmlBaseModel.getItems().get(0).get("recognition_type").toString());//识别类型
                    //1.红外图谱2.可见光照片3.音频
                    cruiseResultMap.put("fileType",xmlBaseModel.getItems().get(0).get("file_type").toString());//采集文件类型
                    String filePath = xmlBaseModel.getItems().get(0).get("file_path").toString();//文件名称
                    String fenGe[] = filePath.split("/");
                    String fileName = fenGe[fenGe.length - 1];
                    /*
                    将ftp服务器上的文件复制到开发环境
                    * */
                    String temporaryPath = Constant.filePath+filePath;//文件在ftp服务器上的绝对路径
                    log.info("temporaryPath是==="+temporaryPath);

                    //开发环境图片绝对路径文件目录
                    String developAbsoluteUrl = absoluteImgMap.get("content") + "/"+ todayTime  + "/"+ xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";
                    //开发环境图片相对路径文件目
                    String developRelativeUrl = relativeImgMap.get("content")+ "/"+ todayTime  + "/"+ xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";

                    if (xmlBaseModel.getItems().get(0).get("file_type").toString().equals("1")){//红外图谱文件
                        developAbsoluteUrl = developAbsoluteUrl + "FIR";
                        developRelativeUrl = developRelativeUrl + "FIR";
                    }else if(xmlBaseModel.getItems().get(0).get("file_type").toString().equals("2")){//可见光照片文件
                        developAbsoluteUrl = developAbsoluteUrl + "CCD";
                        developRelativeUrl = developRelativeUrl + "CCD";
                    }else if(xmlBaseModel.getItems().get(0).get("file_type").toString().equals("3")){//音频文件
                        developAbsoluteUrl = developAbsoluteUrl + "Audio";
                        developRelativeUrl = developRelativeUrl + "Audio";
                    }
                    log.info("developUrl是==="+developAbsoluteUrl);

                    File f=new File(developAbsoluteUrl);
                    if (!f.exists()){
                        f.setWritable(true, false);
                        f.mkdirs();
                    }

                    try {
                        String url = "cp " + temporaryPath + " "+developAbsoluteUrl;
                        log.info("url是==="+url);
                        Runtime.getRuntime().exec(url);
                    }catch (Exception e){
                        e.getMessage();
                    }

                    cruiseResultMap.put("relativePath",developRelativeUrl + "/" +fileName);//相对路径
                    cruiseResultMap.put("absolutePath",developAbsoluteUrl+"/"+fileName);//绝对路径
                    cruiseResultMap.put("rectangle",xmlBaseModel.getItems().get(0).get("rectangle").toString());//图像框
                    cruiseResultMap.put("taskPatrolledId",xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());

                    log.info("机器人巡视结果数据是："+cruiseResultMap);

                    /*巡检结果处理,在另外一个线程做结果处理
                     *《这》《里》《是》《处》《理》《巡》
                     * 《检》《结》《果》《的》《步》《骤》
                     * */
                    CruiseResultDealThread cruiseResultDealThread = new CruiseResultDealThread(cruiseResultMap,redisTemplate);
                    TaskExecutePool.getInstance().execute(cruiseResultDealThread);

                    String cruiseResultXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                    byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, cruiseResultXmlString);
                    send(ctx, cruiseResultProtocol);
                    log.info("巡视主机给机器人响应了");

                    break;
            }
        }
    }

    //快速创建command=3 的消息体
    private XMLBaseModel sendMessageForCommandThree(boolean flag,String sendCode){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XMLBaseModel xmlBaseModelEmpty = new XMLBaseModel();
        xmlBaseModelEmpty.setCommand("3");
        xmlBaseModelEmpty.setTime(sdf.format(new Date()));
        xmlBaseModelEmpty.setType("251");
        xmlBaseModelEmpty.setCode(flag?"200":"500");
        xmlBaseModelEmpty.setSendCode("Server01");
        xmlBaseModelEmpty.setReceiveCode(sendCode);
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