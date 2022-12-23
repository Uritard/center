package com.yjh.platform.module.task.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/** 统计信息 */
@Data
@Accessors(chain = true)
public class Statistics {
  /** 日期 格式：yyyy-MM-dd */
  private String day;
  /** 第几周 */
  private Integer week;
  /** 日期 格式：yyyy-MM */
  private String month;
  /** 统计符合的数量 */
  private Integer validNum;
  /** 总数 */
  private Integer totalNum;
  /** 百分比 */
  private String percent;
  /** 巡视时长*/
  private String duration;
  /** 缺陷类型与数量的映射关系集合*/
  private List<StatisticalDefectMapping> defectMappings;
}
