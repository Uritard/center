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
    public final static String GATEWAY_ATTACH = "/gateway/gatewayAttach";
    public final static String GATEWAY_ATTACH_ACK = "/gateway/${gatewayId}/gatewayAttachAck";

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
     */
    public final static String DATA = "/gateway/%s/data";

    /**
     * 遥控
     */
    public final static String COMMAND = "/gateway/%s/command";
    public final static String COMMAND_ACk = "/gateway/${gatewayId}/commandAck";


    /**
     * 联动列表
     */
    public final static String CONFIG = "/gateway/%s/config";
    public final static String CONFIG_ACK = "/gateway/%s/configAck";

}
