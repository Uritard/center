package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraGroup对象", description = "相机分组表")
public class TCameraGroup implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "分组ID")
    private Long groupId;

    @ApiModelProperty(value = "分组名称")
    private String groupName;

    @ApiModelProperty(value = "相机ID")
    private String cameraIds;

    @ApiModelProperty(value = "备注")
    private String remarks;


}
