package com.yjh.access.netty.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.znv.access104.common.Constant;
import com.znv.access104.kafka.KafkaSender;
import com.znv.access104.kafka.bean.KVResult;
import com.znv.access104.kafka.utils.KafkaConsts;
import com.znv.access104.module.entity.Device;
import com.znv.access104.module.entity.Devicemete;
import com.znv.access104.module.service.DeviceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019-12-14.
 */
@lombok.extern.slf4j.Slf4j
public class DataFormatThread  implements Runnable {

    private RedisTemplate redisTemplate;
    public RedisTemplate getRedisTemplate() { return redisTemplate; }
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private RedisTemplate redisTemplate2;
    public RedisTemplate getRedisTemplate2() {
        return redisTemplate2;
    }
    public void setRedisTemplate2(RedisTemplate redisTemplate2) {
        this.redisTemplate2 = redisTemplate2;
    }

    private DeviceService deviceService;
    public DeviceService getDeviceService() {
        return deviceService;
    }
    public void setDeviceService(DeviceService deviceService) { this.deviceService = deviceService; }

    StringBuffer sourcedata = new StringBuffer();
    public StringBuffer getSourcedata() { return sourcedata; }
    public void setSourcedata(StringBuffer sourcedata) { this.sourcedata = sourcedata; }

    private String topic;
    private JSONObject rep;
    public DataFormatThread(String topic, JSONObject rep){
        this.topic = topic;
        this.rep = rep;
    }
    @Override
    public void run() { sendToKafka(topic,rep); }

    public void sendToKafka(String topic, JSONObject rep) {
    KVResult result = null;
    Map<String,Object> sourceDataMap = new HashMap<String,Object>();
        if (KafkaConsts.KAFKA_104_TOPIC_DATA.equalsIgnoreCase(topic)) {
        if (KafkaConsts.KAFKA_104_TOPIC_YX.equalsIgnoreCase(rep.getString("TYPE"))) {
            result = convertPushYX(rep);
            if(result == null) { return; }
            String resultValue = result.getValue();
            JSONArray dataArray = JSON.parseObject(resultValue).getJSONArray("data");
            if (dataArray != null && dataArray.size() > 0) {
                log.info("pushKafkaSignal: " + resultValue);
                KafkaSender.sendMsg(topic, null, resultValue);
            }
            //writeForLog
            insertRedis(result);
            Map<String,Object> sourceDataMapTem = new HashMap<String,Object>();
            sourceDataMapTem.put("message",sourcedata);
            Object projectId=redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", rep.getString("strChannelID"))).get("projectId");
            sourceDataMapTem.put("projectId",projectId);
            sourceDataMap.put("pre",sourceDataMapTem);
            sourceDataMap.put("data",dataArray);
            try {
                redisTemplate2.opsForValue().set("iotcenter_count_accessdata:104_"+projectId+"_"+new Date().getTime(),projectId,60, TimeUnit.SECONDS);
                deviceService.insertStandardize(sourceDataMap);
            } catch (Exception e) { log.error("YXinsertIntoHistory: "+e.getMessage()); }
        } else if (KafkaConsts.KAFKA_104_TOPIC_YM.equalsIgnoreCase(rep.getString("TYPE"))) {
            result = convertPushYM(rep);
            if(result==null) {return;}
            String resultValue = result.getValue();
            JSONArray dataArray = JSON.parseObject(resultValue).getJSONArray("data");
            if (dataArray != null && dataArray.size() > 0) {
                //推送kafka遥脉消息:{"data":[],"type":"iotValue"}
                log.info("pushKafkaPulse: " + resultValue);
                KafkaSender.sendMsg(topic, null, resultValue);
            }
            //writeForLog
            insertRedis(result);
            Map<String,Object> sourceDataMapTem = new HashMap<String,Object>();
            sourceDataMapTem.put("message",sourcedata);
            Object projectId=redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", rep.getString("strChannelID"))).get("projectId");
            sourceDataMapTem.put("projectId",projectId);
            sourceDataMap.put("pre",sourceDataMapTem);
            sourceDataMap.put("data",dataArray);
            try {
                redisTemplate2.opsForValue().set("iotcenter_count_accessdata:104_"+projectId+"_"+new Date().getTime(),projectId,60, TimeUnit.SECONDS);
                deviceService.insertStandardize(sourceDataMap);
            } catch (Exception e) { log.error("YMinsertIntoHistory: "+e.getMessage()); }
        } else if (KafkaConsts.KAFKA_104_TOPIC_YC.equalsIgnoreCase(rep.getString("TYPE"))) {
            result = convertPushYC(rep);
            if(result==null) {return;}
            String resultValue = result.getValue();
            JSONArray dataArray = JSON.parseObject(resultValue).getJSONArray("data");
            if (dataArray != null && dataArray.size() > 0) {
                log.info("pushKafkaTelemete: " + resultValue);
                KafkaSender.sendMsg(topic, null, resultValue);
            }
            //writeForLog
            insertRedis(result);
            Map<String,Object> sourceDataMapTem = new HashMap<String,Object>();
            sourceDataMapTem.put("message",sourcedata);
            Object projectId=redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", rep.getString("strChannelID"))).get("projectId");
            sourceDataMapTem.put("projectId",projectId);
            sourceDataMap.put("pre",sourceDataMapTem);
            sourceDataMap.put("data",dataArray);
            try {
                redisTemplate2.opsForValue().set("iotcenter_count_accessdata:104_"+projectId+"_"+new Date().getTime(),projectId,60, TimeUnit.SECONDS);
                deviceService.insertStandardize(sourceDataMap);
            } catch (Exception e) { log.error("YCinsertIntoHistory: "+e.getMessage()); }
        }
    } else {
        log.info("104DatasFormIsIllegal");
        return;
    }
}

