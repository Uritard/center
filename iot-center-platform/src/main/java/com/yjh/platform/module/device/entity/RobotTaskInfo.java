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
public class RobotTaskInfo implements Serializable {

    private String cruiseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;

    private Long deviceID;

    private String deviceName;

    private String result;
}
