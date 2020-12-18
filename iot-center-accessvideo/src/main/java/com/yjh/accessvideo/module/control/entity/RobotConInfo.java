package com.yjh.accessvideo.module.control.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tt
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotConInfo对象", description = "机器人相机信息")
public class RobotConInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人编号")
    private String robotCode;

    @ApiModelProperty(value = "机器人名字")
    private String robotName;

    @ApiModelProperty(value = "机器人状态 ")
    private String robotStatus;

    @ApiModelProperty(value = "机器人型号")
    private Integer robotType;

    private String robotTypeName;

    @ApiModelProperty(value = "机器人IP")
    private String robotIp;

    @ApiModelProperty(value = "机器人端口")
    private Integer robotPort;

    @ApiModelProperty(value = "可见光IP")
    private String lightIp;

    @ApiModelProperty(value = "可见光端口")
    private String lightPort;

    @ApiModelProperty(value = "可见光用户名")
    private String lightUsername;

    @ApiModelProperty(value = "可见光密码")
    private String lightPassword;

    @TableField("lnferad_IP")
    @ApiModelProperty(value = "红外IP")
    private String lnferadIp;

    @TableField("Inferad_Port")
    @ApiModelProperty(value = "红外端口")
    private Integer inferadPort;

    @TableField("Inferad_username")
    @ApiModelProperty(value = "红外用户名")
    private String inferadUsername;

    @TableField("Inferad_password")
    @ApiModelProperty(value = "红外密码")
    private String inferadPassword;

    @ApiModelProperty(value = "照片路径")
    private String photePath;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "修改日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

     @ApiModelProperty(value = "修改时间")
    private String updateBy;

    @ApiModelProperty(value = "修改日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    @ApiModelProperty(value = "机器人厂家 ")
    private String robotFactory;

    private String robotFactoryName;

    @ApiModelProperty(value = "使用状态 ")
    private String isUse;

    private String isUseName;

    @ApiModelProperty(value = "投运时间",example = "2020-08-01")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date commissionDate;

    @ApiModelProperty(value = "区域ID")
    private Long upRegionId;

    private List<Long> upRegionIds;

    private String regionName;

    @ApiModelProperty(value = "机器人类型")
    private String robotPosition;

    private String robotPositionName;

    private String remarks;

}
