package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/11/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseType对象扩展", description = "巡视类型关联实例点表扩展")
public class TCruiseTypeDetail extends TCruiseType{

    private String instanceName;

    private Long deviceId;

    private String deviceName;

    private Long customId;

    private String customName;

    private Integer cruiseType;

    private String cruiseTypeName;
}

