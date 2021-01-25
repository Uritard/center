package com.yjh.accessvqd.commons.utils.xmlAnalyse;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

public class ResponseXML {

    public static String unPackingXMl(String response) throws DocumentException {

       String statusResult=null;
        Document doc= DocumentHelper.parseText(response);
        Element root=doc.getRootElement();
        Element status=root.element("status");
        String value=status.getText();
        System.out.print("responseValue:"+value);
        switch (value){
            case "0":
                statusResult="成功";
                break;
            case "1":
                statusResult="无权限";
                break;
            case "2":
                statusResult="数据有误";
                break;
            case "3":
                statusResult="用户不在线";
                break;
            case "4":
                statusResult="用户已存在";
                break;
            case "5":
                statusResult="超过最大用户数";
                break;
            case "6":
                statusResult="超过最大任务数";
                break;
        }
        return  statusResult;
    }
}
