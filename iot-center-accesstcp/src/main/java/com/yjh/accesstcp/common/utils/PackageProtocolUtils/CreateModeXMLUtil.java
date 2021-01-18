package com.yjh.accesstcp.common.utils.PackageProtocolUtils;


import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
 * @author YC
 * @date 2020/11/13 - 16:02
 */
public class CreateModeXMLUtil {
    //生成xml
    public static String createXmlFile(List<Map<String,Object>> list,String failPath,String fileName) throws Exception{
        Document document = DocumentHelper.createDocument();
        //todo 记得改
        Element rss = document.addElement("Device_model");//根节点

        if(list!=null && list.size()>0) {
            List<Map<String,Object>> itemsList = list;
            for(Map<String, Object> item : itemsList) {
                Element childNode61 = rss.addElement("Item");
                for(String key : item.keySet()){
                    childNode61.addAttribute(key, String.valueOf(item.get(key)));
                }
            }
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        File file = new File(failPath+"/"+fileName);
        if(file.exists()){
            deleteDir(new File(failPath + "/"+fileName));
        }
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        // 设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        writer.write(document);
        writer.close();

        return  failPath+"/"+fileName;
    }
}

