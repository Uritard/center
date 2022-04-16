package com.yjh.platform.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * <功能描述>
 *
 * @author xmchen
 * @date 2022/4/16
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class JSONUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public JSONUtil() {
    }

    public static String toJSONString(Object data) {
        if (ObjectUtils.notEqual(data, null)) {
            try {
                return objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException var3) {
                log.error("JSONUtil.toJSONString.error", var3);
                return "";
            }
        }
        return StringUtils.EMPTY;
    }
}
