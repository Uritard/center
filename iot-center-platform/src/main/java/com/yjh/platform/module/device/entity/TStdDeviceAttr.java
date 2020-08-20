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
 * @since 2020-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDeviceAttr对象", description = "标准化设备参数表")
public class TStdDeviceAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long deviceId;

    private Integer deviceSubtype;

    private String serial;

    @ApiModelProperty(value = "生产厂家")
    private String manufacturer;

    private String supplier;

    private Date productionDate;

    @ApiModelProperty(value = "投运时间")
    private Date openingDate;

    private Date disableDate;

    private Date lastMaintenance;

    private String maintenanceCycle;

    @ApiModelProperty(value = "所属单位")
    private String organization;

    @ApiModelProperty(value = "管理部门")
    private String department;

    @ApiModelProperty(value = "责任人")
    private String responsiblePerson;

    private String latitude;

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
