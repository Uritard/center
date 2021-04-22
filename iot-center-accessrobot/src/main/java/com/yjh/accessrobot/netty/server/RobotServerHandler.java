package com.yjh.accessrobot.netty.server;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.service.SysLogsService;
import com.yjh.accessrobot.thread.TaskExecutePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
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
        Integer heartNum;
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
    private String redisValue = "content";
    private Integer heartNum = 0;
    private Integer flag2 = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    public static String Packet2 = "";
    public static long sendRobotSessionId = -1L;


    public void setWebSocketUrl(String webSocketUrl) {
        this.webSocketUrl = webSocketUrl;
    }

    private String webSocketUrl;

    public static Map<String, Object> getRobotResultMap() {
        return robotResultMap;
    }

    public static Map<String,Object> robotResultMap =  new HashMap<>();

    public boolean getIsThreadStart() {
        return isThreadStart;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

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
        log.info("Connection is Removed."+ctx.channel().id());
        log.info(ctx.channel().remoteAddress()+" Successful remove");
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

        for(Map.Entry<Object,RobotServerHandler> vo : robotServerHandlerMap.entrySet()){
            log.info("当前的robotServerHandlerMap的key为"+vo.getKey());
            String robotCode = vo.getKey().toString();
            robotServerHandlerMap.remove(robotCode);

            robotService.updateRobotInfo(robotCode,"离线");
            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
            robotStatusMap.put("value","1");//异常
            redisTemplate.opsForHash().putAll("RobotStatus:"+robotCode+":2",robotStatusMap);//update robot Network Status
            flag2 = 0;
        }

        for (Object key : robotServerHandlerMap.keySet()){
            if (this.equals(robotServerHandlerMap.get(key))){
                log.info("当前建立连接的机器人=="+key.toString());
            }
        }
        log.info("channel.isActive(): " + channel.isActive());
        log.info("此时的packet====="+Packet);
        Packet = "";
        Constant.registerCount = 1;

        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strRobotCode == null || strRobotCode.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            robotServerHandlerMap.remove(strRobotCode);
            strRobotCode = strRobotCode.replace("\0", "");
            Object strid2 = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strRobotCode)).get("deviceId");
            if (strid2 != null) {
                log.info("offlineClientId:" + strid2 + "  channel.isActive(): " + channel.isActive());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        log.info("Client "+ctx.channel().remoteAddress() + " connected");
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
    public  void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收机器人发送的指令
        log.info("+++++++++++++++++收到包了+++++++++++++++++");
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        log.info("OnlineSize: " + maps.size());

        StringBuilder Str = new StringBuilder();
        for (byte byteItem : bytes) {
            Str.append(String.format("%02x ", byteItem));
        }
        log.info("机器人发送的的指令是<start>" + Str + "<end>");

        Packet = Packet + Str.toString().replace(" ","");
        log.info("allPacket:"+Packet);
        //拆包2.0
        Packet2 = Packet.replace(""," ");
        byte[] packetByte = PlatformPacketUtil.HexString2Bytes(Packet2);
        byte[] xmlByteLengthByte = new byte[4];
        System.arraycopy(packetByte,19,xmlByteLengthByte,0,4);
        int xmlByteLength = PlatformPacketUtil.bytesToInt1(xmlByteLengthByte,0);//xml的字节长度
        int onePacketLength = 4 + 16 + 16 + 2 + 8 + xmlByteLength * 2 + 4;
        if (Packet.length() >= onePacketLength){
            String onePacket= Packet.substring(0,onePacketLength);
            log.info("onePacket==="+onePacket);
            int headNum = appearNumber(onePacket,"eb90");
            openPackage(onePacket,headNum);
        }

        //拆包3.0
//        Packet2 = Packet.replace(""," ");
//        openPackage2(Packet2);

        //拆包1.1
        /*String body = new String(packetByte, StandardCharsets.UTF_8);
        log.info("Packet2="+body);
        String temporaryBody2 = body.replace("\"UTF-8\"","\'UTF-8\'");//临时
        String finalBody = temporaryBody2.replace("\"1.0\"","\'1.0\'");//最终的body
        boolean res = handlingMethod(bytes,finalBody);
        if (res){
            int headNum = appearNumber(Packet,"eb90");
            openPackage(Packet,headNum);
        }*/

        //拆包1.0
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
        ReferenceCountUtil.release(byteBuf);//引用计数器及时申请释放不再引用的对象
    }
    /*
     *拆包工具1.0
     * */
    private boolean handlingMethod(byte[] bytes,String parameter)throws Exception {
        String xmlTemp = null;

        if (parameter.contains("开始") || parameter.contains("结束")) {
            xmlTemp = parameter;
        } else {
            String changeXML = parameter.replace("<?xml version='1.0' encoding='UTF-8'?>", "开始<?xml version='1.0' encoding='UTF-8'?>");
            xmlTemp = changeXML.replace("</Robot>", "</Robot>结束");
        }
        Pattern pattern = Pattern.compile("(\\<\\?xml version='1.0' encoding='UTF-8'?[^>])([\\s\\S]*?)(</Robot>)");
        Matcher matcher = pattern.matcher(xmlTemp);
        if (matcher.find()) {
            return true;
        }
        return false;
    }
    /*
     * 拆包工具3.0
     * */
    public void openPackage2(String socketMessageHex) throws Exception{
        String onePacketString = null;
        String residueString = null;
        byte[] packetByte = PlatformPacketUtil.HexString2Bytes(socketMessageHex);

        byte[] sendSessionIdByte = new byte[8];
        System.arraycopy(packetByte, 2, sendSessionIdByte, 0, 8);
        long sendRobotNewSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);
        log.info("本身的发送会话序列号为=="+sendRobotSessionId+"新来的发送会话序列号==="+sendRobotNewSessionId);

        byte[] xmlByteLengthByte = new byte[4];
        System.arraycopy(packetByte,19,xmlByteLengthByte,0,4);
        int xmlByteLength = PlatformPacketUtil.bytesToInt1(xmlByteLengthByte,0);//xml的字节长度

        int onePacketLength = 4 + 16 + 16 + 2 + 8 + xmlByteLength * 2 + 4;

        if (Packet.length() == onePacketLength){
            String onePacket= Packet.substring(0,onePacketLength);
            log.info("一个完整的包==="+onePacket);

            byte[] onePacketByte = PlatformPacketUtil.HexString2Bytes(onePacket);
            StringBuilder Str2 = new StringBuilder();
            for (byte byteItem : onePacketByte) {
                Str2.append(String.format("%02x ", byteItem));
            }
            log.info("一个完整的包,准备解析的字节数组="+Str2);

            String body1 = onePacket.substring(46,onePacket.length()-4);
            onePacketString = PlatformPacketUtil.toStringHex(body1);
            log.info("准备解析的xml=="+onePacketString);
            Packet = Packet.replace(onePacket,"");
            stringToXml(onePacketByte,onePacketString);
            return;
        }else if (Packet.length() > onePacketLength ){
            log.info("大于一个完整的包==="+socketMessageHex);
            onePacketString = socketMessageHex.substring(0,onePacketLength);
            residueString = socketMessageHex.replace(onePacketString,"");//除去一个完整包剩余的内容
            openPackage2(onePacketString);

            byte[] residuePacket = PlatformPacketUtil.HexString2Bytes(residueString);
            StringBuilder Str2 = new StringBuilder();
            for (byte byteItem : residuePacket) {
                Str2.append(String.format("%02x ", byteItem));
            }
            log.info("除去一个完整包剩余的字节数组="+Str2);
            Packet = residueString;
            openPackage2(residueString);
        }
    }
    /*
     * 拆包工具2.0
     * */
    public void openPackage (String socketMessageHex,int headNum) throws Exception{
        String onePacketString = null;
        String residueString = null;

        Packet2 = socketMessageHex.replace(""," ");
        byte[] packetByte = PlatformPacketUtil.HexString2Bytes(Packet2);

        byte[] sendSessionIdByte = new byte[8];
        System.arraycopy(packetByte, 2, sendSessionIdByte, 0, 8);
        long sendRobotNewSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);
        log.info("本身的发送会话序列号为=="+sendRobotSessionId+"新来的发送会话序列号==="+sendRobotNewSessionId);

        if(socketMessageHex.startsWith("eb90")
                && headNum >= 2
            /*&& Long.valueOf(sendRobotNewSessionId).equals(Long.valueOf(sendRobotSessionId + 1))*/) {
            //有至少一个完整的包
            int limitNum = socketMessageHex.indexOf("eb90", socketMessageHex.indexOf("eb90") + 1) + 3;//一个包的长度-1
//            sendRobotSessionId = sendRobotNewSessionId;
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
                Packet = Packet.replace(socketMessageHex,"");
                stringToXml(onePacket,onePacketString);
                return;
            }else{
                log.info("大于一个完整的包==="+socketMessageHex);
                onePacketString = socketMessageHex.substring(0,limitNum+1);
                residueString = socketMessageHex.replace(onePacketString,"");//除去一个完整包剩余的内容
                openPackage(onePacketString,appearNumber(onePacketString,"eb90"));

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
    public void stringToXml(byte[] bytes,String xmlContext)throws Exception{
        Document document = DocumentHelper.parseText(xmlContext);//String转XML
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
        byte[] sendSessionIdByte = new byte[8];
        byte[] receiveSessionIdByte = new byte[8];
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
        if (xmlRes.getSendCode() == null) {
            log.info("连接可能断了，等待重连.....");
        } else {
            doProcessMessage(xmlRes, sendSessionId, receiveSessionId);
            log.info("+++++++++++++++++解包完成+++++++++++++++++");
        }
    }
    /*
     * 分析解析后的xml,进行响应处理
     * */
    private void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception  {
        log.info("robotServerHandlerMap==="+robotServerHandlerMap);
        Map<String, String> platformServerMap = redisTemplate.opsForHash().entries("t_sys_param:PlatformServer");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> heartbeatIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:heartbeatInterval");
        Map<String, String> runDataIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:runDataInterval");
        Map<String, String> weatherDataIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:weatherDataInterval");
        Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");


        try {
            if ("251".equals(xmlBaseModel.getType())){
                switch (xmlBaseModel.getType()+xmlBaseModel.getCommand()){
                    //注册指令(发送响应)
                    case "2511":
                        log.info("巡视主机收到注册指令了,这是第"+Constant.registerCount+"次");
                        Constant.registerCount++;
                        List<Map<String, Object>> itemsList = new ArrayList<>();
                        Map<String, Object> items = new HashMap<>();
                        String code = "";
                        if (allRobotCodeMap.containsValue(xmlBaseModel.getSendCode())){
                            code = "200";//success
                            log.info("缓存有,可以注册");
                        }else {
                            List<String> robotCodeList = StaticContextAccessor.getBean(RobotService.class).selectAllRobotCode();
                            if (robotCodeList.contains(xmlBaseModel.getSendCode())){
                                code = "200";
                                log.info("缓存无，表中有，可以注册");
                            }else {
                                code = "400";//refuse
                                log.info("缓存无，表中无，不可以注册");
                            }
                        }
                        items.put("heart_beat_interval", heartbeatIntervalMap.get(redisValue));//心跳间隔
                        items.put("robot_run_interval", runDataIntervalMap.get(redisValue));//机器人运行数据间隔
                        items.put("weather_interval", weatherDataIntervalMap.get(redisValue));//微气象数据间隔
                        itemsList.add(items);

                        XMLBaseModel xmlBaseModelTemp = new XMLBaseModel()
                                .setSendCode(platformServerMap.get(redisValue))
                                .setReceiveCode(xmlBaseModel.getSendCode())
                                .setType("251")
                                .setCode(code)
                                .setCommand("4")
                                .setTime(sdf.format(new Date()))
                                .setItems(itemsList);
                        String registerXmlString = PlatformXMLUtil.generateXml(xmlBaseModelTemp);//生成xml
                        byte[] registerProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, registerXmlString);
                        sendHeartBeat(registerProtocol,xmlBaseModel.getSendCode());

                        if (Objects.nonNull(robotServerHandlerMap.get(xmlBaseModel.getSendCode()))) {
                            robotServerHandlerMap.get(xmlBaseModel.getSendCode()).ctx.close();
                        }

                        robotServerHandlerMap.put(xmlBaseModel.getSendCode(), this);
                        //Start heatBreakDealThread
                        HeartBreakDealThread dataDealThread = new HeartBreakDealThread(this, xmlBaseModel.getSendCode(),redisTemplate,isThreadStart,sendSessionId,receiveSessionId);
                        Thread thread = new Thread(dataDealThread);
                        thread.setDaemon(true);
                        thread.start();
                        break;
                    //心跳指令(发送响应)
                    case "2512":
                        log.info("+++++++++++++++++巡视主机收到心跳指令了+++++++++++++++++");
                        heartNum = 0;
                        String robotCode = xmlBaseModel.getSendCode();
                        if (allRobotCodeMap.containsValue(robotCode)){
                            log.info("缓存有,发送心跳响应");
                            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                            heartBeatSuccessAfter(robotCode,sendSessionId,robotStatusMap);
                        }else {
                            List<String> robotCodeList = robotService.selectAllRobotCode();
                            if (robotCodeList.contains(robotCode)){
                                log.info("缓存无,表中有,发送心跳相应");
                                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                                heartBeatSuccessAfter(robotCode,sendSessionId,robotStatusMap);
                            }else {
                                log.info("缓存无,表中无,断开连接");
                                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                                heartBeatFailAfter(robotCode,robotStatusMap);
                            }
                        }
                        break;
                    //模型同步指令and任务控制指令(接收响应)
                    case "2514":
                        if (xmlBaseModel.getItems().get(0).size() == 1) {
                            //Deal with task control
                            String taskId = xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString();
                            log.info("机器人收到任务控制指令了,这是机器人响应的巡视任务执行Id==="+taskId);
                        }else if (xmlBaseModel.getItems().get(0).size() == 2){
                            log.info("机器人收到模型指令了,这是机器人的响应");
                            //Deal with synchronous model
                            String deviceFile = xmlBaseModel.getItems().get(0).get("device_file_path").toString();
                            String robotFile = xmlBaseModel.getItems().get(0).get("robot_file_path").toString();
                            XMLBaseModel deviceModel = getXmlMessage(filePathMap.get(redisValue) + "/" +deviceFile);
                            List<Map<String,Object>> deviceMap = deviceModel.getItems();
                            XMLBaseModel robotModel = getXmlMessage(filePathMap.get(redisValue) + "/" +robotFile);
                            List<Map<String,Object>> robotMap = robotModel.getItems();
                            log.info("deviceMap==="+deviceMap+",robotMap==="+robotMap);
                            robotService.robotFileIntoDB(deviceMap,robotMap,xmlBaseModel);

                            robotService.uploadFile(deviceFile,deviceFile);//设备模型
                            robotService.uploadFile(robotFile,robotFile);//机器人模型
                            robotService.upToCruise(xmlBaseModel);//国网要求
                        }else if (xmlBaseModel.getItems().get(0).size() == 0){
                            log.info("机器人收到检修区域指令了,这是机器人的响应");
                            //Deal with the maintenance area was issued successfully
                            robotService.receivingResponse(xmlBaseModel,receiveSessionId);
                        }
                        break;
                    //任务下发指令and控制指令(接收响应)
                    case "2513":
                        log.info("机器人收到下发任务指令/控制指令了,这是机器人的响应");
                        //Deal with task issue/control
                        robotService.receivingResponse(xmlBaseModel,receiveSessionId);
                        break;
                    default:
                        break;
                }
            }else {
                switch (xmlBaseModel.getType()){
                    //机器人状态数据(接收并发送响应)
                    case "1":
                        log.info("+++++++++++++++++巡视主机收到机器人状态数据了+++++++++++++++++");
                        //Deal with robot status data
                        List<Map<String,String>> robotStatusList = new ArrayList<>();
                        xmlBaseModel.getItems().forEach(res->{
                            Map<String, String> robotStatusMap = new HashMap<>();
                            robotStatusMap.put("robotName",res.get("robot_name").toString());
                            robotStatusMap.put("robotCode",xmlBaseModel.getSendCode());
                            robotStatusMap.put("time",res.get("time").toString());
                            robotStatusMap.put("type",res.get("type").toString());
                            robotStatusMap.put("value",res.get("value").toString());
                            robotStatusMap.put("valueUnit",res.get("value_unit").toString());
                            robotStatusMap.put("unit",res.get("unit").toString());
                            robotStatusList.add(robotStatusMap);

                            robotService.upToCruise(xmlBaseModel);//国网要求
                        });
                        log.info("机器人状态数据是："+robotStatusList);
                        //放缓存
                        for (int i = 0; i < robotStatusList.size(); i++) {
                            redisTemplate.opsForHash().putAll("RobotStatus:"+xmlBaseModel.getSendCode()+":"+ robotStatusList.get(i).get("type"), robotStatusList.get(i));
                        }
                        String statusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, statusXmlString);
                        send(ctx, statusProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");

                        break;
                    //机器人运行数据(接收并发送响应)
                    case "2":
                        log.info("+++++++++++++++++巡视主机收到机器人运行数据了+++++++++++++++++");
                        //Deal with robot operation data
                        List<Map<String,String>> robotOperationList = new ArrayList<>();
                        xmlBaseModel.getItems().forEach(res->{
                            Map<String,String> robotOperationMap = new HashMap<>();
                            robotOperationMap.put("robotName",res.get("robot_name").toString());
                            robotOperationMap.put("robotCode",res.get("robot_code").toString());
                            robotOperationMap.put("time",res.get("time").toString());
                            robotOperationMap.put("type",res.get("type").toString());
                            robotOperationMap.put("value",res.get("value").toString());
                            robotOperationMap.put("valueUnit",res.get("value_unit").toString());
                            robotOperationMap.put("unit",res.get("unit").toString());
                            robotOperationList.add(robotOperationMap);

                        });
                        //放缓存
                        for (int i = 0; i < robotOperationList.size(); i++) {
                            redisTemplate.opsForHash().putAll("RobotOperation:"+xmlBaseModel.getSendCode()+":"+ robotOperationList.get(i).get("type"), robotOperationList.get(i));
                        }
                        String operationXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, operationXmlString);
                        send(ctx, operationProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");
                        robotService.upToCruise(xmlBaseModel);//国网要求

                        break;
                    //机器人坐标(接收并发送响应)
                    case "3":
                        log.info("+++++++++++++++++巡视主机收到机器人坐标数据了+++++++++++++++++");
                        //Deal with robot coordinates data
                        List<Map<String,String>> robotCoordinateList = new ArrayList<>();
                        xmlBaseModel.getItems().forEach(res-> {
                            Map<String, String> robotCoordinateMap = new HashMap<>();
                            robotCoordinateMap.put("robotName", res.get("robot_name").toString());
                            robotCoordinateMap.put("filePath", res.get("file_path").toString());
                            robotService.uploadFile(res.get("file_path").toString(),res.get("file_path").toString());
                            robotCoordinateMap.put("robotCode", xmlBaseModel.getSendCode());
                            robotCoordinateMap.put("time",res.get("time").toString());
                            robotCoordinateMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
                            robotCoordinateMap.put("coordinateGeography", res.get("coordinate_geography").toString());
                            robotCoordinateList.add(robotCoordinateMap);
                        });
                        //放缓存
                        for (int i = 0; i < robotCoordinateList.size(); i++) {
                            redisTemplate.opsForHash().putAll("RobotCoordinate:"+xmlBaseModel.getSendCode(), robotCoordinateList.get(i));
                        }
                        String coordinateXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] coordinateProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, coordinateXmlString);
                        send(ctx, coordinateProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");
                        robotService.upToCruise(xmlBaseModel);//国网要求
                        break;
                    //机器人巡视路线(接收并发送响应)
                    case "4":
                        log.info("+++++++++++++++++巡视主机收到机器人巡视路线数据了+++++++++++++++++");
                        //Deal with robot cruise road data
                        List<Map<String,String>> robotRoadList = new ArrayList<>();
                        xmlBaseModel.getItems().forEach(res-> {
                            Map<String, String> robotRoadMap = new HashMap<>();
                            robotRoadMap.put("robotName", res.get("robot_name").toString());
                            String filePath = res.get("file_path").toString();
                            robotService.uploadFile(filePath,filePath);
                            String splitArray[] = filePath.split("/");
                            String fileName = splitArray[splitArray.length - 1];
                            log.info("巡检路线图片名称=="+fileName);
                            String taskId = splitArray[splitArray.length - 3];
                            /*
                            将ftp服务器上的文件复制到开发环境
                            * */
                            String temporaryPath = filePathMap.get(redisValue) + "/" +filePath;//文件在ftp服务器上的绝对路径
                            log.info("temporaryPath是==="+temporaryPath);

                            //开发环境图片相对路径文件目录
                            String developRelativeUrl =  relativeImgMap.get(redisValue) +  "/"+ todayTime+ "/"+ taskId + "/Road";
                            //开发环境图片绝对路径文件目录
                            String developAbsoluteUrl = absoluteImgMap.get(redisValue) + "/"+ todayTime+ "/"+taskId + "/Road";
                            log.info("developAbsoluteUrl是==="+developAbsoluteUrl);
                            File f=new File(developAbsoluteUrl);
                            if (!f.exists()){
                                f.setWritable(true, false);
                                f.mkdirs();
                            }

                            try {
                                String url = "cp " + temporaryPath + " " + developAbsoluteUrl;
                                Runtime.getRuntime().exec(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            robotRoadMap.put("relativePath",developRelativeUrl + "/" +fileName);//相对路径
                            robotRoadMap.put("absolutePath",developAbsoluteUrl + "/"+fileName);//绝对路径
                            robotRoadMap.put("robotCode", xmlBaseModel.getSendCode());
                            robotRoadMap.put("time",res.get("time").toString());
                            robotRoadMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
                            robotRoadMap.put("coordinateGeography", res.get("coordinate_geography").toString());
                            robotRoadList.add(robotRoadMap);
                        });
                        //放缓存
                        for (int i = 0; i < robotRoadList.size(); i++) {
                            redisTemplate.opsForHash().putAll("RobotRoad:"+xmlBaseModel.getSendCode(), robotRoadList.get(i));
                        }
                        String roadXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] roadProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, roadXmlString);
                        send(ctx, roadProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");

                        robotService.upToCruise(xmlBaseModel);//国网要求
                        break;
                    //机器人异常告警数据(接收并发送响应)
                    case "5":
                        log.info("+++++++++++++++++巡视主机收到机器人异常告警数据了+++++++++++++++++");
                        //Deal with robot alarm data
                        Map<String, String> robotAlarmMap = new HashMap<>();
                        robotAlarmMap.put("robotName",xmlBaseModel.getItems().get(0).get("robot_name").toString());
                        robotAlarmMap.put("robotCode",xmlBaseModel.getSendCode());
                        robotAlarmMap.put("time",xmlBaseModel.getItems().get(0).get("time").toString());
                        robotAlarmMap.put("content",xmlBaseModel.getItems().get(0).get(redisValue).toString());

                        //Start alarmResultDealThread
                        AlarmResultDealThread alarmResultDealThread = new AlarmResultDealThread(robotAlarmMap,isThreadStart,this);
                        TaskExecutePool.getInstance().execute(alarmResultDealThread);

                        String alarmXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] alarmProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, alarmXmlString);
                        send(ctx, alarmProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");


                        //xmlBaseModel.setType("62");
                        robotService.upToCruise(xmlBaseModel);//国网要求

                        break;
                    //微气象数据(接收并发送响应)
                    case "21":
                        log.info("+++++++++++++++++巡视主机收到微气象数据了+++++++++++++++++");
                        //Deal with robot micro climate data
                        List<Map<String,String>> weatherList = new ArrayList<>();
                        Map<String,String> info = new HashMap<>();
                        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
                        xmlBaseModel.getItems().forEach(res-> {
                            Map<String, String> weatherMap = new HashMap<>();
                            weatherMap.put("robotName", res.get("robot_name").toString());
                            weatherMap.put("robotCode", xmlBaseModel.getSendCode());
                            weatherMap.put("time", res.get("time").toString());
                            weatherMap.put("type", res.get("type").toString());
                            weatherMap.put("value", res.get("value").toString());
                            weatherMap.put("valueUnit", res.get("value_unit").toString());
                            weatherMap.put("unit", res.get("unit").toString());
                            weatherList.add(weatherMap);
                            //1=温度 2=湿度 3=风速 4=大气压  5=降雨量 6=风向
                            if("1".equals(weatherMap.get("type"))){
                                info.put("temperature",decimalFormat.format(Double.valueOf(weatherMap.get("value"))).toString());
                                info.put("temperatureUnit","℃");
                            }
                            if("2".equals(weatherMap.get("type"))){
                                info.put("humidity",decimalFormat.format(Double.valueOf(weatherMap.get("value"))).toString());
                                info.put("humidityUnit","%");
                            }
                            if("3".equals(weatherMap.get("type"))){
                                info.put("windSpeed",decimalFormat.format(Double.valueOf(weatherMap.get("value"))).toString());
                                info.put("windSpeedUnit","m/s");
                            }
                            if("4".equals(weatherMap.get("type"))){
                                info.put("airPressure",decimalFormat.format(Double.valueOf(weatherMap.get("value"))/10).toString());
                                info.put("airPressureUnit","kPa");
                            }
                            if("5".equals(weatherMap.get("type"))){
                                info.put("precipitation",decimalFormat.format(Double.valueOf(weatherMap.get("value"))).toString());
                                info.put("precipitationUnit","mm");
                            }
                            if("6".equals(weatherMap.get("type"))){
                                if("".equals(weatherMap.get("value")) || null==weatherMap.get("value")){
                                    info.put("windDirection","--");
                                }else {
                                    info.put("windDirection",weatherMap.get("value").toString());
                                }

                                //info.put("precipitationUnit","mm");
                            }
                        });
                        //放缓存
                        for (int i = 0; i < weatherList.size(); i++) {
                            redisTemplate.opsForHash().putAll("RobotWeather:"+xmlBaseModel.getSendCode()+":"+ weatherList.get(i).get("type"), weatherList.get(i));
                        }
                        Constant.weatherServer(info,Constant.WEATHER_URL);
                        String weatherXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] weatherProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, weatherXmlString);
                        send(ctx, weatherProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");

                        //robotService.upToCruise(xmlBaseModel);//国网要求
                        break;
                    //任务状态数据(接收并发送响应)
                    case "41":
                        log.info("+++++++++++++++++巡视主机收到任务状态数据了+++++++++++++++++");
                        //Deal with robot task status data
                        Map<String, Object> taskStatusMap = new HashMap<>();
                        taskStatusMap.put("taskPatrolled_id", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
                        taskStatusMap.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
                        taskStatusMap.put("taskCode", xmlBaseModel.getItems().get(0).get("task_code").toString());
                        taskStatusMap.put("taskState", xmlBaseModel.getItems().get(0).get("task_state").toString());
                        taskStatusMap.put("planStartTime", xmlBaseModel.getItems().get(0).get("plan_start_time"));
                        String startTime =  xmlBaseModel.getItems().get(0).get("start_time").toString();
                        taskStatusMap.put("startTime", sdf.format(sdf.parse(startTime)));
                        taskStatusMap.put("taskProgress", xmlBaseModel.getItems().get(0).get("task_progress").toString());
                        taskStatusMap.put("taskEstimatedTime", xmlBaseModel.getItems().get(0).get("task_estimated_time").toString());
                        taskStatusMap.put("description", xmlBaseModel.getItems().get(0).get("description").toString());

                        //判断任务是否属于机器人本体任务
                        Long robotId = robotService.selectIsRobotTask(xmlBaseModel.getItems().get(0).get("task_code").toString());
                        if (Objects.nonNull(robotId)){
                            Map<String,String> jasonMap=new HashMap<>();
                            jasonMap.put("type","newTask");
                            jasonMap.put("taskId",xmlBaseModel.getItems().get(0).get("task_code").toString());
                            String json= JSON.toJSONString(jasonMap);
                            Constant.postUrl(webSocketUrl,json);
                        }

                        Map<String, Object> listMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+xmlBaseModel.getSendCode()
                                +":"+xmlBaseModel.getItems().get(0).get("task_code").toString());
                        taskStatusMap.put("instanceList",listMap.get("instanceIdList"));
                        log.info("taskStatusMap=="+taskStatusMap);
                        redisTemplate.opsForHash().putAll("RobotTaskStatus:"+xmlBaseModel.getSendCode()
                                +":"+xmlBaseModel.getItems().get(0).get("task_code").toString(), taskStatusMap);

                        String taskStatusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, taskStatusXmlString);
                        send(ctx, taskStatusProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");

                        robotService.upToCruise(xmlBaseModel);//国网要求
                        break;
                    //巡视结果
                    case "61":
                        log.info("+++++++++++++++++巡视主机收到巡视结果了+++++++++++++++++");
                        //Deal with robot task result data
                        Map<String, String> cruiseResultMap = new HashMap<>();
                        cruiseResultMap.put("robotCode",xmlBaseModel.getSendCode());
                        cruiseResultMap.put("taskName",xmlBaseModel.getItems().get(0).get("task_name").toString());
                        cruiseResultMap.put("taskCode",xmlBaseModel.getItems().get(0).get("task_code").toString());
                        cruiseResultMap.put("deviceName",xmlBaseModel.getItems().get(0).get("device_name").toString());
                        cruiseResultMap.put("deviceId",xmlBaseModel.getItems().get(0).get("device_id").toString());
                        cruiseResultMap.put("value",xmlBaseModel.getItems().get(0).get("value").toString());
                        cruiseResultMap.put("valueUnit",xmlBaseModel.getItems().get(0).get("value_unit").toString());
                        cruiseResultMap.put("unit",xmlBaseModel.getItems().get(0).get("unit").toString());
                        cruiseResultMap.put("time",xmlBaseModel.getItems().get(0).get("time").toString());
                        cruiseResultMap.put("recognitionType",xmlBaseModel.getItems().get(0).get("recognition_type").toString());
                        cruiseResultMap.put("fileType",xmlBaseModel.getItems().get(0).get("file_type").toString());
                        cruiseResultMap.put("rectangle",xmlBaseModel.getItems().get(0).get("rectangle").toString());
                        cruiseResultMap.put("taskPatrolledId",xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
                        if (Objects.nonNull(xmlBaseModel.getItems().get(0).get("valid"))){
                            cruiseResultMap.put("valid",xmlBaseModel.getItems().get(0).get("valid").toString());
                        }
                        String developAbsoluteUrl = absoluteImgMap.get(redisValue) + "/"+ todayTime  + "/"+ xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";
                        String developRelativeUrl = relativeImgMap.get(redisValue)+ "/"+ todayTime  + "/"+ xmlBaseModel.getItems().get(0).get("task_code").toString() + "/";
                        //可见光结果、红外fir、音频wav
                        String ftpFilePath = xmlBaseModel.getItems().get(0).get("file_path").toString();
                        robotService.uploadFile(ftpFilePath,ftpFilePath);
                        String sArray[] = ftpFilePath.split("/");
                        String ftpFileName = sArray[sArray.length - 1];
                        String temporaryFilePath = filePathMap.get(redisValue) + "/" +ftpFilePath;
                        log.info("temporaryFilePath==="+temporaryFilePath);

                        //红外原图
                        if (xmlBaseModel.getItems().get(0).containsKey("origin_file_path")){
                            String ftpInfraredOriginPath = xmlBaseModel.getItems().get(0).get("origin_file_path").toString();//红外原图
                            String sArray2[] = ftpInfraredOriginPath.split("/");
                            String ftpInfraredOriginName = sArray2[sArray2.length - 1];//红外原图名称
                            String temporaryInfraredOriginPath = filePathMap.get(redisValue) + "/" +ftpInfraredOriginPath;
                            copyFileToDevelop(temporaryInfraredOriginPath,developAbsoluteUrl + "InfraredOrigin");//拷贝原图
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "InfraredOrigin" + "/" + ftpInfraredOriginName);
                        }
                        String ftpOriginPath = null;
                        //可见光原图、音频和红外结果
                        if (xmlBaseModel.getItems().get(0).containsKey("origin_file_result_path")){
                            ftpOriginPath = xmlBaseModel.getItems().get(0).get("origin_file_result_path").toString();//可见光原图、红外结果
                        }else {
                            ftpOriginPath = xmlBaseModel.getItems().get(0).get("file_path").toString();//可见光原图、音频
                        }
                        String sArray2[] = ftpOriginPath.split("/");
                        String ftpOriginName = sArray2[sArray2.length - 1];//原图文件名称
                        String temporaryOriginPath = filePathMap.get(redisValue) + "/" +ftpOriginPath;
                        log.info("temporaryOriginPath==="+temporaryOriginPath);

                        String fileType = xmlBaseModel.getItems().get(0).get("file_type").toString();
                        if ("1".equals(fileType)){//红外
                            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "Infrared");//拷贝巡视结果图
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "FIR");//拷贝fir
                            cruiseResultMap.put("relativePath",developRelativeUrl + "Infrared" + "/" + ftpOriginName);
                            cruiseResultMap.put("resultPic", developRelativeUrl + "FIR" + "/" +ftpFileName);
                        }else if ("2".equals(fileType)){//可见光
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "CCD");//拷贝巡视结果图
                            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "BigImg");//拷贝原图
                            cruiseResultMap.put("relativePath",developRelativeUrl + "CCD" + "/" + ftpFileName);
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl+ "BigImg" + "/"+ ftpOriginName);
                        }else if ("3".equals(fileType)){//音频
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "Audio");//拷贝巡视结果图
                            cruiseResultMap.put("relativePath",developRelativeUrl + "Audio" + "/" +ftpFileName);
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "Audio" + "/" + ftpOriginName);
                        }
                        /*
                         * 新版的图片处理
                         * */
                        /*String fileType = xmlBaseModel.getItems().get(0).get("file_type").toString();
                        String ftpFilePath = xmlBaseModel.getItems().get(0).get("file_path").toString();
                        String sArray[] = ftpFilePath.split("/");
                        String ftpFileName = sArray[sArray.length - 1];//巡视结果文件名称
                        String temporaryFilePath = filePathMap.get(redisValue) + "/" +ftpFilePath;

                        String ftpOriginPath = null;
                        if (xmlBaseModel.getItems().get(0).containsKey("origin_file_path")){
                            ftpOriginPath = xmlBaseModel.getItems().get(0).get("origin_file_path").toString();
                        }else {
                            ftpOriginPath = xmlBaseModel.getItems().get(0).get("file_path").toString();
                        }
                        String sArray2[] = ftpOriginPath.split("/");
                        String ftpOriginName = sArray2[sArray2.length - 1];//原图文件名称
                        String temporaryOriginPath = filePathMap.get(redisValue) + "/" +ftpOriginPath;
                        log.info("temporaryOriginPath==="+temporaryOriginPath);

                        if ("1".equals(fileType)){//红外
                            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "Infrared");//拷贝原图
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "FIR");//拷贝巡视结果图
                            cruiseResultMap.put("relativePath",developRelativeUrl + "Infrared" + "/" + ftpOriginName);
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "Infrared" + "/" + ftpOriginName);
                            cruiseResultMap.put("resultPic", developRelativeUrl + "FIR" + "/" +ftpFileName);
                        }else if ("2".equals(fileType)){//可见光
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "CCD");//拷贝巡视结果图
                            copyFileToDevelop(temporaryOriginPath,developAbsoluteUrl + "BigImg");//拷贝原图
                            cruiseResultMap.put("relativePath",developRelativeUrl + "CCD" + "/" + ftpFileName);
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl+ "BigImg" + "/"+ ftpOriginName);
                        }else if ("3".equals(fileType)){//音频
                            copyFileToDevelop(temporaryFilePath,developAbsoluteUrl + "Audio");//拷贝巡视结果图
                            cruiseResultMap.put("relativePath",developRelativeUrl + "Audio" + "/" +ftpFileName);
                            cruiseResultMap.put("absolutePath",developAbsoluteUrl + "Audio" + "/" + ftpOriginName);
                        }*/

                        log.info("机器人巡视结果数据是："+cruiseResultMap);

                        //判断结果是否产生告警,只判断红外和可见光
                        Map<String, String> cResultMap = new HashMap<>();
                        cResultMap.put("robotCode",xmlBaseModel.getSendCode());
                        cResultMap.put("taskCode",xmlBaseModel.getItems().get(0).get("task_code").toString());
                        cResultMap.put("deviceId",xmlBaseModel.getItems().get(0).get("device_id").toString());
                        cResultMap.put("value",xmlBaseModel.getItems().get(0).get("value").toString());
                        if ("1".equals(fileType)){
                            cResultMap.put("relativePath",developRelativeUrl + "Infrared" + "/" + ftpOriginName);//相对路径
                        }else if ( "2".equals(fileType)){
                            cResultMap.put("relativePath",developRelativeUrl + "CCD" + "/" + ftpFileName);//相对路径
                        }
                        IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(cResultMap,redisTemplate,webSocketUrl);
                        TaskExecutePool.getInstance().execute(isWarnAfterCruiseThread);
                        //Start CruiseResultDealThread
                        CruiseResultDealThread cruiseResultDealThread = new CruiseResultDealThread(cruiseResultMap,redisTemplate,webSocketUrl);
                        TaskExecutePool.getInstance().execute(cruiseResultDealThread);

                        String cruiseResultXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,xmlBaseModel.getSendCode()));
                        byte[] cruiseResultProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseResultXmlString);
                        send(ctx, cruiseResultProtocol,xmlBaseModel.getSendCode());
                        log.info("巡视主机给机器人响应了");

                    {
                        //巡视点结果上报站端
                        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("Robot_SPAndIN_Info:"+xmlBaseModel.getSendCode()+":"+xmlBaseModel.getCode());
                        String instanceId = redisInfoMap.get("instanceId");
                        Map<String,String> mapForGet = redisTemplate.opsForHash().entries("t_cruise_task_result:"+xmlBaseModel.getCode() + ":" + instanceId);
                        xmlBaseModel.getItems().get(0).put("material_id",mapForGet.get("realCode"));
                        xmlBaseModel.getItems().get(0).put("data_type",mapForGet.get("0x02"));
                        xmlBaseModel.getItems().get(0).put("patroldevice_code",instanceId);
                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                        //                    xmlBaseModel.getItems().get(0).put("taskPatrolledId",mapForGet.get("taskId")+"_"+simpleDateFormat2.format(mapForGet.get("cruiseTime")));
                        xmlBaseModel.getItems().get(0).remove("robot_code");
                        List<XMLBaseModel> list = new ArrayList<>();
                        list.add(xmlBaseModel);
                        Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
                        cruiseResult.put("list",list);
                        log.info("信息上报：-"+cruiseResult);
                        //Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
                    }

                    robotService.upToCruise(xmlBaseModel);//国网要求
                    break;
                    case "71":
                        log.info("巡视主机收到机器人站端的任务了");
                        //Deal with robot task data
                        String taskFile = xmlBaseModel.getItems().get(0).get("task_file_path").toString();
                        XMLBaseModel taskModel = getXmlMessage(filePathMap.get(redisValue) + "/" +taskFile);
                        List<Map<String,Object>> taskModelMapList = taskModel.getItems();
                        log.info("taskModelItemsMap是："+taskModelMapList);
                        robotService.robotTaskIntoDB(taskModelMapList,xmlBaseModel);
                        break;
                    default:
                        break;
                }
            }
        }catch (Exception e){
            log.error("处理机器人响应消息错误:"+e.getMessage());
            String taskStatusXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(false,xmlBaseModel.getSendCode()));
            byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, taskStatusXmlString);
            send(ctx, taskStatusProtocol,xmlBaseModel.getSendCode());
            log.info("巡视主机给机器人响应了");
        }
    }
    /*
    将ftp服务器上的文件复制到开发环境
    * */
    void copyFileToDevelop(String source,String aim){
        File ff=new File(aim);
        if (!ff.exists()){
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " "+aim;
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.getMessage();
        }
    }
    /*
     * 查询缓存中所有机器人编码
     * */
    private List<String> nowRobotCode(){
        Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");

        Iterator<String> iterator = allRobotCodeMap.keySet().iterator();
        List<String> allRobotCodeList = new ArrayList<>();
        while(iterator.hasNext()){
            String key=iterator.next();
            String value = allRobotCodeMap.get(key);
            allRobotCodeList.add(value);
        }
        return allRobotCodeList;
    }
    /*
     * 快速创建command=3 的消息体
     * */
    private XMLBaseModel sendMessageForCommandThree(boolean flag,String sendCode){
        XMLBaseModel xmlBaseModelEmpty = new XMLBaseModel();
        xmlBaseModelEmpty.setCommand("3");
        xmlBaseModelEmpty.setTime(sdf.format(new Date()));
        xmlBaseModelEmpty.setType("251");
        xmlBaseModelEmpty.setCode(flag?"200":"500");
        xmlBaseModelEmpty.setSendCode("Server01");
        xmlBaseModelEmpty.setReceiveCode(sendCode);
        return xmlBaseModelEmpty;
    }
    /*
     * 通过文件路径解析XML
     * */
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }
    /*
     * 接收到心跳后续判断
     * */
    void procSend(String robotCode, Map<String, String> robotStatusMap,long sendSessionId,long receiveSessionId ) {
        heartNum ++;
        log.info("heartNum==="+heartNum+"      "+ctx.channel().id());
        if (heartNum > 3){
            heartBeatFailAfter(robotCode,robotStatusMap);
        }
        /*Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");

        if (allRobotCodeMap.containsValue(robotCode)){
            log.info("缓存有,发送心跳响应");
            heartBeatSuccessAfter(robotCode,sendSessionId,receiveSessionId);
        }else {
            List<String> robotCodeList = robotService.selectAllRobotCode();
            if (robotCodeList.contains(robotCode)){
                log.info("缓存无,表中有,发送心跳相应");
                heartBeatSuccessAfter(robotCode,sendSessionId,receiveSessionId);
            }else {
                log.info("缓存无,表中无,断开连接");
                heartBeatFailAfter(robotCode,robotStatusMap);
            }
        }*/
    }
    /*
     * 成功收到心跳指令,sendHeartResponse && updateRobotStatus
     * */
    void heartBeatSuccessAfter(String robotCode,long sendSessionId,Map<String, String> robotStatusMap){
        String heartXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true,robotCode));
        byte[] heartProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, heartXmlString);
        send(ctx, heartProtocol,robotCode);
        flag2 ++;
        log.info("成功收到心跳flag2的值==="+flag2);
        //为了等待客户端和服务端连接稳定,收到三次以上再修改
        if (flag2 > 2){
            robotService.updateRobotInfo(robotCode,"在线");
            robotStatusMap.put("value","0");//正常
            redisTemplate.opsForHash().putAll("RobotStatus:"+robotCode+":2",robotStatusMap);//update robot Network Status
            flag2 = 0;
        }
    }
    /*
     * 没有收到心跳指令,removeLink && updateRobotStatus
     * */
    void heartBeatFailAfter(String robotCode,Map<String, String> robotStatusMap){
        removeLink(robotCode);
        log.info("没有收到心跳flag2的值==="+flag2);
        robotService.updateRobotInfo(robotCode,"离线");
        robotStatusMap.put("value","1");//异常
        redisTemplate.opsForHash().putAll("RobotStatus:"+robotCode+":2",robotStatusMap);//update robot Network Status
        flag2 = 0;
    }
    /*
     * 3、2、1走你
     * */
    private void send(ChannelHandlerContext ctx, byte[] bytes,String strRobotCode) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder sendToRobotStr = new StringBuilder();
        for (byte byteItem : bytes) {
            sendToRobotStr.append(String.format("%02x ", byteItem));
        }
        log.info("commandSendToRobot:" + sendToRobotStr + " : " + strRobotCode);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendToRobotSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }
    /*
     * 准备发送指令
     * */
    public void sendHeartBeat (byte[] protocolBytes,String robotCode) {
        send(ctx,protocolBytes,robotCode);
    }
    /*
     * 获取指定字符串出现的次数
     * */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }
    /*
     *看指令的工具
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
        for (byte byteItem : startIdentifierByte) {
            startStr.append(String.format("%02x ", byteItem));
        }
        log.info("起始标志符指令为："+startStr);
        //2.发送会话序列号
        System.arraycopy(bytes,2,sendSessionIdByte,0,8);
        StringBuilder sendSessionIdStr = new StringBuilder();
        for (byte byteItem : sendSessionIdByte) {
            sendSessionIdStr.append(String.format("%02x ", byteItem));
        }
        log.info("发送会话序列号指令为："+sendSessionIdStr);
        long ssi = PlatformPacketUtil.reserve(sendSessionIdByte);//发送会话序列号的字节长度
        log.info("发送会话序列号为："+ssi);
        //3.接收会话序列号
        System.arraycopy(bytes,10,receiveSessionIdByte,0,8);
        StringBuilder receiveSessionIdStr = new StringBuilder();
        for (byte byteItem : receiveSessionIdByte) {
            receiveSessionIdStr.append(String.format("%02x ", byteItem));
        }
        log.info("接收会话序列号指令为："+receiveSessionIdStr);
        //4.会话源标识
        System.arraycopy(bytes,18,sessionSourceIdByte,0,1);
        StringBuilder sessionSourceIdStr = new StringBuilder();
        for (byte byteItem : sessionSourceIdByte) {
            sessionSourceIdStr.append(String.format("%02x ", byteItem));
        }
        log.info("会话源标识指令为："+sessionSourceIdStr);
        //5.xml的字节长度
        System.arraycopy(bytes,19,xmlByteLengthByte,0,4);
        StringBuilder xmlByteLengthStr = new StringBuilder();
        for (byte byteItem : xmlByteLengthByte) {
            xmlByteLengthStr.append(String.format("%02x ", byteItem));
        }
        log.info("xml的字节长度指令为："+xmlByteLengthStr);
        long xmlByteLength = PlatformPacketUtil.reserve(xmlByteLengthByte);//xml的字节长度
        log.info("xml的字节长度为："+xmlByteLength);
        byte[] xmlByte = new byte[(int)xmlByteLength];
        //6.xml的内容
        System.arraycopy(bytes,23,xmlByte,0,(int)xmlByteLength);
        StringBuilder xmlContentStr = new StringBuilder();
        for (byte byteItem : xmlByte) {
            xmlContentStr.append(String.format("%02x ", byteItem));
        }
        log.info("发送的xml内容指令是<start>" + xmlContentStr + "<end>");
        //7.结束标志符号
        System.arraycopy(bytes,bytes.length-2,endIdentifierByte,0,2);
        StringBuilder endStr = new StringBuilder();
        for (byte byteItem : endIdentifierByte) {
            endStr.append(String.format("%02x ", byteItem));
        }
        log.info("结束标志符号指令是<start>" + endStr + "<end>");
    }
    /*
     * 和客户端断开连接
     * */
    public void removeLink(String robotCode){
        log.info("+++++++++++++++++断连开始+++++++++++++++++");
        log.info("准备断连的是=="+ctx.channel().id());
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

        /*for(Map.Entry<Object,RobotServerHandler> vo : robotServerHandlerMap.entrySet()) {
            log.info("当前的robotServerHandlerMap的key为" + vo.getKey());
            String robotCodeFlag = vo.getKey().toString();
            String timeFlag = robotCodeFlag.replace(robotCode,"");
            robotServerHandlerMap.remove(robotCode);
        }*/
        robotServerHandlerMap.remove(robotCode);

        log.info("channel.isActive(): " + channel.isActive());
        log.info("此时的packet====="+Packet);
        Constant.registerCount = 1;
    }
}