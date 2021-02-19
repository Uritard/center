package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2020/12/1
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "联动任务对象", description = "联动任务对象")
public class UnionTaskInfo implements Serializable {

    private String unionId;

    private String meteName;

    private String meteValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date triggeringTime;

    private String resultNum;

    private String origpic;

    private String personCheck;

    private String taskName;

    private Long deviceId;

    private String deviceName;

    private Integer meteKind;

    private String meteKindName;

    private String instanceName;

}
