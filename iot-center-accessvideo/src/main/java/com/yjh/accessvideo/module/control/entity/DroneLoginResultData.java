package com.yjh.accessvideo.module.control.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/5/15 11:05
 */
@Data
public class DroneLoginResultData {
    private String access_token;
    private Integer expires_in;
}
