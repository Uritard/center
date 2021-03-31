package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yjh.accessrobot.common.Constant.Packet;

/**
 * @author YC
 * @date 2021/2/6 17:41
 */
@lombok.extern.slf4j.Slf4j
public class PacketDealThread implements Runnable {

    private int headNum;
    private String Packet;
    private Map<Object, RobotServerHandler> robotServerHandlerMap;
    private RedisTemplate redisTemplate;
    private RobotServerHandler robotServerHandler;
    private String redisValue = "content";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    public PacketDealThread(String Packet,int headNum,Map<Object, RobotServerHandler> robotServerHandlerMap,RedisTemplate redisTemplate){
        this.Packet = Packet;
        this.headNum = headNum;
        this.robotServerHandlerMap = robotServerHandlerMap;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void run() {
        try {
//            openPackage(Packet,headNum);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}
