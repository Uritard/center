package com.yjh.accesstcp.netty.server;

import com.alibaba.fastjson.JSON;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.thread.RegisterManager;
import com.yjh.accesstcp.thread.TaskExecutePool;
import com.yjh.accesstcp.thread.WeatherThread;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accesstcp.common.Constant.Packet;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class TCPClientHandlerImpl extends SimpleChannelInboundHandler<DatagramPacket> implements TCPClientHandler {

    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;
    private AnalysisUnionTaskFileService analysisUnionTaskFileService;
    private RegisterManager registerManager;


    public TCPClientHandlerImpl(RedisTemplate redisTemplate,SendToUpSystemServices sendToUpSystemServices,AnalysisUnionTaskFileService analysisUnionTaskFileService, RegisterManager registerManager) {
        this.redisTemplate = redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;
        this.analysisUnionTaskFileService = analysisUnionTaskFileService;
        this.registerManager =registerManager;

    }
    private boolean isThreadStart = true;

    //场站号
    private String strChannelID = "TT";

    private byte[] bufBytes = new byte[1024];
    private int bufdateLen;
    private ChannelHandlerContext ctx;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        isThreadStart = false;
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        TCPClientHandlerHashMap.remove(remotePort);
        log.error("服务端主动断开连接！");
        log.info("analysisClientHandlerHashMap: " + TCPClientHandlerHashMap);
        String serverUrl = remoteAdds.substring(0, remoteAdds.indexOf(":"));
        log.info("serverUrl: " + serverUrl.substring(1));
        InetSocketAddress remoteAddress = new InetSocketAddress(serverUrl.substring(1), remotePort);
        //使用过程中断线重连
        if (Objects.nonNull(Constant.bootstrapHashMap.get(1))) {
            doConnect(remoteAddress, Constant.bootstrapHashMap.get(1));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法

        this.ctx = ctx;
        sendRegister();//发送注册
        isThreadStart = true;

        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        if (TCPClientHandlerHashMap.get(remotePort) == null) {
            TCPClientHandlerHashMap.put(remotePort, this);
        }
        log.info("客户端注册成功: " + ctx.channel().remoteAddress());
        log.info("客户端注册成功: " + remoteAdds);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if (cause.toString().equals("java.io.IOException: 远程主机强迫关闭了一个现有的连接。") || cause.toString().equals("java.io.IOException: Connection reset by peer")) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }

    }

    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }
    public void openPackage (String socketMessageHex,int headNum) throws Exception{
        String onePacketString = null;
        String residueString = null;
        if(headNum >= 2) {
            //有至少一个完整的包
            if(!socketMessageHex.startsWith("eb90")){
                socketMessageHex = socketMessageHex.substring(socketMessageHex.indexOf("eb90"));
            }
            int limitNum = socketMessageHex.indexOf("eb90", socketMessageHex.indexOf("eb90") + 1) + 3;//一个包的长度-1

            if(socketMessageHex.length()-limitNum == 1){
                byte[] onePacket = PlatformPacketUtil.HexString2Bytes(socketMessageHex);
                StringBuilder Str2 = new StringBuilder();
                for (byte byteItem : onePacket) {
                    Str2.append(String.format("%02x ", byteItem));
                }
                log.info("一个完整的包,准备解析的字节数组="+Str2);

                String body1 = socketMessageHex.substring(46,socketMessageHex.length()-4);
                onePacketString = PlatformPacketUtil.toStringHex(body1);
                log.info("准备解析的xml=="+onePacketString);
                Packet = "";
                stringToXml(onePacket,onePacketString);
                return;
            }else{
                log.info("大于一个完整的包==="+socketMessageHex);
                onePacketString = socketMessageHex.substring(0,limitNum+1);
//                log.info("一个完整的包==="+onePacketString);
                residueString = socketMessageHex.replace(onePacketString,"");//除去一个完整包剩余的内容
                openPackage(onePacketString,appearNumber(onePacketString,"eb90"));
//                log.info("residueString===="+residueString);

                byte[] residuePacket = PlatformPacketUtil.HexString2Bytes(residueString);
                StringBuilder Str2 = new StringBuilder();
                for (byte byteItem : residuePacket) {
                    Str2.append(String.format("%02x ", byteItem));
                }
                log.info("除去一个完整包剩余的字节数组="+Str2);
                Packet = residueString;
                openPackage(residueString,appearNumber(residueString,"eb90"));
            }
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        //log.info("OnlineSize: " + maps.size());

        StringBuilder Str = new StringBuilder();
        for (byte byteItem : bytes) {
            Str.append(String.format("%02x ", byteItem));
        }
        log.info("机器人发送的的指令是<start>" + Str + "<end>");

        Packet = Packet + Str.toString().replace(" ","").toLowerCase();
        log.info("socketMessageHex:"+Packet);
        /*PacketDealThread packetDealThread = new PacketDealThread(this,socketMessageHex,headNum);
        TaskExecutePool.getInstance().execute(packetDealThread);*/
        int headNum = appearNumber(Packet,"eb90");
        openPackage(Packet,headNum);

//        lookByte(bytes);//看指令
        /*String body = new String(bytes, StandardCharsets.UTF_8);
        log.info("机器人发来的内容="+body);

        log.info("还没处理的Packet="+Packet);
        String zzbds ="^.*<?xml.*";
        Pattern pattern1 = Pattern.compile(zzbds);
        Matcher matcher1 = pattern1.matcher(Packet);
        if (!matcher1.find()){
            Packet = "";
        }
        log.info("处理过的Packet="+Packet);
        String temporaryBody = Packet + body ;//临时
        log.info("准备解析的xml是==="+temporaryBody);
        String temporaryBody2 = temporaryBody.replace("\"UTF-8\"","\'UTF-8\'");//临时
        String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body

        handlingMethod(bytes,finalBody);*/
        ReferenceCountUtil.release(byteBuf);

//        ByteBuf byteBuf = (ByteBuf) msg;
//        byte[] bytes = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(bytes);
//        try {
//            //处理接收到的数据
////            DataDealThread dataDealThread=new DataDealThread(bytes,this,redisTemplate, sendToUpSystemServices);
////            TaskExecutePool.getInstance().execute(dataDealThread);
//            StringBuilder dataByte = new StringBuilder();
//            byte[] data = bytes;
//            for (byte byteitem : data) {
//                dataByte.append(String.format("%02x ", byteitem));
//            }
//            log.info("我在处理数据-字节"+dataByte);
//
//            String dataString = new String(data, "UTF-8");
//            log.info("我在处理数据-字符串"+dataString);
//            //处理毡包问题
//            String zzbds ="^.*<?xml.*";
//            Pattern pattern1 = Pattern.compile(zzbds);
//            Matcher matcher1 = pattern1.matcher(Packet);
//            if (!matcher1.find()){
//                Packet = "";
//            }
//            log.info("--Packet--"+Packet);
//            String temporaryBody = Packet + dataString ;//临时
//            String temporaryBody2 = temporaryBody.replace("\"UTF-8\"","\'UTF-8\'");//临时
//            String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body
//            handlingMethod(data,finalBody);
//
//        } catch (Exception e) {
//            log.info("解析出错"+e);
//        }
//
//        ReferenceCountUtil.release(byteBuf);
    }

    //拆包 解决毡包
    private void handlingMethod(byte[] bytes,String parameter)throws Exception {

        String hua = null;

        if (parameter.contains("开始") || parameter.contains("结束")) {
            hua = parameter;
        } else {
            String bian = parameter.replace("<?xml version='1.0' encoding='UTF-8'?>", "开始<?xml version='1.0' encoding='UTF-8'?>");
            hua = bian.replace("</PatrolHost>", "</PatrolHost>结束");
        }
        Pattern pattern1 = Pattern.compile("(\\<\\?xml version='1.0' encoding='UTF-8'?[^>])([\\s\\S]*?)(</PatrolHost>)");
        Matcher matcher1 = pattern1.matcher(hua);
        String wanZheng = null;
        String shengYu = null;
        if (matcher1.find()) {
            wanZheng = matcher1.group();
            log.info("匹配到一个完整包的内容=" + wanZheng);
            stringToXml(bytes, wanZheng);
            //log.info("--到这了--");
            shengYu = parameter.replace(wanZheng, "");
            handlingMethod(bytes, shengYu);
        } else {
            log.info("不完整啊，小老弟");
            Packet = parameter;
            log.info("不足一个完整的包：" + Packet);
        }

    }
    private void stringToXml(byte[] bytes,String xmlContext)throws Exception{

        Document document = DocumentHelper.parseText(xmlContext);//String转XML
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
        log.info("解析出来的xml是：" + xmlRes);
        byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
        byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
        // Constant.receiveSessionId = sendSessionId;
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
        if (xmlRes.getSendCode() == null) {
            log.info("连接可能断了，等待重连.....");
        } else {
            MessageThread.doProcessMessage(xmlRes, sendSessionId, this, sendToUpSystemServices, analysisUnionTaskFileService, redisTemplate, registerManager);
            // doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
        }

    }
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Constant.receiveSessionId = sendSessionId;
        //解析的xml文件
        if ("251".equals(xmlBaseModel.getType())) {//系统消息
            if ("4".equals(xmlBaseModel.getCommand())) {//响应注册
                log.info("---注册响应---");
                if ("100".equals(xmlBaseModel.getCode())) {
                    //需要重发注册消息
                    this.sendRegister();
                }        else if ("200".equals(xmlBaseModel.getCode())) {
                    //服务端响应 我方开启心跳

                    List<Map<String, Object>> items = xmlBaseModel.getItems();
                    if (items == null || items.size() == 0) {
                        return;
                    }
                    Constant.paramMap.put("sendCode",xmlBaseModel.getSendCode());
                    for (Map<String, Object> item : items) {
                        if(item.get("heart_beat_interval") != null){
                            Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                        }
                        if(item.get("patroldevice_run_interval") != null){
                            Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                        }
                        if(item.get("weather_interval") != null){
                            Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                        }
                        if(item.get("nest_run_interval") != null){
                            Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());//无人机巢运行数据间隔
                        }
                    }
                    //将数据放入redis 做个保存
                    redisTemplate.opsForHash().putAll("upSystemParameter",Constant.paramMap);
                    //心跳线程发心跳
                    HeartBeatThead heartBeatThead = new HeartBeatThead(this, true);
//                    //天气线程发天气
                    WeatherThread weatherThread = new WeatherThread(this,redisTemplate,true,sendToUpSystemServices);
//                    //运行数据
//
                    TaskExecutePool.getInstance().execute(heartBeatThead);
                    TaskExecutePool.getInstance().execute(weatherThread);//江苏要求

                    Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
                    List<XMLBaseModel> list = new ArrayList<>();
                    list.add(xmlBaseModel);
                    robotMap.put("list",list);
                    Result re = Constant.otherServer(robotMap,Constant.ROBOT_TASK_URL);//国网要求
                } else {
                    return;
                }

            }

            if ("3".equals(xmlBaseModel.getCommand())) {//响应心跳
                log.info("--心跳响应--");
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (items == null || items.size() == 0) {
                    return;
                }
                for (Map<String, Object> item : items) {
                    if(item.get("heart_beat_interval") != null){
                        Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                    }
                    if(item.get("patroldevice_run_interval") != null){
                        Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                    }
                    if(item.get("weather_interval") != null){
                        Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                    }
                    if(item.get("nest_run_interval") != null){
                        Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());//无人机巢运行数据间隔
                    }
                }
                //将数据放入redis 做个保存
                redisTemplate.opsForHash().putAll("upSystemParameter",Constant.paramMap);
            }
        }
        if("1".equals(xmlBaseModel.getType()) || "2".equals(xmlBaseModel.getType()) || "3".equals(xmlBaseModel.getType())
                || "4".equals(xmlBaseModel.getType()) || "21".equals(xmlBaseModel.getType()) || "22".equals(xmlBaseModel.getType())){//控制消息
            log.info("--响应控制 控制下发--");
            Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list",list);
            Result re = Constant.otherServer(robotMap,Constant.ROBOT_TASK_URL);//国网要求
            if(re == null){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else if(200 == re.getCode()){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }
            log.info("==控制响应== {}", JSON.toJSONString(re));
        }
        if("20001".equals(xmlBaseModel.getType())
           || "20002".equals(xmlBaseModel.getType())
           || "20003".equals(xmlBaseModel.getType())
           || "20004".equals(xmlBaseModel.getType())
           || "20005".equals(xmlBaseModel.getType())
        ){
            //发给无人机
            log.info("--响应控制 无人机控制下发--");
            Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list",list);
            Result re = Constant.otherServer(robotMap,Constant.ROBOT_TASK_URL);//国网要求
            if(re == null){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else if(200 == re.getCode()){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }
            log.info("==控制响应== {}", JSON.toJSONString(re));
        }
        if ("41".equals(xmlBaseModel.getType())){
            log.info("--响应控制--");
            Result re = new Result();
            Map<String,List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            map.put("list",list);
            //re = Constant.otherServer(map,Constant.TASK_STATE_URL);//江苏要求
            re = Constant.otherServer(map,Constant.ROBOT_TASK_URL);//国网要求
            List<Map<String,Object>> items = new ArrayList<>();
            Map<String,Object> item = new HashMap<>();
            item.put("task_patrolled_id",xmlBaseModel.getCode());
            items.add(item);
            if(re == null){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",items, false);
            }else if(200 == re.getCode()){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",items, false);
            }else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",items, false);
            }
            log.info("==任务控制响应== {}", JSON.toJSONString(re));


        }


        if ("101".equals(xmlBaseModel.getType())){
            log.info("--响应任务--");
            if("1".equals(xmlBaseModel.getCommand())){
                log.info("--响应任务 任务下发--");
                List<Map<String,Object>> list = xmlBaseModel.getItems();
                for (Map<String,Object> item:list){
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String type = item.get("type").toString();
                    String device_list = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(device_list);
                    if(type != null && "1".equals(type)){
                        type = "214";
                    }
                    if(type != null && "2".equals(type)){
                        type = "216";
                    }
                    if(type != null && "3".equals(type)){
                        type = "217";
                    }
                    if(type != null && "4".equals(type)){
                        type = "218";
                    }
                    tCruiseTaskAdd.setType(Integer.valueOf(type));
                    tCruiseTaskAdd.setTaskId(item.get("task_code").toString());
                    tCruiseTaskAdd.setTaskName(item.get("task_name").toString());
                    if(item.get("priority") != null && "".equals(item.get("priority"))){
                        tCruiseTaskAdd.setTaskLevel(Integer.valueOf(item.get("priority").toString()));
                    }else {
                        tCruiseTaskAdd.setTaskLevel(2);
                    }

                    if(item.get("fixed_start_time") != null || "".equals(item.get("fixed_start_time"))){
                        tCruiseTaskAdd.setIfRun(172);
                        StringBuilder stringBuilder = new StringBuilder("0 0");
                        String interval_type = item.get("interval_type").toString();
                        if("1".equals(interval_type)){
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0/"+interval_number+" ?");
                            String cycle_month = item.get("cycle_month").toString();
                            if("".equals(cycle_month) || null == cycle_month){
                                stringBuilder.append(" ?");
                            }else {
                                stringBuilder.append(" "+cycle_month);
                            }
                            String cycle_week = item.get("cycle_week").toString();
                            if("".equals(cycle_week) || null == cycle_week){
                                stringBuilder.append(" ?");
                            }else {
                                stringBuilder.append(" "+cycle_week);
                            }
                        }else if("2".equals(interval_type)){
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0 1/"+interval_number);
                            String cycle_month = item.get("cycle_month").toString();
                            if("".equals(cycle_month) || null == cycle_month){
                                stringBuilder.append(" ?");
                            }else {
                                stringBuilder.append(" "+cycle_month);
                            }
                            String cycle_week = item.get("cycle_week").toString();
                            stringBuilder.append(" ?");
                        }
                        tCruiseTaskAdd.setDateType(stringBuilder.toString());
                    }else {
                        tCruiseTaskAdd.setIfRun(174);
                    }
                    tCruiseTaskAdd.setStartTime(simpleDateFormat.parse(item.get("fixed_start_time").toString()));

//                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
//                    List<TCruiseTaskAdd> taskList = new ArrayList<>();
//                    taskList.add(tCruiseTaskAdd);
//                    map.put("list",taskList);
                    //Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);//江苏要求
                    Map<String,List<XMLBaseModel>> map = new HashMap<>();
                    List<XMLBaseModel> taskList = new ArrayList<>();
                    taskList.add(xmlBaseModel);
                    map.put("list",taskList);
                    Result re = Constant.otherServer(map,Constant.ROBOT_TASK_URL);//国网要求
                    if(re == null){
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
                    }else if(200 == re.getCode()){
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
                    }else {
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
                    }
                    log.info("--响应任务下发-- {}", JSON.toJSONString(re));
                }

            }
        }

        if ("102".equals(xmlBaseModel.getType())) {
            log.info("--响应联动任务--");
            if("1".equals(xmlBaseModel.getCommand())){
                //联动任务
                List<Map<String,Object>> list = xmlBaseModel.getItems();
                for (Map<String,Object> item:list){
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String taskId = item.get("task_code").toString();
                    tCruiseTaskAdd.setTaskId(taskId);
                    String taskName = item.get("task_name").toString();
                    tCruiseTaskAdd.setTaskName(taskName);
                    String taskLevel = item.get("priority").toString();
                    tCruiseTaskAdd.setTaskLevel(Integer.valueOf(taskLevel));
                    String deviceList = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(deviceList);
                    tCruiseTaskAdd.setIfRun(173);
                    tCruiseTaskAdd.setStartTime(new Date());

//                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
//                    List<TCruiseTaskAdd> listTask = new ArrayList<>();
//                    listTask.add(tCruiseTaskAdd);
//                    map.put("list",listTask);
                    //Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);//江苏要求
                    Map<String,List<XMLBaseModel>> map = new HashMap<>();
                    List<XMLBaseModel> taskList = new ArrayList<>();
                    taskList.add(xmlBaseModel);
                    map.put("list",taskList);
                    Result re = Constant.otherServer(map,Constant.ROBOT_TASK_URL);//国网要求
                    List<Map<String,Object>> xmlItems = new ArrayList<>();
                    Map<String,Object> xmlItem = new HashMap<>();
                    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                    if(re == null){
                        xmlItem.put("error_code","3");
                        xmlItem.put("task_patrolled_id",taskId+"_"+simpleDateFormat2.format(new Date()));
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",xmlItems, false);
                    }else if(200 == re.getCode()){
                        xmlItem.put("task_patrolled_id",re.getData());
                        xmlItem.put("error_code","0");
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",xmlItems, false);
                    }else {
                        xmlItem.put("error_code","1");
                        xmlItem.put("task_patrolled_id",taskId+"_"+simpleDateFormat2.format(new Date()));
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",xmlItems, false);
                    }
                    log.info("--联动任务响应-- {}", JSON.toJSONString(re));
                }

            }
        }

        if ("61".equals(xmlBaseModel.getType())){
            try {
                log.info("--模型同步--");
                //1-巡视主机模型 设备模型及设备点位模型文件中应至少包括设备信息及巡视点位信息 A.2.3
                //2-机器人模型 巡视设备模型文件中应至少包括巡视主机、 智能分析主机、 机器人、 无人机、 高清视频、声纹的属性信息 A.2.4
                //3-摄像机模型 同上
                //4-点位模型 同上
                //5-无人机模型  同上
                //6-声纹模型  同上
                //7-任务文件  任务模型 A.2.5
                //8-检修区域配置文件 检修区域模型 A.2.6
                //9-地图文件
                Map<String, Object> map = sendToUpSystemServices.creatModel(xmlBaseModel.getCommand());
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(map);
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list, false);
            }catch (Exception e){
                log.info("模型同步错误"+e);
//                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list);
            }
        }

        if ("81".equals(xmlBaseModel.getType())){
            log.info("--检修区域--");
            Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list",list);
            Result re = Constant.otherServer(robotMap,Constant.MAINTENANCE_URL);//platfrom设置
            Result re2 = Constant.otherServer(robotMap,Constant.ROBOT_TASK_URL);//下发给机器人
            log.info("--响应检修-- {}", JSON.toJSONString(re));
            if(re == null){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else if(200 == re.getCode()){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251","3","200",null, false);
            }

        }

        if ("121".equals(xmlBaseModel.getType())){
            log.info("巡视结果统计查询：{}",xmlBaseModel);
            String startTime="";
            String endTime="";
            if(xmlBaseModel.getItems()!=null && xmlBaseModel.getItems().size()>0) {
                List<Map<String,Object>> itemsList = xmlBaseModel.getItems();
                for(Map<String, Object> item : itemsList) {
                    if(item.get("begin_time") != null){
                        startTime =item.get("begin_time").toString();
                    }
                    if(item.get("end_time") != null){
                        endTime =item.get("end_time").toString();
                    }
                }
            }
//            List<Map<String,Object>> list = sendToUpSystemServices.resultStatistical(xmlBaseModel.getCommand(),startTime,endTime);
            List<Map<String,Object>> list = new ArrayList<>();
            if(list == null){
                sendToUpSystemServices.sendResponse(sendSessionId, "251","4","100",null, false);
            }else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251","4","200",list, false);
            }
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // 解析数据包

    }

    @Override
    public boolean getIsThreadStart() {
        return isThreadStart;
    }

    @Override
    public void setIsThreadStart(Boolean status) {
        isThreadStart = status;
    }

    @Override
    public String getCruise() {
        return Constant.cruise();
    }

    @Override
    public String getServer() {
        return Constant.server();
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return ctx;
    }

}
