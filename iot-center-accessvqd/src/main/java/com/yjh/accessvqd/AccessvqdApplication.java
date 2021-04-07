package com.yjh.accessvqd;

import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import com.yjh.accessvqd.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;

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
@SpringBootApplication(scanBasePackages = {"com.yjh.accessvqd", "com.yjh.accessvqd.commons.logs"})
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class AccessvqdApplication implements CommandLineRunner {

    @Autowired
    private ChanResultService chanResultService;

    @Autowired
    private TDiagnosePlanDao tDiagnosePlanDao;

    @Autowired
    private RedisTemplate redisTemplate;

    //诊断结果报文分隔范围
    @Value("${dataKey}")
    private int dataKey;

    private NettyServer nettyServer = new NettyServer();

    public static void main(String[] args) {
        SpringApplication.run(AccessvqdApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String url = getLocalIp();
        InetSocketAddress address = new InetSocketAddress(url, 18725);
        log.info("accessVqd is running, url is : " + url);
        nettyServer.start(address, chanResultService, redisTemplate, tDiagnosePlanDao,dataKey);
//        SocketServerListenHandler socketServerListenHandler=new SocketServerListenHandler(18725,chanResultService,tDiagnosePlanDao,redisTemplate);
//        socketServerListenHandler.listenClientConnect();
//        log.info("socket服务开启------------------------");
    }


//    @Bean
//    public static ConfigureRedisAction configureRedisAction() {
//        return ConfigureRedisAction.NO_OP;
//    }

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
            ex.getMessage();
        }
        return ip;
    }


}
