package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accessrobot.common.Constant.Packet;
import static com.yjh.accessrobot.common.Constant.maps;

/**
 * @author YC
 * @date 2021/2/6 17:41
 */
@lombok.extern.slf4j.Slf4j
public class PacketDealThread implements Runnable {

    private Integer heartNum;
    private String Packet;
    private Map<Object, RobotServerHandler> robotServerHandlerMap;
    private RedisTemplate redisTemplate;
    private RobotServerHandler robotServerHandler;
    private String redisValue = "content";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private XMLBaseModel xmlBaseModel;
    private long sendSessionId;
    private long receiveSessionId;
    private Integer flag2;
    private boolean isThreadStart;

    public PacketDealThread(XMLBaseModel xmlBaseModel,
                            long sendSessionId,
                            long receiveSessionId,
//                            String Packet,
                            Integer heartNum,
                            Integer flag2,
                            RobotServerHandler robotServerHandler,
                            boolean isThreadStart,
                            RedisTemplate redisTemplate){
        this.Packet = Packet;
        this.heartNum = heartNum;
        this.robotServerHandlerMap = robotServerHandlerMap;
        this.redisTemplate = redisTemplate;
        this.receiveSessionId = receiveSessionId;
        this.sendSessionId =sendSessionId;
        this.xmlBaseModel = xmlBaseModel;
        this.robotServerHandler = robotServerHandler;
        this.flag2 = flag2;
        this.isThreadStart = isThreadStart;
    }


    @Override
    public void run() {
        try {
            doProcessMessage(xmlBaseModel,sendSessionId,receiveSessionId);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /*
     * 分析解析后的xml,进行响应处理
     * */
    private synchronized void doProcessMessage(XMLBaseModel xmlBaseModel,long sendSessionId,long receiveSessionId) throws Exception {
        log.info("robotServerHandlerMap===" + robotServerHandlerMap);
        Map<String, String> platformServerMap = redisTemplate.opsForHash().entries("t_sys_param:PlatformServer");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> heartbeatIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:heartbeatInterval");
        Map<String, String> runDataIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:runDataInterval");
        Map<String, String> weatherDataIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:weatherDataInterval");
        Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");


        if ("251".equals(xmlBaseModel.getType())) {
            switch (xmlBaseModel.getType() + xmlBaseModel.getCommand()) {
                //注册指令(发送响应)
                case "2511":
                    log.info("巡视主机收到注册指令了,这是第" + Constant.registerCount + "次");
                    Constant.registerCount++;
                    List<Map<String, Object>> itemsList = new ArrayList<>();
                    Map<String, Object> items = new HashMap<>();
                    String code = "";
                    if (allRobotCodeMap.containsValue(xmlBaseModel.getSendCode())) {
                        code = "200";//success
                        log.info("缓存有,可以注册");
                    } else {
                        List<String> robotCodeList = StaticContextAccessor.getBean(RobotService.class).selectAllRobotCode();
                        if (robotCodeList.contains(xmlBaseModel.getSendCode())) {
                            code = "200";
                            log.info("缓存无，表中有，可以注册");
                        } else {
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
                    robotServerHandler.sendHeartBeat(registerProtocol, xmlBaseModel.getSendCode());

                    if (Objects.nonNull(robotServerHandlerMap.get(xmlBaseModel.getSendCode()))) {
                        robotServerHandlerMap.get(xmlBaseModel.getSendCode()).getCtx().close();
                    }

                    robotServerHandlerMap.put(xmlBaseModel.getSendCode(), robotServerHandler);
                    //Start heatBreakDealThread
                    HeartBreakDealThread dataDealThread = new HeartBreakDealThread(robotServerHandler, xmlBaseModel.getSendCode(), redisTemplate, isThreadStart, sendSessionId, receiveSessionId);
                    Thread thread = new Thread(dataDealThread);
                    thread.setDaemon(true);
                    thread.start();
                    break;
                //心跳指令(发送响应)
                case "2512":
                    log.info("巡视主机收到心跳指令了");
                    heartNum = 0;
                    String robotCode = xmlBaseModel.getSendCode();
                    if (allRobotCodeMap.containsValue(robotCode)) {
                        log.info("缓存有,发送心跳响应");
                        Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                        robotServerHandler.heartBeatSuccessAfter(robotCode, sendSessionId, robotStatusMap);
                    } else {
                        List<String> robotCodeList = StaticContextAccessor.getBean(RobotService.class).selectAllRobotCode();
                        if (robotCodeList.contains(robotCode)) {
                            log.info("缓存无,表中有,发送心跳相应");
                            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                            robotServerHandler.heartBeatSuccessAfter(robotCode, sendSessionId, robotStatusMap);
                        } else {
                            log.info("缓存无,表中无,断开连接");
                            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                            heartBeatFailAfter(robotCode, robotStatusMap);
                        }
                    }
                    break;
            }
        }
    }
    /*
     * 没有收到心跳指令,removeLink && updateRobotStatus
     * */
    void heartBeatFailAfter(String robotCode,Map<String, String> robotStatusMap){
        robotServerHandler.removeLink(robotCode);
        log.info("没有收到心跳flag2的值==="+flag2);
        StaticContextAccessor.getBean(RobotService.class).updateRobotInfo(robotCode,"离线");
        robotStatusMap.put("value","1");//异常
        redisTemplate.opsForHash().putAll("RobotStatus:"+robotCode+":2",robotStatusMap);//update robot Network Status
        flag2 = 0;
    }
    private void send(ChannelHandlerContext ctx, byte[] bytes, String strRobotCode) {
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
}
