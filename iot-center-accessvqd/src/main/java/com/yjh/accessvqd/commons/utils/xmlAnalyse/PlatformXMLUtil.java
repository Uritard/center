package com.yjh.accessvqd.commons.utils.xmlAnalyse;

// TODO: 2021/1/14 导入指定报文xml的容器对象 
import com.yjh.accessvqd.module.device.entity.XMLBaseModel;
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
//    //解析xml
//    public static XMLBaseModel readStringXmlOut(Document doc) {
//        XMLBaseModel xmlBaseModel = new XMLBaseModel();
//        List<Map<String, Object>> itemsList = new ArrayList<>();
////        Document doc = null;
//
//        try {
////            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
//            Element rootElt = doc.getRootElement(); // 获取根节点
//            List<Element> list = rootElt.elements();// 获取根节点下所有节点
//            for (Element element : list) { // 遍历节点
//                if (element.getName().equals("SendCode")) {
//                    xmlBaseModel.setSendCode(element.getText());
//                }
//                if (element.getName().equals("ReceiveCode")) {
//                    xmlBaseModel.setReceiveCode(element.getText());
//                }
//                if (element.getName().equals("Code")) {
//                    xmlBaseModel.setCode(element.getText());
//                }
//                if (element.getName().equals("Type")) {
//                    xmlBaseModel.setType(element.getText());
//                }
//                if (element.getName().equals("Command")) {
//                    xmlBaseModel.setCommand(element.getText());
//                }
//                if (element.getName().equals("Time")) {
//                    xmlBaseModel.setTime(element.getText());
//                }
//                if(element.getName().equals("Items")){
//                    List<Element> items = element.elements();
//                    if (items.size()!= 0){
//                        for(Element item : items){
//                            List<Attribute> attributes = item.attributes();
//                            Map<String, Object> map = new HashMap<String, Object>();
//                            for(Attribute defaultAttribute : attributes){
//                                map.put(defaultAttribute.getName(),defaultAttribute.getValue());// 节点的属性name为map的key，value为map的value
//                            }
//                            itemsList.add(map);
//                        }
//                    }else {
//                        Map<String, Object> map = new HashMap<String, Object>();
//                        if (element.attribute("value")!=null){
//                            map.put(element.attribute("value").getName(),element.attribute("value").getValue());
//                        }
//                        if (element.attribute("direction")!=null){
//                            map.put(element.attribute("direction").getName(),element.attribute("direction").getValue());
//                        }
//                        if (!map.isEmpty()){
//                            itemsList.add(map);
//                        }
//                    }
//                    xmlBaseModel.setItems(itemsList);
//                }
//            }
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
//        return xmlBaseModel;
//    }
    //生成xml
    public static String generateXml(XMLBaseModel xmlBaseModel){
        Document document = DocumentHelper.createDocument();
        Element rss = document.addElement("Robot");//根节点
        Element childNode1 = rss.addElement("SendCode");//生成子节点（必有）
        childNode1.setText(xmlBaseModel.getSendCode());//子节点内容

        Element childNode2 = rss.addElement("ReceiveCode");//必有
        childNode2.setText(xmlBaseModel.getReceiveCode());

        Element childNode3 = rss.addElement("Type");//必有
        childNode3.setText(xmlBaseModel.getType());

        Element childNode4 = rss.addElement("Code");//非必有
        if (StringUtils.isNotEmpty(xmlBaseModel.getCode())){ childNode4.setText(xmlBaseModel.getCode()); }

        Element childNode5 = rss.addElement("Time");//必有
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        childNode5.setText(sdf.format(new Date()));
        Element childNode6= rss.addElement("Items");//非必有
        if(xmlBaseModel.getItems()!=null && xmlBaseModel.getItems().size()>0) {
            List<String> itemsList = xmlBaseModel.getItems();
            for(int i=0;i<itemsList.size();i++){
                switch (i){
                    case 0:
                        Element childNode61=childNode6.addElement("sign");
                        childNode61.setText(itemsList.get(0));
                        break;
                    case 1:
                        Element childNode62=childNode6.addElement("blur");
                        childNode62.setText(itemsList.get(1));
                        break;
                }

            }
        }
        Element childNode7 = rss.addElement("Command");//必有
        childNode7.setText(xmlBaseModel.getCommand());
        String xmlString = document.asXML();

        return xmlString;
    }
}

