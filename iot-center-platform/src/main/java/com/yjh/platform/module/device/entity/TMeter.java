package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName t_meter
 */
@Data
public class TMeter implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 上级区域id
     */
    private Long upRegionId;

    /**
     * 电表名称
     */
    @ApiModelProperty(value = "电表名称")
    private String name;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    private String ip;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    private Integer port;

    /**
     * 电表地址(12位字符串)
     */
    private String address;

    /**
     * 正向有功总电量
     */
    @ApiModelProperty(value = "正向有功总电量")
    private String totalPositivePower;

    /**
     * 正向无功总电量
     */
    @ApiModelProperty(value = "正向无功总电量")
    @TableField(value = "total_positive_reactive_power", updateStrategy = FieldStrategy.IGNORED)
    private String totalPositiveReactivePower;

    /**
     * 反向无功总电量
     */
    @ApiModelProperty(value = "反向无功总电量")
    @TableField(value = "total_negative_positive_power", updateStrategy = FieldStrategy.IGNORED)
    private String totalNegativeReactivePower;

    /**
     * 反向无功总电量(对应数据库字段，轨交mybatis-plus注解会失效)*
     */
    @ApiModelProperty(value = "反向无功总电量")
    private String totalNegativePositivePower;

    /**
     * 电量采集时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "电量采集时间")
    private Date collectPowerTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 区域名称
     */
    @ApiModelProperty(value = "域名称")
    private String regionName;

    private static final long serialVersionUID = 1L;

    /**
     * 更新时间*
     */
    private Date updateTime;

    private String createPerson;

    private String updatePerson;

    private Integer isDeleted;

    private String startTime;

    private String endTime;
}
