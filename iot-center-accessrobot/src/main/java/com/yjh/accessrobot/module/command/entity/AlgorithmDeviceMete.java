package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * @author YC
 * @date 2021/2/3 16:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AlgorithmDeviceMete对象", description = "算法测点关联拓展表")
public class AlgorithmDeviceMete implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人测点编码")
    private String inspectionCode;
    @ApiModelProperty(value = "机器人测点Id")
    private Long inspectionId;
    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;
    @ApiModelProperty(value = "测点Id")
    private Long deviceMeteId;

    @Max(value=999999999999999999l)
    @Length(max = 64,message = "algorithmName长度必须小于等于64")
    @TableField(value = "algorithm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;
    @TableField(value = "algorithm_name",updateStrategy = FieldStrategy.IGNORED)
    private String algorithmName;

    @Length(max = 64,message = "aliasName长度必须小于等于64")
    @ApiModelProperty(value = "算法类型")
    @TableField(value = "alias_name",updateStrategy = FieldStrategy.IGNORED)
    private String aliasName;

    @Length(max = 20,message = "algorithmCode长度必须小于等于20")
    @ApiModelProperty(value = "算法编码")
    private String algorithmCode;

    @Length(max = 11,message = "analyseType长度必须小于等于11")
    @TableField(value = "analyse_type",updateStrategy = FieldStrategy.IGNORED)
    private String analyseType;

    @Max(value=9)
    private Integer isAi;

}
