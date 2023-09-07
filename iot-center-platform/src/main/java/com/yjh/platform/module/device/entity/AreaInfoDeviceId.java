package com.yjh.platform.module.device.entity;

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
@ApiModel(value = "AreaInfoDeviceId", description = "区域树实体")
public class AreaInfoDeviceId implements Serializable {

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

    @ApiModelProperty(value = "设备类型Id")
    private String deviceTypeId;

    @ApiModelProperty(value = "子类")
    private List<AreaInfoDeviceId> children;

    @ApiModelProperty(value = "测点关联相机Id")
    private String cameraId;

    @ApiModelProperty(value = "设备状态")
    private Integer state;

    @ApiModelProperty(value = "绑定预置位列表")
    private String presetId;

    @ApiModelProperty(value = "绑定类型")
    private String bindType;

}
