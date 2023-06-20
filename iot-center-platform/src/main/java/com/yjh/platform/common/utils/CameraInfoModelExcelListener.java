package com.yjh.platform.common.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.TCameraInfoExcel;
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
public class CameraInfoModelExcelListener implements ReadListener<TCameraInfoExcel> {
    List<TCameraInfoExcel> excelEntities = new ArrayList<>();

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {
        log.error("模型文件读取失败！，错误：{}",e.getMessage());
    }

    @Override
    public void invokeHead(Map<Integer, CellData> map, AnalysisContext analysisContext) {
        List<String> headerList = Arrays.asList("所属区域", "PMS编码", "设备类型", "设备型号", "设备名称", "设备别名", "IP", "端口", "录像机", "红外测温端口", "用户名",
                "密码", "录像机通道号", "使用单位", "安装位置", "生产厂家", "经度", "纬度", "是否可控", "投运时间");
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
