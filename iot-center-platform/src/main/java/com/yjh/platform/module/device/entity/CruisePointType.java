package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2020/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "标准设备测点巡检类型")
public class CruisePointType {



    private String cruiseType;

    private List<Map<Object,Object>> list = new LinkedList<>();
    private List<String> cruiseIdList = new LinkedList<>();

    private String cruiseTypeName;
}
