/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.protocol_a.impl;

import cn.hutool.core.util.XmlUtil;
import com.yjh.protocol_a.Message;
import org.apache.commons.lang3.StringUtils;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
public class MessageCodecHandler {

    public static String encodeXml(MessageCodec codec, String platform, Message message) {
        if (codec == null) {
            MessageCodec defaulCodec = new MessageCodec(rootTag(platform), true);
            return defaulCodec.encode(message);
        }
        return XmlUtil.format(codec.encode(message));
    }

    public static String encodeXml(MessageCodec codec, Message message) {
        if (codec == null) {
            MessageCodec defaulCodec = new MessageCodec(rootTag(null), true);
            return defaulCodec.encode(message);
        }
        return XmlUtil.format(codec.encode(message));
    }

    public static String rootTag(String platform) {
        String rootTag = "PatrolDevice";
        if (StringUtils.containsAny(platform, "上级系统与巡视主机接口", "巡视系统与边缘节点接口")) {
            rootTag = "PatrolHost";
        } else if (StringUtils.contains(platform, "巡视系统与算法管理平台")) {
            rootTag = "CloudHost";
        }
        return rootTag;
    }
}
