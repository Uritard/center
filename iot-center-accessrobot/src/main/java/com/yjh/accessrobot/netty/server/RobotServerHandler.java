package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategy;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategyFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accessrobot.common.Constant.*;

/**
 * Created by tt on 2019/7/31.
 * @author YChen
 */
@Slf4j
public class RobotServerHandler extends ChannelInboundHandlerAdapter {

    private RobotService robotService;
    private RedisTemplate redisTemplate;
    private ChannelHandlerContext ctx;
    private static final String TAG = "eb90";
    public static Map<String, Object> channelPacket = new HashMap<>();
    public static Map<String, Object> robotResultMap = new HashMap<>();

    public void setRobotService(RobotService robotService) {
        this.robotService = robotService;
    }
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public ChannelHandlerContext getCtx() {
        return ctx;
    }
    public static Map<String, Object> getRobotResultMap() {
        return robotResultMap;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // TO DO: channel 和 ChannelPipeline 是否需要关闭？？
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed."+ctx.channel().id());
        log.info(ctx.channel().remoteAddress()+" Successful remove");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        Channel channel = ctx.channel();
        ChannelId id = channel.id();
        if (id != null) {
            maps.remove(id.toString());
            log.info("mapsAfterRemoved: " + maps);
        }

        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("clientDisconnect: " + e.getMessage());
        }

        for(Map.Entry<String, String> vo : Constant.robotChannels.entrySet()){
            log.info("当前的robotChannels的key为"+vo.getKey());
            String robotCode = vo.getKey();
            String channelId = Constant.robotChannels.get(robotCode);

            if (channelId.equals(String.valueOf(id))) {
                robotService.updateRobotInfo(robotCode,"离线");
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                // abnormal
                robotStatusMap.put("value", "1");
                // Update Robot Network Status
                redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
                Constant.robotChannels.remove(robotCode);
                Constant.robotThreadFlag.put(robotCode,false);
                Constant.robotRegisterFlag.put(robotCode,false);
                log.info("id: " + channel.id() + ", robotCode: " + robotCode + " left," + "onlineSize: " + maps.size());
            }
        }

        log.info("channel.isActive(): " + channel.isActive());
        log.info("此时的packet=====" + channelPacket.get(channel.id().toString()));
        channelPacket.put(channel.id().toString(), "");

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        log.info("Client " + ctx.channel().remoteAddress() + " connected");
        this.ctx = ctx;
        maps.put(ctx.channel().id().toString(), ctx);
        log.info("mapsAfterAdded: " + maps);
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: " + maps.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if ("java.io.IOException: 远程主机强迫关闭了一个现有的连接。".equals(cause.toString()) || "java.io.IOException: Connection reset by peer".equals(cause.toString())) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("+++++++++++++++++Received the robot service message+++++++++++++++++");
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        log.info("OnlineSize: " + maps.size());

        StringBuilder str = new StringBuilder();
        for (byte byteItem : bytes) {
            str.append(String.format("%02x ", byteItem));
        }
        log.info("机器人发送的的指令是<start>" + str + "<end>");

        ChannelId channelId = ctx.channel().id();
        String allPacket = channelPacket.getOrDefault(channelId.toString(), "").toString() + str.toString().replace(" ","");
        channelPacket.put(channelId.toString(), allPacket);

        String socketPacket = allPacket.replace(""," ");
        byte[] packetByte = PlatformPacketUtil.HexString2Bytes(socketPacket);
        // XML length in bytes
        byte[] xmlByteLengthByte = new byte[4];
        System.arraycopy(packetByte,19, xmlByteLengthByte,0,4);
        int xmlByteLength = PlatformPacketUtil.bytesToInt1(xmlByteLengthByte,0);
        int onePacketLength = 4 + 16 + 16 + 2 + 8 + xmlByteLength * 2 + 4;

