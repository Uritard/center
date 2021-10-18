package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author YChen
 * @date 2021/8/20
 */
@ApiModel(value = "RoutePlan",description = "沉浸式路径规划")
public class RoutePlan {
    @ApiModelProperty(value = "x坐标")
    private String xCoordinate;
    @ApiModelProperty(value = "y坐标")
    private String yCoordinate;
    @ApiModelProperty(value = "z坐标")
    private String zCoordinate;
    @ApiModelProperty(value = "点位顺序")
    private int order;

    public String getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(String xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public String getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(String yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public String getzCoordinate() {
        return zCoordinate;
    }

    public void setzCoordinate(String zCoordinate) {
        this.zCoordinate = zCoordinate;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


}
