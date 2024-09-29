/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/12/25
 * @since [产品/模块版本] （可选）
 */
public interface ValidateConstant {
    String REG_RICH_NAME = "^[a-zA-Z0-9\\u4e00-\\u9fa5_\\-#IVXLCDM]*$";
    String MSG_RICH_NAME = "不可以有“#-_”以外的特殊字符";

    String REG_DEVICE_PROPERTIES = "^[ #a-zA-Z0-9,\":\\{\\}\\[\\]_]*$";
    String MSG_DEVICE_PROPERTIES = "不可以有中文和“:#{},[] _”以外的特殊字符";

    String REG_SPECIAL_CHARACTERS = "^[a-zA-Z0-9_]*$";
    String MSG_SPECIAL_CHARACTERS = "只可以输入字母数字和“_”";

    String REG_IP_ADDRESS = "^((((\\d)|([1-9]\\d)|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d)|([1-9]\\d)|(1\\d{2})|(2[0-4]\\d)|(25[0-5])))?$";
    String MSG_IP_ADDRESS = "IP 地址不合法";
}
