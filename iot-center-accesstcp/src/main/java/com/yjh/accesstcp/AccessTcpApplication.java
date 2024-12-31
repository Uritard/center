package com.yjh.accesstcp;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.impl.UpType;
import com.yjh.accesstcp.module.device.service.uphandler.tek.TekRobotStatusUpHandler;
import com.yjh.accesstcp.netty.NettyClient;
import com.yjh.accesstcp.netty.algorithm.StateGridAlgorithmHandlerImpl;
import com.yjh.accesstcp.netty.iot.StateGridADecoder;
import com.yjh.accesstcp.netty.iot.StateGridAHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
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

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TekRobotStatusUpHandler tekRobotStatusUpHandler;

    public static void main(String[] args) {
        SpringApplication.run(AccessTcpApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String url = getLocalIp();
        log.info("local ip is : {}", url);
        Constant.redisTemplate = redisTemplate;
        int upflag = NumberUtils.toInt(Constant.upSystemFlag());
        //上级系统连接
        if(UpType.STATE_GRID.getType() == upflag) {
            InetSocketAddress address = new InetSocketAddress(Constant.upSystemIp(), Constant.upSystemPort());
            log.info("access tcp is running, address is : " + address.getAddress());
            NettyClient nettyClient = new NettyClient(StateGridADecoder.class, new StateGridAHandlerImpl());
            nettyClient.start(address);
        } else if (UpType.TEK.getType() == upflag) {
            tekRobotStatusUpHandler.uploadRobotStatus(redisTemplate);
        }
        //上级系统算法连接
        if(Constant.ONE.equals(Constant.managerSystemFlag())) {
            InetSocketAddress address = new InetSocketAddress(Constant.managerSystemIp(), Constant.managerSystemPort());
            log.info("access tcp cloud is running, address is : " + address.getAddress());
            NettyClient nettyClient = new NettyClient(StateGridADecoder.class, new StateGridAlgorithmHandlerImpl());
            nettyClient.start(address);
        }
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
