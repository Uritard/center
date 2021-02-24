package com.yjh.device;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.yjh.device.commons.restTemplate.ServiceRestTemplate;
import com.yjh.device.tradio.NET_TRADIO_DEVICEINFO;
import com.yjh.device.tradio.TradioLibrary;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.LongBuffer;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.device", "com.yjh.device.commons.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class DeviceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DeviceApplication.class, args);
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]");
            }
        });
        return factory;
    }

    private static TradioLibrary sdk_= TradioLibrary.INSTANCE;;

    public DeviceApplication() { }

    @Override
    public void run(String... strings) throws Exception {
        if (sdk_.NET_TRADIO_Init() == 0) {
            System.out.println("SDK初始化成功");
        } else {
            System.out.println("SDK初始化失败");
        }

        LongBuffer hd = LongBuffer.allocate(1);
        if(sdk_.NET_TRADIO_CreateDevice(hd) == 0){
            System.out.println("创建设备成功");
        }else{
            System.out.println("创建设备失败");
        }

        final int[] byteArratTemLength = {0};
        long hdForData = hd.get();
        sdk_.NET_TRADIO_SetRtpCallback(hdForData, new TradioLibrary.PRtpCallback() {
            @Override
            public void apply(Pointer data, int len, int channel, int db, int sample_rate, long dev) {
                log.info("收到数据：charPtr1=" + data + "，int1=" + len + "，int2=" + channel + "，int3=" + db
                        + "，long1=" + dev);
                byte[] bytesArrayTem = data.getByteArray(len, len);
                StringBuilder StrArrayTem = new StringBuilder();
                for (int i = 0; i < len; i++) { StrArrayTem.append(String.format("%02x ", bytesArrayTem[i])); }
                log.info("receiveOriginalDataArray:" + StrArrayTem);
                String path = "F:\\workspace\\";
                try {
                    File file = new File(path);
                    if (!file.exists()) { file.mkdirs(); }
                    File tempWav = new File(file, "temp.aac");
                    if (!tempWav.exists()) try { tempWav.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
                    FileOutputStream fos = new FileOutputStream(tempWav, true);
//                    fos.write(bytesArrayTem, 0, bytesArrayTem.length);
                    fos.write(bytesArrayTem);
                    fos.flush();
                    fos.close();
                    byteArratTemLength[0] = byteArratTemLength[0]+len;
                    log.info("byteArratTemLength[0]: "+byteArratTemLength[0]);
                } catch (IOException e) { e.getMessage(); }
            }
        }, 0);

        NET_TRADIO_DEVICEINFO dev = new NET_TRADIO_DEVICEINFO();
        if (sdk_.NET_TRADIO_Login(hdForData, "192.168.10.127", 38000, "admin", "123456", dev) == 0) {
            System.out.println("注册成功");
        }else {
            System.out.println("注册失败");
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int id = 0;
        if (sdk_.NET_TRADIO_Logout(id) != 0) {
            System.out.println("设备注销成功");
        } else {
            System.out.println("设备注销失败");
        }

        sdk_.NET_TRADIO_Clear();
    }

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }


}
