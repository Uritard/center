package com.yjh.accessrobot.module.command.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2022-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDroneCamera对象", description = "无人机相机信息表")
public class TDroneCamera implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键")
    private Long cameraId;

    @ApiModelProperty(value = "无人机id")
    private Long droneId;

    @ApiModelProperty(value = "相机名称")
    private String cameraName;

    @ApiModelProperty(value = "相机型号")
    private Integer cameraModel;

    @ApiModelProperty(value = "PMS ID")
    private String pmsId;

    @ApiModelProperty(value = "相机别名")
    private String aliasName;

    @ApiModelProperty(value = "录像机ID")
    private Integer recordId;

    @ApiModelProperty(value = "NVR所在通道号")
    private Integer channelNum;

    @ApiModelProperty(value = "相机本身通道号")
    private Integer originChannel;

    @ApiModelProperty(value = "检测点ID")
    private String monitorId;

    @ApiModelProperty(value = "生产厂家")
    private String vendorId;

    @ApiModelProperty(value = "码流类型")
    private Integer streamType;

    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    private Integer protocolType;

    @ApiModelProperty(value = "码流地址")
    private String protocolUrl;

    @ApiModelProperty(value = "相机IP")
    private String cameraIp;

    @ApiModelProperty(value = "相机端口")
    private Integer cameraPort;

    @ApiModelProperty(value = "用户名")
    private String identityManager;

    @ApiModelProperty(value = "密码")
    private String identityCode;

    @ApiModelProperty(value = "0可见光相机, 1红外相机，2深度相机，3USB相机，4内窥镜相机")
    private Integer cameraType;

    @ApiModelProperty(value = "是否可控(0-可控球机，1-不可控枪机)")
    private Integer isControl;

    @ApiModelProperty(value = "单位")
    private String unit;


}
