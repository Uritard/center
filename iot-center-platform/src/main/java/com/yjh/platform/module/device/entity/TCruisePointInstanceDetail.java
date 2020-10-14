package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lqh
 * @since 2020/9/3
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstance对象扩充", description = "巡检点实例表的扩充")
public class TCruisePointInstanceDetail extends TCruisePointInstance{

    private String meteId;

    private Integer meteKind;

    private List<Long> ids;

    private String meteName;

}
