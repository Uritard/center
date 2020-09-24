package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/24 - 15:03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "HistoryStatistical对象", description = "历史统计")
public class YearEntity implements Serializable {

    @ApiModelProperty(value = "前1月")
    private Integer monthOne;
    @ApiModelProperty(value = "前2月")
    private Integer monthTwo;
    @ApiModelProperty(value = "前3月")
    private Integer monthThree;
    @ApiModelProperty(value = "前4月")
    private Integer monthFour;
    @ApiModelProperty(value = "前5月")
    private Integer monthFive;
    @ApiModelProperty(value = "前6月")
    private Integer monthSix;
    @ApiModelProperty(value = "前7月")
    private Integer monthSeven;
    @ApiModelProperty(value = "前8月")
    private Integer monthEight;
    @ApiModelProperty(value = "前9月")
    private Integer monthNine;
    @ApiModelProperty(value = "前10月")
    private Integer monthTen;
    @ApiModelProperty(value = "前11月")
    private Integer monthEleven;
    @ApiModelProperty(value = "前12月")
    private Integer monthTwelve;



}
