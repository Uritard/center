package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/8/22 - 12:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraInfoByDict对象", description = "摄像头信息表新增")
public class TCameraInfoByDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像机id")
    private Long cameraId;

    @ApiModelProperty(value = "摄像机名称")
    private String cameraName;

    @ApiModelProperty(value = "摄像机别名")
    private String aliasName;

    @ApiModelProperty(value = "录像机ID")
    private Long recordId;

    @ApiModelProperty(value = "区域名称")
    private String upRegionName;

    @ApiModelProperty(value = "通道号")
    private Integer channelNum;

    @ApiModelProperty(value = "主流媒体服务器")
    private Integer smsId;

    @ApiModelProperty(value = "主录像媒体服务器")
    private Integer rmsId;

    @ApiModelProperty(value = "厂家名称")
    private String vendorName;

    @ApiModelProperty(value = "厂家类型")
    private String vendorId;

    @ApiModelProperty(value = "码流类型")
    private Integer streamType;

    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    private Integer protocolType;

    @ApiModelProperty(value = "码流地址")
    private String url;

    @ApiModelProperty(value = "相机端口")
    private Integer port;

    @ApiModelProperty(value = "相机类型")
    private String cameraTypeName;

    @ApiModelProperty(value = "0可见光摄像机, 1红外摄像机")
    private Integer cameraType;

    @ApiModelProperty(value = "是否可控(0-可控球机，1-不可控枪机)")
    private Integer isControl;

    @ApiModelProperty(value = "摄像机ip")
    private String cameraIp;

    @ApiModelProperty(value = "安装地址")
    private String address;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "摄像机型号")
    private Integer cameraModel;
    @ApiModelProperty(value = "单位")
    private String unit;

}
