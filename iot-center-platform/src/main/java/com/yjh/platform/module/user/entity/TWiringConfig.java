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
 * @author 丫C
 * @since 2023-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWiringConfig对象", description = "主接线图与设备关联表")
public class TWiringConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long wiringConfigId;

    @ApiModelProperty(value = "主接线图id")
    private Integer wiringDiagramId;

    @ApiModelProperty(value = "关联设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "关联设备名称")
    private String equipmentName;

    @ApiModelProperty(value = "关联设备类型")
    private Integer equipmentType;

    @ApiModelProperty(value = "坐标位置x")
    private Float xCoordinate;

    public Float getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(Float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Float getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(Float yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    @ApiModelProperty(value = "坐标位置y")
    private Float yCoordinate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime ;

    @ApiModelProperty(value = "更新人")
    private String updatePerson;
}
