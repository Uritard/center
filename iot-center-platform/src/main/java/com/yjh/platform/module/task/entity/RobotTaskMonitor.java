package com.yjh.platform.module.task.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lqh
 * @since 2020/11/20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RobotTaskMonitor implements Serializable {
    private Integer cruiseType;

    private String taskId;

    private String taskName;

    private Integer priority;

    private Integer deviceLevel = 3;

    private List<String> deviceList;

}
