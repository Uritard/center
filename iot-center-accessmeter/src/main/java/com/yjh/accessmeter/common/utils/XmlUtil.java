/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.common.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/6
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class XmlUtil {
    private XmlUtil() {
        // do nothing
    }

    public static <T> T parseObject(Element root, Class<T> cls) {

        try {
            Map<String, Field> fieldMap = ReflectUtil.getFieldMap(cls);

            T instance = cls.newInstance();
            for (Element ele : root.elements()) {
                String name = ele.getName();
                Field field = fieldMap.get(name);
                if (field == null) {
                    continue;
                }
                String text = ele.getText();
                if (List.class.isAssignableFrom(field.getType())) {
                    // 获取字段的泛型信息
                    Class<?> listGenericType = getListClass(field);
                    List<Object> objList = (List)ReflectUtil.getFieldValue(instance, field);
                    if (objList == null) {
                        objList = new ArrayList<>();
                        ReflectUtil.setFieldValue(instance, field, objList);
                    }
                    Object child = parseChildObj(ele, listGenericType, text);
                    objList.add(child);
                } else {
                    ReflectUtil.setFieldValue(instance, field, text);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据泛型解析 List 对象
     * 若泛型不存在，则解析成 Map
     */
    private static Object parseChildObj(Element ele, Class<?> listGenericType, String text)
        throws InstantiationException, IllegalAccessException {
        Object child = Objects.nonNull(listGenericType) ? listGenericType.newInstance() : new HashMap<>(16);
        List<Attribute> attrs = ele.attributes();
        Map<String, Field> attrFieldMap = Objects.nonNull(listGenericType) ? ReflectUtil.getFieldMap(listGenericType) : null;
        for (Attribute att : attrs) {
            String attrName = att.getName();
            if (attrFieldMap != null) {
                Field attrField = attrFieldMap.get(attrName);
                if (attrField == null) {
                    continue;
                }
                ReflectUtil.setFieldValue(child, attrField, att.getValue());
            } else {
                Map m = (Map)child;
                m.put(attrName, att.getValue());
            }
        }
        if (StringUtils.isNotBlank(text) && attrFieldMap != null && attrFieldMap.containsKey("textValue")) {
            ReflectUtil.setFieldValue(child, attrFieldMap.get("textValue"), text);
        } else if (StringUtils.isNotBlank(text) && attrFieldMap == null) {
            Map m = (Map)child;
            m.put("textValue", text);
        }
        return child;
    }

    /**
     * 获取 List 的泛型
     */
    private static Class<?> getListClass(Field field) {
        Type genericType = field.getGenericType();
        Class<?> listGenericType = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length > 0 && !(typeArgs[0] instanceof WildcardType) && !"java.lang.Object".equals(typeArgs[0].getTypeName())) {
                listGenericType = (Class<?>)typeArgs[0];
            }
        }
        return listGenericType;
    }

    public static <T> String createXmlString(String rootName, T obj) throws IOException {
        Document document = createXml(rootName, obj);
        return formatXml(document);
    }

    public static <T> Document createXml(String rootName, T obj) {
        Document document = DocumentHelper.createDocument();
        //根节点
        Element root = document.addElement(rootName);

        createXml(root, obj, false);
        return document;
    }

    public static <T> void createXml(Element ele, T obj, boolean isAttr) {
        try {
            Class<T> cls = (Class<T>)obj.getClass();
            Field[] fields = ReflectUtil.getFields(cls);

            for (Field f : fields) {
                String name = f.getName();
                Object v = ReflectUtil.getFieldValue(obj, f);
                if (v == null) {
                    continue;
                }
                if (v instanceof List) {
                    List<?> list = (List)v;
                    for (Object child : list) {
                        Element node = ele.addElement(name);
                        createXml(node, child, true);
                    }
                } else if (v instanceof Map) {
                    Map<String, String> map = (Map)v;
                    map.forEach(ele::addAttribute);
                } else if (!ClassUtil.isSimpleValueType(f.getType())) {
                    Element node = ele.addElement(name);
                    createXml(node, v, true);
                } else if (isAttr) {
                    if ("textValue".equals(name)) {
                        ele.setText(Objects.toString(v));
                    } else {
                        ele.addAttribute(name, Objects.toString(v));
                    }
                } else {
                    Element node = ele.addElement(name);
                    node.setText(Objects.toString(v));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String formatXml(Document document) throws IOException {
        String xmlString = null;
        XMLWriter writer = null;
        if (document != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                OutputFormat format = OutputFormat.createPrettyPrint();
                format.setNewLineAfterDeclaration(false);
                writer = new XMLWriter(stringWriter, format);
                writer.write(document);
                writer.flush();
                xmlString = stringWriter.toString();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignored) {
                        // noting to do
                    }
                }
            }
        }
        return xmlString;
    }
}
