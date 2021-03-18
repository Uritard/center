package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.List;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "主键")
    private Long cameraId;

    @Length(max = 128,message = "cameraName长度必须小于等于128")
    @ApiModelProperty(value = "摄像机名称")
    @TableField(value = "camera_name",updateStrategy = FieldStrategy.IGNORED)
    private String cameraName;

    @Length(max = 128,message = "aliasName长度必须小于等于128")
    @ApiModelProperty(value = "摄像机别名")
     @TableField(value = "alias_name",updateStrategy = FieldStrategy.IGNORED)
    private String aliasName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "录像机ID")
     @TableField(value = "record_id",updateStrategy = FieldStrategy.IGNORED)
    private Long recordId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "上级区域ID")
     @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(value = "url",updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(value = "camera_ip",updateStrategy = FieldStrategy.IGNORED)
    private String cameraIp;

    @Length(max = 32,message = "address长度必须小于等于128")
    @ApiModelProperty(value = "安装地址")
     @TableField(value = "address",updateStrategy = FieldStrategy.IGNORED)
    private String address;

    @Length(max = 32,message = "latitude长度必须小于等于128")
    @ApiModelProperty(value = "纬度")
    private String latitude;

    @Length(max = 32,message = "longitude长度必须小于等于128")
    @ApiModelProperty(value = "经度")
    private String longitude;

    private List<Long> upRegionIds;

    @ApiModelProperty(value = "摄像机型号")
    private Integer cameraModel;
    @ApiModelProperty(value = "单位")
    private String unit;
    @ApiModelProperty(value = "PMS ID")
    @TableField(value = "pms_id",updateStrategy = FieldStrategy.IGNORED)
    private String pmsId;
    @ApiModelProperty(value = "检测点ID")
    private String monitorId;
    @ApiModelProperty(value = "相机本身通道")
    private Integer cameraNum;
    @ApiModelProperty(value = "红外测温端口")
    private Integer infreadPort;
    @ApiModelProperty(value = "相机用户名")
    private String cameraManager;
    @ApiModelProperty(value = "相机密码")
    private String cameraCode;

}
