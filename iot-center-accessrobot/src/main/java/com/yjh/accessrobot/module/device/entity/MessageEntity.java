package com.yjh.accessrobot.module.device.entity;


import com.google.common.base.Strings;
import com.yjh.accessrobot.common.utils.ValueUtil;

import java.util.Map;
import java.util.Objects;

/**
 * 报文对象
 *
 * @author liuwentong
 */
public class MessageEntity {
    //属性
    private String property;
    //属性值
    private String value;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getValueFromMap(Map map, String key) {
        if (map != null && (!Strings.isNullOrEmpty(key)) && map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof MessageEntity) {
                return ((MessageEntity) value).getValue();
            }
        }
        return null;
    }

    public static Double getValueAsDouble(Map map, String key) {
        String value = getValueFromMap(map, key);
        return value != null ? ValueUtil.getDouble(value, null) : null;
    }

    public static Integer getValueAsInteger(Map map, String key) {
        String value = getValueFromMap(map, key);
        return value != null ? ValueUtil.getInteger(value, null) : null;
    }

    public static Boolean getValueAsBoolean(Map map, String key) {
        String value = getValueFromMap(map, key);
        return value != null ? ValueUtil.getBoolean(value) : null;
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "property='" + property + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(toString(), obj.toString());
    }
}