        if (allPacket.length() >= onePacketLength){
            String onePacket= allPacket.substring(0,onePacketLength);
            log.info("onePacket==="+onePacket+ ", allPacket===" + allPacket.length());
            int headNum = appearNumber(onePacket,TAG);
            openPackage(ctx, onePacket, headNum);
        }
        // 引用计数器及时申请释放不再引用的对象
        ReferenceCountUtil.release(byteBuf);
    }

    /**
     * 拆包工具2.0
     * */
    public void openPackage (ChannelHandlerContext ctx, String socketMessageHex, int headNum){
        String onePacketString = null;
        String residueString = null;

        int counts = 2;
        if(socketMessageHex.startsWith(TAG) && headNum >= counts) {
            // 有至少一个完整的包
            // 一个包的长度-1
            int limitNum = socketMessageHex.indexOf(TAG, socketMessageHex.indexOf(TAG) + 1) + 3;
            ChannelId channelId = ctx.channel().id();
            if(socketMessageHex.length() - limitNum == 1){
                byte[] onePacket = PlatformPacketUtil.HexString2Bytes(socketMessageHex);
                StringBuilder onePacketStr = new StringBuilder();
                for (byte byteItem : onePacket) {
                    onePacketStr.append(String.format("%02x ", byteItem));
                }
                log.info("一个完整的包,准备解析的字节数组=" + onePacketStr);

                String body1 = socketMessageHex.substring(46, socketMessageHex.length()-4);
                onePacketString = PlatformPacketUtil.toStringHex(body1);
                log.info("准备解析的xml==" + onePacketString);
                channelPacket.put(channelId.toString(), channelPacket.get(channelId.toString()).toString().replace(socketMessageHex, ""));
                stringToXml(ctx, onePacket, onePacketString);
            }else{
                log.info("大于一个完整的包===" + socketMessageHex);
                onePacketString = socketMessageHex.substring(0, limitNum+1);
                // 除去一个完整包剩余的内容
                residueString = socketMessageHex.replace(onePacketString,"");
                openPackage(ctx,onePacketString,appearNumber(onePacketString, TAG));

                byte[] residuePacket = PlatformPacketUtil.HexString2Bytes(residueString);
                StringBuilder residuePacketStr = new StringBuilder();
                for (byte byteItem : residuePacket) {
                    residuePacketStr.append(String.format("%02x ", byteItem));
                }
                log.info("除去一个完整包剩余的字节数组=" + residuePacketStr);
                channelPacket.put(channelId.toString(), residueString);
                openPackage(ctx, residueString, appearNumber(residueString, TAG));
            }
        }
    }

    /**
     * String转XML并解析
     */
    public void stringToXml(ChannelHandlerContext ctx, byte[] bytes, String xmlContext){
        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlContext);
        } catch (DocumentException e) {
            e.getMessage();
        }
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);
        byte[] sendSessionIdByte = new byte[8];
        byte[] receiveSessionIdByte = new byte[8];
        System.arraycopy(bytes, 2, sendSessionIdByte, 0, 8);
        System.arraycopy(bytes, 10, receiveSessionIdByte, 0, 8);
        long sendSessionId = PlatformPacketUtil.bytesToLong(sendSessionIdByte);
        long receiveSessionId = PlatformPacketUtil.bytesToLong(receiveSessionIdByte);
        if (xmlRes.getSendCode() == null) {
            log.info("客户端" + ctx.channel().remoteAddress() + "与服务端连接可能断了，等待重连.....");
        } else {
            doProcessMessage(ctx, xmlRes, sendSessionId, receiveSessionId);
            log.info("+++++++++++++++++解包完成+++++++++++++++++");
        }
    }

    /**
     * 分析解析后的xml,进行响应处理
     * @param ctx 通道
     * @param xmlBaseModel xml格式的内容
     * @param sendSessionId 发送会话序列号
     * @param receiveSessionId 接收会话序列号
     * @return void
     */
    private void doProcessMessage(ChannelHandlerContext ctx, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId){
        log.info("++++++此时的robotChannels:{}", Constant.robotChannels);
        try {
            String handlerType;
            String resultType = "251";
            String type = xmlBaseModel.getType();
            if (Objects.equals(resultType, type)){
                handlerType = type + xmlBaseModel.getCommand();
            }else {
                handlerType = type;
            }

            MessageHandlerStrategy messageHandlerStrategy = MessageHandlerStrategyFactory.getStrategyType(handlerType);
            if (Optional.of(messageHandlerStrategy).isPresent()) {
                messageHandlerStrategy.handler(ctx, this, xmlBaseModel, sendSessionId, receiveSessionId);
            }

        }catch (Exception e){
            log.error("处理机器人响应消息错误:" + e.getMessage());
            String responseMsgXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(false, xmlBaseModel.getSendCode()));
            byte[] responseMsgProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId,false, responseMsgXmlString);
            send(responseMsgProtocol, xmlBaseModel.getSendCode());
            log.info("巡视主机给机器人{}响应了", xmlBaseModel.getSendCode());
        }
    }

    /**
     * 快速创建command=3 的消息体
     * @param flag 接收消息成功与否标识
     * @param sendCode 接收方唯一标识
     * @return XMLBaseModel
     */
    public static XMLBaseModel sendMessageForCommandThree(boolean flag, String sendCode){
        XMLBaseModel xmlBaseModelEmpty = new XMLBaseModel();
        xmlBaseModelEmpty.setCommand("3");
        xmlBaseModelEmpty.setTime(DateTimeUtil.getDateTimeString(false));
        xmlBaseModelEmpty.setType("251");
        xmlBaseModelEmpty.setCode(flag ? "200" : "500");
        xmlBaseModelEmpty.setSendCode("Server01");
        xmlBaseModelEmpty.setReceiveCode(sendCode);
        return xmlBaseModelEmpty;
    }

   /**
    * 通过文件路径解析XML
    * @param filePathAndName 文件路径
    * @return XMLBaseModel
    */
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }

    /**
     * 成功收到心跳指令,发送响应并更新机器人状态
     * @param ctx 通道
     * @param robotCode 机器人唯一标识
     * @param sendSessionId 发送会话序列号
     * @param robotStatusMap 机器人在线状态
     * @return void
     */
    public void heartBeatSuccessAfter(ChannelHandlerContext ctx, String robotCode, long sendSessionId, Map<String, String> robotStatusMap){
        String heartXmlString = PlatformXMLUtil.generateXml(sendMessageForCommandThree(true, robotCode));
        byte[] heartProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, heartXmlString);
        send(heartProtocol, robotCode);
        Integer heartBeat = robotHeartBeatCounts.getOrDefault(robotCode, 0);
        heartBeat++;
        robotHeartBeatCounts.put(robotCode, heartBeat);
        ChannelId channelId = ctx.channel().id();
        log.info("channelId:{},robotCode:{},成功收到第{}次心跳", channelId.toString(), robotCode, heartBeat);
        log.info("maps:{}", maps);
        log.info("robotChannels:{}", robotChannels);
        // 为了等待客户端和服务端连接稳定,收到三次以上再修改
        if (heartBeat > 2 && Constant.robotChannels.containsKey(robotCode)){
            log.info("连接稳定且注册成功,客户端:{}正常", robotCode);
            robotService.updateRobotInfo(robotCode,"在线");
            // normal
            robotStatusMap.put("value", "0");
            // Update Robot Network Status
            redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
            robotHeartBeatCounts.put(robotCode, 0);
        }
    }

    /**
     * 没有收到心跳指令,removeLink && updateRobotStatus
     * @param robotCode 机器人唯一标识
     * @param robotStatusMap  机器人在线状态
     * @return void
     */
    public void heartBeatFailAfter(String robotCode, Map<String, String> robotStatusMap){
        removeLink(robotCode);
        robotService.updateRobotInfo(robotCode,"离线");
        // abnormal
        robotStatusMap.put("value", "1");
        // Update Robot Network Status
        redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
        Constant.robotHeartBeatCounts.put(robotCode, 0);
    }

    /**
     * 获取ChannelHandlerContext对象
     * @param robotCode 机器人唯一标识
     * @return ChannelHandlerContext
     */
    public static ChannelHandlerContext getChannelHandlerContextByRobot(String robotCode) {
        String channelId = robotChannels.getOrDefault(robotCode, "");
        if (StringUtils.isNotEmpty(channelId)) {
            log.info("robotCode:{}获取会话通道channelId:{}", robotCode, channelId);
            return maps.getOrDefault(channelId, null);
        }
        return null;
    }

    /**
     * 发送消息
     * @param bytes 发送的内容
     * @param robotCode 机器人唯一标识
     * @return void
     */
    public static void send( byte[] bytes, String robotCode) {
        log.info("+++++++++++++++++robotCode:{}发送消息+++++++++++++++++", robotCode);
        ChannelHandlerContext context = getChannelHandlerContextByRobot(robotCode);
        if (null != context) {
            ByteBuf byteBuf = context.alloc().buffer();
            byteBuf.writeBytes(bytes);
            context.pipeline().writeAndFlush(byteBuf);
            log.info("command Send To Robot {} Success", robotCode);
            if (byteBuf.refCnt() >= 1) {
                ReferenceCountUtil.release(context);
            }
        } else {
            log.error("robotCode:{},通道为空,发送指令失败", robotCode);
        }
    }

    /**
     * 获取指定字符串出现的次数
     * @param srcText 总字符串
     * @param findText 需要的字符串
     * @return int
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }


    /**
     * 和客户端断开连接
     * @param robotCode 机器人唯一标识
     * @return void
     */
    public static void removeLink(String robotCode){
        log.info("+++++++++++++++++robotCode:{}断连开始+++++++++++++++++", robotCode);
        if (StringUtils.isNotEmpty(robotCode)) {
            ChannelHandlerContext context = getChannelHandlerContextByRobot(robotCode);

            if (Objects.nonNull(context)){
                ChannelId channelId = context.channel().id();
                log.info("准备断连的是:{}==", channelId);
                Constant.robotThreadFlag.put(robotCode, false);

                if (Objects.nonNull(channelId)){
                    maps.remove(channelId.toString());
                    robotChannels.remove(robotCode);
                    log.info("id: " + channelId + ", robotCode: " + robotCode + " left," + "onlineSize: " + maps.size());

                    try {
                        context.close().sync();
                        context.flush();
                    } catch (Exception e) {
                        Constant.robotThreadFlag.put(robotCode, false);
                        log.error("clientDisconnect: " + e.getMessage());
                    }

                    log.info("mapsAfterRemoved: " + maps);
                    log.info("robotChannelsAfterRemoved: " + robotChannels);
                    log.info("channel.isActive(): " + context.channel().isActive());
                    log.info("此时的packet=====" + channelPacket.get(channelId.toString()));
                    Constant.robotRegisterFlag.put(robotCode, false);
                }
            }else {
                log.error("robotCode:{},通道为空,发送指令失败", robotCode);
            }
            robotRegisterCounts.remove(robotCode);
            robotRemoveCounts.remove(robotCode);
        }
    }
}
