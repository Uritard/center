package com.yjh.platform.module.device.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author lqh
 * @since 2020/9/2
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevice对象与TStdDeviceAttr对象", description = "标准化设备表及属性表")
public class TStdDeviceDetail {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String customId;

    @ApiModelProperty(value = "设备编码")
    private String deviceCode;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "别名")
    private String aliasName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    private String deviceTypeName;

    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    private String positionType;

    @ApiModelProperty(value = "模版ID")
    private Long modelId;

    private String modelName;

    @ApiModelProperty(value = "路径")
    private String regionPath;

    @ApiModelProperty(value = "上级区域id")
    private Long upRegionId;

    private List<Long> upRegionIds;

    @ApiModelProperty(value = "上级区域名称")
    private String upRegionName;

    @ApiModelProperty(value = "部位名称")
    private String customName;

    @ApiModelProperty(value = "部位类型")
    private Integer customType;

    @ApiModelProperty(value = "设备状态(0：新建，1：在线，2：离线)")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "设备型号")
    private Integer deviceModel;

    private String deviceModelName;

    @ApiModelProperty(value = "PMS类型")
    private String pmsType;

    @ApiModelProperty(value = "PMS ID")
    private String pmsId;

    @ApiModelProperty(value = "生产厂家")
    private String deviceVendor;

    private String deviceVendorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date productionDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "投运时间")
    private Date usedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date disableDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date lastMaintenance;

    @ApiModelProperty(value = "维修次数")
    private String maintenanceCount;

    @ApiModelProperty(value = "所属单位")
    private String organization;

    @ApiModelProperty(value = "管理部门")
    private String department;

    @ApiModelProperty(value = "责任人")
    private String responsiblePerson;

    @ApiModelProperty(value = "纬度")
    private String latitude;

    @ApiModelProperty(value = "经度")
    private String longitude;

    @ApiModelProperty(value = "设备IP地址")
    private String ip;

    @ApiModelProperty(value = "端口")
    private Integer port;

    @ApiModelProperty(value = "电压等级")
    private String voltageLevel;

    private String voltageLevelName;

    @ApiModelProperty(value = "顺控点号")
    private String sequencePoint;

    @ApiModelProperty(value = "实物编码")
    private String realCode;

    @ApiModelProperty(value = "安装地址")
    private String address;

    private String picRealPath;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
