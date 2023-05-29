/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils.smUtil;

import com.yjh.platform.common.result.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/6/13
 * @since [产品/模块版本] （可选）
 */
@Component
@Slf4j
public class ModelDecodeUtil {
    private static Demo demo;
    private static RedisTemplate redisTemplate;
    private static final String identifierField = "identifier";

    @Autowired
    public void setDemo(Demo demo) {
        ModelDecodeUtil.demo = demo;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        ModelDecodeUtil.redisTemplate = redisTemplate;
    }

    /**
     * 对对象内的参数进行自动解密，并重新赋值
     */
    public static void decodeField(Object obj, String... fields) {
        if (obj == null || fields == null || fields.length == 0) {
            log.error("对象或需解密字段为空， Obj:{}, field: {}", obj, fields);
            return;
        }
        String isDecode = (String)redisTemplate.opsForHash().get("t_sys_param:isEncryption", "content");
        // 不需要解密操作
        if (!"true".equals(isDecode)) {
            return;
        }
        try {
            // 获取对象
            Class<?> cls = obj.getClass();
            // 秘钥 key
            Method identifierMethod = methodGet(cls, identifierField);
            String identifier = (String)identifierMethod.invoke(obj);
            if (StringUtils.isEmpty(identifier)) {
                return;
            }
            // 每个字段值处理
            for (String f : fields) {
                // 使用 get 方法获取数据
                Method getMethod = methodGet(cls, f);
                String fget = (String)getMethod.invoke(obj);
                // 数据为空和长度大于 32 的需要解密
                if (StringUtils.isNotEmpty(fget) && fget.length() > 32) {
                    // 参数解密
                    String decompiled = demo.decryptIdentifierNoExp(fget, identifier);
                    if (decompiled.length() > 64 && decompiled.equals(fget)) {
                        throw new BusinessException("密码解析失败，请重试！");
                    } else if (decompiled.length() > 32) {
                        throw new BusinessException("密码长度需小于等于32位！");
                    }
                    // 使用 set 方法将密码替换对象值
                    Method setMethod = methodSet(cls, f);
                    setMethod.invoke(obj, decompiled);
                }
            }

        } catch (NoSuchFieldException e) {
            log.error("字段不存在", e);
        } catch (NoSuchMethodException e) {
            log.error("方法不存在", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("对象解析失败", e);
        }
    }

    public static Method methodGet(Class<?> cls, String field) throws NoSuchMethodException {
        String getField = getField(field);
        return cls.getMethod(getField);
    }

    public static Method methodSet(Class<?> cls, String field) throws NoSuchMethodException, NoSuchFieldException {
        Class<?>[] parameterTypes = new Class<?>[1];
        Field fd = cls.getDeclaredField(field);
        parameterTypes[0] = fd.getType();

        String getField = setField(field);
        return cls.getMethod(getField, parameterTypes);
    }

    private static String getField(String field) {
        return "get" + field.substring(0, 1).toUpperCase(Locale.ROOT) + field.substring(1);
    }

    private static String setField(String field) {
        return "set" + field.substring(0, 1).toUpperCase(Locale.ROOT) + field.substring(1);
    }
}
