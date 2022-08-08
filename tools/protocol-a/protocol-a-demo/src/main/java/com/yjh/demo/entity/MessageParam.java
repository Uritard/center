package com.yjh.demo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2022/07/22
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "消息", description = "消息")
public class MessageParam {

    @ApiModelProperty(value = "报文")
    private String xml;
}
