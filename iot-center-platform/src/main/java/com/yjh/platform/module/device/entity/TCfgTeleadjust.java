package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
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
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgTeleadjust对象", description = "遥调量表")
public class TCfgTeleadjust implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 20,message = "deviceId长度必须小于等于20")
    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @Length(max = 20,message = "meteId长度必须小于等于20")
    @ApiModelProperty(value = "监控量编号")
    private String meteId;

    @Length(max = 256,message = "meteName长度必须小于等于256")
    @ApiModelProperty(value = "监控量名称")
    private String meteName;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效下限")
    private Float downEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "小数点后的有效位数")
    private Integer metePrecision;

    @Length(max = 16,message = "unit长度必须小于等于16")
    @ApiModelProperty(value = "单位")
    private String unit;

    @Max(value=999999999)
    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @Max(value=999999999)
    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @Max(value=999999999)
    @ApiModelProperty(value = "遥调量类型")
    private Integer adjustKind;

    @Max(value=999999999)
    @ApiModelProperty(value = "当前值")
    private Float lastValue;

    @Past
    @ApiModelProperty(value = "更新时间")
    private Date lastTime;

    @Length(max = 20,message = "meteCode长度必须小于等于20")
    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;;

    @Length(max = 20,message = "deviceType长度必须小于等于20")
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @Length(max = 512,message = "description长度必须小于等于512")
    @ApiModelProperty(value = "监控量描述")
    private String description;

    @Max(value=999999999)
    @ApiModelProperty(value = "标称值")
    private Float stander;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否可控")
    private Integer controlenable;


}
