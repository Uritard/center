package com.yjh.accessrobot.module.command.entity;

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
 * @author lqh
 * @since 2021-03-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotCameraPreset对象", description = "机器人预位置表")
public class TRobotCameraPreset implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "预置位id")
    @TableId(value = "preset_id", type = IdType.AUTO)
    private Long presetId;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    private String robotName;

    @ApiModelProperty(value = "机器人编码")
    private String robotCode;

    @ApiModelProperty(value = "预置位名称")
    private String presetName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creatorTime;

    @ApiModelProperty(value = "机器人摄像机类型")
    private Integer cameraType;

    private String cameraName;

    private Integer presetNum;


}
