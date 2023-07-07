package com.yjh.platform.module.device.entity;

import com.yjh.platform.module.user.entity.AreaInfoDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 设备综合树实体
 *
 * @author 丫C
 * @date 2023/07/06
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SynthesisTreeAreaInfo", description = "综合树实体")
public class SynthesisTreeAreaInfo implements Serializable {

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
    private List<SynthesisTreeAreaInfo> children;

    @ApiModelProperty(value = "测点关联相机Id")
    private String cameraId;

    @ApiModelProperty(value = "设备状态")
    private Integer state;

    @ApiModelProperty(value = "设备(摄像机、无人机、机器人等)类型")
    private Integer deviceType;

    @ApiModelProperty(value = "绑定预置位列表")
    private List<Long> presetId;

}
