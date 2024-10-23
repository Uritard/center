package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Data
public class LoginRespMsg {

    private String timestamp;
    private String gatewayId;
    private String type;
    private String name;
    private String status;
    private String code;
    private String msg;
}
