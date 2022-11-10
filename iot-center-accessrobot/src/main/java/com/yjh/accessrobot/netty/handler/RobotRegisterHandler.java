package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class RobotRegisterHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${netty.server.name}")
    private String sendCode;

    @Value("${heart.beat.interval}")
    private Integer heartBeatInterval;

    @Value("${patroldevice.run.interval}")
    private String patroldeviceRunInterval;

    @Value("${env.interval}")
    private String envInterval;

    @Value("${nest.run.interval}")
    private String nestRunInterval;

    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        String robotCode = xmlBaseModel.getSendCode();
        Integer registerCount = Constant.robotRegisterCounts.getOrDefault(robotCode, 1);
        log.info("巡视主机收到注册指令了,robotCode：{},这是第{}次", robotCode, registerCount);
        registerCount++;
        Constant.robotRegisterCounts.put(robotCode, registerCount);

        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(robotCode, 1);
        // 如果边缘节点 code 不为空，则表示底端上传数据的是边缘节点，不是机器人或无人机
        boolean isEdge = CollectionUtils.isNotEmpty(stdRegionList);

        Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");
        String code = null;
        if (isEdge || allRobotCodeMap.containsValue(robotCode)) {
            code = "200";
            log.info("robotCode：{},缓存有,可以注册", robotCode);
            Constant.robotRegisterFlag.put(robotCode, true);
        } else {
            List<String> robotCodeList = robotService.selectAllRobotCode();
            if (robotCodeList.contains(robotCode)) {
                code = "200";
                log.info("robotCode：{},缓存无，表中有，可以注册", robotCode);
                Constant.robotRegisterFlag.put(robotCode, true);
            } else {
                code = "400";
                log.info("robotCode：{},缓存无，表中无，不可以注册", robotCode);
            }
        }

        List<Map<String, Object>> itemsList = new ArrayList<>();
        Map<String, Object> items = new HashMap<>(5);
        // heart beat interval
        items.put("heart_beat_interval", heartBeatInterval);
        // robot run interval
        //巡视设备运行时间间隔 2022过检 robot_run_interval修改为patroldevice_run_interval
        items.put("patroldevice_run_interval", patroldeviceRunInterval);
        //环境数据间隔 2022过检  weather interval 修改为 env_interval
        // 220kv改为weather_interval  
        items.put("weather_interval", envInterval);
        // 220kv过检
//        if (isEdge || robotService.selectIsDrone(robotCode)) {
        //2022过检新增 无人机机巢运行数据间隔
        items.put("nest_run_interval", nestRunInterval);
//        }

        itemsList.add(items);
        XMLBaseModel xmlBaseModelTemp = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(robotCode)
                .setType("251")
                .setCode(code)
                .setCommand("4")
                .setTime(DateTimeUtil.getDateTimeString(false))
                .setItems(itemsList);

        if (Constant.robotChannels.containsKey(robotCode)) {
            log.info("-----同一连接有多个通道,关闭多余的-----");
            ChannelHandlerContext channelHandlerContext = RobotServerHandler.getChannelHandlerContextByRobot(robotCode);
            if (Objects.nonNull(channelHandlerContext)) {
                channelHandlerContext.channel().close();
            }
        }
        Constant.robotChannels.put(robotCode, ctx.channel().id().toString());
        log.info("++++++之后的Constant.robotChannels:{}", Constant.robotChannels);
        Constant.robotThreadFlag.put(robotCode, true);

        String registerXmlString = PlatformXMLUtil.generateXml(xmlBaseModelTemp);
        byte[] registerProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, registerXmlString);
        RobotServerHandler.send(registerProtocol, robotCode);

//        // Start heatBreakDealThread
//        HeartBeatThread dataDealThread = new HeartBeatThread(robotServerHandler, robotCode, redisTemplate, heartBeatInterval);
//        Thread thread = new Thread(dataDealThread);
//        thread.setDaemon(true);
//        thread.start();

        // Check whether there are unfinished tasks on the inspection host
        try {
            robotService.hasStandTaskIsFinish(xmlBaseModel.getSendCode());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.REGISTER.getCode(), this);
    }
}
