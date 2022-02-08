package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.LogsAspect;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.SysUserDao;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.dao.TRobotInspectionDao;
import com.yjh.accessrobot.module.command.dao.TRobotRegionDao;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yjh.accessrobot.netty.server.RobotServerHandler.getXmlMessage;
import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    private static final String OFF_LINE = "离线";

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
    @Value("${netty.server.url}")
    private String serverUrl;
    @Value("${netty.server.ftps.port}")
    private String ftpsPort;
    @Value("${netty.server.ftps.username}")
    private String ftpsUserName;
    @Value("${netty.server.ftps.password}")
    private String ftpsPassWord;
    @Value("${netty.server.ftps.keypw}")
    private String key;
    @Value("${netty.server.ftps.local.path}")
    private String ftpsLocalPath;
    @Value("${netty.server.name}")
    private String sendCode;

    /**
     * 巡视主机下发控制指令到机器人
     * @param robotCode 机器人唯一标识
     * @param type 类型
     * @param command 命令
     * @param value 值
     * @param direction 描述
     * @param key
     * @param userId
     * @param password
     * @param request
     * @param content
     * @return Map<String,Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> feignRobotControl(String robotCode, String type, String command, String value, String direction, String key, Long userId, String password, HttpServletRequest request,String content) throws Exception{
        Map scmap = new HashMap();
        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
        if (command.equals("1") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("3") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("5") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("6") && type.equals("1")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("9") && type.equals("3")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        } else if (command.equals("8") && type.equals("22")) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.set("logType", "5");
        params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
        params.set("title", "控制机器人");
        params.set("state", 1);
        params.set("userId", userId);
        params.set("userName", userName);
        params.set("requestOrigin", request.getRequestURL());
        params.set("requestPath", request.getRequestURI());
        params.set("requestMethod", request.getMethod());
        /*String operationContent = selectContentByCommand(type,command,value);
        params.set("content",operationContent);*/
        params.set("content", content);
        LogsAspect logsAspect = new LogsAspect();
        logsAspect.post(params);
        List<Map<String, Object>> item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>(3);
        if (StringUtils.isNotEmpty(value)) {
            map.put("value", value);
        }
        if (StringUtils.isNotEmpty(direction)) {
            map.put("direction", direction);
        }
        item.add(map);

        if (StringUtils.isEmpty(robotCode)){
            log.error("==========没有设置机器人编码==========");
            scmap.put("code", 3);
            scmap.put("result", "当前不存在机器人编码,请先添加");
            return scmap;
        }
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (StringUtils.equals(OFF_LINE, robotStatus)){
            log.error("==========该机器人处于离线状态,没有成功将控制指令下发到机器人==========");
            scmap.put("code", 3);
            scmap.put("result", "机器人不在线");
            return scmap;
        }else {
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(robotCode)
                    .setCode(robotCode)
                    .setTime(DateTimeUtil.format(new Date()))
                    .setType(type)
                    .setCommand(command)
                    .setItems(item);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);

            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":61");
            String robotPattern = robotStatusMap.get("value");
            boolean flag = StringUtils.equals("1", robotPattern) && !("1".equals(type) && "5".equals(command));
            if (Boolean.TRUE.equals(flag)){
                log.error("==========当前机器人处于任务模式,请切换模式==========");
                scmap.put("code", 3);
                scmap.put("result", "当前机器人处于任务模式,请切换模式");
                return scmap;
            }else{
                RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);

                // Capture and video file return
                String filePath = null;
                if (Objects.nonNull(RobotServerHandler.getRobotResultMap().get("Item"))){
                    String ftpFilePath = JSONObject.parseObject(JSON.toJSONString(RobotServerHandler.getRobotResultMap().get("Item"))).get("file_path").toString();
                    String[] sArray = ftpFilePath.split("/");
                    String ftpFileName = sArray[sArray.length - 1];

                    Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
                    filePath = relativeImgMap.get("content") + "/" + todayTime  + "/CameraLib/";
                    if (ftpFileName.endsWith(".jpg")){
                        filePath = filePath + "BigImg/" + ftpFileName;
                    }else if (ftpFileName.endsWith(".bmp")){
                        filePath = filePath + "Infrared/" + ftpFileName;
                    }else if (ftpFileName.endsWith(".mp4")){
                        filePath = filePath + "Video/" + ftpFileName;
                    }
                }
                scmap.put("code", 4);
                scmap.put("result", "指令下发成功");
                scmap.put("path", filePath);
                return scmap;
            }
        }
    }

    /**
     * 巡视主机下发模型文件同步指令到机器人
     * @param robotCode 机器人唯一标识
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean feignRobotTransfer(String robotCode)  {
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (StringUtils.equals(OFF_LINE, robotStatus)){
            log.error("==========该机器人处于离线状态,没有成功将模型文件同步指令下发到机器人==========");
            return false;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(robotCode)
                .setCode("变电站编码")
                .setTime(DateTimeUtil.format(new Date()))
                .setType("61")
                .setCommand("1");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        return true;
    }

   /**
    * 生成发送byte指令，附带测试
    * @param xmlString xml数据
    * @param robotCode 机器人唯一标识
    * @return byte[]
    */
    public byte[] generateByteOrder(String xmlString, String robotCode) {
        long sendSessionId = Constant.sendSessionId;
        ChannelHandlerContext context = RobotServerHandler.getChannelHandlerContextByRobot(robotCode);
        log.info("context是<start>{}<end>", context);
        if (context != null) {
            // 请求报文每次累加1
            sendSessionId = sendSessionId + 1L;
            Constant.sendSessionId = sendSessionId;
        } else {
            Constant.sendSessionId = 0L;
        }
        log.info("-------------这是刚发命令的请求{}-------------", sendSessionId);
        return PlatformPacketUtil.createPacket(sendSessionId, 0, true, xmlString);
    }

    /**
     * 更新机器人的在线状态
     * @param robotCode 机器人唯一标识
     * @param robotStatus 在线状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRobotInfo(String robotCode, String robotStatus) {
        Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus(robotStatus);
        int res = tRobotInfoDao.update(tRobotInfo);
        log.info("robotCode为==={},robotId为==={}的机器人状态是==={},修改结果==={}", robotCode, robotId, tRobotInfo.getRobotStatus(), res);
        return res;
    }
    /**
     * 查询表中所有的机器人
     * @return List<String>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllRobotCode() {
        return tRobotInfoDao.selectAllRobotCode();
    }

    /**
     * 处理机器人返回的模型文件
     * @param map 机器人返回的模型文件相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRobotFile(Map<String, String> map) throws Exception {
        String robotCode = map.get("robotCode");
        if (StringUtils.isEmpty(robotCode)){
            log.error("机器人编码为空,robotCode：{}", robotCode);
            throw new RuntimeException("机器人编码为空");
        }
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");

        Object deviceFile = map.getOrDefault("deviceFile", "");
        Object robotFile = map.getOrDefault("robotFile", "");
        XMLBaseModel deviceModel = getXmlMessage(filePathMap.get("content") + File.separator + deviceFile);
        List<Map<String, Object>> deviceMap = deviceModel.getItems();
        XMLBaseModel robotModel = getXmlMessage(filePathMap.get("content") + File.separator + robotFile);
        List<Map<String, Object>> robotMap = robotModel.getItems();

        if (CollectionUtils.isNotEmpty(deviceMap) && CollectionUtils.isNotEmpty(robotMap)) {
            Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
            // Robot Model Info
            addRobotModel(robotMap, robotId);
            // Device Point Info
            addDevicePoint(deviceMap, robotId);
            // Device Point Region Info
            addDevicePointRegion(deviceMap, robotId);
        }
    }
    /**
     * 机器人模型文件信息处理
     * @param robotMap 模型文件信息
     * @param robotId 机器人id
     * @return void
     */
    public void addRobotModel( List<Map<String, Object>> robotMap, Long robotId){
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");

        String picPath = filePathMap.get("content") + "/" + robotMap.get(0).get("mappath").toString();
        log.info("图片路径为：{}", picPath);

        String[] splitArray = picPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        String developMap = absoluteImgMap.get("content") + "/Map";
        copyFileToDevelop(picPath, developMap);
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;
        TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setPhotePath(developRelativeUrl);
        tRobotInfoDao.update(tRobotInfo);
    }
    /**
     * 机器人设备点位文件信息处理
     * @param deviceMapList 设备点位文件信息
     * @param robotId 机器人id
     * @return void
     */
    public void addDeviceModel(List<Map<String, Object>> deviceMapList, Long robotId){

    }

    /**
     * 更新机器人测点信息
     * @param deviceMapList 设备点位文件信息
     * @param robotId 机器人id
     * @return void
     */
    private void addDevicePoint(List<Map<String, Object>> deviceMapList, Long robotId){
        List<TRobotInspection> deviceList = new ArrayList<>();

        for (Map<String, Object> deviceMap : deviceMapList) {
            TRobotInspection tRobotInspection = new TRobotInspection()
                    .setInspectionCode(deviceMap.get("device_id").toString())
                    .setRobotId(robotId)
                    .setInspectionName((deviceMap.get("main_device_name").toString() + "/" + deviceMap.get("device_name").toString()))
                    .setSaveTypeList(deviceMap.get("save_type_list").toString())
                    .setComponentId(deviceMap.get("main_device_id").toString())
                    .setRecognitionTypeList(deviceMap.get("recognition_type_list").toString());
            /*if (!"".equals(deviceMap.get("component_id").toString())){
                tRobotInspection.setComponentId(deviceMap.get("component_id").toString());
            }*/
            if (!"".equals(deviceMap.get("meter_type").toString())){
                Integer meterType = selectDictCode("meterType", deviceMap.get("meter_type").toString(),"meter_type");
                tRobotInspection.setMeterType(meterType);
            }
            if (!"".equals(deviceMap.get("appearance_type").toString())){
                Integer appearanceType = selectDictCode("appearanceType", deviceMap.get("appearance_type").toString(),"appearance_type");
                tRobotInspection.setAppearanceType(appearanceType);
            }
            if (!"".equals(deviceMap.get("phase").toString())){
                tRobotInspection.setPhase(deviceMap.get("phase").toString());
            }
            if (!"".equals(deviceMap.get("device_info").toString())){
                tRobotInspection.setDeviceInfo(deviceMap.get("device_info").toString());
            }

            deviceList.add(tRobotInspection);
//            log.info("获得的deviceList是：" + deviceList);

            // 该机器人现有的巡检点
            List<String> nowList = tRobotInspectionDao.selectAllByRobotId(robotId);
//        log.info("机器人现有的deviceList是：" + nowList);

            List<TRobotInspection> addList = new ArrayList<>();
            for (TRobotInspection str : deviceList) {
                if (nowList.contains(str.getInspectionCode())) {
                    nowList.remove(str.getInspectionCode());
                } else {
                    addList.add(str);
                }
            }
            log.info("最后要插库的deviceList是==={}", addList);
            log.info("准备要删除的inspectionCodeList是==={}", nowList);
            if (CollectionUtils.isNotEmpty(nowList)) {
                List<Long> inspectionIdList = tRobotInspectionDao.selectInspectionIdList(nowList);
//            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
                List<Long> instanceIdList = tRobotInspectionDao.selectInstanceIdList(inspectionIdList);
//            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

                // 删库TRI
                int res1 = tRobotInspectionDao.batchDeleteTRobotInspection(inspectionIdList);
                // 删库TCPI
                int res2 = tRobotInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);
                // 删库TCPA
                int res3 = tRobotInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);
                log.info("TRI删除条数=={},TCPI删除条数=={},TCPA删除条数=={}", res1, res2, res3);
            }
            if (CollectionUtils.isNotEmpty(addList)) {
                // 插库TRI
                int res = tRobotInspectionDao.batchInsertTRobotInspection(addList);
                log.info("TRI插入条数=={}", res);
            }
            if (deviceList.containsAll(addList)) {
                deviceList.removeAll(addList);
            }
            log.info("准备更新的deviceList是=={}", deviceList);
            for (TRobotInspection item : deviceList) {
                // 更新TRI
                tRobotInspectionDao.update(item);
            }
        }
    }

    /**
     * 更新机器人测点区域信息
     * @param deviceMapList 设备点位文件信息
     * @param robotId 机器人id
     * @return void
     */
    private void addDevicePointRegion(List<Map<String, Object>> deviceMapList, Long robotId){
        List<TRobotRegion> tRobotRegionList = new ArrayList<>();

        for (Map<String, Object> deviceMap : deviceMapList) {
            // 只有主设备信息
            TRobotRegion tr3 = new TRobotRegion();
            tr3.setRegionId(deviceMap.get("main_device_id").toString());
            tr3.setRegionName(deviceMap.get("main_device_name").toString());
            tr3.setUpRegionId(deviceMap.get("bay_id").toString());
            tr3.setRobotId(robotId);
            Integer deviceType = selectDictCode("deviceType", deviceMap.get("device_type").toString(), "device_type");
            tr3.setDeviceType(deviceType);
            tRobotRegionList.add(tr3);

            /// 区域、间隔、主设备信息都有的情况,先留着
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
            Integer deviceType = selectDictCode("deviceType",deviceMap.get("device_type").toString(),"device_type");
            tr3.setDeviceType(deviceType);
            tRobotRegionList.add(tr3);

            TRobotRegion tr4 = new TRobotRegion();
            tr4.setRegionId(deviceMap.get("component_id").toString());
            tr4.setRegionName(deviceMap.get("component_name").toString());
            tr4.setUpRegionId(deviceMap.get("main_device_id").toString());
            tRobotRegionList.add(tr4);*/
        }

        // 机器人区域层级
        log.info("获得的tRobotRegionList是：{}", tRobotRegionList);
        List<TRobotRegion> lst = tRobotRegionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(o -> o.getRegionId() + "#" + o.getRegionName() + "#" + o.getUpRegionId()))),
                ArrayList::new));
        log.info("去重后的tRobotRegionList是：{}", lst);

        // 该机器人现有的区域层级点
        List<String> nowRobotRegionList = tRobotRegionDao.selectAllByRobotId(robotId);
