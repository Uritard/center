package com.yjh.platform.common.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author YC
 * @date 2020/11/13 - 16:02
 */
public class PlatformXmlUtil {
    private static final Logger log = LoggerFactory.getLogger(PlatformXmlUtil.class);

    /**
     * 根据 List 信息创建 xml 文件
     * @param list     xml 内容，对象是 Map
     * @param filePath xml 文件路径
     * @param fileName xml 文件名
     * @param root     xml 根节点
     * @return xml文件绝对路径
     * @throws IOException 异常
     */
    public static String createXmlFile(List<Map<String, Object>> list, String filePath, String fileName, String root) throws IOException {
        Document document = DocumentHelper.createDocument();
        // 根节点
        Element rss = document.addElement(root);

        if (CollectionUtils.isNotEmpty(list)) {
            for (Map<String, Object> item : list) {
                Element childNode61 = rss.addElement("Item");
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    childNode61.addAttribute(entry.getKey(), Objects.toString(entry.getValue(), ""));
                }
            }
        }
        return writeXmlFile(filePath, fileName, document);
    }

    /**
     * 根据 List 信息创建 xml 文件
     * @param list     xml 内容，对象会转换成 Map，并将驼峰转为下划线
     * @param filePath xml 文件路径
     * @param fileName xml 文件名
     * @param root     xml 根节点
     * @return xml文件绝对路径
     * @throws IOException 创建异常
     */
    public static <T> String createXmlFileT(List<T> list, String filePath, String fileName, String root) throws IOException {
        Document document = DocumentHelper.createDocument();
        // 根节点
        Element rss = document.addElement(root);

        if (CollectionUtils.isNotEmpty(list)) {
            for (T item : list) {
                Element childNode61 = rss.addElement("Item");
                Map<String, Object> itemMap = BeanUtil.beanToMap(item, true, false);
                for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                    childNode61.addAttribute(entry.getKey(), Objects.toString(entry.getValue(), ""));
                }
            }
        }
        return writeXmlFile(filePath, fileName, document);
    }

    /**
     * 创建 xml 文件
     * @param filePath xml 文件路径
     * @param fileName xml 文件名
     * @param document xml Document 节点
     * @return xml文件绝对路径
     * @throws IOException 创建异常
     */
    private static @NotNull String writeXmlFile(String filePath, String fileName, Document document) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        File dir = new File(filePath);
        // 创建父目录
        FileUtil.mkdir(dir);
        File file = new File(dir, fileName);
        // 删除已存在文件
        FileUtil.del(file);

        XMLWriter writer = new XMLWriter(Files.newOutputStream(file.toPath()), format);
        // 设置是否转义，默认使用转义字符
        writer.setEscapeText(false);
        writer.write(document);
        writer.close();

        return file.getAbsolutePath();
    }

}

