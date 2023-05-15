package com.yjh.accessvideo.module.control.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description
 * @Date create in 2023/5/15 10:37
 */
@Data
public class DroneLoginBodyEntity {
    private String userName;
    private String passWord;
    private String uuid;
    private String code;
}
