package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/11 - 9:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDictBussinessExpand对象", description = "字典表拓展")
public class TDictBussinessExpand implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典编码")
    private String dictCode;
    @ApiModelProperty(value = "字典描述")
    private String dictNote;
}
