package com.yjh.accessrobot;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.accessrobot", "com.yjh.accessrobot.commons.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
@EnableScheduling
@EnableAsync
public class AccessRobotApplication implements CommandLineRunner {

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    //WS调用接口地址
    @Value("${other.webSocketUrl}")
    private String syncWebsocketUrl;

    private NettyServer nettyServer = new NettyServer();

    public static void main(String[] args) {
        SpringApplication.run(AccessRobotApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Constant.WEBSOCKET_URL = syncWebsocketUrl;
        Constant.redisTemplate = redisTemplate;
        Constant.apiPermissions= Boolean.parseBoolean(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:otherConfig", "springInterfaceApi")));
        robotService.updateAllRobotStatus();
        String url = getLocalIp();
//        String url = "192.168.40.71";

        Constant.sendCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeCode", "content"));
        Constant.stationCode = (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
        Constant.handlerNew = Boolean.parseBoolean(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyHandlerNew")));
        int port = Integer.parseInt(String.valueOf(redisTemplate.opsForHash().get("systemConfigKey:robotServerConfig", "nettyServerPort")));
        InetSocketAddress address = new InetSocketAddress(url, port);
        log.info("accessrobot is running, url is : " + url);
        nettyServer.start(address,redisTemplate, robotService);
    }

    private static String getLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                log.info(ipaddress);
                                ip = ipaddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
            log.error(ex.getMessage(), ex);
        }
        return ip;
    }

}
