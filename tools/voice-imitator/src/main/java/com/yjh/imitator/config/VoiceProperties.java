package com.yjh.imitator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
@Configuration
@ConfigurationProperties(prefix = "voice", ignoreInvalidFields = true)
@Data
public class VoiceProperties {

    private String voicePath = "voice";

    /**
     * 返回结果中是否增加错误
     */
    private boolean retError = true;

    /**
     * 结果批量返回，多个相同采集结束时间的点位在一个请求中返回
     */
    private boolean batchResponse = true;

    /**
     * 返回URL http://172.24.39.9/voice-files/
     */
    private String fileUrlPath = "";

    /**
     * 采集返回接口
     */
    private String collectNotify = "http://{}:{}/voiceprintDataCollectRetNotify";

    /**
     * 分析返回接口
     */
    private String analyseNotify = "http://{}:{}/voiceprintAnalyseRetNotify";
}
