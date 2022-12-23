package com.yjh.platform.module.task.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author YJH
 * 缺陷统计信息映射关系类
 */
@Data
@Accessors(chain = true)
public class StatisticalDefectMapping {
    /** 日期 格式：yyyy-MM-dd */
    private String day;
    /** 第几周*/
    private Integer week;
    /**日期 格式：yyyy-MM */
    private String month;
    /**识别到的缺陷类型 */
    private String defectType;
    /** 识别到的缺陷数量*/
    private Integer count;
}
