/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.alibaba.fastjson.JSON;
import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.common.result.BusinessException;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.common.utils.ByteUtil;
import com.yjh.accessmeter.common.utils.XmlUtil;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDeviceDataEx;
import com.yjh.accessmeter.module.device.entity.LinkageConfig;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.netty.DLT645Message;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import com.yjh.accessmeter.protocol.aigateway.info.ConfigResp;
import com.yjh.accessmeter.protocol.aigateway.info.Data;
import com.yjh.accessmeter.protocol.entity.EnvAction;
import com.yjh.accessmeter.protocol.impl.EnvTerminalProtocolImpl;
import com.yjh.accessmeter.protocol.impl.transport.TcpShortManager;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@DependsOn("springBeanUtils")
public class SensorCollectService {
    @Resource
    private ThreadPoolTaskScheduler taskScheduler;
    @Resource
    private ThreadPoolTaskExecutor asyncExecutor;
    @Resource
    private TIotDeviceDao iotDeviceDao;
    @Resource
    private PlatformProxy platformProxy;

    private static final Map<Integer, DataCollectTask> COLLECT_TASK_MAP = new HashMap<>(8);

    /**
     * 初始化连接
     */
    @PostConstruct
    public void initAllMeter() {
        try {
            List<IotDevice> iotDeviceList = iotDeviceDao.selectAll();
            Map<ProtocolEnum, List<IotDevice>> protocolSet =
                iotDeviceList.stream().collect(Collectors.groupingBy(i -> ProtocolEnum.getEnum(i.getProtocolModel())));

            protocolSet.forEach((k, v) -> {
                if (k.getType() == 0) {
                    log.info("协议类型无需主动建立连接: {}", k);
                } else {
                    ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(k);
                    if (sensorProtocol != null && !sensorProtocol.isInit()) {
                        sensorProtocol.init(v);
                    } else {
                        log.warn("协议未实现或已经初始化: {}", k);
                    }
                }
            });

            Map<Integer, List<IotDevice>> freSet = iotDeviceList.stream().filter(
                    i -> i.getCollectionFrequency() != null && i.getCollectionFrequency() > 0
                        && Optional.ofNullable(ProtocolEnum.getEnum(i.getProtocolModel())).map(ProtocolEnum::getType).orElse(0) != 0)
                .collect(Collectors.groupingBy(IotDevice::getCollectionFrequency));

            freSet.forEach((k, v) -> taskScheduler.scheduleAtFixedRate(
                COLLECT_TASK_MAP.compute(k, (k1, v1) -> new DataCollectTask(v, iotDeviceDao, asyncExecutor, platformProxy)),
                Instant.ofEpochMilli(System.currentTimeMillis() + 15000), Duration.ofMinutes(k)));
        } catch (Exception e) {
            log.error("初始化设备连接出错", e);
        }

    }

    @Scheduled(cron = "${accessmeter.collectdata.cron}")
    public void collectMeterDataTask() {
        log.info("开始采集所有电表电量");
        List<IotDevice> iotDeviceList = iotDeviceDao.selectAllMeter();
        DataCollectTask dataCollectTask = new DataCollectTask(iotDeviceList, iotDeviceDao, asyncExecutor, platformProxy);
        dataCollectTask.collectMeter(iotDeviceList);
    }

