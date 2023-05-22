package com.yjh.platform.module.patrol.entity;

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
@ApiModel(value = "TDefectInfo对象", description = "缺陷信息表")
public class TDefectInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "缺陷ID")
    @TableId(value = "defect_id", type = IdType.AUTO)
    private Long defectId;

    @ApiModelProperty(value = "缺陷等级")
    private Integer defectLevel;

    @ApiModelProperty(value = "缺陷时间")
    private Date defectTime;

    @ApiModelProperty(value = "缺陷类型")
    private Integer defectType;

    @ApiModelProperty(value = "缺陷名称")
    private String defectName;

    @ApiModelProperty(value = "缺陷内容")
    private String defectContent;

    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String cunstomId;

    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @ApiModelProperty(value = "标准测点ID")
    private Long stdMeteId;

    @ApiModelProperty(value = "缺陷状态：1未处理 2已处理 3已确认 4已忽略")
    private Integer confMode;

    @ApiModelProperty(value = "是否缺陷")
    private Integer isDefect;

    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @ApiModelProperty(value = "处理意见")
    private String dealInfo;

    @ApiModelProperty(value = "确认人ID")
    private String dealPersonId;

    @ApiModelProperty(value = "确认时间")
    private Date dealTime;

    @ApiModelProperty(value = "是否缺陷抑制")
    private Integer ifDefectDisable;

    @ApiModelProperty(value = "缺陷来源")
    private Integer alarmSource;

    @ApiModelProperty(value = "缺陷子类型")
    private Integer defectSubtype;

    @ApiModelProperty(value = "设备编码")
    private String deviceCode;

    @ApiModelProperty(value = "图片地址")
    private String imagePath;

    @ApiModelProperty(value = "视频地址")
    private String videoPath;

    @TableField("VALUE")
    private String value;

    private String outRange;

    private String deviceName;

    private String deviceMeteName;

    @ApiModelProperty(value = "联动信息")
    private String linkMessage;


}
