package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Length(max = 50,message = "taskResultId长度必须小于等于50")
    @ApiModelProperty(value = "任务结果UUID")
    private String taskResultId;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务ID")
    private String taskId;

    @Length(max = 50,message = "taskName长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务ID")
    private String taskName;

    @Length(max = 32,message = "planName长度必须小于等于32")
    @ApiModelProperty(value = "区域id")
    private String areaId;

    @Max(value=999999999)
    @ApiModelProperty(value = "巡检类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer cType;

    @Max(value=999999999)
    @ApiModelProperty(value = "当前状态  -1.数据异常 0.正在执行 1.执行完成 2.任务暂停 3.任务终止 4任务异常终止5. 任务超期")
    private Integer cState;

    @Max(value=999999999)
    @ApiModelProperty(value = "状态修正值")
    private Integer modifyState;

    @Max(value=999999999)
    @ApiModelProperty(value = "检测点数")
    private Integer taskCount;

    @Max(value=999999999)
    @ApiModelProperty(value = "待检测点数")
    private Integer taskWait;

    @Length(max = 128,message = "checkUser长度必须小于等于128")
    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @Length(max = 255,message = "weather长度必须小于等于255")
    @ApiModelProperty(value = "微气象")
    private String weather;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡检时间")
    private Date createTime;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @Length(max = 32,message = "taskCode长度必须小于等于32")
    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @Length(max = 256,message = "remark长度必须小于等于256")
    @ApiModelProperty(value = "备用字段3")
    private String remark;


}
