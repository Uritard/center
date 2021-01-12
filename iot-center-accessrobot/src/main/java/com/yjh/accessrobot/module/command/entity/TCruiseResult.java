package com.yjh.accessrobot.module.command.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseResult对象", description = "巡检任务结果表")
public class TCruiseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "cruiseDataId")
    @Length(min=1,max = 50,message = "taskResultId长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "任务结果UUID")
    private String taskResultId;

    @Length(min=1,max = 50,message = "taskId长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检任务ID")
    private String taskId;

    @Length(min=1,max = 50,message = "taskName长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检任务名称")
    private String taskName;

    @Length(min=1,max = 32,message = "areaId长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "区域id")
    private String areaId;

    @Range(min=0,max = 11,message = "cType长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer cType;

    @Range(min=0,max = 11,message = "cState长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "当前状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期")
    private Integer cState;

    @Range(min=0,max = 11,message = "modifyState长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "状态修正值")
    private Integer modifyState;

    @Range(min=0,max = 11,message = "taskCount长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "检测点数")
    private Integer taskCount;

    @Range(min=0,max = 11,message = "taskWait长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "待检测点数")
    private Integer taskWait;

    @Length(min=1,max = 128,message = "checkUser长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @Length(min=1,max = 255,message = "taskResultId长度必须在{min}-{max}之间")
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

    @Length(min=1,max = 32,message = "taskCode长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @Length(max = 256,message = "remark长度必须在{max}之间")
    @ApiModelProperty(value = "备用字段3")
    private String remark;


}
