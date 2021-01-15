package com.yjh.accessatmosphere;

import com.yjh.accessatmosphere.common.Constant;
import com.yjh.accessatmosphere.commons.utils.weatherUtils.ParamConfig;
import com.yjh.accessatmosphere.thread.ListerThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.accessatmosphere", "com.yjh.accessatmosphere.commons.logs"})
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class AccessAtmosphereApplication implements CommandLineRunner {
    @Value("{spring.send.service.url}")
    private String path;

    public static void main(String[] args) {
        SpringApplication.run(AccessAtmosphereApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Constant.path = path;
        ParamConfig paramConfig = new ParamConfig("COM3", 19200, 0, 8, 1);
        Thread thread = new ListerThread(paramConfig);
        thread.start();
    }

}
