package com.yjh.platform.module.user.entity;

import com.yjh.platform.module.device.entity.AreaInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lqh
 * @since 2020/11/18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AreaInfo", description = "区域树实体")
public class AreaInfoDetail implements Serializable {
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

    @ApiModelProperty(value = "子类")
    private List<AreaInfoDetail> children;

    @ApiModelProperty(value = "设备状态")
    private Integer state;

    @ApiModelProperty(value = "相机ID")
    private String cameraId;

    @ApiModelProperty(value = "设备(摄像机、无人机、机器人等)类型")
    private Integer deviceType;

}
