package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <功能描述>
 *
 *  土星声纹厂家上报的声纹告警实体类
 *
 * @author huyuhang
 * @date 2024/11/5
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VoiceAlarm {

    @ApiModelProperty(value = "监测终端编号")
    private String deviceNumber;

    @ApiModelProperty(value = "监测终端名称")
    private String deviceName;

    @ApiModelProperty(value = "监测点位编码")
    private String hcCode;

    @ApiModelProperty(value = "告警时间")

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;

    @ApiModelProperty(value = "告警类型")
    private String alarmType;

    @ApiModelProperty(value = "告警描述")
    private String alarmDesc;

    @ApiModelProperty(value = "原始数据下载URL 下载的数据为wav文件")
    private String rawDataUrl;

}
