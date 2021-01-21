package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.logs.Logs;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.dao.SysUserDao;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    private static final String TIMEFORMATTPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Resource
    private SysUserDao sysUserDao;

    @Logs(title = "巡视主机向机器人下发控制指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> feignRobotControl(String robotCode, String type, String command, String value, String direction, String key, Long userId, String password) {
        Map scmap = new HashMap();
        if (command.equals("1") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("3") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("5") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("6") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("9") && type.equals("3")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("8") && type.equals("22")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password);
            if (zcz.get("code") != null) {
                return zcz;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        List<Map<String, Object>> Item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>();
        if (!"".equals(value) || null != value) {
            map.put("value", value);
        }
        if (!"".equals(direction) || null != direction) {
            map.put("direction", direction);
        }
        Item.add(map);

        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Server01")
                .setReceiveCode(robotCode)
                .setCode("省检018")
                .setTime(sdf.format(new Date()))
                .setType(type)
                .setCommand(command)
                .setItems(Item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
        //根据不同的机器人对应不同的管道发送指令
        RobotServerHandler.getRobotServerHandlerMap().get(robotCode).SendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        scmap.put("code", 3);
        scmap.put("result", "success");
        return scmap;

    }

    @Logs(title = "巡视主机向机器人下发模型同步指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public boolean feignRobotTransfer(String robotCode) throws Exception {
        boolean res = false;
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Server01")
                .setReceiveCode(robotCode)
                .setCode("省检018")
                .setTime(sdf.format(new Date()))
                .setType("61")
                .setCommand("1");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人模型同步调用xml是<start>" + xmlString + "<end>");
        Constant.flag = 0;
        RobotServerHandler sendId = RobotServerHandler.getRobotServerHandlerMap().get(robotCode);
        log.info("sendId是<start>" + sendId + "<end>");
        if (sendId != null) {
            sendId.SendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        }
        Thread.sleep(500);
        if (Constant.flag == 1) {
            res = true;
        }
        return res;
    }

    //生成发送byte指令，附带测试
    public byte[] generateByteOrder(String xmlString, String robotCode) {
        long sendSessionId = Constant.sendSessionId;
        RobotServerHandler sendId = RobotServerHandler.getRobotServerHandlerMap().get(robotCode);
        log.info("sendId是<start>" + sendId + "<end>");
        if (sendId != null) {
            sendSessionId = sendSessionId + 1L;//请求报文每次累加1
            Constant.sendSessionId = sendSessionId;//刷新sendSessionId
        } else {
            Constant.sendSessionId = 0L;//刷新sendSessionId
        }
        log.info("发送会话序列号<start>" + sendSessionId + "<end>");
        byte[] requestProtocol = PlatformPacketUtil.createPacket(sendSessionId, 0, true, xmlString);//生成发送的报文

        StringBuilder Str = new StringBuilder();
        for (byte byteitem : requestProtocol) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("发送给机器人的指令是<start>" + Str + "<end>");
        return requestProtocol;
    }

    @Logs(title = "机器人在线状态更新", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int updateRobotInfo(String robotCode, String robotStatus) {
        log.info("机器人code是:" + robotCode);
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        log.info("机器人的id是：" + robotId);
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus(robotStatus);
        int res = tRobotInfoDao.update(tRobotInfo);
        return res;
    }

    @Logs(title = "机器人模型同步到数据库", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int robotFileIntoDB(List<Map<String, Object>> deviceMapList, List<Map<String, Object>> robotMap, XMLBaseModel xmlBaseModel) {
        //从缓存中获取系统参数
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        //机器人模型文件
        String picPath = filePathMap.get("content") + "/" + robotMap.get(0).get("mappath").toString();
        log.info("图片路径为：" + picPath);
        /*
         * 将ftp图copy到40环境
         * */
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

        String fenGe[] = picPath.split("/");
        String fileName = fenGe[fenGe.length - 1];
        String developMap = absoluteImgMap.get("content") + "/Map";
        File f = new File(developMap);
        if (!f.exists()) {
            f.setWritable(true, false);
            f.mkdirs();
        }

        try {
            String url = "cp " + picPath + " " + developMap;
            log.info("url是===" + url);
            Runtime.getRuntime().exec(url);
        } catch (Exception e) {
            e.getMessage();
        }
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;

        log.info("机器人code是:" + xmlBaseModel.getSendCode());
        Long robotId = tRobotInfoDao.selectRobotIdByCode(xmlBaseModel.getSendCode());
        log.info("机器人的id是：" + robotId);

        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setPhotePath(developRelativeUrl);
        log.info("获得的tRobotInfo是：" + tRobotInfo);

        //更新机器人地图信息
        tRobotInfoDao.update(tRobotInfo);
        //设备模型文件
        List<TRobotInspection> deviceList = new ArrayList<>();
        for (Map<String, Object> deviceMap : deviceMapList) {
            TRobotInspection tRobotInspection = new TRobotInspection()
                    .setInspectionCode(deviceMap.get("device_id").toString())
                    .setRobotId(robotId)
                    .setInspectionName(deviceMap.get("device_name").toString());
            deviceList.add(tRobotInspection);
        }
        log.info("获得的deviceList是：" + deviceList);

        //对测点数据进行相应的处理
        List<String> nowList = tRobotInspectionDao.selectAllByRobotId(robotId);//该机器人现有的巡检点
        log.info("机器人现有的deviceList是：" + nowList);

        List<TRobotInspection> addList = new ArrayList<>();
        for (TRobotInspection str : deviceList) {
            if (nowList.contains(str.getInspectionCode())) {
                nowList.remove(str.getInspectionCode());
            } else {
                addList.add(str);
            }

        }
        log.info("最后要插库的deviceList是===" + addList);
        log.info("准备要删除的inspectionCodeList是===" + nowList);
        if (nowList != null && nowList.size() > 0) {
            List<Long> inspectionIdList = tRobotInspectionDao.selectInspectionIdList(nowList);
            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
            List<Long> instanceIdList = tRobotInspectionDao.selectInstanceIdList(inspectionIdList);
            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

            int res1 = tRobotInspectionDao.batchDeleteTRobotInspection(inspectionIdList); //删库TRI
            int res2 = tRobotInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);//删库TCPI
            int res3 = tRobotInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);//删库TCPA
            log.info("TRI删除条数==" + res1 + ",TCPI删除条数==" + res2 + ",TCPA删除条数==" + res3);
        }
        if (addList != null && addList.size() > 0) {
            //插库TRI
            int res = tRobotInspectionDao.batchInsertTRobotInspection(addList);
            log.info("TRI插入条数==" + res);
        }
        if (deviceList.containsAll(addList)) {
            deviceList.removeAll(addList);
        }
        System.out.println("准备更新的deviceList是==" + deviceList);
        for (TRobotInspection tRobotInspection : deviceList) {
            tRobotInspectionDao.update(tRobotInspection);//更新TRI
        }

        return 1;
    }

    @Logs(title = "巡视主机向机器人下发任务指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int feignRobotTaskIssued(Map<String, List<RobotTaskInstanceInfo>> ItemMap) {
        log.info("传来的ItemMap是==" + ItemMap);
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);

        if (null != ItemMap) {
            List<RobotTaskInstanceInfo> rTIIList = ItemMap.get("robotTaskInfoList");
            List<Map<String, String>> redisInfoList = new ArrayList<>();//缓存信息List
            log.info("rTIIList是===" + rTIIList);
            for (RobotTaskInstanceInfo rTII : rTIIList) {
                //巡视类型
                Integer planType = null;
                switch (rTII.getCruiseType()) {
                    case 213://全面
                        planType = 1;
                        break;
                    case 214://例行
                        planType = 2;
                        break;
                    case 215://熄灯
                        planType = 3;
                        break;
                    case 216://特殊
                        planType = 4;
                        break;
                    case 217://专项
                        planType = 3;
                        break;
                    case 218://自定义
                        planType = 3;
                        break;
                }
                //根据instanceIdList查询inspectionCodeList
                StringJoiner str = new StringJoiner(",");
                List<Long> instanceIdList = rTII.getInstanceList();
                log.info("instanceIdList是===" + instanceIdList);

                //将instanceIdList放缓存，以备后续使用
                Map<String, Object> instanceListMap = new HashMap<>();
                instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
                instanceListMap.put("taskId", rTII.getTaskId());
                redisTemplate.opsForHash().putAll("RobotTaskStatus:" + rTII.getRobotCode(), instanceListMap);

                for (Long instanceId : instanceIdList) {
                    String inspectionCode = tRobotInfoDao.selectInspectionCode(instanceId);
                    str.add(inspectionCode);

                    Map<String, String> redisInfoMap = new HashMap<>();//缓存信息map
                    redisInfoMap.put("robotCode", rTII.getRobotCode());//缓存中放robotCode
                    redisInfoMap.put("instanceId", instanceId + "");//缓存中放instanceId
                    redisInfoMap.put("inspectionCode", inspectionCode);//缓存中放inspectionCode
                    redisInfoMap.put("cruiseTime", sdf.format(new Date()));//缓存中放cruiseTime
                    redisInfoMap.put("taskId", rTII.getTaskId());//缓存中放taskId
                    redisInfoList.add(redisInfoMap);//缓存信息List添加数据
                }

                log.info("redisInfoList是===" + redisInfoList);
                //放数据到缓存
                for (int i = 0; i < redisInfoList.size(); i++) {
                    redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + redisInfoList.get(i).get("robotCode")
                            + ":" + redisInfoList.get(i).get("taskId") + i, redisInfoList.get(i));
                }

                String deviceIdList = str.toString();
                log.info("deviceIdList是==" + deviceIdList);
                //组装任务下发的item
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("type", planType);//巡视类型
                map.put("task_code", rTII.getTaskId());//任务编码
                map.put("task_name", rTII.getTaskName());//任务名称
                map.put("priority", rTII.getPriority());//优先级
                map.put("device_level", rTII.getDeviceLevel());//设备层级
                map.put("device_list", deviceIdList);//设备列表
                map.put("fixed_start_time", sdf.format(new Date()));//定期开始时间
//                map.put("cycle_month", "周期（月）");
//                map.put("cycle_week", "周期（周）");
//                map.put("cycle_execute_time", "周期（执行时间） ");
//                map.put("cycle_start_time", "周期开始时间 ");
//                map.put("cycle_end_time", "周期开始时间 ");
//                map.put("interval_number", "间隔（数量）");
//                map.put("interval_type", "间隔（类型） ");
//                map.put("interval_execute_time", "间隔（执行时间）");
//                map.put("interval_start_time", "间隔开始时间");
//                map.put("interval_end_time", "间隔结束时间 ");
//                map.put("invalid_start_time", "不可用开始时间 ");
//                map.put("invalid_end_time", "不可用结束时间");
//                map.put("isenable", "是否可用");
//                map.put("creator", "编制人");
//                map.put("create_time", "编制时间");
                mapList.add(map);
                log.info("任务下发的item是：" + mapList);

                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("101")
                        .setSendCode("Server01")
                        .setReceiveCode(rTII.getRobotCode())
                        .setCode("省检018")
                        .setTime(sdf.format(new Date()))
                        .setCommand("1")
                        .setItems(mapList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的机器人下发任务的xml是<start>" + xmlString + "<end>");

                //根据不同的机器人对应不同的管道发送指令
                RobotServerHandler.getRobotServerHandlerMap().get(rTII.getRobotCode()).SendHeartBeat(generateByteOrder(xmlString, rTII.getRobotCode()), rTII.getRobotCode());

                //启动一个线程
//                Map<String,String> threadMap = new HashMap<>();
//                threadMap.put("instanceIdList",instanceIdList.toString());
//                threadMap.put("taskCode",rTII.getTaskId());
//                threadMap.put("robotCode",rTII.getRobotCode());
//                Constant.flagMap.put(rTII.getTaskId(),0);
//
//                OneDealThread oneDealThread = new OneDealThread(threadMap,redisTemplate);
//                TaskExecutePool.getInstance().execute(oneDealThread);

            }

        }
        return 1;
    }

    @Logs(title = "巡视主机向机器人下发任务控制指令接口", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int feignRobotTaskControl(Map<String, Object> robotTaskControlMap) throws Exception {
        log.info("robotTaskControlMap===" + robotTaskControlMap);

        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        String taskId = robotTaskControlMap.get("taskId").toString();
        //1.任务启动2.任务暂停3.任务继续4.任务停止
        String commandValue = robotTaskControlMap.get("commandValue").toString();
        String json = JSONObject.toJSONString(robotTaskControlMap.get("robotCodeList"));
        log.info("判断条件是===" + (!"null".equals(json)));
        if (!"null".equals(json)) {
            List<String> robotCodeList = JSON.parseArray(json, String.class);
            log.info("robotCodeList===" + robotCodeList);

            List<Map<String, String>> redisInfoList = new ArrayList<>();//缓存信息List

            for (String robotCode : robotCodeList) {
                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("41")
                        .setSendCode("Server01")
                        .setReceiveCode(robotCode)
                        .setCode(taskId)
                        .setCommand(commandValue)
                        .setTime(sdf.format(new Date()));
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的任务控制xml是<start>" + xmlString + "<end>");

                //根据不同的机器人对应不同的管道发送指令
                RobotServerHandler.getRobotServerHandlerMap().get(robotCode).SendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);

                if (commandValue.equals("2") || commandValue.equals("4")) {
                    insertForPause(robotCode, taskId, Integer.valueOf(commandValue));
                }
            }
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertForPause(String robotCode, String taskId, Integer taskStatus) throws Exception {

        //统计巡视主机下发给机器人的巡检点大小
        Map<String, String> redisInfoMap2 = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode);
        String instanceList = (String) redisInfoMap2.get("instanceIdList");
        instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
        String[] instanceIdArray = instanceList.split(", ");
        List<String> instanceIdList = new ArrayList<>();
        for (String i : instanceIdArray) {
            instanceIdList.add(i);
        }
        log.info("instanceIdList的大小====" + instanceIdList.size());

        List<Long> instanceIDList = Constant.flagMap.get(taskId);//做过的点
        log.info("做过的点instanceIDList====" + instanceIDList);

        if (instanceIDList != null && instanceIDList.size() > 0) {
            for (Long instanceId : instanceIDList) {
                instanceIdList.remove(instanceId.toString());
            }
        }
        log.info("准备遍历的点是===" + instanceIdList);
        List<TCruiseDataResult> tCDRList = new ArrayList<>();//巡检点数据表tCDRList
        List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();//巡检点状态详细表tCTRDList
        List<Long> instancedList = new ArrayList<>();//插过库的点

        //读异常点缓存表巡检点
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

        //缓存中的总检测点
        Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
        log.info("总检测点数是===" + totalCheckPoint);
        //缓存中的异常点
        Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString());
        log.info("异常点数是===" + abnormalCheckPoint);
        //缓存中的正常点
        Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString());
        log.info("正常点数是===" + normalCheckPoint);


        Integer abnormal = abnormalCheckPoint;//异常
        Integer normal = normalCheckPoint;//正常

        for (String instanceId : instanceIdList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            log.info("cruiseResult 是 ===" + redisInfoMap.get("cruiseResult"));
            if (redisInfoMap.get("cruiseResult").equals("246") ||
                    redisInfoMap.get("cruiseResult").equals("247")) {//缓存中该巡检点有结果
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                        .setTaskResultId(redisInfoMap.get("taskResultId"))
                        .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
                        .setInstanceName(redisInfoMap.get("instanceName"))
                        .setCruiseTime(sdf.parse(redisInfoMap.get("cruiseTime")))
                        .setEndTime(sdf.parse(redisInfoMap.get("endTime")))
                        .setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")))
                        .setDeviceName(redisInfoMap.get("deviceName"))
                        .setCruiseStatus(252);//252.已执行253.未执行254.执行失败255.未知
                tCTRDList.add(tCruiseTaskResultDetail);
                instancedList.add(Long.valueOf(redisInfoMap.get("instanceId")));

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                        .setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")))
                        .setCruiseName(redisInfoMap.get("cruiseName"))
                        .setCruiseType(228)//机器人
                        .setPicpath(redisInfoMap.get("picpath"))
                        .setOrigpic(redisInfoMap.get("origpic"))
                        .setIsWarn(0)
                        .setEvaluationState(257)
                        .setCreatetime(new Date())
                        .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                } else {
                    tCruiseDataResult.setCruiseAbnormal(null);
                }
                if (!"--".equals(redisInfoMap.get("resultNum"))) {
                    tCruiseDataResult.setResultNum(redisInfoMap.get("resultNum"));
                } else {
                    tCruiseDataResult.setResultNum(null);
                }
                tCDRList.add(tCruiseDataResult);

                if ("null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    normal = normal + 1;
                    log.info("这次变化的normal是===" + normal);
                } else if ("250".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    abnormal = abnormal + 1;
                    log.info("这次变化的abnormal是===" + abnormal);
                }
            }
        }
        log.info("准备更新的abnormal是===" + abnormal);
        log.info("准备更新的normal是===" + normal);

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        //更新异常点缓存的数据
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        log.info("tCTRDList的内容是===" + tCTRDList);
        log.info("tCDRList的内容是===" + tCDRList);
        log.info("instancedList的内容是===" + instancedList);

        //将插过库的点放进公共类
        if (instanceIDList != null && instanceIDList.size() > 0) {
            log.info("将插过库的点放进公共类");
            instanceIDList.addAll(instancedList);
            Constant.flagMap.put(taskId, instancedList);
        } else {
            log.info("不用放");
            Constant.flagMap.put(taskId, instancedList);
        }

        //批量插入TCTRD库
        int res1 = batchInsertCruiseTaskResultDetail(tCTRDList);//批量插tCTRDList
        log.info("res1的内容是===" + res1);
        //批量插入TCDR库
        int res2 = batchInsertCruiseDataResult(tCDRList);//批量插tCDRList
        log.info("res2的内容是===" + res2);


        Integer cState = null;
        if (taskStatus == 2) {
            cState = 241;//任务暂停
        } else if (taskStatus == 4) {
            cState = 242;//任务终止
            //将公共类的instanceIdList清空
            for (Long instancedId : Constant.flagMap.get(taskId)) {
                instanceIdList.remove(instancedId.toString());
            }
        }

        TCruiseResult tCruiseResult = selectTaskResultId(taskId);
        log.info("taskResultId是===" + tCruiseResult.getTaskResultId());

        Integer taskWait = totalCheckPoint - normal - abnormal;
        log.info("taskWait的值是==" + taskWait);
        tCruiseResult.setTaskWait(taskWait);
        tCruiseResult.setCState(cState);
        tCruiseResult.setTaskCode(taskId);
        log.info("tCruiseResult的内容是===" + tCruiseResult);
        //更新TCR表
        int res = updateTCruiseResult(tCruiseResult);
        log.info("更新TCR的条数====" + res);

        return 1;
    }

    @Logs(title = "根据taskId查询相关内容", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectTaskResultId(String taskId) {
        TCruiseResult tCruiseResult = tRobotInfoDao.selectTaskResultId(taskId);
        return tCruiseResult;
    }

    @Logs(title = "根据taskId查询相关内容2", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectTCruiseTask(String taskId) {
        return tRobotInfoDao.selectTCruiseTask(taskId);
    }

    @Logs(title = "根据robotCode查询robotId", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public Long selectRobotIdByCode(String robotCode) {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        return robotId;
    }

    @Logs(title = "机器人本体告警信息入库", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int insertRobotAlarm(TRobotAlarm tRobotAlarm) {
        return this.tRobotInfoDao.insertRobotAlarm(tRobotAlarm);
    }

    @Logs(title = "TCDR信息入库--批量插", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> tCruiseDataResultList) {
        return this.tRobotInfoDao.batchInsertCruiseDataResult(tCruiseDataResultList);
    }

    @Logs(title = "TCTRD信息入库--批量插", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList) {
        return this.tRobotInfoDao.batchInsertCruiseTaskResultDetail(tCruiseTaskResultDetailList);
    }

    @Logs(title = "TCR信息更新", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int updateTCruiseResult(TCruiseResult tCruiseResult) {
        return this.tRobotInfoDao.updateTCruiseResult(tCruiseResult);
    }

    @Logs(title = "TCTR信息更新", code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public int insertTCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.tRobotInfoDao.insertTCruiseTaskResult(tCruiseTaskResult);
    }

    public Map<String, Object> booleanZcz(String key, Long userId, String password) {
        SysUser sysUserCurrent = sysUserDao.selectByPrimaryId(userId);
        Map map = new HashMap();
        String keys = Constant.map.get(String.valueOf(userId));
        if (StringUtils.isEmpty(key) && keys == null&&StringUtils.isEmpty(password) ) {
            String random = String.valueOf((int) (Math.random() * 1000000000 + 1));
            Constant.map.put(String.valueOf(userId), random);
            map.put("code", 1);
            map.put("result", random);
            return map;
        } else if(StringUtils.isEmpty(key)&&StringUtils.isEmpty(password)&&keys!=null) {
            map.put("code", 1);
            map.put("result", keys);
            return map;
        }else if (keys == null&&StringUtils.isNoneBlank(key)&&StringUtils.isNoneBlank(password)) {
            map.put("code", 2);
            map.put("result", "密钥已过期");
            return map;
        } else if (!key.equals(keys)) {
            map.put("code", 2);
            map.put("result", "密钥不正确,请重新输入");
            return map;
        } else if (!sysUserCurrent.getPassword().equals(password)) {
            map.put("code", 2);
            map.put("result", "用户密码错误，请重新输入");
            return map;
        }
        return map;
    }


    @Logs(title ="站端控制机器人",code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public String upSystemCommand(XMLBaseModel xmlBaseModel){
        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);

        xmlBaseModel
                .setSendCode("Server01")
                .setReceiveCode(xmlBaseModel.getCode())
                .setCode("省检018");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
        //根据不同的机器人对应不同的管道发送指令
        RobotServerHandler.getRobotServerHandlerMap().get(xmlBaseModel.getCode()).SendHeartBeat( generateByteOrder(xmlString,xmlBaseModel.getCode()),xmlBaseModel.getCode());
        return "success";
    }

    @Logs(title ="机器人数据上报巡视主机",code = "Robot")
    @Transactional(rollbackFor = Exception.class)
    public String upToCruise(XMLBaseModel xmlBaseModel){
        Map<String,List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        robotMap.put("list",list);
        robotTaskStates(robotMap);
        return "success";
    }

    public Result robotTaskStates(Map<String,List<XMLBaseModel>> robotMap) {
        Result re = new Result();
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re = serviceRestTemplate.postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }

}

