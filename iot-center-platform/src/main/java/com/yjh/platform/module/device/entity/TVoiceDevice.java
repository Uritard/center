package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * @author lqh
 * @since 2020-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TVoiceDevice对象", description = "声纹设备表")
public class TVoiceDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "声纹监控设备Id（默认为Ip地址）")
    private Long voiceDeviceId;

    @Length(max = 255, message = "voiceDeviceName长度必须小于等于255")
    @ApiModelProperty(value = "声纹监控设备名称（）")
    private String voiceDeviceName;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "变压器下面换流变的设备Id")
    private Long stdDeviceId;

    @Length(max = 64, message = "deviceType长度必须小于等于64")
    @ApiModelProperty(value = "被监测的设备类型")
    private String deviceType;

    @Length(max = 32, message = "configId长度必须小于等于32")
    private Long configId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "上级区域id")
    private Long upRegionId;

    private String state;

    private String voiceCode;


    /**
     * 设备类型
     */
    private String voiceType;

    /**
     * 设备型号
     */
    private String voiceModel;

    /**
     * 生产厂家
     */
    private String voiceFactory;
    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
