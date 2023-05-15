package com.yjh.accessvideo.module.control.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/5/15 11:08
 */
@Data
public class DroneLoginResult {
    /**
     * 默认返回状态码
     */
    private int code = 200;
    /**
     * 默认返回状态说明
     */
    private String message = "ok";
    /**
     * 返回数据对象
     */
    private DroneLoginResultData data;
}
