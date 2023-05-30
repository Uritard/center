package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
    private String evaluationStateName;
    /**
     * 巡视类型
     */
    private Integer cruiseType;
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
     * 数据来源
     */
    private String dataType;

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

}
