/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/19
 * @since [产品/模块版本] （可选）
 */
@Data
public class TAlgorithmMeteInfo extends TAlgorithmInfo {
    @TableField(value = "device_mete_id", updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @TableField(value = "device_point_id", updateStrategy = FieldStrategy.IGNORED)
    private String devicePointId;

    @TableField(value = "mete_analyse", updateStrategy = FieldStrategy.IGNORED)
    private String meteAnalyse;

    @TableField(value = "mete_ai", updateStrategy = FieldStrategy.IGNORED)
    private String meteAi;

    @TableField(value = "mete_judge", updateStrategy = FieldStrategy.IGNORED)
    private String meteJudge;
}
