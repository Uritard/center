package com.yjh.platform.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 * 轨交首页大屏 3 * 2显示模块
 *
 * @author yanhao
 * @date 2022/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
public class TaskInfoBean {

    /**
     * 设备总数
     */
    private int totalDeviceCount;
    /**
     * 总监测点数
     */
    private int totalPointCount;
    /**
     * 当日异常数
     */
    private int curAlarmCount;
    /**
     * 当日未处理数
     */
    private int untreatedAlarmCount;
    /**
     * 任务执行总数
     */
    private  int totalTaskCount;
    /**
     * 执行异常任务总数
     */
    private int  abnormalTaskCount;
    /**
     * 当前执行任务
     */
    private int runningTaskCount;
}
