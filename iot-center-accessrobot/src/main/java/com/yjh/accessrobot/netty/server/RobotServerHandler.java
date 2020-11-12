package com.yjh.accessrobot.netty.server;

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
import org.springframework.data.redis.core.RedisTemplate;

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
    public void setSysLogsService(SysLogsService sysLogsService) {
        this.sysLogsService = sysLogsService;
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
    private boolean isThreadStart;
    public boolean getIsThreadStart() { return isThreadStart; }
    private long hisT3;
    private long T3;

    private byte[] bufBytes = new byte[1024];
    private ChannelHandlerContext ctx;
    //遥调遥控
    private static Map<Object, RobotServerHandler> robotServerHandlerMap = new HashMap<>();
    public static Map<Object, RobotServerHandler> getRobotServerHandlerMap() { return robotServerHandlerMap; }

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
                Date dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
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
        //接收发送的指令
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        StringBuilder Str1 = new StringBuilder();
        for (byte byteitem : bytes) {
            Str1.append(String.format("%02x ", byteitem));
        }
        log.info("接收到机器人的指令是<start>" + Str1 + "<end>");
        byteBuf.readBytes(bytes);
        String body = new String(bytes, "UTF-8");
        String bodyTem = body.substring(21);
        log.info("接收机器人端数据bodyTem<start>" + bodyTem+"<end>");
        String bodyTem2 = bodyTem.substring(0,bodyTem.length()-1);
        log.info("接收机器人端数据bodyTem2<start>" + bodyTem2+"<end>");
        int hahah = bodyTem2.getBytes("UTF-8").length ;
        log.info("发送的xml字节长度:"+hahah);


        Map<String, Object> xmlToMap = getXmlMessage(bodyTem2);//解析xml

//        redisTemplate.opsForHash().putAll("RobotXML", xmlToMap);//将Map放缓存

//        Map<String, Object> RobotXMLMap = redisTemplate.opsForHash().entries("RobotXML");//读redis

        if (xmlToMap.get("SendCode").equals("Client01") && xmlToMap.get("Command").equals("1")) {
            //注册指令接收相应
            String robotName = String.valueOf(xmlToMap.get("SendCode"));
            log.info("该机器人是<start>"+robotName+"<end>");
            //组装响应协议
            //创建响应XML
            Document document = DocumentHelper.createDocument();
            Element rss = document.addElement("Robot");//根节点
            Element childNode1 = rss.addElement("SendCode");//生成子节点
            childNode1.setText("巡视主机");//子节点内容
            Element childNode2 = rss.addElement("ReceiveCode");
            childNode2.setText(robotName);
            Element childNode3 = rss.addElement("Type");
            childNode3.setText("251");
            Element childNode4 = rss.addElement("Code");
            childNode4.setText("200");
            Element childNode5 = rss.addElement("Time");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            childNode5.setText(sdf.format(new Date()));
            Element childNode6= rss.addElement("Items");
            Element childNode66 = childNode6.addElement("Item");
            Element childNode67 = childNode6.addElement("Item");
            Element childNode68 = childNode6.addElement("Item");
            childNode66.addAttribute("heart_beat_interval","6");
            childNode67.addAttribute("robot_run_interval","66");
            childNode68.addAttribute("weather_interval","666");
            Element childNode7 = rss.addElement("Command");
            childNode7.setText("4");

            String xmlString = document.asXML();
            log.info("生成的注册xml是<start>"+xmlString+"<end>");
            byte[] xmlByte = xmlString.getBytes();
            int lenll = xmlByte.length;
            byte[] bytes5 = new byte[4];
            bytes5[0] = (byte)(lenll & 0xFF);
            bytes5[1] = (byte)(lenll >> 8 & 0xFF);
            bytes5[2] = (byte)(lenll >> 16 & 0xFF);
            bytes5[3] = (byte)(lenll >> 24 & 0xFF);

            byte[] headerByte = new byte[19];
            byte[] firstByte = new byte[2];
            System.arraycopy(bytes, 0, headerByte, 0,19);
            System.arraycopy(bytes, 0, firstByte, 0,2);

            List<Byte> listAll= new ArrayList<>();
            for(byte item:headerByte){
                listAll.add(item);
            }
            for(byte item:bytes5){
                listAll.add(item);
            }
            for(byte item:xmlByte){
                listAll.add(item);
            }
            for(byte item:firstByte){
                listAll.add(item);
            }
            byte[] xiexi = new byte[listAll.size()];
            int i = 0;
            for(byte item:listAll){
                xiexi[i] = item;
                i++;
            }

//            byte[] responseByte = new byte[headerByte.length+xmlByte.length+1];
//            System.arraycopy(headerByte, 0, responseByte, 0, 23);//头
//            System.arraycopy(bytes5, 0, responseByte, 18, 4);//xml长度
//            System.arraycopy(xmlByte, 0, responseByte, 22, xmlByte.length);//xml
//            System.arraycopy(firstByte, 0, responseByte, responseByte.length-2, 2);//尾
            StringBuilder Str = new StringBuilder();
            for (byte byteitem : xiexi) {
                Str.append(String.format("%02x ", byteitem));
            }
            log.info("发送给机器人的指令是<start>" + Str + "<end>");
            send(ctx,xiexi);
        }else if (xmlToMap.get("SendCode").equals("Client01") && xmlToMap.get("Command").equals("2")){
            //心跳指令接收响应
            String robotName = "Client01";
            //组装响应协议
            //创建响应XML
            Document document = DocumentHelper.createDocument();
            Element rss = document.addElement("Robot");//根节点
            Element childNode1 = rss.addElement("SendCode");//生成子节点
            childNode1.setText("巡视主机");//子节点内容
            Element childNode2 = rss.addElement("ReceiveCode");
            childNode2.setText(robotName);
            Element childNode3 = rss.addElement("Type");
            childNode3.setText("251");
            Element childNode4 = rss.addElement("Code");
            childNode4.setText("200");
            Element childNode5 = rss.addElement("Time");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            childNode5.setText(sdf.format(new Date()));
            Element childNode6 = rss.addElement("Items");
            Element childNode7 = rss.addElement("Command");
            childNode7.setText("3");

            String xmlString = document.asXML();
            log.info("生成的心跳xml是<start>" + xmlString+"<end>");
            byte[] xmlByte = xmlString.getBytes();
            int lenll = xmlByte.length;
            byte[] bytes5 = new byte[4];
            bytes5[0] = (byte)(lenll & 0xFF);
            bytes5[1] = (byte)(lenll >> 8 & 0xFF);
            bytes5[2] = (byte)(lenll >> 16 & 0xFF);
            bytes5[3] = (byte)(lenll >> 24 & 0xFF);

            byte[] headerByte = new byte[19];
            byte[] firstByte = new byte[2];
            System.arraycopy(bytes, 0, headerByte, 0,19);
            System.arraycopy(bytes, 0, firstByte, 0,2);

            List<Byte> listAll= new ArrayList<>();
            for(byte item:headerByte){
                listAll.add(item);
            }
            for(byte item:bytes5){
                listAll.add(item);
            }
            for(byte item:xmlByte){
                listAll.add(item);
            }
            for(byte item:firstByte){
                listAll.add(item);
            }
            byte[] xiexi = new byte[listAll.size()];
            int i = 0;
            for(byte item:listAll){
                xiexi[i] = item;
                i++;
            }

            StringBuilder Str = new StringBuilder();
            for (byte byteitem : xiexi) {
                Str.append(String.format("%02x ", byteitem));
            }
            log.info("发送给机器人的指令是<start>" + Str + "<end>");
            send(ctx,xiexi);

        }
//        DataDealThread dataDealThread = new DataDealThread(this, isThreadStart);
//        Thread thread = new Thread(dataDealThread);
//        thread.setDaemon(true);
//        thread.start();

        ReferenceCountUtil.release(byteBuf);
    }
    public static String Bytes2HexString(byte[] b) {
        byte[] hex = "0123456789ABCDEF".getBytes();
        byte[] buff = new byte[3 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[3 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[3 * i + 1] = hex[b[i] & 0x0f];
            buff[3 * i + 2] = 45;
        }
        String re = new String(buff);
        return re.replace("-", "");
    }
    public static byte[] HexString2Bytes(String hexstr) {
        hexstr = hexstr.replace(" ", "");
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }
    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }
    //解析XML
    public static Map<String, Object> getXmlMessage(String messageXML) throws Exception {
        Map<String, Object> message = new HashMap<String, Object>();
        //将报文XML交给DocumentHelper解析为Document
        Document document = DocumentHelper.parseText(messageXML);
        //获取根节点Robot
        Element root = document.getRootElement();
        log.info("根节点是："+root.getName());
        Iterator itt = root.elementIterator();
        while (itt.hasNext()) {
            Element rootChild = (Element) itt.next();
            String name = rootChild.getName();
            String value = rootChild.getStringValue();
            message.put(name,value);
        }
        log.info("message是："+message);
        return message;
    }

    private void handlerData() { }

    void ProcSend() {
        // 心跳报文(客户端,服务端均可发起测试);
        if (istimeout(HEARTBEAT, hisT3, false)) {
            SendHeartBeat();
            hisT3 = System.currentTimeMillis();
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

    private boolean istimeout(byte type, long value, boolean set) {
        if (type == HEARTBEAT) {
            //log.info("commandSend:68 04 43 00 00 00 "+System.currentTimeMillis() + ":" + value +":" +(System.currentTimeMillis() - value));
            if ((System.currentTimeMillis() - value) > T3) {
                if (set) hisT3 = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else return false;
    }

    public void SendHeartBeat () {

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