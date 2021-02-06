package com.yjh.accessudp.module.device.entity;


import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
/**
 * @author lqh
 * @since 2020/11/5
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgDataCurrent对象", description = "实时数据表")
public class TCfgDataCurrent implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "监控量编号")
    @TableField(value = "mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long meteId;

    @ApiModelProperty(value = "设备编号")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    @TableField(value = "cunstom_id",updateStrategy = FieldStrategy.IGNORED)
    private String cunstomId;

    @ApiModelProperty(value = "数值时间")
    private Date recordTime;

    @ApiModelProperty(value = "监控量种类")
    private Integer meteKind;

    @ApiModelProperty(value = "区域编号")
    @TableField(value = "region_id",updateStrategy = FieldStrategy.IGNORED)
    private String regionId;

    @ApiModelProperty(value = "本次四遥值")
    @TableField(value = "mete_value",updateStrategy = FieldStrategy.IGNORED)
    private String meteValue;

    @ApiModelProperty(value = "上一次值")
    @TableField(value = "last_mete_value",updateStrategy = FieldStrategy.IGNORED)
    private String lastMeteValue;


}