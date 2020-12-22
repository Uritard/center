package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmConfBak对象", description = "算法配置表2")
public class TAlgorithmConfBak implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备标准测点ID")
    private Long deviceMeteId;

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

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;


}
