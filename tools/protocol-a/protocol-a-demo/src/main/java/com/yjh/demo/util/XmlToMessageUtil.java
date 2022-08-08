package com.yjh.demo.util;

import com.yjh.protocol_a.Message;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: lqh
 * @Date: 2022/07/22
 */
public class XmlToMessageUtil {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Message decode(String xmlString) throws Exception {
        Document document = DocumentHelper.parseText(xmlString);
        Element root = document.getRootElement();

        Message message = new Message();
        message.setSendCode(root.elementText("SendCode"));
        message.setReceiveCode(root.elementText("ReceiveCode"));
        message.setType(root.elementText("Type"));
        message.setCommand(root.elementText("Command"));
        message.setCode(root.elementText("Code"));
        message.setTime(timeStringToLocalDateTime(root.elementText("Time")));
        message.setItems(decodeItems(root.element("Items")));

        return message;
    }

    private static LocalDateTime timeStringToLocalDateTime(String str) {
        try {
            return LocalDateTime.parse(str, formatter);
        } catch (Exception var3) {
            return LocalDateTime.now();
        }
    }

    private static List<Map<String, Object>> decodeItems(Element itemsElement) {
        if (itemsElement == null) {
            return Collections.emptyList();
        } else {
            List<Element> elementItems = itemsElement.elements();
            List<Map<String, Object>> items = new ArrayList(elementItems.size());
            Iterator var4 = elementItems.iterator();

            while(true) {
                Element element;
                do {
                    if (!var4.hasNext()) {
                        return items;
                    }

                    element = (Element)var4.next();
                } while(!"Item".equals(element.getName()));

                List<Attribute> attributes = element.attributes();
                Map<String, Object> map = new HashMap(attributes.size());
                Iterator var8 = attributes.iterator();

                while(var8.hasNext()) {
                    Attribute attr = (Attribute)var8.next();
                    map.put(attr.getName(), attr.getValue());
                }

                items.add(map);
            }
        }
    }
}
