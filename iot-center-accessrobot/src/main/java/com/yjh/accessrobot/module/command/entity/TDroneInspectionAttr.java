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
 * @author hyh
 * @since 2022/2/17
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDroneInspectionAttr对象", description = "无人机巡检点属性表")
public class TDroneInspectionAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "无人机ID")
    @TableField(value = "drone_id",updateStrategy = FieldStrategy.IGNORED)
    private Long droneId;

    @Length(max = 68,message = "inspectionCode长度必须小于等于68")
    @ApiModelProperty(value = "无人机检测点编码")
    @TableField(value = "inspection_code",updateStrategy = FieldStrategy.IGNORED)
    private String inspectionCode;

    @ApiModelProperty(value = "左上角X坐标")
    @TableField(value = "x",updateStrategy = FieldStrategy.IGNORED)
    private int x;

    @ApiModelProperty(value = "左上角Y坐标")
    @TableField(value = "y",updateStrategy = FieldStrategy.IGNORED)
    private int y;

    @ApiModelProperty(value = "宽")
    @TableField(value = "width",updateStrategy = FieldStrategy.IGNORED)
    private int width;

    @ApiModelProperty(value = "高")
    @TableField(value = "height",updateStrategy = FieldStrategy.IGNORED)
    private int height;

}
