package com.yjh.platform.module.task.entity;

import com.yjh.platform.module.task.entity.input.RegionVideo;
import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/11/10
 * @since [产品/模块版本] （可选）
 */
@Data
public class StationDetail {

    List<StationCount> stationCounts;

    List<EnvDeviceStatus> envDeviceStatuses;

    List<RegionVideo> regionVideos;

}
