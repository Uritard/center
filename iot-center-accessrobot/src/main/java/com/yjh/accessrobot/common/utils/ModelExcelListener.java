package com.yjh.accessrobot.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.yjh.accessrobot.common.enumeration.DeviceTypeEnum;
import com.yjh.accessrobot.common.enumeration.MeteTypeEnum;
import com.yjh.accessrobot.module.command.entity.ExcelEntity;
import lombok.extern.slf4j.Slf4j;

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
    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {

    }

    @Override
    public void invoke(ExcelEntity excelEntity, AnalysisContext analysisContext) {
        excelEntity.setDeviceTypeName(DeviceTypeEnum.getDictNodeByUpDict(Integer.parseInt(excelEntity.getDeviceTypeId())));
        excelEntity.setMeteTypeName(MeteTypeEnum.getNodeByUpDict(Integer.parseInt(excelEntity.getMeteTypeId())));
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
