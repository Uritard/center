package com.yjh.accessudp;

import com.yjh.accessudp.common.Constant;
import com.yjh.accessudp.module.device.entity.SYAllInfo;
import com.yjh.accessudp.module.device.service.TCfgMeteService;
import com.yjh.accessudp.netty.server.NettyServer;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.accessudp", "com.yjh.accessudp.commons.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class AccessUdpApplication implements CommandLineRunner {

    @Value("${netty.server.port}")
    private int port;

    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TCfgMeteService tCfgMeteService;

    @Value("${spring.unoin.deviceInfo.path}")
    private String devicePath;

    private NettyServer nettyServer = new NettyServer();

    public static void main(String[] args) {
        SpringApplication.run(AccessUdpApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String url = getLocalIp();
        loadDeviceInfo();
        InetSocketAddress address = new InetSocketAddress(url, port);
        log.info("accessudp is running, url is : " + url);
        nettyServer.start(address, redisTemplate,tCfgMeteService);
    }
    public void loadDeviceInfo()throws IOException{
        //读取联动设备的信息
        BufferedReader br = null;
        FileReader reader = null;
        try  {
            reader = new FileReader(devicePath);
            br = new BufferedReader(reader);
            String line;
            String[] strArray = null;
            List<SYAllInfo> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                if(line.contains("time")){//读取时间
                    String regex = "time='([\\s\\S]*?)\\W!";
                    Matcher matcher = Pattern.compile(regex).matcher(line);
                    if (matcher.find()){
                        log.info("时间读取成功");
                        String time = matcher.group(1).trim();
                        if(time.equals(Constant.TIME)){
                            //todo 时间是写死的
                            log.info("设备数据时间与上一次时间一致");
                            br.close();
                            reader.close();
                            return;
                        }else {
                            Constant.TIME =time;
                        }
                        log.info("设备数据时间与上一次时间不一致，设备数据更新");
                    }else {
                        log.info("时间读取失败");
                    }
                }
                if(line.contains("#")){
                    strArray = line.split("\\s+");
                    if("".equals(strArray[0])){
                        strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                    }
                    //取数据
                    SYAllInfo syAllInfo = new SYAllInfo();
                    syAllInfo.setStationId(strArray[1]);
                    String[] mete = strArray[3].split("/");
                    String meteName = mete[mete.length-1]+"-"+strArray[4];
                    syAllInfo.setMeteId(strArray[2]);
                    syAllInfo.setMeteName(meteName);
                    syAllInfo.setDeviceId(strArray[2]);
                    syAllInfo.setDeviceName(strArray[3]);
                    Integer meteKind = strArray[4].contains("遥信")?1:(strArray[4].contains("遥测")?2:(strArray[4].contains("遥控")?3:4));
                    syAllInfo.setMeteKind(meteKind);
                    list.add(syAllInfo);
                    //System.out.println(list);
                }
            }
            br.close();
            reader.close();
            tCfgMeteService.deleteAll();
            tCfgMeteService.insertForAll(list);
        } catch (IOException e) {
            log.info("读取联动设备错误: "+e);
        } finally {
            if(br != null ){
                br.close();
            }
            if(reader != null ){
                reader.close();
            }

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
