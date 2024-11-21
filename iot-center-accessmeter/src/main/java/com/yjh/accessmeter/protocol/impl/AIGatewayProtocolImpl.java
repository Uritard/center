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
    private static Integer midNumber = 1;


    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        for (IotDevice device : devices) {
            if (!mqttClientMap.containsKey(device.getIp())) {
                try {
                    log.info("{}开始注册mqtt",device);
                    //注册
                    String url = "tcp://" + device.getIp() + ":" + device.getPort();
                    MqttClient client = new MqttClient(url, Topic.serverClientId, new MemoryPersistence());
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
                            msgHandler(device.getIp(), client, topic, mqttMessage);
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
                            String unionTopic = String.format(Topic.DATA,gatewayId);
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
                }

            } else {
                String gatewayId = gatewayIdMap.get(ip);
                if (topic.equals(String.format(Topic.DATA, gatewayId))) {
                    //实时数据
//                    Data data = JSONObject.parseObject(String.valueOf(message), Data.class);
//                    log.info("实时数据：{}", data);
//                    //结果数据处理
//                    List<Data.DataInfo> list = data.getDataList().getSignal().getData();
//                    list.addAll(data.getDataList().getPulse().getData());
//                    list.addAll(data.getDataList().getPulse().getData());
                    Map<String, Object> re = new HashMap<>();
//                    list.forEach(dataInfo -> {
//                        re.put(dataInfo.getNodeId(), dataInfo.getValue());
//                    });
                    re.put("type", ProtocolEnum.AI_GATEWAY.getCode());
                    re.put("ip", ip);
//                    SpringBeanUtils.getBean(SensorCollectService.class).asyncResultHandler(re);
                } else if (topic.equals(String.format(Topic.CONFIG_ACK, gatewayId))) {
                    //联动配置列表请求
                    ConfigResp configResp = JSONObject.parseObject(String.valueOf(message), ConfigResp.class);
                    SpringBeanUtils.getBean(SensorCollectService.class).linkageConfigSync(configResp, ip);
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
            String gatewayId = device.getAddress();
            String topic = String.format(Topic.COMMAND, gatewayId);
            ControlReq message = new ControlReq();
            message.setMsgType("cloudReq");
            message.setMid(String.valueOf(midNumber));
            midNumber++;
            message.setCmd("remoteControlCommand");
            message.setDeviceId(point.getChannelNum());
            DeviceExtend deviceExtend = JSONObject.parseObject(point.getExtend(), DeviceExtend.class);
            Map<String,String> paras = new HashMap<>();
            String state = params.get("deviceState").toString();//前段传来 1-开 2-关 网关要求 1-开 0-关
            paras.put(deviceExtend.getCtrl(),"1".equals(state)?"1":"0");
            message.setParas(paras);
            client.publish(topic, message);
        } catch (Exception e) {
            result.setCode(SYSTEMERROR.getCode(), SYSTEMERROR.getName());
            log.error("控制：{} ,参数：{} 出错", device, params, e);
        }
        return result;
    }
}
