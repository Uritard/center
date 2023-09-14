package com.yjh.accessmeter;

import com.yjh.accessmeter.module.service.TMeterCollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//@EnableDiscoveryClient
@Slf4j
public class IotCenterAccessmeterApplication implements CommandLineRunner {
    @Autowired
    private TMeterCollectService tMeterCollectService;

    public static void main(String[] args) {
        SpringApplication.run(IotCenterAccessmeterApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        tMeterCollectService.init();
    }
}
