package com.yjh.accesstcp.module.device.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 摄像头信息表
 * @TableName t_camera_info
 */
@Data
public class TCameraInfo implements Serializable {
    /**
     * 主键
     */
    private Long cameraId;

    /**
     * 摄像机名称
     */
    private String cameraName;

    /**
     * 摄像机型号
     */
    private Integer cameraModel;

    /**
     * PMS ID
     */
    private String pmsId;

    /**
     * 摄像机别名
     */
    private String aliasName;

    /**
     * 录像机ID
     */
    private Long recordId;

    /**
     * 上级区域ID
     */
    private String upRegionId;

    /**
     * 通道号
     */
    private Integer channelNum;

    /**
     * 相机本身通道
     */
    private Integer cameraNum;

    /**
     * 主流媒体服务器
     */
    private Integer smsId;

    /**
     * 主录像媒体服务器
     */
    private Integer rmsId;

    /**
     * 检测点ID
     */
    private String monitorId;

    /**
     * 生产厂家
     */
    private String vendorId;

    /**
     * 码流类型
     */
    private Integer streamType;

    /**
     * 接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG
     */
    private Integer protocolType;

    /**
     * 码流地址
     */
    private String url;

    /**
     * 相机IP
     */
    private String cameraIp;

    /**
     * 相机端口
     */
    private Integer port;

    /**
     * 红外测温端口
     */
    private Integer infreadPort;

    /**
     * 相机用户名
     */
    private String cameraManager;

    /**
     * 相机密码
     */
    private String cameraCode;

    /**
     * 0可见光摄像机, 1红外摄像机
     */
    private Integer cameraType;

    /**
     * 是否可控(0-可控球机，1-不可控枪机)
     */
    private Integer isControl;

    /**
     * 
     */
    private String latitude;

    /**
     * 
     */
    private String longitude;

    /**
     * 
     */
    private String address;

    /**
     * 单位
     */
    private String unit;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 投运日期
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date commissionDate;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    private static final long serialVersionUID = 1L;
}