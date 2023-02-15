package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/8/12 - 21:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraPreset对象", description = "摄像机预置位表")
public class TCameraPreset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "预置位id")
    @TableField(value = "preset_id",updateStrategy = FieldStrategy.IGNORED)
    private Long presetId;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "摄像头id")
    @TableField(value = "camera_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraId ;

    @Max(value=999999999)
    @ApiModelProperty(value = "预置位号")
    private Integer presetNum ;

    @Length(max = 128,message = "presetName长度必须小于等于128")
    @ApiModelProperty(value = "预置位名称")
    @TableField(value = "preset_name",updateStrategy = FieldStrategy.IGNORED)
    private String presetName ;

    @Length(max = 64,message = "creatorUser长度必须小于等于64")
    @ApiModelProperty(value = "啥也不是")
    @TableField(value = "creator_user",updateStrategy = FieldStrategy.IGNORED)
    private String creatorUser ;


    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date creatorTime ;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否使用")
    private Integer isUse ;

    @Length(max = 255,message = "presetImg长度必须小于等于255")
    @ApiModelProperty(value = "预置位图片地址")
     @TableField(value = "preset_img",updateStrategy = FieldStrategy.IGNORED)
    private String presetImg ;

    @Max(value=999999999)
    @ApiModelProperty(value = "检测点位置，0-室外 1-室内")
    private Integer inspectionPostion ;

    @Max(value=999999999)
    @ApiModelProperty(value = "采集状态，0-未采集 1-已采集")
    private Integer collectStatus ;

    @Max(value=999999999)
    @ApiModelProperty(value = "标定状态，0-未标定 1-已标定")
    private Integer calibrationStatus ;

    @Length(max = 255,message = "remark长度必须小于等于255")
    @ApiModelProperty(value = "备注")
    private String remark ;


    @Max(value=999999999)
    @ApiModelProperty(value = "守望位类型")
    // 默认普通预置位
    private Integer presetType = 1;

    @ApiModelProperty(value = "守望位类型")
    private String presetTypeName;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
