package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseDataResult对象", description = "巡检点数据表")
public class TCruiseDataResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡视点数据id")
    @TableId(value = "cruise_data_id", type = IdType.AUTO)
    private Long cruiseDataId;

    @ApiModelProperty(value = "巡视任务结果id")
    private String cruiseResultId;

    private Long cruiseId;

    @ApiModelProperty(value = "巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer cruiseType;

    @ApiModelProperty(value = "巡检结果文字描述")
    private String resultDesc;

    @ApiModelProperty(value = "巡检结果数值")
    private String resultNum;

    @ApiModelProperty(value = "修正值")
    private String modifyNum;

    @ApiModelProperty(value = "巡检分析图片")
    private String picpath;

    @ApiModelProperty(value = "人工校核结果")
    private String personCheck;

    @ApiModelProperty(value = "算法原始图片/红外可见光")
    private String origpic;

    @ApiModelProperty(value = "巡视结果")
    private Integer cruiseResult;

    @ApiModelProperty(value = "巡视异常类型 -1数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    private Integer cruiseAbnormal;

    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @ApiModelProperty(value = "识别状态 1识别正常 2识别异常")
    private Integer identifyState;

    @ApiModelProperty(value = "实际结果 1正常 2异常")
    private Integer identifyResult;

    private Date createtime;

    @ApiModelProperty(value = "备用字段3")
    private String remark;

    private Integer isWarn;


}
