/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.impl;

import com.alibaba.fastjson.JSON;
import com.yjh.accessmeter.common.result.BusinessException;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.common.utils.XmlUtil;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.module.service.SensorListenerService;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.ProtocolListener;
import com.yjh.accessmeter.protocol.ProtocolType;
import com.yjh.accessmeter.protocol.entity.*;
import com.yjh.accessmeter.protocol.impl.transport.TcpShortManager;
import io.netty.channel.ChannelFuture;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/1/30
 * @since [产品/模块版本] （可选）
 */
@ProtocolType({ProtocolEnum.ENV_TERMINAL})
public class EnvTerminalProtocolImpl implements ISensorProtocol {

    private volatile boolean inited = false;

    private static ChannelFuture serverFuture;

    public static final Map<String, String> DEVICE_STATION_MAP = new ConcurrentHashMap<>(32);
    public static final String DEFAULT_STATION = "000001";

    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        inited = true;
        try {
            initServerBind();

            TcpShortManager shortManager = TcpShortManager.INSTANSE;

            devices.stream()
                .collect(Collectors.toMap(d -> d.getIp() + d.getPort(), Function.identity(), (d1, d2) -> d1))
                .forEach((k, v) -> {
                    try {
                        ChannelFuture future = shortManager.connect(v);
                        future.channel().closeFuture().sync();
                        LOGGER.info("初始化成功: {}", v);
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("初始化异常 {}", v, e);
                    }
                });
        } catch (Exception e) {
            LOGGER.error("初始化异常", e);
        }
        return this;
    }

    public synchronized void initServerBind() {
        try {
            if (serverFuture == null) {
                SensorListenerService listenerService = SpringBeanUtils.getBean(SensorListenerService.class);
                if (listenerService != null) {
                    serverFuture = listenerService.initServerBind();
                }
            }
        } catch (Exception e) {
            LOGGER.error("绑定端口失败", e);
        }
    }

    @Override
    public boolean isInit() {
        return inited;
    }

    @Override
    public List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints) {
        try {
            String station = null;
            Map<Pair<String, String>, Map<String, IotDevicePoint>> msgTypeGroup = new HashMap<>(16);

            for (IotDevicePoint d : devicePoints) {
                EnvBase env = JSON.parseObject(d.getExtend(), EnvBase.class);
                if (env == null) {
                    LOGGER.error("点位没有配置额外参数： {}", d);
                    continue;
                }
                if (StringUtils.isEmpty(station) && StringUtils.isNotBlank(env.getStationId())) {
                    station = env.getStationId();
                }
                Pair<String, String> key =
                    Pair.of(StringUtils.defaultIfEmpty(env.getType(), "query"), StringUtils.defaultIfEmpty(env.getMsgType(), "0001"));
                Map<String, IotDevicePoint> pointMap = msgTypeGroup.computeIfAbsent(key, k -> new HashMap<>(16));
                pointMap.put(d.getChannelNum(), d);
            }
            station = StringUtils.defaultIfEmpty(station, DEFAULT_STATION);
            DEVICE_STATION_MAP.put(device.getInetAddr(), station);

            return sendMsg(device, msgTypeGroup, station);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private List<ResultMete> sendMsg(IotDevice device, Map<Pair<String, String>, Map<String, IotDevicePoint>> msgTypeGroup,
        String station) {
        List<ResultMete> resultMeteList = new ArrayList<>();
        msgTypeGroup.forEach((k, v) -> {
            try {
                EnvBase msg = new EnvBase().setType(k.getKey()).setMsgType(k.getValue()).setStationId(station);
                String msgXml = XmlUtil.createXmlString("root", msg);

                String response = TcpShortManager.INSTANSE.sendAndGet(msgXml, device);
                if (StringUtils.isEmpty(response)) {
                    LOGGER.error("采集数据失败，没有返回消息！！！");
                    return;
                }

                Document document = DocumentHelper.parseText(response);

                Element rootElt = document.getRootElement();
                EnvData dataResult = XmlUtil.parseObject(rootElt, EnvData.class);

                List<DataItem> itemList = dataResult.getData();

                for (DataItem item : itemList) {
                    IotDevicePoint point = v.get(item.getPIndex());
                    if (point == null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("采集通道没有添加，{}", item);
                        }
                        continue;
                    }
                    ResultMete mete = new ResultMete();
                    mete.setIotDeviceId(point.getIotDeviceId())
                        .setChannle(item.getPIndex())
                        .setValue(StringUtils.defaultIfEmpty(item.getTextValue(), item.getStatus()))
                        .setStatus(item.getStatus());
                    resultMeteList.add(mete);
                }

            } catch (Exception e) {
                LOGGER.error("报文发送失败, device: {}, {}", device, k, e);
            }
        });

        return resultMeteList;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    @Override
    public Result sendControl(IotDevice device, Map<String, Object> params) {
        Result result = new Result();
        try {
            int status = MapUtils.getIntValue(params, "deviceStatus");
            String deviceStatus = status == 2 ? "off" : "on";
            String deviceAttr = MapUtils.getString(params, "deviceAttr");

            EnvAction action = new EnvAction();
            EnvAction.Action act = new EnvAction.Action();
            act.setTextValue(deviceStatus);
            if (StringUtils.isNotEmpty(deviceAttr)) {
                act.addAttr("attr1", deviceAttr);
            }
            String stationId = DEVICE_STATION_MAP.getOrDefault(device.getInetAddr(), DEFAULT_STATION);
            action.setAction(act).setPIndex(MapUtils.getString(params, "deviceId")).setType("set").setMsgType("0003").setStationId(stationId);

            String msgXml = XmlUtil.createXmlString("root", action);

            String response = TcpShortManager.INSTANSE.sendAndGet(msgXml, device);
            if (StringUtils.isEmpty(response)) {
                LOGGER.error("发送控制指令失败，没有返回消息！！！");
                result.setMessage(ResultCodeEnum.CODE10001.getCode(), "设备响应超时");
            } else {
                Document document = DocumentHelper.parseText(response);

                Element rootElt = document.getRootElement();
                EnvResult dataResult = XmlUtil.parseObject(rootElt, EnvResult.class);
                if (!dataResult.isSuccess()) {
                    result.setMessage(ResultCodeEnum.CODE10001.getCode(), "控制设备失败");
                }
                result.setData(dataResult.getValue());
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.CODE10001.getCode(), "发送控制指令失败");
            throw new BusinessException("发送控制消息失败");
        }
        return result;
    }

}