    private KVResult convertPushYX(JSONObject rep) {
        JSONArray dataArray = new JSONArray();
        String redisDeviceCode = "";
        JSONObject repJSONObjectInfo = rep.getJSONObject("Info");
        if (repJSONObjectInfo == null) {
            log.info("YXInfoIsNull");
            return null;
        }
        String strChannelID = rep.getString("strChannelID");
        if (strChannelID == null) {
            log.info("ClientIdIsNull");
            return null;
        }
        Map<Object, Object> gwDeviceMap = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strChannelID));
        String deviceCode = String.valueOf(gwDeviceMap.get("deviceCode"));
        String projectId = String.valueOf(gwDeviceMap.get("projectId"));

        if (deviceCode.equals(strChannelID)) {
            Map deviceMap;
            HashMap<Object, Map> devicesObjectMap = new HashMap<>();
            Set<String> devicesTreeSet = new TreeSet<>();
            //Set deviceObjectKeys = redisTemplate.keys("dmp_device_base:104_" + projectId + "_" + "*");
            Set deviceObjectKeys = redisScan("dmp_device_base:104_" + projectId + "_" + "*");
            List devicesObjectList = redisTemplate.executePipelined(
                    new SessionCallback<Object>() {
                        @Override
                        public <K, V> Object execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                            for (Object key : deviceObjectKeys) {
                                redisTemplate.opsForHash().entries(key);
                            }
                            return null;
                        }});
            int devicesObjectListLen = devicesObjectList.size();
            for (int i=0; i<devicesObjectListLen; i++) {
                deviceMap = (Map) devicesObjectList.get(i);
                String deviceKey = "dmp_device_base:104_" + projectId + "_" +deviceMap.get("deviceCode");
                if (deviceMap.get("upDeviceId").toString().equals(gwDeviceMap.get("deviceId"))) {
                    devicesTreeSet.add(deviceKey);
                    devicesObjectMap.put(deviceKey, deviceMap);
                }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dataTime = df.format(new Date());
            for (String accessMeteId : repJSONObjectInfo.keySet()) {
                for (Iterator<String> it = devicesTreeSet.iterator(); it.hasNext(); ) {
                    String deviceObject = it.next();
                    Map<Object, Object> redisDeviceMap = devicesObjectMap.get(deviceObject);
                    String deviceId = String.valueOf(redisDeviceMap.get("deviceId"));
                    String gatewayId = String.valueOf(redisDeviceMap.get("upDeviceId"));
                    String deviceKind = String.valueOf(redisDeviceMap.get("deviceKind"));
                    String deviceType = String.valueOf(redisDeviceMap.get("deviceType"));
                    String deviceName = String.valueOf(redisDeviceMap.get("deviceName"));
                    String accessCode = String.valueOf(redisDeviceMap.get("accessCode"));
                    String accessDeviceId = String.valueOf(redisDeviceMap.get("deviceCode"));
                    JSONObject dataJSONObjectTem = new JSONObject();
                    String meteInfo = String.valueOf(redisDeviceMap.get(accessMeteId));
                    String meteId = "";
                    String meteKind = "";
                    String meteType = "";
                    String meteName = "";
                    //Float remark = null;
                    String ERROR_VALUE = "#####";
                    if (meteInfo != null && !meteInfo.isEmpty() && !meteInfo.equalsIgnoreCase("null")) {
                        String splitChar = ",";
                        int index1 = meteInfo.indexOf(splitChar);
                        int index2 = meteInfo.indexOf(splitChar, index1 + 1);
                        int index3 = meteInfo.indexOf(splitChar, index2 + 1);
                        int index4 = meteInfo.indexOf(splitChar, index3 + 1);
                        if (index1 != -1) {
                            meteId = meteInfo.substring(0, index1);
                            if (index2 != -1) {
                                meteKind = meteInfo.substring(index1 + 1, index2);
                                if (index3 != -1) {
                                    meteType = meteInfo.substring(index2 + 1, index3);
                                    if (index4 != -1) {
                                        meteName = meteInfo.substring(index3 + 1, index4);
                                        //remark = Float.parseFloat(meteInfo.substring(index4 + 1));
                                    } else {
                                        log.error("index4 is -1,meteInfo:" + meteInfo);
                                        meteName = ERROR_VALUE;
                                    }
                                } else {
                                    log.error("index3 is -1,meteInfo:" + meteInfo);
                                    meteType = ERROR_VALUE;
                                    meteName = ERROR_VALUE;
                                }
                            } else {
                                log.error("index2 is -1,meteInfo:" + meteInfo);
                                meteKind = ERROR_VALUE;
                                meteType = ERROR_VALUE;
                                meteName = ERROR_VALUE;
                            }
                        } else {
                            log.error("Get meteInfo null, accessMeteId:" + accessMeteId);
                            meteId = ERROR_VALUE;
                            meteKind = ERROR_VALUE;
                            meteType = ERROR_VALUE;
                            meteName = ERROR_VALUE;
                        }
                        dataJSONObjectTem.put("meteId", meteId);
                        dataJSONObjectTem.put("meteKind", meteKind);
                        dataJSONObjectTem.put("meteType", meteType);
                        dataJSONObjectTem.put("meteName", meteName);
                        dataJSONObjectTem.put("accessMeteId", accessMeteId);
                        dataJSONObjectTem.put("deviceId", deviceId);
                        dataJSONObjectTem.put("gatewayId", gatewayId);
                        dataJSONObjectTem.put("deviceKind", deviceKind);
                        dataJSONObjectTem.put("deviceType", deviceType);
                        dataJSONObjectTem.put("deviceName", deviceName);
                        dataJSONObjectTem.put("accessCode", accessCode);
                        dataJSONObjectTem.put("accessGatewayId", strChannelID);
                        dataJSONObjectTem.put("accessDeviceId", accessDeviceId);
                        dataJSONObjectTem.put("accessServiceType", String.valueOf(gwDeviceMap.get("deviceType")));
                        dataJSONObjectTem.put("reportTime", dataTime);
                        dataJSONObjectTem.put("createTime",dataTime);

                        int meteValue = repJSONObjectInfo.getIntValue(accessMeteId);
                        dataJSONObjectTem.put("meteValue", meteValue);

                        if (dataJSONObjectTem.size() != 0) {
                            dataArray.add(dataJSONObjectTem);
                        }

                        String key = Constant.REDIS_DEVICE_IOCENTER.replace("ACCESSCODE_DEVICECODE", deviceId);
                        redisTemplate.opsForHash().put(key, "lastCommTime", dataTime);
                    }
                }
            }
        } else {
            log.info("noMatchClientId");
            return null;
        }
        if (dataArray.size() == 0) {
            log.info("signalIsNull");
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(KafkaConsts.JSON_DATA, dataArray);
        result.put(KafkaConsts.JSON_TYPE, KafkaConsts.KAFKA_IOT_TYPE_DATA);
        KVResult kvResult = new KVResult();
        kvResult.setKey(redisDeviceCode);
        kvResult.setValue(result.toJSONString());
        return kvResult;
    }

    private KVResult convertPushYM(JSONObject rep) {

        JSONArray dataArray = new JSONArray();
        String redisDeviceCode = "";
        JSONObject repJSONObjectInfo = rep.getJSONObject("Info");
        if (repJSONObjectInfo == null) {
            log.info("YMInfoIsNull");
            return null;
        }
        String strChannelID = rep.getString("strChannelID");
        if (strChannelID == null) {
            log.info("ClientIdIsNull");
            return null;
        }
        Map<Object, Object> gwDeviceMap = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strChannelID));
        String deviceCode = String.valueOf(gwDeviceMap.get("deviceCode"));
        String projectId = String.valueOf(gwDeviceMap.get("projectId"));

        if (deviceCode.equals(strChannelID)) {
            Map deviceMap;
            HashMap<Object, Map> devicesObjectMap = new HashMap<>();
            Set<String> devicesTreeSet = new TreeSet<>();
            //Set deviceObjectKeys = redisTemplate.keys("dmp_device_base:104_" + projectId + "_" + "*");
            Set deviceObjectKeys = redisScan("dmp_device_base:104_" + projectId + "_" + "*");
            List devicesObjectList = redisTemplate.executePipelined(
                    new SessionCallback<Object>() {
                        @Override
                        public <K, V> Object execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                            for (Object key : deviceObjectKeys) {
                                redisTemplate.opsForHash().entries(key);
                            }
                            return null;
                        }});
            int devicesObjectListLen = devicesObjectList.size();
            for (int i=0; i<devicesObjectListLen; i++) {
                deviceMap = (Map) devicesObjectList.get(i);
                String deviceKey = "dmp_device_base:104_" + projectId + "_" +deviceMap.get("deviceCode");
                if (deviceMap.get("upDeviceId").toString().equals(gwDeviceMap.get("deviceId"))) {
                    devicesTreeSet.add(deviceKey);
                    devicesObjectMap.put(deviceKey, deviceMap);
                }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dataTime = df.format(new Date());
            for (String accessMeteId : repJSONObjectInfo.keySet()) {
                for (Iterator<String> it = devicesTreeSet.iterator(); it.hasNext(); ) {
                    String deviceObject = it.next();
                    Map<Object, Object> redisDeviceMap = devicesObjectMap.get(deviceObject);
                    String deviceId = String.valueOf(redisDeviceMap.get("deviceId"));
                    String gatewayId = String.valueOf(redisDeviceMap.get("upDeviceId"));
                    String deviceKind = String.valueOf(redisDeviceMap.get("deviceKind"));
                    String deviceType = String.valueOf(redisDeviceMap.get("deviceType"));
                    String deviceName = String.valueOf(redisDeviceMap.get("deviceName"));
                    String accessCode = String.valueOf(redisDeviceMap.get("accessCode"));
                    String accessDeviceId = String.valueOf(redisDeviceMap.get("deviceCode"));
                    JSONObject dataJSONObjectTem = new JSONObject();
                    String meteInfo = String.valueOf(redisDeviceMap.get(accessMeteId));
                    String meteId = "";
                    String meteKind = "";
                    String meteType = "";
                    String meteName = "";
                    Float remark = null;
                    String ERROR_VALUE = "#####";
                    if (meteInfo != null && !meteInfo.isEmpty() && !meteInfo.equalsIgnoreCase("null")) {
                        String splitChar = ",";
                        int index1 = meteInfo.indexOf(splitChar);
                        int index2 = meteInfo.indexOf(splitChar, index1 + 1);
                        int index3 = meteInfo.indexOf(splitChar, index2 + 1);
                        int index4 = meteInfo.indexOf(splitChar, index3 + 1);
                        if (index1 != -1) {
                            meteId = meteInfo.substring(0, index1);
                            if (index2 != -1) {
                                meteKind = meteInfo.substring(index1 + 1, index2);
                                if (index3 != -1) {
                                    meteType = meteInfo.substring(index2 + 1, index3);
                                    if (index4 != -1) {
                                        meteName = meteInfo.substring(index3 + 1, index4);
                                        remark = Float.parseFloat(meteInfo.substring(index4 + 1));
                                    } else {
                                        log.error("index4 is -1,meteInfo:" + meteInfo);
                                        meteName = ERROR_VALUE;
                                    }
                                } else {
                                    log.error("index3 is -1,meteInfo:" + meteInfo);
                                    meteType = ERROR_VALUE;
                                    meteName = ERROR_VALUE;
                                }
                            } else {
                                log.error("index2 is -1,meteInfo:" + meteInfo);
                                meteKind = ERROR_VALUE;
                                meteType = ERROR_VALUE;
                                meteName = ERROR_VALUE;
                            }
                        } else {
                            log.error("Get meteInfo null, accessMeteId:" + accessMeteId);
                            meteId = ERROR_VALUE;
                            meteKind = ERROR_VALUE;
                            meteType = ERROR_VALUE;
                            meteName = ERROR_VALUE;
                        }
                        dataJSONObjectTem.put("meteId", meteId);
                        dataJSONObjectTem.put("meteKind", meteKind);
                        dataJSONObjectTem.put("meteType", meteType);
                        dataJSONObjectTem.put("meteName", meteName);
                        dataJSONObjectTem.put("accessMeteId", accessMeteId);
                        dataJSONObjectTem.put("deviceId", deviceId);
                        dataJSONObjectTem.put("gatewayId", gatewayId);
                        dataJSONObjectTem.put("deviceKind", deviceKind);
                        dataJSONObjectTem.put("deviceType", deviceType);
                        dataJSONObjectTem.put("deviceName", deviceName);
                        dataJSONObjectTem.put("accessCode", accessCode);
                        dataJSONObjectTem.put("accessGatewayId", strChannelID);
                        dataJSONObjectTem.put("accessDeviceId", accessDeviceId);
                        dataJSONObjectTem.put("accessServiceType", String.valueOf(gwDeviceMap.get("deviceType")));
                        dataJSONObjectTem.put("reportTime", dataTime);
                        dataJSONObjectTem.put("createTime",dataTime);

                        Float meteValue = repJSONObjectInfo.getIntValue(accessMeteId)*remark;
                        dataJSONObjectTem.put("meteValue", meteValue);
                        if (dataJSONObjectTem.size() != 0) {
                            dataArray.add(dataJSONObjectTem);
                        }

                        String key = Constant.REDIS_DEVICE_IOCENTER.replace("ACCESSCODE_DEVICECODE", deviceId);
                        redisTemplate.opsForHash().put(key, "lastCommTime", dataTime);
                    }
                }
            }
        } else {
            log.info("noMatchClientId");
            return null;
        }
        if (dataArray.size() == 0) {
            log.info("PulseIsNull");
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(KafkaConsts.JSON_DATA, dataArray);
        result.put(KafkaConsts.JSON_TYPE, KafkaConsts.KAFKA_IOT_TYPE_DATA);
        KVResult kvResult = new KVResult();
        kvResult.setKey(redisDeviceCode);
        kvResult.setValue(result.toJSONString());
        return kvResult;
    }

    private KVResult convertPushYC(JSONObject rep) {
        JSONArray dataArray = new JSONArray();
        String redisDeviceCode = "";
        JSONObject repJSONObjectInfo = rep.getJSONObject("Info");
        if (repJSONObjectInfo == null) {
            log.info("YCInfoIsNull");
            return null;
        }
        String strChannelID = rep.getString("strChannelID");
        if (strChannelID == null) {
            log.info("ClientIdIsNull");
            return null;
        }
        Map<Object, Object> gwDeviceMap = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strChannelID));
        String deviceCode = String.valueOf(gwDeviceMap.get("deviceCode"));
        String projectId = String.valueOf(gwDeviceMap.get("projectId"));

        if (deviceCode.equals(strChannelID)) {
            Map deviceMap;
            HashMap<Object, Map> devicesObjectMap = new HashMap<>();
            Set<String> devicesTreeSet = new TreeSet<>();
            //Set deviceObjectKeys = redisTemplate.keys("dmp_device_base:104_" + projectId + "_" + "*");
            Set deviceObjectKeys = redisScan("dmp_device_base:104_" + projectId + "_" + "*");
            List devicesObjectList = redisTemplate.executePipelined(
                    new SessionCallback<Object>() {
                        @Override
                        public <K, V> Object execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                            for (Object key : deviceObjectKeys) {
                                redisTemplate.opsForHash().entries(key);
                            }
                            return null;
                        }});
            int devicesObjectListLen = devicesObjectList.size();
            for (int i=0; i<devicesObjectListLen; i++) {
                deviceMap = (Map) devicesObjectList.get(i);
                String deviceKey = "dmp_device_base:104_" + projectId + "_" +deviceMap.get("deviceCode");
                if (deviceMap.get("upDeviceId").toString().equals(gwDeviceMap.get("deviceId"))) {
                    devicesTreeSet.add(deviceKey);
                    devicesObjectMap.put(deviceKey, deviceMap);
                }
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String DataTime = df.format(new Date());
            for (String accessMeteId : repJSONObjectInfo.keySet()) {
                for (Iterator<String> it = devicesTreeSet.iterator(); it.hasNext(); ) {
                    String deviceObject = it.next();
                    Map<Object, Object> redisDeviceMap = devicesObjectMap.get(deviceObject);
                    String deviceId = String.valueOf(redisDeviceMap.get("deviceId"));
                    String gatewayId = String.valueOf(redisDeviceMap.get("upDeviceId"));
                    String deviceKind = String.valueOf(redisDeviceMap.get("deviceKind"));
                    String deviceType = String.valueOf(redisDeviceMap.get("deviceType"));
                    String deviceName = String.valueOf(redisDeviceMap.get("deviceName"));
                    String accessCode = String.valueOf(redisDeviceMap.get("accessCode"));
                    String accessDeviceId = String.valueOf(redisDeviceMap.get("deviceCode"));
                    JSONObject dataJSONObjectTem = new JSONObject();
                    String meteInfo = String.valueOf(redisDeviceMap.get(accessMeteId));
                    String meteId = "";
                    String meteKind = "";
                    String meteType = "";
                    String meteName = "";
                    Float remark = null;
                    String ERROR_VALUE = "#####";
                    if (meteInfo != null && !meteInfo.isEmpty() && !meteInfo.equalsIgnoreCase("null")) {
                        String splitChar = ",";
                        int index1 = meteInfo.indexOf(splitChar);
                        int index2 = meteInfo.indexOf(splitChar, index1 + 1);
                        int index3 = meteInfo.indexOf(splitChar, index2 + 1);
                        int index4 = meteInfo.indexOf(splitChar, index3 + 1);
                        if (index1 != -1) {
                            meteId = meteInfo.substring(0, index1);
                            if (index2 != -1) {
                                meteKind = meteInfo.substring(index1 + 1, index2);
                                if (index3 != -1) {
                                    meteType = meteInfo.substring(index2 + 1, index3);
                                    if (index4 != -1) {
                                        meteName = meteInfo.substring(index3 + 1, index4);
                                        remark = Float.parseFloat(meteInfo.substring(index4 + 1));
                                    } else {
                                        log.error("index4 is -1,meteInfo:" + meteInfo);
                                        meteName = ERROR_VALUE;
                                    }
                                } else {
                                    log.error("index3 is -1,meteInfo:" + meteInfo);
                                    meteType = ERROR_VALUE;
                                    meteName = ERROR_VALUE;
                                }
                            } else {
                                log.error("index2 is -1,meteInfo:" + meteInfo);
                                meteKind = ERROR_VALUE;
                                meteType = ERROR_VALUE;
                                meteName = ERROR_VALUE;
                            }
                        } else {
                            log.error("Get meteInfo null, accessMeteId:" + accessMeteId);
                            meteId = ERROR_VALUE;
                            meteKind = ERROR_VALUE;
                            meteType = ERROR_VALUE;
                            meteName = ERROR_VALUE;
                        }
                        dataJSONObjectTem.put("meteId", meteId);
                        dataJSONObjectTem.put("meteKind", meteKind);
                        dataJSONObjectTem.put("meteType", meteType);
                        dataJSONObjectTem.put("meteName", meteName);
                        dataJSONObjectTem.put("accessMeteId", accessMeteId);
                        dataJSONObjectTem.put("deviceId", deviceId);
                        dataJSONObjectTem.put("gatewayId", gatewayId);
                        dataJSONObjectTem.put("deviceKind", deviceKind);
                        dataJSONObjectTem.put("deviceType", deviceType);
                        dataJSONObjectTem.put("deviceName", deviceName);
                        dataJSONObjectTem.put("accessCode", accessCode);
                        dataJSONObjectTem.put("accessGatewayId", strChannelID);
                        dataJSONObjectTem.put("accessDeviceId", accessDeviceId);
                        dataJSONObjectTem.put("accessServiceType", String.valueOf(gwDeviceMap.get("deviceType")));
                        dataJSONObjectTem.put("reportTime", DataTime);
                        dataJSONObjectTem.put("createTime",DataTime);

                        Float meteValue = repJSONObjectInfo.getFloatValue(accessMeteId)*remark;
                        dataJSONObjectTem.put("meteValue", meteValue);
                        if (dataJSONObjectTem.size() != 0) {
                            dataArray.add(dataJSONObjectTem);
                        }

                        String key = Constant.REDIS_DEVICE_IOCENTER.replace("ACCESSCODE_DEVICECODE", deviceId);
                        redisTemplate.opsForHash().put(key, "lastCommTime", DataTime);
                    }
                }
            }
        } else {
            log.info("noMatchClientId");
            return null;
        }
        if (dataArray.size() == 0) {
            log.info("TeleMeteIsNull");
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(KafkaConsts.JSON_DATA, dataArray);
        result.put(KafkaConsts.JSON_TYPE, KafkaConsts.KAFKA_IOT_TYPE_DATA);
        KVResult kvResult = new KVResult();
        kvResult.setKey(redisDeviceCode);
        kvResult.setValue(result.toJSONString());
        return kvResult;
    }

    private void insertRedis(KVResult result){
        String value = result.getValue();
        JSONObject valueObj = JSONObject.parseObject(value);
        JSONArray jsonArray=new JSONArray();
        jsonArray=valueObj.getJSONArray("data");
        List<Devicemete> meteList = new ArrayList<Devicemete>();
        Device device=new Device();
        for(Object obj:jsonArray){
            JSONObject deviceObj = new JSONObject();
            deviceObj = (JSONObject) obj;
            device=new Device();
            Devicemete deviceMete = new Devicemete();
            device.setDeviceId(deviceObj.getString("deviceId"));
            deviceMete.setMeteId(deviceObj.getString("meteId"));
            deviceMete.setMeteValue(deviceObj.getString("meteValue"));
            deviceMete.setReportTime(deviceObj.getString("reportTime"));
            meteList.add(deviceMete);
        }
        setRedisByMete(device,meteList);
    }

    private void setRedisByMete(Device device, List<Devicemete> meteList) {
        Map<String, Object> deviceMap = new HashMap<>();
        if(meteList!=null&&meteList.size()!=0){
            for (Devicemete mete : meteList) {
                Integer accuracy=deviceService.selectAccuracy(device.getDeviceId(),mete.getMeteId());
                if(mete.getMeteValue()!=null&&mete.getMeteValue().indexOf("E")>0){
                    BigDecimal meteValue=new BigDecimal(mete.getMeteValue());
                    if(accuracy!=null&&meteValue.scale()>accuracy){
                        meteValue=meteValue.setScale(accuracy,BigDecimal.ROUND_HALF_UP);
                    }
                    deviceMap.put(mete.getMeteId(), mete.getReportTime()+"|"+meteValue);
                }else{
                    String meteValue="";
                    if(accuracy!=null&&getNumberDecimalDigits(mete.getMeteValue())>accuracy){
                        meteValue=String .format("%."+accuracy+"f",Double.parseDouble(mete.getMeteValue()));
                    }else{
                        meteValue=mete.getMeteValue();
                    }
                    deviceMap.put(mete.getMeteId(), mete.getReportTime()+"|"+meteValue);
                }
            }
        }
        String key = Constant.REDIS_DEVICE_REDIS.replace("CODE", device.getDeviceId());

        Boolean flag=redisTemplate2.hasKey(key);
        if(flag){
            redisTemplate2.opsForHash().putAll(key, deviceMap);
        }else{
            for(Devicemete mete : meteList){
                redisTemplate2.opsForHash().delete(key, mete.getMeteId());
                redisTemplate2.opsForHash().put(key, mete.getMeteId(),mete.getReportTime()+"|"+mete.getMeteValue());
            }
        }

    }

    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }

    private static int getNumberDecimalDigits(String moneyStr) {
        String[] num = moneyStr.split("\\.");
        if (num.length == 2) {
            for (;;){
                if (num[1].endsWith("0")) {
                    num[1] = num[1].substring(0, num[1].length() - 1);
                }else {
                    break;
                }
            }
            return num[1].length();
        }else {
            return 0;
        }
    }
}