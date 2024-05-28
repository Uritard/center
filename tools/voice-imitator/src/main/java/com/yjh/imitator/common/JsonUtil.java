package com.yjh.imitator.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/1/3
 * @since [产品/模块版本] （可选）
 */
public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private JsonUtil() {
        // nothing to do
    }

    public static String prettyJson(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 启用格式化输出
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            Object jsonObj = objectMapper.readValue(jsonString, Object.class);

            return objectMapper.writeValueAsString(jsonObj);
        } catch (JsonProcessingException e) {
            LOGGER.error("invalid param, json parse error: {}", e.getMessage());
            return "";
        }
    }

    public static String toJson(Object jsonObj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 启用格式化输出
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            return objectMapper.writeValueAsString(jsonObj);
        } catch (JsonProcessingException e) {
            LOGGER.error("invalid param, json parse error: {}", e.getMessage());
            return "";
        }
    }
}
