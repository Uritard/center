package com.yjh.accesstcp.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.KeyValue;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWarnInfo对象", description = "告警信息表")
public class TWarnInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否告警")
    private Integer isWarn;

    @Length(max = 32,message = "dealPersonId长度必须小于等于32")
    @ApiModelProperty(value = "确认人ID")
    @TableField(value = "deal_person_id",updateStrategy = FieldStrategy.IGNORED)
    private String dealPersonId;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "确认时间")
    private Date dealTime;

    @Length(max = 512,message = "taskId长度必须小于等于512")
    @ApiModelProperty(value = "任务ID")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    private String edgeCode;
    /**
     * 巡检点ID集合
     */
    private String instanceIds;
}
