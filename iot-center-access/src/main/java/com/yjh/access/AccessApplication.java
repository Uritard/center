package com.yjh.access;

import com.yjh.access.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by tt on 2019/7/31
 */
@SpringBootApplication(scanBasePackages = "com.yjh.access")
@EnableDiscoveryClient
@Slf4j
public class AccessApplication implements CommandLineRunner {

    @Value("${netty.server.port}")
    private int port;

    //@Value("${netty.server.url}")
    //private String url;

    private NettyServer nettyServer = new NettyServer();

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name="redisDB1Template")
    private RedisTemplate redisTemplate2;

    public static void main(String[] args) {
        SpringApplication.run(AccessApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String url = getLocalIp();
        InetSocketAddress address = new InetSocketAddress(url, port);
        log.info("104Access is running, url is : " + url);
        nettyServer.start(address, redisTemplate,redisTemplate2);
        //deviceCommand.commandReceive();
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
