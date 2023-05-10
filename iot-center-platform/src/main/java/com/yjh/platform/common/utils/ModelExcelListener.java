package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.yjh.platform.module.device.entity.ExcelEntity;
import com.yjh.platform.module.user.entity.TDictBusiness;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    Map<String,String> analyseTypeMap;
    Map<String,String> meterTypeMap;
    Map<String,String> meteTypeMap;
    Map<String,String> deviceTypeMap;
    Map<String,String> customMap;
    Map<String,String> meteKindMap;
    Map<String,String> alarmLevelMap;
    static {

    }
    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
        List<TDictBusiness> analyseType = DictBusinessCache.getDictList("analyse_type");
        analyseTypeMap = analyseType.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> meterType = DictBusinessCache.getDictList("meter_type");
        meterTypeMap = meterType.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> meteType = DictBusinessCache.getDictList("mete_type");
        meteTypeMap = meteType.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> deviceType = DictBusinessCache.getDictList("device_type");
        deviceTypeMap = deviceType.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> customType = DictBusinessCache.getDictList("custom_type");
        customMap = customType.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> meteKind = DictBusinessCache.getDictList("mete_kind");
        meteKindMap = meteKind.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
        List<TDictBusiness> alarmLevel = DictBusinessCache.getDictList("alarm_level");
        alarmLevelMap = alarmLevel.stream().collect(Collectors.toMap(TDictBusiness::getDictNote,TDictBusiness::getDictCode));
    }

    @Override
    public void invoke(ExcelEntity excelEntity, AnalysisContext analysisContext) {
        log.info("{}",excelEntity);
        if (StringUtils.isNotBlank(excelEntity.getAnalyseTypeName())) {
            excelEntity.setAnalyseTypeId(Integer.valueOf(analyseTypeMap.get(excelEntity.getAnalyseTypeName())));
        }

        if (StringUtils.isNotBlank(excelEntity.getDeviceTypeName())) {
            excelEntity.setDeviceTypeId(deviceTypeMap.get(excelEntity.getDeviceTypeName()));
        }

        if (StringUtils.isNotBlank(excelEntity.getMeterTypeName())) {
            excelEntity.setMeterTypeId(Integer.valueOf(meterTypeMap.get(excelEntity.getMeterTypeName())));
        }

        if (StringUtils.isNotBlank(excelEntity.getMeteTypeName())) {
            excelEntity.setMeteTypeId(meteTypeMap.get(excelEntity.getMeteTypeName()));
        }
        if (StringUtils.isNotBlank(excelEntity.getCustomName())) {
            excelEntity.setCustomId(customMap.get(excelEntity.getCustomName()));
        }
        if (StringUtils.isNotBlank(excelEntity.getMeteKindName())) {
            excelEntity.setMeteKindId(meteKindMap.get(excelEntity.getMeteKindName()) == null ? null : Integer.valueOf(meteKindMap.get(excelEntity.getMeteKindName())));
        }
        if (StringUtils.isNotBlank(excelEntity.getAlarmLevelName())) {
            excelEntity.setAlarmLevel(alarmLevelMap.get(excelEntity.getAlarmLevelName()) == null ? null : Integer.valueOf(alarmLevelMap.get(excelEntity.getAlarmLevelName())));
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
