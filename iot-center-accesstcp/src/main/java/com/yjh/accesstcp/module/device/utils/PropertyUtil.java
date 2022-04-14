package com.yjh.accesstcp.module.device.utils;


import java.util.Properties;
import java.util.ResourceBundle;

public class PropertyUtil {

    private static Properties properties = new Properties();

    static {
        ResourceBundle propResourceBundle = ResourceBundle.getBundle("application-dev");
        for (String key : propResourceBundle.keySet()) {
            properties.put(key, propResourceBundle.getString(key));
        }
    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

}
