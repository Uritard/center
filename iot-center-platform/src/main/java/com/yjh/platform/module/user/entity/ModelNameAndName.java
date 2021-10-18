package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author YChen
 * @date 2021/8/31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ModelNameAndName对象", description = "模型名称和名称")
public class ModelNameAndName {

    @ApiModelProperty(value = "模型名称列表")
    private List<String> modelNameList;
    @ApiModelProperty(value = "名称列表")
    private List<String> nameList;
}
