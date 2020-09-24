package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YC
 * @date 2020/9/24 - 17:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "HistoryStatistical对象", description = "历史统计")
public class MonthEntity {

    @ApiModelProperty(value = "前1天")
    private Integer DayOne;
    @ApiModelProperty(value = "前2天")
    private Integer DayTwo;
    @ApiModelProperty(value = "前3天")
    private Integer DayThree;
    @ApiModelProperty(value = "前4天")
    private Integer DayFour;
    @ApiModelProperty(value = "前5天")
    private Integer DayFive;
    @ApiModelProperty(value = "前6天")
    private Integer DaySix;
    @ApiModelProperty(value = "前7天")
    private Integer DaySeven;
    @ApiModelProperty(value = "前8天")
    private Integer DayEight;
    @ApiModelProperty(value = "前9天")
    private Integer DayNine;
    @ApiModelProperty(value = "前10天")
    private Integer DayTen;
    @ApiModelProperty(value = "前11天")
    private Integer DayEleven;
    @ApiModelProperty(value = "前12天")
    private Integer DayTwelve;
    @ApiModelProperty(value = "前13天")
    private Integer DayThirteen;
    @ApiModelProperty(value = "前14天")
    private Integer DayFourteen;
    @ApiModelProperty(value = "前15天")
    private Integer DayFifteen;
    @ApiModelProperty(value = "前16天")
    private Integer DaySixteen;
    @ApiModelProperty(value = "前17天")
    private Integer DaySeventeen;
    @ApiModelProperty(value = "前18天")
    private Integer DayEighteen;
    @ApiModelProperty(value = "前19天")
    private Integer DayNineteen;
    @ApiModelProperty(value = "前20天")
    private Integer DayTwenty;
    @ApiModelProperty(value = "前21天")
    private Integer Day21;
    @ApiModelProperty(value = "前22天")
    private Integer Day22;
    @ApiModelProperty(value = "前23天")
    private Integer Day23;
    @ApiModelProperty(value = "前24天")
    private Integer Day24;
    @ApiModelProperty(value = "前25天")
    private Integer Day25;
    @ApiModelProperty(value = "前26天")
    private Integer Day26;
    @ApiModelProperty(value = "前27天")
    private Integer Day27;
    @ApiModelProperty(value = "前28天")
    private Integer Day28;
    @ApiModelProperty(value = "前29天")
    private Integer Day29;
    @ApiModelProperty(value = "前30天")
    private Integer Day30;
}
