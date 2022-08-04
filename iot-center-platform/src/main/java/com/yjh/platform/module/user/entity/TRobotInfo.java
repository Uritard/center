package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
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

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "机器人id")
    @TableField(value = "robot_id", updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;

    @Length(max = 32, message = "robotCode长度必须小于等于32")
    @ApiModelProperty(value = "机器人编号")
    @TableField(value = "robot_code", updateStrategy = FieldStrategy.IGNORED)
    private String robotCode;

    @Length(max = 64, message = "robotName长度必须小于等于64")
    @ApiModelProperty(value = "机器人名字")
    @TableField(value = "robot_name", updateStrategy = FieldStrategy.IGNORED)
    private String robotName;

    @Length(max = 32, message = "nestCode长度必须小于等于32")
    @ApiModelProperty(value = "机巢编号")
    @TableField(value = "nest_code", updateStrategy = FieldStrategy.IGNORED)
    private String nestCode;

    @Length(max = 64, message = "nestName长度必须小于等于64")
    @ApiModelProperty(value = "机巢名称")
    @TableField(value = "nest_name", updateStrategy = FieldStrategy.IGNORED)
    private String nestName;

    @Length(max = 6, message = "robotStatus长度必须小于等于6")
    @ApiModelProperty(value = "机器人状态 ")
    @TableField(value = "robot_status", updateStrategy = FieldStrategy.IGNORED)
    private String robotStatus;

    @Max(value = 999999)
    @ApiModelProperty(value = "机器人型号")
    private Integer robotType;

    @Max(value = 999999)
    @ApiModelProperty(value = "无人机型号")
    private Integer droneType;

    private String robotTypeName;

    private String droneTypeName;

    @Length(max = 32, message = "robotIp长度必须小于等于32")
    @ApiModelProperty(value = "机器人IP")
    @TableField(value = "robot_ip", updateStrategy = FieldStrategy.IGNORED)
    private String robotIp;

    @Max(value = 99999999)
    @ApiModelProperty(value = "机器人端口")
    private Integer robotPort;

    @Length(max = 32, message = "lightIp长度必须小于等于32")
    @ApiModelProperty(value = "可见光IP")
    @TableField(value = "light_ip", updateStrategy = FieldStrategy.IGNORED)
    private String lightIp;

    @Length(max = 8, message = "lightPort长度必须小于等于8")
    @ApiModelProperty(value = "可见光端口")
    @TableField(value = "light_port", updateStrategy = FieldStrategy.IGNORED)
    private String lightPort;

    @Length(max = 32, message = "lightUsername长度必须小于等于32")
    @ApiModelProperty(value = "可见光用户名")
    @TableField(value = "identity_manager", updateStrategy = FieldStrategy.IGNORED)
    private String identityManager;

    @ApiModelProperty(value = "可见光密码")
    @Length(max = 15, message = "identity_code长度必须小于等于15")
    @TableField(value = "identity_code", updateStrategy = FieldStrategy.IGNORED)
    private String identityCode;

    @Length(max = 32, message = "lnferadIp长度必须小于等于32")
    @ApiModelProperty(value = "红外IP")
    @TableField(value = "lnferad_IP", updateStrategy = FieldStrategy.IGNORED)
    private String lnferadIp;

    @Max(value = 99999999)
    @ApiModelProperty(value = "红外端口")
    @TableField(value = "lnferad_IP", updateStrategy = FieldStrategy.IGNORED)
    private Integer inferadPort;

    @Length(max = 32, message = "inferadUsername长度必须小于等于32")
    @TableField(value = "Inferad_username", updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "红外用户名")
    private String inferadUsername;

    @TableField(value = "Inferad_password", updateStrategy = FieldStrategy.IGNORED)
    @Length(max = 15, message = "Inferad_password长度必须小于等于15")
    @ApiModelProperty(value = "红外密码")
    private String inferadPassword;

    @Length(max = 255, message = "photePath长度必须小于等于255")
    @ApiModelProperty(value = "照片路径")
    @TableField(value = "phote_path", updateStrategy = FieldStrategy.IGNORED)
    private String photePath;

    @Length(max = 64, message = "createBy长度必须小于等于64")
    @ApiModelProperty(value = "创建人")
    @TableField(value = "create_by", updateStrategy = FieldStrategy.IGNORED)
    private String createBy;


    @ApiModelProperty(value = "修改日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @Length(max = 64, message = "updateBy长度必须小于等于64")
    @ApiModelProperty(value = "修改日期")
    @TableField(value = "update_by", updateStrategy = FieldStrategy.IGNORED)
    private String updateBy;


    @ApiModelProperty(value = "修改日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    @Length(max = 28, message = "robotFactory长度必须小于等于28")
    @ApiModelProperty(value = "机器人厂家 ")
    @TableField(value = "robotFactory", updateStrategy = FieldStrategy.IGNORED)
    private String robotFactory;

    private String robotFactoryName;

    @Length(max = 28, message = "madeIn长度必须小于等于28")
    @ApiModelProperty(value = "生产国家 ")
    @TableField(value = "made_in")
    private String madeIn;

    @ApiModelProperty(value = "出厂日期", example = "2018-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(value = "made_date")
    private Date madeDate;

    @Length(max = 10, message = "isUse长度必须小于等于10")
    @ApiModelProperty(value = "使用状态 ")
    @TableField(value = "is_use", updateStrategy = FieldStrategy.IGNORED)
    private String isUse;

    private String isUseName;

    @ApiModelProperty(value = "投运时间", example = "2020-08-01")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date commissionDate;

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "区域ID")
    @TableField(value = "upRegionId", updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    private List<Long> upRegionIds;

    private String regionName;

    @Length(max = 32, message = "robotPosition长度必须小于等于32")
    @ApiModelProperty(value = "机器人类型")
    @TableField(value = "robot_position", updateStrategy = FieldStrategy.IGNORED)
    private String robotPosition;

    private String robotPositionName;

    @Length(max = 32, message = "dronePosition长度必须小于等于32")
    @ApiModelProperty(value = "无人机类型")
    @TableField(value = "drone_position", updateStrategy = FieldStrategy.IGNORED)
    private String dronePosition;
    private String dronePositionName;

    @Length(max = 255, message = "remarks长度必须小于等于255")
    @TableField(value = "remarks", updateStrategy = FieldStrategy.IGNORED)
    private String remarks;


    @ApiModelProperty(value = "设备来源")
    @Length(max = 255, message = "设备来源长度必须小于等于255")
    @TableField(value = "robot_source", updateStrategy = FieldStrategy.IGNORED)
    private String robotSource;

    @Length(max = 255, message = "安装位置长度必须小于等于255")
    @ApiModelProperty(value = "安装位置")
    @TableField(value = "address", updateStrategy = FieldStrategy.IGNORED)
    private String address;

    @ApiModelProperty(value = "使用单位")
    @Length(max = 255, message = "使用单位长度必须小于等于255")
    @TableField(value = "building_user", updateStrategy = FieldStrategy.IGNORED)
    private String buildingUser;

    @ApiModelProperty(value = "出场编号")
    @Length(max = 255, message = "出场编号长度必须小于等于255")
    @TableField(value = "appearance_number", updateStrategy = FieldStrategy.IGNORED)
    private String appearanceNumber;

    @ApiModelProperty(value = "缺陷记录")
    @Length(max = 512, message = "缺陷记录长度必须小于等于512")
    @TableField(value = "defect_record", updateStrategy = FieldStrategy.IGNORED)
    private String defectRecord;

    @ApiModelProperty(value = "大修记录")
    @Length(max = 512, message = "缺陷记录长度必须小于等于512")
    @TableField(value = "repair_record", updateStrategy = FieldStrategy.IGNORED)
    private String repairRecord;

    @ApiModelProperty(value = "退出再重放记录")
    @Length(max = 512, message = "退出再重放记录长度必须小于等于512")
    @TableField(value = "exit_putInto_record", updateStrategy = FieldStrategy.IGNORED)
    private String exitPutIntoRecord;

    private Long recordId;

    private Integer channelNumLight;
    private Integer channelNumInferad;


    @ApiModelProperty(value = "上次登录时间(毫秒数)")
    @TableField(value = "last_online_time",updateStrategy = FieldStrategy.IGNORED)
    private Long lastOnlineTime;

    @ApiModelProperty(value = "在线时长累积(毫秒)")
    @TableField(value = "duration",updateStrategy = FieldStrategy.IGNORED)
    private Long duration;

    @ApiModelProperty(value = "离线次数")
    @TableField(value = "off_line_count",updateStrategy = FieldStrategy.IGNORED)
    private Long offLineCount;

    @ApiModelProperty(value = "秘钥标识符", hidden=true)
    private String identifier;

    @Max(value = 99999999)
    @ApiModelProperty(value = "机器人/无人机编号编码")
    @TableField(value = "robot_num", updateStrategy = FieldStrategy.IGNORED)
    private Integer robotNum;
}
