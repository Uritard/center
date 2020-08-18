package com.yjh.platform.module.device.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDeviceAttr对象", description = "标准化设备参数表")
public class TStdDeviceAttr {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "")
    private Integer deviceSubtype;

    @ApiModelProperty(value = "")
    private String serial;

    @ApiModelProperty(value = "生产厂家")
    private String manufacturer;

    @ApiModelProperty(value = "")
    private String supplier;

    @ApiModelProperty(value = "",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date productionDate;

    @ApiModelProperty(value = "投运时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date openingDate;

    @ApiModelProperty(value = "",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date disableDate;

    @ApiModelProperty(value = "",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastMaintenance;

    @ApiModelProperty(value = "")
    private String maintenanceCycle;

    @ApiModelProperty(value = "所属单位")
    private String organization;

    @ApiModelProperty(value = "管理部门")
    private String department;

    @ApiModelProperty(value = "责任人")
    private String responsiblePerson;

    @ApiModelProperty(value = "")
    private String latitude;

    @ApiModelProperty(value = "")
    private String longitude;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "电压等级")
    private String para1;

    @ApiModelProperty(value = "顺控点号")
    private String para2;

    @ApiModelProperty(value = "实物编码")
    private String para3;

}
