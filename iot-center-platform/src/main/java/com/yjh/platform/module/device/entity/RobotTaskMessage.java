package com.yjh.platform.module.device.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2020/11/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RobotTaskMessage implements Serializable {

    private String instanceName;//巡视点名称

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;//巡视时间

//    private Long deviceId;//设备id

    private String deviceName;//设备名称

    private String result;//巡视结果
}
