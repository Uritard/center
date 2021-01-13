package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

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

    @NotNull(message = "cameraId不为空")
    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "主键")
    private Long cameraId;

    @Length(max = 128,message = "cameraName长度必须小于等于128")
    @ApiModelProperty(value = "摄像机名称")
    private String cameraName;

    @Length(max = 128,message = "aliasName长度必须小于等于128")
    @ApiModelProperty(value = "摄像机别名")
    private String aliasName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "录像机ID")
    private Long recordId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "上级区域ID")
    private Long upRegionId;

    @Max(value=999999999)
    @ApiModelProperty(value = "通道号")
    private Integer channelNum;

    @Max(value=999999999)
    @ApiModelProperty(value = "主流媒体服务器")
    private Integer smsId;

    @Max(value=999999999)
    @ApiModelProperty(value = "主录像媒体服务器")
    private Integer rmsId;

    @Max(value=999999999)
    @ApiModelProperty(value = "厂家ID")
    private Integer vendorId;

    @Max(value=999999999)
    @ApiModelProperty(value = "码流类型")
    private Integer streamType;

    @Max(value=999999999)
    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    private Integer protocolType;

    @Length(max = 128,message = "url长度必须小于等于128")
    @ApiModelProperty(value = "码流地址")
    private String url;

    @Max(value=999999999)
    @ApiModelProperty(value = "相机端口")
    private Integer port;

    @Max(value=999999999)
    @ApiModelProperty(value = "0可见光摄像机, 1红外摄像机")
    private Integer cameraType;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否可控(0-可控球机，1-不可控枪机)")
    private Integer isControl;

    @Length(max = 38,message = "cameraIp长度必须小于等于128")
    @ApiModelProperty(value = "摄像机ip")
    private String cameraIp;

    @Length(max = 32,message = "address长度必须小于等于128")
    @ApiModelProperty(value = "安装地址")
    private String address;

    @Length(max = 32,message = "latitude长度必须小于等于128")
    @ApiModelProperty(value = "纬度")
    private String latitude;

    @Length(max = 32,message = "longitude长度必须小于等于128")
    @ApiModelProperty(value = "经度")
    private String longitude;

    private List<Long> upRegionIds;
}
