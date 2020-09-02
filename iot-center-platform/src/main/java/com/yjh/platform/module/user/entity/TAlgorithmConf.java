package com.yjh.platform.module.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/9/2 - 11:15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmConf对象", description = "算法配置表")
public class TAlgorithmConf  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像头预置位ID或者机器人巡检点ID")
    private String insId;

    private Long algorithmId;

    @ApiModelProperty(value = "预置点名称")
    private String insName;

    @ApiModelProperty(value = "巡检设备ID")
    private Long inspectiondevId;

    @ApiModelProperty(value = "算法名称")
    private String algorithmName;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "是否删除")
    private Integer ifDel;

    @ApiModelProperty(value = "是否展示 1展示，2不展示")
    private Integer ifShow;

    @ApiModelProperty(value = "图标路径")
    private String picUrl;

    @ApiModelProperty(value = "0不应用，1应用到日常巡视，2..待定")
    private Integer applyModule;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "修改时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;





}
