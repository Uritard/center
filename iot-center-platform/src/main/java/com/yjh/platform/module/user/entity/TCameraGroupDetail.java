package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2020/11/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraGroup对象扩展", description = "相机分组表扩展")
public class TCameraGroupDetail extends TCameraGroup{
    List<Camera>  cameraList;
}
