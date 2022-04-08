package com.yjh.accessrobot.module.command.entity;

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
 * @author YC
 * @since 2020-11-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDroneInspection对象", description = "无人机巡检点信息表")
public class TDroneInspection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备检测点编码")
    @TableId(value = "inspection_id", type = IdType.AUTO)
    private Long inspectionId;

    @Length(max = 68,message = "inspectionCode长度必须小于等于68")
    @ApiModelProperty(value = "无人机检测点编码")
    @TableField(value = "inspection_code",updateStrategy = FieldStrategy.IGNORED)
    private String inspectionCode;

    @TableField(value = "inspection_type",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "测点类型, 1.巡检点 2.操作点")
    private Integer inspectionType;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "无人机ID")
    @TableField(value = "drone_id",updateStrategy = FieldStrategy.IGNORED)
    private Long droneId;

    @Length(max = 60,message = "inspectionName长度必须小于等于60")
    @ApiModelProperty(value = "测点名称")
    @TableField(value = "inspection_name",updateStrategy = FieldStrategy.IGNORED)
    private String inspectionName;

    @ApiModelProperty(value = "部件ID")
    @TableField(value = "component_id",updateStrategy = FieldStrategy.IGNORED)
    private String componentId;
    @ApiModelProperty(value = "表计类型")
    private Integer meterType;
    @ApiModelProperty(value = "外观类型")
    private Integer appearanceType;
    @ApiModelProperty(value = "主操作类型")
    private Integer mainOperationType;
    @ApiModelProperty(value = "操作类型")
    private Integer operationType;
    @ApiModelProperty(value = "采集/保存文件类型列表")
    @TableField(value = "save_type_list",updateStrategy = FieldStrategy.IGNORED)
    private String saveTypeList;
    @ApiModelProperty(value = "识别类型列表")
    @TableField(value = "recognition_type_list",updateStrategy = FieldStrategy.IGNORED)
    private String recognitionTypeList;
    @ApiModelProperty(value = "相位，A相B相C相")
    @TableField(value = "phase",updateStrategy = FieldStrategy.IGNORED)
    private String phase;
    @ApiModelProperty(value = "备注信息")
    @TableField(value = "device_info",updateStrategy = FieldStrategy.IGNORED)
    private String deviceInfo;
    @ApiModelProperty(value = "属性图")
    @TableField(value = "property_pic_path",updateStrategy = FieldStrategy.IGNORED)
    private String propertyPicPath;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
