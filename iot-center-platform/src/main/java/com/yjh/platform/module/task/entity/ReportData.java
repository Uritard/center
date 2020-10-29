package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author YC
 * @date 2020/10/29 - 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ReportData对象", description = "巡检记录报表")
public class ReportData {
    //1.总体情况
    TaskVO taskVO;
    //2.分项预览
    List<CheckPointType> cpTypeItems;
    //3.环境监测
    List<EnvironmentResult> evnRecordList;
    //4.关联设备
    List<RelationDevice> rcpRecordList;
    //5.明细
    List<TCruiseDataResultDetail> tCDRDList;
}
