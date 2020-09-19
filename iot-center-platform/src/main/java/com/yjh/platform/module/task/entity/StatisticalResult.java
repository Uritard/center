package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author YC
 * @date 2020/9/18 - 17:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StatisticalToolsList对象", description = "统计实体类拓展")
public class StatisticalResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "时间")
    private String timeNode;

    private List<StatisticalTools> statisticalList;

}
