package com.yjh.etl;

import com.yjh.etl.commons.restTemplate.ServiceRestTemplate;
import org.apache.catalina.connector.Connector;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.etl", "com.yjh.etl.commons.logs"})
@EnableDiscoveryClient
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@EnableScheduling
public class EtlApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtlApplication.class, args);
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

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }


}
