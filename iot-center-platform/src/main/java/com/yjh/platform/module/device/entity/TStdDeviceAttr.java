package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDeviceAttr对象", description = "标准化设备参数表")
public class TStdDeviceAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long deviceId;

    @ApiModelProperty(value = "设备型号")
    private Integer deviceModel;

    @ApiModelProperty(value = "PMS类型")
    private String pmsType;

    @ApiModelProperty(value = "PMS ID")
    private String pmsId;

    @ApiModelProperty(value = "生产厂家")
    private String manufacturer;

    private Date productionDate;

    @ApiModelProperty(value = "投运时间")
    private Date openingDate;

    private Date disableDate;

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
    private String para1;

    @ApiModelProperty(value = "顺控点号")
    private String para2;

    @ApiModelProperty(value = "实物编码")
    private String para3;


}
