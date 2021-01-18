package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTask对象", description = "巡检任务表")
public class TCruiseTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务UUID")
    private String taskId;

    @Length(max = 32,message = "taskName长度必须小于等于32")
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "所属预案id")
    private Long planId;

    @Length(max = 32,message = "areaId长度必须小于等于32")
    @ApiModelProperty(value = "所属厂站")
    private String areaId;

    @Max(value=999999999)
    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否立即执行（172.周期，173.立即，174.定期）")
    private Integer ifRun;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @Length(max = 255,message = "dateType长度必须小于等于255")
    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private String dateType;

    @Max(value=999999999)
    @ApiModelProperty(value = "任务来源：1 日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务")
    private Integer taskType;

    @Max(value=99)
    @ApiModelProperty(value = "任务等级(从高到低):4级,3级,2级,1级")
    private Integer taskLevel;

    @Past
    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Past
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
