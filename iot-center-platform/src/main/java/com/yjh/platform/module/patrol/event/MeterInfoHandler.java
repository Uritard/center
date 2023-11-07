package com.yjh.platform.module.patrol.event;

import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.dao.TMeterLogDao;
import com.yjh.platform.module.device.entity.TMeter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, String> meterInfo = new HashMap<>(2);
            String value = getNumeric(event.getResult());
            ;
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
    }

    @EventListener
    public void handleTaskEndEvent(TaskEndEvent event) {
        // 任务结束后 处理电表数据
        log.info("任务：{}结束了", event.getTaskId());
        List<TMeter> meterList = tMeterDao.selectMeterByDeviceId();
        for (TMeter meter : meterList) {
            Map<String, String> meterInfo = redisTemplate.opsForHash().entries(meterKey + event.getTaskId() + ":" + meter.getDeviceId());
            if (meterInfo.size() == 3) {
                tMeterLogDao.insert(meter);
                meter.setTotalPositivePower(meterInfo.get("totalPositivePower"));
                meter.setTotalPositiveReactivePower(meterInfo.get("totalPositiveReactivePower"));
                meter.setTotalNegativePositivePower(meterInfo.get("totalNegativePositivePower"));
                tMeterDao.updateByPrimaryKey(meter);
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
}
