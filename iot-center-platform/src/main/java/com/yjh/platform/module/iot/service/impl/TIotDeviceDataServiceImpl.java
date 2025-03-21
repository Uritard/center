package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.yjh.commons.ValueUtil;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.dao.TMeterLogDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TMeter;
import com.yjh.platform.module.iot.dao.TIotDeviceDataMapper;
import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDeviceDataService;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.EnvDeviceStatus;
import com.yjh.platform.module.task.service.StatisticsService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private TMeterDao tMeterDao;
    @Autowired
    private TMeterLogDao tMeterLogDao;
    @Autowired
    private TIotDeviceService tIotDeviceService;
    @Autowired
    private TIotDevicePointService tIotDevicePointService;

    @Override
    public List<Map<String, Object>> selectIotData(Long upRegionId, Boolean meterFlag) {
        List<Long> regionList = tStdRegionDao.selectDownId(upRegionId);
        List<IotDeviceDataEx> tIotDeviceList = this.selectIotDataEx(regionList, meterFlag);
        Map<Long, List<IotDeviceDataEx>> resultMap = tIotDeviceList.stream().collect(Collectors.groupingBy(IotDeviceDataEx::getIotDeviceId));
        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultMap.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>(4);
            map.put("iotName", v.get(0).getIotDeviceName());
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
    public List<IotDeviceDataEx> selectIotDataEx(List<Long> regionList, Boolean meterFlag) {
        int iotMeterType = 840;
        List<IotDeviceDataEx> tIotDeviceList = meterFlag ? getBaseMapper().selectIotData(regionList, iotMeterType)
                : getBaseMapper().selectIotData(regionList, null);
        List<TIotDeviceData> tIotDeviceDataList = new ArrayList<>();
        if (meterFlag) {
            //筛选出电表的设备
            List<Long> pointList = tIotDeviceList.stream()
                    .map(IotDeviceDataEx::getPointId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(pointList)) {
                tIotDeviceDataList = getBaseMapper().selectMeterData(pointList);
            }
        }
        Map<Long, List<TIotDeviceData>> dataMap = tIotDeviceDataList.stream().collect(Collectors.groupingBy(TIotDeviceData::getPointId));

        tIotDeviceList.forEach(data -> {
            if (data.getChannelNum() != null) {
                String key = Constant.envKey + data.getIotDeviceId();
                Map<String, String> map = redisTemplate.opsForHash().entries(key);
                String value = map.get(data.getChannelNum());
                data.setValue(ValueUtil.getOrDefault(value,""));
                data.setCreateTime(DateTimeUtil.parse(map.get("time")));
                data.setState(ValueUtil.getOrDefault(map.get("state"),"0"));

                // 如果值是状态量，则将 0,1 状态量通过unit配置的参数转成汉字
                if (StringUtils.contains(data.getUnit(), ":")) {
                    Map<String, String> valueMap = Arrays.stream(StringUtils.split(data.getUnit(), ","))
                                                         .map(e -> StringUtils.split(e, ":"))
                                                         .collect(Collectors.toMap(s -> s[0], s -> s[1], (e1, e2) -> e1));
                    data.setValue(valueMap.getOrDefault(data.getValue(), ""));
                    data.setUnit("");
                }
                if (data.getIotDeviceType() == iotMeterType) {
                    List<TIotDeviceData> list = dataMap.get(data.getPointId());
                    if (CollectionUtils.isNotEmpty(list)) {
                        //数据库最早一条数据
                        TIotDeviceData currentData = list.get(0);
                        //前一天最早一条数据数据
                        Date lastDay = DateTimeUtil.lastDay(currentData.getLastTime());
                        TIotDeviceData lastData = list.stream().collect(Collectors.toMap(TIotDeviceData::getLastTime, Function.identity())).get(lastDay);
                        if (Objects.nonNull(lastData)) {
                            double num = Double.parseDouble(StringUtils.isNotBlank(currentData.getValue()) ? currentData.getValue() : "0")
                                    - Double.parseDouble(StringUtils.isNotBlank(lastData.getValue()) ? lastData.getValue() : "0");
                            //差值太大也是0
                            if (num < 0 || num > 10) {
                                data.setPowerValue("");
                            } else {
                                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                                float nc = Objects.nonNull(data.getMagnificationCoefficient()) ? data.getMagnificationCoefficient() : 1;
                                data.setPowerValue(decimalFormat.format(num * nc));
                            }
                        } else  {
                            data.setPowerValue("");
                        }
                    } else {
                        data.setPowerValue("");
                    }
                }
            }
        });
        return tIotDeviceList;
    }

    @Override
    public List<List<String>> selectIotLine(Long iotDeviceId, String startTime, String endTime, Boolean meterFlag, Boolean powerFlag) {
        List<TIotDeviceData> tIotDeviceList = new ArrayList<>();
        QueryWrapper<TIotDeviceData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iot_device_id", iotDeviceId);
        queryWrapper.between("create_time", startTime, endTime);
        queryWrapper.orderByAsc("create_time");
        if (meterFlag){
            queryWrapper.eq("iot_device_type", 840);
        }
        List<TIotDeviceData> tIotDeviceDataList = this.list(queryWrapper);

        //查询耗电量
        if (powerFlag) {
            tIotDeviceDataList.forEach(v -> v.setLastTime(DateTimeUtil.parseFormat(DateTimeUtil.getDateString(v.getCreateTime()), DateTimeUtil.getDatePattern())));
            Map<Date, List<TIotDeviceData>> timeGroupMap = tIotDeviceDataList.stream()
                    .collect(Collectors.groupingBy(TIotDeviceData::getLastTime, TreeMap::new, Collectors.toList()));
            timeGroupMap.forEach((time, l) -> {
                Map<Long, List<TIotDeviceData>> pointGroupMap = l.stream().collect(Collectors.groupingBy(TIotDeviceData::getPointId));
                pointGroupMap.forEach((key, value) ->{
                    TIotDeviceData data = value.get(0);
                    List<TIotDeviceData> lastList = timeGroupMap.get(DateTimeUtil.lastDay(time));
                    if (CollectionUtils.isNotEmpty(lastList)) {
                        List<TIotDeviceData> list =  lastList.stream().filter(o -> Objects.equals(o.getPointId(), key)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(list)) {
                            TIotDeviceData lastData = list.get(0);
                            if (StringUtils.isNotBlank(data.getValue()) && StringUtils.isNotBlank(lastData.getValue())) {
                                double num = Double.parseDouble(data.getValue()) - Double.parseDouble(lastData.getValue());
                                TIotDeviceData realData = new TIotDeviceData()
                                        .setPointId(data.getPointId()).setPointName(data.getPointName())
                                        .setCreateTime(data.getCreateTime()).setUnit(data.getUnit());
                                if (num < 0 || num > 10) {
                                    realData.setValue("0.00");
                                } else {
                                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                                    float nc = Objects.nonNull(data.getMagnificationCoefficient()) ? data.getMagnificationCoefficient() : 1;
                                    realData.setValue(decimalFormat.format(num * nc));
                                }
                                tIotDeviceList.add(realData);
                            }
                        }
                    }
                });
            });
        } else {
            tIotDeviceList.addAll(tIotDeviceDataList);
        }

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
            String lineName = lineName(vb.getPointName(), vb.getUnit(), vb.getValue());
            KeyValue<String, String> cruiseDevice = new LineKeyValue<>(String.valueOf(vb.getPointId()), lineName);
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
            dateLine[dex] = conventResult(StringUtils.substringBefore(vb.getValue(), ","));
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

    private String lineName(String pointName, String unit, String value) {
        if (StringUtils.isNotEmpty(unit)) {
            return pointName + "(" + unit +")";
        } else if (!StringUtils.equals(conventResult(value), value)) {
            return pointName + "(0:正常/开启,1:异常/关闭)";
        } else {
            return pointName;
        }
    }

    private String conventResult(String value) {
        switch (Optional.ofNullable(value).orElse("")) {
            case "正常":
            case "开启":
                return "0";
            case "异常":
            case "关闭":
                return "1";
            default:
                return value;
        }
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
                            v.stream().filter(i->StringUtils.isNotEmpty(i.getValue())).collect(Collectors.toMap(IotDeviceDataEx::getChannelNum, IotDeviceDataEx::getValue, (e1, e2)->e1));
                        IotDeviceDataEx ex = v.get(0);
                        valMap.put("time", DateTimeUtil.format(ex.getCreateTime()));
                        if (!CommonUtils.isEmptyOrNullstr(ex.getState())) {
                            int state = v.stream().mapToInt(e-> NumberUtils.toInt(e.getState())).max().orElse(0);
                            valMap.put("state", String.valueOf(state));
                        }
                        if (StringUtils.isNotEmpty(ex.getType())){
                            valMap.put("type", ex.getType());
                        }
                        String key = "EnvDevice:" + k;
                        operations.opsForHash().putAll(key, valMap);
                    });
                    return null;
                }
            });

        } catch (Exception e) {
            log.info("data:{}",dataList);
            log.error(e.getMessage(), e);
        }

        iotDeviceDataUpload(dataList);

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
                item.put("createTime", DateTimeUtil.format(tIotDeviceData.getCreateTime()));
                item.put("magnificationCoefficient", tIotDeviceData.getMagnificationCoefficient());
                item.put("channelNum", tIotDeviceData.getChannelNum());
                item.put("iotDeviceType", tIotDeviceData.getIotDeviceType());
                item.put("meterType", tIotDeviceData.getMeterType());
                item.put("upRegionName", tIotDeviceData.getUpRegionName());
                item.put("controllable", tIotDeviceData.getControllable());
                itemList.add(item);
            });
            Constant.otherServer(map, Constant.TCP_URL);
        } catch (Exception e) {
            log.info("上报环控数据出错！", e);
        }
    }

    @Override
    public void deviceConverted(){
        List<TMeter> meterList = tMeterDao.selectAll();
        List<TIotDevice> iotDevices = new ArrayList<>();
        meterList.forEach(tMeter -> {
            TIotDevice tIotDevice = new TIotDevice();
            tIotDevice.setDeviceName(tMeter.getName());
            tIotDevice.setIp(tMeter.getIp());
            tIotDevice.setPort(tMeter.getPort());
            tIotDevice.setAddress(tMeter.getAddress());
            tIotDevice.setIotDeviceType(840);
            tIotDevice.setProtocolModel(String.valueOf(tMeter.getType() == 2 ? 8 : "1997".equals(tMeter.getProtocol()) ? 4 : 5));
            tIotDevice.setMeterType(tMeter.getType() == 2 ? 1 : 0);
            tIotDevice.setUpRegionId(tMeter.getUpRegionId());
            tIotDevice.setUpRegionName(tStdRegionDao.selectByPrimaryId(tMeter.getUpRegionId()).getRegionName());
            tIotDevice.setCreateTime(tMeter.getCreateTime());
            tIotDevice.setCreatePerson("10001");
            tIotDevice.setUpdateTime(tMeter.getCreateTime());
            tIotDevice.setUpdatePerson("10001");
            tIotDevice.setMagnificationCoefficient(Float.valueOf(tMeter.getMagnificationCoefficient()));
            tIotDevice.setCollectionFrequency(0);
            iotDevices.add(tIotDevice);
        });
        tIotDeviceService.saveBatch(iotDevices);
        iotDevices.forEach(tIotDevice -> {
            List<TIotDevicePoint> iotDevicePoints  = new ArrayList<>();
            if ("4".equals(tIotDevice.getProtocolModel())){
                String[] dlt97 = new String[]{"1090","1091","2091"};
                for (String s : dlt97) {
                    TIotDevicePoint tIotDevicePoint = new TIotDevicePoint();
                    tIotDevicePoint.setIotDeviceId(tIotDevice.getId());
                    tIotDevicePoint.setIotDeviceName(tIotDevice.getDeviceName());
                    tIotDevicePoint.setChannelNum(s);
                    tIotDevicePoint.setUnit("1090".equals(s) ? "kwh" : "kvarh");
                    tIotDevicePoint.setPointName("1090".equals(s) ? "正向有功总" : "1091".equals(s) ? "正向无功总" : "反向无功总");
                    iotDevicePoints.add(tIotDevicePoint);
                }
            }else if ("5".equals(tIotDevice.getProtocolModel())){
                String[] dlt07 = new String[]{"00000100","00000500","00000800"};
                for (String s : dlt07) {
                    TIotDevicePoint tIotDevicePoint = new TIotDevicePoint();
                    tIotDevicePoint.setIotDeviceId(tIotDevice.getId());
                    tIotDevicePoint.setIotDeviceName(tIotDevice.getDeviceName());
                    tIotDevicePoint.setChannelNum(s);
                    tIotDevicePoint.setUnit("00000100".equals(s) ? "kwh" : "kvarh");
                    tIotDevicePoint.setPointName("00000100".equals(s) ? "正向有功总" : "00000500".equals(s) ? "无功I总" : "无功IV总");
                    iotDevicePoints.add(tIotDevicePoint);
                }
            }else {
                String[] task = new String[]{"正向有功总","无功I总","无功IV总"};
                for (String s : task) {
                    TIotDevicePoint tIotDevicePoint = new TIotDevicePoint();
                    tIotDevicePoint.setIotDeviceId(tIotDevice.getId());
                    tIotDevicePoint.setIotDeviceName(tIotDevice.getDeviceName());
                    tIotDevicePoint.setChannelNum("");
                    tIotDevicePoint.setUnit("正向有功总".equals(s) ? "kwh" : "kvarh");
                    tIotDevicePoint.setPointName(s);
                    iotDevicePoints.add(tIotDevicePoint);
                }
            }
            tIotDevicePointService.saveBatch(iotDevicePoints);
        });
    }

    @Override
    public void insertDataFromMeterLog() {
        List<TMeter> list = tMeterLogDao.selectAll();
        TMeter lastMeter = list.get(list.size() - 1);
        Date date = DateTimeUtil.parseFormat(DateTimeUtil.getDateString(lastMeter.getCreateTime()), DateTimeUtil.getDatePattern());
        List<IotDeviceDataEx> dataExList = getBaseMapper().selectIotData(new ArrayList<>(), 840);
        List<TIotDeviceData> dataList = new ArrayList<>();
        List<IotDeviceDataEx> redisList = new ArrayList<>();
        Map<String, List<TMeter>> addressMap = list.stream().collect(Collectors.groupingBy(TMeter::getAddress));
        addressMap.forEach((key, value) -> {
            List<IotDeviceDataEx> pointList = dataExList.stream().filter(t-> t.getAddress().equals(key)).collect(Collectors.toList());
            value.forEach(tMeter -> {
                Map<String, String> map = new HashMap<>(1);
                map.put("正向有功总", tMeter.getTotalPositivePower());
                if (tMeter.getType() == 2 || "2007".equals(tMeter.getProtocol())){
                    map.put("无功I总", tMeter.getTotalPositiveReactivePower());
                    map.put("无功IV总", tMeter.getTotalNegativePositivePower());
                }else {
                    map.put("正向无功总", tMeter.getTotalPositiveReactivePower());
                    map.put("反向无功总", tMeter.getTotalNegativePositivePower());
                }
                map.forEach((k,v) -> {
                    TIotDeviceData iotDeviceData = new TIotDeviceData();
                    IotDeviceDataEx deviceDataEx = pointList.stream().filter(f -> f.getPointName().equals(k)).collect(Collectors.toList()).get(0);
                    iotDeviceData.setIotDeviceType(deviceDataEx.getIotDeviceType());
                    iotDeviceData.setIotDeviceId(deviceDataEx.getIotDeviceId());
                    iotDeviceData.setIotDeviceName(deviceDataEx.getIotDeviceName());
                    iotDeviceData.setMagnificationCoefficient(deviceDataEx.getMagnificationCoefficient());
                    iotDeviceData.setPointId(deviceDataEx.getPointId());
                    iotDeviceData.setUnit(deviceDataEx.getUnit());
                    iotDeviceData.setUpRegionId(deviceDataEx.getUpRegionId());
                    iotDeviceData.setCreateTime(tMeter.getCreateTime());
                    iotDeviceData.setPointName(k);
                    iotDeviceData.setValue(v);
                    dataList.add(iotDeviceData);
                    if (tMeter.getCreateTime().getTime() >= date.getTime()){
                        deviceDataEx.setValue(v);
                        deviceDataEx.setCreateTime(tMeter.getCreateTime());
                        redisList.add(deviceDataEx);
                    }
                });
            });
        });
        this.saveBatch(dataList);
        addToRedis(redisList);
    }

    @Override
    public String exportMeterReport(Integer year, Integer month) {
        Map<String, Object> objectMap = StatisticsService.getDateByMonth(1, year, month);
        Date startTime = (Date)objectMap.get("startTime");
        Date endTime = (Date)objectMap.get("endTime");
        List<Date> dateList = DateTimeUtil.intervalAllTime(UPatrolTaskService.INTERVAL + "2,1", startTime, endTime);
        // 模板文件路径
        String templatePath = "/home/yjh_iot_center/iotCenter-web/dist/static/files/" + "template_" + dateList.size() + ".xlsx";
        String fileName = "meter_output_" + year + "_" + month + ".xlsx";
        // 输出文件路径
        String outputPath = SysParamConfig.getSysContent("tempReflect") + "/" + fileName;
        //当月数据
        List<TIotDeviceData> dataList = getBaseMapper().selectMasterMeterList(startTime, endTime);
        //次月一号数据
        Date nextEndTime = DateTimeUtil.getNextDate(endTime);
        Date nextStartTime = DateTimeUtil.parseFormat(DateTimeUtil.formatYMD(nextEndTime),DateTimeUtil.getDatePattern());
        List<TIotDeviceData> nextDataList = getBaseMapper().selectMasterMeterList(nextStartTime, nextEndTime);

        dateList.add(nextStartTime);
        dataList.addAll(nextDataList);
        Map<Date, List<TIotDeviceData>> timeGroupMap =
            dataList.stream().collect(Collectors.groupingBy(TIotDeviceData::getLastTime, TreeMap::new, Collectors.toList()));
        //填充模板
        fillTemplate(templatePath, outputPath, timeGroupMap, dateList, year, month);

        outputPath = SysParamConfig.getSysContent("meteModelPath") + "/" + fileName;
        return outputPath;
    }

    public static void fillTemplate(String templatePath, String outputPath, Map<Date, List<TIotDeviceData>> timeGroupMap,
        List<Date> dateList, Integer year, Integer month) {
        try (FileInputStream fis = new FileInputStream(templatePath); Workbook workbook = new XSSFWorkbook(fis)) {
            workbook.setSheetName(0, year + "年" + "-" + month + "月");
            Sheet sheet = workbook.getSheetAt(0);
            Row rowStation = sheet.getRow(3);
            Cell cell = rowStation.getCell(1);
            cell.setCellValue(SysParamConfig.getSysContent("stationName"));

            // 从第8行开始（索引为7）
            int rowIndex = 7;
            for (Date date : dateList) {
                List<TIotDeviceData> list = timeGroupMap.get(date);
                if (CollectionUtils.isEmpty(list)) {
                    // 间隔一行
                    rowIndex += 2;
                    continue;
                }
                Map<String, List<TIotDeviceData>> dailyDataMap =
                    list.stream().sorted(Comparator.comparing(TIotDeviceData::getIotDeviceName, Comparator.reverseOrder()))
                        .collect(Collectors.groupingBy(TIotDeviceData::getIotDeviceName));
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                int num = 1;
                for (Map.Entry<String, List<TIotDeviceData>> entry : dailyDataMap.entrySet()) {
                    int start = 5 + (num - 1) * 7;
                    sheet.getRow(5).getCell(num).setCellValue(entry.getKey());
                    Map<String, String> dataMap = entry.getValue().stream()
                        .collect(Collectors.toMap(TIotDeviceData::getPointName, TIotDeviceData::getValue, (e1, e2) -> e1));
                    setCellValue(row, num, MapUtils.getDoubleValue(dataMap, "正向有功总"));
                    setCellValue(row, start, MapUtils.getDoubleValue(dataMap, "无功I总"));
                    setCellValue(row, start + 1, MapUtils.getDoubleValue(dataMap, "无功II总"));
                    setCellValue(row, start + 2, MapUtils.getDoubleValue(dataMap, "无功III总"));
                    setCellValue(row, start + 3, MapUtils.getDoubleValue(dataMap, "无功IV总"));
                    setCellValue(row, start + 5, MapUtils.getDoubleValue(dataMap, "反向有功总"));
                    setCellValue(row, start + 6, MapUtils.getDoubleValue(dataMap, "正向有功总需量"));
                    sheet.getRow(5).getCell(4 * num + num).setCellValue(entry.getKey());
                    num++;
                }
                // 间隔一行
                rowIndex += 2;
            }
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            log.error("电表报表生成失败", e);
            throw new BusinessException("电表报表生成失败!");
        }
    }

    private static void setCellValue(Row row, int columnIndex, double value) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        cell.setCellValue(value);
    }
}




