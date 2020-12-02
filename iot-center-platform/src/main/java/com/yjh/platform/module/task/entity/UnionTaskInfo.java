package com.yjh.platform.module.task.entity;

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

    private Date cruiseTime;

    private String resultNum;

    private String origpic;

    private String personCheck;

}
