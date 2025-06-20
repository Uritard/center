package com.yjh.platform.module.simple.entity;

import lombok.Data;

/**
 * si300初始任务状态实体类
 *
 * @author llf
 * @date 2025/6/12
 */
@Data
public class InitialTaskStatus {

    /**
     * 初始任务ID
     */
    private String taskId;

    /**
     * 初始任务状态code
     */
    private int initStatus;

    /**
     * 初始任务状态名称
     */
    private String  initStatusName;
}
