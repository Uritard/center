package com.yjh.accessmeter.protocol.aigateway.info;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@lombok.Data
public class DataInfo {
    private List<Devices> devices;

    @lombok.Data
    public static class Devices{
        private String deviceId;
        private String serviceId;
        private String deviceType;
        private List<Map<String,String>> data;
        private String eventTime;
    }


    public static void main(String[] args) {
        String message = "{\n" +
                "\t\"devices\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"deviceId\": \"xxxxxx\",\n" +
                "\t\t\t\"serviceId\": \"analog\",\n" +
                "\t\t\t\"deviceType\": \"TTAH\",\n" +
                "\t\t\t\"data\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"Tmp\": \"24.5\",\n" +
                "\t\t\t\t\t\"Hum\": \"54.8\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"eventTime\": \"20191010T091310Z\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"deviceId\": \"xxxxxx\",\n" +
                "\t\t\t\"serviceId\": \"analog\",\n" +
                "\t\t\t\"deviceType\": \"TTAH\",\n" +
                "\t\t\t\"data\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"O2\": \"21.7\",\n" +
                "\t\t\t\t\t\"SF6\": \"0.0\"\n" +
                "\t\t\t\t}\n" +
                "\t\t\t],\n" +
                "\t\t\t\"eventTime\": \"20191010T091310Z\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        DataInfo dataInfo = JSONObject.parseObject(String.valueOf(message), DataInfo.class);
        System.out.println(dataInfo);
    }
}
