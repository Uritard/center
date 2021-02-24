package com.yjh.accessrobot.common;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import io.netty.channel.ChannelHandlerContext;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Constant {

    //心跳报文
    public static final byte HEARTBEAT = 0x03;

    public static Map<String, ChannelHandlerContext> maps = new HashMap<>();
    public static AtomicInteger heartNum = new AtomicInteger(0);
    public static int flag = 0;
    public static String Packet = "";
    public static int registerCount = 1;


    public static long sendSessionId = 0L;//发送会话序列号

    public static Map<String, List<Long>> flagMap = new HashMap<>();

    public static  ExpiringMap<String, String> map = ExpiringMap.builder()
            .maxSize(100)
            .expiration(120000, TimeUnit.MILLISECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    public static String SEND_ROBOT_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static<T> Result otherServer(Map<String, List<T>> map, String url) throws Exception{
        Result re = new Result();
        //ServiceRestTemplate serviceRestTemplate1 = serviceRestTemplate;
        //SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, map, Result.class);
        return re;
    }

    public static final String TCP_URL = "http://iot-center-accesstcp/sendToUpSystem/v1/sendXML";

    public static Map<String,String> robotResultMap =  new HashMap<>();

    //算法接口
    public static final String algorithmUrl = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    public static final String defectUrl = "http://iot-center-accessvideo/analysis/v1/defect";

    public static<T> Result otherServerList( List<T> list, String url){
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(url, list, Result.class);
    }
    public static final String TASK_FINISH="http://iot-center-platform/tCruiseDataResult/v1/updateCruiseAnalyze?cruiseResultIdList";


}
