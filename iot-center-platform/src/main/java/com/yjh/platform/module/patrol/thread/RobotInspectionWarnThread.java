package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskAlarm;
import com.yjh.platform.module.task.entity.TDefectInfo;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.AnalyseDataOperateService;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.ProcessResultToUpSystem;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.service.TDefectInfoService;
import com.yjh.platform.module.task.service.TWarnInfoService;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_ABNORMAL_ABNORMALALARM;
import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_ABNORMAL;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.MAP_LOCK;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * @author YChen
 * @date 2021/12/16
 * 机器人巡视结果的告警处理线程
 */
@Slf4j
public class RobotInspectionWarnThread implements Runnable{

    private final RobotPatrolTaskAlarm taskAlarm;
    private final RedisTemplate redisTemplate;
    private final PatrolResultHandler patrolResultHandler;
    private final AnalyseDataOperateService analyseDataOperateService;
    private final UPatrolTaskService uPatrolTaskService;
    private final String taskCode;
    private final TRobotInspectionDao tRobotInspectionDao;
    private final Object waiter = new Object();

    public RobotInspectionWarnThread(RobotPatrolTaskAlarm taskAlarm, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService, String taskCode){
        this.taskAlarm = taskAlarm;
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
        this.taskCode = taskCode;
        this.patrolResultHandler = StaticContextAccessor.getBean(PatrolResultHandler.class);
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.tRobotInspectionDao = StaticContextAccessor.getBean(TRobotInspectionDao.class);
    }

    @Override
    public void run() {
        try {
            log.info("开始处理巡视结果产生的告警数据 >>>>>>> taskAlarm==={}", JSON.toJSONString(taskAlarm));
            String taskId = taskAlarm.getTaskCode();
            String robotCode = taskAlarm.getRobotCode();

            // taskId是巡视主机的id,robotTaskId是机器人上报的id
            String robotTaskId = taskCode;
            log.info("robotTaskId==={}", robotTaskId);

            UPatrolTask uPatrolTaskTemp = uPatrolTaskService.selectTaskByTaskCode(robotTaskId);
            log.info("uPatrolTaskTemp=={}", uPatrolTaskTemp);
            if (Objects.nonNull(uPatrolTaskTemp) && StringUtils.isNotEmpty(uPatrolTaskTemp.getDateType())){
                boolean moreTime = uPatrolTaskTemp.getDateType().split(" ")[2].contains(",");
                if (moreTime) {
                    robotTaskId = taskId;
                }
            }
            log.info("robotTaskId=={}", robotTaskId);

            String redisKey = "Robot_SPAndIN_Info:" + robotCode + ":" + taskAlarm.getDeviceId();
            Map<String,String> robotInfoKeyMap = redisTemplate.opsForHash().entries(redisKey);
            long instanceId = NumberUtils.toLong(robotInfoKeyMap.get("instanceId"));
            // 上级系统没有存储对应值，DeviceId 就是下级的 instanceId
            if (instanceId == 0) {
                String originId = taskAlarm.getDeviceId();
                TCruisePointInstance insInfo = tRobotInspectionDao.selectRealInstance(originId, robotCode);
                log.info("instanceInfo: {}", JSON.toJSONString(insInfo));
                instanceId = insInfo.getInstanceId();
            }

            log.info("instanceId=={}", instanceId);
            TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(instanceId);

            TWarnInfo warnInfo = new TWarnInfo();
            if (CommonUtils.isEmptyOrNullstr(taskAlarm.getDefectType())){
                warnInfo = getWarnInfo(tStdDevicemete, taskId, instanceId, robotCode, warnInfo);
                putWarnMapRedis(taskId, warnInfo);
            }else {
                TDefectInfo tDefectInfo = getDefectInfo(tStdDevicemete, taskId, instanceId);
                warnInfo.setWarnSubtype(tDefectInfo.getDefectType());
                putDefectMapRedis(taskId, tDefectInfo);
            }

            Map<String, String> cruiseMap = new HashMap<>();
            cruiseMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            cruiseMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_ABNORMALALARM));
            // 为了避免存入告警时初始化的值不正确，再次传入一下 instanceId避免问题
            cruiseMap.put("instanceId", String.valueOf(instanceId));

