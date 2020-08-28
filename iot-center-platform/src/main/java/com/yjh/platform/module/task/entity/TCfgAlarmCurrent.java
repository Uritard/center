package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgAlarmCurrent对象", description = "活动告警表")
public class TCfgAlarmCurrent implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "告警流水号")
    @TableId(value = "alarm_no", type = IdType.AUTO)
    private Long alarmNo;

    @ApiModelProperty(value = "设备编号")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    @ApiModelProperty(value = "监控量编号")
    private Long meteId;

    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @ApiModelProperty(value = "告警值")
    private String alarmValue;

    @ApiModelProperty(value = "告警描述")
    private String alarmDesc;

    @ApiModelProperty(value = "确认状态")
    private Integer confirmState;

    @ApiModelProperty(value = "确认人")
    private String confirmPeople;

    @ApiModelProperty(value = "确认时间")
    private Date confirmTime;

    @ApiModelProperty(value = "确认说明")
    private String confirmRemark;

    @ApiModelProperty(value = "是否缺陷（0：是，1：否）")
    private Integer defect;

    @ApiModelProperty(value = "缺陷等级：0-一级，1-二级，2-三级")
    private Integer defectLevel;

    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @ApiModelProperty(value = "告警状态")
    private String isClear;

    @ApiModelProperty(value = "显示类型")
    private String showType;

    @ApiModelProperty(value = "告警插入时间")
    private Date updateTime;


}
