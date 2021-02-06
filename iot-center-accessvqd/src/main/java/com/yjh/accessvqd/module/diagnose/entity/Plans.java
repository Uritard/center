package com.yjh.accessvqd.module.diagnose.entity;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author czh
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "诊断计划对象", description = "视频诊断-诊断计划")
public class Plans extends PlanInfo implements Serializable  {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "是否检测，0-不检测 1-检测")
    private String checkFlag;
    @ApiModelProperty(value = "-1：立即计划，0-星期计划")

    private String period;
    @ApiModelProperty(value = "执行星期")
    private List<String> weeks;
    @ApiModelProperty(value = "诊断结束时间1")
    private String startTime1;
    @ApiModelProperty(value = "诊断结束时间1")
    private String endTime1;
    @ApiModelProperty(value = "诊断开始时间2")
    private String startTime2;
    @ApiModelProperty(value = "诊断结束时间2")
    private String endTime2;
    @ApiModelProperty(value = "诊断结束时间3")
    private String startTime3;
    @ApiModelProperty(value = "诊断结束时间3")
    private String endTime3;
    @ApiModelProperty(value = "诊断开始时间4")
    private String startTime4;
    @ApiModelProperty(value = "诊断结束时间4")
    private String endTime4;
    @ApiModelProperty(value = "诊断结束时间5")
    private String startTime5;
    @ApiModelProperty(value = "诊断结束时间5")
    private String endTime5;
    @ApiModelProperty(value = "诊断开始时间6")
    private String startTime6;
    @ApiModelProperty(value = "诊断结束时间6")
    private String endTime6;
    @ApiModelProperty(value = "诊断结束时间7")
    private String startTime7;
    @ApiModelProperty(value = "诊断结束时间7")
    private String endTime7;
    @ApiModelProperty(value = "是否循环 0-不启用 1-启用")
    private String repeat;
    @ApiModelProperty(value = "信号丢失 0-不检测 1-检测")
    private String signal;
    @ApiModelProperty(value = "图像模糊")
    private String blur;
    @ApiModelProperty(value = "对比度")
    private String contrast;
    @ApiModelProperty(value = "图像过亮")
    private String bright;
    @ApiModelProperty(value = "图像过暗")
    private String dark;
    @ApiModelProperty(value = "图像偏色")
    private String chroma;
    @ApiModelProperty(value = "黑白图像")
    private String mono;
    @ApiModelProperty(value = "噪声干扰")
    private String noise;
    @ApiModelProperty(value = "条纹干扰")
    private String streak;
    @ApiModelProperty(value = "画面冻结")
    private String freeze;
    @ApiModelProperty(value = "视频抖动")
    private String shake;
    @ApiModelProperty(value = "视频剧变")
    private String flash;
    @ApiModelProperty(value = "场景变换")
    private String scene;
    @ApiModelProperty(value = "视频遮挡")
    private String cover;
    @ApiModelProperty(value = "云台失控")
    private String ptz;
    @ApiModelProperty(value = "监测点ID List")
    private List<String> taskList;


}
