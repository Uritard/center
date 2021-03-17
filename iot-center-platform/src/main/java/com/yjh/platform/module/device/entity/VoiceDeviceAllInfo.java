package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author lqh
 * @since 2021/2/26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "TVoiceDevice对象全量信息", description = "声纹设备表全部信息")
public class VoiceDeviceAllInfo {

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

    @Max(value = 999999999999999999l)
    private Long configId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "上级区域id")
    private Long upRegionId;

    @ApiModelProperty(value = "ftp地址")
    private String ftpUrl;

    @ApiModelProperty(value = "所属电站")
    private String stationId;

    @ApiModelProperty(value = "用户名")
    private String owner;

    @ApiModelProperty(value = "登陆密码")
    private String ownerCode;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "Ftp声纹绝对路径")
    @TableField("absoluPath")
    private String absoluPath;

    @ApiModelProperty(value = "Ftp声纹相对路径")
    @TableField("relativePath")
    private String relativePath;

    @ApiModelProperty(value = "分贝告警值")
    @TableField("dbValue")
    private String dbValue;

    @ApiModelProperty(value = "频率限值")
    private String fValue;

    private String mpValue;

    @ApiModelProperty(value = "算法配置文件路径")
    @TableField("filePath")
    private String filePath;

    private String channelNum;


}
