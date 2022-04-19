package com.yjh.accessvideo.common.mqtt.testjsonmsg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * jeff， 测试消息 模拟算法平台返回的json消息体 //缺陷消息
 */
@NoArgsConstructor
@Data
public class Jsonmsg {
    @JsonProperty("msgType")
    private String msgType;
    @JsonProperty("msgID")
    private String msgID;
    @JsonProperty("msgData")
    private MsgDataDTO msgData;

    @NoArgsConstructor
    @Data
    public static class MsgDataDTO {
        @JsonProperty("data")
        private DataDTO data;
        @JsonProperty("desNode")
        private String desNode;
        @JsonProperty("srcNode")
        private String srcNode;

        @NoArgsConstructor
        @Data
        public static class DataDTO {
            @JsonProperty("resultInfo1")
            private ResultInfo1DTO resultInfo1;

            @NoArgsConstructor
            @Data
            public static class ResultInfo1DTO {
                @JsonProperty("resultValue")
                private String resultValue;
                @JsonProperty("instanceId")
                private String instanceId;
                @JsonProperty("analyseType")
                private String analyseType;
                @JsonProperty("analyseResultImg")
                private String analyseResultImg;
                @JsonProperty("taskId")
                private String taskId;
            }
        }
    }
}
