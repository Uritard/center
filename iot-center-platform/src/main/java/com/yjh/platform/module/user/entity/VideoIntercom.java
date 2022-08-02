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



    @ApiModelProperty(value = "ID")
    private Long videoIntercomId;

    @ApiModelProperty(value = "门口机名称")
    @TableField(value = "camera_name",updateStrategy = FieldStrategy.IGNORED)
    private String cameraName;

    @ApiModelProperty(value = "门口机ip")
    @TableField(value = "camera_ip",updateStrategy = FieldStrategy.IGNORED)
    private String cameraIp;

    @ApiModelProperty(value = "门口机用户名")
    @TableField(value = "owner",updateStrategy = FieldStrategy.IGNORED)
    private String owner;

    @ApiModelProperty(value = "门口机密码")
    @TableField(value = "owner_code",updateStrategy = FieldStrategy.IGNORED)
    private String ownerCode;


    @ApiModelProperty(value = "通道号")
    @TableField(value = "channel_num",updateStrategy = FieldStrategy.IGNORED)
    private Integer channelNum;

    @ApiModelProperty(value = "门口机类型")
    @TableField(value = "camera_type",updateStrategy = FieldStrategy.IGNORED)
    private int cameraType;



    @ApiModelProperty(value = "上级区域ID")
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @TableField(value = "region_Name",updateStrategy = FieldStrategy.IGNORED)
    private String regionName;


    @ApiModelProperty(value = "厂家ID")
    @TableField(value = "vendor_id",updateStrategy = FieldStrategy.IGNORED)
    private Integer vendorId;

    @ApiModelProperty(value = "厂家")
    @TableField(value = "vendor",updateStrategy = FieldStrategy.IGNORED)
    private String vendor;

    @ApiModelProperty(value = "接入协议 0-SDK 1-onvif 2-rtsp 3-GB 4-SG")
    @TableField(value = "protocol_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer protocolType;

    @ApiModelProperty(value = "相机端口")
    @TableField(value = "port",updateStrategy = FieldStrategy.IGNORED)
    private Integer port;
    @ApiModelProperty(value = "相机端口")
    @TableField(value = "rtspPort",updateStrategy = FieldStrategy.IGNORED)
    private Integer rtspPort;

    @ApiModelProperty(value = "安装地址")
    @TableField(value = "address",updateStrategy = FieldStrategy.IGNORED)
    private String address;


    @TableField(value = "camera_model",updateStrategy = FieldStrategy.IGNORED)
    private String cameraModel;
    @ApiModelProperty(value = "描述")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @ApiModelProperty(value = "在线状态 0 在在线  1 离线")
    private Integer state=0;

    @ApiModelProperty(value = "秘钥标识符", hidden=true)
    private String identifier;
}
