/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty.server;

import com.alibaba.fastjson.JSON;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.TCruiseTaskAdd;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.thread.MyThreadFactory;
import com.yjh.accesstcp.thread.TaskExecutePool;
import com.yjh.accesstcp.thread.WeatherThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            new ThreadPoolExecutor.DiscardPolicy());

    public static void doProcessMessage(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
        SendToUpSystemServices sendToUpSystemServices, RedisTemplate redisTemplate) {
        executorService.execute(() -> {
            try {
                processMessage(xmlBaseModel, sendSessionId, clientHandler, sendToUpSystemServices, redisTemplate);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public static void doProcessMessageSync(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
        SendToUpSystemServices sendToUpSystemServices, RedisTemplate redisTemplate) {
        try {
            processMessage(xmlBaseModel, sendSessionId, clientHandler, sendToUpSystemServices, redisTemplate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void processMessage(XMLBaseModel xmlBaseModel, long sendSessionId, TCPClientHandler clientHandler,
        SendToUpSystemServices sendToUpSystemServices, RedisTemplate redisTemplate) throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //解析的xml文件
        if ("251".equals(xmlBaseModel.getType())) {//系统消息
            if ("4".equals(xmlBaseModel.getCommand())) {//响应注册
                log.info("---注册响应---");
                if ("100".equals(xmlBaseModel.getCode())) {
                    //需要重发注册消息
                    clientHandler.sendRegister();
                } else if ("200".equals(xmlBaseModel.getCode())) {
                    //服务端响应 我方开启心跳

                    List<Map<String, Object>> items = xmlBaseModel.getItems();
                    if (items == null || items.size() == 0) {
                        return;
                    }
                    Constant.paramMap.put("sendCode", xmlBaseModel.getSendCode());
                    for (Map<String, Object> item : items) {
                        if (item.get("heart_beat_interval") != null) {
                            Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                        }
                        if (item.get("patroldevice_run_interval") != null) {
                            Constant.paramMap.put("patroldevice_run_interval",
                                item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                        }
                        if (item.get("weather_interval") != null) {
                            Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                        }
                        if (item.get("nest_run_interval") != null) {
                            Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());//无人机巢运行数据间隔
                        }
                    }
                    //将数据放入redis 做个保存
                    redisTemplate.opsForHash().putAll("upSystemParameter", Constant.paramMap);
                    //心跳线程发心跳
                    HeartBeatThead heartBeatThead = new HeartBeatThead(clientHandler, true);
                    //                    //天气线程发天气
                    WeatherThread weatherThread = new WeatherThread(clientHandler, redisTemplate, true, sendToUpSystemServices);
                    //                    //运行数据
                    //
                    TaskExecutePool.getInstance().execute(heartBeatThead);
                    TaskExecutePool.getInstance().execute(weatherThread);//江苏要求

                    Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
                    List<XMLBaseModel> list = new ArrayList<>();
                    list.add(xmlBaseModel);
                    robotMap.put("list", list);
                    Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);//国网要求
                } else {
                    return;
                }

            }

            if ("3".equals(xmlBaseModel.getCommand())) {//响应心跳
                log.info("--心跳响应--");
                List<Map<String, Object>> items = xmlBaseModel.getItems();
                if (items == null || items.size() == 0) {
                    return;
                }
                for (Map<String, Object> item : items) {
                    if (item.get("heart_beat_interval") != null) {
                        Constant.paramMap.put("heart_beat_interval", item.get("heart_beat_interval").toString());//心跳间隔
                    }
                    if (item.get("patroldevice_run_interval") != null) {
                        Constant.paramMap.put("patroldevice_run_interval", item.get("patroldevice_run_interval").toString());//巡视设备运行数据间隔间隔
                    }
                    if (item.get("weather_interval") != null) {
                        Constant.paramMap.put("weather_interval", item.get("weather_interval").toString());//微气象数据间隔
                    }
                    if (item.get("nest_run_interval") != null) {
                        Constant.paramMap.put("nest_run_interval", item.get("nest_run_interval").toString());//无人机巢运行数据间隔
                    }
                }
                //将数据放入redis 做个保存
                redisTemplate.opsForHash().putAll("upSystemParameter", Constant.paramMap);
            }
        }
        if ("1".equals(xmlBaseModel.getType()) || "2".equals(xmlBaseModel.getType()) || "3".equals(xmlBaseModel.getType()) || "4".equals(
            xmlBaseModel.getType()) || "21".equals(xmlBaseModel.getType()) || "22".equals(xmlBaseModel.getType())) {//控制消息
            log.info("--响应控制 控制下发--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);//国网要求
            if (re == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else if (200 == re.getCode()) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "3", "200", null, false);
            }
            log.info("==控制响应== {}", re);
        }
        if ("20001".equals(xmlBaseModel.getType()) || "20002".equals(xmlBaseModel.getType()) || "20003".equals(xmlBaseModel.getType())
            || "20004".equals(xmlBaseModel.getType()) || "20005".equals(xmlBaseModel.getType())) {
            //发给无人机
            log.info("--响应控制 无人机控制下发--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            Result re = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);//国网要求
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
            //re = Constant.otherServer(map,Constant.TASK_STATE_URL);//江苏要求
            re = Constant.otherServer(map, Constant.ROBOT_TASK_URL);//国网要求
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            if (StringUtils.equals("1", xmlBaseModel.getCommand())){
                String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                item.put("task_patrolled_id", xmlBaseModel.getCode() + "_" + time);
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
                for (Map<String, Object> item : list) {
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String type = item.get("type").toString();
                    String device_list = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(device_list);
                    if (type != null && "1".equals(type)) {
                        type = "214";
                    }
                    if (type != null && "2".equals(type)) {
                        type = "216";
                    }
                    if (type != null && "3".equals(type)) {
                        type = "217";
                    }
                    if (type != null && "4".equals(type)) {
                        type = "218";
                    }
                    tCruiseTaskAdd.setType(Integer.valueOf(type));
                    tCruiseTaskAdd.setTaskId(item.get("task_code").toString());
                    tCruiseTaskAdd.setTaskName(item.get("task_name").toString());
                    if (item.get("priority") != null && "".equals(item.get("priority"))) {
                        tCruiseTaskAdd.setTaskLevel(Integer.valueOf(item.get("priority").toString()));
                    } else {
                        tCruiseTaskAdd.setTaskLevel(2);
                    }

                    if (item.get("fixed_start_time") != null || "".equals(item.get("fixed_start_time"))) {
                        tCruiseTaskAdd.setIfRun(172);
                        StringBuilder stringBuilder = new StringBuilder("0 0");
                        String interval_type = item.get("interval_type").toString();
                        if ("1".equals(interval_type)) {
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0/" + interval_number + " ?");
                            String cycle_month = item.get("cycle_month").toString();
                            if ("".equals(cycle_month) || null == cycle_month) {
                                stringBuilder.append(" ?");
                            } else {
                                stringBuilder.append(" " + cycle_month);
                            }
                            String cycle_week = item.get("cycle_week").toString();
                            if ("".equals(cycle_week) || null == cycle_week) {
                                stringBuilder.append(" ?");
                            } else {
                                stringBuilder.append(" " + cycle_week);
                            }
                        } else if ("2".equals(interval_type)) {
                            String interval_number = item.get("interval_number").toString();
                            stringBuilder.append(" 0 1/" + interval_number);
                            String cycle_month = item.get("cycle_month").toString();
                            if ("".equals(cycle_month) || null == cycle_month) {
                                stringBuilder.append(" ?");
                            } else {
                                stringBuilder.append(" " + cycle_month);
                            }
                            String cycle_week = item.get("cycle_week").toString();
                            stringBuilder.append(" ?");
                        }
                        tCruiseTaskAdd.setDateType(stringBuilder.toString());
                    } else {
                        tCruiseTaskAdd.setIfRun(174);
                    }
                    tCruiseTaskAdd.setStartTime(simpleDateFormat.parse(item.get("fixed_start_time").toString()));

                    //                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
                    //                    List<TCruiseTaskAdd> taskList = new ArrayList<>();
                    //                    taskList.add(tCruiseTaskAdd);
                    //                    map.put("list",taskList);
                    //Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);//江苏要求
                    Map<String, List<XMLBaseModel>> map = new HashMap<>();
                    List<XMLBaseModel> taskList = new ArrayList<>();
                    taskList.add(xmlBaseModel);
                    map.put("list", taskList);
                    Result re = Constant.otherServer(map, Constant.ROBOT_TASK_URL);//国网要求
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
        }

        if ("102".equals(xmlBaseModel.getType())) {
            log.info("--响应联动任务--");
            if ("1".equals(xmlBaseModel.getCommand())) {
                //联动任务
                List<Map<String, Object>> list = xmlBaseModel.getItems();
                for (Map<String, Object> item : list) {
                    TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
                    String taskId = item.get("task_code").toString();
                    tCruiseTaskAdd.setTaskId(taskId);
                    String taskName = item.get("task_name").toString();
                    tCruiseTaskAdd.setTaskName(taskName);
                    String taskLevel = item.get("priority").toString();
                    tCruiseTaskAdd.setTaskLevel(Integer.valueOf(taskLevel));
                    String deviceList = item.get("device_list").toString();
                    tCruiseTaskAdd.setDeviceList(deviceList);
                    tCruiseTaskAdd.setIfRun(173);
                    tCruiseTaskAdd.setStartTime(new Date());

                    //                    Map<String,List<TCruiseTaskAdd>> map = new HashMap<>();
                    //                    List<TCruiseTaskAdd> listTask = new ArrayList<>();
                    //                    listTask.add(tCruiseTaskAdd);
                    //                    map.put("list",listTask);
                    //Result re = Constant.otherServer(map,Constant.TASK_ISSUE_URL);//江苏要求
                    Map<String, List<XMLBaseModel>> map = new HashMap<>();
                    List<XMLBaseModel> taskList = new ArrayList<>();
                    taskList.add(xmlBaseModel);
                    map.put("list", taskList);
                    Result re = Constant.otherServer(map, Constant.ROBOT_TASK_URL);//国网要求
                    List<Map<String, Object>> xmlItems = new ArrayList<>();
                    Map<String, Object> xmlItem = new HashMap<>();
                    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                    if (re == null) {
                        xmlItem.put("error_code", "3");
                        xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat2.format(new Date()));
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
                    } else if (200 == re.getCode()) {
                        xmlItem.put("task_patrolled_id", re.getData());
                        xmlItem.put("error_code", "0");
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
                    } else {
                        xmlItem.put("error_code", "1");
                        xmlItem.put("task_patrolled_id", taskId + "_" + simpleDateFormat2.format(new Date()));
                        xmlItems.add(xmlItem);
                        sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", xmlItems, false);
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
                Map<String, Object> map = sendToUpSystemServices.creatModel(xmlBaseModel.getCommand());
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(map);
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list, false);
            } catch (Exception e) {
                log.info("模型同步错误" + e);
                //                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list);
            }
        }

        if ("81".equals(xmlBaseModel.getType())) {
            log.info("--检修区域--");
            Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            robotMap.put("list", list);
            Result re = Constant.otherServer(robotMap, Constant.MAINTENANCE_URL);//platfrom设置
            Result re2 = Constant.otherServer(robotMap, Constant.ROBOT_TASK_URL);//下发给机器人
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
            if (xmlBaseModel.getItems() != null && xmlBaseModel.getItems().size() > 0) {
                List<Map<String, Object>> itemsList = xmlBaseModel.getItems();
                for (Map<String, Object> item : itemsList) {
                    if (item.get("begin_time") != null) {
                        startTime = item.get("begin_time").toString();
                    }
                    if (item.get("end_time") != null) {
                        endTime = item.get("end_time").toString();
                    }
                }
            }
            List<Map<String, Object>> list = sendToUpSystemServices.resultStatistical(xmlBaseModel.getCommand(), startTime, endTime);
            if (list == null) {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "100", null, false);
            } else {
                sendToUpSystemServices.sendResponse(sendSessionId, "251", "4", "200", list, false);
            }
        }

    }

}
