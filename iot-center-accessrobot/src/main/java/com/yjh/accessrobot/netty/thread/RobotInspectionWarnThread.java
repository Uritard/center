package com.yjh.accessrobot.netty.thread;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.mqtt.GetSpringUtil;
import com.yjh.accessrobot.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.accessrobot.common.mqtt.alarmMsgBody.Defect;
import com.yjh.accessrobot.common.mqtt.ftpsservice;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.configuration.UpFtpsConfig;
import com.yjh.accessrobot.module.command.entity.TStdDeviceMete;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.device.service.AlarmService;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YChen
 * @date 2021/12/16
 * 机器人巡视结果的告警处理线程
 */
@lombok.extern.slf4j.Slf4j
public class RobotInspectionWarnThread implements Runnable{

    private Map<String,String> warnResultMap;
    private RedisTemplate redisTemplate;
    private String webSocketUrl;
    private UpFtpsConfig upFtpsConfig;
    private String stationCode;

    public RobotInspectionWarnThread(Map<String,String> warnResultMap, RedisTemplate redisTemplate, String webSocketUrl, String stationCode){
        this.warnResultMap = warnResultMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
        this.stationCode = stationCode;
        this.upFtpsConfig = StaticContextAccessor.getBean(UpFtpsConfig.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡视结果产生的告警数据 >>>>>>> warnResultMap==={}",warnResultMap);
            String taskId = warnResultMap.get("taskCode");

            String robotCode = warnResultMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + taskId);
            for (String key : robotInfoKeys) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                if (Objects.equals(robotCode,redisInfoMap.get("robotCode"))
                        && Objects.equals(taskId,redisInfoMap.get("taskId"))
                        && Objects.equals(warnResultMap.get("deviceId"),redisInfoMap.get("inspectionCode"))) {
                    Long instanceId = Long.valueOf(redisInfoMap.get("instanceId"));
                    TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMeteInfo(instanceId);

                    storeWarnInfo(tStdDevicemete, instanceId, taskId, robotCode);

                    Long warnId = StaticContextAccessor.getBean(RobotService.class).selectWarnId(taskId, instanceId);
                    log.info("warnId===" + warnId);

                    Map<String, String> currentWarnInfo = new HashMap<>(16);
                    currentWarnInfo.put("warnId", warnId.toString());
                    currentWarnInfo.put("defectModel", "450");
                    currentWarnInfo.put("isPop", "false");

                    // webSocket通知前端刷新告警统计数量
                    Map<String, Object> jasonMaps = new HashMap<>(16);
                    jasonMaps.put("type", "newAlarm");
                    jasonMaps.put("alarmName", warnResultMap.get("content"));
                    jasonMaps.put("alarmTime", warnResultMap.get("time"));
                    jasonMaps.put("alarmContent", warnResultMap.get("content"));
                    String jsons = JSON.toJSONString(jasonMaps);
                    log.info("告警生成-前端推送：" + jsons);
                    Constant.postUrl(webSocketUrl, jsons);

                    //webSocket通知前端调用查询告警弹框的接口
                    currentWarnInfo.put("isPop", "true");
                    Map<String, Object> jasonMaps2 = new HashMap<>(16);
                    jasonMaps2.put("type", "alarmPopUp");
                    jasonMaps2.put("warnId", warnId);
                    jasonMaps2.put("defectModel", 450);
                    String json = JSON.toJSONString(jasonMaps2);
                    log.info("发送给前端的消息：" + json);
                    Constant.postUrl(webSocketUrl, json);

                    //最近一条告警信息 入缓存
                    redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
                    log.info("currentWarnInfo666" + currentWarnInfo);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 存储告警基本信息
     * @param tStdDevicemete 测点信息
     * @param instanceId 巡检点id
     * @param taskId 任务id
     * @param robotCode 机器人唯一标识
     * @return void
     */
    private void storeWarnInfo(TStdDeviceMete tStdDevicemete, Long instanceId, String taskId, String robotCode){
        try {
            TWarnInfo warnInfo = new TWarnInfo();
            warnInfo.setWarnTime(new Date());
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(instanceId);
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(276);
            Integer warnFlag = Integer.valueOf(StaticContextAccessor.getBean(RobotService.class).selectDictCodeByNote("其他", "defect_model"));
            warnInfo.setDefectModel(warnFlag);
            warnInfo.setAlarmSource(282);
            warnInfo.setValue(warnResultMap.get("value"));
            warnInfo.setTaskId(taskId);
            Long robotId = StaticContextAccessor.getBean(RobotService.class).selectRobotIdByCode(robotCode);
            warnInfo.setDeviceCode(robotId.toString());
            warnInfo.setWarnName(warnResultMap.get("content"));
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
            warnInfo.setImagePath(redisInfoMap.get("picpath"));
            if (Objects.nonNull(warnResultMap.get("alarmType")) && !StringUtils.isEmpty(warnResultMap.get("alarmLevel"))) {
                int warnLevel = StaticContextAccessor.getBean(RobotService.class).selectDictCode("alarmLevel", warnResultMap.get("alarmLevel"), "alarm_level");
                warnInfo.setWarnLevel(warnLevel);
            }
            warnInfo.setWarnContent(warnResultMap.get("content"));
            if (Objects.nonNull(warnResultMap.get("alarmType")) && !StringUtils.isEmpty(warnResultMap.get("alarmType"))) {
                int warnType = StaticContextAccessor.getBean(RobotService.class).selectDictCode("pointAlarmType", warnResultMap.get("alarmType"), "point_alarm_type");
                warnInfo.setWarnType(warnType);
            }
            log.info("要插库的告警数据是==={}", warnInfo);

            String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
            Map<String, String> warnMap = new HashMap<>(16);
            warnMap.put("deviceId", warnInfo.getDeviceId().toString());
            warnMap.put("customId", warnInfo.getCunstomId());
            warnMap.put("instanceId", warnInfo.getInstanceId().toString());
            warnMap.put("stdMeteId", warnInfo.getStdMeteId().toString());
            warnMap.put("taskId", warnInfo.getTaskId());
            warnMap.put("value", warnInfo.getValue());
            warnMap.put("confMode", "276");
            warnMap.put("alarmSource", warnInfo.getAlarmSource().toString());
            warnMap.put("defectModel", warnInfo.getDefectModel().toString());
            warnMap.put("warnLevel", warnInfo.getWarnLevel().toString());
            warnMap.put("warnName", warnInfo.getWarnName());
            warnMap.put("warnTime", new SimpleDateFormat().format(warnInfo.getWarnTime()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
            log.info("warnMap===" + warnMap);
            redisTemplate.opsForHash().putAll(warnName, warnMap);

            StaticContextAccessor.getBean(RobotService.class).insertWarn(warnInfo);

            // 将产生的告警上送至上级系统
            alarmToUpSystem(warnInfo, tStdDevicemete);

            // 若是缺陷，上报至算法管理平台    识别类型是3即设备外观查看
            if (Objects.equals("3", warnResultMap.get("recognitionType"))){
                alarmToAlgorithmManagement(warnInfo, tStdDevicemete);
            }

        }catch (Exception e){
            log.error(e.getMessage(), e);
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
            xmlItem.put("patroldevice_code", warnResultMap.getOrDefault("robotCode", ""));
            String robotName = StaticContextAccessor.getBean(RobotService.class).selectRobotNameByCode(warnResultMap.get("robotCode"));
            xmlItem.put("patroldevice_name", robotName);
            xmlItem.put("task_name", warnResultMap.get("taskName"));
            xmlItem.put("task_code", warnResultMap.get("taskCode"));
            xmlItem.put("device_name", warnResultMap.get("deviceName"));
            xmlItem.put("device_id", warnResultMap.get("deviceId"));
            xmlItem.put("alarm_level", warnResultMap.get("alarmLevel"));
            xmlItem.put("alarm_type", warnResultMap.get("alarmType"));
            xmlItem.put("recognition_type", warnResultMap.get("recognitionType"));

            String recognitionType = warnResultMap.get("recognitionType");
            String fileNamePath = "";
            String fileType = "";
            switch (recognitionType){
                case "1":
                case "3": fileType = "2"; fileNamePath = "/CCD/"; break;
                case "2": fileType = "5"; fileNamePath = "/CCD/"; break;
                case "4": fileType = "1"; fileNamePath = "/FIR/"; break;
                case "5": fileType = "3"; fileNamePath = "/Audio/"; break;
                default: break;
            }
            xmlItem.put("file_type", fileType);

            String imgPath = warnInfo.getImagePath().replaceAll(
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content")),
                    String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content")));
            String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(warnInfo.getWarnTime());
            // 文件格式：变电站编码/年/月/日/巡视任务编码/CCD或FIR或Audio/设备点位ID_编码_时间.jpg
            String tagPath = "robotAlarm/" + stationCode + "/" + timeFormat.substring(0,4) + "/" + timeFormat.substring(4,6) + "/" + timeFormat.substring(6,8)
                    + "/" + warnInfo.getTaskId() + fileNamePath + warnResultMap.get("deviceId") + "_" + warnResultMap.get("robotCode") + "_" + timeFormat + ".jpg";
            log.info("tagPath==={}", tagPath);
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
     * 将产生的缺陷上送至算法管理平台
     *
     * @param warnInfo 告警信息
     * @param tStdDevicemete 测点信息
     * @return void
     */
    private void alarmToAlgorithmManagement(TWarnInfo warnInfo, TStdDeviceMete tStdDevicemete){
        try {
            ftpsservice ftpsservice= GetSpringUtil.getBean("ftpsservice");
            String flag= ftpsservice.getFlag();
            if("1".equals(flag)) {
                log.info("defect类型:开始向算法管理平台发送图片和mqtt消息");
                Alarm alarmDetail = new Alarm();
                SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String yearMonth = ym.format(new Date());
                String nowTime = timeFormat.format(new Date());

                String redisKeyName = "t_cruise_task_result:" + warnInfo.getTaskId() + ":" + warnInfo.getInstanceId();
                String originPicPath = String.valueOf(redisTemplate.opsForHash().get(redisKeyName, "origpic"));

                // 间隔名称  设备名称  测点名称
                String picF = nowTime+"_"+"间隔名称"+"_"+"设备名称"+"_"+"测点名称"+"_";

                String origpicpath = ""; //原图地址  /home 开头

                String resultImagebak = ""; //结果图地址  /home 开头

                //原图目标地址
                String remoteorigfilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+picF+"原图.jpg";

                //告警目标地址
                String remotefilepath=ftpsservice.getFtpsRemotePath() + "/" +"缺陷"+"/"+yearMonth+"/"+picF+"缺陷告警.jpg";


                List<Defect> defectList=new ArrayList<>();

                for (int i=0;i<0 ; i++) {//构建坐标
                    Defect defect=new Defect();
                    defect.setX1("坐标1");
                    defect.setY1("坐标2");
                    defect.setX2("坐标3");
                    defect.setY2("坐标4");
                    defect.setType("拼音缩写 例：wcaqm");
                    DecimalFormat df =  new DecimalFormat("0%");
                    String confidence = df.format(0d);//置信度 0-100之间
                    defect.setConfidence(confidence);
                    // 描述 未穿工装(坐标位置 11,22,55,66;置信度 70%)
                    defect.setDesc("缺陷中文描述 例：未穿安全帽"+"(坐标位置 "+
                            defect.getX1()+","+
                            defect.getY1()+","+
                            defect.getX2()+","+
                            defect.getY2()+";"+
                            "置信度 "+ confidence
                            +"%)");
                    defectList.add(defect);
                }
                alarmDetail.setDefect(defectList);
                alarmDetail.setBay_name("间隔名称");
                alarmDetail.setDevice_name("设备名");  //需要修改位devicename
                alarmDetail.setPoint_name("测点名称");
                alarmDetail.setTime("告警时间");
                alarmDetail.setPic_raw(remoteorigfilepath);         //图片原图
                alarmDetail.setPic_diff_base("");               //判别基准图路径
                alarmDetail.setPic_different("");               //判别告警图路径,即分析结果图
                alarmDetail.setPic_defect(remotefilepath);      //缺陷告警图路径//

                ftpsservice.uploadFile("缺陷告警",origpicpath,remoteorigfilepath);
                ftpsservice.uploadFile("缺陷告警",resultImagebak,remotefilepath);
                if( !ftpsservice.fileExits(remoteorigfilepath)){
                    ftpsservice.uploadFile("缺陷告警",origpicpath,remoteorigfilepath);  //原始图片上传
                }
                if( !ftpsservice.fileExits(remotefilepath)){
                    ftpsservice.uploadFile("缺陷告警",resultImagebak,remotefilepath);
                }

                log.info("巡视主机与智能分析主机：origpicpath:{}",origpicpath);
                log.info("巡视主机与智能分析主机：remoteorigfilepath:{}",remoteorigfilepath);
                log.info("巡视主机与智能分析主机：resultImagebak:{}",resultImagebak);
                log.info("巡视主机与智能分析主机：remotefilepath:{}",remotefilepath);
                AlarmService alarmService= GetSpringUtil.getBean("alarmService");
                alarmService.PushMsg(alarmDetail);
                log.info("发送算法管理平台结束");
            }
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
            if(org.apache.commons.lang3.StringUtils.isEmpty(sourcePath) || org.apache.commons.lang3.StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, upFtpsConfig.getIp(), upFtpsConfig.getPort(),
                    upFtpsConfig.getKeypw(), upFtpsConfig.getUsername(), upFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至上级系统ftp服务器错误:{}", e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
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
