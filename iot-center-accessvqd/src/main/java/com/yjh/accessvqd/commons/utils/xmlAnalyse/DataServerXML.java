package com.yjh.accessvqd.commons.utils.xmlAnalyse;

import com.yjh.accessvqd.module.device.entity.XMLBaseModel;
import com.yjh.accessvqd.module.diagnose.entity.DataServer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.lang.annotation.ElementType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 数据服务器配置信息XML生成器（DataServer）
 */
public class DataServerXML {

    public static String generateDataServerXML(DataServer dataServer){
        Document document=DocumentHelper.createDocument();
        Element root=document.addElement("DataServer");
        Element childNode1=root.addElement("id");
        childNode1.setText(dataServer.getServerId());
        Element childNode2=root.addElement("ip");
        childNode2.setText(dataServer.getServerIp());
        Element childNode3=root.addElement("port");
        childNode3.setText(dataServer.getServerPort());
        if(Objects.nonNull(dataServer.getAlarmFlag())){
            Element childNode4=root.addElement("alarmFlag");
            childNode4.setText(dataServer.getAlarmFlag());
        }

        String xmlString=document.asXML();
        return xmlString;
    }

}
