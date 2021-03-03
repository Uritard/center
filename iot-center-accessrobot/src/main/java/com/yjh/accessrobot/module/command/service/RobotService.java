package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.dao.SysUserDao;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.dao.TRobotRegionDao;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private TRobotRegionDao tRobotRegionDao;
    @Resource
    private SysUserDao sysUserDao;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> feignRobotControl(String robotCode, String type, String command, String value, String direction, String key, Long userId, String password) throws Exception{
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

        String sendCode = tRobotInfoDao.selectContent("PlatformServer");
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(robotCode)
                .setCode("省检018")
                .setTime(sdf.format(new Date()))
                .setType(type)
                .setCommand(command)
                .setItems(Item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
        //根据不同的机器人对应不同的管道发送指令
        RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        Thread.sleep(500);
        String code = Constant.robotResultMap.get("Code");
        if ("200".equals(code)){
            log.info("给机器人成功发送指令,且成功响应给巡视主机的状态码是"+code);
            scmap.put("code", 3);
            scmap.put("result", "success");
        }else{
            log.info("给机器人成功发送指令,但是响应给巡视主机的状态码是"+code);
            scmap.put("code", 3);
            scmap.put("result", "fail");
        }
        /*scmap.put("code", 3);
        scmap.put("result", "success");*/
        return scmap;

    }

    @Transactional(rollbackFor = Exception.class)
    public boolean feignRobotTransfer(String robotCode) throws Exception {
        boolean res = false;
        String sendCode = tRobotInfoDao.selectContent("PlatformServer");

        SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMATTPL);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
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
            sendId.sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        }
        Thread.sleep(500);
        if (Constant.flag == 1) {
            res = true;
        }
        return res;
    }

    /*
    * 生成发送byte指令，附带测试
    * */
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

    @Transactional(rollbackFor = Exception.class)
    public int updateRobotInfo(String robotCode, String robotStatus) {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus(robotStatus);
        log.info("robotCode为==="+robotCode+",robotId为==="+robotId+"的机器人状态是==="+tRobotInfo.getRobotStatus());

        int res = tRobotInfoDao.update(tRobotInfo);
        log.info("修改结果==="+res);
        return res;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllRobotCode() {
        return tRobotInfoDao.selectAllRobotCode();
    }
    @Transactional(rollbackFor = Exception.class)
    public int robotFileIntoDB(List<Map<String, Object>> deviceMapList, List<Map<String, Object>> robotMap, XMLBaseModel xmlBaseModel) {
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");

        //机器人模型文件
        String picPath = filePathMap.get("content") + "/" + robotMap.get(0).get("mappath").toString();
        log.info("图片路径为：" + picPath);
        /*
         * 将ftp图copy到开发环境
         * */
        String splitArray[] = picPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
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
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;

        Long robotId = tRobotInfoDao.selectRobotIdByCode(xmlBaseModel.getSendCode());
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setPhotePath(developRelativeUrl);
        log.info("获得的tRobotInfo是：" + tRobotInfo);
        tRobotInfoDao.update(tRobotInfo);

        //设备模型文件
        List<TRobotInspection> deviceList = new ArrayList<>();
        List<TRobotRegion> tRobotRegionList = new ArrayList<>();
        for (Map<String, Object> deviceMap : deviceMapList) {
            TRobotInspection tRobotInspection = new TRobotInspection()
                    .setInspectionCode(deviceMap.get("device_id").toString())
                    .setRobotId(robotId)
                    .setInspectionName(deviceMap.get("device_name").toString())
                    .setSaveTypeList(deviceMap.get("save_type_list").toString())
                    .setRecognitionTypeList(deviceMap.get("recognition_type_list").toString());
            /*if (!"".equals(deviceMap.get("component_id").toString())){
                tRobotInspection.setComponentId(deviceMap.get("component_id").toString());
            }*/
            if (!"".equals(deviceMap.get("meter_type").toString())){
                Integer meterType = selectDictCode("meterType",deviceMap.get("meter_type").toString(),"meter_type");
                tRobotInspection.setMeterType(meterType);
            }
            if (!"".equals(deviceMap.get("appearance_type").toString())){
                Integer appearanceType = selectDictCode("appearanceType",deviceMap.get("appearance_type").toString(),"appearance_type");
                tRobotInspection.setAppearanceType(appearanceType);
            }
            if (!"".equals(deviceMap.get("phase").toString())){
                tRobotInspection.setPhase(deviceMap.get("phase").toString());
            }
            /*if (!"".equals(deviceMap.get("device_info").toString())){
                tRobotInspection.setDeviceInfo(deviceMap.get("device_info").toString());
            }*/
            deviceList.add(tRobotInspection);

            //机器人区域层级
            /*TRobotRegion tr1 = new TRobotRegion();
            tr1.setRegionId(deviceMap.get("place_id").toString());
            tr1.setRegionName(deviceMap.get("place_name").toString());
            tr1.setUpRegionId("-1");
            tRobotRegionList.add(tr1);

            TRobotRegion tr2 = new TRobotRegion();
            tr2.setRegionId(deviceMap.get("bay_id").toString());
            tr2.setRegionName(deviceMap.get("bay_name").toString());
            tr2.setUpRegionId(deviceMap.get("place_id").toString());
            tRobotRegionList.add(tr2);

            TRobotRegion tr3 = new TRobotRegion();
            tr3.setRegionId(deviceMap.get("main_device_id").toString());
            tr3.setRegionName(deviceMap.get("main_device_name").toString());
            tr3.setUpRegionId(deviceMap.get("bay_id").toString());
            tr3.setDeviceType(deviceMap.get("device_type").toString());
            tRobotRegionList.add(tr3);

            TRobotRegion tr4 = new TRobotRegion();
            tr4.setRegionId(deviceMap.get("component_id").toString());
            tr4.setRegionName(deviceMap.get("component_name").toString());
            tr4.setUpRegionId(deviceMap.get("main_device_id").toString());
            tRobotRegionList.add(tr4);*/
        }
        log.info("获得的deviceList是：" + deviceList);

        //对机器人测点数据进行相应的处理
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
        if (nowList != null && !nowList.isEmpty()) {
            List<Long> inspectionIdList = tRobotInspectionDao.selectInspectionIdList(nowList);
            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
            List<Long> instanceIdList = tRobotInspectionDao.selectInstanceIdList(inspectionIdList);
            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

            int res1 = tRobotInspectionDao.batchDeleteTRobotInspection(inspectionIdList); //删库TRI
            int res2 = tRobotInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);//删库TCPI
            int res3 = tRobotInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);//删库TCPA
            log.info("TRI删除条数==" + res1 + ",TCPI删除条数==" + res2 + ",TCPA删除条数==" + res3);
        }
        if (addList != null && !addList.isEmpty()) {
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

        //机器人区域层级
        /*log.info("获得的tRobotRegionList是："+tRobotRegionList);
        List<TRobotRegion> lst = tRobotRegionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.getRegionId() + "#" + o.getRegionName() + "#" + o.getUpRegionId()))),
                ArrayList::new));
        log.info("去重后的tRobotRegionList是："+lst);
        //对机器人区域层级数据进行相应的处理
        List<String> nowRobotRegionList = tRobotRegionDao.selectAllRobotRegion();//该机器人现有的区域层级带点
        log.info("机器人现有的nowRobotRegionList=="+nowRobotRegionList);

        List<TRobotRegion> addRobotRegionList = new ArrayList<>();
        for (TRobotRegion str : lst){
            if (nowRobotRegionList.contains(str.getRegionId())){
                nowRobotRegionList.remove(str.getRegionId());
            }else {
                addRobotRegionList.add(str);
            }
        }
        log.info("最后要插库的list是=="+addRobotRegionList);
        log.info("准备要删除的list是=="+nowRobotRegionList);
        if (nowRobotRegionList != null && nowRobotRegionList.size() > 0){
            int res = tRobotRegionDao.batchDelete(nowRobotRegionList);
            log.info("TRR删除条数=="+res);
        }
        if (addRobotRegionList != null && addRobotRegionList.size() > 0){
            //插库TRR
            int res = tRobotRegionDao.batchInsert(addRobotRegionList);
            log.info("TRR的插入条数是=="+res);
        }
        if (lst.containsAll(addRobotRegionList)){
            lst.removeAll(addRobotRegionList);
        }
        log.info("准备更新的list是=="+lst);
        for (TRobotRegion tRobotRegion : lst){
            tRobotRegionDao.update(tRobotRegion);//更新TRR
        }*/
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> receivingResponse(XMLBaseModel xmlBaseModel) {
        Constant.robotResultMap.put("Type",xmlBaseModel.getType());
        Constant.robotResultMap.put("Code",xmlBaseModel.getCode());
        log.info("组成的robotResultMap是==="+Constant.robotResultMap);
        return Constant.robotResultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public String deviceMaintenanceIssued(Map<String,Object> resMap) {
        String sendCode = tRobotInfoDao.selectContent("PlatformServer");
        log.info("传来的map是==="+resMap);
        List<Map<String, Object>> ItemList = new ArrayList<>();
        Map<String,Object> itemMap = new HashMap<>();
        itemMap.put("enable",Integer.valueOf(resMap.get("enable").toString()));
        itemMap.put("start_time",resMap.get("startTime").toString());
        itemMap.put("end_time",resMap.get("endTime").toString());
        itemMap.put("device_level",resMap.get("deviceLevel").toString());
        String deviceList = resMap.get("deviceList").toString();
        String deviceIdList = deviceList.substring(1,deviceList.length()-1);
        itemMap.put("device_list",deviceIdList);
        ItemList.add(itemMap);

        List<String> robotCodeList = tRobotInfoDao.selectOnline();

        for (String robotCode : robotCodeList){
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(robotCode)
                    .setCode("变电站编码")
                    .setType("81")
                    .setCommand("4")
                    .setItems(ItemList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
            log.info("生成的机器人下发检修区域指令xml是<start>" + xmlString + "<end>");
//            RobotServerHandler.getRobotServerHandlerMap().get(robotCode).SendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        }

        /*String code = Constant.robotResultMap.get("Code");
        if ("200".equals(code)){
            log.info("给机器人成功发送指令,且成功响应给巡视主机的状态码是"+code);
            return "true";
        }else{
            log.info("给机器人成功发送指令,但是响应给巡视主机的状态码是"+code);
            return "false";
        }*/
        return "success";
    }
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTaskIssued(Map<String, List<RobotTaskInstanceInfo>> ItemMap) {
        log.info("传来的ItemMap是==" + ItemMap);

        List<RobotTaskInstanceInfo> rTIIList = ItemMap.get("robotTaskInfoList");
        List<Map<String, String>> redisInfoList = new ArrayList<>();
        log.info("rTIIList是===" + rTIIList);
        for (RobotTaskInstanceInfo rTII : rTIIList) {
            String robotStatus = tRobotInfoDao.selectStatusByRobotCode(rTII.getRobotCode());
            if (robotStatus.equals("离线")){
                log.info("该机器人处于离线状态,没有成功将任务下发到机器人......");
            }else{
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
                    default:
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

                    Map<String, String> redisInfoMap = new HashMap<>();
                    redisInfoMap.put("robotCode", rTII.getRobotCode());
                    redisInfoMap.put("instanceId", instanceId + "");
                    redisInfoMap.put("inspectionCode", inspectionCode);
                    redisInfoMap.put("cruiseTime", sdf.format(new Date()));
                    redisInfoMap.put("taskId", rTII.getTaskId());
                    redisInfoList.add(redisInfoMap);
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
                map.put("type", planType);
                map.put("task_code", rTII.getTaskId());
                map.put("task_name", rTII.getTaskName());
                map.put("priority", rTII.getPriority());
                map.put("device_level", rTII.getDeviceLevel());
                map.put("device_list", deviceIdList);
                map.put("fixed_start_time", sdf.format(new Date()));
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
                String sendCode = tRobotInfoDao.selectContent("PlatformServer");

                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("101")
                        .setSendCode(sendCode)
                        .setReceiveCode(rTII.getRobotCode())
                        .setCode("省检018")
                        .setTime(sdf.format(new Date()))
                        .setCommand("1")
                        .setItems(mapList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的机器人下发任务的xml是<start>" + xmlString + "<end>");
                RobotServerHandler.getRobotServerHandlerMap().get(rTII.getRobotCode()).sendHeartBeat(generateByteOrder(xmlString, rTII.getRobotCode()), rTII.getRobotCode());
            }
                    }
    }
    @Transactional(rollbackFor = Exception.class)
    public int feignRobotTaskIssued2(Map<String,Object> resMap){
        List<Map<String,Object>> mapList = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("task_code",resMap.get("taskCode"));
        map.put("task_name",resMap.get("taskName"));
        map.put("priority",4);
        map.put("device_level",3);
        map.put("device_list",resMap.get("device_list"));
        mapList.add(map);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setType("102")
                .setSendCode(resMap.get("sendCode").toString())
                .setReceiveCode(resMap.get("receiveCode").toString())
                .setCode(resMap.get("taskCode").toString())
                .setTime(sdf.format(new Date()))
                .setCommand("1")
                .setItems(mapList);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人下发任务的xml是<start>" + xmlString + "<end>");

        //根据不同的机器人对应不同的管道发送指令
        RobotServerHandler.getRobotServerHandlerMap().get(resMap.get("receiveCode").toString())
                .sendHeartBeat(generateByteOrder(xmlString,resMap.get("receiveCode").toString()), resMap.get("receiveCode").toString());
        return 1;
    }
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

            String sendCode = tRobotInfoDao.selectContent("PlatformServer");

            for (String robotCode : robotCodeList) {
                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("41")
                        .setSendCode(sendCode)
                        .setReceiveCode(robotCode)
                        .setCode(taskId)
                        .setCommand(commandValue)
                        .setTime(sdf.format(new Date()));
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的任务控制xml是<start>" + xmlString + "<end>");

                //根据不同的机器人对应不同的管道发送指令
                RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);

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
        String instanceList = redisInfoMap2.get("instanceIdList");
        instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
        String[] instanceIdArray = instanceList.split(", ");
        List<String> instanceIdList = new ArrayList<>();
        for (String i : instanceIdArray) {
            instanceIdList.add(i);
        }
        log.info("instanceIdList的大小====" + instanceIdList.size());

        List<Long> instanceIDList = Constant.flagMap.get(taskId);//做过的点
        log.info("做过的点instanceIDList====" + instanceIDList);

        if (instanceIDList != null && !instanceIDList.isEmpty()) {
            for (Long instanceId : instanceIDList) {
                instanceIdList.remove(instanceId.toString());
            }
        }
        log.info("准备遍历的点是===" + instanceIdList);
        List<TCruiseDataResult> tCDRList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();
        List<Long> instancedList = new ArrayList<>();//插过库的点

        //读异常点缓存表巡检点
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

        Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
        log.info("总检测点数是===" + totalCheckPoint);
        Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString());
        log.info("异常点数是===" + abnormalCheckPoint);
        Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString());
        log.info("正常点数是===" + normalCheckPoint);

        Integer abnormal = abnormalCheckPoint;
        Integer normal = normalCheckPoint;

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
                        .setCruiseStatus(252)
                        .setRemark(redisInfoMap.get("remark"));
                tCTRDList.add(tCruiseTaskResultDetail);
                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));
                instancedList.add(Long.valueOf(redisInfoMap.get("instanceId")));

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                        .setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")))
                        .setCruiseName(redisInfoMap.get("cruiseName"))
                        .setCruiseType(228)
                        .setResultNum(redisInfoMap.get("resultNum"))
                        .setModifyNum(redisInfoMap.get("modifyNum"))
                        .setPicpath(redisInfoMap.get("picpath"))
                        .setPicPathAnl(redisInfoMap.get("picPathAnl"))
                        .setOrigpic(redisInfoMap.get("origpic"))
                        .setOrigPicAnl(redisInfoMap.get("origPicAnl"))
                        .setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")))
                        .setEvaluationState(257)
                        .setCreatetime(sdf.parse(redisInfoMap.get("cruiseTime")))
                        .setRemark(redisInfoMap.get("remark"))
                        .setIsWarn(0)
                        .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                /*if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                } else {
                    tCruiseDataResult.setCruiseAbnormal(250);
                }*/
                /*if (!"--".equals(redisInfoMap.get("resultNum"))) {
                    tCruiseDataResult.setResultNum(redisInfoMap.get("resultNum"));
                } else {
                    tCruiseDataResult.setResultNum(null);
                }*/
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
        log.info("准备更新的abnormal是：" + abnormal + ",准备更新的normal是: "+normal);

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        //更新异常点缓存的数据
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        log.info("tCTRDList的内容是===" + tCTRDList);
        log.info("tCDRList的内容是===" + tCDRList);
        log.info("instancedList的内容是===" + instancedList);

        //将插过库的点放进公共类
        if (instanceIDList != null && !instanceIDList.isEmpty()) {
            log.info("将插过库的点放进公共类");
            instanceIDList.addAll(instancedList);
            Constant.flagMap.put(taskId, instancedList);
        } else {
            log.info("不用放");
            Constant.flagMap.put(taskId, instancedList);
        }

        int res1 = batchInsertCruiseTaskResultDetail(tCTRDList);
        log.info("res1的内容是===" + res1);
        int res2 = batchInsertCruiseDataResult(tCDRList);
        otherServer(cruiseResultIdList);
        log.info("res2的内容是===" + res2);

        Integer cState = null;
        if (taskStatus == 2) {
            cState = 241;
        } else if (taskStatus == 4) {
            cState = 242;
            //将公共类的instanceIdList清空
            for (Long instancedId : Constant.flagMap.get(taskId)) {
                instanceIdList.remove(instancedId.toString());
            }
        }

        TCruiseResult tCruiseResult = selectTaskResultId(taskId);
        Integer taskWait = totalCheckPoint - normal - abnormal;
        log.info("taskResultId是: " + tCruiseResult.getTaskResultId()+"的taskWait值是==" + taskWait);
        tCruiseResult.setTaskWait(taskWait);
        tCruiseResult.setCState(cState);
        tCruiseResult.setTaskCode(taskId);
        log.info("tCruiseResult的内容是===" + tCruiseResult);
        //更新TCR表
        int res = updateTCruiseResult(tCruiseResult);
        log.info("更新TCR的条数====" + res);

        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result otherServer(List<String > cruiseResultIdList) {
        return Constant.otherServerList(cruiseResultIdList,Constant.TASK_FINISH);
    }
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectTaskResultId(String taskId) {
        return tRobotInfoDao.selectTaskResultId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectTCruiseTask(String taskId) {
        return tRobotInfoDao.selectTCruiseTask(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long selectRobotIdByCode(String robotCode) {
        return tRobotInfoDao.selectRobotIdByCode(robotCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertRobotAlarm(TRobotAlarm tRobotAlarm) {
        return this.tRobotInfoDao.insertRobotAlarm(tRobotAlarm);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> tCruiseDataResultList) {
        return this.tRobotInfoDao.batchInsertCruiseDataResult(tCruiseDataResultList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList) {
        return this.tRobotInfoDao.batchInsertCruiseTaskResultDetail(tCruiseTaskResultDetailList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateTCruiseResult(TCruiseResult tCruiseResult) {
        return this.tRobotInfoDao.updateTCruiseResult(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertTCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.tRobotInfoDao.insertTCruiseTaskResult(tCruiseTaskResult);
    }

    public Map<String, Object> booleanZcz(String key, Long userId, String password) throws  Exception{
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
        } else if (!Demo.decryptDB(sysUserCurrent.getPassword()).equals(password)) {
            map.put("code", 2);
            map.put("result", "用户密码错误，请重新输入");
            return map;
        }
        return map;
    }


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
        RobotServerHandler.getRobotServerHandlerMap().get(xmlBaseModel.getCode()).sendHeartBeat( generateByteOrder(xmlString,xmlBaseModel.getCode()),xmlBaseModel.getCode());
        return "success";
    }

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
        re = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
//        try {
//            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
//            if (null != serviceRestTemplate) {
//                re = serviceRestTemplate.postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        return re;
    }
    public Integer selectDictCode(String valueName,String value,String colName){
        String dictNote = null;
        if (valueName.equals("appearanceType")){
            switch (value){
                case "1":
                    dictNote = "电子围栏";
                    break;
                case "2":
                    dictNote = "红外对射";
                    break;
                case "3":
                    dictNote = "泡沫喷淋";
                    break;
                case "4":
                    dictNote = "消防水泵";
                    break;
                case "5":
                    dictNote = "消防栓";
                    break;
                case "6":
                    dictNote = "消防室";
                    break;
                case "7":
                    dictNote = "设备室";
                    break;
                case "8":
                    dictNote = "照明灯";
                    break;
                case "9":
                    dictNote = "摄像头";
                    break;
                case "10":
                    dictNote = "水位线";
                    break;
                case "11":
                    dictNote = "排水泵";
                    break;
                case "12":
                    dictNote = "沉降监测点";
                    break;
                default:
                    break;
            }
        }else if (valueName.equals("meterType")){
            switch (value){
                case "1":
                    dictNote = "油位表";
                    break;
                case "2":
                    dictNote = "避雷器动作次数表";
                    break;
                case "3":
                    dictNote = "泄漏电流表";
                    break;
                case "4":
                    dictNote = "SF6压力表";
                    break;
                case "5":
                    dictNote = "液压表";
                    break;
                case "7":
                    dictNote = "开关动作次数表";
                    break;
                case "8":
                    dictNote = "档位表";
                    break;
                case "9":
                    dictNote = "气压表";
                    break;
                default:
                    break;
            }
        }else if (valueName.equals("deviceType")){
            switch (value){
                case "1":
                    dictNote = "油浸式变压器";
                    break;
                case "2":
                    dictNote = "断路器";
                    break;
                case "3":
                    dictNote = "组合电器";
                    break;
                case "4":
                    dictNote = "隔离开关";
                    break;
                case "5":
                    dictNote = "开关柜";
                    break;
                case "6":
                    dictNote = "电流互感器";
                    break;
                case "7":
                    dictNote = "电压互感器";
                    break;
                case "8":
                    dictNote = "避雷器";
                    break;
                case "9":
                    dictNote = "并联电容器组";
                    break;
                case "10":
                    dictNote = "干式电抗器";
                    break;
                case "11":
                    dictNote = "串联补偿装置";
                    break;
                case "12":
                    dictNote = "母线及绝缘子";
                    break;
                case "13":
                    dictNote = "穿墙套管";
                    break;
                case "14":
                    dictNote = "消弧线圈";
                    break;
                case "15":
                    dictNote = "高频阻波器";
                    break;
                case "16":
                    dictNote = "耦合电容器";
                    break;
                case "17":
                    dictNote = "高压熔断器";
                    break;
                case "18":
                    dictNote = "中性点隔直装置";
                    break;
                case "19":
                    dictNote = "接地装置";
                    break;
                case "20":
                    dictNote = "端子箱及检修电源箱";
                    break;
                case "21":
                    dictNote = "站用变";
                    break;
                case "22":
                    dictNote = "站用交流电源";
                    break;
                case "23":
                    dictNote = "站用直流电源";
                    break;
                case "24":
                    dictNote = "构支架";
                    break;
                case "25":
                    dictNote = "辅助设施";
                    break;
                case "26":
                    dictNote = "土建设施";
                    break;
                case "27":
                    dictNote = "避雷针";
                    break;
                case "28":
                    dictNote = "避雷器动作次数表";
                    break;
                default:
                    break;
            }
        }
        Integer dictCode = Integer.valueOf(tRobotInfoDao.selectDictCode(colName,dictNote));
        return dictCode;
    }
    /*
    * 机器人巡视结果交给算法,分析后再处理
    * */
    public List<AlgorithmDeviceMete> AnalyzeAfterRobot(Analysis analysisItem,String inspectionCode){
        List<AlgorithmDeviceMete> algorithmDeviceMeteList = tRobotInfoDao.selectAlgorithm(inspectionCode);

        Map<String,Object> picModelPathMap  = redisTemplate.opsForHash().entries("t_sys_param:robotPicModelPath");//机器人标定文件路径
        String picModelPath = picModelPathMap.get("content").toString();

        for (AlgorithmDeviceMete algorithmDeviceMete:algorithmDeviceMeteList) {
            Analysis analysis = new Analysis();
            analysis.setTaskId(analysisItem.getTaskId());
            analysis.setInstanceId(analysisItem.getInstanceId());
            analysis.setPicPath(analysisItem.getPicPath());
            analysis.setAnalyseType(algorithmDeviceMete.getAnalyseType());
            analysis.setPicModelPath(picModelPath+"/"+algorithmDeviceMete.getInspectionCode());
            analysis.setIsAi(algorithmDeviceMete.getIsAi());
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            Map<String, List<Analysis>> analysisMap  = new HashMap<>();
            analysisMap.put("list",analysisList);
            log.info("算法信息==="+analysisMap);
            if(algorithmDeviceMete.getIsAi() == 1){//0-缺陷 1-表记
                analysis(analysisMap);
            }else {
                defect(analysisMap);
            }
        }
        return algorithmDeviceMeteList;
    }
    //表记分析
    private void analysis(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.algorithmUrl, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    //缺陷分析
    private void defect(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.defectUrl, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    /*
    * 若修改机器人编码或删除机器人断开在线的机器人连接
    * */
    public String removeLink(String robotCode,Long robotId){
        RobotServerHandler.getRobotServerHandlerMap().get(robotCode).removeLink(robotCode);
        /*TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus("离线");
        int res = tRobotInfoDao.update(tRobotInfo);
        log.info("修改成功==="+res);*/
        return "离线";
    }
}

