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

import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-09-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskDel对象", description = "周期任务删除记录表")
public class TCruiseTaskDel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务UUID")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    @Past
    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "del_time",updateStrategy = FieldStrategy.IGNORED)
    private Date delTime;

    @Past
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;


}
