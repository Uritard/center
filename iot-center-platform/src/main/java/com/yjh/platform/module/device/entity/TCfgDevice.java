package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Past;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgDevice对象", description = "设备表")
public class TCfgDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 20,message = "deviceId长度必须小于等于20")
    @ApiModelProperty(value = "设备编号")
     @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private String deviceId;

    @Length(max = 128,message = "deviceName长度必须小于等于128")
    @ApiModelProperty(value = "设备名称")
     @TableField(value = "device_name",updateStrategy = FieldStrategy.IGNORED)
    private String deviceName;

    @Length(max = 20,message = "deviceType长度必须小于等于20")
    @ApiModelProperty(value = "设备类型")
    @TableField(value = "device_type",updateStrategy = FieldStrategy.IGNORED)
    private String deviceType;

    @Length(max = 40,message = "deviceCode长度必须小于等于40")
    @ApiModelProperty(value = "设备编码+设备测点地址")
    @TableField(value = "device_code",updateStrategy = FieldStrategy.IGNORED)
    private String deviceCode;

    @Length(max = 20,message = "stationId长度必须小于等于20")
    @ApiModelProperty(value = "项目id")
    @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @Length(max = 32,message = "relationCode长度必须小于等于32")
    @ApiModelProperty(value = "关联设备编码")
    @TableField(value = "relationCode",updateStrategy = FieldStrategy.IGNORED)
    private String relationCode;


    @ApiModelProperty(value = "创建时间")
     @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;


    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time",updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;

    @Length(max = 512,message = "remark长度必须小于等于512")
    @ApiModelProperty(value = "描述")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;
    @TableField(value = "edge_code",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "边缘节点编码")
    private String edgeCode;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
