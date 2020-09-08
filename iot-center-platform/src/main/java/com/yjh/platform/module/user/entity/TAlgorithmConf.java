package com.yjh.platform.module.user.entity;

import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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


    @ApiModelProperty(value = "摄像头预置位ID或者机器人巡检点ID")
    private Long presetId;

    private Long algorithmId;

    @ApiModelProperty(value = "算法配置名称")
    private String configName;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "是否删除")
    private Integer ifDel;

    @ApiModelProperty(value = "是否展示1展示，2不展示")
    private Integer ifShow;

    @ApiModelProperty(value = "图标路径")
    private String picUrl;

    @ApiModelProperty(value = "0不应用，1应用到日常巡视，2..待定")
    private Integer applyModule;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;




}
