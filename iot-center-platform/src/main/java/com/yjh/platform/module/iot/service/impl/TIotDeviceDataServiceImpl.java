package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.iot.service.TIotDeviceDataService;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.yjh.platform.module.iot.dao.TIotDeviceDataMapper;
import com.yjh.platform.module.patrol.entity.LineKeyValue;
import com.yjh.platform.module.task.entity.BrokenLineInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    @Override
    public List<Map<String, Object>> selectIotData(Long upRegionId) {
        QueryWrapper<TIotDeviceData> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(upRegionId)) {
            queryWrapper.eq("up_region_id", upRegionId);
        }
        queryWrapper.inSql("(point_id, create_time)", "SELECT point_id, MAX(create_time) FROM t_iot_device_data GROUP BY point_id");
        List<TIotDeviceData> tIotDeviceList = this.list(queryWrapper);
        Map<String, List<TIotDeviceData>> resultMap = tIotDeviceList.stream().collect(Collectors.groupingBy(TIotDeviceData::getIotDeviceName));
        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultMap.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>(4);
            map.put("iotName", k);
            map.put("iotDeviceId", v.get(0).getIotDeviceId());
            map.put("time", DateTimeUtil.getDateTimeString(v.get(0).getCreateTime()));
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
}




