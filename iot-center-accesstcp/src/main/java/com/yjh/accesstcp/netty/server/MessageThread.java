/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty.server;

import com.alibaba.fastjson.JSON;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.Object2Map;
import com.yjh.accesstcp.common.utils.StaticContextAccessor;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.entity.RobotTaskInstanceInfo;
import com.yjh.accesstcp.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.proxy.UPatrolTaskProxy;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.module.device.service.TCameraPresetService;
import com.yjh.accesstcp.thread.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/6/22
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class MessageThread {
    private static final ExecutorService executorService =
        new ThreadPoolExecutor(10, 30, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(128), new MyThreadFactory(true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static final String TASK_PRIORITY_REDIS_KEY="task_priority_config:";

    public static void doProcessMessage(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
        SendToUpSystemServices sendToUpSystemServices, AnalysisUnionTaskFileService analysisUnionTaskFileService, RedisTemplate redisTemplate, RegisterManager registerManager) {
        executorService.execute(() -> {
            try {
                processMessage(xmlBaseModel, sendSessionId, clientHandler, sendToUpSystemServices, analysisUnionTaskFileService, redisTemplate, registerManager);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public static void doProcessMessageSync(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
                                            SendToUpSystemServices sendToUpSystemServices, AnalysisUnionTaskFileService analysisUnionTaskFileService,
                                            RedisTemplate redisTemplate, RegisterManager registerManager) {
        try {
            processMessage(xmlBaseModel, sendSessionId, clientHandler, sendToUpSystemServices, analysisUnionTaskFileService, redisTemplate, registerManager);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void processMessage(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
        SendToUpSystemServices sendToUpSystemServices, AnalysisUnionTaskFileService analysisUnionTaskFileService,
                                       RedisTemplate redisTemplate, RegisterManager registerManager) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //系统消息
        if ("251".equals(xmlBaseModel.getType())) {
            //响应注册
            if ("4".equals(xmlBaseModel.getCommand())) {
                // 100:需要重发注册消息 200:服务端响应 我方开启心跳
                if ("100".equals(xmlBaseModel.getCode())) {
                    log.info("---注册响应---200需要重发注册消息----");
                    registerManager.register(clientHandler);
                } else if ("200".equals(xmlBaseModel.getCode())) {
                    log.info("---注册响应--200响应成功----");
                    registerManager.terminate();
                    List<Map<String, Object>> items = xmlBaseModel.getItems();
                    if (items == null || items.size() == 0) {
                        return;
                    }
                    Constant.paramMap.put("sendCode", xmlBaseModel.getSendCode());
                    for (Map<String, Object> item : items) {
                        if (item.get("heart_beat_interval") != null) {
                            //心跳间隔
                            Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());
                        }
                        if (item.get("patroldevice_run_interval") != null) {
                            //巡视设备运行数据间隔间隔
                            Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());
                        }
                        if (item.get("weather_interval") != null) {
                            //微气象数据间隔
                            Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());
                        }
                        if (item.get("nest_run_interval") != null) {
                            //无人机巢运行数据间隔
                            Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());
                        }
                    }
                    //将数据放入redis 做个保存
                    redisTemplate.opsForHash().putAll("upSystemParameter", Constant.paramMap);
                    //心跳线程发心跳
                    HeartBeatThead heartBeatThead = new HeartBeatThead(clientHandler, true);
//                    //天气线程发天气
//                    WeatherThread weatherThread = new WeatherThread(clientHandler, redisTemplate, true, sendToUpSystemServices);
//                    //运行数据
//                    RunningThread runningThread = new RunningThread(clientHandler,redisTemplate,true, sendToUpSystemServices);
//                    // 无人机机巢数据线程
////                    NestRunThread nestRunThread = new NestRunThread(clientHandler,redisTemplate,true, sendToUpSystemServices);
////                    TaskExecutePool.getInstance().execute(nestRunThread);
//                    TaskExecutePool.getInstance().execute(runningThread);
                    TaskExecutePool.getInstance().execute(heartBeatThead);
//                    //江苏要求
//                    TaskExecutePool.getInstance().execute(weatherThread);

                    Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
                    List<XMLBaseModel> list = new ArrayList<>();
                    list.add(xmlBaseModel);
                    robotMap.put("list", list);
                    //国网要求
//                    Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);
                } else {
                    log.info("---注册响应--400拒绝注册----");
                    //注册拒绝 继续注册
                    registerManager.register(clientHandler);
                }

            }

            //响应心跳
            if ("3".equals(xmlBaseModel.getCommand())) {
                log.info("--心跳响应--");
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (items == null || items.size() == 0) {
                    return;
                }
                for (Map<String, Object> item : items) {
                    if (item.get("heart_beat_interval") != null) {
                        //心跳间隔
                        Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());
                    }
                    if (item.get("patroldevice_run_interval") != null) {
                        //巡视设备运行数据间隔间隔
                        Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());
                    }
                    if (item.get("weather_interval") != null) {
                        //微气象数据间隔
                        Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());
                    }
                    if (item.get("nest_run_interval") != null) {
                        //无人机巢运行数据间隔
                        Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());
                    }
                }
                //将数据放入redis 做个保存
                redisTemplate.opsForHash().putAll("upSystemParameter", Constant.paramMap);

                // 查询没有成功上报到上一级系统的巡视结果,若存在,重连后开始上报
//                sendToUpSystemServices.failReportCruiseResult();
            }
        }
        //控制消息
        if ("1".equals(xmlBaseModel.getType()) || "2".equals(xmlBaseModel.getType()) || "3".equals(xmlBaseModel.getType()) || "4".equals(
            xmlBaseModel.getType()) || "21".equals(xmlBaseModel.getType()) || "22".equals(xmlBaseModel.getType()) || "23".equals(xmlBaseModel.getType())) {
            log.info("--响应控制 控制下发--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            //国网要求
            Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);
            if (re == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else if (200 == re.getCode()) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            }
            log.info("==控制响应== {}", re);
        }
        //发给无人机
        if ("20001".equals(xmlBaseModel.getType()) || "20002".equals(xmlBaseModel.getType()) || "20003".equals(xmlBaseModel.getType())
            || "20004".equals(xmlBaseModel.getType()) || "20005".equals(xmlBaseModel.getType())) {
            // 无人机机巢控制参数校验
            if (StringUtils.equals("20005", xmlBaseModel.getType())){
                String value = String.valueOf(xmlBaseModel.getItems().get(0).get("value"));
                switch (xmlBaseModel.getCommand()){
                    case "1":
                    case "3":
                        if (!ArrayUtils.contains(new String[]{"1","2","3"}, value)){
                            sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "400", null, false);
                            return;
                        }
                        break;
                    case "2":
                        if (!ArrayUtils.contains(new String[]{"1","2"}, value)){
                            sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "400", null, false);
                            return;
                        }
                        break;
                    default:
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "400", null, false);
                        return;
                }
            }
            log.info("--响应控制 无人机控制下发--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            //国网要求
            Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);
            if (re == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else if (200 == re.getCode()) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            }
            log.info("==控制响应==" + re);
        }
        if ("41".equals(xmlBaseModel.getType())) {
            log.info("--响应控制--");
            Result re = new Result();
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            map.put("list", list);
            re = Constant.otherServer(map,Constant.TASK_STATE_URL);
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            if (StringUtils.equals("1", xmlBaseModel.getCommand())){
                item.put("task_patrolled_id", re.getData());
            }else{
                item.put("task_patrolled_id", xmlBaseModel.getCode());
            }
            items.add(item);
            if (re == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", items, false);
            } else if (200 == re.getCode()) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", items, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", items, false);
            }
            log.info("==任务控制响应==" + re);

        }

        if ("101".equals(xmlBaseModel.getType())) {
            log.info("--响应任务--");
            if ("1".equals(xmlBaseModel.getCommand())) {
                log.info("--响应任务 任务下发--");
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                // 如果从上级系统下发  点位为机器人的id 对应t_std_devicemete表中的device_point_id 需要转为 巡视系统的instanceId
                String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
                boolean standardPoints = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
                for (Map<String, Object> item : list) {
                    TCruiseTaskAdd tCruiseTaskAdd = covertBean(item, redisTemplate, false, edgeLevel);
                    if (standardPoints) {
                        List<String> instanceIds = sendToUpSystemServices.selectForTaskInstanceId(item.get("device_list").toString());
                        tCruiseTaskAdd.setDeviceList(StringUtils.join(instanceIds, ","));
                    }else {
                        tCruiseTaskAdd.setDeviceList(item.get("device_list").toString());
                    }
//                    tCruiseTaskAdd.setUnionTaskStatus("0");
                    tCruiseTaskAdd.setAreaId(xmlBaseModel.getSendCode());
                    log.info("101任务组装好发送platform:{}", tCruiseTaskAdd);
                    //统一调度分发主站平台下发的任务
//                    Result re = receivedTaskInfoHandler(sendToUpSystemServices, tCruiseTaskAdd, item);
                    Map<String, List<TCruiseTaskAdd>> map = new HashMap<>();
                    List<TCruiseTaskAdd> taskList = new ArrayList<>();
                    taskList.add(tCruiseTaskAdd);
                    map.put("list", taskList);
                    Result re = Constant.otherServer(map, Constant.TASK_ISSUE_URL);

                    if (re == null) {
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
                    } else if (200 == re.getCode()) {
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
                    } else {
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
                    }
                    log.info("--响应任务下发--" + re);
                }

            }
            else if ("102".equals(xmlBaseModel.getCommand())) {
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                if(CollectionUtils.isNotEmpty(list)) {
                    Map<String,Object> map = list.get(0);
                    String taskId = map.getOrDefault("taskId","").toString();
                    String startTime = map.getOrDefault("startTime","").toString();
                    String source = map.getOrDefault("source","").toString();
                    UPatrolTaskProxy uPatrolTaskProxy = StaticContextAccessor.getBean(UPatrolTaskProxy.class);
                    uPatrolTaskProxy.delete(taskId,startTime,source);
                }
            }
        }

        if ("102".equals(xmlBaseModel.getType())) {
            log.info("--响应联动任务--");
            if ("1".equals(xmlBaseModel.getCommand())) {
                //联动任务
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                // 如果使用标准点位  点位为机器人的id 需要转为 巡视系统的instanceId
                String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
                boolean standardPoints = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:standardPoints", "content"));
                for (Map<String, Object> item : list) {
                    String taskId = item.get("task_code").toString();
                    TCruiseTaskAdd tCruiseTaskAdd = covertBean(item, redisTemplate, true, edgeLevel);
                    boolean isRobotFlag = false;
                    boolean isEdgeFlag = false;
                    String robotDevice = "";
                    String edgeDevice = "";
                    List<String> insList = new ArrayList<>();
                    if (standardPoints) {
                        insList = sendToUpSystemServices.selectForTaskInstanceId(item.get("device_list").toString());
                        tCruiseTaskAdd.setDeviceList(StringUtils.join(insList, ","));
                    }else {
                        insList = Arrays.asList(item.get("device_list").toString());
                        tCruiseTaskAdd.setDeviceList(item.get("device_list").toString());
                    }
                    robotDevice = sendToUpSystemServices.selectIsRobotDevice(insList);
                    isRobotFlag = StringUtils.isNotEmpty(robotDevice);

                    edgeDevice = sendToUpSystemServices.selectIsDownSystem(insList);
                    isEdgeFlag = StringUtils.isNotEmpty(edgeDevice);

                    tCruiseTaskAdd.setUnionTaskStatus("1");
                    tCruiseTaskAdd.setAreaId(xmlBaseModel.getSendCode());
                    log.info("102联动任务组装好发送platform:{}", tCruiseTaskAdd);
                    //统一调度分发主站平台下发的任务
//                    Result re = receivedTaskInfoHandler(sendToUpSystemServices, tCruiseTaskAdd, item);
                    Map<String, List<TCruiseTaskAdd>> map = new HashMap<>();
                    List<TCruiseTaskAdd> taskList = new ArrayList<>();
                    taskList.add(tCruiseTaskAdd);
                    map.put("list", taskList);
                    Result re = Constant.otherServer(map, Constant.TASK_ISSUE_URL);
                    //非机器人的和下级系统的直接返回 机器人点的等待机器人返回结果 下级的等下级返回
                    log.info("platform响应结果:{}",re);
                    Constant.getParamMap.put("sendSessionId",sendSessionId);
                    if (!isRobotFlag && !isEdgeFlag) {
                        List<Map<String, Object>> xmlItems = new ArrayList<>();
                        Map<String, Object> xmlItem = new HashMap<>();
                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
                        if (re == null) {
                            xmlItem.put("error_code", "3");
                            xmlItem.put("task_patrolled_id", stationCode + "_" + taskId + "_" + simpleDateFormat2.format(new Date()));
                            xmlItems.add(xmlItem);
                            sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
                        } else if (200 == re.getCode()) {
                            String taskPatrolledId = String.valueOf(((LinkedHashMap<?, ?>) re.getData()).get("taskPatrolledId"));
                            xmlItem.put("task_patrolled_id", taskPatrolledId);
                            xmlItem.put("error_code", "0");
                            xmlItems.add(xmlItem);
                            sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
                        } else {
                            xmlItem.put("error_code", "1");
                            xmlItem.put("task_patrolled_id", stationCode + "_" + taskId + "_" + simpleDateFormat2.format(new Date()));
                            xmlItems.add(xmlItem);
                            sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
                        }
                    }
                    log.info("--联动任务响应--" + re);
                }

            }
        }

        if ("61".equals(xmlBaseModel.getType())) {
            try {
                log.info("--模型同步--");
                //1-巡视主机模型 设备模型及设备点位模型文件中应至少包括设备信息及巡视点位信息 A.2.3
                //2-机器人模型 巡视设备模型文件中应至少包括巡视主机、 智能分析主机、 机器人、 无人机、 高清视频、声纹的属性信息 A.2.4
                //3-摄像机模型 同上
                //4-点位模型 同上
                //5-无人机模型  同上
                //6-声纹模型  同上
                //7-任务文件  任务模型 A.2.5
                //8-检修区域配置文件 检修区域模型 A.2.6
                //9-地图文件
                List<Map<String, Object>> list = sendToUpSystemServices.creatModelList(xmlBaseModel.getCommand());
                String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
                String command = "4";
                if ("1".equals(edgeLevel)){
                    command = "3";
                }
                sendToUpSystemServices.sendResponse(sendSessionId, "251", command, "200", list, false);
            } catch (Exception e) {
                log.info("模型同步错误" + e);
                //                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list);
            }
        }

        //联动文件下发指令
        if ("71".equals(xmlBaseModel.getType())) {
            try {
                log.info("--联动文件下发指令--");
                Map<String, Object> item = xmlBaseModel.getItems().get(0);
                String filePath = String.valueOf(item.get("file_path"));
                if (StringUtils.isNotEmpty(filePath)) {
                    switch (xmlBaseModel.getCommand()) {
                        //<1>: =联动配置文件
                        case "1":
                            log.info("联动配置文件{}", filePath);
                            analysisUnionTaskFileService.handleUnionTaskFile(filePath);
                            break;
                        case "2":
                        case "3":
                            //<2>: =一键顺控视频确认反馈信息文件
                            //<3>: =反向联动信息转发文件
                            sendToUpSystemServices.dealLinkage(filePath);
                            break;
                        default:
                            break;
                    }
                } else {
                    log.error("联动文件下发 filePath is empty !!!");
                }
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } catch (Exception e) {
                log.info("联动文件下发指令" + e);
            }
        }

        if ("81".equals(xmlBaseModel.getType())) {
            log.info("--检修区域--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            Result re = Constant.otherServer(robotMap, Constant.MAINTENANCE_URL);//platfrom设置
            log.info("--响应检修--" + re);
            if (re == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else if (200 == re.getCode()) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            }

        }

        if ("121".equals(xmlBaseModel.getType())) {
            log.info("巡视结果统计查询：{}", JSON.toJSONString(xmlBaseModel));
            String startTime = "";
            String endTime = "";
            String type = "";
            List<Map<String, Object>> list = null;
            if (xmlBaseModel.getItems() != null && xmlBaseModel.getItems().size() > 0) {
                Map<String, Object> item = xmlBaseModel.getItems().get(0);
                item.put("cmd",xmlBaseModel.getCommand());
                list = sendToUpSystemServices.resultStatistical(item);
            }
            if (list == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "100", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list, false);
            }
        }

        // 相机预置位图片和ptz信息同步
        if ("2023".equals(xmlBaseModel.getType())) {
            StaticContextAccessor.getBean(TCameraPresetService.class).updateCameraPresetInfo(xmlBaseModel);
        }
    }

    /**
     *
     * @param item
     * @param linkage 是否为联动任务
     * @return
     */
    public static TCruiseTaskAdd covertBean(Map<String, Object> item, RedisTemplate redisTemplate, Boolean linkage, String edgeLevel){
        TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
        tCruiseTaskAdd.setTaskId(item.get("task_code").toString());
        tCruiseTaskAdd.setTaskName(item.get("task_name").toString());
        tCruiseTaskAdd.setCreateTime(item.containsKey("create_time") ? DateTimeUtil.getDate(item.get("create_time").toString()) : new Date());
        tCruiseTaskAdd.setType(item.containsKey("type") ? Integer.parseInt(item.get("type").toString()) : 1);
        tCruiseTaskAdd.setCycleExecuteTime(item.getOrDefault("cycle_execute_time", "").toString());
        tCruiseTaskAdd.setCycleMonth(item.getOrDefault("cycle_month", "").toString());
        tCruiseTaskAdd.setCycleWeek(item.getOrDefault("cycle_week", "").toString());
        tCruiseTaskAdd.setCycleEndTime(item.getOrDefault("cycle_end_time", "").toString());
        tCruiseTaskAdd.setCycleStartTime(item.getOrDefault("cycle_start_time", "").toString());
        tCruiseTaskAdd.setIntervalExecuteTime(item.getOrDefault("interval_execute_time", "").toString());
        tCruiseTaskAdd.setIntervalNumber(item.getOrDefault("interval_number", "").toString());
        tCruiseTaskAdd.setIntervalType(item.getOrDefault("interval_type", "").toString());
        tCruiseTaskAdd.setIntervalStartTime(item.getOrDefault("interval_start_time", "").toString());
        tCruiseTaskAdd.setIntervalEndTime(item.getOrDefault("interval_end_time", "").toString());

        String priority = item.getOrDefault("priority", "").toString();

        // 周期任务需要处理
        String cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime();
        if (StringUtils.isNotEmpty(cycleExecuteTime)){
            cycleExecuteTime = cycleExecuteTime.startsWith("0") ? cycleExecuteTime.substring(1, 2) : cycleExecuteTime.substring(0,2);
            tCruiseTaskAdd.setCycleExecuteTime(cycleExecuteTime);
        }
        String cycleWeek = tCruiseTaskAdd.getCycleWeek();
        StringJoiner str = new StringJoiner(",");
        if (StringUtils.isNotEmpty(cycleWeek)){
            String[] split = cycleWeek.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = String.valueOf((NumberUtils.toInt(split[i]) + 1) == 8 ? 1: (NumberUtils.toInt(split[i]) + 1));
                str.add(split[i]);
            }
            tCruiseTaskAdd.setCycleWeek(str.toString());
        }
        String level;
        //联动任务立即执行
        if (linkage) {
            level = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "904", "level");
            tCruiseTaskAdd.setIfRun(173);
            tCruiseTaskAdd.setStartTime(new Date());
        } else {
            level = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "902", "level");
            if (StringUtils.isNotEmpty(MapUtils.getString(item,"fixed_start_time"))) {
                long fixedStartTime = DateTimeUtil.parse(String.valueOf(item.get("fixed_start_time"))).getTime();
                log.info("fixedStartTime=={},当前时间:{}", fixedStartTime, System.currentTimeMillis());
                if (Math.abs(System.currentTimeMillis() - fixedStartTime) <= (60 * 1000)) {
                    // 立即(fixed_start_time和当前时间相差5min)
                    tCruiseTaskAdd.setIfRun(173);
                } else {
                    // 定期
                    tCruiseTaskAdd.setIfRun(174);
                }
                tCruiseTaskAdd.setStartTime(DateTimeUtil.getDate(item.get("fixed_start_time").toString()));
            } else {
                tCruiseTaskAdd.setIfRun(172);
                String startTime = StringUtils.isNotEmpty(tCruiseTaskAdd.getCycleStartTime()) ? tCruiseTaskAdd.getCycleStartTime() : tCruiseTaskAdd.getIntervalStartTime();
                tCruiseTaskAdd.setStartTime(DateTimeUtil.getDate(startTime));
            }
        }

        priority = !StringUtils.equalsAny(priority, "1", "2", "3", "4")  || "2".equals(edgeLevel) ? level : priority;
        tCruiseTaskAdd.setTaskLevel(NumberUtils.toInt(priority));

        return tCruiseTaskAdd;
    }

    private static Result receivedTaskInfoHandler(SendToUpSystemServices sendToUpSystemServices, TCruiseTaskAdd tCruiseTaskAdd, Map<String, Object> item){
        String deviceIds = tCruiseTaskAdd.getDeviceList();
        List<Long> allList = new ArrayList<>();
        Result re = null;

        String[] listArray = deviceIds.split(",");
        for (int i = 0; i < listArray.length; i++) {
            allList.add(Long.valueOf(listArray[i]));
        }
        List<TCruisePointInstanceNameDetail> taskPoints = sendToUpSystemServices.selectForTask(allList);

        Map<Boolean,List<TCruisePointInstanceNameDetail>> edgePartlyMap=taskPoints
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(tCruisePointInstanceNameDetail -> tCruisePointInstanceNameDetail.getEdgeCode()==null));

        List<TCruisePointInstanceNameDetail> edgePointElement = edgePartlyMap.get(false);
        List<TCruisePointInstanceNameDetail> nullEdgePointElement = edgePartlyMap.get(true);

        List<Long> regionRobotInstances = nullEdgePointElement
                .stream()
                .filter(Objects::nonNull)
                .filter(cruiseInfo->cruiseInfo.getCruiseType()==228 || cruiseInfo.getCruiseType()==524)
                .map(TCruisePointInstanceNameDetail::getInstanceId)
                .collect(Collectors.toList());

        List<Long> regionRobotInspections = nullEdgePointElement
                .stream()
                .filter(Objects::nonNull)
                .filter(cruiseInfo->cruiseInfo.getCruiseType()==228 || cruiseInfo.getCruiseType()==524)
                .map(TCruisePointInstanceNameDetail::getCruiseId)
                .collect(Collectors.toList());

        List<Long> regionDetectiveDevicePoints = nullEdgePointElement.stream()
                .filter(cruiseInfo->cruiseInfo.getCruiseType()!=228 && cruiseInfo.getCruiseType()!=524)
                .map(TCruisePointInstanceNameDetail::getInstanceId)
                .collect(Collectors.toList());

        if(regionRobotInstances.size()!=0){
            log.info("任务分发：上层平台-->巡视主机机器人");
            try {
                re = sendTaskToRegionRobot(sendToUpSystemServices, tCruiseTaskAdd, item, regionRobotInspections, regionRobotInstances);
            }catch (Exception e){
                log.error("站端平台任务分发至机器人操作失败:",e);
            }
        }
        if(regionDetectiveDevicePoints.size()!=0){
            log.info("任务分发：上层平台-->巡视主机");
            try {
                re = sendTaskToRegionSystem(tCruiseTaskAdd, regionDetectiveDevicePoints);
            }catch (Exception e){
                log.error("站端平台任务分发至区域巡视主机操作失败:",e);
            }
        }
        if(edgePointElement.size()!=0){
            log.info("任务分发：上层平台-->巡视主机-->边缘节点");
            try {
                re = sendTaskToEdgeNode(tCruiseTaskAdd, item, edgePointElement);
            }catch (Exception e){
                log.error("站端平台任务分发至边缘节点操作失败:",e);
            }
        }

        return re;
    }

    private static Result sendTaskToRegionSystem(TCruiseTaskAdd tCruiseTaskAdd,
                                                 List<Long> regionDetectiveDevicePoints) throws Exception{
        String regionDeviceList = "";
        for(Long deviceId: regionDetectiveDevicePoints){
            if(deviceId != null){
                regionDeviceList = regionDeviceList+ deviceId.toString()+",";
            }
        }
        regionDeviceList = regionDeviceList.substring(0,regionDeviceList.length()-1);

        Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
        List<TCruiseTaskAdd> taskList = new ArrayList<>();
        taskList.add(tCruiseTaskAdd.setDeviceList(regionDeviceList));
        map.put("list",taskList);
        return Constant.otherServer(map,Constant.TASK_ISSUE_URL);
    }

    private static Result sendTaskToRegionRobot(SendToUpSystemServices sendToUpSystemServices,
                                                TCruiseTaskAdd tCruiseTaskAdd,
                                                Map<String, Object> item,
                                                List<Long> regionRobotInspections,
                                                List<Long> regionRobotInstances) throws Exception{
        List<String> robotCodes = sendToUpSystemServices.selectForRobotTask(regionRobotInspections);
        List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();
        if(robotCodes==null || robotCodes.size()==0){
            log.warn("未找到对应的robotCode");
            return null;
        }
        for(String robotCode:robotCodes){
            List<Long> individualDeviceIds = sendToUpSystemServices.selectRobotTaskInstanceId(regionRobotInstances, robotCode);
            RobotTaskInstanceInfo robotTaskInstanceInfo = new RobotTaskInstanceInfo()
                    .setCruiseType(tCruiseTaskAdd.getType())
                    .setTaskId(tCruiseTaskAdd.getTaskId())
                    .setIfRun(Objects.nonNull(tCruiseTaskAdd.getIfRun())?tCruiseTaskAdd.getIfRun().toString():null)
                    .setRobotCode(robotCode)
                    .setPriority(tCruiseTaskAdd.getTaskLevel())
                    .setTaskName(tCruiseTaskAdd.getTaskName())
                    .setInstanceList(individualDeviceIds)
                    .setFixedStartTime(String.valueOf(item.get("fixed_start_time")))
                    .setCycleMonth(String.valueOf(item.get("cycle_month")))
                    .setCycleWeek(String.valueOf(item.get("cycle_week")))
                    .setCycleExecuteTime(String.valueOf(item.get("cycle_execute_time")))
                    .setIntervalType(String.valueOf(item.get("interval_type")))
                    .setIntervalNumber(String.valueOf(item.get("interval_number")))
                    .setIntervalExecuteTime(String.valueOf(item.get("interval_execute_time")))
                    .setCycleStartTime(String.valueOf(item.get("cycle_start_time")))
                    .setCycleEndTime(String.valueOf(item.get("cycle_end_time")))
                    .setIntervalStartTime(String.valueOf(item.get("interval_start_time")))
                    .setIntervalEndTime(String.valueOf(item.get("invalid_end_time")));

            robotTaskInfoList.add(robotTaskInstanceInfo);
        }
        Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(3);
        robotTaskInfoMap.put("robotTaskInfoList", robotTaskInfoList);
        log.info("robotTaskInfoMap = {}", robotTaskInfoMap);

        // 调用robot服务下发任务
        return Constant.otherServer(robotTaskInfoMap, Constant.ROBOT_TASK_ISSUE_URL);

    }

    private static Result sendTaskToEdgeNode(TCruiseTaskAdd tCruiseTaskAdd,
                                             Map<String, Object> item,
                                             List<TCruisePointInstanceNameDetail> edgePointElement) throws Exception{
        List<RobotTaskInstanceInfo> edgeTaskInfoList = new ArrayList<>();

        Map<Long,List<TCruisePointInstanceNameDetail>> allNodePoints = edgePointElement.stream()
                .collect(Collectors.groupingBy(TCruisePointInstanceNameDetail::getEdgeCode));

        Set<Long> edgeCodeSet = allNodePoints.keySet();
        log.info("目标边缘节点对象集：{}",edgeCodeSet);
        for(Long edgeCode : edgeCodeSet){
            List<Long> individualDeviceIds = allNodePoints.get(edgeCode).stream()
                    .map(TCruisePointInstanceNameDetail::getInstanceId)
                    .collect(Collectors.toList());
            RobotTaskInstanceInfo edgeTaskInfo = new RobotTaskInstanceInfo()
                    .setCruiseType(tCruiseTaskAdd.getType())
                    .setTaskId(tCruiseTaskAdd.getTaskId())
                    .setIfRun(Objects.nonNull(tCruiseTaskAdd.getIfRun())?tCruiseTaskAdd.getIfRun().toString():null)
                    .setEdgeCode(edgeCode.toString())
                    .setPriority(tCruiseTaskAdd.getTaskLevel())
                    .setTaskName(tCruiseTaskAdd.getTaskName())
                    .setInstanceList(individualDeviceIds)
                    .setFixedStartTime(String.valueOf(item.get("fixed_start_time")))
                    .setCycleMonth(String.valueOf(item.get("cycle_month")))
                    .setCycleWeek(String.valueOf(item.get("cycle_week")))
                    .setCycleExecuteTime(String.valueOf(item.get("cycle_execute_time")))
                    .setIntervalType(String.valueOf(item.get("interval_type")))
                    .setIntervalNumber(String.valueOf(item.get("interval_number")))
                    .setIntervalExecuteTime(String.valueOf(item.get("interval_execute_time")))
                    .setCycleStartTime(String.valueOf(item.get("cycle_start_time")))
                    .setCycleEndTime(String.valueOf(item.get("cycle_end_time")))
                    .setIntervalStartTime(String.valueOf(item.get("interval_start_time")))
                    .setIntervalEndTime(String.valueOf(item.get("invalid_end_time")));;

            edgeTaskInfoList.add(edgeTaskInfo);

        }
        Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(3);
        robotTaskInfoMap.put("robotTaskInfoList", edgeTaskInfoList);
        log.info("robotTaskInfoMap = {}", robotTaskInfoMap);

        // 调用robot服务下发任务
        return Constant.otherServer(robotTaskInfoMap, Constant.ROBOT_TASK_ISSUE_URL);
    }

}
