package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    private Long robotId;

    private String robotCode;

    private String robotName;

    private String robotStatus;

    private Integer robotType;

    private String robotIp;

    private Integer robotPort;

    @ApiModelProperty(value = "可见光IP")
    private String lightIp;

    private String lightPort;

    private String lightUsername;

    private String lightPassword;

    @ApiModelProperty(value = "红外IP")
    @TableField("lnferad_IP")
    private String lnferadIp;

    @TableField("Inferad_Port")
    private Integer inferadPort;

    @TableField("Inferad_username")
    private String inferadUsername;

    @TableField("Inferad_password")
    private String inferadPassword;

    @ApiModelProperty(value = "照片路径")
    private String photePath;

    private String createBy;

    private Date createDate;

    private String updateBy;

    private Date updateDate;

    @ApiModelProperty(value = "机器人厂家")
    private String robotFactory;

    @ApiModelProperty(value = "使用状态")
    private String isUse;

    @ApiModelProperty(value = "投运日期")
    private Date commissionDate;

    private Long upRegionId;

    private String robotPosition;

    private String remarks;


}
