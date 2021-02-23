package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgMete对象", description = "系统测点信息表")
public class TCfgMete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 20, message = "meteId长度必须小于等于20")
    @ApiModelProperty(value = "监控量编码")
    @TableField(value = "mete_id", updateStrategy = FieldStrategy.IGNORED)
    private String meteId;

    @Length(max = 20, message = "meteType长度必须小于等于20")
    @ApiModelProperty(value = "测点标准化编码")
    @TableField(value = "mete_type", updateStrategy = FieldStrategy.IGNORED)
    private String meteType;

    @Max(value = 999999999)
    @ApiModelProperty(value = "四遥类型 0：遥信，1：遥测，2：遥控，3：遥调，4：遥脉")
    private Integer meteKind;

    @Length(max = 128, message = "meteName长度必须小于等于128")
    @ApiModelProperty(value = "测点标准名")
    @TableField(value = "mete_name", updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @Length(max = 20, message = "meteCode长度必须小于等于20")
    @ApiModelProperty(value = "测点编码")
    @TableField(value = "mete_code", updateStrategy = FieldStrategy.IGNORED)
    private String meteCode;

    @Length(max = 50, message = "unit长度必须小于等于50")
    @ApiModelProperty(value = "单位")
    @TableField(value = "unit", updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @Length(max = 1000, message = "meteExplainType长度必须小于等于1000")
    @ApiModelProperty(value = "测点解释")
    @TableField(value = "mete_explain_type", updateStrategy = FieldStrategy.IGNORED)
    private String meteExplainType;


    @TableField(value = "create_time", updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;


    @TableField(value = "update_time", updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;

    @Max(value = 999999999)
    @ApiModelProperty(value = "系数")
    private Integer modulus;

    @Length(max = 64, message = "stationName长度必须小于等于64")
    @TableField(value = "station_name", updateStrategy = FieldStrategy.IGNORED)
    private String stationName;

    @Length(max = 28, message = "stationId长度必须小于等于28")
    @TableField(value = "station_id", updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @Max(value = 999999999)
    private Integer upEffect;

    @Max(value = 999999999)
    private Integer downEffect;

    @Max(value = 999999999)
    private Integer alarmlevel;

    @Max(value = 999999999)
    private Integer alarmthresbhold;

    @Length(max = 64, message = "describer长度必须小于等于64")
    @TableField(value = "describer", updateStrategy = FieldStrategy.IGNORED)
    private String describer;

    @Max(value = 999999999)

    private Integer metePrecision;

    @Max(value = 999999999)
    @TableField(value = "change_limit", updateStrategy = FieldStrategy.IGNORED)
    private Float changeLimit;

    @Max(value = 999999999)
    @TableField(value = "hilimit1", updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit1;

    @Max(value = 999999999)
    @TableField(value = "lolimit1", updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit1;

    @Max(value = 999999999)
    @TableField(value = "hilimit2", updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit2;

    @Max(value = 999999999)
    @TableField(value = "lolimit2", updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit2;

    @Max(value = 999999999)
    @TableField(value = "hilimit3", updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit3;

    @Max(value = 999999999)
    @TableField(value = "lolimit3", updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit3;

    @Max(value = 999999999)
    @TableField(value = "hilimit4", updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit4;

    @Max(value = 999999999)
    @TableField(value = "stander", updateStrategy = FieldStrategy.IGNORED)
    private Float stander;

    @Max(value = 999999999)
    private Integer controlenable;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
