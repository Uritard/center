package com.yjh.accessvideo.module.device.entity.interlanalysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 二维点坐标
 *
 * @author zilong
 * @date 2022/4/11
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private float x;
    private float y;
}

