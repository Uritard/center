package com.yjh.accesstcp.module.device.service.uphandler.tek.entitiy;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/12/06
 */
@Data
public class TaskStatusEntity {

    /**
     *计划号（为任务下发时的计划号）
     */
    private String planNo;
    /**
     *任务编号（为任务下发时的任务编号）
     */
    private String taskNo;
    /**
     *巡检方式 0-机器人 1-相机
     */
    private String patrolDeviceType;
    /**
     *0-待检测 1-检测中 2-已检测 3-已取消 4-已暂停
     */
    private String taskStatus;
    /**
     *任务开始时间，13位毫秒时间戳
     */
    private Long taskStart;
    /**
     *任务结束时间，13位毫秒时间戳，当任务已完成或者
     * 已取消时，结束时间不能为空
     */
    private Long taskStop;

}
