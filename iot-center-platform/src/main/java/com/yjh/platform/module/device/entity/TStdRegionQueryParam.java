package com.yjh.platform.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/4
 * @since [产品/模块版本] （可选）
 */
@Data
public class TStdRegionQueryParam extends  TStdRegion{

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
