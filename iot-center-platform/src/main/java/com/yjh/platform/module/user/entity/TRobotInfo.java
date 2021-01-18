package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;
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
@ApiModel(value = "TRobotInfo对象", description = "机器人表")
public class TRobotInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @Length(max = 32, message = "robotCode长度必须小于等于32")
    @ApiModelProperty(value = "机器人编号")
    private String robotCode;

    @Length(max = 64, message = "robotName长度必须小于等于64")
    @ApiModelProperty(value = "机器人名字")
    private String robotName;

    @Length(max = 6, message = "robotStatus长度必须小于等于6")
    @ApiModelProperty(value = "机器人状态 ")
    private String robotStatus;

    @Max(value = 999999)
    @ApiModelProperty(value = "机器人型号")
    private Integer robotType;

//    @Length(max = 255, message = "robotTypeName长度必须小于等于255")
    private String robotTypeName;

    @Length(max = 32, message = "robotIp长度必须小于等于32")
    @ApiModelProperty(value = "机器人IP")
    private String robotIp;

    @Max(value = 99999999)
    @ApiModelProperty(value = "机器人端口")
    private Integer robotPort;

    @Length(max = 32, message = "lightIp长度必须小于等于32")
    @ApiModelProperty(value = "可见光IP")
    private String lightIp;

    @Length(max = 8, message = "lightPort长度必须小于等于8")
    @ApiModelProperty(value = "可见光端口")
    private String lightPort;

    @Length(max = 32, message = "lightUsername长度必须小于等于32")
    @ApiModelProperty(value = "可见光用户名")
    private String lightUsername;

    @Length(max = 15, message = "lightPassword长度必须小于等于15")
    @ApiModelProperty(value = "可见光密码")
    private String lightPassword;

    @Length(max = 32, message = "lnferadIp长度必须小于等于32")
    @TableField("lnferad_IP")
    @ApiModelProperty(value = "红外IP")
    private String lnferadIp;

    @Max(value = 99999999)
    @TableField("Inferad_Port")
    @ApiModelProperty(value = "红外端口")
    private Integer inferadPort;

    @Length(max = 32, message = "inferadUsername长度必须小于等于32")
    @TableField("Inferad_username")
    @ApiModelProperty(value = "红外用户名")
    private String inferadUsername;

    @Length(max = 8, message = "inferadPassword长度必须小于等于8")
    @TableField("Inferad_password")
    @ApiModelProperty(value = "红外密码")
    private String inferadPassword;

    @Length(max = 255, message = "photePath长度必须小于等于255")
    @ApiModelProperty(value = "照片路径")
    private String photePath;

    @Length(max = 64, message = "createBy长度必须小于等于64")
    @ApiModelProperty(value = "创建人")
    private String createBy;

    @Past
    @ApiModelProperty(value = "修改日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @Length(max = 64, message = "updateBy长度必须小于等于64")
    @ApiModelProperty(value = "修改时间")
    private String updateBy;

    @Past
    @ApiModelProperty(value = "修改日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    @Length(max = 28, message = "robotFactory长度必须小于等于28")
    @ApiModelProperty(value = "机器人厂家 ")
    private String robotFactory;

//    @Length(max = 100, message = "robotFactoryName长度必须小于等于100")
    private String robotFactoryName;

    @Length(max = 10, message = "isUse长度必须小于等于10")
    @ApiModelProperty(value = "使用状态 ")
    private String isUse;

//    @Length(max = 64, message = "robotCode长度必须小于等于32")
    private String isUseName;

    @Past
    @ApiModelProperty(value = "投运时间", example = "2020-08-01")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date commissionDate;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "区域ID")
    private Long upRegionId;

    private List<Long> upRegionIds;

    private String regionName;

    @Length(max = 32, message = "robotPosition长度必须小于等于32")
    @ApiModelProperty(value = "机器人类型")
    private String robotPosition;

    private String robotPositionName;

    @Length(max = 255, message = "remarks长度必须小于等于255")
    private String remarks;


    @ApiModelProperty(value = "设备来源")
    private String robotSource;
    @ApiModelProperty(value = "安装位置")
    private String address;
    @ApiModelProperty(value = "使用单位")
    private String buildingUser;
    @ApiModelProperty(value = "出场编号")
    private String appearanceNumber;
}
