package com.yjh.accessvideo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 丫C
 * @date 2022/4/27
 */
@Data
@Component
@ConfigurationProperties(prefix = "intelligent.algorithm")
public class IntelligentAlgorithmConfig {
    private String analysisUrl;
    private String updateUrl;
    private String resultIp;
    private String resultPort;
    private String silentMonitorType;
    private String defectType;
    private String distinguishType;
}
