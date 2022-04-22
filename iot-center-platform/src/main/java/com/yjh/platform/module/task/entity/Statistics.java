package com.yjh.platform.module.task.entity;

import lombok.Data;

/** 统计信息 */
@Data
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
}