//        log.info("机器人现有的nowRobotRegionList=="+nowRobotRegionList);

        List<TRobotRegion> addRobotRegionList = new ArrayList<>();
        for (TRobotRegion str : lst){
            if (nowRobotRegionList.contains(str.getRegionId())){
                nowRobotRegionList.remove(str.getRegionId());
            }else {
                addRobotRegionList.add(str);
            }
        }
        log.info("最后要插库的regionList是=={}", addRobotRegionList);
        log.info("准备要删除的regionList是=={}", nowRobotRegionList);
        if (CollectionUtils.isNotEmpty(nowRobotRegionList)){
            int res = tRobotRegionDao.batchDelete(nowRobotRegionList);
            log.info("TRR删除条数=={}", res);
        }
        if (CollectionUtils.isNotEmpty(addRobotRegionList)){
            // 插库TRR
            int res = tRobotRegionDao.batchInsert(addRobotRegionList);
            log.info("TRR的插入条数是=={}", res);
        }
        if (lst.containsAll(addRobotRegionList)){
            lst.removeAll(addRobotRegionList);
        }
        log.info("准备更新的list是=={}", lst);
        for (TRobotRegion tRobotRegion : lst){
            // 更新TRR
            tRobotRegionDao.update(tRobotRegion);
        }
    }
    /**
     * 机器人控制、任务下发及检修区域下发指令返回响应处理
     * @param xmlBaseModel xml格式的内容
     * @param receiveSessionId 接收会话序列号
     * @return Map<String,Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> receivingResponse(XMLBaseModel xmlBaseModel, long receiveSessionId) {
        RobotServerHandler.getRobotResultMap().put("Code", xmlBaseModel.getCode());
        RobotServerHandler.getRobotResultMap().put("receiveSessionId", receiveSessionId);

        if (Constant.sendSessionId == receiveSessionId){
            log.info("-------------这是刚发命令的响应{}-------------", receiveSessionId);
            if (Objects.isNull(xmlBaseModel.getItems()) || xmlBaseModel.getItems().isEmpty()){
                RobotServerHandler.getRobotResultMap().put("Item", null);
            }else{
                RobotServerHandler.getRobotResultMap().put("Item", xmlBaseModel.getItems().get(0));

                Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
                Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
                Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

                // 文件在ftp服务器上的绝对路径
                String temporaryPath = filePathMap.get("content");
                String ftpFilePath = xmlBaseModel.getItems().get(0).get("file_path").toString();
                String[] sArray = ftpFilePath.split("/");
                // 抓图结果文件名称
                String ftpFileName = sArray[sArray.length - 1];
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

                log.info("developAbsoluteUrl是: {} ------developRelativeUrl是: {}", developAbsoluteUrl, developRelativeUrl);

                // 将机器人摄像机抓图结果从ftp服务器上的复制到开发环境
                copyFileToDevelop(temporaryPath, developAbsoluteUrl);
                RobotServerHandler.getRobotResultMap().put("developAbsoluteUrl", developAbsoluteUrl);
                RobotServerHandler.getRobotResultMap().put("developRelativeUrl", developRelativeUrl);
            }
        }else{
            log.info("-------------这不是刚发命令的响应{}-------------", receiveSessionId);
            return RobotServerHandler.getRobotResultMap();
        }
        return RobotServerHandler.getRobotResultMap();
    }

    /**
     * 将ftp服务器上的文件复制到开发环境
     * @param source 源文件
     * @param aim 目标文件
     * @return void
     */
    public static void copyFileToDevelop(String source, String aim){
        File ff = new File(aim);
        if (!ff.exists()){
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " "+aim;
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.getMessage();
        }
    }

    /**
     * 机器人检修区域下发
     * @param resMap 传来的机器人检修区域相关信息
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String deviceMaintenanceIssued(Map<String, Object> resMap) {
        List<Map<String, Object>> itemList = new ArrayList<>();
        Map<String, Object> itemMap = new HashMap<>(16);
        itemMap.put("enable", Integer.valueOf(resMap.get("enable").toString()));
        itemMap.put("start_time", resMap.get("startTime").toString());
        itemMap.put("end_time", resMap.get("endTime").toString());
        itemMap.put("device_level", resMap.get("deviceLevel").toString());
        String deviceList = resMap.get("deviceList").toString();
        String deviceIdList = deviceList.substring(1, deviceList.length()-1);
        itemMap.put("device_list", deviceIdList);
        itemList.add(itemMap);

        List<String> robotCodeList = tRobotInfoDao.selectOnline();
        log.info("在线的robotCodeList: {}", robotCodeList);

        for (String robotCode : robotCodeList){
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(robotCode)
                    .setCode(robotCode)
                    .setType("81")
                    .setCommand("4")
                    .setItems(itemList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的机器人下发检修区域指令xml是<start>{}<end>", xmlString);
            RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        }

        String code = RobotServerHandler.getRobotResultMap().get("Code").toString();

        if ("200".equals(code)){
            return "true";
        }
        return "false";
    }
    /**
     * 判断机器人是否正常运作之后再下发任务
     * @param itemMap 传来的机器人任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTaskIssued(Map<String, List<RobotTaskInstanceInfo>> itemMap) throws Exception{
        List<RobotTaskInstanceInfo> robotTaskInfoList = itemMap.get("robotTaskInfoList");
        log.info("robotTaskInfoList是==={}", robotTaskInfoList);

        for (RobotTaskInstanceInfo item : robotTaskInfoList) {
            String robotOnlineStatus = tRobotInfoDao.selectStatusByRobotCode(item.getRobotCode());
            if (OFF_LINE.equals(robotOnlineStatus)){
                log.info("==========该机器人处于离线状态,没有成功将任务下发到机器人,巡视结果数据默认==========");
                return;
            }else{
                Map<String,String> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + item.getRobotCode() + ":41");
                String robotStatus = mapForRobotState.get("value");
                if ("4".equals(robotStatus)){
                    log.info("==========该机器人处于检修状态,没有成功将任务下发到机器,巡视结果数据默认==========");
                    return;
                }else {
                    // 控制权获得
                    String controlRes = robotModeSwitch(item.getRobotCode(), "1", "6", "");
                    if ("200".equals(controlRes)) {
                        // 任务模式
                       String taskModelRes = robotModeSwitch(item.getRobotCode(), "1", "5", "1");
                        if ("200".equals(taskModelRes)) {
                            feignRobotTask(itemMap);
                            return;
                        }else if ("500".equals(taskModelRes)) {
                            log.info("==========机器人任务模式切换失败==========");
                            return;
                        }
                    }else if ("500".equals(controlRes)) {
                        log.info("==========机器人控制权获得失败==========");
                        return;
                    }
                }
            }
        }
    }

    /**
     * 正常任务及联动任务下发
     * @param itemMap 传来的机器人任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTask(Map<String, List<RobotTaskInstanceInfo>> itemMap) {
        List<RobotTaskInstanceInfo> robotTaskInfoList = itemMap.get("robotTaskInfoList");
        List<Map<String, String>> redisInfoList = new ArrayList<>();
        log.info("这才开始构建关于机器人任务的相关信息......");

        for (RobotTaskInstanceInfo item : robotTaskInfoList) {
            String robotCode = item.getRobotCode();
            String taskId = item.getTaskId();

            StringJoiner str = new StringJoiner(",");
            List<Long> instanceIdList = item.getInstanceList();

            // 将instanceIdList放缓存，以备后续使用
            Map<String, Object> instanceListMap = new HashMap<>(5);
            instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
            instanceListMap.put("taskId", taskId);
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskId, instanceListMap);

            // 根据instanceIdList查询inspectionCodeList
            for (Long instanceId : instanceIdList) {
                String inspectionCode = tRobotInfoDao.selectInspectionCode(instanceId);
                str.add(inspectionCode);

                Map<String, String> redisInfoMap = new HashMap<>(16);
                redisInfoMap.put("robotCode", robotCode);
                redisInfoMap.put("instanceId", instanceId + "");
                redisInfoMap.put("inspectionCode", inspectionCode);
                redisInfoMap.put("cruiseTime", DateTimeUtil.format(new Date()));
                redisInfoMap.put("taskId", taskId);
                redisInfoList.add(redisInfoMap);
            }
            for (int i = 0; i < redisInfoList.size(); i++) {
                redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + redisInfoList.get(i).get("robotCode")
                        + ":" + redisInfoList.get(i).get("taskId") + ":" + redisInfoList.get(i).get("instanceId"), redisInfoList.get(i));
            }

            String deviceIdList = str.toString();
            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>(16);
            String type = "";

            if (Objects.isNull(item.getUnionTaskStatus())) {
                log.info("这是正常的任务！！！！！！！！！！！！！");
                type = "101";
                // 巡视类型
                Integer planType = null;
                switch (item.getCruiseType()) {
                    // 全面
                    case 213: planType = 1;break;
                    // 例行
                    case 214: planType = 2;break;
                    // 熄灯
                    case 215:
                    // 专项
                    case 217:
                    // 自定义
                    case 218: planType = 3;break;
                    // 特殊
                    case 216: planType = 4;break;
                    default: break;
                }
                map.put("type", planType);
                map.put("task_code", taskId);
                map.put("task_name", item.getTaskName());
                map.put("priority", item.getPriority());
                map.put("device_level", item.getDeviceLevel());
                map.put("device_list", deviceIdList);
                // 周期任务参数
                if ("172".equals(item.getIfRun())){
                    map.put("cycle_month", Optional.ofNullable(item.getCycleMonth()).orElse(""));
                    map.put("cycle_week", Optional.ofNullable(item.getCycleWeek()).orElse(""));
                    map.put("cycle_execute_time", Optional.ofNullable(item.getCycleExecuteTime()).orElse(""));
                    map.put("cycle_start_time", Optional.ofNullable(item.getCycleStartTime()).orElse(""));
                    map.put("cycle_end_time", Optional.ofNullable(item.getCycleEndTime()).orElse(""));
                    map.put("interval_number", Optional.ofNullable(item.getIntervalNumber()).orElse(""));
                    map.put("interval_type", Optional.ofNullable(item.getIntervalType()).orElse(""));
                    map.put("interval_execute_time", Optional.ofNullable(item.getIntervalExecuteTime()).orElse(""));
                    map.put("interval_start_time", Optional.ofNullable(item.getIntervalStartTime()).orElse(""));
                    map.put("interval_end_time", Optional.ofNullable(item.getIntervalEndTime()).orElse(""));
                    map.put("fixed_start_time", "");
                }
                // 定期和立即任务参数
                else{
                    map.put("fixed_start_time", item.getFixedStartTime());
                    map.put("cycle_month", "");
                    map.put("cycle_week", "");
                    map.put("cycle_execute_time", "");
                    map.put("cycle_start_time", "");
                    map.put("cycle_end_time", "");
                    map.put("interval_number", "");
                    map.put("interval_type", "");
                    map.put("interval_execute_time", "");
                    map.put("interval_start_time", "");
                    map.put("interval_end_time", "");
                }
                map.put("invalid_start_time", "");
                map.put("invalid_end_time", "");
                map.put("isenable", "0");
                map.put("creator", "1");
                map.put("create_time", "");
                mapList.add(map);
            } else {
                log.info("这是联动任务！！！！！！！！！！！！！");
                type = "102";
                map.put("task_code", taskId);
                map.put("task_name", item.getTaskName());
                map.put("priority", 4);
                map.put("device_level", 3);
                map.put("device_list", deviceIdList);
                mapList.add(map);
            }

            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setType(type)
                    .setSendCode(sendCode)
                    .setReceiveCode(robotCode)
                    .setCode("变电站编码")
                    .setTime(DateTimeUtil.format(new Date()))
                    .setCommand("1")
                    .setItems(mapList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的机器人任务的xml是<start>{}<end>", xmlString);
            RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        }
    }

    /**
     * 给机器人下发任务控制指令
     * @param robotTaskControlMap 传来的机器人任务相关信息
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public Result feignRobotTaskControl(Map<String, Object> robotTaskControlMap){
        Result result = new Result();
        log.info("robotTaskControlMap==={}", robotTaskControlMap);

        // 1.任务启动 2.任务暂停 3.任务继续 4.任务停止
        String commandValue = robotTaskControlMap.get("commandValue").toString();
        String json = JSONObject.toJSONString(robotTaskControlMap.get("robotCodeList"));
        log.info("判断条件是==={}", (!"null".equals(json)));
        if (!"null".equals(json)) {
            List<String> robotCodeList = JSON.parseArray(json, String.class);
            log.info("robotCodeList==={}", robotCodeList);

            for (String robotCode : robotCodeList) {
                String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
                if (Objects.equals(OFF_LINE, robotStatus)){
                    result.setMessage(209,"该机器人处于离线状态,没有成功将任务控制下发到机器人......");
                    return result;
                }else {
                    String taskId = robotTaskControlMap.get("taskId").toString();

                    XMLBaseModel xmlBaseModel = new XMLBaseModel()
                            .setType("41")
                            .setSendCode(sendCode)
                            .setReceiveCode(robotCode)
                            .setCode(taskId)
                            .setCommand(commandValue)
                            .setTime(DateTimeUtil.format(new Date()));
                    String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                    log.info("生成的任务控制xml是<start>{}<end>", xmlString);

                    RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);

                    if (Objects.equals("2", commandValue) || Objects.equals("4", commandValue)) {
                        modifyTaskResult(taskId, Integer.valueOf(commandValue));
                    }
                    result.setMessage(200,"该机器人处于离线状态,没有成功将任务控制下发到机器人......");
                }
            }
        }
        return result;
    }
    /**
     * 更新任务状态及已做巡检点结果
     * @param taskId 任务id
     * @param taskStatus 任务状态
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyTaskResult(String taskId, Integer taskStatus)  {
        List<TCruiseDataResult> tcdrList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tctrdList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();

        List<Long> instanceIdDoneList = Constant.flagMap.get(taskId);
        log.info("任务为{}已经做过的巡视点===={}", taskId, instanceIdDoneList);

        for (Long instanceId : instanceIdDoneList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
            log.info("redisInfoMap的数据是{}",redisInfoMap);
            // 缓存中该巡检点有结果
            boolean conditionRes = !StringUtils.equals("设备检修中", redisInfoMap.get("resultNum"))
                && (StringUtils.equals("246", redisInfoMap.get("cruiseResult"))
                || StringUtils.equals("247", redisInfoMap.get("cruiseResult")));
            if (Boolean.TRUE.equals(conditionRes)){
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                        .setTaskResultId(redisInfoMap.get("taskResultId"))
                        .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
                        .setInstanceName(redisInfoMap.get("instanceName"))
                        .setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")))
                        .setEndTime(DateTimeUtil.parse(redisInfoMap.get("endTime")))
                        .setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")))
                        .setDeviceName(redisInfoMap.get("deviceName"))
                        .setCruiseStatus(252)
                        .setRemark(redisInfoMap.get("remark"));
                tctrdList.add(tCruiseTaskResultDetail);
                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));

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
                        .setCreatetime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")))
                        .setIsWarn(0)
                        .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")))
                        .setCruiseAbnormal(Boolean.TRUE.equals(!"null".equals(redisInfoMap.get("cruiseAbnormal"))) ?
                                Integer.valueOf(redisInfoMap.get("cruiseAbnormal")) : null)
                        .setResultPic(Boolean.TRUE.equals(!"null".equals(redisInfoMap.get("resultPic"))) ?
                                redisInfoMap.get("resultPic") : null)
                        .setFirName("f");
                tcdrList.add(tCruiseDataResult);
            }
        }
        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}", taskId, tctrdList.size(), tcdrList.size());

        int res1 = 0;
        int res2 = 0;
        if (CollectionUtils.isNotEmpty(tctrdList)) {
            res1 = batchInsertCruiseTaskResultDetail(tctrdList);
        }
        if (CollectionUtils.isNotEmpty(tcdrList)){
            res2 = batchInsertCruiseDataResult(tcdrList);
            log.info("准备传其他服务的cruiseResultIdList==={}", cruiseResultIdList);
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);
        }
        log.info("插tCTRD的条数:{},插tCDR的条数:{}", res1, res2);

        // 将已经做过的巡视点Map清空
        if (StringUtils.isNotEmpty(Constant.flagMap.get(taskId).toString())){
            log.info("将公共类的instanceIdList清空");
            Constant.flagMap = new HashMap<>(16);
        }

        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);
        Integer totalNum = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
        Integer normalNum  = Integer.valueOf(abnormalCount.get("normal").toString()) ;
        log.info("taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, totalNum, abnormalNum, normalNum);
        Integer abnormal = abnormalNum;
        Integer normal = normalNum;
        Integer taskWait = totalNum - normal - abnormal;
        TCruiseResult tCruiseResult = selectTaskResultId(taskId);
        tCruiseResult.setTaskWait(taskWait);
        if (taskStatus == 2) {
            tCruiseResult.setCState(241);
        }else if (taskStatus == 4) {
            tCruiseResult.setCState(242);
        }
        tCruiseResult.setTaskCode(taskId);
        log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
        // 更新TCR表
        int res = updateTCruiseResult(tCruiseResult);
        log.info("更新TCR的条数===={}", res);

        for (TCruiseTaskResultDetail tctrd : tctrdList){
            updateIsWarn(taskId, tctrd.getInstanceId(), tctrd.getCruiseResultId());
        }
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

    public Long selectRobotIdByCode(String robotCode) {
        return tRobotInfoDao.selectRobotIdByCode(robotCode);
    }
    @Transactional(rollbackFor = Exception.class)
    public String selectRobotNameByCode(String robotCode) {
        return tRobotInfoDao.selectRobotNameByCode(robotCode);
    }
    /**
     * 机器人本体告警入库
     * @param tRobotAlarm 机器人本体告警信息
     * @return int
     */
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
    /**
     * 安全操作机器人返回参数
     * @param key
     * @param userId
     * @param password
     * @param request
     * @return Map<String,Object>
     */
    public Map<String, Object> booleanZcz(String key, Long userId, String password, HttpServletRequest request) throws  Exception{
        SysUser sysUserCurrent = sysUserDao.selectByPrimaryId(userId);
        Map map = new HashMap();
        String keys = Constant.map.get(String.valueOf(userId));
        if (StringUtils.isEmpty(key) && keys == null && StringUtils.isEmpty(password)) {
            String random = String.valueOf((int) (Math.random() * 1000000000 + 1));
            Constant.map.put(String.valueOf(userId), random);
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 1);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "获取密钥");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 1);
            map.put("result", random);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isEmpty(password) && keys != null) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 1);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "获取密钥");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 1);
            map.put("result", keys);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isNoneBlank(password) && keys == null) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 2);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "未输入密钥");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 2);
            map.put("result", "请输入密钥");
            return map;
        } else if (keys == null && StringUtils.isNoneBlank(key) && StringUtils.isNoneBlank(password)) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 2);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "密钥已过期");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 2);
            map.put("result", "密钥已过期");
            return map;
        } else if (!key.equals(keys)) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 2);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "密钥错误");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 2);
            map.put("result", "密钥不正确,请重新输入");
            return map;
        } else if (!Demo.decryptDB(sysUserCurrent.getPassword()).equals(password)) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制机器人");
            params.set("state", 2);
            params.set("userId", userId);
            params.set("userName", sysUserCurrent.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "密码错误");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            map.put("code", 2);
            map.put("result", "用户密码错误，请重新输入");
            return map;
        }
        return map;
    }
    /**
     * 上层服务即集控命令下发
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String upSystemCommand(XMLBaseModel xmlBaseModel){
        xmlBaseModel
                .setSendCode(sendCode)
                .setReceiveCode(Constant.robotCode);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的机器人控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send( generateByteOrder(xmlString, Constant.robotCode), Constant.robotCode);
        return "success";
    }
    /**
     * 向上层服务即集控上报信息
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String upToCruise(XMLBaseModel xmlBaseModel){
        Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        robotMap.put("list", list);
        robotTaskStates(robotMap);
        return "success";
    }
    public Result robotTaskStates(Map<String, List<XMLBaseModel>> robotMap) {
        Result result = new Result();
        result = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
//        try {
//            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
//            if (null != serviceRestTemplate) {
//                result = serviceRestTemplate.postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        return result;
    }
    /**
     * 将机器人传来的信息对应到巡视主机
     * @param valueName 类型名称
     * @param value 值
     * @param colName 巡视主机对应类型
     * @return Integer
     */
    public Integer selectDictCode(String valueName, String value, String colName){
        String dictNote = null;
        if (Objects.equals("appearanceType", valueName)){
            switch (value){
                case "1": dictNote = "电子围栏";break;
                case "2": dictNote = "红外对射";break;
                case "3": dictNote = "泡沫喷淋";break;
                case "4": dictNote = "消防水泵";break;
                case "5": dictNote = "消防栓";break;
                case "6": dictNote = "消防室";break;
                case "7": dictNote = "设备室";break;
                case "8": dictNote = "照明灯";break;
                case "9": dictNote = "摄像头";break;
                case "10": dictNote = "水位线";break;
                case "11": dictNote = "排水泵";break;
                case "12": dictNote = "沉降监测点";break;
                default: break;
            }
        }else if (Objects.equals("meterType", valueName)){
            switch (value){
                case "1": dictNote = "油位表";break;
                case "2": dictNote = "避雷器动作次数表";break;
                case "3": dictNote = "泄漏电流表";break;
                case "4": dictNote = "SF6压力表";break;
                case "5": dictNote = "液压表";break;
                case "6": dictNote = "开关动作次数表";break;
                case "7": dictNote = "油温表";break;
                case "8": dictNote = "档位表";break;
                case "9": dictNote = "气压表";break;
                default: break;
            }
        }else if (Objects.equals("deviceType", valueName)){
            switch (value){
                case "1": dictNote = "油浸式变压器";break;
                case "2": dictNote = "断路器";break;
                case "3": dictNote = "组合电器";break;
                case "4": dictNote = "隔离开关";break;
                case "5": dictNote = "开关柜";break;
                case "6": dictNote = "电流互感器";break;
                case "7": dictNote = "电压互感器";break;
                case "8": dictNote = "避雷器";break;
                case "9": dictNote = "并联电容器组";break;
                case "10": dictNote = "干式电抗器";break;
                case "11": dictNote = "串联补偿装置";break;
                case "12": dictNote = "母线及绝缘子";break;
                case "13": dictNote = "穿墙套管";break;
                case "14": dictNote = "消弧线圈";break;
                case "15": dictNote = "高频阻波器";break;
                case "16": dictNote = "耦合电容器";break;
                case "17": dictNote = "高压熔断器";break;
                case "18": dictNote = "中性点隔直装置";break;
                case "19": dictNote = "接地装置";break;
                case "20": dictNote = "端子箱及检修电源箱";break;
                case "21": dictNote = "站用变";break;
                case "22": dictNote = "站用交流电源";break;
                case "23": dictNote = "站用直流电源";break;
                case "24": dictNote = "构支架";break;
                case "25": dictNote = "辅助设施";break;
                case "26": dictNote = "土建设施";break;
                case "27": dictNote = "避雷针";break;
                case "28": dictNote = "避雷器动作次数表";break;
                default: break;
            }
        }
        return Integer.valueOf(selectDictCodeByNote(dictNote, colName));
    }
    /**
     * 若修改机器人编码或删除机器人断开在线的机器人连接
     * @param robotCode 机器人唯一标识
     * @param robotId  机器人id
     * @return String
     */
    public String removeLink(String robotCode, Long robotId){
        RobotServerHandler.removeLink(robotCode);
        /*TRobotInfo tRobotInfo = new TRobotInfo()
                .setRobotId(robotId)
                .setRobotStatus(OFF_LINE);
        int res = tRobotInfoDao.update(tRobotInfo);
        log.info("修改成功==="+res);*/
        return OFF_LINE;
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
    /**
     * 根据测点查询关联算法信息
     * @param deviceMeteId  测点信息
     * @return List<TAlgorithmInfo>
     */
    public List<TAlgorithmInfo> selectByDeviceMeteId(Long deviceMeteId){
        return this.tRobotInfoDao.selectByDeviceMeteId(deviceMeteId);
    }
    /**
     * 根据测点id查询测点信息
     * @param deviceMeteId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMete(Long deviceMeteId){
        return this.tRobotInfoDao.selectDeviceMete(deviceMeteId);
    }
    /**
     * 机器人站端任务信息入库
     * @param taskModelMapList 机器人本体任务相关数据
     * @param xmlBaseModel xml格式的内容
     * @return int
     */
    public int addRobotSelfTask(List<Map<String, Object>> taskModelMapList, XMLBaseModel xmlBaseModel) {
        List<TCruiseTask> tCruiseTaskList = new ArrayList<>();
        for (Map<String, Object> taskModelMap : taskModelMapList) {
            // 构建t_cruise_task
            String taskId = taskModelMap.get("task_code").toString();
            String taskName = taskModelMap.get("task_name").toString();

            TCruiseTask tCruiseTask = new TCruiseTask();
            tCruiseTask.setTaskId(taskId);
            tCruiseTask.setTaskCode(taskId);
            tCruiseTask.setTaskName(taskName);
            Integer cType = null;
            if (StringUtils.isNotEmpty(null)){
                String type = taskModelMap.get("type").toString();
                switch (type){
                    // 例行
                    case "1": cType = 214;break;
                    // 特殊
                    case "2":cType = 216;break;
                    // 专项
                    case "3":cType = 217;break;
                    // 自定义
                    case "4":cType = 218;break;
                    default:break;
                }
            }
            tCruiseTask.setType(cType);
            if (!"".equals(taskModelMap.get("cycle_execute_time")) || !"".equals(taskModelMap.get("interval_start_time"))){
                tCruiseTask.setIfRun(172);
            }else{
                tCruiseTask.setIfRun(174);
            }
            tCruiseTask.setStartTime(DateTimeUtil.parse(taskModelMap.get("fixed_start_time").toString()));
            Long robotId = tRobotInfoDao.selectRobotIdByCode(xmlBaseModel.getSendCode());
            tCruiseTask.setRobotId(robotId);
            // 机器人本体任务
            tCruiseTask.setTaskType(271);
            if (!"".equals(taskModelMap.get("priority").toString())){
                tCruiseTask.setTaskLevel(Integer.valueOf(taskModelMap.get("priority").toString()));
            }
            tCruiseTask.setCreateTime(new Date());
            tCruiseTaskList.add(tCruiseTask);
            log.info("tCruiseTaskList==={}", tCruiseTaskList);

            // 该任务下包含的点位
            String inspectionIdString = taskModelMap.get("device_list").toString();
            String[] inspectionIds = inspectionIdString.split(",");
            List<String> inspectionIdList = new ArrayList<>();
            Collections.addAll(inspectionIdList, inspectionIds);
            log.info("机器人对应点list==={}", inspectionIdList);

            // 构建t_cruise_point_instance
            List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>();
            for (String inspectionCode : inspectionIdList){
                TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
                TStdRegion tStdRegion = tRobotInfoDao.selectTSRegionForStation();
                tCruisePointInstance.setStationId(tStdRegion.getStationId());
                tCruisePointInstance.setStationName(tStdRegion.getStationName());
                tCruisePointInstance.setCruiseType(228);
                Map<String,Object> map = tRobotInspectionDao.selectInspection(inspectionCode);
                tCruisePointInstance.setCruiseId(Long.valueOf(map.get("inspection_id").toString()));
                tCruisePointInstance.setCruiseName(map.get("inspection_name").toString());
                tCruisePointInstance.setIfSy(1);
                tCruisePointInstanceList.add(tCruisePointInstance);
            }
            log.info("tCruisePointInstanceList==={}", tCruisePointInstanceList);
            tRobotInfoDao.batchInsertInstance(tCruisePointInstanceList);

            // 构建t_cruise_task_attr
            List<Map<String, String>> redisInfoList = new ArrayList<>();
            List<Long> instanceIdList = new ArrayList<>();
            List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
            for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList){
                TCruiseTaskAttr tCruiseTaskAttr = new TCruiseTaskAttr()
                        .setTaskId(taskId)
                        .setInstanceId(tCruisePointInstance.getInstanceId());
                tCruiseTaskAttrList.add(tCruiseTaskAttr);
                instanceIdList.add(tCruisePointInstance.getInstanceId());

                Map<String, String> redisInfoMap = new HashMap<>();
                redisInfoMap.put("robotCode", xmlBaseModel.getSendCode());
                redisInfoMap.put("instanceId", tCruisePointInstance.getInstanceId() + "");
                Map<String,Object> map = tRobotRegionDao.selectInspectionId(tCruisePointInstance.getCruiseId());
                redisInfoMap.put("inspectionCode", map.get("inspection_code").toString());
                redisInfoMap.put("deviceName", map.get("region_name").toString());
                redisInfoMap.put("taskId", taskId);
                redisInfoMap.put("inspectionName", tCruisePointInstance.getCruiseName());
                redisInfoList.add(redisInfoMap);
            }
            log.info("redisInfoList是==={}", redisInfoList);
            // 放数据到缓存
            for (int i = 0; i < redisInfoList.size(); i++) {
                redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + redisInfoList.get(i).get("robotCode")
                        + ":" + redisInfoList.get(i).get("taskId") + ":" +redisInfoList.get(i).get("instanceId"), redisInfoList.get(i));
            }

            log.info("tCruiseTaskAttrList==={}", tCruiseTaskAttrList);
            tRobotInfoDao.batchInsertTaskAttr(tCruiseTaskAttrList);

            // 将instanceIdList放缓存，以备后续使用
            log.info("instanceIdList是==={}", instanceIdList);
            Map<String, Object> instanceListMap = new HashMap<>(5);
            instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
            instanceListMap.put("taskId", taskId);
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + xmlBaseModel.getSendCode() + ":" +taskId, instanceListMap);

            // 构建t_cruise_result
            TCruiseResult tCruiseResult = new TCruiseResult()
                    .setTaskResultId(String.valueOf(UUID.randomUUID()).replace("-", ""))
                    .setTaskId(taskId)
                    .setTaskName(taskName)
                    .setCType(cType)
                    .setCState(238)
                    .setTaskCode(taskId)
                    .setCreateTime(new Date())
                    .setRemark("0");
            log.info("tCruiseResult==={}", tCruiseResult);
            tRobotInfoDao.insertTCruiseResult(tCruiseResult);
        }
        return  tRobotInfoDao.batchInsertTask(tCruiseTaskList);
    }
    /**
     * 机器人巡检点告警及分析机器人巡检点结果后的告警
     * @param warnInfo 告警信息
     * @return int
     */
    public int insertWarn(TWarnInfo warnInfo){
        return tRobotInfoDao.insertWarn(warnInfo);
    }
    /**
     * 根据任务id和巡检点id查询告警id
     * @param taskId 任务id
     * @param instanceId 巡检点id
     * @return Long
     */
    public Long selectWarnId(String taskId, Long instanceId){
        return tRobotInfoDao.selectWarnId(taskId, instanceId);
    }

    /**
     * 判断是否生成告警，若是则更新任务结果表
     * @param taskId 任务id
     * @param instanceId 巡检点id
     * @param cruiseResultId 任务结果巡检点id
     * @return int
     */
    public int updateIsWarn(String taskId, Long instanceId, String cruiseResultId){
        int isWarnFlag = tRobotInfoDao.selectIsWarn(instanceId, taskId);
        if (isWarnFlag > 0){
            tRobotInfoDao.updateIsWarn(cruiseResultId);
        }
        return isWarnFlag;
    }
    /**
     * 根据巡视点id查询该测点信息
     * @param instanceId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId){
        return tRobotInfoDao.selectDeviceMeteInfo(instanceId);
    }
   /**
    * 下发获得机器人控制权及任务模式切换的指令
    * @param robotCode 机器人唯一标识
    * @param type 类型
    * @param command 命令
    * @param value 值
    * @return String
    */
    @Transactional(rollbackFor = Exception.class)
    public String robotModeSwitch(String robotCode, String type, String command, String value) throws InterruptedException {
        List<Map<String, Object>> item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>(5);
        if (!"".equals(value) || null != value) {
            map.put("value", value);
        }
        item.add(map);

        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(robotCode)
                .setCode(robotCode)
                .setTime(DateTimeUtil.format(new Date()))
                .setType(type)
                .setCommand(command)
                .setItems(item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的机器人控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        TimeUnit.MILLISECONDS.sleep(1000);

        return RobotServerHandler.getRobotResultMap().get("Code").toString();
    }
    /**
     * 判断任务是否属于机器人本体任务
     * @param taskId 任务id
     * @return Long
     */
    public Long selectIsRobotTask(String taskId){
        return tRobotInfoDao.selectIsRobotTask(taskId);
    }
    /**
     * 字典值查询
     * @param dictNote 说明
     * @param colName 类型
     * @return String
     */
    public String selectDictCodeByNote(String dictNote, String colName){
        return tRobotInfoDao.selectDictCodeByNote(dictNote,colName);
    }
    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     * @param taskId 任务id
     * @return Long
     */
    public List<Long> selectInstanceForTaskGoOn(String taskId){
        return tRobotInfoDao.selectInstanceForTaskGoOn(taskId);
    }
    /**
     * 根据type和command获取日志记录内容
     * @param type 类型
     * @param command 命令
     * @param value 值
     * @return String
     */
    public String selectContentByCommand(String type, String command, String value) {
        if ("1".equals(type)) {
            switch (command) {
                case "1": return "机器人远方复位";
                case "2": return "机器人系统自检";
                case "3": return "机器人一键返航";
                case "4": return "机器人手动充电";
                case "5":
                    if ("1".equals(value)) { return "机器人控制模式切换为任务模式";
                    } else if ("2".equals(value)) { return "机器人控制模式切换为紧急定位模式";
                    } else if ("3".equals(value)) { return "机器人控制模式切换为后台遥控模式";
                    } else if ("4".equals(value)) { return "机器人控制模式切换为手持遥控模式";
                    }
                    break;
                case "6": return "机器人控制权获得";
                case "7": return "机器人控制权释放";
                default: break;
            }
        } else if ("2".equals(type)) {
            switch (command) {
                case "1": return "机器人前进";
                case "2": return "机器人后退";
                case "3": return "机器人左转";
                case "4": return "机器人右转";
                case "5":
                    if ("1".equals(value)) { return "机器人左转弯";
                    } else if ("2".equals(value)) { return "机器人右转弯";
                    }
                    break;
                case "6": return "机器人停止";
                default: break;
            }
        } else if ("3".equals(type)) {
            switch (command) {
                case "1": return "机器人云台上仰";
                case "2": return "机器人云台下俯";
                case "3": return "机器人云台左转";
                case "4": return "机器人云台右转";
                case "5": return "机器人云台上升";
                case "6": return "机器人云台下降";
                case "7": return "机器人云台预置位调用";
                case "8": return "机器人云台停止";
                case "9": return "机器人云台复位";
                case "10": return "机器人云台预置位新增";
                case "11": return "机器人云台预置位修改";
                case "12": return "机器人云台预置位删除";
                default: break;
            }
        } else if ("4".equals(type)) {
            switch (command) {
                case "1":
                    if ("1".equals(value)) {
                        return "打开机器人红外电源";
                    } else if ("2".equals(value)) {
                        return "关闭机器人红外电源";
                    }
                    break;
                case "2":
                    if ("1".equals(value)) {
                        return "打开机器人雨刷";
                    } else if ("2".equals(value)) {
                        return "关闭机器人雨刷";
                    }
                    break;
                case "3":
                    if ("1".equals(value)) {
                        return "打开机器人超声";
                    } else if ("2".equals(value)) {
                        return "关闭机器人超声";
                    }
                    break;
                case "4":
                    if ("1".equals(value)) {
                        return "打开机器人红外射灯";
                    } else if ("2".equals(value)) {
                        return "关闭机器人红外射灯";
                    }
                    break;
                default:
                    break;
            }
        } else if ("21".equals(type)) {
            switch (command) {
                case "1": return "机器人可见光摄像机镜头拉近";
                case "2": return "机器人可见光摄像机镜头拉远";
                case "3": return "机器人可见光摄像机镜头拉焦停止";
                case "4": return "机器人可见光摄像机焦距增加";
                case "5": return "机器人可见光摄像机焦距减少";
                case "6": return "机器人可见光摄像机自动聚焦";
                case "7": return "机器人可见光摄像机抓图";
                case "8": return "机器人可见光摄像机重启";
                case "9": return "机器人可见光摄像机启动录像";
                case "10": return "机器人可见光摄像机停止录像";
                case "11": return "机器人可见光摄像机倍率值设置";
                case "12": return "机器人可见光摄像机聚焦值设置";
                default: break;
            }
        } else if ("22".equals(type)) {
            switch (command) {
                case "5": return "机器人红外热像仪设定焦距值";
                case "6": return "机器人红外热像仪自动聚焦";
                case "7": return "机器人红外热像仪抓图";
                case "8": return "机器人红外热像仪重启";
                default: break;
            }
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String localPath, String targetName) {
        try {
            if("".equals(localPath)) {return;}
            FtpsUtil.putFile(ftpsLocalPath + "/" + localPath, targetName,
                    serverUrl, Integer.valueOf(ftpsPort), key, ftpsUserName, ftpsPassWord);
        } catch (Exception e) {
            log.error("上传至ftps错误 " + e);
        }
    }

    /**
     * 离线情况同步机器人模型信息-上传文件
     * @param file 文件
     * @param robotCode 机器人唯一标识
     * @return Result
     */
    public Result upLoadRobotModel(MultipartFile file, String robotCode){
        Result result = new Result();
        try{
            if(file == null){
                result.setCode(209,"文件错误，文件为null");
                result.setData(0);
                return result;
            }
            String fileName = "copy-robotModel.xml";
            String pathName = excelDataImport(file, fileName);
            // 解析xml文件
            XMLBaseModel robotModel = getXmlMessage(pathName);
            List<Map<String,Object>> robotMap = robotModel.getItems();
            Long robotId = selectRobotIdByCode(robotCode);
            // 机器人模型信息入库
            addRobotModel(robotMap, robotId);
        }catch (Exception e){
            log.info(e.getMessage());
        }
        return result;
    }
    /**
     * 离线情况同步机器人设备文件-上传文件
     * @param file 文件
     * @param robotCode 机器人唯一标识
     * @return Result
     */
    public Result upLoadRobotDevice(MultipartFile file, String robotCode){
        Result result = new Result();
        try{
            if(file == null){
                result.setCode(209,"文件错误，文件为null");
                result.setData(0);
                return result;
            }
            String fileName = "copy-robotDevice.xml";
            String pathName = excelDataImport(file,fileName);
            // 解析xml文件
            XMLBaseModel robotDevice = getXmlMessage(pathName);
            List<Map<String,Object>> deviceMap = robotDevice.getItems();
            Long robotId = selectRobotIdByCode(robotCode);
            // 机器人设备点位入库
            addDevicePoint(deviceMap, robotId);
            addDevicePointRegion(deviceMap, robotId);
        }catch (Exception e){
            log.info(e.getMessage());
        }
        return result;
    }
    /**
     * 将上传文件写入临时文件
     * @param file 文件
     * @param fileName 文件名
     * @return String
     */
    private String excelDataImport(MultipartFile file, String fileName) {
        Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String path = (String) mapForPicModelPath.get("content");
        // 将上传文件写入
        try {
            deleteDir(new File(path + File.separator + fileName));
            file.transferTo(new File(path + File.separator + fileName));
        }catch (NullPointerException | IOException e) {
            e.getMessage();
        }
        return path +"/"+ fileName;
    }
    /**
     * 判断站端的任务是否在巡视主机上已完成
     * @param robotCode 机器人唯一标识
     * @return String
     */
    public List<String> hasStandTaskIsFinish(String robotCode){
        Long robotId = selectRobotIdByCode(robotCode);
        List<String> taskIdList = tRobotInfoDao.HasStandTaskIsFinish(robotId);
        log.info("站端在巡视主机显示未完成的任务=={}", taskIdList);
        if (CollectionUtils.isNotEmpty(taskIdList)){
            for (String taskId : taskIdList){
                Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode+":"+taskId);
                Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());
                try {
                    if (1 == taskState){
                        tRobotInfoDao.updateStandTaskStatus(taskId,240);
                    }
                }catch (Exception e){
                    e.getMessage();
                }
            }
        }
        return taskIdList;
    }

    public int deleteInstanceId(List<Long> instanceIdList){
        for (Long instanceId : instanceIdList){
            tRobotInfoDao.deleteInstanceId(instanceId);
        }
        return 1;
    }
}

