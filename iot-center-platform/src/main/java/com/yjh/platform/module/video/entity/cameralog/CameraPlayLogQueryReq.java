package com.yjh.platform.module.video.entity.cameralog;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
@Data
public class CameraPlayLogQueryReq implements Serializable {
    /**
     * 相机id*
     */
    private Long cameraId;
    /**
     * 用户id*
     */
    private Long userId;
    /**
     * 开始播放时间*
     */
    private String startTime;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
