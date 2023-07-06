package com.yjh.accessvideo.module.control.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/7/5 9:08
 */
@Data
public class CameraConfigBatchReq {
    private List<Long> cameraIds;
}
