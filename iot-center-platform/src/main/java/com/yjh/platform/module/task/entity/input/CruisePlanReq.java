package com.yjh.platform.module.task.entity.input;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Data
public class CruisePlanReq {
    private Long planId;

    private Integer pageSize;

    private Integer pageNum;
}
