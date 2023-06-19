package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.yjh.platform.module.user.entity.TCameraInfoExcel;
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
public class CameraInfoModelExcelListener implements ReadListener<TCameraInfoExcel> {
    List<TCameraInfoExcel> excelEntities = new ArrayList<>();

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
    }

    @Override
    public void invoke(TCameraInfoExcel cameraInfoExcel, AnalysisContext analysisContext) {
        log.info("cameraInfo {}", cameraInfoExcel);
        if (StringUtils.isNotBlank(cameraInfoExcel.getCameraTypeStr())) {
            cameraInfoExcel.setCameraType(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("cameraType", cameraInfoExcel.getCameraTypeStr())));
        }
        if (StringUtils.isNotBlank(cameraInfoExcel.getCameraModelStr())) {
            cameraInfoExcel.setCameraModel(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("cameraModel", cameraInfoExcel.getCameraModelStr())));
        }
        if (StringUtils.isNotBlank(cameraInfoExcel.getVendorIdStr())) {
            cameraInfoExcel.setVendorId(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("cameraVendor", cameraInfoExcel.getVendorIdStr())));
        }
        if (StringUtils.isNotBlank(cameraInfoExcel.getIsControlStr())) {
            String type = "云台球机";
            if (type.equals(cameraInfoExcel.getIsControlStr())) {
                cameraInfoExcel.setIsControl(1);
            } else {
                cameraInfoExcel.setIsControl(0);
            }
        }
        excelEntities.add(cameraInfoExcel);
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("模型Excel读取完毕！");
    }

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {
        return true;
    }

    public List<TCameraInfoExcel> getExcelEntities() {
        return excelEntities;
    }
}
