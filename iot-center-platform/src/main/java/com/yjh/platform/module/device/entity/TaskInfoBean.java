package com.yjh.platform.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
public class TaskInfoBean {

    private  int totalCount;

    private int  abnormalCount;

    private int runningCount;
}
