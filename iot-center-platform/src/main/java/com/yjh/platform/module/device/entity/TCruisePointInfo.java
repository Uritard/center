package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInfo对象", description = "巡检点实例信息表")
public class TCruisePointInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点实例ID")
    @TableId(value = "instance_id", type = IdType.AUTO)
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "测点实例ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @ApiModelProperty(value = "备注")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    private Integer pageNum=1;

    private Integer pageSize=0;

}