package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Max(value = 999999999999999999l)
    private Long deviceId;

    @Max(value = 999999999)
    @ApiModelProperty(value = "设备型号")
    private Integer deviceModel;

    @Length(max = 32, message = "pmsType长度必须小于等于32")
    @ApiModelProperty(value = "PMS类型")
    private String pmsType;

    @Length(max = 32, message = "pmsId长度必须小于等于32")
    @ApiModelProperty(value = "PMS ID")
    private String pmsId;

    @Length(max = 64, message = "deviceVendor长度必须小于等于64")
    @ApiModelProperty(value = "生产厂家")
    private String deviceVendor;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date productionDate;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "投运时间")
    private Date usedTime;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date disableDate;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastMaintenance;

    @Length(max = 32, message = "maintenanceCount长度必须小于等于32")
    @ApiModelProperty(value = "维修次数")
    private String maintenanceCount;

    @Length(max = 32, message = "organization长度必须小于等于32")
    @ApiModelProperty(value = "所属单位")
    private String organization;

    @Length(max = 32, message = "department长度必须小于等于32")
    @ApiModelProperty(value = "管理部门")
    private String department;

    @Length(max = 32, message = "responsiblePerson长度必须小于等于32")
    @ApiModelProperty(value = "责任人")
    private String responsiblePerson;

    @Length(max = 32, message = "latitude长度必须小于等于32")
    @ApiModelProperty(value = "纬度")
    private String latitude;

    @Length(max = 32, message = "longitude长度必须小于等于32")
    @ApiModelProperty(value = "经度")
    private String longitude;

    @Length(max = 32, message = "ip长度必须小于等于32")
    @ApiModelProperty(value = "设备IP地址")
    private String ip;

    @Max(value = 999999999)
    @ApiModelProperty(value = "端口")
    private Integer port;

    @Length(max = 32, message = "voltageLevel长度必须小于等于32")
    @ApiModelProperty(value = "电压等级")
    private String voltageLevel;

    @Length(max = 32, message = "sequencePoint长度必须小于等于32")
    @ApiModelProperty(value = "顺控点号")
    private String sequencePoint;

    @Length(max = 32, message = "realCode长度必须小于等于32")
    @ApiModelProperty(value = "实物编码")
    private String realCode;

    @Length(max = 68, message = "address长度必须小于等于68")
    @ApiModelProperty(value = "安装地址")
    private String address;


}
