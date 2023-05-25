package com.yjh.platform.module.config.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2023/04/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ConfigTreeNode {

    private String id;

    private String upId;

    private String configName;

    private String configKey;

    private String configType;

    private String configValue;

    private String remark;

    private String rule;

    private String upName;


    List<ConfigTreeNode> child;
}
