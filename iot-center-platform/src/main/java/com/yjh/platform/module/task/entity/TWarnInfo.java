package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel(value = "TWarnInfo对象", description = "告警信息表")
public class TWarnInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "告警ID")
    @TableId(value = "warn_id", type = IdType.AUTO)
    private Long warnId;

    @ApiModelProperty(value = "告警等级")
    private Integer warnLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "告警时间")
    private Date warnTime;

    @ApiModelProperty(value = "告警类型")
    private Integer warnType;

    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    private Long instanceId;

    private Long stdMeteId;

    @ApiModelProperty(value = "告警状态：1未处理 2已处理 3已确认 4已忽略")
    private Integer confMode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "确认时间")
    private Date confTime;

    @ApiModelProperty(value = "确认人")
    private String confUserId;

    @ApiModelProperty(value = "处理意见")
    private String confInfo;

    @ApiModelProperty(value = "是否告警抑制")
    private Integer ifWarnDisable;

    @ApiModelProperty(value = "告警来源")
    private Integer alarmSource;

    @ApiModelProperty(value = "告警子类型")
    private Integer warnSubtype;

    @ApiModelProperty(value = "设备编码")
    private String deviceCode;

    @ApiModelProperty(value = "图片地址")
    private String imagePath;

    @ApiModelProperty(value = "视频地址")
    private String videoPath;

    private String value;

    @ApiModelProperty(value = "是否缺陷（0：是，1：否）")
    private Integer defect;

    @ApiModelProperty(value = "缺陷等级：0-一级，1-二级，2-三级")
    private Integer defectLevel;

    private String outRange;

    @ApiModelProperty(value = "联动信息")
    private String linkMessage;


}
