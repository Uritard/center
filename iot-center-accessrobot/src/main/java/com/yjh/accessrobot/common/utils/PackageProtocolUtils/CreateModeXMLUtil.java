package com.yjh.accessrobot.common.utils.PackageProtocolUtils;


import com.yjh.accessrobot.common.utils.ValueUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
 * @author YC
 * @date 2020/11/13 - 16:02
 */
public class CreateModeXMLUtil {
    //生成xml
    public static String createXmlFile(List<Map<String, Object>> list, String ftpsFilePath, String stationCode, String fileName, String model) throws Exception {
        String failPath = stationCode + "/linkage/";
        Document document = DocumentHelper.createDocument();
        Element rss = document.addElement(model);//根节点

        if (list != null && list.size() > 0) {
            for (Map<String, Object> item : list) {
                Element childNode61 = rss.addElement("Item");
                for (String key : item.keySet()) {
                    childNode61.addAttribute(key, ValueUtil.Object2String(item.get(key), ""));
                }
            }
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        File dir = new File(ftpsFilePath + "/" + failPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(ftpsFilePath + "/" + failPath + fileName);
        if (file.exists()) {
            deleteDir(new File(ftpsFilePath + "/" + failPath + fileName));
        }
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        // 设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        writer.write(document);
        writer.close();

        return failPath + fileName;
    }
}

