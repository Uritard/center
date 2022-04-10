package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2022/4/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "任务模型", description = "任务模型")
public class TaskModel {

    private String type;//巡视类型
    private String taskCode;//任务编码
    private String taskName;//任务名称
    private String priority;//优先级
    private String devicelevel;//设备层级
    private String devicelist;//设备列表
    private String fixedStartTime;//定期开始时间
    private String cycleMonth;//周期（月）
    private String cycleWeek;//周期（周）
    private String cycleExecuteTime;//周期执行时间
    private String cycleStartTime;//周期开始时间
    private String cycleEndTime;//周期结束时间
    private String intervalNumber;//间隔（数量）
    private String intervalType;//间隔类型
    private String intervalExecuteTime;//间隔执行时间
    private String intervalStartTime;//间隔开始时间
    private String intervalEndTime;//间隔结束时间
    private String invalidStartTime;//不可用开始时间
    private String invalidEndTime;//不可用结束时间
    private String isenable;//是否可用
    private String creator;//编制人
    private String createTime;//编制时间
}
