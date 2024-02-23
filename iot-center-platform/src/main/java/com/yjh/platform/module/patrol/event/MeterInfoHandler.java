package com.yjh.platform.module.patrol.event;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.dao.TMeterLogDao;
import com.yjh.platform.module.device.entity.TMeter;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author: lqh
 * @Date: 2023/11/07
 */
@Slf4j
@Component
public class MeterInfoHandler {
    /**
     * 正向有功总:2131.17        无功I总:16.07                        无功IV总:226.88
     * total_positive_power     total_positive_reactive_power       total_negative_positive_power
     * 电表的数据结果 对结果进行字符匹配
     */

    private final static String meterKey = "meter:";

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TMeterDao tMeterDao;
    @Autowired
    private TMeterLogDao tMeterLogDao;

    @EventListener
    public void handleResultEvent(InspectionResultEvent event) {
        // 巡视结果
        if (StringUtils.isNotEmpty(event.getResult())) {
            String key = meterKey + event.getTaskId() + ":" + event.getDeviceId();
            Map<String, String> meterInfo = new HashMap<>(4);
            String value = getNumeric(event.getResult());
            if (StringUtils.isEmpty(value)){
                log.info("结果为空：{}",value);
//                return;
            }
            if (event.getResult().contains("正向有功总")) {
                meterInfo.put("totalPositivePower", value);
            } else if (event.getResult().contains("无功I总")) {
                meterInfo.put("totalPositiveReactivePower", value);
            } else if (event.getResult().contains("无功IV总")) {
                meterInfo.put("totalNegativePositivePower", value);
            }
            redisTemplate.opsForHash().putAll(key, meterInfo);
            log.info("巡视：{}结束了", meterInfo);
        }
        Map<String, String> meterInfo = redisTemplate.opsForHash().entries(meterKey + event.getTaskId() + ":" + event.getDeviceId());
        if (meterInfo.size() == 3) {
            if (NumberUtils.toFloat(meterInfo.get("totalPositivePower")) <= 0){
                log.info("电表数值小于0！");
            }
            log.info("电表：{}结束了", event.getDeviceId());
            handleMeterEndEvent(event.getTaskId());
        }

    }
    public void handleMeterEndEvent(String taskId) {
        List<TMeter> meterList = tMeterDao.selectMeterByDeviceId();
        for (TMeter meter : meterList) {
            Map<String, String> meterInfo = redisTemplate.opsForHash().entries(meterKey + taskId + ":" + meter.getDeviceId());
            if (meterInfo.size() == 3) {
                if (NumberUtils.toFloat(meterInfo.get("totalPositivePower")) <= 0){
                    log.info("数值小于0！{}",meterInfo);
                    continue;
                }
                //正向有功与上一次的差值
                Float value = NumberUtils.toFloat(meterInfo.get("totalPositivePower")) - NumberUtils.toFloat(meter.getTotalPositivePower());
                if (value < 0 || value > 100){
                    value = 0f;
                }
                meter.setTotalPositivePowerDifferenceValue(String.valueOf(value));
                //正向无功与上一次的差值
                value = NumberUtils.toFloat(meterInfo.get("totalPositiveReactivePower")) - NumberUtils.toFloat(meter.getTotalPositiveReactivePower());
                if (value < 0 || value > 100){
                    value = 0f;
                }
                meter.setTotalPositiveReactivePowerDifferenceValue(String.valueOf(value));
                //反向无功与上一次的差值
                value = NumberUtils.toFloat(meterInfo.get("totalNegativePositivePower")) - NumberUtils.toFloat(meter.getTotalNegativePositivePower());
                if (value < 0 || value > 100){
                    value = 0f;
                }
                meter.setTotalNegativePositivePowerDifferenceValue(String.valueOf(value));

                meter.setTotalPositivePower(meterInfo.getOrDefault("totalPositivePower", "0"));
                meter.setTotalPositiveReactivePower(meterInfo.getOrDefault("totalPositiveReactivePower", "0"));
                meter.setTotalNegativePositivePower(meterInfo.getOrDefault("totalNegativePositivePower", "0"));
                meter.setCollectPowerTime(new Date());
                tMeterDao.updateByPrimaryKey(meter);
                tMeterLogDao.insert(meter);
                meterInfoUpload(meter);
                //处理完了  删除电表数据
                log.info("电表：{} 数据已处理完毕！删除redis数据:{}",meter.getName(),meterInfo);
                redisTemplate.delete(meterKey + taskId + ":" + meter.getDeviceId());
            }
        }
    }
    public void handleTaskEndEvent(TaskEndEvent event) {
        // 任务结束后 处理电表数据
        log.info("任务：{}结束了", event.getTaskId());
        List<TMeter> meterList = tMeterDao.selectMeterByDeviceId();
        for (TMeter meter : meterList) {
            Map<String, String> meterInfo = redisTemplate.opsForHash().entries(meterKey + event.getTaskId() + ":" + meter.getDeviceId());
            if (meterInfo.size() == 3) {
                if (NumberUtils.toFloat(meterInfo.get("totalPositivePower")) <= 0){
                    log.info("数值小于0！{}",meterInfo);
                    continue;
                }
                Float value = NumberUtils.toFloat(meterInfo.get("totalPositivePower")) - NumberUtils.toFloat(meter.getTotalPositivePower());
                if (value < 0){
                    value = 0f;
                }
                meter.setTotalPositivePowerDifferenceValue(String.valueOf(value));
                meter.setTotalPositivePower(meterInfo.getOrDefault("totalPositivePower", "0"));
                meter.setTotalPositiveReactivePower(meterInfo.getOrDefault("totalPositiveReactivePower", "0"));
                meter.setTotalNegativePositivePower(meterInfo.getOrDefault("totalNegativePositivePower", "0"));
                meter.setCollectPowerTime(new Date());
                tMeterDao.updateByPrimaryKey(meter);
                tMeterLogDao.insert(meter);
                meterInfoUpload(meter);
            }
        }
    }

    public static String getNumeric(String str) {
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if ((str.charAt(i) >= 48 && str.charAt(i) <= 57) || str.charAt(i) == '.') {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }

    public static void main(String[] args) {
        System.out.println(getNumeric("正向有功总:2131.17 "));
    }

    private void meterInfoUpload(TMeter meter){
        try {
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> itemList = new ArrayList<>();

            xmlBaseModel.setItems(itemList);
            xmlBaseModel.setType("meter");
            xmlBaseModelList.add(xmlBaseModel);

            map.put("list", xmlBaseModelList);
            Map<String, Object> item = new HashMap<>();
            item.put("name", meter.getName());
            item.put("ip", meter.getIp());
            item.put("port", meter.getPort());
            item.put("address", meter.getAddress());
            item.put("upRegionId", meter.getUpRegionId());
            item.put("totalPositivePower", meter.getTotalPositivePower());
            item.put("totalPositiveReactivePower", meter.getTotalPositiveReactivePower());
            item.put("totalNegativePositivePower", meter.getTotalNegativePositivePower());
            item.put("collectPowerTime", DateTimeUtil.format(meter.getCollectPowerTime()));
            item.put("totalPositivePowerDifferenceValue", meter.getTotalPositivePowerDifferenceValue());
            item.put("totalNegativePositivePowerDifferenceValue", meter.getTotalNegativePositivePowerDifferenceValue());
            item.put("totalPositiveReactivePowerDifferenceValue", meter.getTotalPositiveReactivePowerDifferenceValue());
            item.put("magnificationCoefficient", meter.getMagnificationCoefficient());
            itemList.add(item);

            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.info("上报电表数出错！",e);
        }
    }
}
