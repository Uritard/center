package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgTelecontrol对象", description = "遥控量表")
public class TCfgTelecontrol implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @ApiModelProperty(value = "监控量编号")
    private String meteId;

    @ApiModelProperty(value = "监控量名称")
    private String meteName;

    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @ApiModelProperty(value = "可控状态")
    private Integer controlStatus;

    @ApiModelProperty(value = "控制使能条件表达式")
    private String enableString;

    @ApiModelProperty(value = "控制成功条件表达式")
    private String succeedString;

    @ApiModelProperty(value = "触发条件表达式")
    private String triggerString;

    @ApiModelProperty(value = "控制参数")
    private Integer controlValue;

    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "监控量描述")
    private String description;

    @ApiModelProperty(value = "态值描述")
    private String describer;


}
