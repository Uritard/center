package com.yjh.accessvqd.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "XMLBaseModel对象", description = "xml封装类")
public class XMLBaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "发送方唯一标识")
    private String SendCode;
    @ApiModelProperty(value = "接收方唯一标识")
    private String ReceiveCode;
    @ApiModelProperty(value = "目标对象唯一标识")
    private String Code;
    @ApiModelProperty(value = "消息类型")
    private String Type;
    @ApiModelProperty(value = "命令")
    private String Command;
    @ApiModelProperty(value = "时间")
    private String Time;
    @ApiModelProperty(value = "消息内容")
    private List<String> Items;
}