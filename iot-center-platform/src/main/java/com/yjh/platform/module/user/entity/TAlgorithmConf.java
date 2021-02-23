package com.yjh.platform.module.user.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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

/**
 * @author lqh
 * @since 2020-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmConf对象", description = "算法配置表")
public class TAlgorithmConf implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "摄像头预置位ID或者机器人巡检点ID")
    @TableField(value = "preset_id",updateStrategy = FieldStrategy.IGNORED)
    private Long presetId;

    @Max(value=999999999999999999l)
    @TableField(value = "algorithm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;

    @Length(max = 68,message = "configName长度必须小于等于68")
    @ApiModelProperty(value = "算法配置名称")
    @TableField(value = "config_name",updateStrategy = FieldStrategy.IGNORED)
    private String configName;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "状态")
    private Integer status;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "是否删除")
    private Integer ifDel;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "是否展示1展示，2不展示")
    private Integer ifShow;

    @Length(max = 255,message = "picUrl长度必须小于等于255")
    @ApiModelProperty(value = "图标路径")
     @TableField(value = "pic_url",updateStrategy = FieldStrategy.IGNORED)
    private String picUrl;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "0不应用，1应用到日常巡视，2..待定")
    @TableField(value = "apply_module",updateStrategy = FieldStrategy.IGNORED)
    private Integer applyModule;


    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;


    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
