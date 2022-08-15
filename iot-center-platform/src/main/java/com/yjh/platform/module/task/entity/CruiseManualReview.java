package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/9/9 - 9:36
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseManualReview对象", description = "巡视点人工复核表")
public class CruiseManualReview {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡视点数据id")

    private Long cruiseDataId;
    @ApiModelProperty(value = "巡检任务id")
    private String taskResultId;
    @ApiModelProperty(value = "巡检任务id")
    private String taskId;
    @ApiModelProperty(value = "巡检点实例id")
    private Long instanceId;
    @Length(max = 128,message = "人工校核结果长度必须小于等于128")
    @ApiModelProperty(value = "人工校核结果")
    @TableField(value = "person_check",updateStrategy = FieldStrategy.IGNORED)
    private String personCheck;
    @ApiModelProperty(value = "评价状态")
    private Integer evaluationState;
    @ApiModelProperty(value = "评价状态-字典表")
    private String evaluationStateName;
    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;
    @ApiModelProperty(value = "实际结果-字典表")
    private String identifyResultName;
    @ApiModelProperty(value = "识别状态")
    private Integer identifyState;
    @ApiModelProperty(value = "识别状态-字典表")
    private String identifyStateName;
    @ApiModelProperty(value = "审核人")
    @TableField(value = "check_user",updateStrategy = FieldStrategy.IGNORED)
    private String checkUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;
    @ApiModelProperty(value = "执行时间")
    private String executeTime;

}
