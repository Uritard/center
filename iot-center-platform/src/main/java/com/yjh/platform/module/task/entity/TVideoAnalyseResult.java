package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TVideoAnalyseResult对象", description = "算法结果表")
public class TVideoAnalyseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "算法分析结果ID")
    @TableId(value = "algorithm_result_id", type = IdType.AUTO)
    @TableField(value = "algorithm_result_id", updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmResultId;


    @ApiModelProperty(value = "算法分析时间")
    private Date analyseTime;

    @Length(max = 32, message = "analyseConfId长度必须小于等于32")
    @ApiModelProperty(value = "算法配置ID")
    @TableField(value = "analyse_conf_id", updateStrategy = FieldStrategy.IGNORED)
    private String analyseConfId;

    @Length(max = 256, message = "algorithmResult长度必须小于等于256")
    @ApiModelProperty(value = "分析结果")
    @TableField(value = "algorithm_result", updateStrategy = FieldStrategy.IGNORED)
    private String algorithmResult;

    @Length(max = 512, message = "algorithmPicture长度必须小于等于512")
    @ApiModelProperty(value = "图片地址")
    @TableField(value = "algorithm_picture", updateStrategy = FieldStrategy.IGNORED)
    private String algorithmPicture;

    @Max(value = 999999999)
    @ApiModelProperty(value = "结果类型")

    private Integer algorithmStatus;

    @Length(max = 128, message = "resultRate长度必须小于等于128")
    @ApiModelProperty(value = "算法分析结果评价（0-准确，1-错误）")
    @TableField(value = "result_rate", updateStrategy = FieldStrategy.IGNORED)
    private String resultRate;

    @Length(max = 256, message = "resultDescribe长度必须小于等于256")
    @ApiModelProperty(value = "算法分析结果描述")
    @TableField(value = "result_describe", updateStrategy = FieldStrategy.IGNORED)
    private String resultDescribe;

    @Max(value = 999999999)
    @ApiModelProperty(value = "备用字段1")
    private Integer reserver;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
