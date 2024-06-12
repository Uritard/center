/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.protocol_a.impl;

import com.yjh.protocol_a.Message;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
public class MessageCodecHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageCodecHandler.class);

    public static String encodeXml(MessageCodec codec, String platform, Message message) {
        if (codec == null) {
            MessageCodec defaulCodec = new MessageCodec(rootTag(platform), true);
            return formatXml(defaulCodec.encode(message));
        }
        return formatXml(codec.encode(message));
    }

    public static String encodeXml(MessageCodec codec, Message message) {
        if (codec == null) {
            MessageCodec defaulCodec = new MessageCodec(rootTag(null), true);
            return defaulCodec.encode(message);
        }
        return formatXml(codec.encode(message));
    }

    public static String formatXml(String xmlStr) {
        StringWriter out = new StringWriter();
        try {
            Document document = DocumentHelper.parseText(xmlStr);

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            format.setNewLineAfterDeclaration(false);

            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.flush();
        } catch (Exception e) {
            log.error("格式化 xml 失败 ", e);
        }
        return out.toString();
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
