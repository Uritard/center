package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "站所统计数据", description = "站所统计数据")
public class RegionPath {

    private String regionName;

    private String regionPath;
}