    /**
     * 新增设备接入，创建后只需接入一次
     */
    public boolean add(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.error("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }
        return add(device, true);
    }

    public boolean add(IotDevice device, boolean collectImmediate) {
        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask == null) {
            synchronized (this) {
                ProtocolEnum protocolEnum = ProtocolEnum.getEnum(device.getProtocolModel());
                if (protocolEnum.getType() == 0) {
                    log.info("协议类型无需主动建立连接: {}", protocolEnum);
                    return true;
                }
                ISensorProtocol sensorProtocol =
                    SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.getEnum(device.getProtocolModel()));
                if (sensorProtocol != null && !sensorProtocol.isInit()) {
                    sensorProtocol.init(Collections.singletonList(device));
                } else {
                    log.warn("协议未实现或已经初始化: {}", protocolEnum);
                }
                dataCollectTask = COLLECT_TASK_MAP.compute(device.getCollectionFrequency(), (k1, v1) -> {
                    if (v1 == null) {
                        return new DataCollectTask(Collections.singletonList(device), iotDeviceDao, asyncExecutor, platformProxy);
                    } else {
                        v1.addDevice(device, collectImmediate);
                        return v1;
                    }
                });
                taskScheduler.scheduleAtFixedRate(dataCollectTask,
                        Instant.ofEpochMilli(System.currentTimeMillis() + 15000), Duration.ofMinutes(device.getCollectionFrequency()));
            }
        } else {
            dataCollectTask.addDevice(device, collectImmediate);
        }
        log.info("新增数据采集 device:{}", device);

