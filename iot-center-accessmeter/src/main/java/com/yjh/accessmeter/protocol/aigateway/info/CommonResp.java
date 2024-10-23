package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Data
public class CommonResp {
    private String timestamp;
    private String gatewayId;
    private String type;
    private String code;
    private String msg;
}
