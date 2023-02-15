package com.yjh.platform.module.user.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/2/14 21:17
 */
@Data
public class CameraPresetCheckResult {
    /**
     * 相机预置位
     */
    private TCameraPreset preset;

    /**
     * 相机预置位校验结果,1正常  -1偏移  0正在校验
     */
    private Integer presetCheckResult;
}
