package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author lqh
 * @since 2021-01-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDeviceMaintenance对象", description = "设备区域检修表")
public class TDeviceMaintenance implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "检修ID")
    @TableId(value = "maintenance_id", type = IdType.AUTO)
    private Long maintenanceId;

    @ApiModelProperty(value = "检修名称")
    private String maintenanceName;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "是否使用，0-不使用，1-使用")
    private Integer isValid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始检修时间")
    private Date maintenanceStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束检修时间")
    private Date maintenanceStop;

    private Integer effectiveState;
    private String effectiveStateName;

    private List<Long> deviceIdList;

}
