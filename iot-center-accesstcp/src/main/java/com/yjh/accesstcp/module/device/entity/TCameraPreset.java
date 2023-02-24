package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 摄像机预位置表
 * @TableName t_camera_preset
 */
@Data
public class TCameraPreset implements Serializable {
    /**
     * 预置位id
     */
    private Long presetId;

    /**
     * 摄像头id
     */
    private Long cameraId;

    /**
     * 预置位号
     */
    private Integer presetNum;

    /**
     * 预置位名称
     */
    private String presetName;

    /**
     * 是否守望位置。0-不是，1-是
     */
    private Integer isKeepWatch;

    /**
     * 是否静默任务。0-不是，1-是
     */
    private Integer isKeepWatchTask;

    /**
     * 预置位类型
     */
    private Integer presetType;

    /**
     * 
     */
    private String creatorUser;

    /**
     * 创建时间
     */
    private Date creatorTime;

    /**
     * 是否使用
     */
    private Integer isUse;

    /**
     * 
     */
    private String presetImg;

    /**
     * 检测点位置，0-室外 1-室内
     */
    private Integer inspectionPostion;

    /**
     * 采集状态，0-未采集 1-已采集
     */
    private Integer collectStatus;

    /**
     * 标定状态，0-未标定 1-已标定
     */
    private Integer calibrationStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 是否秒级静默任务。0-不是，1-是
     */
    private Integer isSecondKeepWatchTask;

    /**
     * 相机预置位PTZ信息
     */
    private String presetPtz;

    private static final long serialVersionUID = 1L;

}