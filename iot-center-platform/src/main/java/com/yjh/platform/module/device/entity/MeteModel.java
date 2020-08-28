package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MeteModel", description = "子模版信息")

public class MeteModel implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "模板ID")
    @TableId(value = "model_id", type = IdType.AUTO)
    private Long modelId;

    @ApiModelProperty(value = "模板名称")
    private String modelName;

    @ApiModelProperty(value = "所属设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "层级标志")
    private int level=2;
}
