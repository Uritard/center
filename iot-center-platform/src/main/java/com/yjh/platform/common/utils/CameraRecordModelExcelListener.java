package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderExcel;
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
public class CameraRecordModelExcelListener implements ReadListener<TCameraRecorderExcel> {
    List<TCameraRecorderExcel> excelEntities = new ArrayList<>();

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
    }

    @Override
    public void invoke(TCameraRecorderExcel cameraRecorder, AnalysisContext analysisContext) {
        log.info("cameraRecorder {}",cameraRecorder);
        if (StringUtils.isNotBlank(cameraRecorder.getRecorderType())) {
            cameraRecorder.setRecorderType(DictConvertUtil.DICT.getDictCode("recorderType", cameraRecorder.getRecorderType()));
        }
        if (StringUtils.isNotBlank(cameraRecorder.getRecorderModelName())){
            cameraRecorder.setRecorderModel(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("recorderModel", cameraRecorder.getRecorderModelName())));
        }
        if (StringUtils.isNotBlank(cameraRecorder.getVendorIdStr())){
            cameraRecorder.setVendorId(NumberUtils.toInt(DictConvertUtil.DICT.getDictCode("cameraVendor", cameraRecorder.getVendorIdStr())));
        }
        if (StringUtils.isNotBlank(cameraRecorder.getProtocol())){
            cameraRecorder.setProtocol(DictConvertUtil.DICT.getDictCode("protocolType", cameraRecorder.getProtocol()));
        }

        excelEntities.add(cameraRecorder);
    }
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("模型Excel读取完毕！");
    }

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {
        return true;
    }

    public List<TCameraRecorderExcel> getExcelEntities() {
        return excelEntities;
    }
}
