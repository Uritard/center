/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/6/5
 * @since [产品/模块版本] （可选）
 */
@Data
@Component
@ConfigurationProperties(prefix = "default")
public class DefaultParams {
    private Map<String, String> params;
}
