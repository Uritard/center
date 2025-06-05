package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author Zyy
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "充电区域对象", description = "充电区域")
public class TRobotChargingAreaInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;
    @ApiModelProperty(value = "机器人地图")
    private String imgUrl;
    @ApiModelProperty(value = "充电区域左上角坐标x")
    private Double x;
    @ApiModelProperty(value = "充电区域左上角坐标y")
    private Double y;
    @ApiModelProperty(value = "充电区域宽度")
    private Double width;
    @ApiModelProperty(value = "充电区域高度")
    private Double height;
}
