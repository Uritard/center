package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.iot.dao.TIotDeviceDataMapper;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.yjh.platform.module.iot.service.TIotDeviceDataService;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.entity.EnvDeviceStatus;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YIJIAHE
 * @description 针对表【t_iot_device_data(物联设备结果表)】的数据库操作Service实现
 * @createDate 2023-11-28 14:48:41
 */
@Service
@Slf4j
public class TIotDeviceDataServiceImpl extends ServiceImpl<TIotDeviceDataMapper, TIotDeviceData> implements TIotDeviceDataService {

    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Override
    public List<Map<String, Object>> selectIotData(Long upRegionId) {
        List<Long> regionList = tStdRegionDao.selectDownId(upRegionId);
        List<IotDeviceDataEx> tIotDeviceList = getBaseMapper().selectIotData(regionList);
        tIotDeviceList.forEach(data ->{
            if (data.getChannelNum() != null){
                String key = Constant.envKey+data.getIotDeviceId();
                Map<String,String> map = redisTemplate.opsForHash().entries(key);
                String value = map.get(data.getChannelNum());
                data.setValue(ValueUtil.getOrDefault(value,""));
                data.setCreateTime(DateTimeUtil.parse(map.get("time")));
                data.setState(ValueUtil.getOrDefault(map.get("state"),"0"));
                data.setType(ValueUtil.getOrDefault(map.get("type"),"0"));
            }

        });
        Map<String, List<IotDeviceDataEx>> resultMap = tIotDeviceList.stream().collect(Collectors.groupingBy(IotDeviceDataEx::getIotDeviceName));
        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultMap.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>(4);
            map.put("iotName", k);
            map.put("iotDeviceId", v.get(0).getIotDeviceId());
            map.put("controllable", v.get(0).getControllable());
            map.put("iotDeviceType", v.get(0).getIotDeviceType());
            if (v.get(0).getCreateTime() != null){
                map.put("time", DateTimeUtil.getDateTimeString(v.get(0).getCreateTime()));
            } else {
                map.put("time", "");
            }
            map.put("list", v);
            resultList.add(map);
        });
        return resultList;
    }

    @Override
    public List<List<String>> selectIotLine(Long iotDeviceId, String startTime, String endTime) {
        QueryWrapper<TIotDeviceData> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("iot_device_id", iotDeviceId);
        queryWrapper.between("create_time", startTime, endTime);
        List<TIotDeviceData> tIotDeviceList = this.list(queryWrapper);

        List<List<String>> line = new ArrayList<>();
        List<String> fir = new ArrayList<>();
        Map<Date, String[]> dateIdx = new LinkedHashMap<>(64);
        Map<KeyValue<String, String>, Integer> deviceMap = new HashMap<>(8);

        // 第一行 ["product","2023-07-25 11:36:55","2023-07-25 11:41:15","2023-07-25 15:59:46","2023-07-26 11:25:44","2023-07-26 16:34:32"]
        fir.add("product");
        line.add(fir);

        // 记录巡视设备编号
        int idx = 1;
        // 解析成时间对应数组，数组内对应不同巡视设备的巡视值
        for (TIotDeviceData vb : tIotDeviceList) {
            KeyValue<String, String> cruiseDevice = new LineKeyValue<>(String.valueOf(vb.getPointId()), vb.getPointName());
            Date time = vb.getCreateTime();
            // 获取巡视设备序号，若不存在，则表示需新增一个巡视设备编号
            Integer dex = deviceMap.get(cruiseDevice);
            if (dex == null) {
                dex = idx++;
                deviceMap.put(cruiseDevice, dex);
            }
            String[] dateLine = dateIdx.get(time);
            // 根据巡视设备编号判断数组是否需要进行扩容
            if (dateLine == null || dateLine.length < idx) {
                String[] newLine = new String[idx];
                if (dateLine != null) {
                    System.arraycopy(dateLine, 0, newLine, 0, dateLine.length);
                }
                dateLine = newLine;
                dateIdx.put(time, dateLine);
            }
            // 将巡视设备编号作为数组下标，存入巡视设备对应巡视值，对“,”分割的值取前面部分（当前特指红外测点）
            dateLine[dex] = StringUtils.substringBefore(vb.getValue(), ",");
        }

        // 初始化剩余行信息
        // ["大黑红外","-1","40.29,25.54","39.49,25.09","39.72,24.88","39.70,24.98","39.78,24.81"]
        // ["大白可见光","20","20",null,"20",null,"20"]
        List<String>[] dataLine = new List[deviceMap.size()];
        for (int i = 0; i < dataLine.length; i++) {
            dataLine[i] = new ArrayList<>();
        }

        // 将时间格式对应的测点转为需要的测点开头的数组
        dateIdx.forEach((dk, dv) -> {
            fir.add(DateTimeUtil.format(dk));
            int i = 0;
            for (Map.Entry<KeyValue<String, String>, Integer> entry : deviceMap.entrySet()) {
                Integer iv = entry.getValue();
                KeyValue<String, String> ik = entry.getKey();
                if (dataLine[i].isEmpty()) {
                    dataLine[i].add(ik.getValue());
                }
                // 取值时判断一下下标是否超过数组长度，若两种巡视设备值没有交叉，则会导致数组长度不满足要求
                dataLine[i].add(dv.length <= iv ? null : dv[iv]);
                i++;
            }

        });
        line.addAll(Arrays.asList(dataLine));
        return line;
    }

    @Override
    public Boolean insertEnvData(List<EnvDeviceStatus> envDeviceStatusList) {
        if (envDeviceStatusList.isEmpty()) {
            log.info("环境数据为空！");
            return false;
        }
        String robotCode = envDeviceStatusList.get(0).getRobotCode();
        String ip = "-1";
        //对于机器人的环控设备 环控设备的ip就是机器人的ip
        TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
        if (StringUtils.isNotEmpty((tRobotInfo.getRobotIp()))) {
            ip = tRobotInfo.getRobotIp();
        } else {
            log.info("根据robotCode:{} 没找对应机器人信息！",robotCode);
            return false;
        }
//        TStdRegion tStdRegion = tStdRegionDao.selectByRegionCode(robotCode);
//        if (StringUtils.isNotEmpty((tStdRegion.getRobotIp()))) {
//            ip = tRobotInfo.getRobotIp();
//        } else {
//            log.info("根据robotCode:{} 没找对应区域信息！",robotCode);
//        }
        List<IotDeviceDataEx> deviceDataList = getBaseMapper().selectInfoByIp(ip);
        List<IotDeviceDataEx> insertDeviceDataList = new ArrayList<>();
        envDeviceStatusList.forEach(envDeviceStatus -> {
            IotDeviceDataEx data = deviceDataList.
                            stream().
                            filter(deviceData -> Objects.equals(deviceData.getChannelNum(), envDeviceStatus.getDeviceId()))
                            .findFirst()
                            .orElse(null);
            if (data != null){
                data.setValue(envDeviceStatus.getDeviceValue());
                insertDeviceDataList.add(data);
            } else {
                return;
            }
            String key = Constant.envKey+data.getIotDeviceId();
            Map<String,String> map = new HashMap<>();
            map.put(data.getChannelNum(),data.getValue());
            map.put("time",DateTimeUtil.format(new Date()));
            map.put("state", ValueUtil.toString(envDeviceStatus.getStatus(),"0"));
            map.put("type", ValueUtil.getOrDefault(envDeviceStatus.getType(),"0"));
            //将本次数据放入redis
            redisTemplate.opsForHash().putAll(key,map);
        });
        if (!insertDeviceDataList.isEmpty()){
            iotDeviceDataUpload(insertDeviceDataList);
            List<TIotDeviceData> insertList = new ArrayList<>(insertDeviceDataList);
            return this.saveBatch(insertList);
        }
        return false;
    }

    @Override
    public Boolean addToRedis(List<IotDeviceDataEx> dataList) {
        try {
            Map<Long, List<IotDeviceDataEx>> groupData = dataList.stream().collect(Collectors.groupingBy(IotDeviceDataEx::getIotDeviceId));

            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    groupData.forEach((k, v) -> {
                        Map<String, String> valMap =
                            v.stream().collect(Collectors.toMap(IotDeviceDataEx::getChannelNum, TIotDeviceData::getValue));
                        valMap.put("time", DateTimeUtil.format(v.get(0).getCreateTime()));
                        String key = "EnvDevice:" + k;
                        operations.opsForHash().putAll(key, valMap);
                    });
                    return null;
                }
            });

            iotDeviceDataUpload(dataList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    private void iotDeviceDataUpload(List<IotDeviceDataEx> deviceDataList) {
        try {
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> itemList = new ArrayList<>();

            xmlBaseModel.setItems(itemList);
            xmlBaseModel.setType("iotDeviceData");
            xmlBaseModelList.add(xmlBaseModel);

            map.put("list", xmlBaseModelList);
            deviceDataList.forEach(tIotDeviceData -> {
                Map<String, Object> item = new HashMap<>();
                item.put("pointId", tIotDeviceData.getPointId());
                item.put("pointName", tIotDeviceData.getPointName());
                item.put("iotDeviceId", tIotDeviceData.getIotDeviceId());
                item.put("iotDeviceName", tIotDeviceData.getIotDeviceName());
                item.put("ip", tIotDeviceData.getIp());
                item.put("port", tIotDeviceData.getPort());
                item.put("address", tIotDeviceData.getAddress());
                item.put("value", tIotDeviceData.getValue());
                item.put("unit", tIotDeviceData.getUnit());
                item.put("upRegionId", tIotDeviceData.getUpRegionId());
                item.put("createTime", DateTimeUtil.format(new Date()));
                item.put("magnificationCoefficient", tIotDeviceData.getMagnificationCoefficient());
                item.put("channelNum", tIotDeviceData.getChannelNum());
                item.put("iotDeviceType", tIotDeviceData.getIotDeviceType());
                item.put("deviceId", tIotDeviceData.getDeviceId());
                item.put("upRegionName", tIotDeviceData.getUpRegionName());
                item.put("controllable", tIotDeviceData.getControllable());
                itemList.add(item);
            });
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            log.info("上报环控数据出错！", e);
        }
    }
}




