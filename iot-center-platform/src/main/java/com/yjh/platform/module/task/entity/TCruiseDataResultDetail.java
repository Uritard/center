package com.yjh.platform.module.task.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;

/**
 * 巡检记录报表-明细
 *
 * @author YC
 * @date 2020/10/29 - 14:46
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TCruiseDataResultDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 巡视设备
     */
    private String deviceName;
    /**
     * 巡视点
     */
    private String instanceName;
    /**
     * 巡视值
     */
    private String resultNum;
    /**
     * 巡视值带
     */
    private String resultDesc;
    /**
     * 图片
     */
    private String picPath;
    /**
     * 识别状态
     */
    private Integer cruiseResult;
    /**
     * 识别状态
     */
    private String cruiseResultName;
    /**
     * 巡视时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;
    /**
     * 序号
     */
    private int px;
    /**
     * 实物编码
     */
    private String realCode;
    /**
     * 审核值
     */
    private String personCheck;

    /**
     * 审核结果
     */
    private Integer identifyResult;
    /**
     * 审核结果
     */
    private String identifyResultName;
    /**
     * 审核状态
     */
    private Integer evaluationState;
    /**
     * 审核状态
     */
    private String evaluationStateName;
    /**
     * 巡视类型
     */
    private Integer cruiseType;
    /**
     * 上级区域ID
     */
    private Long regionId;
    /**
     * 区域
     */
    private String regionName;
    /**
     * 间隔
     */
    private String intervalName;
    /**
     * 部件
     */
    private String componentName;
    /**
     * 状态一描述
     */
    private String stateZero;
    /**
     * 状态二描述
     */
    private String stateOne;
    /**
     * 告警上限1
     */
    private Float highLimit1;
    /**
     * 告警下限1
     */
    private Float lowLimit1;
    /**
     * 告警上限2
     */
    private Float highLimit2;
    /**
     * 告警下限2
     */
    private Float lowLimit2;
    /**
     * 告警上限3
     */
    private Float highLimit3;
    /**
     * 告警下限3
     */
    private Float lowLimit3;
    /**
     * 告警上限4
     */
    private Float highLimit4;
    /**
     * 告警下限4
     */
    private Float lowLimit4;
    /**
     * 测点类型:0-遥信，1-遥测
     */
    private Integer meteKind;
    /**
     * 告警等级
     */
    private Integer alarmLevel;
    /**
     * 告警级别
     */
    private String alarmLevelName;
    /**
     * 告警规则
     */
    private Integer alarmRuleType;
    /**
     * 告警规则名称
     */
    private String alarmRuleTypeName;
    /**
     * 告警状态
     */
    private Integer alarmState;
    /**
     * 测点类型
     */
    private Integer meteType;
    /**
     * 测点类型名称
     */
    private String meteTypeName;
    /**
     * 分贝告警值
     */
    private String dbValue;
    /**
     * 频率限值
     */
    private String fValue;
    /**
     * 数据来源
     */
    private String dataType;
    /**
     * 巡视设备名称
     */
    private String cruiseDeviceName;
    /**
     * 原始图片
     */
    private String oriImg;

    /**
     * 状态:0-已执行 1-未执行 2-执行失败 3-未知
     */
    private Integer cruiseState;

    /**
     * 状态:0-已执行 1-未执行 2-执行失败 3-未知
     */
    private Integer cruiseAbnormal;

    private Integer mergeCount;

    private Long instanceId;

    private  Integer isWarn;

    /**
     * 任务结果对象
     * */
     private TaskVO taskVO;

     private Integer redundantType;

     private String redundantTypeName;

     private Integer identifyState;

}
