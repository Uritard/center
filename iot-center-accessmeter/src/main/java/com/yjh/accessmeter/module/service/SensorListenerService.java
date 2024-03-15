/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.common.utils.XmlUtil;
import com.yjh.accessmeter.logs.SpringBeanUtils;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.protocol.entity.EnvBase;
import com.yjh.accessmeter.protocol.entity.EnvData;
import com.yjh.accessmeter.protocol.entity.EnvResult;
import com.yjh.accessmeter.protocol.entity.EnvWarn;
import com.yjh.accessmeter.protocol.impl.transport.ServerListener;
import com.yjh.accessmeter.protocol.impl.transport.TcpServerManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/20
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class SensorListenerService {
    @Resource
    private ThreadPoolTaskScheduler taskScheduler;
    @Resource
    private ThreadPoolTaskExecutor asyncExecutor;
    @Resource
    private TIotDeviceDao iotDeviceDao;
    @Resource
    private PlatformProxy platformProxy;
    @Resource
    private SystemManagerService systemManagerService;

    public ChannelFuture initServerBind() {
        String[] strs = StringUtils.split(Constant.meterWarnAddr, ":");
        if (ArrayUtils.getLength(strs) < 2) {
            log.error("告警端口绑定失败：{}", Constant.meterWarnAddr);
        } else {
            String ip = "127.0.0.1".equals(strs[0]) ? null : strs[0];
            int port = NumberUtils.toInt(strs[1]);
            return TcpServerManager.INSTANSE.bind(ip, port, new EnvWarnListener());
        }
        return null;
    }

    private class EnvWarnListener implements ServerListener {

        @Override
        public String dispatch(ChannelHandlerContext ctx, String message) {
            EnvResult result = new EnvResult();
            try {
                Document document = DocumentHelper.parseText(message);

                Element rootElt = document.getRootElement();
                EnvBase envBase = XmlUtil.parseObject(rootElt, EnvBase.class);
                List<EnvWarn> warnList = XmlUtil.parseArray(rootElt, EnvWarn.class);
                result.setType(envBase.getType()).setMsgType(envBase.getMsgType()).setStationId(envBase.getStationId());

                InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();

                String ip = inetSocketAddress.getHostName();

                for (EnvWarn warn : warnList) {
                    Map<String, String> envWarnMap = new HashMap<>(8);

                    String pindex = warn.getPIndex();
                    String now = DateUtil.now();
                    envWarnMap.put("deviceIp", ip);
                    envWarnMap.put("time", now);
                    String warnType = warn.getWarningType();
                    String value = systemManagerService.getWarnName(warnType);
                    envWarnMap.put("value", value);
                    envWarnMap.put("valueType", warnType);
                    envWarnMap.put("type", warnType);
                    envWarnMap.put("warnLevel", warn.getWaringLevel());
                    envWarnMap.put("sn", warn.getMsgId());
                    envWarnMap.put("device_num", pindex);
                    envWarnMap.put("type_device_num", pindex);
                    envWarnMap.put("alarmTime", now);

                    log.info("收到告警信息: {}", JSON.toJSONString(envWarnMap));
                    platformProxy.uploadIotWarn(envWarnMap);
                }

                result.setValue("1");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                result.setValue("2");
            }
            try {
                return XmlUtil.createXmlString("root", result);
            } catch (Exception e) {
                log.error("组织报文失败", e);
                return null;
            }
        }
    }
}
