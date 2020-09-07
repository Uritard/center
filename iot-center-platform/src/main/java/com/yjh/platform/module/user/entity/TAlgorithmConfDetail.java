package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/9/7
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmConf对象", description = "算法配置表")
public class TAlgorithmConfDetail  extends TAlgorithmConf{
    private String deviceName;
    private String presetName;
    private String analysTypeName;
    private String analysType;
}
