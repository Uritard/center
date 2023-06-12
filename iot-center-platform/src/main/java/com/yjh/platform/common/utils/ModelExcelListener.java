package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.ExcelEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

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
        throw e;
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
        List<String> headerList = Arrays.asList("变电站编码", "变电站名称", "区域ID", "区域名称", "间隔ID", "间隔名称", "主设备编码", "设备名称", "设备类型", "测点ID", "测点名称",
            "位置类型", "识别类型", "测点级别", "设备部位", "表计类型", "识别算法", "AI缺陷", "AI判别", "测点类型", "是否告警弹框", "告警上限1", "告警上限2", "告警上限3", "告警上限4", "告警下限1",
            "告警下限2", "告警下限3", "告警下限4", "单位", "告警级别", "测点类型");
        Set<String> importHeaderList = new HashSet<>(map.size());
        map.forEach((integer, cellData) -> {
            importHeaderList.add(cellData.getStringValue());
        });
        if (importHeaderList.size() < headerList.size() || !importHeaderList.containsAll(headerList)) {
            throw new BusinessException(ResultCodeEnum.CODE20018.getCode(),ResultCodeEnum.CODE20018.getName());
        }
        log.info("map:{}", JSON.toJSONString(map));
    }

    @Override
    public void invoke(ExcelEntity excelEntity, AnalysisContext analysisContext) {
        String validString = valid(excelEntity);
        if (!StringUtils.isBlank(validString)) {
            excelEntity.setErrorInfo(validString);
            log.warn("行号：{}，下列字段异常：{}",analysisContext.readRowHolder().getRowIndex(),validString);
            errorExcelEntities.add(excelEntity);
            return;
        }
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

    public List<ExcelEntity> getErrorExcelEntities() {
        return errorExcelEntities;
    }

    public String valid(ExcelEntity excelEntity) {
        StringJoiner stringJoiner = new StringJoiner(",");
        if (StringUtils.isBlank(excelEntity.getStationId())) {
            stringJoiner.add("变电站编号");
        }
        if (StringUtils.isBlank(excelEntity.getStationName())) {
            stringJoiner.add("变电站名称");
        }
        if (StringUtils.isBlank(excelEntity.getAreaId())) {
            stringJoiner.add("区域编码");
        }
        if (StringUtils.isBlank(excelEntity.getAreaName())) {
            stringJoiner.add("区域名称");
        }
        if (StringUtils.isBlank(excelEntity.getRegionId())) {
            stringJoiner.add("间隔ID");
        }
        if (StringUtils.isBlank(excelEntity.getRegionName())) {
            stringJoiner.add("间隔名称");
        }
        if (StringUtils.isBlank(excelEntity.getMainDeviceId())) {
            stringJoiner.add("主设备编码");
        }
        if (StringUtils.isBlank(excelEntity.getMainDeviceName())) {
            stringJoiner.add("设备名称");
        }
        if (StringUtils.isBlank(excelEntity.getDeviceTypeName())) {
            stringJoiner.add("设备类型");
        }
        if (StringUtils.isBlank(excelEntity.getMeteId())) {
            stringJoiner.add("测点ID");
        }
        if (StringUtils.isBlank(excelEntity.getMeteName())) {
            stringJoiner.add("测点名称");
        }
        if (StringUtils.isBlank(excelEntity.getPositionType())) {
            excelEntity.setPositionType("inside");
        }
        if (StringUtils.isBlank(excelEntity.getMeteTypeName())) {
            stringJoiner.add("识别类型");
        }
        if (StringUtils.isBlank(excelEntity.getRedundantType())) {
            stringJoiner.add("测点级别");
        }
        if (StringUtils.isBlank(excelEntity.getCustomName())) {
            stringJoiner.add("设备部位");
        }
        if (StringUtils.isBlank(excelEntity.getIsAi())) {
            stringJoiner.add("AI缺陷");
        }
        if (StringUtils.isBlank(excelEntity.getIsJudge())) {
            stringJoiner.add("AI判别");
        }
        if (StringUtils.isBlank(excelEntity.getMeteKindName())) {
            excelEntity.setMeteKindName("巡检类");
        }
        return stringJoiner.length() == 0 ? StringUtils.EMPTY : stringJoiner.toString();
    }
}
