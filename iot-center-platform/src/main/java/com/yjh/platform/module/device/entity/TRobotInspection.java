package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInspection对象", description = "机器人巡检点信息表")
public class TRobotInspection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备检测点编码")
    @TableId(value = "inspection_id", type = IdType.AUTO)
    @TableField(value = "inspection_id",updateStrategy = FieldStrategy.IGNORED)
    private Long inspectionId;

    @Length(max = 68,message = "inspectionCode长度必须小于等于68")
    @ApiModelProperty(value = "机器人检测点编码")
    @TableField(value = "inspection_code",updateStrategy = FieldStrategy.IGNORED)
    private String inspectionCode;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人ID")
    @TableField(value = "robot_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;

    @Length(max = 60,message = "inspectionName长度必须小于等于60")
    @ApiModelProperty(value = "测点名称")
    @TableField(value = "inspection_name",updateStrategy = FieldStrategy.IGNORED)
    private String inspectionName;

    @ApiModelProperty(value = "部件ID")
    private String componentId;
    @ApiModelProperty(value = "表计类型")
    private Integer meterType;
    @ApiModelProperty(value = "外观类型")
    private Integer appearanceType;
    @ApiModelProperty(value = "采集/保存文件类型列表")
    private String saveTypeList;
    @ApiModelProperty(value = "识别类型列表")
    private String recognitionTypeList;
    @ApiModelProperty(value = "相位，A相B相C相")
    private String phase;
    @ApiModelProperty(value = "备注信息")
    private String deviceInfo;


}
