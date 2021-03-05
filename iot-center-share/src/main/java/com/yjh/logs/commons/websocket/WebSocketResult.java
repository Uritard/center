package com.yjh.logs.commons.websocket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketResult {

    //上报类型
    private String type;
    //上报内容
    private String info;

}
