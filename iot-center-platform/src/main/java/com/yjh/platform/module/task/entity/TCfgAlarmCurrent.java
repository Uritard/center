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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "告警流水号")
    @TableId(value = "alarm_no", type = IdType.AUTO)
    private Long alarmNo;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备编号")
    private Long deviceId;

    @Length(max = 32,message = "cunstomId长度必须小于等于32")
    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "监控量编号")
    private Long meteId;

    @Past
    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @Length(max = 100,message = "alarmValue长度必须小于等于100")
    @ApiModelProperty(value = "告警值")
    private String alarmValue;

    @Length(max = 128,message = "alarmDesc长度必须小于等于128")
    @ApiModelProperty(value = "告警描述")
    private String alarmDesc;

    @Max(value=999999999)
    @ApiModelProperty(value = "确认状态")
    private Integer confirmState;

    @Length(max = 50,message = "confirmPeople长度必须小于等于50")
    @ApiModelProperty(value = "确认人")
    private String confirmPeople;

    @Past
    @ApiModelProperty(value = "确认时间")
    private Date confirmTime;

    @Length(max = 128,message = "confirmRemark长度必须小于等于128")
    @ApiModelProperty(value = "确认说明")
    private String confirmRemark;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否缺陷（0：是，1：否）")
    private Integer defect;

    @Max(value=999999999)
    @ApiModelProperty(value = "缺陷等级：0-一级，1-二级，2-三级")
    private Integer defectLevel;

    @Length(max = 20,message = "meteCode长度必须小于等于20")
    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @Length(max = 11,message = "isClear长度必须小于等于11")
    @ApiModelProperty(value = "告警状态")
    private String isClear;

    @Length(max = 20,message = "showType长度必须小于等于20")
    @ApiModelProperty(value = "显示类型")
    private String showType;

    @Past
    @ApiModelProperty(value = "告警插入时间")
    private Date updateTime;


}
