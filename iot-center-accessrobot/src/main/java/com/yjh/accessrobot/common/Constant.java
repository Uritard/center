package com.yjh.accessrobot.common;

import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import io.netty.channel.ChannelHandlerContext;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constant {

    //心跳报文
    public static final byte HEARTBEAT = 0x03;

    public static Map<String, ChannelHandlerContext> maps = new HashMap<>();
    public static int heartNum = 0;
    public static int flag = 0;
    public static String Packet = "";

    public static long sendSessionId = 0L;//发送会话序列号

    public static Map<String, List<Long>> flagMap = new HashMap<>();

    public static  ExpiringMap<String, String> map = ExpiringMap.builder()
            .maxSize(100)
            .expiration(120000, TimeUnit.MILLISECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static String SEND_ROBOT_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static Result otherServer(Map<String, List<XMLBaseModel>> map, String url) throws Exception{
        Result re = new Result();
        ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        if (null != serviceRestTemplate) {
            re = serviceRestTemplate.postForObject(url, map, Result.class);
        }
        return re;
    }

    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

}
