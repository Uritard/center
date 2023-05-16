package com.yjh.accessvideo.commons.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

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

    private JSONUtil() {
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

    public static <T> T toBean(String str, Type type) {
        return toBean(str, TypeFactory.defaultInstance().constructType(type));
    }

    public static <T> T toBean(String str, JavaType javaType) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.error("Jsons.toBean error: ", e);
        }
        return null;
    }
    public static <T> T toBean(String str, Class<T> cls) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return objectMapper.readValue(str, cls);
        } catch (Exception e) {
            log.error("Jsons.toBean error: ", e);
        }
        return null;
    }

    /**
     * 格式化 json 字符串
     */
    public static String prettyJSONString(Object object) {
        return JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteDateUseDateFormat);
    }
}
