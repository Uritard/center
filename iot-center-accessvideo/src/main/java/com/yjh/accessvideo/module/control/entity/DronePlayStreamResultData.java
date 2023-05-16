package com.yjh.accessvideo.module.control.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/5/15 11:32
 */
@Data
public class DronePlayStreamResultData {
    private String app;
    private String ip;
    private String steam;
    private String sn;
    private String rtmp_port;
    private String drone_ip;
    private String http_port;
}
