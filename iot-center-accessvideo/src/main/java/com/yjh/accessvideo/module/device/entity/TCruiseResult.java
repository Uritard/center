package com.yjh.accessvideo.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-10-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseResult对象", description = "巡检任务结果表")
public class TCruiseResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "任务结果UUID")
    @TableField(value = "task_result_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskResultId;

    @ApiModelProperty(value = "巡检任务ID")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    private  String taskName;

    @ApiModelProperty(value = "区域id")
    @TableField(value = "area_id",updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(value = "check_user",updateStrategy = FieldStrategy.IGNORED)
    private String checkUser;

    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @ApiModelProperty(value = "微气象")
     @TableField(value = "weather",updateStrategy = FieldStrategy.IGNORED)
    private String weather;

    @ApiModelProperty(value = "巡检时间")
    private Date createTime;

    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @ApiModelProperty(value = "任务编码")
     @TableField(value = "task_code",updateStrategy = FieldStrategy.IGNORED)
    private String taskCode;

    @ApiModelProperty(value = "备用字段3")
     @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;


}
