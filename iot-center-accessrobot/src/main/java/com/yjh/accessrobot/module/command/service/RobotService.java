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
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    private static final String DATE_TIME_FORMAT_TPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_TPL);
    String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

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
    @Value("${other.webSocketUrl}")
    private String webSocketUrl;

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

        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (robotStatus.equals("离线")){
            log.info("该机器人处于离线状态,没有成功将控制指令下发到机器人......");
            scmap.put("code", 3);
            scmap.put("result", "机器人不在线");
            return scmap;
        }else {
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

            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":61");
            String robotPattern = robotStatusMap.get("value");
            if ("1".equals(robotPattern) && !("1".equals(type) && "5".equals(command))){
                log.info("当前机器人处于任务模式,请切换模式");
                scmap.put("code", 3);
                scmap.put("result", "当前机器人处于任务模式,请切换模式");
                return scmap;
            }else{
                RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
                if ("21".equals(type) && "7".equals(command) //抓图
                        || "21".equals(type) && "10".equals(command)//停止录像
                        || "22".equals(type) && "7".equals(command)){
                    TimeUnit.SECONDS.sleep(5);
                }
                String filePath = null;
                if (Objects.nonNull(RobotServerHandler.getRobotResultMap().get("Item"))){
                    String ftpFilePath = JSONObject.parseObject(JSON.toJSONString(RobotServerHandler.getRobotResultMap().get("Item"))).get("file_path").toString();
                    String sArray[] = ftpFilePath.split("/");
                    String ftpFileName = sArray[sArray.length - 1];

                    Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
                    filePath = relativeImgMap.get("content")+ "/" + todayTime  + "/CameraLib/";
                    if (ftpFileName.endsWith(".jpg")){
                        filePath = filePath + "BigImg/"+ftpFileName;
                    }else if (ftpFileName.endsWith(".bmp")){
                        filePath = filePath + "Infrared/"+ftpFileName;
                    }else if (ftpFileName.endsWith(".mp4")){
                        filePath = filePath + "Video/"+ftpFileName;
                    }
                }
                scmap.put("code", 4);
                scmap.put("result", "指令下发成功");
                scmap.put("path",filePath);
                return scmap;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean feignRobotTransfer(String robotCode) throws Exception {
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (robotStatus.equals("离线")){
            log.info("该机器人处于离线状态,没有成功将模型文件同步指令下发到机器人......");
            return false;
        }
        String sendCode = tRobotInfoDao.selectContent("PlatformServer");
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(robotCode)
                .setCode("省检018")
                .setTime(sdf.format(new Date()))
                .setType("61")
                .setCommand("1");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人模型同步调用xml是<start>" + xmlString + "<end>");
        RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString,robotCode),robotCode);
        /*Constant.flag = 0;
        RobotServerHandler sendId = RobotServerHandler.getRobotServerHandlerMap().get(robotCode);
        log.info("sendId是<start>" + sendId + "<end>");
        if (sendId != null) {
            sendId.sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        }
        Thread.sleep(500);
        if (Constant.flag == 1) {
            res = true;
        }*/
        return true;
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
            Constant.sendSessionId = sendSessionId;
        } else {
            Constant.sendSessionId = 0L;
        }
        log.info("-------------这是刚发命令的请求"+sendSessionId+"-------------");

        byte[] requestProtocol = PlatformPacketUtil.createPacket(sendSessionId, 0, true, xmlString);//生成发送的报文

        return requestProtocol;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateRobotInfo(String robotCode, String robotStatus) {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus(robotStatus);
        int res = tRobotInfoDao.update(tRobotInfo);
        log.info("robotCode为==="+robotCode+",robotId为==="+robotId+"的机器人状态是==="+tRobotInfo.getRobotStatus()+",修改结果==="+res);
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
            if (!"".equals(deviceMap.get("component_id").toString())){
                tRobotInspection.setComponentId(deviceMap.get("component_id").toString());
            }
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
            TRobotRegion tr1 = new TRobotRegion();
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
            Integer deviceType = selectDictCode("deviceType",deviceMap.get("device_type").toString(),"device_type");
            tr3.setDeviceType(deviceType);
            tRobotRegionList.add(tr3);

            TRobotRegion tr4 = new TRobotRegion();
            tr4.setRegionId(deviceMap.get("component_id").toString());
            tr4.setRegionName(deviceMap.get("component_name").toString());
            tr4.setUpRegionId(deviceMap.get("main_device_id").toString());
            tRobotRegionList.add(tr4);
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
        log.info("获得的tRobotRegionList是："+tRobotRegionList);
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
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> receivingResponse(XMLBaseModel xmlBaseModel,long receiveSessionId) {
        RobotServerHandler.getRobotResultMap().put("Code",xmlBaseModel.getCode());
        RobotServerHandler.getRobotResultMap().put("receiveSessionId",receiveSessionId);

        if (Constant.sendSessionId == receiveSessionId){
            log.info("-------------这是刚发命令的响应"+receiveSessionId+"-------------");
            if (Objects.isNull(xmlBaseModel.getItems()) || xmlBaseModel.getItems().isEmpty()){
                RobotServerHandler.getRobotResultMap().put("Item",null);
                return RobotServerHandler.getRobotResultMap();
            }else{
                RobotServerHandler.getRobotResultMap().put("Item",xmlBaseModel.getItems().get(0));
            }
        }else{
            log.info("-------------这不是刚发命令的响应"+receiveSessionId+"-------------");
        }
        log.info("组成的robotResultMap是==="+RobotServerHandler.getRobotResultMap());

        Map<String,Object> res = new HashMap<>();
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        /*
        将ftp服务器上的文件复制到开发环境
        * */
        String temporaryPath = filePathMap.get("content");//文件在ftp服务器上的绝对路径

        String ftpFilePath = xmlBaseModel.getItems().get(0).get("file_path").toString();
        String sArray[] = ftpFilePath.split("/");
        String ftpFileName = sArray[sArray.length - 1];//巡视结果文件名称
        temporaryPath  = temporaryPath + "/" + ftpFilePath;
        String developAbsoluteUrl = absoluteImgMap.get("content") + "/"+ todayTime  + "/CameraLib/";
        String developRelativeUrl = relativeImgMap.get("content")+ "/" + todayTime  + "/CameraLib/";

        if (ftpFileName.endsWith(".jpg")){
            developAbsoluteUrl = developAbsoluteUrl + "BigImg/";
            developRelativeUrl = developRelativeUrl + "BigImg/";
        }else if (ftpFileName.endsWith(".bmp")){
            developAbsoluteUrl = developAbsoluteUrl + "Infrared/";
            developRelativeUrl = developRelativeUrl + "Infrared/";
        }else if (ftpFileName.endsWith(".mp4")){
            developAbsoluteUrl = developAbsoluteUrl + "Video/";
            developRelativeUrl = developRelativeUrl + "Video/";
        }

        log.info("developAbsoluteUrl是: "+developAbsoluteUrl+"------developRelativeUrl是: "+developRelativeUrl);

        File f=new File(developAbsoluteUrl);
        if (!f.exists()){
            f.setWritable(true, false);
            f.mkdirs();
        }

        try {
            String url = "cp " + temporaryPath + " "+developAbsoluteUrl;
            log.info("url是==="+url);
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.getMessage();
        }
        res.put("developAbsoluteUrl",developAbsoluteUrl);
        res.put("developRelativeUrl",developRelativeUrl);

        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public String deviceMaintenanceIssued(Map<String,Object> resMap) {
        log.info("其他服务传来的map是==="+resMap);
        String sendCode = tRobotInfoDao.selectContent("PlatformServer");
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
        log.info("在线的robotCodeList: "+robotCodeList);

        for (String robotCode : robotCodeList){
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(robotCode)
                    .setCode("省检018")
                    .setType("81")
                    .setCommand("4")
                    .setItems(ItemList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
            log.info("生成的机器人下发检修区域指令xml是<start>" + xmlString + "<end>");
            RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);

        }

//        String code = Constant.robotResultMap.get("Code");
        String code = RobotServerHandler.getRobotResultMap().get("Code").toString();

        if ("200".equals(code)){
            return "true";
        }
        return "false";
    }
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTaskIssued(Map<String, List<RobotTaskInstanceInfo>> ItemMap) throws Exception{
        log.info("传来的ItemMap是==" + ItemMap);

        List<RobotTaskInstanceInfo> rTIIList = ItemMap.get("robotTaskInfoList");
        List<Map<String, String>> redisInfoList = new ArrayList<>();
        log.info("rTIIList是===" + rTIIList);
        for (RobotTaskInstanceInfo rTII : rTIIList) {
            String robotStatus = tRobotInfoDao.selectStatusByRobotCode(rTII.getRobotCode());
            if (robotStatus.equals("离线")){
                log.info("该机器人处于离线状态,没有成功将任务下发到机器人,巡视结果数据默认......");
            }else{
                Map<String,Object> mapRes1 = robotTaskDragonService(rTII.getRobotCode(),"1","6","");//控制权获得
                if ("200".equals(mapRes1.get("code").toString())){
                    Map<String,Object> mapRes2 = robotTaskDragonService(rTII.getRobotCode(),"1","5","1");//任务模式
                    if ("200".equals(mapRes2.get("code").toString())){
                        log.info("任务这才真正的下发到机器人,开始构建任务相关信息......");
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
                        redisTemplate.opsForHash().putAll("RobotTaskStatus:" + rTII.getRobotCode() + ":" + rTII.getTaskId(), instanceListMap);

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
                                    + ":" + redisInfoList.get(i).get("taskId") + ":" +redisInfoList.get(i).get("instanceId"), redisInfoList.get(i));
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
                        map.put("cycle_month", "");//周期（月）
                        map.put("cycle_week", "");//周期（周）
                        map.put("cycle_execute_time", "");//周期（执行时间）
                        map.put("cycle_start_time", "");//周期开始时间
                        map.put("cycle_end_time", "");//周期开始时间
                        map.put("interval_number", "");//间隔（数量）
                        map.put("interval_type", "");//间隔（类型）
                        map.put("interval_execute_time", "");//间隔（执行时间）
                        map.put("interval_start_time", "");//间隔开始时间
                        map.put("interval_end_time", "");//间隔结束时间
                        map.put("invalid_start_time", "");//不可用开始时间
                        map.put("invalid_end_time", "");//不可用结束时间
                        map.put("isenable", "");//是否可用
                        map.put("creator", "");//编制人
                        map.put("create_time", "");//编制时间
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
                    }else if ("209".equals(mapRes1.get("code"))){
                        log.info("机器人不在线");
                    }else if("500".equals(mapRes1.get("code"))){
                        log.info("机器人任务模式切换失败");
                    }
                }else if ("209".equals(mapRes1.get("code"))){
                    log.info("机器人不在线");
                }else if("500".equals(mapRes1.get("code"))){
                    log.info("机器人控制权获得失败");
                }
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
                String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
                if (robotStatus.equals("离线")){
                    log.info("该机器人处于离线状态,没有成功将任务控制下发到机器人......");
                }else {
                    XMLBaseModel xmlBaseModel = new XMLBaseModel()
                            .setType("41")
                            .setSendCode(sendCode)
                            .setReceiveCode(robotCode)
                            .setCode(taskId)
                            .setCommand(commandValue)
                            .setTime(sdf.format(new Date()));
                    String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                    log.info("生成的任务控制xml是<start>" + xmlString + "<end>");

                    RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);

                    if (commandValue.equals("2") || commandValue.equals("4")) {
                        insertForPause(robotCode, taskId, Integer.valueOf(commandValue));
                    }
                }
            }
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertForPause(String robotCode, String taskId, Integer taskStatus) throws Exception {
        //统计巡视主机下发给机器人的巡检点大小
        Map<String, String> redisInstanceIdMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode+ ":"+ taskId);
        String redisInstanceIdList = redisInstanceIdMap.get("instanceIdList");
        redisInstanceIdList = redisInstanceIdList.replaceAll("\\[", "").replaceAll("]", "");
        String[] instanceIdArray = redisInstanceIdList.split(", ");
        List<String> allInstanceIdList = new ArrayList<>();
        for (String i : instanceIdArray) {
            allInstanceIdList.add(i);
        }
        log.info("发给机器人的巡检点的个数====" + allInstanceIdList.size());

        List<Long> instanceIDList = Constant.flagMap.get(taskId);//已经做过的点
        log.info("已经做过的巡视点====" + instanceIDList);

        //删除已经做过的点
        if (instanceIDList != null && !instanceIDList.isEmpty()) {
            for (Long instanceId : instanceIDList) {
                allInstanceIdList.remove(instanceId.toString());
            }
        }
        log.info("删除已经做过的巡视点后==="+allInstanceIdList);

        List<Long> isFinishedInstanceList = tRobotInfoDao.selectInstanceForTaskGoOn(taskId);//已经入库的点
        log.info("已经入库的巡视点==="+isFinishedInstanceList);

        //删除已经入库的点
        if (isFinishedInstanceList != null && !isFinishedInstanceList.isEmpty()) {
            for (Long instanceIdInTable : isFinishedInstanceList) {
                allInstanceIdList.remove(instanceIdInTable.toString());
            }
        }
        log.info("删除已经入库的巡视点后==="+allInstanceIdList);

        List<TCruiseDataResult> tCDRList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();
        List<Long> instancedList = new ArrayList<>();//插过库的点

        //读异常点缓存表巡检点
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

        Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString());
        Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString());
        log.info("总检测点数是==="+totalCheckPoint+",异常点数是==="+abnormalCheckPoint+",正常点数是===" + normalCheckPoint);

        Integer abnormal = abnormalCheckPoint;
        Integer normal = normalCheckPoint;

        log.info("准备遍历的点是===" + allInstanceIdList);
        for (String instanceId : allInstanceIdList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            log.info("cruiseResult是 ===" + redisInfoMap.get("cruiseResult"));
            //缓存中该巡检点有结果
            if (!redisInfoMap.get("resultNum").equals("设备检修中")) {
                if (redisInfoMap.get("cruiseResult").equals("246") || redisInfoMap.get("cruiseResult").equals("247")) {
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
                            .setEvaluationState(257)
                            .setCreatetime(sdf.parse(redisInfoMap.get("cruiseTime")))
                            .setIsWarn(0)
                            .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                    if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                        tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                    } else {
                        tCruiseDataResult.setCruiseAbnormal(null);
                    }
                    if (redisInfoMap.containsKey("resultPic")){
                        tCruiseDataResult.setResultPic(redisInfoMap.get("resultPic"));
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
        }
        log.info("准备更新的abnormal是：" + abnormal + ",准备更新的normal是: "+normal);

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        //更新异常点缓存的数据
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        log.info("tCTRDList的内容是===" + tCTRDList+",大小size是: "+tCTRDList.size());
        log.info("tCDRList的内容是===" + tCDRList+",大小size是: "+tCDRList.size());
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
        
        int res1 = 0;
        int res2 = 0;
        if (tCTRDList != null && !tCTRDList.isEmpty())
        {
            res1 = batchInsertCruiseTaskResultDetail(tCTRDList);
        }
        if (tCDRList != null && !tCDRList.isEmpty()){
            res2 = batchInsertCruiseDataResult(tCDRList);
            otherServer(cruiseResultIdList);
        }
        log.info("插tCTRD的条数: "+res1+",插tCDR的条数: "+res2);

        Integer cState = null;
        if (taskStatus == 2) {
            cState = 241;
        } else if (taskStatus == 4) {
            cState = 242;
            //将公共类的instanceIdList清空
            for (Long instancedId : Constant.flagMap.get(taskId)) {
                allInstanceIdList.remove(instancedId.toString());
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
        if (StringUtils.isEmpty(key) && keys == null && StringUtils.isEmpty(password)) {
            String random = String.valueOf((int) (Math.random() * 1000000000 + 1));
            Constant.map.put(String.valueOf(userId), random);
            map.put("code", 1);
            map.put("result", random);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isEmpty(password) && keys != null) {
            map.put("code", 1);
            map.put("result", keys);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isNoneBlank(password) && keys == null) {
            map.put("code", 2);
            map.put("result", "请输入密钥");
            return map;
        } else if (keys == null && StringUtils.isNoneBlank(key) && StringUtils.isNoneBlank(password)) {
            map.put("code", 2);
            map.put("result", "密钥已过期");
            return map;
        } else if (!key.equals(keys)) {
            log.info("key==========================================="+key);
            log.info("keys==========================================="+keys);
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

        xmlBaseModel
                .setSendCode("Server01")
                .setReceiveCode(Constant.robotCode);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
        log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
        //根据不同的机器人对应不同的管道发送指令
        RobotServerHandler.getRobotServerHandlerMap().get(Constant.robotCode).sendHeartBeat( generateByteOrder(xmlString,Constant.robotCode),Constant.robotCode);
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
    public List<TCruiseTaskResultDetail> selectRepairTCTRDList(String taskId){
        return this.tRobotInfoDao.selectRepairTCTRDList(taskId);
    }
    public List<TCruiseDataResult> selectRepairTCDRList(String taskId){
        return this.tRobotInfoDao.selectRepairTCDRList(taskId);
    }
    public TCruisePointInstanceDetail selectForTask (Long instanceId){
        return this.tRobotInfoDao.selectForTask(instanceId);
    }
    public List<TAlgorithmInfo> selectByDeviceMeteId(Long deviceMeteId){
        return this.tRobotInfoDao.selectByDeviceMeteId(deviceMeteId);
    }
    public TStdDeviceMete selectDeviceMete(Long deviceMeteId){
        return this.tRobotInfoDao.selectDeviceMete(deviceMeteId);
    }
    public int methodTest1(String taskId)throws Exception{
        Map<String, Object> jasonMap = new HashMap<>();
        jasonMap.put("type", "finishedOneInstance");
        jasonMap.put("taskId", taskId);
        String json = JSON.toJSONString(jasonMap);
        log.info("发送给前端的消息：" + json);
        Constant.postUrl(json,webSocketUrl);
        return 1;
    }
    public int robotTaskIntoDB(List<Map<String, Object>> taskModelMapList, XMLBaseModel xmlBaseModel) {
        List<TCruiseTask> tCruiseTaskList = new ArrayList<>();
        for (Map<String, Object> taskModelMap : taskModelMapList) {
//            String newTaskId = String.valueOf(UUID.randomUUID()).replace("-", "");
            /*
            * 构建t_cruise_task
            * */
            TCruiseTask tCruiseTask = new TCruiseTask();
            tCruiseTask.setTaskId(taskModelMap.get("task_code").toString());
            tCruiseTask.setTaskCode(taskModelMap.get("task_code").toString());
            tCruiseTask.setTaskName(taskModelMap.get("task_name").toString());
            Integer cType = null;
            if (Objects.nonNull(taskModelMap.get("type")) && !"".equals(taskModelMap.get("type").toString())){
                String type = taskModelMap.get("type").toString();
                switch (type){
                    case "1":
                        cType = 214;break;//例行
                    case "2":
                        cType = 216;break;//特殊
                    case "3":
                        cType = 217;break;//专项
                    case "4":
                        cType = 218;break;//自定义
                    default:break;
                }
            }
            tCruiseTask.setType(cType);
            //贼几把难搞
//            tCruiseTask.setDateType();
            /*if (Objects.nonNull(taskModelMap.get("fixed_start_time")) || "".equals(taskModelMap.get("fixed_start_time").toString())){
                tCruiseTask.setIfRun();
            }else {
                tCruiseTask.setIfRun();
            }*/
            if (!"".equals(taskModelMap.get("cycle_execute_time")) || !"".equals(taskModelMap.get("interval_start_time"))){
                tCruiseTask.setIfRun(172);//周期或间隔
            }else{
                tCruiseTask.setIfRun(174);//定期
            }
            try {
                tCruiseTask.setStartTime(sdf.parse(taskModelMap.get("fixed_start_time").toString()));
            }catch (ParseException e){
                e.getMessage();
            }
            Long robotId = tRobotInfoDao.selectRobotIdByCode(xmlBaseModel.getSendCode());
            tCruiseTask.setRobotId(robotId);
            tCruiseTask.setTaskType(271);//机器人本体任务
            if (!"".equals(taskModelMap.get("priority").toString())){
                tCruiseTask.setTaskLevel(Integer.valueOf(taskModelMap.get("priority").toString()));
            }
            tCruiseTask.setCreateTime(new Date());
            tCruiseTaskList.add(tCruiseTask);
            log.info("tCruiseTaskList==="+tCruiseTaskList);

            /*
            * 该任务下包含的点位
            * */
            String inspectionIdString = taskModelMap.get("device_list").toString();
            String inspectionIds[] = inspectionIdString.split(",");
            List<String> inspectionIdList = new ArrayList<>();
            for(String inspectionId : inspectionIds){
                inspectionIdList.add(inspectionId);
            }
            log.info("机器人对应点list==="+inspectionIdList);

            /*
             * 构建t_cruise_point_instance
             * */
            List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>();
            for (String inspectionCode : inspectionIdList){
                TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
                TStdRegion tStdRegion = tRobotInfoDao.selectTSRegionForStation();
                tCruisePointInstance.setStationId(tStdRegion.getStationId());
                tCruisePointInstance.setStationName(tStdRegion.getStationName());
                tCruisePointInstance.setCruiseType(228);//机器人
                Map<String,Object> map = tRobotInspectionDao.selectInspection(inspectionCode);
                tCruisePointInstance.setCruiseId(Long.valueOf(map.get("inspection_id").toString()));
                tCruisePointInstance.setCruiseName(map.get("inspection_name").toString());
                tCruisePointInstance.setIfSy(1);
                tCruisePointInstanceList.add(tCruisePointInstance);

            }
            log.info("tCruisePointInstanceList==="+tCruisePointInstanceList);
            tRobotInfoDao.batchInsertInstance(tCruisePointInstanceList);

            /*
             * 构建t_cruise_task_attr
             * */
            List<Map<String, String>> redisInfoList = new ArrayList<>();
            List<Long> instanceIdList = new ArrayList<>();
            List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
            for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList){
                TCruiseTaskAttr tCruiseTaskAttr = new TCruiseTaskAttr()
                        .setTaskId(taskModelMap.get("task_code").toString())
                        .setInstanceId(tCruisePointInstance.getInstanceId());
                tCruiseTaskAttrList.add(tCruiseTaskAttr);
                instanceIdList.add(tCruisePointInstance.getInstanceId());

                Map<String, String> redisInfoMap = new HashMap<>();
                redisInfoMap.put("robotCode", xmlBaseModel.getSendCode());
                redisInfoMap.put("instanceId", tCruisePointInstance.getInstanceId() + "");
                Map<String,Object> map = tRobotRegionDao.selectInspectionId(tCruisePointInstance.getCruiseId());
                redisInfoMap.put("inspectionCode", map.get("inspection_code").toString());
                redisInfoMap.put("deviceName",map.get("region_name").toString());
                redisInfoMap.put("taskId", taskModelMap.get("task_code").toString());
                redisInfoMap.put("inspectionName", tCruisePointInstance.getCruiseName());
                redisInfoList.add(redisInfoMap);
            }
            log.info("redisInfoList是===" + redisInfoList);
            //放数据到缓存
            for (int i = 0; i < redisInfoList.size(); i++) {
                redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + redisInfoList.get(i).get("robotCode")
                        + ":" + redisInfoList.get(i).get("taskId") + ":" +redisInfoList.get(i).get("instanceId"), redisInfoList.get(i));
            }

            log.info("tCruiseTaskAttrList==="+tCruiseTaskAttrList);
            tRobotInfoDao.batchInsertTaskAttr(tCruiseTaskAttrList);

            //将instanceIdList放缓存，以备后续使用
            log.info("instanceIdList是===" + instanceIdList);
            Map<String, Object> instanceListMap = new HashMap<>();
            instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
            instanceListMap.put("taskId", taskModelMap.get("task_code").toString());
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + xmlBaseModel.getSendCode() + ":" +taskModelMap.get("task_code").toString(), instanceListMap);

            /*
             * 构建t_cruise_result
             * */
            TCruiseResult tCruiseResult = new TCruiseResult()
                    .setTaskResultId(String.valueOf(UUID.randomUUID()).replace("-", ""))
                    .setTaskId(taskModelMap.get("task_code").toString())
                    .setTaskName(taskModelMap.get("task_name").toString())
                    .setCType(cType)
                    .setCState(238)//任务未开始
                    .setTaskCode(taskModelMap.get("task_code").toString())
                    .setCreateTime(new Date())
                    .setRemark("0");
            log.info("tCruiseResult==="+tCruiseResult);
            tRobotInfoDao.insertTCruiseResult(tCruiseResult);
        }

        return  tRobotInfoDao.batchInsertTask(tCruiseTaskList);
    }
    public int insertWarn(TWarnInfo warnInfo){
        return tRobotInfoDao.insertWarn(warnInfo);
    }
    public int updateIsWarn(Long cruiseDataId){
        return tRobotInfoDao.updateIsWarn(cruiseDataId);
    }
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId){
        return tRobotInfoDao.selectDeviceMeteInfo(instanceId);
    }
    public int sendWebSocket(String json) throws Exception{
        log.info("发送给前端的消息：" + json);
        Constant.postUrl(json,webSocketUrl);
        return 1;
    }
    /*public int robotDeviceIntoDB(List<Map<String, Object>> deviceMapList, XMLBaseModel xmlBaseModel){
        List<TRobotRegion> tRobotRegionList = new ArrayList<>();
        for (Map<String, Object> deviceMap : deviceMapList) {

            //机器人区域层级
            TRobotRegion tr1 = new TRobotRegion();
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
            Integer deviceType = selectDictCode("deviceType",deviceMap.get("device_type").toString(),"device_type");
            tr3.setDeviceType(deviceType);
            tRobotRegionList.add(tr3);

            TRobotRegion tr4 = new TRobotRegion();
            tr4.setRegionId(deviceMap.get("component_id").toString());
            tr4.setRegionName(deviceMap.get("component_name").toString());
            tr4.setUpRegionId(deviceMap.get("main_device_id").toString());
            tRobotRegionList.add(tr4);
        }

        //机器人区域层级
        log.info("获得的tRobotRegionList是："+tRobotRegionList);
        List<TRobotRegion> lst = tRobotRegionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.getRegionId() + "#" + o.getRegionName() + "#" + o.getUpRegionId()))),
                ArrayList::new));
        log.info("去重后的tRobotRegionList是："+lst);
        //对机器人区域层级数据进行相应的处理
        List<String> nowRobotRegionList = tRobotRegionDao.selectAllRobotRegion();//该机器人现有的区域层级节点
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
        }
        return 1;
    }*/
    //机器人任务一条龙服务
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> robotTaskDragonService(String robotCode, String type, String command, String value) throws Exception{
        List<Map<String, Object>> Item = new LinkedList<>();
        Map scmap = new HashMap();

        Map<String, Object> map = new HashMap<>();
        if (!"".equals(value) || null != value) {
            map.put("value", value);
        }
        Item.add(map);

        String sendCode = tRobotInfoDao.selectContent("PlatformServer");
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (robotStatus.equals("离线")){
            log.info("该机器人处于离线状态,没有成功将控制指令下发到机器人......");
            scmap.put("code", "209");
            return scmap;
        }
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
        RobotServerHandler.getRobotServerHandlerMap().get(robotCode).sendHeartBeat(generateByteOrder(xmlString, robotCode), robotCode);
        Thread.sleep(1000);
//        String code = Constant.robotResultMap.get("Code");
        String code = RobotServerHandler.getRobotResultMap().get("Code").toString();

        scmap.put("code", code);
        log.info("下发控制指令返回结果: "+scmap);
        return scmap;

    }
    public Long selectIsRobotTask(String taskId){
        return tRobotInfoDao.selectIsRobotTask(taskId);
    }
    public String selectDictCodeByNote(String dictNote,String colName){
        return tRobotInfoDao.selectDictCodeByNote(dictNote,colName);
    }
}

