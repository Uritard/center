package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

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

    @NotEmpty(message = "cruiseDataId")
    @Length(min=1,max = 68,message = "cruiseDataId长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡视点数据id")
    @TableId(value = "cruise_data_id", type = IdType.AUTO)
    private Long cruiseDataId;

    @Length(min=1,max = 82,message = "cruiseResultId长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡视任务结果id")
    private String cruiseResultId;

    @Length(min=1,max = 48,message = "cruiseId长度必须在{min}-{max}之间")
    private Long cruiseId;

    @Length(min=1,max = 50,message = "cruiseName长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检点名称")
    private String cruiseName;

    @Length(min=1,max = 11,message = "cruiseType长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer cruiseType;

    @Length(min=1,max = 512,message = "resultDesc长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检结果文字描述")
    private String resultDesc;

    @Length(min=1,max = 100,message = "resultNum长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检结果数值")
    private String resultNum;

    @Length(min=1,max = 100,message = "modifyNum长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "修正值")
    private String modifyNum;

    @Length(min=1,max = 256,message = "picpath长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡检分析图片")
    private String picpath;

    @ApiModelProperty(value = "操作前结果图片,相对")
    private String confirmPicPath;
    @ApiModelProperty(value = "操作前结果图片,绝对")
    private String origConfirmPicPath;

    @Length(min=1,max = 256,message = "personCheck长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "人工校核结果")
    private String personCheck;

    @Length(min=1,max = 256,message = "origpic长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "算法原始图片/红外可见光")
    private String origpic;

    @Length(min=1,max = 11,message = "cruiseResult长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡视执行结果")
    private Integer cruiseResult;

    @Length(min=1,max = 11,message = "cruiseAbnormal长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "巡视异常原因")
    private Integer cruiseAbnormal;

    @Length(min=1,max = 11,message = "evaluationState长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @Length(min=1,max = 11,message = "identifyState长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "识别状态 1识别正常 2识别异常")
    private Integer identifyState;

    @Length(min=1,max = 11,message = "identifyResult长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "实际结果 1正常 2异常")
    private Integer identifyResult;

    private Date createtime;

    @Length(max = 256,message = "remark长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "备用字段3")
    private String remark;

    @Length(min=1,max = 32,message = "checkUser长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @Length(min=1,max = 11,message = "isWarn长度必须在{min}-{max}之间")
    @ApiModelProperty(value = "是否产生告警1.是0.否")
    private Integer isWarn;

    @ApiModelProperty(value = "机器人巡检图片分析,相对")
    private String picPathAnl;
    @ApiModelProperty(value = "机器人巡检图片分析,绝对")
    private String origPicAnl;
    @ApiModelProperty(value = "FIR文件存储路径")
    private String resultPic;
    @ApiModelProperty(value = "红外FIR文件名称")
    private String firName;
    @ApiModelProperty(value = "红外FIR文件生成时间")
    private String firDate;
    @ApiModelProperty(value = "声纹文件地址")
    private String voicePath;

}
