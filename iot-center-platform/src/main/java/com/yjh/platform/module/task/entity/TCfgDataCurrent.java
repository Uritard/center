package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgDataCurrent对象", description = "实时数据表")
public class TCfgDataCurrent implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "监控量编号")
    private Long meteId;

    @ApiModelProperty(value = "设备编号")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    @ApiModelProperty(value = "数值时间")
    private Date recordTime;

    @ApiModelProperty(value = "监控量种类")
    private Integer meteKind;

    @ApiModelProperty(value = "区域编号")
    private String regionId;

    @ApiModelProperty(value = "本次四遥值")
    private String meteValue;

    @ApiModelProperty(value = "上一次值")
    private String lastMeteValue;


}
