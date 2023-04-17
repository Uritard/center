/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils;

import com.google.common.base.CaseFormat;
import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.module.user.service.TDictBusinessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 缓存字典表信息，并将对象结果转成字典表对应结果
 *
 * @author Chenfei
 * @date 2023/4/17
 * @since [产品/模块版本] （可选）
 */
public enum DictConvertUtil {
    /**
     * 字典表
     */
    DICT;

    private final static Logger LOGGER = LoggerFactory.getLogger(DictConvertUtil.class);
    private TDictBusinessService dictBusinessService;

    private final Map<String, String> dictMap = new HashMap<>(1024);

    /**
     * 初始化字典表进内存
     */
    public void loadDict(TDictBusinessService dictBusinessService) {
        this.dictBusinessService = dictBusinessService;
        loadDict();
    }

    /**
     * 更新内存字典表
     */
    public void loadDict() {
        if (this.dictBusinessService == null) {
            throw new IllegalStateException("dictBusinessService 为空，请先初始化 loadDict(TDictBusinessService dictBusinessService)");
        }
        List<TDictBusiness> dictAll = dictBusinessService.selectAll();
        for (TDictBusiness dict : dictAll) {
            // 下划线转驼峰
            String colName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, dict.getColName());
            dictMap.put(colName + ":" + dict.getDictCode(), dict.getDictNote());
        }
    }

    /**
     * 将字典编码转换为对应字符串
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     */
    public String covertToDict(String colName, Object dictCode) {
        if (dictCode == null) {
            return "";
        }
        String key = String.valueOf(dictCode);
        return covertToDict(colName, key);
    }

    /**
     * 将字典编码转换为对应字符串
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     */
    public String covertToDict(String colName, String dictCode) {
        if (StringUtils.isEmpty(dictCode) || StringUtils.isEmpty(colName)) {
            return "";
        }
        String key = colName + ":" + dictCode;
        return dictMap.getOrDefault(key, "");
    }

    /**
     * 将对象中字典表转换为字典对应name
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     * codeName 字典 dict_code 对应信息所在字段
     * noteName 字典 dict_note 对应信息存储字段
     */
    public boolean covertToDict(Object obj, String colName, String codeName, String noteName) {
        boolean result = true;
        // 判断对象是否是Map类型
        if (obj == null) {
            return false;
        }
        if (obj instanceof Map) {
            Map<Object, Object> objMap = (Map)obj;
            Object value = objMap.get(codeName);

            String val = this.covertToDict(colName, value);
            if (StringUtils.isNotEmpty(val)) {
                objMap.put(noteName, val);
            }
        } else {
            // 反射函数处理对象结果
            try {
                Class<?> cls = obj.getClass();
                // 使用get方法获取值，注意不规范的驼峰设值
                Method methodGet = cls.getMethod("get" + StringUtils.capitalize(codeName));
                Object value = methodGet.invoke(obj);

                String val = this.covertToDict(colName, value);
                if (StringUtils.isNotEmpty(val)) {
                    // 使用 set 方法设值值，注意不规范的驼峰设值
                    Method methodSet = cls.getMethod("set" + StringUtils.capitalize(noteName), String.class);
                    methodSet.invoke(obj, val);
                }
            } catch (Exception e) {
                result = false;
                LOGGER.error("转换字典错误", e);
            }
        }
        return result;
    }

    /**
     * 将对象中字典表转换为字典对应name
     * 使用 DictOptional 一次对多个值获取dict信息
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     * codeName 字典 dict_code 对应信息所在字段
     * noteName 字典 dict_note 对应信息存储字段
     */
    public boolean covertToDict(Object obj, DictOptional dictOptional) {
        List<OptionalItem> itemList = dictOptional.list;
        if (CollectionUtils.isEmpty(itemList)) {
            LOGGER.error("传参不正确，DictOptional.list 为空！");
            return false;
        }
        for (OptionalItem item : itemList) {
            covertToDict(obj, item.getColName(), item.getCodeName(), item.getNoteName());
        }

        return true;
    }

    /**
     * 将对象中字典表转换为字典对应name
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     * codeName 字典 dict_code 对应信息所在字段
     * noteName 字典 dict_note 对应信息存储字段
     */
    public boolean covertToDict(List<?> objList, String colName, String codeName, String noteName) {
        if (CollectionUtils.isEmpty(objList)) {
            LOGGER.error("传参不正确，objList 为空！");
            return false;
        }
        for (Object obj : objList) {
            covertToDict(obj, colName, codeName, noteName);
        }

        return true;
    }

    /**
     * 将对象中字典表转换为字典对应name
     * 使用 DictOptional 一次对多个值获取dict信息
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     * codeName 字典 dict_code 对应信息所在字段
     * noteName 字典 dict_note 对应信息存储字段
     */
    public boolean covertToDict(List<?> objList, DictOptional dictOptional) {
        List<OptionalItem> itemList = dictOptional.list;
        if (CollectionUtils.isEmpty(itemList) || CollectionUtils.isEmpty(objList)) {
            LOGGER.error("不需要进行字典转码，DictOptional.list: {} 或 objList: {} 为空！", itemList, objList);
            return false;
        }
        for (Object obj : objList) {
            for (OptionalItem item : itemList) {
                covertToDict(obj, item.getColName(), item.getCodeName(), item.getNoteName());
            }
        }

        return true;
    }

    /**
     * 组装 DictOptional 信息
     * colName 必须为驼峰格式，如 alarmLevel, deviceType
     * codeName 字典 dict_code 对应信息所在字段
     * noteName 字典 dict_note 对应信息存储字段
     */
    public static DictOptional optional(String colName, String codeName, String noteName) {
        DictOptional dictOptional = new DictOptional();
        dictOptional.add(colName, codeName, noteName);
        return dictOptional;
    }

    /**
     * 组装 DictOptional 信息
     * colName 和 codeName 值相同，必须为驼峰格式，如 alarmLevel, deviceType
     * noteName 字典 dict_note 对应信息存储字段
     */
    public static DictOptional optional(String colName, String noteName) {
        DictOptional dictOptional = new DictOptional();
        dictOptional.add(colName, colName, noteName);
        return dictOptional;
    }

    /**
     * 组装 DictOptional 信息
     * colName 和 codeName 值相同，必须为驼峰格式，如 alarmLevel, deviceType
     * 最终结果存入 colName + "Name"
     */
    public static DictOptional optional(String colName) {
        DictOptional dictOptional = new DictOptional();
        dictOptional.add(colName, colName, colName + "Name");
        return dictOptional;
    }

    public static class DictOptional {
        private final List<OptionalItem> list;

        private DictOptional() {
            list = new ArrayList<>();
        }

        public DictOptional add(String colName, String codeName, String noteName) {
            list.add(new OptionalItem(colName, codeName, noteName));
            return this;
        }

        public DictOptional add(String colName, String noteName) {
            list.add(new OptionalItem(colName, colName, noteName));
            return this;
        }

        public DictOptional add(String colName) {
            list.add(new OptionalItem(colName, colName, colName + "Name"));
            return this;
        }
    }

    private static class OptionalItem {
        private String colName;
        private String codeName;
        private String noteName;

        public OptionalItem(String colName, String codeName, String noteName) {
            this.colName = colName;
            this.codeName = codeName;
            this.noteName = noteName;
        }

        public String getColName() {
            return colName;
        }

        public void setColName(String colName) {
            this.colName = colName;
        }

        public String getCodeName() {
            return codeName;
        }

        public void setCodeName(String codeName) {
            this.codeName = codeName;
        }

        public String getNoteName() {
            return noteName;
        }

        public void setNoteName(String noteName) {
            this.noteName = noteName;
        }
    }

}
