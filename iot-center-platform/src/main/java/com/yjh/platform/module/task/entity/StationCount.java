package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "站所统计数据", description = "站所统计数据")
public class StationCount {

    private String type;

    private Integer count;

    //1:机器人，2：无人机，3：视频设备，4：物联设备
    private  String deviceType;


}
