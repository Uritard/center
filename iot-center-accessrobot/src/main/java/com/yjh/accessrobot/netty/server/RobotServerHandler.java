package com.yjh.accessrobot.netty.server;

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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Value("${ftp.key.path}")
    private String filePath;//SFTP文件在服务器的地址

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
        ctx.close().sync();
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收机器人发送的指令
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = new String(bytes, "UTF-8");
        String bodyTem = body.substring(21);
        String xmlContext = bodyTem.substring(0, bodyTem.length() - 1);

        Document document = DocumentHelper.parseText(xmlContext);
//        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(xmlContext);//解析xml
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml

        log.info("解析出来的xml是："+xmlRes);
        //item的大小为0(注册，心跳，控制),1(任务控制、联动任务),2(模型同步),7(状态,运行,微气象),
        //6(坐标，巡视路线),4(异常告警),9(任务状态),14(巡视结果)
//        int itemSize = xmlRes.getItems().get(0).size();
//        log.info("itemSize的大小是："+itemSize);

//        redisTemplate.opsForHash().putAll("RobotXML", xmlToMap);//将Map放缓存
//        Map<String, Object> RobotXMLMap = redisTemplate.opsForHash().entries("RobotXML");//读redis
        if (xmlRes.getSendCode() == null){
            log.info("连接可能断了，等待重连.....");
        }else {
            if (xmlRes.getCommand().equals("1")) {
                //注册指令接收响应
                //创建响应XML
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                xmlBaseModel.setSendCode("巡视主机");
                xmlBaseModel.setReceiveCode("Client01");
                xmlBaseModel.setType("251");
                xmlBaseModel.setCode("200");
                xmlBaseModel.setCommand("4");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                xmlBaseModel.setTime(sdf.format(new Date()));
                List<Map<String, Object>> ItemsList = new ArrayList<>();
                Map<String, Object> Items = new HashMap<>();
                Items.put("heart_beat_interval", "6");
                Items.put("robot_run_interval", "66");
                Items.put("weather_interval", "666");
                ItemsList.add(Items);
                xmlBaseModel.setItems(ItemsList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的注册xml是<start>" + xmlString + "<end>");

//            byte[] xmlByte = xmlString.getBytes();//xmlByte
//            byte[] xmlLenByte = xmlLenToByte(xmlByte);//xmlLenByte
//            byte[] headerByte = new byte[19];//xmlLen前Byte
//            byte[] endByte = new byte[2];//xml后Byte
                byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
                byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte

//            System.arraycopy(bytes, 0, headerByte, 0, 19);
//            System.arraycopy(bytes, 0, endByte, 0, 2);
                System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
                System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
                long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
                long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号
                byte[] responseProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, xmlString);

                StringBuilder Str = new StringBuilder();
                for (byte byteitem : responseProtocol) {
                    Str.append(String.format("%02x ", byteitem));
                }
                log.info("发送给机器人的注册指令是<start>" + Str + "<end>");
                SendHeartBeat(responseProtocol);
//            //启动线程
//            DataDealThread dataDealThread = new DataDealThread(this, isThreadStart);
//            Thread thread = new Thread(dataDealThread);
//            thread.setDaemon(true);
//            thread.start();
                robotServerHandlerMap.put(strRobotCode, this);
            } else if (xmlRes.getCommand().equals("2")) {
                //心跳指令接收响应
                //创建响应XML
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                xmlBaseModel.setSendCode("巡视主机");
                xmlBaseModel.setReceiveCode("Client01");
                xmlBaseModel.setType("251");
                xmlBaseModel.setCode("200");
                xmlBaseModel.setCommand("3");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                xmlBaseModel.setTime(sdf.format(new Date()));
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml

                log.info("生成的心跳xml是<start>" + xmlString + "<end>");
//            byte[] xmlByte = xmlString.getBytes();//xmlByte
//            byte[] xmlLenByte = xmlLenToByte(xmlByte);//xmlLenByte
//            byte[] headerByte = new byte[19];//xmlLen前Byte
//            byte[] endByte = new byte[2];//xml后Byte
                byte[] sendSessionIdByte = new byte[8];//发送会话序列号Byte
                byte[] receiveSessionIdByte = new byte[8];//接收会话序列号Byte
//            System.arraycopy(bytes, 0, headerByte, 0, 19);
//            System.arraycopy(bytes, 0, endByte, 0, 2);
                System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
                System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
                long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);//发送会话序列号
                long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);//接收会话序列号

//            byte[] responseProtocol = packageProtocol(headerByte,xmlLenByte,xmlByte,endByte);//组装响应协议
                byte[] responseProtocol = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, xmlString);

                StringBuilder Str = new StringBuilder();
                for (byte byteitem : responseProtocol) {
                    Str.append(String.format("%02x ", byteitem));
                }
                log.info("发送给机器人的心跳指令是<start>" + Str + "<end>");
                send(ctx, responseProtocol);
            } else if (xmlRes.getCommand().equals("3")) {
                log.info("机器人收到控制/任务下发指令并响应了");
            } else if (xmlRes.getCommand().equals("4")) {
                log.info("机器人收到模型同步/联动任务下发/任务控制指令了");
                log.info("解析得到的items是<start>"+xmlRes.getItems()+"<end>");
                String deviceFile = xmlRes.getItems().get(0).get("device_file_path").toString();//设备文件路径
                String robotFile = xmlRes.getItems().get(0).get("robot_file_path").toString();//机器人文件路径
                XMLBaseModel deviceModel = getXmlMessage(filePath+"/"+deviceFile);
                List<Map<String,Object>> deviceMap = deviceModel.getItems();
                log.info("deviceMap是："+deviceMap);
                XMLBaseModel robotModel = getXmlMessage(robotFile);
                List<Map<String,Object>> robotMap = robotModel.getItems();
                log.info("robotMap是："+robotMap);
//                robotService.robotFileIntoDB(deviceMap,robotMap);//同步机器人模型到巡视主机
            }
        }
        ReferenceCountUtil.release(byteBuf);
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