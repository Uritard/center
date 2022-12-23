package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author czh
 * @since 2020-11-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DeviceStaticsInfo对象", description = "设备统计信息表")
public class DeviceStatisticInfoResult implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /**
     * 0是robot，1是无人机，2是摄像机
     */
    @ApiModelProperty(value = "设备类型")
    private int deviceType;

    @ApiModelProperty(value = "累积在线时长总和")
    private String duration;

    @ApiModelProperty(value = "累积离线次数总和")
    private String offlineCount;

    @ApiModelProperty(value = "累计连续正常运行天数")
    private String normalDay;

    @ApiModelProperty(value = "正常巡检天数")
    private String commissionDays;

    @ApiModelProperty(value = "巡检出勤率")
    private String cruisePercent;

    @ApiModelProperty(value = "录像完整率")
    private String intactPercent;

    @ApiModelProperty(value = "设备运行状态")
    private String deviceRun;

    @ApiModelProperty(value = "设备恢复正常时间")
    private Date deviceResumeDate;
}
