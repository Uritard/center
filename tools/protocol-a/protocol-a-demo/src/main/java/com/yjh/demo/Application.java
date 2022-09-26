package com.yjh.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author zilong
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        // 禁用扩展协议
        System.setProperty("jdk.tls.useExtendedMasterSecret", "false");

        SpringApplication.run(Application.class, args);
    }

}
