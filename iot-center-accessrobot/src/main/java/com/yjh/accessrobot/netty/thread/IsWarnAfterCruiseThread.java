package com.yjh.accessrobot.netty.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.mqtt.GetSpringUtil;
import com.yjh.accessrobot.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessrobot.common.mqtt.ftpsservice;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.TCruiseTask;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.service.AlarmService;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private String webSocketUrl;

    private UpFtpsConfig upFtpsConfig;
    private String stationCode;
    private SimpleDateFormat timeFormatTemp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public IsWarnAfterCruiseThread(Map<String, String> threadMap, RedisTemplate redisTemplate, String webSocketUrl, String stationCode) {
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
        this.stationCode = stationCode;
        this.upFtpsConfig = StaticContextAccessor.getBean(UpFtpsConfig.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡检结果并判断是否告警 >>>>>>> threadMap==={}", threadMap);
            String taskId = threadMap.get("taskCode");

            String robotCode = threadMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            String robotTaskId = StaticContextAccessor.getBean(RobotService.class).selectTaskId(taskId);
            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + robotTaskId);
            //根据taskId查询相关内容
            TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);
            log.info("taskId是: {}的任务数据tCruiseTask是: {}", taskId, tCruiseTask);

            if (Objects.isNull(tCruiseTask.getTaskType())) {
                for (String key : robotInfoKeys) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (Objects.equals(robotCode, redisInfoMap.get("robotCode"))
                            && Objects.equals(taskId, redisInfoMap.get("taskId"))
                            && Objects.equals(threadMap.get("deviceId"), redisInfoMap.get("inspectionCode"))) {
                        Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                        TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(instanceId);

                        //该巡视点还在,能找到对应测点信息
                        if (Objects.nonNull(tStdDevicemete)) {
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

                            Result result = sendPostRequest(Constant.WARN_JUDGE, params);
                            Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                            log.info("object转map的东西==={}", map);
                            Boolean isWarN = (Boolean) map.get("isWarn");
                            String outRange = null;
                            if (Objects.nonNull(map.get("outRange"))) {
                                outRange = map.get("outRange").toString();
                            }

                            //组装告警基本信息
                            TWarnInfo warnInfo = new TWarnInfo();
                            warnInfo.setWarnTime(DateTimeUtil.parse(threadMap.get("time")));
                            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
                            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
                            warnInfo.setInstanceId(instanceId);
                            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
                            //未核查
                            warnInfo.setConfMode(276);
                            Integer warnFlag = Integer.valueOf(StaticContextAccessor.getBean(RobotService.class).selectDictCodeByNote("其他", "defect_model"));
                            warnInfo.setDefectModel(warnFlag);
                            //主辅设备
                            warnInfo.setAlarmSource(282);
                            warnInfo.setImagePath(threadMap.get("relativePath"));
                            warnInfo.setValue(threadMap.get("value"));
                            warnInfo.setTaskId(taskId);
                            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
                            warnInfo.setDeviceCode(robotId.toString());
                            map.put("absolutePath",threadMap.get("absolutePath"));
                            map.put("deviceName",threadMap.get("deviceName"));

                            ifGeneratedAlarm(isWarN, map, taskId, warnInfo, outRange, tStdDevicemete);


                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 判断该点是否产生告警以及告警信息
     *
     * @param isWarN         是否告警标识
     * @param map            根据告警规则判断的结果
     * @param taskId         任务id
     * @param warnInfo       告警信息
     * @param outRange       告警溢出值
     * @param tStdDevicemete 测点信息
     * @return void
     */
    private void ifGeneratedAlarm(Boolean isWarN, Map<String, Object> map, String taskId, TWarnInfo warnInfo, String outRange, TStdDeviceMete tStdDevicemete) throws Exception {
        if (Boolean.TRUE.equals(isWarN)) {
            warnInfo.setWarnName(map.get("warnName").toString());
            warnInfo.setWarnLevel(Integer.valueOf(map.get("warnLevel").toString()));
            warnInfo.setWarnContent(map.get("warnContent").toString());
            warnInfo.setOutRange(outRange);
            log.info("要插库的告警数据是===" + warnInfo);

            /// 留着备用
            /*Map<String,String> redisWarnInfoMap = new HashMap<>();
            redisWarnInfoMap.put("isWarn","1");
            redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + warnInfo.getInstanceId().toString(),redisWarnInfoMap);*/

            String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
            Map<String, String> warnMap = new HashMap<>(16);
            warnMap.put("deviceId", warnInfo.getDeviceId().toString());
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", warnInfo.getInstanceId().toString());
            warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("imagePath", warnInfo.getImagePath());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
            warnMap.put("defectModel", warnInfo.getDefectModel().toString());
            warnMap.put("warnLevel", warnInfo.getWarnLevel().toString());
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", new SimpleDateFormat().format(warnInfo.getWarnTime()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
            if (Objects.nonNull(warnInfo.getOutRange())) {
                warnMap.put("outRange", warnInfo.getOutRange());
            }
            log.info("warnMap===" + warnMap);
            redisTemplate.opsForHash().putAll(warnName, warnMap);

            StaticContextAccessor.getBean(RobotService.class).insertWarn(warnInfo);
            Long warnId = StaticContextAccessor.getBean(RobotService.class).selectWarnId(warnInfo.getTaskId(), warnInfo.getInstanceId());
            log.info("warnId===" + warnId);

            Map<String, String> currentWarnInfo = new HashMap<>(16);
            currentWarnInfo.put("warnId", warnId.toString());
            currentWarnInfo.put("defectModel", "450");
            currentWarnInfo.put("isPop", "false");

            // webSocket通知前端刷新告警统计数量
            Map<String, Object> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "newAlarm");
            jasonMaps.put("alarmName", warnInfo.getWarnName());
            jasonMaps.put("alarmTime", warnInfo.getWarnTime());
            jasonMaps.put("alarmContent", warnInfo.getWarnContent());
            String jsons = JSON.toJSONString(jasonMaps);
            log.info("告警生成-前端推送：" + jsons);
            Constant.postUrl(webSocketUrl, jsons);

            //判断该测点是否设置了告警推送,若是,则将配置的告警信息组成告警弹框所需内容推给前端;不是,不推
            String alarmNote = tStdDevicemete.getAlarmNote();
            Integer alarmLevel = tStdDevicemete.getAlarmLevel();
            Integer warnLevel = warnInfo.getWarnLevel();
            log.info("该测点是否配置了告警提示是===" + alarmNote);
            log.info("该测点告警推送配置的告警等级是===" + alarmLevel);
            log.info("产生的该条告警等级是===" + warnLevel);
            boolean one = (Objects.nonNull(alarmNote) && "1".equals(alarmNote));
            boolean two = (Objects.nonNull(alarmLevel) && (warnLevel.compareTo(alarmLevel) == 0 || warnLevel > alarmLevel));
            log.info("一层判断" + one + "二层判断" + two);

            if (Boolean.TRUE.equals(one) && Boolean.TRUE.equals(two)) {
                //webSocket通知前端调用查询告警弹框的接口
                Map<String, Object> jasonMaps2 = new HashMap<>(16);
                jasonMaps2.put("type", "alarmPopUp");
                jasonMaps2.put("warnId", warnInfo.getWarnId());
                jasonMaps2.put("defectModel", warnInfo.getDefectModel());
                String json = JSON.toJSONString(jasonMaps2);
                log.info("告警弹窗-前端推送：" + json);
                currentWarnInfo.put("isPop", "true");
                Constant.postUrl(webSocketUrl, json);
            }

            redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);

            // 将产生的告警上送至上级系统
             alarmToUpSystem(warnInfo, tStdDevicemete);

            //jeff add send mqtt message
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
                   Map<String, Object> cruiseResult2 = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskidbak + ":" + instanceIdbak);//读redis
                   String devicename=map.get("deviceName").toString();
                   alarm.setDevice_name(devicename);
                   String origpicpath = cruiseResult2.get("origpic").toString();
                   String[] str2 = origpicpath.split("/");
                   String origpcimagename = str2[str2.length - 1];
                   String remoteorigfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + origpcimagename;
                   //结果文件
                   String resultImagebak = map.get("absolutePath").toString();
                   String[] str3 = resultImagebak.split("/");
                   String resultimagename = str3[str3.length - 1];  //获取结果图名称，然后拼接远程文件全路径
                   String remoteresultfilepath = ftpsservice.getFtpsRemotePath() + "/" + "different" + "/" + year + "/" + month + "/" + resultimagename;
                   alarm.setPic_raw(remoteresultfilepath);        //原图  算法管理平台对应的原始文件路径
                   alarm.setPic_different(remoteorigfilepath);    //机器人分析结果图 算法管理平台对应的原始文件路径
                   ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);   //原始图片上传
                   ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath); //判别结果图片

                   if( !ftpsservice.fileExits(remoteorigfilepath)){
                       ftpsservice.uploadFile("判别告警", origpicpath, remoteorigfilepath);   //原始图片上传
                   }
                   if( !ftpsservice.fileExits(remoteorigfilepath)){
                       ftpsservice.uploadFile("判别告警", resultImagebak, remoteresultfilepath); //判别结果图片
                   }
                   alarmService.PushMsg(alarm);
               }
                  //send end
           } catch (Exception e) {
               log.error("与算法管理平台交互失败" + e);
           }

        }
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param tStdDevicemete 测点信息
     * @return void
     */
    private void alarmToUpSystem( TWarnInfo warnInfo, TStdDeviceMete tStdDevicemete){
        try {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> xmlItems = new ArrayList<>();
            Map<String, Object> xmlItem = new HashMap<>(16);
            xmlBaseModel.setType("62");
            xmlItem.put("patroldevice_code", threadMap.getOrDefault("robotCode", ""));
            String robotName = StaticContextAccessor.getBean(RobotService.class).selectRobotNameByCode(threadMap.get("robotCode"));
            xmlItem.put("patroldevice_name", robotName);
            String taskName = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(warnInfo.getTaskId()).getTaskName();
            xmlItem.put("task_name", Optional.ofNullable(taskName).orElse(""));
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

            String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(
                    timeFormatTemp.parse(threadMap.get("time")));
            String imgPath = warnInfo.getImagePath().replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content")));
            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR或Audio/设备点位ID_编码_时间.jpg
            String tagPath = "robotAlarm/" + stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
                    + "/" + warnInfo.getTaskId() + fileNamePath + threadMap.get("deviceId") + "_" + threadMap.get("robotCode") + "_" + timeFormat + ".jpg";
            log.info("tagPath==={}", tagPath);
//            String targetNamePath = imgPath.replace(
//                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content")), "").substring(1);
            log.info("imgPath:{},tagPath:{}",imgPath,tagPath);
            uploadFileToUpFtps(imgPath, "/" + tagPath, upFtpsConfig);
            xmlItem.put("file_path", tagPath);

            xmlItem.put("value", warnInfo.getValue());
            xmlItem.put("unit", Optional.ofNullable(tStdDevicemete.getUnit()).orElse(""));
            xmlItem.put("value_unit", warnInfo.getValue() + xmlItem.get("unit"));
            xmlItem.put("time", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
            xmlItem.put("task_patrolled_id", warnInfo.getTaskId()+"_"+simpleDateFormat2.format(warnInfo.getWarnTime()));
            xmlItem.put("content", warnInfo.getWarnContent());

            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("告警上报：-" + map);
            Constant.otherServer(map, Constant.TCP_URL);
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

    public Result sendPostRequest(String url, Map<String, Object> params) {
        return StaticContextAccessor.getBean(ServiceRestTemplate.class).getForObject(url, Result.class, params);
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
