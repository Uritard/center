package com.yjh.accesstcp;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.server.NettyClient;
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

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.accesstcp", "com.yjh.accesstcp.commons.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class AccessTcpApplication implements CommandLineRunner {

    @Value("${netty.server.port}")
    private int port;
    @Value("${netty.server.url}")
    private String serverUrl;
    @Value("${spring.union.upSystem}")
    private String server;
    @Value("${spring.union.cruiseHost}")
    private String cruise;
    @Value("${a.interface.flag}")
    private String flag;
    @Value("${spring.union.stationCode}")
    private String stationCode;

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SendToUpSystemServices sendToUpSystemServices;

    private NettyClient nettyClient = new NettyClient();

    public static void main(String[] args) {
        SpringApplication.run(AccessTcpApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String url = getLocalIp();
        Constant.stationCode = stationCode;
        if("1".equals(flag)) {
            InetSocketAddress address = new InetSocketAddress(serverUrl, port);
            log.info("accesstcp is running, url is : " + url);
            nettyClient.start(address, redisTemplate, sendToUpSystemServices, server, cruise);
        }
    }
    public void loadDeviceInfo()throws IOException{

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
                                ip = ipaddress;
                                log.info(ipaddress);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
            ex.getMessage();
        }
        return ip;
    }

}
