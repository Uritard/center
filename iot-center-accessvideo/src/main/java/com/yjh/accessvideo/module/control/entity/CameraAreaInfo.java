package com.yjh.accessvideo.module.control.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author tt
 * @since 2020-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CameraAreaInfo", description = "相机状态树实体")
public class CameraAreaInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    private Long Id;

    @ApiModelProperty(value = "名称")
    private String label;

    @ApiModelProperty(value = "上级区域ID")
    private Long upId;

    @ApiModelProperty(value = "上级区域名称")
    private String upName;

    @ApiModelProperty(value = "消息类型")
    private String infoType;

    @ApiModelProperty(value = "在线状态")
    private String statusInfo;

    @ApiModelProperty(value = "通道号")
    private Integer channelNum;

    @ApiModelProperty(value = "子类")
    private List<CameraAreaInfo> children;

}