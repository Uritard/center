package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/17
 * @since [产品/模块版本] （可选）
 */
@Data
public class LockPresetCommand {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "摄像头id")
    private Long cameraId ;

    @ApiModelProperty(value = "摄像头id")
    private String cameraName ;

    @ApiModelProperty(value = "预置位号")
    private Integer presetNum ;

    @ApiModelProperty(value = "预置位名称")
    private String presetName ;

    @ApiModelProperty(value = "设备测点实例ID")
    private Long deviceMeteId;
    @ApiModelProperty(value = "设备测点实例ID")
    private Long deviceId;

    @ApiModelProperty(value = "设备测点实例ID")
    private String customId;

    @ApiModelProperty(value = "设备名称")
    private String meteName;

    private String inspectionType;
    private Integer cruiseType;

}
