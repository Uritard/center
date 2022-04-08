package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class DroneRegisterHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private DroneService droneService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${netty.server.name}")
    private String sendCode;

    @Value("${heart.beat.interval}")
    private String heartBeatInterval;

    @Value("${patroldevice.run.interval}")
    private String patroldeviceRunInterval;

    @Value("${nest.run.interval}")
    private String nestRunInterval;

    @Value("${drone.env.interval}")
    private String envInterval;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        String droneCode = xmlBaseModel.getSendCode();
        Integer registerCount = Constant.robotRegisterCounts.getOrDefault(droneCode, 1);
        log.info("巡视主机收到注册指令了,droneCode：{},这是第{}次", droneCode, registerCount);
        registerCount++;
        Constant.robotRegisterCounts.put(droneCode, registerCount);

        Map<String, String> allDroneCodeMap = redisTemplate.opsForHash().entries("AllDroneCode");
        String code = null;
        if (allDroneCodeMap.containsValue(droneCode)) {
            code = "200";
            log.info("droneCode：{},缓存有,可以注册", droneCode);
            Constant.robotRegisterFlag.put(droneCode, true);
        } else {
            List<String> droneCodeList = droneService.selectAllDroneCode();
            if (droneCodeList.contains(droneCode)) {
                code = "200";
                log.info("droneCode：{},缓存无，表中有，可以注册", droneCode);
                Constant.robotRegisterFlag.put(droneCode, true);
            } else {
                code = "400";
                log.info("droneCode：{},缓存无，表中无，不可以注册", droneCode);
            }
        }

        List<Map<String, Object>> itemsList = new ArrayList<>();
        Map<String, Object> items = new HashMap<>(5);
        // heart beat interval
        items.put("heart_beat_interval", heartBeatInterval);
        // patroldevice run interval
        items.put("patroldevice_run_interval", patroldeviceRunInterval);
        // drone run interval
        items.put("nest_run_interval", nestRunInterval);
        // weather interval
        items.put("env_interval", envInterval);
        itemsList.add(items);
        XMLBaseModel xmlBaseModelTemp = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(droneCode)
                .setType("251")
                .setCode(code)
                .setCommand("4")
                .setTime(DateTimeUtil.getDateTimeString(false))
                .setItems(itemsList);

        if (Constant.robotChannels.containsKey(droneCode)) {
            log.info("-----同一连接有多个通道,关闭多余的-----");
            ChannelHandlerContext channelHandlerContext = RobotServerHandler.getChannelHandlerContextByRobot(droneCode);
            if (Objects.nonNull(channelHandlerContext)) {
                channelHandlerContext.channel().close();
            }
        }
        Constant.robotChannels.put(droneCode, ctx.channel().id().toString());
        log.info("++++++之后的Constant.droneChannels:{}", Constant.robotChannels);
        Constant.robotThreadFlag.put(droneCode, true);

        String registerXmlString = PlatformXMLUtil.generateXml2(xmlBaseModelTemp, PlatformXMLUtil.DRONEROOTNAME);
        byte[] registerProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, registerXmlString);
        RobotServerHandler.send(registerProtocol, droneCode);

//        // Start heatBreakDealThread
//        HeartBeatThread dataDealThread = new HeartBeatThread(droneServerHandler, droneCode, redisTemplate, heartBeatInterval);
//        Thread thread = new Thread(dataDealThread);
//        thread.setDaemon(true);
//        thread.start();

        // Check whether there are unfinished tasks on the inspection host
        try {
            droneService.hasStandTaskIsFinish(xmlBaseModel.getSendCode());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.REGISTER.getCode(), this);
    }
}
