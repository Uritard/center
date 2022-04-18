package com.yjh.platform.common.mqtt.msg;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author:jeff
 * @Description: 心跳消息
 */
@NoArgsConstructor
@Data
public class HeartMessageInfo {
    @JsonProperty("province_name")
    private String provinceName;
    @JsonProperty("city_name")
    private String cityName;
    @JsonProperty("volt_level")
    private String voltLevel;
    @JsonProperty("station_name")
    private String stationName;
    @JsonProperty("section_ip")
    private String sectionIp;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("msg_type")
    private String msgType;
    @JsonProperty("time")
    private String time;
}
