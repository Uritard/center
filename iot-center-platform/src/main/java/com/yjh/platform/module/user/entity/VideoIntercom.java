package com.yjh.platform.module.user.entity;

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
/**
 * @author prozac.G
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "videoIntercom", description = "可视对讲基本信息")
public class VideoIntercom implements Serializable {


    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "ID")
    private Long videoIntercomId;

    @ApiModelProperty(value = "门口机名称")
    @Length(max = 255, message = "camera_name长度必须小于等于255")
    @TableField(value = "camera_name",updateStrategy = FieldStrategy.IGNORED)
    private String cameraName;

    @ApiModelProperty(value = "门口机ip")
    @Length(max = 255, message = "camera_ip长度必须小于等于255")
    @TableField(value = "camera_ip",updateStrategy = FieldStrategy.IGNORED)
    private String cameraIp;

    @ApiModelProperty(value = "门口机用户名")
    @Length(max = 255, message = "owner长度必须小于等于255")
    @TableField(value = "owner",updateStrategy = FieldStrategy.IGNORED)
    private String owner;

    @ApiModelProperty(value = "门口机密码")
    @Length(max = 255, message = "owner_code长度必须小于等于255")
    @TableField(value = "owner_code",updateStrategy = FieldStrategy.IGNORED)
    private String ownerCode;


    @ApiModelProperty(value = "通道号")
    @Max(value = 999999999)
    @TableField(value = "channel_num",updateStrategy = FieldStrategy.IGNORED)
    private Integer channelNum;

    @ApiModelProperty(value = "门口机类型")
    @Length(max = 255, message = "camera_type长度必须小于等于255")
    @TableField(value = "camera_type",updateStrategy = FieldStrategy.IGNORED)
    private String cameraType;

    @ApiModelProperty(value = "上级区域ID")
    @Max(value = 999999999999999999L)
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @TableField(value = "region_Name",updateStrategy = FieldStrategy.IGNORED)
    @Length(max = 255, message = "regionName长度必须小于等于255")
    private String regionName;

    @ApiModelProperty(value = "厂家ID")
    @Max(value = 999999999)
    @TableField(value = "vendor_id",updateStrategy = FieldStrategy.IGNORED)
    private Integer vendorId;

    @ApiModelProperty(value = "厂家")
    @TableField(value = "vendor",updateStrategy = FieldStrategy.IGNORED)
    @Length(max = 255, message = "vendor长度必须小于等于255")
    private String vendor;

    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    @TableField(value = "protocol_type",updateStrategy = FieldStrategy.IGNORED)
    @Max(value = 999999999)
    private Integer protocolType;

    @ApiModelProperty(value = "相机端口")
    @TableField(value = "port",updateStrategy = FieldStrategy.IGNORED)
    @Max(value = 999999999)
    private Integer port;

    @ApiModelProperty(value = "相机端口")
    @Max(value = 999999999)
    @TableField(value = "rtspPort",updateStrategy = FieldStrategy.IGNORED)
    private Integer rtspPort;

    @ApiModelProperty(value = "安装地址")
    @Length(max = 255, message = "address长度必须小于等于255")
    @TableField(value = "address",updateStrategy = FieldStrategy.IGNORED)
    private String address;

    @Length(max = 255, message = "camera_model长度必须小于等于255")
    @TableField(value = "camera_model",updateStrategy = FieldStrategy.IGNORED)
    private String cameraModel;

    @Length(max = 255, message = "remark长度必须小于等于255")
    @ApiModelProperty(value = "描述")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;


    @ApiModelProperty(value = "在线状态 0 在在线  1 离线")
    private Integer state=0;

    @ApiModelProperty(value = "秘钥标识符", hidden=true)
    private String identifier;
}
