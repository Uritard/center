package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 巡检记录报表
 *
 * @author YC
 * @date 2020/10/29 - 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportData {

    /**
     * 总体情况
     */
    TaskVO taskVO;
    /**
     * 分项预览
     */
    List<CheckPointType> cpTypeItems;
    /**
     * 环境监测
     */
    List<EnvironmentResult> evnRecordList;
    /**
     * 关联设备
     */
    List<RelationDevice> rcpRecordList;
    /**
     * 明细
     */
    List<TCruiseDataResultDetail> tCDRDList;
}
