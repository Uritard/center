package com.yjh.platform.module.iot.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/10/30
 */
@Data
public class LinkageConfigResp {
    private String gatewayId;
    private String type;
    private List<LinkageListData> linkageList;
    private String timestamp;
    private String code;
    private String msg;

    @Data
    public static class LinkageListData {
        private ConditionData linkageCondition;
        private String name;
        private String protocol;
        private Boolean recoverType;
        private List<List<TimeSectionData>> timeSection;
        private ConditionData resetCondition;
    }

    @Data
    public static class TimeSectionData {
        private String time;
        private Boolean enable;
    }

    @Data
    public static class ConditionData {
        private List<MainData> main;
        private List<ViceListData> viceList;
        private String type;
        private Integer activeNum;
    }

    @Data
    public static class MainData {
        private Condition condition;
        private String dataType;
        private String dotName;
        private String nodeId;
    }

    @Data
    public static class Condition {
        private String mode;
        private String value;
    }
    @Data
    public static class ViceListData {
        private String dataType;
        private String nodeId;
        private String presetName;
        private String speed;
        private String dotName;
        private String value;
    }

    public static void main(String[] args) {
        String json ="{\n" +
                "\t\"code\": 200,\n" +
                "\t\"gatewayId\": \"328ed314-ee21-b166-d8e7-7db87246f0a856f0\",\n" +
                "\t\"linkageList\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"linkageCondition\": {\n" +
                "\t\t\t\t\"activeNum\": 1,\n" +
                "\t\t\t\t\"main\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"condition\": {\n" +
                "\t\t\t\t\t\t\t\"mode\": \"equal\",\n" +
                "\t\t\t\t\t\t\t\"value\": 1\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": \"signal\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"主控楼一楼门禁\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000077\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"condition\": {\n" +
                "\t\t\t\t\t\t\t\"mode\": \"equal\",\n" +
                "\t\t\t\t\t\t\t\"value\": 1\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": \"measure\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"锁状态\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000065\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"type\": \"or\",\n" +
                "\t\t\t\t\"viceList\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"dataType\": \"control\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"LampCtrl\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000066\",\n" +
                "\t\t\t\t\t\t\"type\": \"device\",\n" +
                "\t\t\t\t\t\t\"value\": \"open\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t]\n" +
                "\t\t\t},\n" +
                "\t\t\t\"name\": \"门禁-灯光联动\",\n" +
                "\t\t\t\"protocol\": \"pinnacle\",\n" +
                "\t\t\t\"recoverType\": true,\n" +
                "\t\t\t\"resetCondition\": {\n" +
                "\t\t\t\t\"activeNum\": 2,\n" +
                "\t\t\t\t\"main\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"condition\": {\n" +
                "\t\t\t\t\t\t\t\"mode\": \"equal\",\n" +
                "\t\t\t\t\t\t\t\"value\": 0\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": \"signal\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"主控楼一楼门禁\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000077\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"condition\": {\n" +
                "\t\t\t\t\t\t\t\"mode\": \"equal\",\n" +
                "\t\t\t\t\t\t\t\"value\": 0\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"dataType\": \"measure\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"锁状态\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000065\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"type\": \"and\",\n" +
                "\t\t\t\t\"viceList\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"dataType\": \"control\",\n" +
                "\t\t\t\t\t\t\"dotName\": \"LampCtrl\",\n" +
                "\t\t\t\t\t\t\"nodeId\": \"PNC000000066\",\n" +
                "\t\t\t\t\t\t\"type\": \"device\",\n" +
                "\t\t\t\t\t\t\"value\": \"close\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t]\n" +
                "\t\t\t},\n" +
                "\t\t\t\"timeSection\": [\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t[\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": true,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"enable\": false,\n" +
                "\t\t\t\t\t\t\"time\": \"00:00:00-23:59:59\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t]\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"msg\": \"成功\",\n" +
                "\t\"timestamp\": 1648101618375,\n" +
                "\t\"type\": \"CMD_GET_LINKAGELIST\"\n" +
                "}";
//        JSONObject.parseObject(json).toJavaObject(ConfigResp.class);
        LinkageConfigResp configResp = JSONObject.parseObject(json, LinkageConfigResp.class);
        System.out.println(configResp);
    }
}
