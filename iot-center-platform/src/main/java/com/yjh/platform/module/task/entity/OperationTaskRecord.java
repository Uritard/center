package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "操作任务记录数据", description = "操作任务记录数据")
public class OperationTaskRecord {

    private String taskId;

    private String operateTypeName;

    private String duration;

    private String startTime;

    private String regionName;

    private String userName;

    private String mainOperatioTypeName;

}
