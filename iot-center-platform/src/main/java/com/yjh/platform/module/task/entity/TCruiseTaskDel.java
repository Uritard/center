package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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


    @ApiModelProperty(value = "巡检任务UUID")
    private String taskId;

    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date delTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


}
