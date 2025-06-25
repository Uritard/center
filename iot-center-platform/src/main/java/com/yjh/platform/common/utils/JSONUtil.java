package com.yjh.platform.common.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;

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

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
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
     * 美化json字符串
     * @param str str
     * @return  str
     */
    public static String beautifyJson(String str) {
        if (StringUtils.isEmpty(str)) {
            return StringUtils.EMPTY;
        }
        try {
            Object jsonNode = objectMapper.readTree(str);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            log.error("str beautify fail: " , e);
        }
        return str;
    }

    /**
     * 将字符串转换为蛇形命名
     *
     * @param str str
     * @return str
     */
    public static String toSnakeCase(String str) {
        JsonNode jsonNode = JSONUtil.readTree(str);
        if (jsonNode == null) {
            return str;
        }
        return convertKeys(jsonNode).toString();
    }

    public static JsonNode readTree(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception var2) {
            log.error("Jsons.readTree error: ", var2);
            return null;
        }
    }

    /**
     * 递归处理JsonNode，将所有键转换为蛇形命名
     * 支持数组和对象
     * @param jsonNode jsonNode
     * @return JsonNode
     */
    public static JsonNode convertKeys(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            ArrayNode arrayNode = getObjectMapper().createArrayNode();
            for (int i = 0; i < jsonNode.size(); i++) {
                JsonNode item = jsonNode.get(i);
                JsonNode convertedItem = convertKeys(item);
                arrayNode.add(convertedItem);
            }
            return arrayNode;
        }
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode)jsonNode;
            ObjectNode resultNode = getObjectMapper().createObjectNode();

            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String originalKey = fieldNames.next();
                String newKey = toSnakeCaseStr(originalKey);
                JsonNode valueNode = objectNode.get(originalKey);

                // 递归处理嵌套结构
                JsonNode convertedValue = convertKeys(valueNode);
                resultNode.set(newKey, convertedValue);
            }
            return resultNode;
        } else {
            return jsonNode;
        }
    }
    /**
     * 将字符串转换为蛇形命名
     *
     * @param str str
     * @return str
     */
    public static String toSnakeCaseStr(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 格式化 json 字符串
     */
    public static String prettyJSONString(Object object) {
        return JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteDateUseDateFormat);
    }
}
