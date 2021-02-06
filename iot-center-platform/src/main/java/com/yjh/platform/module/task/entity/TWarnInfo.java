package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-10-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWarnInfo对象", description = "告警信息表")
public class TWarnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "告警ID")
    @TableId(value = "warn_id", type = IdType.AUTO)
    private Long warnId;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警等级")
    private Integer warnLevel;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "告警时间")
    private Date warnTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警类型")
    private Integer warnType;

    @Length(max = 125,message = "warnName长度必须小于等于125")
    @ApiModelProperty(value = "告警名称")
    @TableField(value = "warn_name",updateStrategy = FieldStrategy.IGNORED)
    private String warnName;

    @Length(max = 512,message = "warnContent长度必须小于等于512")
    @ApiModelProperty(value = "告警内容")
    @TableField(value = "warn_content",updateStrategy = FieldStrategy.IGNORED)
    private String warnContent;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @Length(max = 32,message = "cunstomId长度必须小于等于32")
    @ApiModelProperty(value = "部位ID")
    @TableField(value = "cunstom_id",updateStrategy = FieldStrategy.IGNORED)
    private String cunstomId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "标准测点ID")
    private Long stdMeteId;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警状态：1未处理 2已处理 3已确认 4已忽略")
    private Integer confMode;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否告警")
    private Integer isWarn;

    @Max(value=9)
    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @Length(max = 2000,message = "dealInfo长度必须小于等于2000")
    @ApiModelProperty(value = "处理意见")
    @TableField(value = "deal_info",updateStrategy = FieldStrategy.IGNORED)
    private String dealInfo;

    @Length(max = 32,message = "dealPersonId长度必须小于等于32")
    @ApiModelProperty(value = "确认人ID")
    @TableField(value = "deal_person_id",updateStrategy = FieldStrategy.IGNORED)
    private String dealPersonId;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "确认时间")
    private Date dealTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "缺陷类型")
    private Integer defectModel;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警来源")
    private Integer alarmSource;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警子类型")
    private Integer warnSubtype;

    @Length(max = 64,message = "deviceCode长度必须小于等于64")
    @ApiModelProperty(value = "设备编码")
    @TableField(value = "device_code",updateStrategy = FieldStrategy.IGNORED)
    private String deviceCode;

    @Length(max = 512,message = "imagePath长度必须小于等于512")
    @ApiModelProperty(value = "图片地址")
    @TableField(value = "image_path",updateStrategy = FieldStrategy.IGNORED)
    private String imagePath;

    @Length(max = 200,message = "videoPath长度必须小于等于200")
    @ApiModelProperty(value = "视频地址")
    @TableField(value = "video_path",updateStrategy = FieldStrategy.IGNORED)
    private String videoPath;

    @Length(max = 100,message = "value长度必须小于等于100")
    @TableField(value = "value",updateStrategy = FieldStrategy.IGNORED)
    private String value;

    @Length(max = 100,message = "outRange长度必须小于等于100")
    @TableField(value = "outRange",updateStrategy = FieldStrategy.IGNORED)
    private String outRange;

    @Length(max = 512,message = "taskId长度必须小于等于512")
    @ApiModelProperty(value = "任务ID")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;


}
