package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/8/28 - 16:35
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotRegion对象", description = "机器人区域层级表")
public class TRobotRegion implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "区域ID")
    private Long regionId;

    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @ApiModelProperty(value = "区域类型（1:国家,2:省份、直辖市,3:运维站,4:变电站,5:间隔,6:设备,7:部位）")
    private Integer sort;

    @ApiModelProperty(value = "上级区域ID")
    private Long upRegionId;

    @ApiModelProperty(value = "区域ID层级")
    private String upRegionIds;

    @ApiModelProperty(value = "类型区域，标准测点区域类型：100；E机器人区域类型：101；相机区域类型：102")
    private Integer regionType;

    @ApiModelProperty(value = "变电站ID")
    private String stationId;

    @ApiModelProperty(value = "0:非当前变电站 1：当前变电站")
    private Integer state;

    private Date createTime;

    private  String stationName;

}
