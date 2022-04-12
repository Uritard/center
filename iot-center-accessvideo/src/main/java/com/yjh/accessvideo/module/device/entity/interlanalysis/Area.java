package com.yjh.accessvideo.module.device.entity.interlanalysis;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 支持多个多边形框区域， 只填两个点表示矩形框（左上角坐标+右下角坐标）
 *
 * @author zilong
 * @date 2022/4/11
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Area {
    private List<Point> areas;
}
