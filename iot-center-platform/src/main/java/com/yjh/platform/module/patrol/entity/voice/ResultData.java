package com.yjh.platform.module.patrol.entity.voice;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/05/24
 */
@Data
public class ResultData {

    private String filename;

    private String fileUrl;

    private String type;
    private String value;
    private float startTime;
    private float endTime;
    private float conf;
    private String desc;
}
