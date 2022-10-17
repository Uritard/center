package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UPatrolResult对象", description = "巡检任务结果表")
public class UPatrolResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检任务ID")
    private String taskId;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "巡检任务名称")
    private String taskName;

    @ApiModelProperty(value = "区域id")
    private String areaId;

    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer taskType;

    @ApiModelProperty(value = "执行类型（172.周期，173.立即，174.定期）")
    private Integer executeType;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "任务来源（暂时没用）")
    private Integer taskSource;

    @ApiModelProperty(value = "任务等级(从高到低):4级,3级,2级,1级")
    private Integer taskLevel;

    @ApiModelProperty(value = "任务状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期")
    private Integer taskState;

    @ApiModelProperty(value = "状态修正值（暂时没用）")
    private Integer modifyState;

    @ApiModelProperty(value = "总巡检点数")
    private Integer taskCount;

    @ApiModelProperty(value = "待巡检点数")
    private Integer taskWait;

    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @ApiModelProperty(value = "微气象")
    private String weather;

    @ApiModelProperty(value = "巡检开始时间")
    private Date createTime;

    @ApiModelProperty(value = "巡检结束时间")
    private Date endTime;

    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @ApiModelProperty(value = "巡视点是否全部审核完成，1-是0-否")
    private String isReview;

    @ApiModelProperty(value = "巡检点异常数量")
    private Integer taskAbnormal;

    @ApiModelProperty(value = "巡视结果：0-正常 1-异常")
    private Integer cruiseResult;

    @ApiModelProperty(value = "备注")
    private String remark;


}
