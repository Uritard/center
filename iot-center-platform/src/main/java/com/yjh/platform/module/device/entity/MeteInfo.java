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
@ApiModel(value = "MeteInfo", description = "测点模板信息表")

public class MeteInfo implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "测点ID")
    private Long meteId;


    @ApiModelProperty(value = "测点名称")
    private String meteName;


}
