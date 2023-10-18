package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * 机器人设备机柜绑定表
 * @TableName t_robot_device_config
 */
@Data
public class TRobotDeviceConfig implements Serializable {
    /**
     * 主键id
     */
    @ApiModelProperty("主键id")
    private Long deviceConfigId;
    /**
     * 机器人id
     */
    @ApiModelProperty("机器人id")
    private Long robotId;
    /**
     * 关联设备id
     */
    @ApiModelProperty("关联设备id")
    private Long equipmentId;
    /**
     * 关联设备名称
     */
    @Size(max= 128,message="编码长度不能超过128")
    @ApiModelProperty("关联设备名称")
    @Length(max= 128,message="编码长度不能超过128")
    private String equipmentName;
    /**
     * 关联设备类型
     */
    @ApiModelProperty("关联设备类型")
    private Integer equipmentType;
    /**
     * 坐标位置x
     */
    @ApiModelProperty("坐标位置x")
    private Integer xCoordinate;
    /**
     * 坐标位置y
     */
    @ApiModelProperty("坐标位置y")
    private Integer yCoordinate;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;
    /**
     * 更新人
     */
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("更新人")
    @Length(max= 20,message="编码长度不能超过20")
    private String updatePerson;

    private static final long serialVersionUID = 1L;
}