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
@ApiModel(value = "TVideoAnalyseResult对象", description = "算法结果表")
public class TVideoAnalyseResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "算法分析结果ID")
    @TableId(value = "algorithm_result_id", type = IdType.AUTO)
    private Long algorithmResultId;

    @ApiModelProperty(value = "算法分析时间")
    private Date analyseTime;

    @ApiModelProperty(value = "算法配置ID")
    private String analyseConfId;

    @ApiModelProperty(value = "分析结果")
    private String algorithmResult;

    @ApiModelProperty(value = "图片地址")
    private String algorithmPicture;

    @ApiModelProperty(value = "结果类型")
    private Integer algorithmStatus;

    @ApiModelProperty(value = "算法分析结果评价（0-准确，1-错误）")
    private String resultRate;

    @ApiModelProperty(value = "算法分析结果描述")
    private String resultDescribe;

    @ApiModelProperty(value = "备用字段1")
    private Integer reserver;


}
