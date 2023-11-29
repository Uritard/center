/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
public enum SensorProtocolFactory {

    /**
     *
     */
    CREATE;

    private static final Logger log = LoggerFactory.getLogger(SensorProtocolFactory.class);
    private static final Map<ProtocolEnum, ISensorProtocol> SENSOR_PROTOCOL_MAP = new ConcurrentHashMap<>(32);
    private static volatile Map<ProtocolEnum, Class<?>> SENSOR_CLASS_MAP = null;

    public ISensorProtocol createProtocol(ProtocolEnum protocol) {
        if (SENSOR_CLASS_MAP == null) {
            loadClass();
        }

        return SENSOR_PROTOCOL_MAP.computeIfAbsent(protocol, this::classInstance);
    }

    void loadClass() {
        synchronized (SENSOR_PROTOCOL_MAP) {
            if (SENSOR_CLASS_MAP == null) {
                Map<ProtocolEnum, Class<?>> classMap = new EnumMap(ProtocolEnum.class);
                try {
                    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
                    final String basePackage = ClassUtils.getPackageName(SensorProtocolFactory.class);
                    String pattern =
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage)
                            + "/impl/**/*.class";
                    Resource[] resources = resourcePatternResolver.getResources(pattern);
                    //MetadataReader 的工厂类
                    MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                    for (Resource resource : resources) {
                        //用于读取类信息
                        MetadataReader reader = readerfactory.getMetadataReader(resource);
                        //扫描到的class
                        String classname = reader.getClassMetadata().getClassName();
                        Class<?> clazz = Class.forName(classname);
                        //判断是否有指定注解
                        if (clazz.isAnnotationPresent(ProtocolType.class)) {
                            ProtocolType type = clazz.getAnnotation(ProtocolType.class);
                            classMap.put(type.value(), clazz);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    log.error("读取接口实现类失败", e);
                }
                if (MapUtils.isNotEmpty(classMap)) {
                    SENSOR_CLASS_MAP = classMap;
                }
            }
        }
    }

    ISensorProtocol classInstance(ProtocolEnum protocol) {
        Class<?> clazz = SENSOR_CLASS_MAP.get(protocol);
        try {
            if (clazz == null) {
                log.error("接口未成功加载【{}】", protocol);
                return null;
            }
            ISensorProtocol sensorProtocol = (ISensorProtocol)clazz.newInstance();
            return sensorProtocol;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("初始化接口失败, {}", clazz, e);
        }
        return null;
    }

    public static void main(String[] args) {
        ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.MODBUS_RTU);
        sensorProtocol.init(null);
    }
}
