package com.yjh.accessmeter.protocol.aigateway.topic;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
public interface Topic {

    public final static String serverClientId = "iot-platform-yjh";
    public final static String CONTROL_TYPE = "CMD_TOPO_CONTROL";

    /**
     * 网关注册
     */
    public final static String GATEWAY_ATTACH = "/v1/edge/access";
    //  /v1/edge/{manufacture}/{module}/{sn}
    public final static String GATEWAY_ATTACH_ACK = "/v1/edge/%d/%d/%d";

    /**
     * 心跳
     */
    public final static String HEART_BEAT = "/gateway/${gatewayId}/heartBeat";

    /**
     * 网关接入
     */
    public final static String LINK_UP = "/gateway/${gatewayId}/linkup";
    public final static String LINK_UP_ACK = "/gateway/${gatewayId}/linkupAck";

    /**
     * 实时数据
     * /v1/devices/{gatewayId}/datas
     */

    public final static String DATA = "/v1/devices/%s/datas";

    /**
     * 遥控
     *  /v1/devices/{gatewayId}/command
     */
    public final static String COMMAND = "/v1/devices/%s/command";
    public final static String COMMAND_ACk = "/v1/devices/%s/commandResponse";


    /**
     * 联动列表
     */
//    public final static String CONFIG = "/v1/devices/%s/command";
//    public final static String CONFIG_ACK = "/v1/devices/%s/commandResponse";

}
