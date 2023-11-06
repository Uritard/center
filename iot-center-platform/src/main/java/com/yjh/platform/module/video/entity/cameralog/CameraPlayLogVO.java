package com.yjh.platform.module.video.entity.cameralog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 */
@Data
public class CameraPlayLogVO implements Serializable {
    /**
     * 相机id*
     */
    private Long cameraId;
    /**
     * 相机名称*
     */
    private String cameraName;
    /**
     * 用户id*
     */
    private Long userId;
    /**
     * 用户名*
     */
    private String userName;
    /**
     * 开始播放时间*
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String startTime;
    /**
     * 结束播放时间*
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String stopTime;

    /**
     * 播放时长*
     */
    private String playDuration;

    /**
     * 记录状态
     * @see com.yjh.platform.module.video.enums.CameraRecordStatusEnum*
     */
    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
