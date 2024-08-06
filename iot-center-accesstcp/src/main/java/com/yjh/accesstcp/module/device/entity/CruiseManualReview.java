package com.yjh.accesstcp.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseManualReview对象", description = "巡视点人工复核表")
public class CruiseManualReview {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检任务id")
    private String taskResultId;
    @ApiModelProperty(value = "巡检点实例id")
    private Long instanceId;
    @ApiModelProperty(value = "人工校核结果")
    @TableField(value = "person_check",updateStrategy = FieldStrategy.IGNORED)
    private String personCheck;
    @ApiModelProperty(value = "审核人")
    @TableField(value = "check_user",updateStrategy = FieldStrategy.IGNORED)
    private String checkUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;
    @ApiModelProperty(value = "下级同步时需要，下级唯一标识")
    private String sendCode;
    @ApiModelProperty(value = "巡检点实例id集合")
    private String instanceIds;
    @ApiModelProperty(value = "人工审核结论")
    private String remark;
}
