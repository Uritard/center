package com.yjh.platform.common.utils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.annotation.ExcelExtend;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.ExcelEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
public class ExcelReadListener<E> implements ReadListener<E> {
    private final String[] headerList;
    private List<E> excelEntities = new ArrayList<>();
    private List<String> errorExcelEntities = new ArrayList<>();

    public ExcelReadListener(String[] headerList) {
        this.headerList = headerList;
    }

    @Override
    public void onException(Exception e, AnalysisContext analysisContext){
        int idx = analysisContext.readRowHolder().getRowIndex() + 1;
        try {
            throw e;
        }catch (ExcelDataConvertException e1){
            int idy = e1.getColumnIndex() + 1;
            String errorStr = "行号: " + idx + "列数: " + idy + ";内容" + e1.getCellData() + "异常";
            log.error(errorStr);
            errorExcelEntities.add(errorStr);
        } catch (Exception ex) {
            errorExcelEntities.add("导入失败，请检查上传 excel 格式是否正确!");
            log.error("模型文件读取失败！，错误", ex);
        }

    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
        Set<String> importHeaderList = new HashSet<>(map.size());
        map.forEach((integer, cellData) -> importHeaderList.add(cellData.getStringValue()));

        if (importHeaderList.size() < headerList.length || !importHeaderList.containsAll(Arrays.asList(headerList))) {
            throw new BusinessException(ResultCodeEnum.CODE20018.getCode(), ResultCodeEnum.CODE20018.getName());
        }
        log.info("map:{}", JSON.toJSONString(map));
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

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {
        return true;
    }

    public List<E> getExcelEntities() {
        return excelEntities;
    }

    public List<String> getErrorExcelEntities() {
        return errorExcelEntities;
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
