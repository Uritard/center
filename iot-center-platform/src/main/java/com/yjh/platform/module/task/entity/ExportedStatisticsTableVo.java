package com.yjh.platform.module.task.entity;

import lombok.Data;

import java.util.List;

@Data
public class ExportedStatisticsTableVo {
    /**
     * 统计数据日期
     */
    private String date;
    /**
     *巡视任务执行次数
     */
    private Integer frequency;
    /**
     *巡视时长
     */
    private String duration;
    /**
     *识别出缺陷种类
     */
    private String defectAndCount;
    /**
     * 缺陷类型与数量
     */
    private List<StatisticalDefectMapping> defectMappings;
}
