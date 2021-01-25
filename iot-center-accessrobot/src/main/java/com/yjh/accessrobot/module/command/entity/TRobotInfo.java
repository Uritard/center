package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @since 2020-11-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInfo对象", description = "机器人表")
public class TRobotInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    @TableId(value = "robot_id", type = IdType.AUTO)
    @TableField(value = "robot_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;
     @TableField(value = "robot_code",updateStrategy = FieldStrategy.IGNORED)
    private String robotCode;
    @TableField(value = "robot_name",updateStrategy = FieldStrategy.IGNORED)
    private String robotName;
     @TableField(value = "robot_status",updateStrategy = FieldStrategy.IGNORED)
    private String robotStatus;
     @TableField(value = "robot_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer robotType;
    @TableField(value = "robot_ip",updateStrategy = FieldStrategy.IGNORED)
    private String robotIp;
    @TableField(value = "robot_port",updateStrategy = FieldStrategy.IGNORED)
    private Integer robotPort;
    @ApiModelProperty(value = "可见光IP")
     @TableField(value = "light_ip",updateStrategy = FieldStrategy.IGNORED)
    private String lightIp;
    @TableField(value = "light_port",updateStrategy = FieldStrategy.IGNORED)
    private String lightPort;
    @TableField(value = "light_username",updateStrategy = FieldStrategy.IGNORED)
    private String lightUsername;
    @TableField(value = "light_password",updateStrategy = FieldStrategy.IGNORED)
    private String lightPassword;

    @ApiModelProperty(value = "红外IP")
    @TableField(value = "lnferad_IP",updateStrategy = FieldStrategy.IGNORED)
    private String lnferadIp;

    @TableField(value = "Inferad_Port",updateStrategy = FieldStrategy.IGNORED)
    private Integer inferadPort;

    @TableField(value = "Inferad_username",updateStrategy = FieldStrategy.IGNORED)
    private String inferadUsername;

    @TableField(value = "Inferad_password",updateStrategy = FieldStrategy.IGNORED)
    private String inferadPassword;

    @ApiModelProperty(value = "照片路径")
    @TableField(value = "phote_path",updateStrategy = FieldStrategy.IGNORED)
    private String photePath;
     @TableField(value = "create_by",updateStrategy = FieldStrategy.IGNORED)
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建日期")
    @TableField(value = "create_date",updateStrategy = FieldStrategy.IGNORED)
    private Date createDate;
    @TableField(value = "update_by",updateStrategy = FieldStrategy.IGNORED)
    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_date",updateStrategy = FieldStrategy.IGNORED)
    private Date updateDate;

    @ApiModelProperty(value = "机器人厂家")
    @TableField(value = "robot_factory",updateStrategy = FieldStrategy.IGNORED)
    private String robotFactory;

    @ApiModelProperty(value = "使用状态")
    @TableField(value = "is_use",updateStrategy = FieldStrategy.IGNORED)
    private String isUse;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "投运日期")
    @TableField(value = "commission_date",updateStrategy = FieldStrategy.IGNORED)
    private Date commissionDate;
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;
    @TableField(value = "robot_position",updateStrategy = FieldStrategy.IGNORED)
    private String robotPosition;
    @TableField(value = "remarks",updateStrategy = FieldStrategy.IGNORED)
    private String remarks;


}