        return true;
    }

    public boolean delete(Long id) {
        IotDevice device = new IotDevice().setId(id);
        return delete(device);
    }

    public boolean delete(IotDevice device) {
        AtomicBoolean deleted = new AtomicBoolean(false);
        COLLECT_TASK_MAP.forEach((k, v) -> {
            if (v.containsDevice(device)) {
                v.removeDevice(device);
                deleted.set(true);
                log.info("删除了数据采集: {}, {}", k, device);
            }
        });
        log.info("已删除数据采集 device:{}", device);
        return deleted.get();
    }

    public boolean update(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.error("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }
        log.info("更新数据采集 device:{}", device);
        delete(device);
        return add(device, false);
    }

    public void collect(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.error("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }

        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask != null) {
            dataCollectTask.collectMeter(Collections.singletonList(device));
        }
    }

    public Result envDeviceControl(Map<String, Object> map) {
        long iotDeviceId = MapUtils.getLongValue(map, "iotDeviceId");
        IotDevice device = iotDeviceDao.selectByPrimaryKey(iotDeviceId);
        if (device == null) {
            log.error("没有查到设备配置 id:{}", iotDeviceId);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }
        ISensorProtocol sensorProtocol =
            SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.getEnum(device.getProtocolModel()));
        if (sensorProtocol == null || !sensorProtocol.isInit()) {
            log.error("协议不支持控制指令下发 device:{}", device);
            return new Result(ResultCodeEnum.CODE10009.getCode(), "协议不支持");
        }

        return sensorProtocol.sendControl(device, map);
    }

    public String getMeterCode(Long iotDeviceId, String channelNum, String value) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(iotDeviceId);
        DLT645Message dlt645Message = new DLT645Message();
        dlt645Message.setAddress(device.getAddress());
        dlt645Message.setValue(value);
        dlt645Message.setDataType(ByteUtil.HexString2Bytes(channelNum));
        if (ProtocolEnum.getEnum(device.getProtocolModel()) == ProtocolEnum.DLT645_97) {
            dlt645Message.setControlCode(Constant.CONTROLL_CODE_ANSWER);
        } else {
            dlt645Message.setControlCode(Constant.CONTROLL_CODE_ANSWER_2007);
        }
        //数据帧
        int decimalIndex = dlt645Message.getValue().indexOf('.');
        String integerPart = dlt645Message.getValue().substring(0, decimalIndex);
        String decimalPart = dlt645Message.getValue().substring(decimalIndex + 1);
        int integerLength = integerPart.length();
        String[] strings = new String[]{
                // 小数部分
                decimalPart,
                // 十位
                integerPart.substring(Math.max(0, integerLength - 2), Math.max(0, integerLength - 1)),
                // 个位
                integerPart.substring(Math.max(0, integerLength - 1), integerLength),
                // 千位
                integerPart.substring(Math.max(0, integerLength - 4), Math.max(0, integerLength - 3)),
                // 百位
                integerPart.substring(Math.max(0, integerLength - 3), Math.max(0, integerLength - 2)),
                // 十万位
                integerPart.substring(Math.max(0, integerLength - 5), Math.max(0, integerLength - 4)),
                // 万位
                integerPart.substring(Math.max(0, integerLength - 4), Math.max(0, integerLength - 43))
        };

        StringBuilder stringBuilder = new StringBuilder();
        for (String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                stringBuilder.append(s);
            } else {
                stringBuilder.append("0");
            }
        }
        byte[] valueArray = ByteUtil.HexString2Bytes(stringBuilder.toString());
        int valueNum = (dlt645Message.getDataType().length + valueArray.length);
        byte[] dataArray = new byte[valueNum];
        System.arraycopy(dlt645Message.getDataType(), 0, dataArray, 0, dlt645Message.getDataType().length);
        System.arraycopy(valueArray, 0, dataArray, dlt645Message.getDataType().length, valueArray.length);
        byte[] dataFarme = new byte[dataArray.length + 12];
        dataFarme[0] = Constant.START_OF_FRAME;
        byte[] address = ByteBufUtil.decodeHexDump(dlt645Message.getAddress());
        //地址域
        for (int i = 0; i < 6; i++) {
            dataFarme[1 + i] = address[5 - i];
        }
        dataFarme[7] = Constant.START_OF_FRAME;
        //控制码
        dataFarme[8] = dlt645Message.getControlCode();
        //数据长度
        dataFarme[9] = (byte) valueNum;
        //数据域
        for (int i = 0; i < dataArray.length; i++) {
            dataFarme[10 + i] = (byte) (dataArray[i] + Constant.DIFF_VALUE);
        }
        int cs = 0;
        //计算校验码
        for (int i = 0; i < dataFarme.length - 2; i++) {
            cs = cs + Byte.toUnsignedInt(dataFarme[i]) % 256;
        }
        dataFarme[dataFarme.length - 2] = (byte) cs;
        dataFarme[dataFarme.length - 1] = Constant.END_OF_FRAME;
        String hex = ByteBufUtil.hexDump(dataFarme);
        log.info("dlt645Message=> {} 的结果数据为 encode:{}", dlt645Message, hex);
        return hex;
    }

    public void asyncResultHandler(Map<String,Object> re){
        if (ProtocolEnum.AI_GATEWAY.getCode().equals(re.get("type"))){
            //智能网关结果处理
            AIGatewayResultHandler(re);
        }
    }

    public void linkageConfigSync(ConfigResp configResp,String ip){
        List<ConfigResp.LinkageListData> list = configResp.getLinkageList();
        List<LinkageConfig> linkageConfigList = new ArrayList<>();
        list.forEach(linkageListData -> {
            LinkageConfig config = new LinkageConfig();
            config.setType(0);
            config.setIp(ip);
            config.setData(JSON.toJSONString(linkageListData));
            linkageConfigList.add(config);
        });
        //先删除 再添加
        iotDeviceDao.deleteLinkageConfigByIp(ip);
        if (!linkageConfigList.isEmpty()){
            iotDeviceDao.banchInsertLinkageConfig(linkageConfigList);
        }
    }

    private void AIGatewayResultHandler(Map<String,Object> re){
        String ip = re.get("ip").toString();
        //根据ip查询设备
        List<IotDeviceDataEx> list = iotDeviceDao.selectByIp(ip);
        List<IotDeviceDataEx> resultList = new ArrayList<>();
        list.forEach(device -> {
            if (re.containsKey(device.getChannelNum())){
                device.setId(null);
                //建设备的时候 一对一 例如;灯=一个device+一个devicePoint
                device.setValue(re.get(device.getChannelNum()).toString());
                resultList.add(device);
            }
        });
        if (!resultList.isEmpty()) {
            try {
                iotDeviceDao.batchInsertData(resultList);
                platformProxy.uploadToRedis(resultList);
            } catch (Exception e) {
                log.error("设备采集数据入库失败: {}", JSON.toJSONString(resultList), e);
            }

        }
    }
}
