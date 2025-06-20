package com.yjh.platform.module.simple.service;

import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.entity.InitialTaskStatus;
import com.yjh.platform.module.simple.entity.CalibrationDataBuild;
import com.yjh.platform.module.simple.entity.*;

import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
public interface SimplePointService {
    /**
     * 获取底图
     *
     * @param deviceId 设备id
     * @return BasePhotoInfo
     */
    BasePhotoInfo basePhoto(Long deviceId);

    /**
     * 底图拆分
     * @param basePhotoInfo 底图信息
     * @return Map
     */
    Map<String, Object> splitPhoto(BasePhotoInfo basePhotoInfo);

    /**
     * 底图保存
     * @param photoBuild 底图信息
     * @return Boolean
     */
    Boolean basePhotoBuild(BasePhotoBuild photoBuild);

    Map<String,Object> initTaskAdd(List<Long> devicesId, Long robotId);

    InitialTaskStatus initTaskStatus(Long robotId, Long regionId);
    Boolean calibrationDataBuild(CalibrationDataBuild dataBuild);


    /**
     * 获取子点位标定数据
     * @param inspectionId 巡检id
     * @return CalibrationDataBuild
     */
    List<DeviceConfig> selectCalibrationDataBuild(Long inspectionId);

    /**
     * 标定数据批量上传
     * @param inspectionIds 巡检id
     * @return Boolean
     */
    Boolean calibrationDataBatchUpload(List<Long> inspectionIds);

    /**
     * 测试分析
     * @param inspectionId 巡检id
     * @param userId userId
     * @return Boolean
     */
    Boolean analyseTest(Long inspectionId, Long userId);

    /**
     * 查询巡视点位的算法分析结果数据
     * @param inspectionId 巡检id
     * @return 算法分析结果数据
     */
    List<MeterAnalyseResult> selectMeterAnalyseResult(Long inspectionId);
}
