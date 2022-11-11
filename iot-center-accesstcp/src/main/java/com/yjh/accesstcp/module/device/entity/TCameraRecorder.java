package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 录像服务器表
 * @TableName t_camera_recorder
 */
@Data
public class TCameraRecorder implements Serializable {
    /**
     * 录像机ID
     */
    private Long recordId;

    /**
     * 服务器名称
     */
    private String recordName;

    /**
     * 录像机型号
     */
    private Integer recorderModel;

    /**
     * 录像机类型
     */
    private String recorderType;

    /**
     * 生产厂家
     */
    private Integer vendorId;

    /**
     * PMS ID
     */
    private String pmsId;

    /**
     * 备用名称
     */
    private String aliasName;

    /**
     * 服务器地址
     */
    private String recordIp;

    /**
     * 传输协议
     */
    private String protocol;

    /**
     * http端口
     */
    private Integer httpPort;

    /**
     * 传输端口
     */
    private Integer transPort;

    /**
     * 控制端口
     */
    private Integer rtspPort;

    /**
     * 用户名
     */
    private String identityManager;

    /**
     * 密码
     */
    private String identityCode;

    /**
     * 协议路径
     */
    private String protocolUrl;

    /**
     * 最大通道数
     */
    private Integer maxChannel;

    /**
     * 缓存磁盘空间
     */
    private Integer hddSize;

    /**
     * 缓存天数
     */
    private Integer bufferDay;

    /**
     * 录制文件时长 单位秒
     */
    private Integer timeLong;

    /**
     * 单位
     */
    private String unit;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始录像机ID
     */
    private String originId;

    private static final long serialVersionUID = 1L;
}