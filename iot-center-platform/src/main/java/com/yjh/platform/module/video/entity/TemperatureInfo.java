package com.yjh.platform.module.video.entity;

import lombok.Data;

import java.util.List;

/**
 * @author prozac.G
 */
@Data
public class TemperatureInfo {

    private List<String> points;

    private Long cameraId;

    private  String  picPath;
}
