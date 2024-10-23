package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Data
public class LoginReqMsg {

    private String gatewayId;
    private String status;
    private String name;
    private String type;
    private String timestamp;
    private String version;
}
