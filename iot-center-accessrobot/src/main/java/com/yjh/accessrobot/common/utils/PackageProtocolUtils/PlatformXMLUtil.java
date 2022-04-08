package com.yjh.accessrobot.common.utils.PackageProtocolUtils;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author YC
 * @date 2020/11/13 - 16:02
 */
public class PlatformXMLUtil {

    public static String DRONEROOTNAME = "PatrolDevice";
    // 解析xml
    public static XMLBaseModel readStringXmlOut(Document doc) {
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> itemsList = new ArrayList<>();
        /*Document doc = null;*/

        try {
            /// 将字符串转为XML
            /*doc = DocumentHelper.parseText(xml);*/
            // 获取根节点
            Element rootElt = doc.getRootElement();
            // 获取根节点下所有节点
            List<Element> list = rootElt.elements();

            if (rootElt.getName().equals("Device_Model") || rootElt.getName().equals("Robot_Model") || rootElt.getName().equals("Task_Model")
                    || rootElt.getName().equals("Property_Model") || rootElt.getName().equals("PatrolDevcie_Model")) {
                for (Element element : list) {
                    if (element.getName().equals("Item")) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        List<Attribute> attributeList = element.attributes();
                        for (Attribute attribute : attributeList) {
                            map.put(attribute.getName(), attribute.getValue());
                        }
                        if (!map.isEmpty()) {
                            itemsList.add(map);
                        }
                        xmlBaseModel.setItems(itemsList);
                    }
                }
                xmlBaseModel.setItems(itemsList);
            } else if (rootElt.getName().equals("Robot") || rootElt.getName().equals("PatrolDevice")) {
                // 遍历节点
                for (Element element : list) {
                    if (element.getName().equals("SendCode")) {
                        xmlBaseModel.setSendCode(element.getText());
                    }
                    if (element.getName().equals("ReceiveCode")) {
                        xmlBaseModel.setReceiveCode(element.getText());
                    }
                    if (element.getName().equals("Code")) {
                        xmlBaseModel.setCode(element.getText());
                    }
                    if (element.getName().equals("Type")) {
                        xmlBaseModel.setType(element.getText());
                    }
                    if (element.getName().equals("Command")) {
                        xmlBaseModel.setCommand(element.getText());
                    }
                    if (element.getName().equals("Time")) {
                        xmlBaseModel.setTime(element.getText());
                    }
                    if (element.getName().equals("Items")) {
                        List<Element> items = element.elements();
                        if (items.size() != 0) {
                            for (Element item : items) {
                                List<Attribute> attributes = item.attributes();
                                Map<String, Object> map = new HashMap<String, Object>();
                                for (Attribute defaultAttribute : attributes) {
                                    // 节点的属性name为map的key，value为map的value
                                    map.put(defaultAttribute.getName(), defaultAttribute.getValue());
                                }
                                itemsList.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            if (element.attribute("value") != null) {
                                map.put(element.attribute("value").getName(), element.attribute("value").getValue());
                            }
                            if (element.attribute("direction") != null) {
                                map.put(element.attribute("direction").getName(), element.attribute("direction").getValue());
                            }
                            if (!map.isEmpty()) {
                                itemsList.add(map);
                            }
                        }
                        xmlBaseModel.setItems(itemsList);
                    }
                }
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return xmlBaseModel;
    }
    /**
     * 根据模型生成xml文件
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    public static String generateXml(XMLBaseModel xmlBaseModel){
        Document document = DocumentHelper.createDocument();
        // 根节点
        Element rss = document.addElement("Robot");
        // 生成子节点（必有）
        Element childNode1 = rss.addElement("SendCode");
        // 子节点内容
        childNode1.setText(xmlBaseModel.getSendCode());

        // 必有
        Element childNode2 = rss.addElement("ReceiveCode");
        childNode2.setText(xmlBaseModel.getReceiveCode());

        // 必有
        Element childNode3 = rss.addElement("Type");
        childNode3.setText(xmlBaseModel.getType());

        // 非必有
        Element childNode4 = rss.addElement("Code");
        if (StringUtils.isNotEmpty(xmlBaseModel.getCode())){ childNode4.setText(xmlBaseModel.getCode()); }

        // 必有
        Element childNode5 = rss.addElement("Time");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        childNode5.setText(sdf.format(new Date()));
        // 非必有
        Element childNode6= rss.addElement("Items");
        if(xmlBaseModel.getItems()!=null && xmlBaseModel.getItems().size()>0) {
            List<Map<String,Object>> itemsList = xmlBaseModel.getItems();
            for(Map<String, Object> item : itemsList) {
                Element childNode61 = childNode6.addElement("Item");
                for(String key : item.keySet()){
                    childNode61.addAttribute(key, String.valueOf(item.get(key)));
                }
            }
        }
        // 必有
        Element childNode7 = rss.addElement("Command");
        childNode7.setText(xmlBaseModel.getCommand());
        String xmlString = document.asXML();

        return xmlString;
    }

    /**
     * 根据模型生成xml文件
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    public static String generateXml2(XMLBaseModel xmlBaseModel,String rootName){
        Document document = DocumentHelper.createDocument();
        // 根节点
        Element rss = document.addElement(rootName);
        // 生成子节点（必有）
        Element childNode1 = rss.addElement("SendCode");
        // 子节点内容
        childNode1.setText(xmlBaseModel.getSendCode());

        // 必有
        Element childNode2 = rss.addElement("ReceiveCode");
        childNode2.setText(xmlBaseModel.getReceiveCode());

        // 必有
        Element childNode3 = rss.addElement("Type");
        childNode3.setText(xmlBaseModel.getType());

        // 非必有
        Element childNode4 = rss.addElement("Code");
        if (StringUtils.isNotEmpty(xmlBaseModel.getCode())){ childNode4.setText(xmlBaseModel.getCode()); }

        // 必有
        Element childNode5 = rss.addElement("Time");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        childNode5.setText(sdf.format(new Date()));
        // 非必有
        Element childNode6= rss.addElement("Items");
        if(xmlBaseModel.getItems()!=null && xmlBaseModel.getItems().size()>0) {
            List<Map<String,Object>> itemsList = xmlBaseModel.getItems();
            for(Map<String, Object> item : itemsList) {
                Element childNode61 = childNode6.addElement("Item");
                for(String key : item.keySet()){
                    childNode61.addAttribute(key, String.valueOf(item.get(key)));
                }
            }
        }
        // 必有
        Element childNode7 = rss.addElement("Command");
        childNode7.setText(xmlBaseModel.getCommand());
        String xmlString = document.asXML();

        return xmlString;
    }
}

