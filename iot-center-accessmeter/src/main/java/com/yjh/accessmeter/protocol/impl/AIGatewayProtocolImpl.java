package com.yjh.accessmeter.protocol.impl;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.module.service.SensorCollectService;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.ProtocolListener;
import com.yjh.accessmeter.protocol.ProtocolType;
import com.yjh.accessmeter.protocol.aigateway.info.ControlReq;
import com.yjh.accessmeter.protocol.aigateway.info.Data;
import com.yjh.accessmeter.protocol.aigateway.info.LoginReqMsg;
import com.yjh.accessmeter.protocol.aigateway.topic.Topic;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yjh.accessmeter.common.result.ResultCodeEnum.SYSTEMERROR;
import static com.yjh.accessmeter.protocol.aigateway.topic.Topic.CONTROL_TYPE;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Slf4j
@ProtocolType({ProtocolEnum.AI_GATEWAY})
public class AIGatewayProtocolImpl implements ISensorProtocol {
    private static Map<String, MqttClient> mqttClientMap = new HashMap<>();
    private static Map<String, String> gatewayIdMap = new HashMap<>();
    private volatile boolean inited = false;


    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        for (IotDevice device : devices) {
            if (!mqttClientMap.containsKey(device.getIp())) {
                try {
                    //注册
                    String url = "tcp://" + device.getIp() + ":" + device.getPort();
                    MqttClient client = new MqttClient(url, Topic.serverClientId, new MemoryPersistence());
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    client.connect();
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable throwable) {
                            log.info("Connection lost: " + throwable.getMessage());
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                            log.info("收到消息，topic：{}，msg:{}", topic, mqttMessage);
                            msgHandler(device.getIp(), client, topic, mqttMessage);
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                            log.info("Delivery complete");
                        }
                    });
                    client.subscribe(Topic.GATEWAY_ATTACH);
                    mqttClientMap.put(device.getIp(), client);
                } catch (Exception e) {
                    log.error("设备：{} 建立mqtt链接出错！=》{}", device, e);
                }
            }
        }
        inited = true;
        return this;
    }

    private void msgHandler(String ip, MqttClient client, String topic, MqttMessage message) throws Exception {
        if (topic.equals(Topic.GATEWAY_ATTACH)) {
            //注册消息
            LoginReqMsg loginReqMsg = JSONObject.parseObject(String.valueOf(message), LoginReqMsg.class);
            String gatewayId = loginReqMsg.getGatewayId();
            gatewayIdMap.put(ip, gatewayId);
            client.subscribe(String.format(Topic.DATA, gatewayId));
        } else {
            String gatewayId = gatewayIdMap.get(ip);
            if (topic.equals(String.format(Topic.DATA, gatewayId))) {
                //实时数据
                Data data = JSONObject.parseObject(String.valueOf(message), Data.class);
                log.info("实时数据：{}", data);
                //结果数据处理
                List<Data.DataInfo> list = data.getDataList().getSignal().getData();
                list.addAll(data.getDataList().getPulse().getData());
                list.addAll(data.getDataList().getPulse().getData());
                Map<String, Object> re = new HashMap<>();
                list.forEach(dataInfo -> {
                    re.put(dataInfo.getNodeId(), dataInfo.getValue());
                });
                re.put("type", ProtocolEnum.AI_GATEWAY.getCode());
                re.put("ip", ip);
                SpringBeanUtils.getBean(SensorCollectService.class).asyncResultHandler(re);
            }
        }
    }


    @Override
    public boolean isInit() {
        return inited;
    }

    @Override
    public List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints) {
        return null;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    @Override
    public Result sendControl(IotDevice device, Map<String, Object> params) {
        Result result = new Result();
        try {
            List<IotDevicePoint> list = SpringBeanUtils.getBean(TIotDeviceDao.class).selectPointByDeviceId(device.getId());
            if (list.isEmpty()) {
                result.setCode(SYSTEMERROR.getCode(), "未找到对应设备编码！");
                return result;
            }
            IotDevicePoint point = list.get(0);
            String ip = device.getIp();
            MqttClient client = mqttClientMap.get(ip);
            String gatewayId = gatewayIdMap.get(ip);
            String topic = String.format(Topic.COMMAND, gatewayId);
            ControlReq message = new ControlReq();
            message.setGatewayId(gatewayId);
            message.setType(CONTROL_TYPE);
            ControlReq.Detail detail = new ControlReq.Detail();
            detail.setDotName(point.getPointName());
            detail.setNodeId(point.getChannelNum());
            message.setDetail(detail);
            String deviceState = String.valueOf(params.get("deviceStatus"));
            if ("1".equals(deviceState)) {
                deviceState = "open";
            } else if ("2".equals(deviceState)) {
                deviceState = "close";
            } else {
                result.setCode(SYSTEMERROR.getCode(), "暂不支持！");
                return result;
            }
            message.setValue(deviceState);
            message.setTimestamp(System.currentTimeMillis());
            client.publish(topic, message);
        } catch (Exception e) {
            result.setCode(SYSTEMERROR.getCode(), SYSTEMERROR.getName());
            log.error("控制：{} ,参数：{} 出错", device, params, e);
        }
        return result;
    }

    public static void main(String[] args) {
        String message = "{\n" +
                "\t\"dataList\": {\n" +
                "\t\t\"signal\": {\n" +
                "\t\t\t\"data\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"comstate\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 1\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"空调状态\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 0\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t\"measure\": {\n" +
                "\t\t\t\"data\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"温度\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 25\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"湿度\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 85\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t\"pulse\": {\n" +
                "\t\t\t\"data\": [\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"电能量\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 10000\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"dotName\": \"总功率\",\n" +
                "\t\t\t\t\t\"nodeId\": \"20FD29C8E4635196\",\n" +
                "\t\t\t\t\t\"value\": 10000\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"gatewayId\": \"fd29c8e4-6351-964e-965b-e15997c8d5c95fd8\",\n" +
                "\t\"type\": \"CMD_ REPORTDATA\",\n" +
                "\t\"timestamp\": 1634351050367\n" +
                "}";

        Data data = JSONObject.parseObject(message, Data.class);
        Data data1 = JSONObject.parseObject(message).toJavaObject(Data.class);

        log.info("ddd=={}", data);
    }
}
