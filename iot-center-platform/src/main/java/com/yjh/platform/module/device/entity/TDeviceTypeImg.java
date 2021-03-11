package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021-03-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDeviceTypeImg对象", description = "")
public class TDeviceTypeImg implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "类型")
    private String typeId;

    @ApiModelProperty(value = "图片绝对路径")
    private String picAbspath;

    @ApiModelProperty(value = "图片相对路径")
    private String picRealpath;

    @ApiModelProperty(value = "描述")
    private String remake;


}
