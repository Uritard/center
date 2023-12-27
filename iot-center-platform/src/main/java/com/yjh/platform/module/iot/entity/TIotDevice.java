package com.yjh.platform.module.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yjh.platform.common.ValidateConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * 物联设备表
 * @TableName t_iot_device
 */
@TableName(value ="t_iot_device")
@Data
public class TIotDevice implements Serializable {
    /**
     * Id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备名称
     */
    @Length(max = 128)
    @Pattern(regexp= ValidateConstant.REG_RICH_NAME, message = ValidateConstant.MSG_RICH_NAME)
    private String deviceName;

    /**
     * 设备IP
     */
    private String ip;

    /**
     * 设备端口号
     */
    @Range(min = 1, max = 65535)
    private Integer port;

    /**
     * 设备地址
     */
    private String address;

    /**
     * 设备类型
     */
    private Integer iotDeviceType;

    /**
     * 协议类型名称
     */
    @TableField(exist = false)
    private String iotDeviceTypeName;

    /**
     * 协议模式 TCP-ACTIVE,TCP-PASSIVE
     */
    private String protocolModel;

    @TableField(exist = false)
    private String protocolModelName;

    /**
     * 关联设备Id
     */
    private Long deviceId;

    /**
     * 上级区域id
     */
    private Long upRegionId;

    /**
     * 上级区域名称
     */
    private String upRegionName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建人
     */
    private String createPerson;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 更新人
     */
    private String updatePerson;

    /**
     * 区域编码
     */
    private String edgeCode;

    /**
     * 系数
     */
    private Float magnificationCoefficient;

    /**
     * 采集频率 单位:分钟
     */
    private Integer collectionFrequency;

    @TableField(exist = false)
    private String collectionFrequencyName;

    /**
     * 是否可以控制 0-不可控制 1-可以控制
     */
    private Integer controllable;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
