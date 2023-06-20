package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderExcel;
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
public class CameraRecordModelExcelListener implements ReadListener<TCameraRecorderExcel> {
    List<TCameraRecorderExcel> excelEntities = new ArrayList<>();

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
        List<String> headerList = Arrays.asList("设备名称", "PMS编码", "设备别名", "设备类型", "设备型号", "生产厂家", "使用单位", "IP", "服务端口", "RTSP端口", "传输协议",
                "协议路径", "用户名", "密码", "最大通道数", "缓存天数", "缓存大小", "录制时长");
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
