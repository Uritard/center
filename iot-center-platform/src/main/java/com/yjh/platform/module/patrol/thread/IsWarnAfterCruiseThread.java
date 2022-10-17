package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.GetSpringUtil;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.ftpsservice;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import com.yjh.platform.module.task.service.TWarnInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2020/12/18 15:38
 * 机器人的巡视结果再做告警判断线程
 */
@lombok.extern.slf4j.Slf4j
public class IsWarnAfterCruiseThread implements Runnable {

    private Map<String, String> threadMap;
    private RedisTemplate redisTemplate;
    private UpFtpsConfig upFtpsConfig;
    private UPatrolTaskService uPatrolTaskService;

    public IsWarnAfterCruiseThread(Map<String, String> threadMap, RedisTemplate redisTemplate) {
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.upFtpsConfig = StaticContextAccessor.getBean(UpFtpsConfig.class);
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始判断巡视结果是否告警 >>>>>>> threadMap==={}", threadMap);
            String taskId = threadMap.get("taskCode");
            String robotCode = threadMap.get("robotCode");

            String robotTaskId = taskId;
            /*if ("true".equals(redisTemplate.opsForValue().get("RobotTask.taskToRobot"))) {
                robotTaskId = StaticContextAccessor.getBean(RobotService.class).selectTaskId(taskId);
            }*/

//            UPatrolTask uPatrolTask = uPatrolTaskService.selectUPatrolTask(taskId);
//            log.info("taskId是: {}的任务数据uPatrolTask是: {}", taskId, uPatrolTask);

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + robotTaskId);
//            if (Objects.isNull(tCruiseTask.getTaskType())) {
                for (String key : robotInfoKeys) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (Objects.equals(robotCode, redisInfoMap.get("robotCode"))
                            && Objects.equals(robotTaskId, redisInfoMap.get("taskId"))
                            && Objects.equals(threadMap.get("deviceId"), redisInfoMap.get("inspectionCode"))) {
                        Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                        TStdDeviceMete tStdDevicemete = uPatrolTaskService.selectDeviceMeteInfo(instanceId);

                        if (Objects.isNull(tStdDevicemete)){
                            return;
                        }
                        //该巡视点还在,能找到对应
                        Map<String, Object> params = new HashMap<>(16);
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

                        Result result = StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(Constant.WARN_JUDGE, Result.class, params);
                        Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                        log.info("object转map的东西==={}", map);
                        Boolean isWarN = (Boolean) map.get("isWarn");

                        if (Boolean.FALSE.equals(isWarN)) {
                            return;
                        }
                        alarmStoreAndHandler(map, taskId, tStdDevicemete, instanceId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 存储告警信息及其他处理
     *
     * @param map            根据告警规则判断的结果
     * @param taskId         任务id
     * @param tStdDevicemete 测点信息
     * @param instanceId     巡视点id
     */
    private void alarmStoreAndHandler(Map<String, Object> map, String taskId, TStdDeviceMete tStdDevicemete, Long instanceId) {
        TWarnInfo warnInfo = new TWarnInfo();

        try {
            warnInfo.setWarnTime(DateTimeUtil.parse(threadMap.get("time")));
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(instanceId);
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(276);
            Integer warnFlag = Integer.valueOf(uPatrolTaskService.selectDictCodeByNote("其他", "defect_model"));
            warnInfo.setDefectModel(warnFlag);
            warnInfo.setAlarmSource(282);
            warnInfo.setImagePath(threadMap.get("relativePath"));
            warnInfo.setValue(threadMap.get("value"));
            warnInfo.setTaskId(taskId);
            String robotId = String.valueOf(uPatrolTaskService.selectRobotInfoByCode(threadMap.get("robotCode")).getRobotId());
            warnInfo.setDeviceCode(robotId);
            map.put("absolutePath",threadMap.get("absolutePath"));
            map.put("deviceName",threadMap.get("deviceName"));
            warnInfo.setWarnName(String.valueOf(map.get("warnName")));
            warnInfo.setWarnLevel(Integer.valueOf(String.valueOf(map.get("warnLevel"))));
            warnInfo.setWarnContent(String.valueOf(map.get("warnContent")));
            String outRange = Objects.nonNull(map.get("outRange"))? String.valueOf(map.get("outRange")) : null;
            warnInfo.setOutRange(outRange);
            log.info("要插库的告警数据是===" + warnInfo);

            // webSocket通知前端刷新告警统计数量
            Map<String, String> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "newAlarm");
            jasonMaps.put("alarmName", warnInfo.getWarnName());
            jasonMaps.put("alarmTime", String.valueOf(warnInfo.getWarnTime()));
            jasonMaps.put("alarmContent", warnInfo.getWarnContent());
            String jsons = JSON.toJSONString(jasonMaps);
            log.info("告警生成-前端推送：" + jsons);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);

            StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);
            log.info("warnId===" + warnInfo.getWarnId());

            // 将告警信息放入redis
            Map<String, String> warnMap = putWarnToRedis(taskId, warnInfo);

            // 告警推送
            alarmPopUp(tStdDevicemete, warnInfo);

            // 将产生的告警上送至上级系统
            alarmToUpSystem(warnInfo, tStdDevicemete);

            // 将产生的告警上送到算法管理平台
            alarmToAmPlatform(map, warnMap);

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断该测点是否设置了告警推送
     * 若是,则将配置的告警信息组成告警弹框所需内容推给前端
     *
     * @param tStdDevicemete  测点信息
     * @param warnInfo        告警信息
     */
    private void alarmPopUp(TStdDeviceMete tStdDevicemete, TWarnInfo warnInfo){
        Map<String, String> currentWarnInfo = new HashMap<>(16);
        try {
            currentWarnInfo.put("warnId", String.valueOf(warnInfo.getWarnId()));
            currentWarnInfo.put("defectModel", "450");
            currentWarnInfo.put("isPop", "false");

            boolean isSet = StringUtils.isNotEmpty(tStdDevicemete.getAlarmNote()) && StringUtils.equals("1", tStdDevicemete.getAlarmNote());
            boolean reachAlarmLevel = Objects.nonNull(tStdDevicemete.getAlarmLevel()) &&
                    (warnInfo.getWarnLevel().compareTo(tStdDevicemete.getAlarmLevel()) == 0 || warnInfo.getWarnLevel() > tStdDevicemete.getAlarmLevel());
            if (Boolean.TRUE.equals(isSet) && Boolean.TRUE.equals(reachAlarmLevel)) {
                // webSocket通知前端调用查询告警弹框的接口
                Map<String, String> jasonMaps2 = new HashMap<>(16);
                jasonMaps2.put("type", "alarmPopUp");
                jasonMaps2.put("warnId", String.valueOf(warnInfo.getWarnId()));
                jasonMaps2.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
                String json = JSON.toJSONString(jasonMaps2);
                log.info("告警弹窗-前端推送：" + json);
                currentWarnInfo.put("isPop", "true");
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps2);
            }
            redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将产生的告警上送到算法管理平台
     *
     * @param map 告警信息
     * @param warnMap 告警信息
     */
    private void alarmToAmPlatform (Map<String, Object> map, Map<String, String> warnMap) {
        try{
            log.info("开始与算法管理平台交互");
            ftpsservice ftpsservice = GetSpringUtil.getBean("ftpsservice");
            String flag = ftpsservice.getFlag();
            if ("1".equals(flag)) {
                Alarm alarm = new Alarm();
                alarm.setBay_name(warnMap.get(""));
                alarm.setTime(warnMap.get("warnTime"));
                AlarmService alarmService = GetSpringUtil.getBean("alarmService");
                //获取原始路径
                String year = Integer.toString(LocalDate.now().getYear());
                String month = Integer.toString(LocalDate.now().getMonthValue());
                String taskidbak = warnMap.get("taskId");
                String instanceIdbak = warnMap.get("instanceId");
                Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskidbak + ":" + instanceIdbak);
                String devicename= map.get("deviceName").toString();
                alarm.setDevice_name(devicename);
                String origpicpath = cruiseResult2.get("origpic").toString();
                String[] str2 = origpicpath.split("/");
                String origpcimagename = str2[str2.length - 1];
                String remoteorigfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + origpcimagename;
                //结果文件
                String resultImagebak = map.get("absolutePath").toString();
                String[] str3 = resultImagebak.split("/");
                //获取结果图名称，然后拼接远程文件全路径
                String resultimagename = str3[str3.length - 1];
                String remoteresultfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + resultimagename;
                //原图  算法管理平台对应的原始文件路径
                alarm.setPic_raw(remoteresultfilepath);
                //机器人分析结果图 算法管理平台对应的原始文件路径
                alarm.setPic_different(remoteorigfilepath);
                //原始图片上传
                ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);
                //判别结果图片
                ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath);

                if( !ftpsservice.fileExits(remoteorigfilepath)){
                    //原始图片上传
                    ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);
                }
                if( !ftpsservice.fileExits(remoteorigfilepath)){
                    //判别结果图片
                    ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath);
                }
                alarmService.PushMsg(alarm);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将告警信息放入redis
     *
     * @param taskId 任务id
     * @param warnInfo 告警信息
     * @return  Map<String, String>
     */
    private Map<String, String> putWarnToRedis(String taskId, TWarnInfo warnInfo) {
        String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        try {
            warnMap.put("deviceId", String.valueOf(warnInfo.getDeviceId()));
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", String.valueOf(warnInfo.getInstanceId()));
            warnMap.put("stdMeteId", String.valueOf(warnInfo.getStdMeteId()));
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("imagePath", warnInfo.getImagePath());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", String.valueOf(warnInfo.getAlarmSource()));
            warnMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            warnMap.put("warnLevel", String.valueOf(warnInfo.getWarnLevel()));
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", new SimpleDateFormat().format(warnInfo.getWarnTime()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
            warnMap.put("outRange", Objects.nonNull(warnInfo.getOutRange()) ? warnInfo.getOutRange() : "");

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        log.info("warnMap===" + warnMap);
        redisTemplate.opsForHash().putAll(warnName, warnMap);
        return warnMap;
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param tStdDevicemete 测点信息
     * @return void
     */
    private void alarmToUpSystem(TWarnInfo warnInfo, TStdDeviceMete tStdDevicemete){
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        try {
            xmlBaseModel.setType("62");
            xmlItem.put("patroldevice_code", threadMap.getOrDefault("robotCode", ""));
            xmlItem.put("patroldevice_name", threadMap.getOrDefault("robotCode", ""));
            xmlItem.put("task_name", Optional.ofNullable(threadMap.get("taskName")).orElse(""));
            xmlItem.put("task_code", Optional.ofNullable(warnInfo.getTaskId()).orElse(""));
            xmlItem.put("device_name", Optional.ofNullable(threadMap.get("deviceName")).orElse(""));
            xmlItem.put("device_id", Optional.ofNullable(threadMap.get("deviceId")).orElse(""));
            switch (warnInfo.getWarnLevel()){
                case 130:
                    xmlItem.put("alarm_level", "1");
                    break;
                case 131:
                    xmlItem.put("alarm_level", "2");
                    break;
                case 132:
                    xmlItem.put("alarm_level", "3");
                    break;
                case 133:
                    xmlItem.put("alarm_level", "4");
                    break;
                default:
                    break;
            }
            // 存在可见光的表计和红外测温和刀闸
            String recognitionType = threadMap.get("recognitionType");
            switch (recognitionType){
                case "1":
                    xmlItem.put("alarm_type", "7"); break;
                case "2":
                    xmlItem.put("alarm_type", "10"); break;
                case "3":
                    xmlItem.put("alarm_type", "6"); break;
                case "4":
                    xmlItem.put("alarm_type", "1"); break;
                default: break;
            }
            xmlItem.put("recognition_type", threadMap.get("recognitionType"));
            String fileType = threadMap.get("fileType");
            String fileNamePath = "";
            switch (fileType){
                case "1": fileNamePath = "/FIR/"; break;
                case "2": fileNamePath = "/CCD/"; break;
                case "3": fileNamePath = "/Audio/"; break;
                default: break;
            }
            xmlItem.put("file_type", fileType);

            String imgPath = warnInfo.getImagePath().replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content")));
            String stationCode = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:stationCode", "content"));

            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR或Audio/设备点位ID_编码_时间.jpg
            String timeFormat = DateTimeUtil.format3(DateTimeUtil.parse(threadMap.get("time")));
            String tagPath = stationCode + "/" + DateTimeUtil.format2(DateTimeUtil.parse(threadMap.get("time"))) + "/" + warnInfo.getTaskId() + fileNamePath +
                    threadMap.get("deviceId") + "_" + threadMap.get("robotCode") + "_" + timeFormat + ".jpg";
            log.info("tagPath==={}", tagPath);
            log.info("imgPath:{},tagPath:{}", imgPath,tagPath);
            uploadFileToUpFtps(imgPath, "/" + tagPath, upFtpsConfig);
            xmlItem.put("file_path", tagPath);

            xmlItem.put("value", warnInfo.getValue());
            xmlItem.put("unit", Optional.ofNullable(tStdDevicemete.getUnit()).orElse(""));
            xmlItem.put("value_unit", warnInfo.getValue() + xmlItem.get("unit"));
            xmlItem.put("time", DateTimeUtil.format(new Date()));
            xmlItem.put("task_patrolled_id", warnInfo.getTaskId()+"_"+DateTimeUtil.format3(warnInfo.getWarnTime()));
            xmlItem.put("content", warnInfo.getWarnContent());

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>(2);
            map.put("list", list);
            log.info("告警上报：-" + map);
//            Constant.otherServer(map, Constant.TCP_URL);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将文件上传至上级系统ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    private void uploadFileToUpFtps(String sourcePath, String targetPathName, UpFtpsConfig upFtpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     *
     * @param key redis的key
     * @return Set<String>
     */
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
}
