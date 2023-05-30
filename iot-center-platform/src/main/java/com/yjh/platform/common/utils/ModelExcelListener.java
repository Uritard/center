package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.yjh.platform.module.device.entity.ExcelEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/22
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class ModelExcelListener implements ReadListener<ExcelEntity> {
    List<ExcelEntity> excelEntities = new ArrayList<>();
    List<ExcelEntity> errorExcelEntities = new ArrayList<>();

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}", e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
    }

    @Override
    public void invoke(ExcelEntity excelEntity, AnalysisContext analysisContext) {
        log.info("{}", excelEntity);
        if (StringUtils.isNotBlank(excelEntity.getAnalyseTypeName())) {
            excelEntity
                .setAnalyseTypeId(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("analyseType", excelEntity.getAnalyseTypeName())));
        }

        if (StringUtils.isNotBlank(excelEntity.getDeviceTypeName())) {
            excelEntity.setDeviceTypeId(DictConvertUtil.DICT.getDictCode("deviceType", excelEntity.getDeviceTypeName()));
        }

        if (StringUtils.isNotBlank(excelEntity.getMeterTypeName())) {
            excelEntity.setMeterTypeId(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("meterType", excelEntity.getMeterTypeName())));
        }

        if (StringUtils.isNotBlank(excelEntity.getMeteTypeName())) {
            excelEntity.setMeteTypeId(DictConvertUtil.DICT.getDictCode("meteType", excelEntity.getMeteTypeName()));
        }
        if (StringUtils.isNotBlank(excelEntity.getCustomName())) {
            excelEntity.setCustomId(DictConvertUtil.DICT.getDictCode("customType", excelEntity.getCustomName()));
        }
        if (StringUtils.isNotBlank(excelEntity.getMeteKindName())) {
            excelEntity.setMeteKindId(CommonUtils.toInteger(DictConvertUtil.DICT.getDictCode("meteKind", excelEntity.getMeteKindName())));
        }
        if (StringUtils.isNotBlank(excelEntity.getAlarmLevelName())) {
            excelEntity
                .setAlarmLevel(CommonUtils.toInteger(DictConvertUtil.DICT.getDictCode("alarmLevel", excelEntity.getAlarmLevelName())));
        }

        if (StringUtils.equalsAny("操作类", excelEntity.getInspectionType())) {
            excelEntity.setInspectionType("2");
        } else {
            excelEntity.setInspectionType("1");
        }

        switch (excelEntity.getRedundantType()) {
            case "I类":
                excelEntity.setRedundantType("1");
                break;
            case "II类":
                excelEntity.setRedundantType("2");
                break;
            case "III类":
                excelEntity.setRedundantType("3");
                break;
            default:
                break;
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

    public List<ExcelEntity> getExcelEntities() {
        return excelEntities;
    }
}
