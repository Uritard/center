package com.yjh.accessvideo.hik;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 丫C
 * @date 2023/4/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "HikDeviceInfo", description = "海康设备信息")
public class HikDeviceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备ip")
    private String deviceIp;

    @ApiModelProperty(value = "设备端口")
    private Integer devicePort;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "设备名称")
    private String hikDeviceName;

    @ApiModelProperty(value = "设备id")
    private Long hikDeviceId;

}
