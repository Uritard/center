package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2023/01/31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SilentConf", description = "静默监视配置")
public class SilentConf {

    private Long id;

    private Integer presetType;

    private String presetTypeName;

    private String recognizeType;

    private Integer chillTime;

    private Boolean editable;

}
