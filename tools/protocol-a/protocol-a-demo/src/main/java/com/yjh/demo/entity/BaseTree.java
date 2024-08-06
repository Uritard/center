/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.entity;

import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
@Data
public class BaseTree {
    private String label;
    private List<BaseTree> children;

    public BaseTree() {

    }

    public BaseTree(String label) {
        this.label = label;
    }
}
