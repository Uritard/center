/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.annotation;

import java.lang.annotation.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/6/18
 * @since [产品/模块版本] （可选）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelExtend {
    /**
     * 是否必须
     */
    boolean require() default false;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 转为 dict 表数值类型
     */
    String dictType() default "";

    /**
     * 是否需要转换为 int类型
     */
    String convent() default "";
}
