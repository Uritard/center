package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;


/**
 * @author lqh
 * @since 2020/11/30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Robot对象", description = "机器人")
public class Robot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人编号")
    private String robotCode;

    @ApiModelProperty(value = "机器人名字")
    private String robotName;

    @ApiModelProperty(value = "机器人状态 ")
    private String robotStatus;

    @ApiModelProperty(value = "机巢编号")
    private String nestCode;

    @ApiModelProperty(value = "机巢名称")
    private String nestName;

    @ApiModelProperty(value = "机器人型号")
    private Integer robotType;

    @ApiModelProperty(value = "无人机型号")
    private Integer droneType;

    @ApiModelProperty(value = "机器人ip")
    private String robotIp;

    private String robotTypeName;

    private String droneTypeName;

    @ApiModelProperty(value = "机器人类型")
    private String robotPosition;

    private String robotPositionName;

    @ApiModelProperty(value = "机器人类型")
    private String dronePosition;

    private String dronePositionName;

    private String photePath;

    @ApiModelProperty(value = "所属区域名称")
    private String areaName;

    @ApiModelProperty(value = "所属区域Id")
    private String areaId;

    @ApiModelProperty(value = "ID")
    private Long Id;

    @ApiModelProperty(value = "名称")
    private String label;

    @ApiModelProperty(value = "上级区域ID")
    private Long upId;

    @ApiModelProperty(value = "上级区域名称")
    private String upName;

    @ApiModelProperty(value = "消息类型")
    private String infoType;

    @ApiModelProperty(value = "子类")
    private List<Robot> children;

}
