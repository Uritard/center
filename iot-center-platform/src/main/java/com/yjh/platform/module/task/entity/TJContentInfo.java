package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/13 - 19:33
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TJContentInfo对象", description = "统计信息表")
public class TJContentInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "统计内容名称")
    private String content;
    @ApiModelProperty(value = "告警个数")
    private Number count;

}