            redisTemplate.opsForHash().putAll(PATROL_TASK_PREFIX + taskId + ":" + instanceId, cruiseMap);

            // 将产生的告警上送至上一级系统
            alarmToUpSystem(taskAlarm.getAlarmLevel(), warnInfo, taskId, instanceId);

            // 若是缺陷，上报至算法管理平台    识别类型是3即设备外观查看
            /*if (Objects.equals("3", taskAlarm.getRecognitionType())){
                alarmToAlgorithmManagement(warnInfo, tStdDevicemete);
            }*/

            // 告警推送
            Map<String, String> infoMap = new HashMap<>(5);
            infoMap.put("alarmLevel", String.valueOf(warnInfo.getWarnLevel()));
            infoMap.put("flag", "robot");
            infoMap.put("defectModel", String.valueOf(warnInfo.getDefectModel()));
            infoMap.put("warnId", String.valueOf(warnInfo.getWarnId()));
            patrolResultHandler.alarmPopUp(tStdDevicemete, infoMap);

        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    private TDefectInfo getDefectInfo(TStdDeviceMete tStdDevicemete, String taskId, Long instanceId){
        TDefectInfo tDefectInfo = new TDefectInfo();
        try {
            tDefectInfo.setDefectTime(DateTimeUtil.parse(taskAlarm.getTime()));
            tDefectInfo.setDefectName(taskAlarm.getContent());
            tDefectInfo.setDefectContent(taskAlarm.getContent());
            tDefectInfo.setDeviceId(tStdDevicemete.getDeviceId());
            tDefectInfo.setCunstomId(tStdDevicemete.getCustomId());
            tDefectInfo.setInstanceId(instanceId);
            tDefectInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            tDefectInfo.setConfMode(276);
            tDefectInfo.setAlarmSource(282);
            tDefectInfo.setValue(taskAlarm.getValue());
            tDefectInfo.setDefectType(Integer.valueOf(taskAlarm.getDefectType()));

            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            tDefectInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            tDefectInfo.setDefectLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));

        }catch (Exception e){
            log.error("组装缺陷信息异常：", e);
        }
        log.info("tDefectInfo==={}", tDefectInfo);
        StaticContextAccessor.getBean(TDefectInfoService.class).insert(tDefectInfo);
        return tDefectInfo;
    }

    private TWarnInfo getWarnInfo(TStdDeviceMete tStdDevicemete, String taskId, Long instanceId, String robotCode, TWarnInfo warnInfo){
        try {
            warnInfo.setWarnTime(DateTimeUtil.parse(taskAlarm.getTime()));
            warnInfo.setDeviceId(tStdDevicemete.getDeviceId());
            warnInfo.setCunstomId(tStdDevicemete.getCustomId());
            warnInfo.setInstanceId(instanceId);
            warnInfo.setStdMeteId(tStdDevicemete.getDeviceMeteId());
            warnInfo.setConfMode(276);
            Integer warnFlag = Integer.valueOf(analyseDataOperateService.selectDictCode("defect_model", "其他"));
            warnInfo.setDefectModel(warnFlag);
            warnInfo.setAlarmSource(282);
            warnInfo.setValue(taskAlarm.getValue());
            warnInfo.setTaskId(taskId);
            String robotId = String.valueOf(StaticContextAccessor.getBean(TRobotInfoDao.class).selectRobotIdByCode(robotCode));
            warnInfo.setDeviceCode(robotId);
            warnInfo.setWarnName(taskAlarm.getContent());
            String alarmType = taskAlarm.getAlarmType();
            if (StringUtils.isNotEmpty(alarmType)) {
                warnInfo.setWarnType(analyseDataOperateService.selectDictCodeByUpDict("point_alarm_type", alarmType));
            }
            warnInfo.setWarnContent(taskAlarm.getContent());

            Map<String, String> info = getWarnOrDefectInfo(taskId, instanceId);
            warnInfo.setImagePath(Optional.ofNullable(info.get("imagePath")).orElse(""));
            warnInfo.setWarnLevel(Integer.valueOf(Optional.ofNullable(info.get("level")).orElse("0")));

        }catch (Exception e){
            log.error("组装告警信息异常：", e);
        }
        log.info("warnInfo==={}", warnInfo);
        StaticContextAccessor.getBean(TWarnInfoService.class).insert(warnInfo);
        return warnInfo;
    }

    private void putWarnMapRedis(String taskId, TWarnInfo warnInfo) {
        String warnName = "warnInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> warnMap = new HashMap<>(16);
        try {
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
            warnMap.put("warnTime", Objects.nonNull(warnInfo.getWarnTime()) ?
                    DateTimeUtil.format(warnInfo.getWarnTime()) : DateTimeUtil.format(new Date()));
            warnMap.put("warnContent", warnInfo.getWarnContent());
        }catch (Exception e){
            log.error("组装告警map异常：", e);
        }
        log.info("warnMap==={}", warnMap);
        redisTemplate.opsForHash().putAll(warnName, warnMap);
    }

    private void putDefectMapRedis(String taskId, TDefectInfo tDefectInfo) {
        String defectName = "defectInfo:" + taskId + String.valueOf(UUID.randomUUID()).replace("-", "");
        Map<String, String> defectMap = new HashMap<>(16);
        try {
            defectMap.put("defectLevel", String.valueOf(tDefectInfo.getDefectLevel()));
            defectMap.put("defectContent", tDefectInfo.getDefectContent());
            defectMap.put("deviceId", String.valueOf(tDefectInfo.getDeviceId()));
            defectMap.put("instanceId", String.valueOf(tDefectInfo.getInstanceId()));
            defectMap.put("customId", tDefectInfo.getCunstomId());
            defectMap.put("stdMeteId", String.valueOf(tDefectInfo.getStdMeteId()));
            defectMap.put("confMode", "276");
            defectMap.put("alarmSource", String.valueOf(tDefectInfo.getAlarmSource()));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", tDefectInfo.getValue());
        }catch (Exception e){
            log.error("组装缺陷map异常：" , e);
        }
        log.info("defectMap==={}", defectMap);
        redisTemplate.opsForHash().putAll(defectName, defectMap);
    }

    private Map<String, String> getWarnOrDefectInfo(String taskId, Long instanceId){
        HashMap<String, String> map = new HashMap<>(4);
        try {
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            log.info("taskId是：{}，instanceId是：{}的 tCruiseTaskResultMap：{}", taskId, instanceId, tCruiseTaskResultMap);
            if (Objects.nonNull(tCruiseTaskResultMap.get("picpath"))) {
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            } else {
                MAP_LOCK.put(taskId + instanceId, waiter);
                synchronized (waiter) {
                    waiter.wait(5000);
                }
                tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                map.put("imagePath", tCruiseTaskResultMap.get("picpath"));
            }

            String alarmLevel = taskAlarm.getAlarmLevel();
            switch (alarmLevel) {
                case "1":
                    map.put("level", "130");
                    break;
                case "2":
                    map.put("level", "131");
                    break;
                case "3":
                    map.put("level", "132");
                    break;
                case "4":
                    map.put("level", "133");
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        log.info("map==={}", map);
        return map;
    }

    /**
     * 将产生的告警上送至上级系统
     *
     * @param warnInfo 告警信息
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    private void alarmToUpSystem(String alarmLevel, TWarnInfo warnInfo, String taskId, Long instanceId){
        try{
            String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
            StaticContextAccessor.getBean(ProcessResultToUpSystem.class).alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, warnInfo);
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
    /*private void alarmToAlgorithmManagement(TWarnInfo warnInfo, TStdDeviceMete tStdDevicemete){
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

                for (int i=0;i<1 ; i++) {//构建坐标
                    Defect defect=new Defect();
                    defect.setX1(11);
                    defect.setY1(22);
                    defect.setX2(33);
                    defect.setY2(44);
                    defect.setType("拼音缩写 例：wcaqm");
                    DecimalFormat df =  new DecimalFormat("0%");
                    int confidence = 0;//置信度 0-100之间
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
    }*/

}
