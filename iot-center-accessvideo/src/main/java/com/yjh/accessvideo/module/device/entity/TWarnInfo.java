package com.yjh.accessvideo.module.device.entity;

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
 * @author tt
 * @since 2020-10-19
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

    @ApiModelProperty(value = "告警时间")
    private Date warnTime;

    @ApiModelProperty(value = "告警类型")
    private Integer warnType;

    @ApiModelProperty(value = "告警名称")
    private String warnName;

    @ApiModelProperty(value = "告警内容")
    private String warnContent;

    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @ApiModelProperty(value = "标准测点ID")
    private Long stdMeteId;

    @ApiModelProperty(value = "告警状态：1未处理 2已处理 3已确认 4已忽略")
    private Integer confMode;

    @ApiModelProperty(value = "是否告警")
    private Integer isWarn;

    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @ApiModelProperty(value = "处理意见")
    private String dealInfo;

    @ApiModelProperty(value = "确认人ID")
    private String dealPersonId;

    @ApiModelProperty(value = "确认时间")
    private Date dealTime;

    @ApiModelProperty(value = "是否审核  0-未审核 1-已审核")
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

    @ApiModelProperty(value = "实际表计识别值")
    private String value;

    @ApiModelProperty(value = "预警值超限差值")
    private String outRange;

    @ApiModelProperty(value = "任务ID")
    private String taskId;



}
