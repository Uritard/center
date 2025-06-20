package com.yjh.platform.module.simple.entity;

import lombok.Data;

import java.util.List;
/**
 * si300初始任务实体类
 *
 * @author llf
 * @date 2025/6/12
 */
@Data
public class InitialTask {

    /**
     * 设备ID
     */
    private List<Long> devices;
    /**
     * 机器人ID
     */
    private Long robotId;
}
