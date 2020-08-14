package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraInfo对象", description = "摄像头信息表")
public class TCameraInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键")
    private Long cameraId;

    @ApiModelProperty(value = "摄像机名称")
    private String cameraName;

    @ApiModelProperty(value = "摄像机别名")
    private String aliasName;

    @ApiModelProperty(value = "录像机ID")
    private String recordId;

    @ApiModelProperty(value = "上级区域ID")
    private Long upRegionId;

    @ApiModelProperty(value = "通道号")
    private Integer channelNum;

    @ApiModelProperty(value = "主流媒体服务器")
    private Integer smsId;

    @ApiModelProperty(value = "主录像媒体服务器")
    private Integer rmsId;

    @ApiModelProperty(value = "厂家名称")
    private String factoryName;

    @ApiModelProperty(value = "码流类型")
    private Integer streamType;

    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    private Integer protocolType;

    @ApiModelProperty(value = "码流地址")
    private String url;

    @ApiModelProperty(value = "相机端口")
    private Integer port;

    @ApiModelProperty(value = "0可见光摄像机, 1红外摄像机")
    private Integer cameraType;

    @ApiModelProperty(value = "是否可控(0-可控球机，1-不可控枪机)")
    private Integer isControl;


}
