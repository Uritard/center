package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 巡检记录报表-总体情况
 *
 * @author YC
 * @date 2020/10/29 - 16:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TaskVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 站所名称
     */
    private String stationName;
    /**
     * 巡检任务名称
     */
    private String taskName;
    /**
     * 测点数
     */
    private Integer meteNum;
    /**
     * 巡检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date cruiseDate;
    /**
     * 电压等级
     */
    private String voltageClasses;
    /**
     * 变电站类别
     */
    private String stationType;
    /**
     * 环境信息
     */
    private String envInfo;
    /**
     * 审核人
     */
    private String reviewer;
    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;
    /**
     * 巡视开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseStartTime;
    /**
     * 巡视结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseEndTime;
    /**
     * 巡视统计
     */
    private String cruiseStatistics;
    /**
     * 巡视结论
     */
    private String cruiseConclusion;
    /**
     * 总点位
     */
    private Integer total;
    /**
     * 已检点位
     */
    private Integer already;
    /**
     * 未检点位
     */
    private Integer wait;
    /**
     * 正常点位
     */
    private Integer normal;
    /**
     * 异常点位
     */
    private Integer abnormal;
    /**
     * 待人工确认点位
     */
    private Integer unReview;


}
