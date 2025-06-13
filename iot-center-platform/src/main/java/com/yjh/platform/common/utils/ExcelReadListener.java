package com.yjh.platform.common.utils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.annotation.ExcelExtend;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/22
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class ExcelReadListener<E> extends AnalysisEventListener<E> {
    private final String[] headerList;
    private final List<E> excelEntities = new ArrayList<>();
    private final List<String> errorExcelEntities = new ArrayList<>();

    public ExcelReadListener(String[] headerList) {
        this.headerList = headerList;
    }

    @Override
    public void onException(Exception e, AnalysisContext analysisContext){
        if (e instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelException = (ExcelDataConvertException) e;
            int idx = excelException.getRowIndex() + 1;
            int idy = excelException.getColumnIndex() + 1;
            String errorStr = "行号: " + idx + "列数: " + idy + ";内容" + excelException.getCellData() + "异常";
            log.error(errorStr);
            errorExcelEntities.add(errorStr);
        } else {
            errorExcelEntities.add("导入失败，请检查上传 excel 格式是否正确!");
            log.error("模型文件读取失败！，错误", e);
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

        if (headMap.size() < headerList.length || !CollectionUtils.containsAll(headMap.values(), Arrays.asList(headerList))) {
            throw new BusinessException(ResultCodeEnum.CODE20018.getCode(), ResultCodeEnum.CODE20018.getName());
        }
        log.info("map:{}", JSON.toJSONString(headMap));
    }

    @Override
    public void invoke(E excelEntity, AnalysisContext analysisContext) {
        String validString = valid(excelEntity);
        if (!StringUtils.isBlank(validString)) {
            int idx = analysisContext.readRowHolder().getRowIndex() + 1;
            log.warn("行号：{}，下列字段异常：{}", idx, validString);
            errorExcelEntities.add("行号: " + idx + validString);
            return;
        }
        excelEntities.add(excelEntity);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("模型Excel读取完毕！");
    }

    public List<E> getExcelEntities() {
        return excelEntities;
    }

    public List<String> getErrorExcelEntities() {
        return errorExcelEntities;
    }

    /**
     * 读取成功，没有错误行则表示读取成功
     */
    public boolean success() {
        return CollectionUtils.isNotEmpty(errorExcelEntities);
    }

    public String valid(E excelEntity) {
        StringJoiner stringJoiner = new StringJoiner(",");

        // 反射函数处理对象结果
        try {
            Class<?> cls = excelEntity.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                ExcelExtend excelExtend = f.getAnnotation(ExcelExtend.class);
                ExcelProperty property = f.getAnnotation(ExcelProperty.class);
                if (f.getModifiers() == Modifier.FINAL || property == null) {
                    continue;
                }
                String fieldName = f.getName();

                String value = BeanUtils.getProperty(excelEntity, fieldName);
                if (excelExtend != null) {
                    if (StringUtils.isBlank(value)) {
                        value = excelExtend.defaultValue();
                    }

                    String dictType = excelExtend.dictType();
                    if (StringUtils.isNotEmpty(dictType) && StringUtils.isNotBlank(value)) {
                        String localValue = DictConvertUtil.DICT.getDictCode(dictType, value);
                        if (StringUtils.isNotBlank(excelExtend.convent())){
                            fieldName = fieldName.replace(excelExtend.convent(),"");
                        }
                        BeanUtils.setProperty(excelEntity, fieldName, localValue);
                    }
                }

                boolean notEmpty = (excelExtend == null || excelExtend.require()) && StringUtils.isBlank(value);
                if (notEmpty) {
                    String popName = fieldName;
                    if (StringUtils.isNotEmpty(property.value()[0])) {
                        popName = property.value()[0];
                    }
                    stringJoiner.add(popName + "字段异常");
                }
            }
        } catch (Exception e) {
            stringJoiner.add("数据处理失败");
            log.error("数据转换错误", e);
        }

        return stringJoiner.length() == 0 ? StringUtils.EMPTY : stringJoiner.toString();
    }

    public static String prettyErrors(List<?> errorList) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (Object obj : errorList) {
            if (obj instanceof String) {
                stringJoiner.add((String)obj);
            } else {
                stringJoiner.add(JSON.toJSONString(obj));
            }
        }
        return stringJoiner.toString();
    }
}
