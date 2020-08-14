package com.yjh.platform.module.user.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInfo对象", description = "机器人表")
public class TRobotInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long robotId;

    private String robotName;

    private String robotStatus;

    private Integer robotType;

    private String robotIp;

    private Integer robotPort;

    @ApiModelProperty(value = "上级区域ID")
    private Long upRegionId;

    @ApiModelProperty(value = "上级区域名称")
    private String upRegionName;

    private String lightIp;

    private String lightPort;

    private String lightUsername;

    private String lightPassword;

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

    @ApiModelProperty(value = "修改日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    private String updateBy;

    @ApiModelProperty(value = "修改日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    private String remarks;


}
