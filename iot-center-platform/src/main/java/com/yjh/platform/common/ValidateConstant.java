/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/12/25
 * @since [产品/模块版本] （可选）
 */
public interface ValidateConstant {
    String REG_RICH_NAME = "^[a-zA-Z0-9\\u4e00-\\u9fa5_\\-#]+$";
    String MSG_RICH_NAME = "不可以有“#-_”以外的特殊字符";

    String REG_DEVICE_PROPERTIES = "^[ a-zA-Z0-9\":\\{\\}\\[\\]_]+$";
    String MSG_DEVICE_PROPERTIES = "不可以有中文和“:{}[] ”以外的特殊字符";
}
