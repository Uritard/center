package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
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

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "巡视点数据id")
    @TableId(value = "cruise_data_id", type = IdType.AUTO)
    @TableField(value = "cruise_data_id", updateStrategy = FieldStrategy.IGNORED)
    private Long cruiseDataId;

    @Length(max = 82, message = "cruiseResultId长度必须小于等于82")
    @ApiModelProperty(value = "巡视任务结果id")
    @TableField(value = "cruise_result_id", updateStrategy = FieldStrategy.IGNORED)
    private String cruiseResultId;

    @Max(value = 999999999999999999l)
    @TableField(value = "cruise_id", updateStrategy = FieldStrategy.IGNORED)
    private Long cruiseId;

    @Length(max = 50, message = "cruiseName长度必须小于等于50")
    @TableField(value = "cruise_Name", updateStrategy = FieldStrategy.IGNORED)
    private String cruiseName;

    @Max(value = 999999999)
    @ApiModelProperty(value = "巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer cruiseType;

    @Length(max = 512, message = "resultDesc长度必须小于等于512")
    @ApiModelProperty(value = "巡检结果文字描述")
    @TableField(value = "result_desc", updateStrategy = FieldStrategy.IGNORED)
    private String resultDesc;

    @Length(max = 100, message = "cruiseResultId长度必须小于等于100")
    @ApiModelProperty(value = "巡检结果数值")
    @TableField(value = "result_num", updateStrategy = FieldStrategy.IGNORED)
    private String resultNum;

    @Length(max = 100, message = "modifyNum长度必须小于等于100")
    @ApiModelProperty(value = "修正值")
    @TableField(value = "modify_num", updateStrategy = FieldStrategy.IGNORED)
    private String modifyNum;

    @Length(max = 256, message = "picpath长度必须小于等于256")
    @ApiModelProperty(value = "巡检分析图片")
    @TableField(value = "picpath", updateStrategy = FieldStrategy.IGNORED)
    private String picpath;

    @Length(max = 256, message = "personCheck长度必须小于等于256")
    @ApiModelProperty(value = "人工校核结果")
    @TableField(value = "person_check", updateStrategy = FieldStrategy.IGNORED)
    private String personCheck;

    @Length(max = 256, message = "origpic长度必须小于等于256")
    @ApiModelProperty(value = "算法原始图片/红外可见光")
    @TableField(value = "origpic", updateStrategy = FieldStrategy.IGNORED)
    private String origpic;

    @Max(value = 999999999)
    @ApiModelProperty(value = "巡视结果")
    private Integer cruiseResult;

    @Max(value = 999999999)
    @ApiModelProperty(value = "巡视异常类型 -1数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    private Integer cruiseAbnormal;

    @Max(value = 999999999)
    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @Max(value = 999999999)
    @ApiModelProperty(value = "识别状态 1识别正常 2识别异常")
    private Integer identifyState;

    @Max(value = 999999999)
    @ApiModelProperty(value = "实际结果 1正常 2异常")
    private Integer identifyResult;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    @Length(max = 256, message = "remark长度必须小于等于256")
    @ApiModelProperty(value = "备用字段3")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Max(value = 999999999)
    @ApiModelProperty(value = "是否产生告警1.是0.否")
    private Integer isWarn;

    @Length(max = 32, message = "checkUser长度必须小于等于32")
    @ApiModelProperty(value = "审核人")
    @TableField(value = "check_user", updateStrategy = FieldStrategy.IGNORED)
    private String checkUser;


    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkDate;

    @ApiModelProperty(value = "机器人巡检图片分析,相对")
    private String picPathAnl;
    @ApiModelProperty(value = "机器人巡检图片分析,绝对")
    private String origPicAnl;


    @ApiModelProperty(value = "红外FIR文件存储路径")
    private String resultPic;

    @ApiModelProperty(value = "FIR文件名称")
    private String firName;

    @ApiModelProperty(value = "FIR文件生成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date firDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private String voicePath;

    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
