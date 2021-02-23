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
 * @author tt
 * @since 2020-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdRegion对象", description = "标准区域表")
public class TStdRegion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "区域ID")
    @TableField(value = "region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long regionId;

    @Length(max = 128,message = "regionName长度必须小于等于128")
    @ApiModelProperty(value = "区域名称")
    @TableField(value = "region_name",updateStrategy = FieldStrategy.IGNORED)
    private String regionName;

    @Max(value=9)
    @ApiModelProperty(value = "区域类型（1:国家,2:省份、直辖市,3:运维站,4:变电站,5:间隔,6:设备,7:部位）")
    private Integer sort;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "上级区域ID")
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @Length(max = 255,message = "upRegionIds长度必须小于等于255")
    @ApiModelProperty(value = "区域ID层级")
    @TableField(value = "up_region_ids",updateStrategy = FieldStrategy.IGNORED)
    private String upRegionIds;

    @Max(value=999999999)
    @ApiModelProperty(value = "类型区域，标准测点区域类型：100；E机器人区域类型：101；相机区域类型：102")
    private Integer regionCode;

    @Length(max = 32,message = "stationId长度必须小于等于32")
    @ApiModelProperty(value = "变电站ID")
     @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @Max(value=9)
    @ApiModelProperty(value = "0:非当前变电站 1：当前变电站")
    private Integer state;


    private Date createTime;

    @Length(max = 32,message = "stationName长度必须小于等于32")
    private  String stationName;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}