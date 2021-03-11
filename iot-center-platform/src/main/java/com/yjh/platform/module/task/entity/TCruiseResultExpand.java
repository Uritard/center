package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author YC
 * @date 2020/9/5 - 11:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseResultExpand对象", description = "巡检任务结果表扩充")
public class TCruiseResultExpand  {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "任务结果UUID")
    private String taskResultId;

    @ApiModelProperty(value = "巡检任务ID")
    private String taskId;

    @ApiModelProperty(value = "区域id")
    private String areaId;

    @ApiModelProperty(value = "巡检类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer cType;

    @ApiModelProperty(value = "当前状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期")
    private Integer cState;

    @ApiModelProperty(value = "状态修正值")
    private Integer modifyState;

    @ApiModelProperty(value = "检测点数")
    private Integer taskCount;

    @ApiModelProperty(value = "待检测点数")
    private Integer taskWait;

    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @ApiModelProperty(value = "微气象")
    private String weather;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡检时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "巡视点是否全部审核完成，1-是0-否")
    private String remark;
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "异常数")
    private Integer taskAbnormal;

    @ApiModelProperty(value = "任务状态")
    private String taskState;

    @ApiModelProperty(value = "巡检数据状态--已测点数")
    private Integer cruiseDataState1;

    @ApiModelProperty(value = "巡检数据状态--未处理")
    private Integer cruiseDataState2;

    @ApiModelProperty(value = "巡检类型")
    private String planType;

    @ApiModelProperty(value = "状态")
    private Integer cruiseStatus;
}
