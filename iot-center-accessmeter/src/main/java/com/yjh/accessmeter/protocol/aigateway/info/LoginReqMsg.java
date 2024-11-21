package com.yjh.accessmeter.protocol.aigateway.info;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Data
public class LoginReqMsg {

    private String sn;
    private String module;
    private String manufacture;
    private String algId;
    private String checkID;
}
