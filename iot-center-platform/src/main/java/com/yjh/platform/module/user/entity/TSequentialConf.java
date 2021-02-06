package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TSequentialConf对象", description = "顺控配置表")
public class TSequentialConf implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    @TableField(value = "cfg_device_id",updateStrategy = FieldStrategy.IGNORED)
    private String cfgDeviceId;

    private String cfgDeviceName;

    @ApiModelProperty(value = "监控量编号")
    @TableField(value = "cfg_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private String cfgMeteId;

    private String cfgMeteName;

    @ApiModelProperty(value = "预置位id")
    @TableField(value = "preset_id",updateStrategy = FieldStrategy.IGNORED)
    private Long presetId;

    @ApiModelProperty(value = "预置位id")
     @TableField(value = "camera_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraId;

    private String presetName;

    @ApiModelProperty(value = "识别结果")

     @TableField(value = "identify_result",updateStrategy = FieldStrategy.IGNORED)
    private String identifyResult;

    private Integer sort;

    private Date recordTime;

}
