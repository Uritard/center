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
import com.yjh.accessmeter.protocol.aigateway.info.ConfigResp;
import com.yjh.accessmeter.protocol.aigateway.info.ControlReq;
import com.yjh.accessmeter.protocol.aigateway.info.DataInfo;
import com.yjh.accessmeter.protocol.aigateway.info.DeviceExtend;
import com.yjh.accessmeter.protocol.aigateway.topic.Topic;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yjh.accessmeter.common.result.ResultCodeEnum.SYSTEMERROR;

/**
 * @Author: lqh
 * @Date: 2024/10/21
 */
@Slf4j
@ProtocolType({ProtocolEnum.AI_GATEWAY})
public class AIGatewayProtocolImpl implements ISensorProtocol {
    public static Map<String, MqttClient> mqttClientMap = new HashMap<>();
    private static Map<String, String> gatewayIdMap = new HashMap<>();
    private volatile boolean inited = false;
    private static Integer midNumber = 1;


    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        for (IotDevice device : devices) {
            if (!mqttClientMap.containsKey(device.getIp())) {
                try {
                    log.info("{}开始注册mqtt",device);
                    //注册
                    String url = "tcp://" + device.getIp() + ":" + device.getPort();
                    MqttClient client = new MqttClient(url, Topic.serverClientId+System.currentTimeMillis(), new MemoryPersistence());
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setCleanSession(true);
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable throwable) {
                            log.info("Connection lost: " + throwable.getMessage());
                            throwable.printStackTrace();
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mqttMessage) {
                            log.info("收到消息，topic：{}，msg:{}", topic, mqttMessage);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    msgHandler(device.getIp(), client, topic, mqttMessage);
                                }
                            }).start();
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                            log.info("Delivery complete");
                        }
                    });
                    client.connect(options);
                    //查询注册到这个ip的网关
                    List<String> gatewayIds = SpringBeanUtils.getBean(TIotDeviceDao.class).selectGatewayIdByIp(device.getIp());
                    if (!gatewayIds.isEmpty()){
                        for (String gatewayId:gatewayIds){
                            String dataTopic = String.format(Topic.DATA,gatewayId);
                            String unionTopic = String.format(Topic.COMMAND_ACk,gatewayId);
                            client.subscribe(dataTopic);
                            client.subscribe(unionTopic);
                            gatewayIdMap.put(dataTopic,gatewayId);
                            gatewayIdMap.put(unionTopic,gatewayId);
                        }
                    }
                    mqttClientMap.put(device.getIp(),client);
                } catch (Exception e) {
                    log.error("设备：{} 建立mqtt链接出错！=》{}", device, e);
                }
            }
        }
        inited = true;
        return this;
    }

    private void msgHandler(String ip, MqttClient client, String topic, MqttMessage message) {
        try {
            if (gatewayIdMap.containsKey(topic)) {

                String gatewayId = gatewayIdMap.get(topic);
                if (topic.equals(String.format(Topic.DATA,gatewayId))){
                    //数据上报消息
                    DataInfo dataInfo = JSONObject.parseObject(String.valueOf(message), DataInfo.class);
                    SpringBeanUtils.getBean(SensorCollectService.class).aIGatewayResultHandler(dataInfo,ip,gatewayId);
                } else if (topic.equals(String.format(Topic.COMMAND_ACk,gatewayId))){
                    //联动规则
                    try {
                        ConfigResp configResp = JSONObject.parseObject(String.valueOf(message), ConfigResp.class);
                        SpringBeanUtils.getBean(SensorCollectService.class).linkageConfigSync(configResp, gatewayId, ip);
                    }catch (Exception e){
                        log.error("消息解析出错！不是对应的消息！ {}",message,e);
                    }
                }
            }
        }catch (Exception e){
            log.error("处理消息出错：topic = {}，message{}",topic,message,e);
        }
    }


    @Override
    public boolean isInit() {
        return inited;
    }

    @Override
    public List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints) {
        //做初始化操作
        this.init(Collections.singletonList(device));
        return null;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    @Override
    public Result sendControl(IotDevice device, Map<String, Object> params) {
        Result result = new Result();
        try {
            if (params.get("linkageType") == null){
                //为空 控制的就是设备
                List<IotDevicePoint> list = SpringBeanUtils.getBean(TIotDeviceDao.class).selectPointByDeviceId(device.getId());
                if (list.isEmpty()) {
                    result.setCode(SYSTEMERROR.getCode(), "未找到对应设备编码！");
                    return result;
                }
                IotDevicePoint point = list.get(0);
                String ip = device.getIp();
                MqttClient client = mqttClientMap.get(ip);
                String gatewayId = device.getAddress();
                String topic = String.format(Topic.COMMAND, gatewayId);
                ControlReq controlReq = new ControlReq();
                controlReq.setMsgType("cloudReq");
                controlReq.setMid(String.valueOf(midNumber));
                midNumber++;
                controlReq.setServiceId("command");
                controlReq.setCmd("remoteControlCommand");
                controlReq.setDeviceId(point.getChannelNum());
                DeviceExtend deviceExtend = JSONObject.parseObject(point.getExtend(), DeviceExtend.class);
                String ctrlCmd = deviceExtend.getCtrl();
                String state = params.get("deviceStatus").toString();
                if (device.getIotDeviceType() == 856){
                    String attr = params.get("deviceAttr").toString();
                    String[] cmd = ctrlCmd.split(",");
                    //空调的话要区分控制开关 还是制冷制热
                    if ("1".equals(state) && "12".contains(attr) && StringUtils.isNotEmpty(attr)){
                        ctrlCmd = cmd[1];
                        state = "1".equals(attr)?"2":"1";
                    } else {
                        ctrlCmd = cmd[0];
                        state = "1".equals(state)?"1":"0";
                    }
                } else {
                    state = "1".equals(state)?"1":"0";
                }
                Map<String,String> paras = new HashMap<>();
                //前段传来 1-开 2-关 网关要求 1-开 0-关
                paras.put(ctrlCmd,state);
                controlReq.setParas(paras);

                MqttMessage mqttMessage = new MqttMessage();
                //保证消息能到达一次
                mqttMessage.setQos(1);
                mqttMessage.setRetained(true);
                String jsonStr = JSONObject.toJSONString(controlReq);
                byte[] msgBytes = jsonStr.getBytes(StandardCharsets.UTF_8);
                mqttMessage.setPayload(msgBytes);
                client.publish(topic, mqttMessage);
            } else {
                //控制的就是同步联动规则
                ControlReq controlReq = new ControlReq();
                controlReq.setMsgType("cloudReq");
                controlReq.setMid(String.valueOf(midNumber));
                midNumber++;
                controlReq.setCmd("LinkageStrategyCall");
                controlReq.setServiceId("command");
                controlReq.setDeviceId(device.getAddress());
                MqttMessage mqttMessage = new MqttMessage();
                //保证消息能到达一次
                mqttMessage.setQos(1);
                mqttMessage.setRetained(true);
                String jsonStr = JSONObject.toJSONString(controlReq);
                byte[] msgBytes = jsonStr.getBytes(StandardCharsets.UTF_8);
                mqttMessage.setPayload(msgBytes);
                String ip = device.getIp();
                MqttClient client = mqttClientMap.get(ip);
                String topic = String.format(Topic.COMMAND, device.getAddress());
                client.publish(topic, mqttMessage);
            }

        } catch (Exception e) {
            result.setCode(SYSTEMERROR.getCode(), SYSTEMERROR.getName());
            log.error("控制：{} ,参数：{} 出错", device, params, e);
        }
        return result;
    }
}
