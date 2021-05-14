package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

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

    @Length(max = 255,message = "图片绝对路径长度必须小于等于255")
    @ApiModelProperty(value = "图片绝对路径")
    private String picAbsPath;

    @Length(max = 255,message = "图片相对路径长度必须小于等于255")
    @ApiModelProperty(value = "图片相对路径")
    private String picRealPath;

    @Length(max = 255,message = "描述长度必须小于等于255")
    @ApiModelProperty(value = "描述")
    private String remake;


}
