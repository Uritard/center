package com.yjh.accessrobot.netty.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author YC
 * @date 2020/12/18 15:38
 */
@lombok.extern.slf4j.Slf4j
public class IsWarnAfterCruiseThread implements Runnable{

    private Map<String,String> threadMap;

    private RedisTemplate redisTemplate;

    public IsWarnAfterCruiseThread(Map<String,String> threadMap, RedisTemplate redisTemplate){
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        try {
            log.info("Process Warn Starting >>>>>>> threadMap==="+threadMap);
            //查询该巡检点对应测点配置的告警阈值相关信息
            /*TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(afterManualReviewInfo.getInstanceId());
            log.info("tStdDeviceMete==="+tStdDevicemete);

            //该巡视点还在,能找到对应测点信息
            if (Objects.nonNull(tStdDevicemete)) {
                Map<String, Object> params = new HashMap<>();
                params.put("value", threadMap.get("value"));
                params.put("stdDeviceMeteName", tStdDevicemete.getMeteName());
                params.put("meteKind", tStdDevicemete.getMeteKind());
                params.put("alarmState", tStdDevicemete.getAlarmState());
                params.put("stateZero", tStdDevicemete.getStateZero());
                params.put("stateOne", tStdDevicemete.getStateOne());
                params.put("alarmLevel", tStdDevicemete.getAlarmLevel());
                params.put("highLimit1", tStdDevicemete.getHighLimit1());
                params.put("lowLimit1", tStdDevicemete.getLowLimit1());
                params.put("highLimit2", tStdDevicemete.getHighLimit2());
                params.put("lowLimit2", tStdDevicemete.getLowLimit2());
                params.put("highLimit3", tStdDevicemete.getHighLimit3());
                params.put("lowLimit3", tStdDevicemete.getLowLimit3());
                params.put("highLimit4", tStdDevicemete.getHighLimit4());
                params.put("lowLimit4", tStdDevicemete.getLowLimit4());
                log.info("params的值是===" + params);

                Result result = sendPostRequest(Constant.WARN_JUDGE, params);
                Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                log.info("object转map的东西===" + map);
                Boolean isWarN = (Boolean) map.get("isWarn");
                String outRange = null;
                if (Objects.nonNull(map.get("outRange"))) {
                    outRange = map.get("outRange").toString();
                }

                //组装告警基本信息
                TWarnInfo warnInfo = new TWarnInfo();
                warnInfo.setWarnTime(new Date());
                warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
                warnInfo.setCunstomId(tStdDevicemete.getCustomId());
                warnInfo.setInstanceId(afterManualReviewInfo.getInstanceId());
                warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
                warnInfo.setConfMode(275);//已核查
                warnInfo.setDealType(286);//属实
                warnInfo.setDealInfo("程序正常，告警属实");
                warnInfo.setDefectModel(405);//其他
                warnInfo.setAlarmSource(282);//主辅设备
                warnInfo.setImagePath(afterManualReviewInfo.getPicPath());
                warnInfo.setValue(threadMap.get("value"));
                warnInfo.setTaskId(threadMap.get("taskCode"));
                log.info("warnInfo=="+warnInfo);

                //判断该点是否产生告警以及告警信息
                if (Boolean.TRUE.equals(isWarN)){//触发告警
                    warnInfo.setWarnName(map.get("warnName").toString());
                    warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
                    warnInfo.setWarnContent(map.get("warnContent").toString());
                    warnInfo.setOutRange(outRange);
                    warnInfo.setDealTime(new Date());
                    warnInfo.setDealPersonId(userId);
                    log.info("要插库的告警数据是==="+warnInfo);
                    StaticContextAccessor.getBean(RobotService.class).insertWarn(warnInfo);
//                    sendWebSocket(warnInfo.getWarnId());
                    StaticContextAccessor.getBean(RobotService.class).updateIsWarn(cruiseManualReview.getCruiseDataId());
                }
            }*/
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Result sendPostRequest(String url, Map<String,Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }
}
