package com.yjh.accessmeter.protocol.aigateway.info;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @Author: lqh
 * @Date: 2024/10/30
 */
@Data
public class ConfigResp {
    private String errcode;
    private String msgType;
    private String mid;
    private String serviceId;
    private String cmd;
    private String deviceId;
    private Body body;

    @Data
    public static class Body {
        private String deviceId;
        private String configType;
        private String sessionId;
        private String remark;
        private List<ConfigContent> configContent;
    }

    @Data
    public static class ConfigContent {
        private List<Trigger> trigger;
        private String startRelation;
        private StartRly startRly;
        private List<Trigger> stopDgt;
        private StopRly stopRly;
        private String stopRelation;
        private String timeout;

    }

    @Data
    public static class Trigger {
        private String nodeId;
        private String nodeName;
        private String sequence;
        private String propertityType;
        private String propertityName;
        private String condition;
        private String value;
    }

    @Data
    public static class StartRly {
        private String nodeId;
        private String nodeName;
        private List<StartParas> startParas;
    }
    @Data
    public static class StopRly {
        private String nodeId;
        private String nodeName;
        private List<StartParas> stopParas;
    }

    @Data
    public static class StartParas {
        private String propertityName;
        private String value;
    }

    public static void main(String[] args) {
        String json ="{\n" +
                "\t\"errcode\": 0,\n" +
                "\t\"msgType\": \"deviceRsp\",\n" +
                "\t\"mid\": 773138166,\n" +
                "\t\"serviceId\": \"command\",\n" +
                "\t\"cmd\": \"LinkageStrategyCall\",\n" +
                "\t\"deviceId\": \"GS10050WS001202209010021\",\n" +
                "\t\"body\": {\n" +
                "\t\t\"deviceId\": \"GS10050WS001202209010021\",\n" +
                "\t\t\"configType\": \"2\",\n" +
                "\t\t\"sessionId\": \"21\",\n" +
                "\t\t\"configContent\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"trigger\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"nodeId\": \"575334200001\",\n" +
                "\t\t\t\t\t\t\"sequence\": \"1\",\n" +
                "\t\t\t\t\t\t\"propertityType\": \"propertity\",\n" +
                "\t\t\t\t\t\t\"propertityName\": \"Tmp\",\n" +
                "\t\t\t\t\t\t\"condition\": \"morethan\",\n" +
                "\t\t\t\t\t\t\"value\": \"30\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"nodeId\": \"575334200001\",\n" +
                "\t\t\t\t\t\t\"sequence\": \"2\",\n" +
                "\t\t\t\t\t\t\"propertityType\": \"propertity\",\n" +
                "\t\t\t\t\t\t\"propertityName\": \"Hum\",\n" +
                "\t\t\t\t\t\t\"condition\": \"morethan\",\n" +
                "\t\t\t\t\t\t\"value\": \"60\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"startRelation\": \"1&2\",\n" +
                "\t\t\t\t\"startRly\": {\n" +
                "\t\t\t\t\t\"nodeId\": \"5753B6200015\",\n" +
                "\t\t\t\t\t\"startParas\": [\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"propertityName\": \"AirControl\",\n" +
                "\t\t\t\t\t\t\t\"value\": \"1\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"propertityName\": \"ModeSet\",\n" +
                "\t\t\t\t\t\t\t\"value\": \"1\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"propertityName\": \"SpeedSet\",\n" +
                "\t\t\t\t\t\t\t\"value\": \"2\"\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"stopDgt\": [\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"nodeId\": \"575334200001\",\n" +
                "\t\t\t\t\t\t\"sequence\": \"1\",\n" +
                "\t\t\t\t\t\t\"propertityType\": \"propertity\",\n" +
                "\t\t\t\t\t\t\"propertityName\": \"Tmp\",\n" +
                "\t\t\t\t\t\t\"condition\": \"lessthan\",\n" +
                "\t\t\t\t\t\t\"value\": \"20\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\"nodeId\": \"575334200001\",\n" +
                "\t\t\t\t\t\t\"sequence\": \"2\",\n" +
                "\t\t\t\t\t\t\"propertityType\": \"propertity\",\n" +
                "\t\t\t\t\t\t\"propertityName\": \"Hum\",\n" +
                "\t\t\t\t\t\t\"condition\": \"lessthan\",\n" +
                "\t\t\t\t\t\t\"value\": \"50\"\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"stopRelation\": \"1|2\",\n" +
                "\t\t\t\t\"stopRly\": {\n" +
                "\t\t\t\t\t\"nodeId\": \"5753B6200015\",\n" +
                "\t\t\t\t\t\"stopParas\": [\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"propertityName\": \"AirControl\",\n" +
                "\t\t\t\t\t\t\t\"value\": \"0\"\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"timeout\": \"5\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}";
        ConfigResp configResp = JSONObject.parseObject(json,ConfigResp.class);
        System.out.println(configResp);
    }
}
