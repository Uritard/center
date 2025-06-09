package com.yjh.platform.module.simple.service;

import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.entity.CalibrationDataBuild;

import java.io.IOException;
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

    /**
     * 子点位标定数据保存
     * @param dataBuild 标定数据信息
     * @return Boolean
     */
    Boolean calibrationDataBuild(CalibrationDataBuild dataBuild);
}
