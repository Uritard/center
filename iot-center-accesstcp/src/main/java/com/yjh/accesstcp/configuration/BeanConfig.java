package com.yjh.accesstcp.configuration;

import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Value("${netty.server.ftps.ip}")
    private String host;
    @Value("${netty.server.ftps.port}")
    private String port;
    @Value("${netty.server.ftps.username}")
    private String username;
    @Value("${netty.server.ftps.password}")
    private String password;


    @Bean
    public FtpsUtil ftpsUtil() {
        FtpsUtil.Options options = new FtpsUtil.Options();
        options.setHost(host);
        options.setPort(port);
        options.setUsername(username);
        options.setPassword(password);
        return new FtpsUtil(options);
    }
}
