package com.yjh.accessrobot.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 丫C
 * @date 2022/4/26
 */
@Data
@Component
@ConfigurationProperties(prefix = "netty.server.ftps")
public class UpFtpsConfig {
    private String flag;
    private String ip;
    private Integer port;
    private String username;
    private String password;
    private String keypw;
    private String remotePath;
}
