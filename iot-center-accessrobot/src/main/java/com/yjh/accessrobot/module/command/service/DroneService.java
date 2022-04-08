package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.LogsAspect;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.SysUserDao;
import com.yjh.accessrobot.module.command.dao.TDroneInfoDao;
import com.yjh.accessrobot.module.command.dao.TDroneInspectionDao;
import com.yjh.accessrobot.module.command.dao.TDroneRegionDao;
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
import java.net.URISyntaxException;
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
public class DroneService {

    private Logger log = LoggerFactory.getLogger(DroneService.class);

    String todayTime = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    private static final String OFF_LINE = "离线";

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TDroneInfoDao tDroneInfoDao;
    @Autowired
    private TDroneInspectionDao tDroneInspectionDao;
    @Autowired
    private TDroneInspectionAttrService TDroneInspectionService;
    @Autowired
    private TDroneRegionDao tDroneRegionDao;
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

    @Value("${other.webSocketUrl}")
    private String websocketUrl;


    @Transactional(rollbackFor = Exception.class)
    public int updateAllDroneStatus() {
        return tDroneInfoDao.updateAllDroneState("离线");
    }

    /**
     * 巡视主机下发控制指令到无人机
     *
     * @param droneCode 无人机唯一标识
     * @param type      类型
     * @param command   命令
     * @param value     值
     * @param direction 描述
     * @param key
     * @param userId
     * @param password
     * @param request
     * @param content
     * @return Map<String, Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> feignDroneControl(String droneCode, String type, String command, String value, String direction, String key, Long userId, String password, HttpServletRequest request, String content) throws Exception {
        Map scmap = new HashMap();
//        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
//        if (command.equals("1") && type.equals("1")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        } else if (command.equals("3") && type.equals("1")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        } else if (command.equals("5") && type.equals("1")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        } else if (command.equals("6") && type.equals("1")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        } else if (command.equals("9") && type.equals("3")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        } else if (command.equals("8") && type.equals("22")) {
//            Map<String, Object> zcz = this.booleanZcz(key, userId, password,request);
//            if (zcz.get("code") != null) {
//                return zcz;
//            }
//        }
//        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
//        params.set("logType", "5");
//        params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
//        params.set("title", "控制无人机");
//        params.set("state", 1);
//        params.set("userId", userId);
//        params.set("userName", userName);
//        params.set("requestOrigin", request.getRequestURL());
//        params.set("requestPath", request.getRequestURI());
//        params.set("requestMethod", request.getMethod());
//        /*String operationContent = selectContentByCommand(type,command,value);
//        params.set("content",operationContent);*/
//        params.set("content", content);
//        LogsAspect logsAspect = new LogsAspect();
//        logsAspect.post(params);
        List<Map<String, Object>> item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>(3);
        if (StringUtils.isNotEmpty(value)) {
            map.put("value", value);
        }
        if (StringUtils.isNotEmpty(direction)) {
            map.put("direction", direction);
        }
        item.add(map);

        if (StringUtils.isEmpty(droneCode)) {
            log.error("==========没有设置无人机编码==========");
            scmap.put("code", 3);
            scmap.put("result", "当前不存在无人机编码,请先添加");
            return scmap;
        }
        String droneStatus = tDroneInfoDao.selectStatusByDroneCode(droneCode);
        if (StringUtils.equals(OFF_LINE, droneStatus)) {
            log.error("==========该无人机处于离线状态,没有成功将控制指令下发到无人机==========");
            scmap.put("code", 3);
            scmap.put("result", "无人机不在线");
            return scmap;
        } else {
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(droneCode)
                    .setCode(droneCode)
                    .setTime(DateTimeUtil.format(new Date()))
                    .setType(type)
                    .setCommand(command)
                    .setItems(item);
            String xmlString = PlatformXMLUtil.generateXml2(xmlBaseModel, PlatformXMLUtil.DRONEROOTNAME);

            Map<String, String> droneStatusMap = redisTemplate.opsForHash().entries("DroneStatus:" + droneCode + ":61");
            String dronePattern = droneStatusMap.get("value");
            boolean flag = StringUtils.equals("1", dronePattern) && !("1".equals(type) && "5".equals(command));
            if (Boolean.TRUE.equals(flag)) {
                log.error("==========当前无人机处于任务模式,请切换模式==========");
                scmap.put("code", 3);
                scmap.put("result", "当前无人机处于任务模式,请切换模式");
                return scmap;
            } else {
                RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);

                // Capture and video file return
                String filePath = null;
                if (Objects.nonNull(RobotServerHandler.getRobotResultMap().get("Item"))) {
                    String ftpFilePath = JSONObject.parseObject(JSON.toJSONString(RobotServerHandler.getRobotResultMap().get("Item"))).get("file_path").toString();
                    String[] sArray = ftpFilePath.split("/");
                    String ftpFileName = sArray[sArray.length - 1];

                    Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
                    filePath = relativeImgMap.get("content") + "/" + todayTime + "/CameraLib/";
                    if (ftpFileName.endsWith(".jpg")) {
                        filePath = filePath + "BigImg/" + ftpFileName;
                    } else if (ftpFileName.endsWith(".bmp")) {
                        filePath = filePath + "Infrared/" + ftpFileName;
                    } else if (ftpFileName.endsWith(".mp4")) {
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
     * 巡视主机下发模型文件同步指令到无人机
     *
     * @param droneCode 无人机唯一标识
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean feignDroneTransfer(String droneCode) {
        String droneStatus = tDroneInfoDao.selectStatusByDroneCode(droneCode);
        if (StringUtils.equals(OFF_LINE, droneStatus)) {
            log.error("==========该无人机处于离线状态,没有成功将模型文件同步指令下发到无人机==========");
            return false;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(droneCode)
                .setCode("变电站编码")
                .setTime(DateTimeUtil.format(new Date()))
                .setType("61")
                .setCommand("1");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
        return true;
    }

    /**
     * 生成发送byte指令，附带测试
     *
     * @param xmlString xml数据
     * @param droneCode 无人机唯一标识
     * @return byte[]
     */
    public byte[] generateByteOrder(String xmlString, String droneCode) {
        long sendSessionId = Constant.sendSessionId;
        ChannelHandlerContext context = RobotServerHandler.getChannelHandlerContextByRobot(droneCode);
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
     * 更新无人机的在线状态
     *
     * @param droneCode   无人机唯一标识
     * @param droneStatus 在线状态
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateDroneInfo(String droneCode, String droneStatus) {
        Long droneId = tDroneInfoDao.selectDroneIdByCode(droneCode);
        TDroneInfo tDroneInfo = new TDroneInfo()
                .setDroneId(droneId)
                .setDroneState(droneStatus);
        int res = tDroneInfoDao.update(tDroneInfo);
        log.info("droneCode为==={},droneId为==={}的无人机状态是==={},修改结果==={}", droneCode, droneId, tDroneInfo.getDroneState(), res);
        return res;
    }

    /**
     * 查询表中所有的无人机
     *
     * @return List<String>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllDroneCode() {
        return tDroneInfoDao.selectAllDroneCode();
    }

    /**
     * 处理无人机返回的模型文件
     *
     * @param map 无人机返回的模型文件相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDroneFile(Map<String, String> map) throws Exception {
        String droneCode = map.get("droneCode");
        if (StringUtils.isEmpty(droneCode)) {
            log.error("无人机编码为空,droneCode：{}", droneCode);
            throw new RuntimeException("无人机编码为空");
        }
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");

        Object deviceFile = map.getOrDefault("deviceFile", "");
        Object droneFile = map.getOrDefault("droneFile", "");
        Object propertyFile = map.getOrDefault("propertyFile", "");
        XMLBaseModel deviceModel = getXmlMessage(filePathMap.get("content") + File.separator + deviceFile);
        List<Map<String, Object>> deviceMap = deviceModel.getItems();
        XMLBaseModel droneModel = getXmlMessage(filePathMap.get("content") + File.separator + droneFile);
        List<Map<String, Object>> droneMap = droneModel.getItems();
        List<Map<String, Object>> propertyMap = new ArrayList<>();
        if (!"".equals(propertyFile)) {
            XMLBaseModel propertyModel = getXmlMessage(filePathMap.get("content") + File.separator + propertyFile);
            propertyMap = propertyModel.getItems();
        }
        if (CollectionUtils.isNotEmpty(deviceMap) && CollectionUtils.isNotEmpty(droneMap)) {
            Long droneId = tDroneInfoDao.selectDroneIdByCode(droneCode);
            // Drone Model Info
            addDroneModel(droneMap, droneId);
            // Device Point Info
            addDevicePoint(deviceMap, droneId);
            //Property Info 属性信息 与点位绑定
            addPropertyModel(propertyMap, droneId);
            // Device Point Region Info
            addDevicePointRegion(deviceMap, droneId);
        }
    }

    /**
     * 无人机模型文件信息处理
     *
     * @param droneMap 模型文件信息
     * @param droneId  无人机id
     * @return void
     */
    public void addDroneModel(List<Map<String, Object>> droneMap, Long droneId) {
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");

        String picPath = filePathMap.get("content") + "/" + droneMap.get(0).get("mappath").toString();
        log.info("图片路径为：{}", picPath);

        String[] splitArray = picPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        String developMap = absoluteImgMap.get("content") + "/Map";
        copyFileToDevelop(picPath, developMap);
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;
        TDroneInfo tDroneInfo = new TDroneInfo()
                .setDroneId(droneId);
//                .setPhotePath(developRelativeUrl);
        tDroneInfoDao.update(tDroneInfo);
    }

    /**
     * 无人机设备点位文件信息处理
     *
     * @param deviceMapList 设备点位文件信息
     * @param droneId       无人机id
     * @return void
     */
    public void addDeviceModel(List<Map<String, Object>> deviceMapList, Long droneId) {

    }

    /**
     * @param propertyMapList 属性文件信息
     */
    private void addPropertyModel(List<Map<String, Object>> propertyMapList, Long droneId) {
        if (CollectionUtils.isNotEmpty(propertyMapList)) {
            List<TDroneInspectionAttr> droneInspectionAttrList = new ArrayList<>();
            for (Map<String, Object> propertyMap : propertyMapList) {
                TDroneInspectionAttr tDroneInspectionAttr = new TDroneInspectionAttr()
                        .setDroneId(droneId)
                        .setInspectionCode(propertyMap.get("device_id").toString())
                        .setX(Integer.parseInt(propertyMap.get("x").toString()))
                        .setY(Integer.parseInt(propertyMap.get("y").toString()))
                        .setWidth(Integer.parseInt(propertyMap.get("width").toString()))
                        .setHeight(Integer.parseInt(propertyMap.get("height").toString()));
                droneInspectionAttrList.add(tDroneInspectionAttr);
            }
            //删除该无人机的测点属性表
            TDroneInspectionService.deleteByDroneId(droneId);
            TDroneInspectionService.batchInsert(droneInspectionAttrList);
        }
    }

    /**
     * 更新无人机测点信息
     *
     * @param deviceMapList 设备点位文件信息
     * @param droneId       无人机id
     * @return void
     */
    private void addDevicePoint(List<Map<String, Object>> deviceMapList, Long droneId) {
        List<TDroneInspection> deviceList = new ArrayList<>();
        for (Map<String, Object> deviceMap : deviceMapList) {
            TDroneInspection tDroneInspection = new TDroneInspection()
                    .setInspectionCode(deviceMap.get("device_id").toString())
                    .setDroneId(droneId)
                    .setInspectionName((deviceMap.get("main_device_name").toString() + "/" + deviceMap.get("device_name").toString()))
                    .setSaveTypeList(deviceMap.get("save_type_list").toString())
                    .setComponentId(deviceMap.get("main_device_id").toString())
                    .setRecognitionTypeList(deviceMap.get("recognition_type_list").toString());
            /*if (!"".equals(deviceMap.get("component_id").toString())){
                tDroneInspection.setComponentId(deviceMap.get("component_id").toString());
            }*/
            int inspectionType = 1;
            if (deviceMap.containsKey("point_type") && !"".equals(deviceMap.get("point_type").toString())) {
                inspectionType = Integer.parseInt(deviceMap.get("point_type").toString());
            }
            tDroneInspection.setInspectionType(inspectionType);
            if (!"".equals(deviceMap.get("meter_type").toString())) {
                Integer meterType = selectDictCode("meterType", deviceMap.get("meter_type").toString(), "meter_type");
                tDroneInspection.setMeterType(meterType);
            }
            if (!"".equals(deviceMap.get("appearance_type").toString())) {
                Integer appearanceType = selectDictCode("appearanceType", deviceMap.get("appearance_type").toString(), "appearance_type");
                tDroneInspection.setAppearanceType(appearanceType);
            }
            if (deviceMap.containsKey("main_operation_type") && !"".equals(deviceMap.get("main_operation_type").toString())) {
                Integer mainOperationType = selectDictCode("mainOperationType", deviceMap.get("main_operation_type").toString(), "main_operation_type");
                tDroneInspection.setMainOperationType(mainOperationType);
            }
            if (deviceMap.containsKey("operation_type") && !"".equals(deviceMap.get("operation_type").toString())) {
                Integer operationType = selectDictCode("operationType", deviceMap.get("operation_type").toString(), "operation_type");
                tDroneInspection.setOperationType(operationType);
            }
            if (!"".equals(deviceMap.get("phase").toString())) {
                tDroneInspection.setPhase(deviceMap.get("phase").toString());
            }
            if (!"".equals(deviceMap.get("device_info").toString())) {
                tDroneInspection.setDeviceInfo(deviceMap.get("device_info").toString());
            }
            if (deviceMap.containsKey("property_pic_path") && !"".equals(deviceMap.get("property_pic_path").toString())) {
                String relativePropertyPicPath = convertPropertyPicPath(deviceMap.get("property_pic_path").toString());
                tDroneInspection.setPropertyPicPath(relativePropertyPicPath);
            }

            deviceList.add(tDroneInspection);
//            log.info("获得的deviceList是：" + deviceList);
        }

        // 该无人机现有的巡检点
        List<String> nowList = tDroneInspectionDao.selectAllByDroneId(droneId);
//        log.info("无人机现有的deviceList是：" + nowList);

        List<TDroneInspection> addList = new ArrayList<>();
        for (TDroneInspection str : deviceList) {
            if (nowList.contains(str.getInspectionCode())) {
                nowList.remove(str.getInspectionCode());
            } else {
                addList.add(str);
            }
        }
        log.info("最后要插库的deviceList是==={}", addList);
        log.info("准备要删除的inspectionCodeList是==={}", nowList);
        if (CollectionUtils.isNotEmpty(nowList)) {
            List<Long> inspectionIdList = tDroneInspectionDao.selectInspectionIdList(nowList);
//            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
            List<Long> instanceIdList = tDroneInspectionDao.selectInstanceIdList(inspectionIdList);
//            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

            // 删库TRI
            int res1 = 0, res2 = 0, res3 = 0;
            if (CollectionUtils.isNotEmpty(inspectionIdList)) {
                res1 = tDroneInspectionDao.batchDeleteTDroneInspection(inspectionIdList);
            }
            if (CollectionUtils.isNotEmpty(instanceIdList)) {
                // 删库TCPI
                res2 = tDroneInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);
                // 删库TCPA
                res3 = tDroneInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);
            }
            log.info("TRI删除条数=={},TCPI删除条数=={},TCPA删除条数=={}", res1, res2, res3);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            // 插库TRI
            int res = tDroneInspectionDao.batchInsertTDroneInspection(addList);
            log.info("TRI插入条数=={}", res);
        }
        if (deviceList.containsAll(addList)) {
            deviceList.removeAll(addList);
        }
        log.info("准备更新的deviceList是=={}", deviceList);
        for (TDroneInspection item : deviceList) {
            // 更新TRI
            tDroneInspectionDao.update(item);
        }
    }

    /**
     * @param path 属性图地址
     * @return
     */
    private String convertPropertyPicPath(String path) {
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        String propertyPicPath = filePathMap.get("content") + "/" + path;
        log.info("属性图片路径为：{}", propertyPicPath);

        String[] splitArray = propertyPicPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        String developMap = absoluteImgMap.get("content") + "/PRO";
        copyFileToDevelop(propertyPicPath, developMap);
        return relativeImgMap.get("content") + "/PRO/" + fileName;
    }

    /**
     * 更新无人机测点区域信息
     *
     * @param deviceMapList 设备点位文件信息
     * @param droneId       无人机id
     * @return void
     */
    private void addDevicePointRegion(List<Map<String, Object>> deviceMapList, Long droneId) {
        List<TDroneRegion> tDroneRegionList = new ArrayList<>();

        for (Map<String, Object> deviceMap : deviceMapList) {
            // 只有主设备信息
            TDroneRegion tr3 = new TDroneRegion();
            tr3.setRegionId(deviceMap.get("main_device_id").toString());
            tr3.setRegionName(deviceMap.get("main_device_name").toString());
            tr3.setUpRegionId(deviceMap.get("bay_id").toString());
            tr3.setDroneId(droneId);
            Integer deviceType = selectDictCode("deviceType", deviceMap.get("device_type").toString(), "device_type");
            tr3.setDeviceType(deviceType);
            tDroneRegionList.add(tr3);

            /// 区域、间隔、主设备信息都有的情况,先留着
            /*TDroneRegion tr1 = new TDroneRegion();
            tr1.setRegionId(deviceMap.get("place_id").toString());
            tr1.setRegionName(deviceMap.get("place_name").toString());
            tr1.setUpRegionId("-1");
            tDroneRegionList.add(tr1);

            TDroneRegion tr2 = new TDroneRegion();
            tr2.setRegionId(deviceMap.get("bay_id").toString());
            tr2.setRegionName(deviceMap.get("bay_name").toString());
            tr2.setUpRegionId(deviceMap.get("place_id").toString());
            tDroneRegionList.add(tr2);

            TDroneRegion tr3 = new TDroneRegion();
            tr3.setRegionId(deviceMap.get("main_device_id").toString());
            tr3.setRegionName(deviceMap.get("main_device_name").toString());
            tr3.setUpRegionId(deviceMap.get("bay_id").toString());
            Integer deviceType = selectDictCode("deviceType",deviceMap.get("device_type").toString(),"device_type");
            tr3.setDeviceType(deviceType);
            tDroneRegionList.add(tr3);

            TDroneRegion tr4 = new TDroneRegion();
            tr4.setRegionId(deviceMap.get("component_id").toString());
            tr4.setRegionName(deviceMap.get("component_name").toString());
            tr4.setUpRegionId(deviceMap.get("main_device_id").toString());
            tDroneRegionList.add(tr4);*/
        }

        // 无人机区域层级
        log.info("获得的tDroneRegionList是：{}", tDroneRegionList);
        List<TDroneRegion> lst = tDroneRegionList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(o -> o.getRegionId() + "#" + o.getRegionName() + "#" + o.getUpRegionId()))),
                ArrayList::new));
        log.info("去重后的tDroneRegionList是：{}", lst);

        // 该无人机现有的区域层级点
        List<String> nowDroneRegionList = tDroneRegionDao.selectAllByDroneId(droneId);
//        log.info("无人机现有的nowDroneRegionList=="+nowDroneRegionList);

        List<TDroneRegion> addDroneRegionList = new ArrayList<>();
        for (TDroneRegion str : lst) {
            if (nowDroneRegionList.contains(str.getRegionId())) {
                nowDroneRegionList.remove(str.getRegionId());
            } else {
                addDroneRegionList.add(str);
            }
        }
        log.info("最后要插库的regionList是=={}", addDroneRegionList);
        log.info("准备要删除的regionList是=={}", nowDroneRegionList);
        if (CollectionUtils.isNotEmpty(nowDroneRegionList)) {
            int res = tDroneRegionDao.batchDelete(nowDroneRegionList);
            log.info("TRR删除条数=={}", res);
        }
        if (CollectionUtils.isNotEmpty(addDroneRegionList)) {
            // 插库TRR
            int res = tDroneRegionDao.batchInsert(addDroneRegionList);
            log.info("TRR的插入条数是=={}", res);
        }
        if (lst.containsAll(addDroneRegionList)) {
            lst.removeAll(addDroneRegionList);
        }
        log.info("准备更新的list是=={}", lst);
        for (TDroneRegion tDroneRegion : lst) {
            // 更新TRR
            tDroneRegionDao.update(tDroneRegion);
        }
    }

    /**
     * 无人机控制、任务下发及检修区域下发指令返回响应处理
     *
     * @param xmlBaseModel     xml格式的内容
     * @param receiveSessionId 接收会话序列号
     * @return Map<String, Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receivingResponse(XMLBaseModel xmlBaseModel, long receiveSessionId) {
        RobotServerHandler.getRobotResultMap().put("Code", xmlBaseModel.getCode());
        RobotServerHandler.getRobotResultMap().put("receiveSessionId", receiveSessionId);

        if (Constant.sendSessionId == receiveSessionId) {
            log.info("-------------这是刚发命令的响应{}-------------", receiveSessionId);
            if (Objects.isNull(xmlBaseModel.getItems()) || xmlBaseModel.getItems().isEmpty()) {
                RobotServerHandler.getRobotResultMap().put("Item", null);
            } else {
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
                temporaryPath = temporaryPath + "/" + ftpFilePath;
                String developAbsoluteUrl = absoluteImgMap.get("content") + "/" + todayTime + "/CameraLib/";
                String developRelativeUrl = relativeImgMap.get("content") + "/" + todayTime + "/CameraLib/";

                if (ftpFileName.endsWith(".jpg")) {
                    developAbsoluteUrl = developAbsoluteUrl + "BigImg/";
                    developRelativeUrl = developRelativeUrl + "BigImg/";
                } else if (ftpFileName.endsWith(".bmp")) {
                    developAbsoluteUrl = developAbsoluteUrl + "Infrared/";
                    developRelativeUrl = developRelativeUrl + "Infrared/";
                } else if (ftpFileName.endsWith(".mp4")) {
                    developAbsoluteUrl = developAbsoluteUrl + "Video/";
                    developRelativeUrl = developRelativeUrl + "Video/";
                }

                log.info("developAbsoluteUrl是: {} ------developRelativeUrl是: {}", developAbsoluteUrl, developRelativeUrl);

                // 将无人机摄像机抓图结果从ftp服务器上的复制到开发环境
                copyFileToDevelop(temporaryPath, developAbsoluteUrl);
                RobotServerHandler.getRobotResultMap().put("developAbsoluteUrl", developAbsoluteUrl);
                RobotServerHandler.getRobotResultMap().put("developRelativeUrl", developRelativeUrl);
            }
        } else {
            log.info("-------------这不是刚发命令的响应{}-------------", receiveSessionId);
            return RobotServerHandler.getRobotResultMap();
        }
        return RobotServerHandler.getRobotResultMap();
    }

    /**
     * 将ftp服务器上的文件复制到开发环境
     *
     * @param source 源文件
     * @param aim    目标文件
     * @return void
     */
    public static void copyFileToDevelop(String source, String aim) {
        File ff = new File(aim);
        if (!ff.exists()) {
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " " + aim;
            Runtime.getRuntime().exec(url);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * 无人机检修区域下发
     *
     * @param resMap 传来的无人机检修区域相关信息
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
        String deviceIdList = deviceList.substring(1, deviceList.length() - 1);
        itemMap.put("device_list", deviceIdList);
        itemList.add(itemMap);

        List<String> droneCodeList = tDroneInfoDao.selectOnline();
        log.info("在线的droneCodeList: {}", droneCodeList);

        for (String droneCode : droneCodeList) {
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(sendCode)
                    .setReceiveCode(droneCode)
                    .setCode(droneCode)
                    .setType("81")
                    .setCommand("4")
                    .setItems(itemList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的无人机下发检修区域指令xml是<start>{}<end>", xmlString);
            RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
        }

        String code = RobotServerHandler.getRobotResultMap().get("Code").toString();

        if ("200".equals(code)) {
            return "true";
        }
        return "false";
    }

    /**
     * 判断无人机是否正常运作之后再下发任务
     *
     * @param itemMap 传来的无人机任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignDroneTaskIssued(Map<String, List<DroneTaskInstanceInfo>> itemMap) throws Exception {
        List<DroneTaskInstanceInfo> droneTaskInfoList = itemMap.get("droneTaskInfoList");
        log.info("droneTaskInfoList是==={}", droneTaskInfoList);

        for (DroneTaskInstanceInfo item : droneTaskInfoList) {
            boolean droneStatus = checkDroneStatus(item);
            if (droneStatus) {
                Thread thread = new Thread(() -> {
                    try {
                        feignDroneTask(itemMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        }
    }

    /**
     * 判断无人机是否正在运作
     *
     * @param item
     * @return
     * @throws InterruptedException
     */
    private boolean checkDroneStatus(DroneTaskInstanceInfo item) throws InterruptedException {
        String droneOnlineStatus = tDroneInfoDao.selectStatusByDroneCode(item.getDroneCode());
        if (OFF_LINE.equals(droneOnlineStatus)) {
            log.info("==========该无人机处于离线状态,没有成功将任务下发到无人机,巡视结果数据默认==========");
            return false;
        } else {
            Map<String, String> mapForDroneState = redisTemplate.opsForHash().entries("DroneStatus:" + item.getDroneCode() + ":41");
            String droneStatus = mapForDroneState.get("value");
            if ("4".equals(droneStatus)) {
                log.info("==========该无人机处于检修状态,没有成功将任务下发到机器,巡视结果数据默认==========");
                return false;
            } else {
                // 控制权获得
//                String controlRes = droneModeSwitch(item.getDroneCode(), "1", "6", "");
//                if ("200".equals(controlRes)) {
                //操作任务直接下发（前端切换操作模式）
                if (item.getCruiseType() == 456 || item.getCruiseType() == 508 || item.getCruiseType() == 509) {
                    return true;
                } else {
//                        // 任务模式
//                        String taskModelRes = droneModeSwitch(item.getDroneCode(), "1", "5", "1");
//                        if ("200".equals(taskModelRes)) {
                    return true;
//                        }else if ("500".equals(taskModelRes)) {
//                            log.info("==========无人机任务模式切换失败==========");
//                            return false;
//                        }
//                    }
//                }else if ("500".equals(controlRes)) {
//                    log.info("==========无人机控制权获得失败==========");
//                    return false;
                }
            }
        }
//        return false;
    }

    /**
     * 正常任务及联动任务下发
     *
     * @param itemMap 传来的无人机任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignDroneTask(Map<String, List<DroneTaskInstanceInfo>> itemMap) {
        List<DroneTaskInstanceInfo> droneTaskInfoList = itemMap.get("droneTaskInfoList");
        List<Map<String, String>> redisInfoList = new ArrayList<>();
        log.info("这才开始构建关于无人机任务的相关信息......");

        for (DroneTaskInstanceInfo item : droneTaskInfoList) {
            String droneCode = item.getDroneCode();
            String taskId = item.getTaskId();

            StringJoiner str = new StringJoiner(",");
            List<Long> instanceIdList = item.getInstanceList();

            // 将instanceIdList放缓存，以备后续使用
            Map<String, Object> instanceListMap = new HashMap<>(5);
            instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
            instanceListMap.put("taskId", taskId);
            redisTemplate.opsForHash().putAll("DroneTaskStatus:" + droneCode + ":" + taskId, instanceListMap);

            // 根据instanceIdList查询inspectionCodeList
            for (Long instanceId : instanceIdList) {
                String inspectionCode = tDroneInfoDao.selectInspectionCode(instanceId);
                str.add(inspectionCode);

                Map<String, String> redisInfoMap = new HashMap<>(16);
                redisInfoMap.put("droneCode", droneCode);
                redisInfoMap.put("instanceId", instanceId + "");
                redisInfoMap.put("inspectionCode", inspectionCode);
                redisInfoMap.put("cruiseTime", DateTimeUtil.format(new Date()));
                redisInfoMap.put("taskId", taskId);
                redisInfoList.add(redisInfoMap);
            }
            for (int i = 0; i < redisInfoList.size(); i++) {
                redisTemplate.opsForHash().putAll("Drone_SPAndIN_Info:" + redisInfoList.get(i).get("droneCode")
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
                    case 213:
                        planType = 1;
                        break;
                    // 例行
                    case 214:
                        planType = 2;
                        break;
                    // 熄灯
                    case 215:
                        // 专项
                    case 217:
                        // 自定义
                    case 218:
                        planType = 3;
                        break;
                    // 特殊
                    case 216:
                        planType = 4;
                        break;
                    //操作
                    case 456: //操作票
                    case 508: //单设备
                    case 509: //紧急分合闸
                        planType = 5;
                        break;
                    default:
                        break;
                }
                map.put("type", planType);
                map.put("task_code", taskId);
                map.put("plan_code", item.getPlanCode());
                map.put("task_name", item.getTaskName());
                map.put("priority", item.getPriority());
                map.put("device_level", item.getDeviceLevel());
                map.put("device_list", deviceIdList);
                // 周期任务参数
                if ("172".equals(item.getIfRun())) {
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
                else {
                    map.put("fixed_start_time", item.getFixedStartTime());
                    map.put("isorc", item.getIsOrc());
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
                    .setReceiveCode(droneCode)
                    .setCode("变电站编码")
                    .setTime(DateTimeUtil.format(new Date()))
                    .setCommand("1")
                    .setItems(mapList);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的无人机任务的xml是<start>{}<end>", xmlString);
            RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
        }
    }

    /**
     * 给无人机下发任务控制指令
     *
     * @param droneTaskControlMap 传来的无人机任务相关信息
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public Result feignDroneTaskControl(Map<String, Object> droneTaskControlMap) {
        Result result = new Result();
        log.info("droneTaskControlMap==={}", droneTaskControlMap);

        // 1.任务启动 2.任务暂停 3.任务继续 4.任务停止
        String commandValue = droneTaskControlMap.get("commandValue").toString();
        String json = JSONObject.toJSONString(droneTaskControlMap.get("droneCodeList"));
        log.info("判断条件是==={}", (!"null".equals(json)));
        if (!"null".equals(json)) {
            List<String> droneCodeList = JSON.parseArray(json, String.class);
            log.info("droneCodeList==={}", droneCodeList);

            for (String droneCode : droneCodeList) {
                String droneStatus = tDroneInfoDao.selectStatusByDroneCode(droneCode);
                if (Objects.equals(OFF_LINE, droneStatus)) {
                    result.setMessage(209, "该无人机处于离线状态,没有成功将任务控制下发到无人机......");
                    return result;
                } else {
                    String taskId = droneTaskControlMap.get("taskId").toString();
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("DroneTaskStatus:" + droneCode + ":" + taskId);
                    String code = redisInfoMap.get("taskPatrolled_id");

                    XMLBaseModel xmlBaseModel = new XMLBaseModel()
                            .setType("41")
                            .setSendCode(sendCode)
                            .setReceiveCode(droneCode)
                            .setCode(code)
                            .setCommand(commandValue)
                            .setTime(DateTimeUtil.format(new Date()));
                    String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                    log.info("生成的任务控制xml是<start>{}<end>", xmlString);

                    RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);

                    if (Objects.equals("2", commandValue) || Objects.equals("4", commandValue)) {
                        modifyTaskResult(taskId, Integer.valueOf(commandValue));
                    }
                    result.setMessage(200, "该无人机处于离线状态,没有成功将任务控制下发到无人机......");
                }
            }
        }
        return result;
    }

    /**
     * 发送确认消息指令接口
     *
     * @param confirmMessageMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result sendConfirmMsg(Map<String, Object> confirmMessageMap) {
        Result result = new Result();
        log.info("confirmMessageMap===" + confirmMessageMap);
        String droneCode = confirmMessageMap.get("droneCode").toString();
        if (StringUtils.isEmpty(droneCode)) {
            log.error("无人机编码为空,droneCode：{}", droneCode);
            throw new RuntimeException("无人机编码为空");
        }
        String taskId = confirmMessageMap.get("taskId").toString();
        List<Map<String, Object>> cfmList = (List<Map<String, Object>>) confirmMessageMap.get("confirmMsgList");
        Map<String, String> droneConfirmMsg = new HashMap<>();
        droneConfirmMsg.put("newMessage", "false");
        for (Map<String, Object> map : cfmList) {
            boolean flag = false;
            if (map.get("type").equals("1") && map.get("value").equals("")) {
                droneConfirmMsg = redisTemplate.opsForHash().entries("DroneConfirmMsg:" + droneCode + ":" + taskId);
                droneConfirmMsg.put("confirmMapList", droneConfirmMsg.get("splitConfirmMapList"));
                droneConfirmMsg.put("splitConfirmMapList", "");
                droneConfirmMsg.put("newMessage", "true");
                redisTemplate.opsForHash().putAll("DroneConfirmMsg:" + droneCode + ":" + taskId, droneConfirmMsg);
                //webSocket通知前端确认消息
                String jsons = JSON.toJSONString(droneConfirmMsg);
                log.info("确认消息生成-前端推送：" + jsons);
                try {
                    Constant.postUrl(websocketUrl, jsons);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
                flag = true;
            }
            if (flag) {
                return result;
            }
        }

        String droneStatus = tDroneInfoDao.selectStatusByDroneCode(droneCode);
        if (droneStatus.equals("离线")) {
            result.setMessage(209, "该无人机处于离线状态,没有成功将确认消息下发到无人机......");
            return result;
        } else {
            if (cfmList.size() > 0) {
                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("51")
                        .setSendCode(sendCode)
                        .setReceiveCode(droneCode)
                        .setCode(taskId)
                        .setCommand("1")
                        .setTime(DateTimeUtil.format(new Date()))
                        .setItems(cfmList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);//生成xml
                log.info("生成的任务控制xml是<start>" + xmlString + "<end>");
                Map<String, String> droneControlModelMap = redisTemplate.opsForHash().entries("DroneStatus:" + droneCode + ":61");
                String dronePattern = droneControlModelMap.get("value");
                if ("5".equals(dronePattern)) {
                    RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
                } else {
                    result.setMessage(209, "该无人机未处于操作模式！！！");
                }
            }
        }
        redisTemplate.opsForHash().putAll("DroneConfirmMsg:" + droneCode + ":" + taskId, droneConfirmMsg);
        return result;
    }

    /**
     * 更新任务状态及已做巡检点结果
     *
     * @param taskId     任务id
     * @param taskStatus 任务状态
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyTaskResult(String taskId, Integer taskStatus) {
        List<TCruiseDataResult> tcdrList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tctrdList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();

        List<Long> instanceIdDoneList = Constant.flagMap.get(taskId);
        log.info("任务为{}已经做过的巡视点===={}", taskId, instanceIdDoneList);

        if (CollectionUtils.isNotEmpty(instanceIdDoneList)) {
            for (Long instanceId : instanceIdDoneList) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
                log.info("redisInfoMap的数据是{}", redisInfoMap);
                // 缓存中该巡检点有结果
                boolean conditionRes = !StringUtils.equals("设备检修中", redisInfoMap.get("resultNum"))
                        && (StringUtils.equals("246", redisInfoMap.get("cruiseResult"))
                        || StringUtils.equals("247", redisInfoMap.get("cruiseResult")));
                if (Boolean.TRUE.equals(conditionRes)) {
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
        }
        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}", taskId, tctrdList.size(), tcdrList.size());

        int res1 = 0;
        int res2 = 0;
        if (CollectionUtils.isNotEmpty(tctrdList)) {
            res1 = batchInsertCruiseTaskResultDetail(tctrdList);
        }
        if (CollectionUtils.isNotEmpty(tcdrList)) {
            res2 = batchInsertCruiseDataResult(tcdrList);
            log.info("准备传其他服务的cruiseResultIdList==={}", cruiseResultIdList);
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);
        }
        log.info("插tCTRD的条数:{},插tCDR的条数:{}", res1, res2);

        // 将已经做过的巡视点Map清空
        if (StringUtils.isNotEmpty(Constant.flagMap.get(taskId).toString())) {
            log.info("将公共类的instanceIdList清空");
            Constant.flagMap = new HashMap<>(16);
        }

        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries("countForAbnormal:" + taskId);
        Integer totalNum = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString());
        Integer normalNum = Integer.valueOf(abnormalCount.get("normal").toString());
        log.info("taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, totalNum, abnormalNum, normalNum);
        Integer abnormal = abnormalNum;
        Integer normal = normalNum;
        Integer taskWait = totalNum - normal - abnormal;
        TCruiseResult tCruiseResult = selectTaskResultId(taskId);
        tCruiseResult.setTaskWait(taskWait);
        if (taskStatus == 2) {
            tCruiseResult.setCState(241);
        } else if (taskStatus == 4) {
            tCruiseResult.setCState(242);
        }
        tCruiseResult.setTaskCode(taskId);
        log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
        // 更新TCR表
        int res = updateTCruiseResult(tCruiseResult);
        log.info("更新TCR的条数===={}", res);

        for (TCruiseTaskResultDetail tctrd : tctrdList) {
            updateIsWarn(taskId, tctrd.getInstanceId(), tctrd.getCruiseResultId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result otherServer(List<String> cruiseResultIdList) {
        return Constant.otherServerList(cruiseResultIdList, Constant.TASK_FINISH);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectTaskResultId(String taskId) {
        return tDroneInfoDao.selectTaskResultId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectTCruiseTask(String taskId) {
        return tDroneInfoDao.selectTCruiseTask(taskId);
    }

    public Long selectDroneIdByCode(String droneCode) {
        return tDroneInfoDao.selectDroneIdByCode(droneCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectDroneNameByCode(String droneCode) {
        return tDroneInfoDao.selectDroneNameByCode(droneCode);
    }

    /**
     * 无人机本体告警入库
     *
     * @param tDroneAlarm 无人机本体告警信息
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public int insertDroneAlarm(TDroneAlarm tDroneAlarm) {
        return this.tDroneInfoDao.insertDroneAlarm(tDroneAlarm);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseDataResult(List<TCruiseDataResult> tCruiseDataResultList) {
        return this.tDroneInfoDao.batchInsertCruiseDataResult(tCruiseDataResultList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertCruiseTaskResultDetail(List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList) {
        return this.tDroneInfoDao.batchInsertCruiseTaskResultDetail(tCruiseTaskResultDetailList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateTCruiseResult(TCruiseResult tCruiseResult) {
        return this.tDroneInfoDao.updateTCruiseResult(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertTCruiseTaskResult(TCruiseTaskResult tCruiseTaskResult) {
        return this.tDroneInfoDao.insertTCruiseTaskResult(tCruiseTaskResult);
    }

    /**
     * 安全操作无人机返回参数
     *
     * @param key
     * @param userId
     * @param password
     * @param request
     * @return Map<String, Object>
     */
    public Map<String, Object> booleanZcz(String key, Long userId, String password, HttpServletRequest request) throws Exception {
        SysUser sysUserCurrent = sysUserDao.selectByPrimaryId(userId);
        Map map = new HashMap();
        String keys = Constant.map.get(String.valueOf(userId));
        if (StringUtils.isEmpty(key) && keys == null && StringUtils.isEmpty(password)) {
            String random = String.valueOf((int) (Math.random() * 1000000000 + 1));
            Constant.map.put(String.valueOf(userId), random);
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "5");
            params.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
            params.set("title", "控制无人机");
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
            params.set("title", "控制无人机");
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
            params.set("title", "控制无人机");
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
            params.set("title", "控制无人机");
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
            params.set("title", "控制无人机");
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
            params.set("title", "控制无人机");
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
     *
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String upSystemCommand(XMLBaseModel xmlBaseModel) {
        xmlBaseModel
                .setSendCode(sendCode)
                .setReceiveCode(Constant.robotCode);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的无人机控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, Constant.robotCode), Constant.robotCode);
        return "success";
    }

    /**
     * 向上层服务即集控上报信息
     *
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String upToCruise(XMLBaseModel xmlBaseModel) {
        Map<String, List<XMLBaseModel>> droneMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        droneMap.put("list", list);
        droneTaskStates(droneMap);
        return "success";
    }

    public Result droneTaskStates(Map<String, List<XMLBaseModel>> droneMap) {
        Result result = new Result();
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                result = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.SEND_ROBOT_URL, droneMap, Result.class);
            }
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
        }
//        try {
//            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
//            if (null != serviceRestTemplate) {
//                result = serviceRestTemplate.postForObject(Constant.SEND_DRONE_URL, droneMap, Result.class);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        return result;
    }

    /**
     * 将无人机传来的信息对应到巡视主机
     *
     * @param valueName 类型名称
     * @param value     值
     * @param colName   巡视主机对应类型
     * @return Integer
     */
    public Integer selectDictCode(String valueName, String value, String colName) {
        String dictNote = null;
        if (Objects.equals("appearanceType", valueName)) {
            switch (value) {
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
        } else if (Objects.equals("meterType", valueName)) {
            switch (value) {
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
                case "6":
                    dictNote = "开关动作次数表";
                    break;
                case "7":
                    dictNote = "油温表";
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
        } else if (Objects.equals("deviceType", valueName)) {
            switch (value) {
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
        } else if (valueName.equals("mainOperationType")) {
            switch (value) {
                case "1":
                    dictNote = "旋钮";
                    break;
                case "2":
                    dictNote = "按钮";
                    break;
                case "3":
                    dictNote = "手车";
                    break;
                case "4":
                    dictNote = "地刀";
                    break;
                case "5":
                    dictNote = "紧急分合闸";
                    break;
                case "6":
                    dictNote = "手车验电";
                    break;
                case "7":
                    dictNote = "机械位判断";
                    break;
                case "8":
                    dictNote = "压板";
                    break;
                case "9":
                    dictNote = "其他";
                    break;
                default:
                    break;
            }
        } else if (valueName.equals("operationType")) {
            switch (value) {
                case "1":
                    dictNote = "转换开关";
                    break;
                case "2":
                    dictNote = "控制开关";
                    break;
                case "3":
                    dictNote = "储能开关";
                    break;
                case "4":
                    dictNote = "手车旋钮";
                    break;
                case "5":
                    dictNote = "转换控制开关";
                    break;
                case "6":
                    dictNote = "电压选择旋钮";
                    break;
                case "7":
                    dictNote = "手车操作开关";
                    break;
                case "8":
                    dictNote = "接地刀操作开关";
                    break;
                case "9":
                    dictNote = "闭锁开关";
                    break;
                case "10":
                    dictNote = "风机旋钮";
                    break;
                case "11":
                    dictNote = "加热器旋钮";
                    break;
                case "12":
                    dictNote = "复归按钮";
                    break;
                case "13":
                    dictNote = "带电显示器";
                    break;
                case "14":
                    dictNote = "线路保护装置";
                    break;
                case "15":
                    dictNote = "手车";
                    break;
                case "16":
                    dictNote = "地刀";
                    break;
                case "17":
                    dictNote = "许继_紧急分合闸";
                    break;
                case "18":
                    dictNote = "西门子_紧急分合闸";
                    break;
                case "19":
                    dictNote = "华电_紧急分合闸";
                    break;
                case "20":
                    dictNote = "天灵_紧急分合闸";
                    break;
                case "21":
                    dictNote = "北辰_紧急分合闸";
                    break;
                case "22":
                    dictNote = "江苏_紧急分合闸";
                    break;
                case "23":
                    dictNote = "手车验电装置";
                    break;
                case "24":
                    dictNote = "地刀机械位判断拍照";
                    break;
                case "25":
                    dictNote = "断路器开关";
                    break;
                case "26":
                    dictNote = "压板";
                    break;
                case "27":
                    dictNote = "OCR识别";
                    break;
                case "28":
                    dictNote = "五防逻辑识别";
                    break;
                case "29":
                    dictNote = "电压转换开关";
                    break;
                case "30":
                    dictNote = "解锁/联锁控制开关";
                    break;
                case "31":
                    dictNote = "并列/解锁控制开关";
                    break;
                default:
                    break;
            }
        } else if (valueName.equals("pointAlarmType")) {
            switch (value) {
                case "1":
                    dictNote = "超温报警";
                    break;
                case "2":
                    dictNote = "温升报警";
                    break;
                case "3":
                    dictNote = "三相温差报警";
                    break;
                case "4":
                    dictNote = "三相对比报警";
                    break;
                case "5":
                    dictNote = "声音异常";
                    break;
                case "6":
                    dictNote = "外观异常";
                    break;
                case "7":
                    dictNote = "仪表越限报警";
                    break;
                case "8":
                    dictNote = "仪表超量程报警";
                    break;
                case "9":
                    dictNote = "仪表三相对比";
                    break;
                case "10":
                    dictNote = "变位报警";
                    break;
                case "11":
                    dictNote = "操作告警";
                    break;
                default:
                    break;
            }
        }
        return Integer.valueOf(selectDictCodeByNote(dictNote, colName));
    }

    /**
     * 若修改无人机编码或删除无人机断开在线的无人机连接
     *
     * @param droneCode 无人机唯一标识
     * @param droneId   无人机id
     * @return String
     */
    public String removeLink(String droneCode, Long droneId) {
        RobotServerHandler.removeLink(droneCode);
        /*TDroneInfo tDroneInfo = new TDroneInfo()
                .setDroneId(droneId)
                .setDroneStatus(OFF_LINE);
        int res = tDroneInfoDao.update(tDroneInfo);
        log.info("修改成功==="+res);*/
        return OFF_LINE;
    }

    public List<TCruiseTaskResultDetail> selectRepairTCTRDList(String taskId) {
        return this.tDroneInfoDao.selectRepairTCTRDList(taskId);
    }

    public List<TCruiseDataResult> selectRepairTCDRList(String taskId) {
        return this.tDroneInfoDao.selectRepairTCDRList(taskId);
    }

    public TCruisePointInstanceDetail selectForTask(Long instanceId) {
        return this.tDroneInfoDao.selectForTask(instanceId);
    }

    /**
     * 根据测点查询关联算法信息
     *
     * @param deviceMeteId 测点信息
     * @return List<TAlgorithmInfo>
     */
    public List<TAlgorithmInfo> selectByDeviceMeteId(Long deviceMeteId) {
        return this.tDroneInfoDao.selectByDeviceMeteId(deviceMeteId);
    }

    /**
     * 根据测点id查询测点信息
     *
     * @param deviceMeteId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMete(Long deviceMeteId) {
        return this.tDroneInfoDao.selectDeviceMete(deviceMeteId);
    }

    /**
     * 无人机站端任务信息入库
     *
     * @param taskModelMapList 无人机本体任务相关数据
     * @param xmlBaseModel     xml格式的内容
     * @return int
     */
    public void addDroneSelfTask(List<Map<String, Object>> taskModelMapList, XMLBaseModel xmlBaseModel) {
        try {
            Long droneId = tDroneInfoDao.selectDroneIdByCode(xmlBaseModel.getSendCode());
            Long upRegionId = Long.parseLong(String.valueOf(tDroneInfoDao.selectByPrimaryId(droneId).getUpRegionId()));
            List<Map<String, Object>> cruiseSelfTask = taskModelMapList.stream().filter(task -> !"5".equals(task.get("type").toString())).collect(Collectors.toList());
            //1.处理巡检任务模型
            if (CollectionUtils.isNotEmpty(cruiseSelfTask)) {
                List<TCruiseTask> tCruiseTaskList = new ArrayList<>();
                for (Map<String, Object> cruiseTaskModelMap : cruiseSelfTask) {
                    // 构建t_cruise_task
                    String taskId = cruiseTaskModelMap.get("task_code").toString();
                    String taskName = cruiseTaskModelMap.get("task_name").toString();
                    TCruiseTask tCruiseTask = new TCruiseTask();
                    tCruiseTask.setTaskId(taskId);
                    tCruiseTask.setTaskCode(taskId);
                    tCruiseTask.setTaskName(taskName);
                    //任务类型
                    String type = cruiseTaskModelMap.get("type").toString();
                    Integer cType = null;
                    switch (type) {
                        // 例行
                        case "1":
                            cType = 214;
                            break;
                        // 特殊
                        case "2":
                            cType = 216;
                            break;
                        // 专项
                        case "3":
                            cType = 217;
                            break;
                        // 自定义
                        case "4":
                            cType = 218;
                            break;
                        default:
                            break;
                    }
                    tCruiseTask.setType(cType);
                    if (!"".equals(cruiseTaskModelMap.get("cycle_execute_time")) || !"".equals(cruiseTaskModelMap.get("interval_start_time"))) {
                        tCruiseTask.setIfRun(172);
                    } else {
                        tCruiseTask.setIfRun(174);
                    }
                    tCruiseTask.setStartTime(DateTimeUtil.parse(cruiseTaskModelMap.get("fixed_start_time").toString()));
                    tCruiseTask.setRobotId(droneId);
                    // 无人机本体任务
                    tCruiseTask.setTaskType(271);
                    if (!"".equals(cruiseTaskModelMap.get("priority").toString())) {
                        tCruiseTask.setTaskLevel(Integer.valueOf(cruiseTaskModelMap.get("priority").toString()));
                    }
                    tCruiseTask.setCreateTime(new Date());
                    tCruiseTaskList.add(tCruiseTask);
                    log.info("tCruiseTaskList==={}", tCruiseTaskList);

                    // 构建t_cruise_point_instance
                    List<TCruisePointInstance> tCruisePointInstanceList = buildTCruisePointInstance(cruiseTaskModelMap, droneId);

                    // 构建t_cruise_task_attr
                    List<Map<String, String>> redisInfoList = new ArrayList<>();
                    List<Long> instanceIdList = new ArrayList<>();
                    List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
                    for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList) {
                        TCruiseTaskAttr tCruiseTaskAttr = new TCruiseTaskAttr()
                                .setTaskId(taskId)
                                .setInstanceId(tCruisePointInstance.getInstanceId());
                        tCruiseTaskAttrList.add(tCruiseTaskAttr);
                        instanceIdList.add(tCruisePointInstance.getInstanceId());

                        Map<String, String> redisInfoMap = new HashMap<>();
                        redisInfoMap.put("droneCode", xmlBaseModel.getSendCode());
                        redisInfoMap.put("instanceId", tCruisePointInstance.getInstanceId() + "");
                        Map<String, Object> map = tDroneRegionDao.selectInspectionId(tCruisePointInstance.getCruiseId());
                        redisInfoMap.put("inspectionCode", map.get("inspection_code").toString());
                        redisInfoMap.put("deviceName", map.get("region_name").toString());
                        redisInfoMap.put("taskId", taskId);
                        redisInfoMap.put("inspectionName", tCruisePointInstance.getCruiseName());
                        redisInfoList.add(redisInfoMap);
                    }
                    log.info("redisInfoList是==={}", redisInfoList);
                    // 放数据到缓存
                    for (Map<String, String> stringStringMap : redisInfoList) {
                        redisTemplate.opsForHash().putAll("Drone_SPAndIN_Info:" + stringStringMap.get("droneCode")
                                + ":" + stringStringMap.get("taskId") + ":" + stringStringMap.get("instanceId"), stringStringMap);
                    }

                    log.info("tCruiseTaskAttrList==={}", tCruiseTaskAttrList);
                    tDroneInfoDao.batchInsertTaskAttr(tCruiseTaskAttrList);

                    // 将instanceIdList放缓存，以备后续使用
                    log.info("instanceIdList是==={}", instanceIdList);
                    Map<String, Object> instanceListMap = new HashMap<>(5);
                    instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
                    instanceListMap.put("taskId", taskId);
                    redisTemplate.opsForHash().putAll("DroneTaskStatus:" + xmlBaseModel.getSendCode() + ":" + taskId, instanceListMap);

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
                    tDroneInfoDao.insertTCruiseResult(tCruiseResult);
                }
                if (CollectionUtils.isNotEmpty(tCruiseTaskList)) {
                    tDroneInfoDao.batchInsertTask(tCruiseTaskList);
                }
            }
            //1.处理操作任务模型(操作票)
            List<Map<String, Object>> operationSelfTask = taskModelMapList.stream().filter(task -> "5".equals(task.get("type").toString())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(operationSelfTask)) {
                Map<String, Object> params = new HashMap<>();
                params.put("droneId", droneId);
                List<String> orderOperationSelfTask = (List<String>) Constant.restTemplateGet(Constant.GET_PLAN_LIST, params).getData();
                //1.添加或更新操作票
                for (Map<String, Object> operationTaskModelMap : operationSelfTask) {
                    // 构建t_cruise_task
                    String taskId = operationTaskModelMap.get("task_code").toString();
                    String taskName = operationTaskModelMap.get("task_name").toString();
                    //任务类型5 ： 操作类任务模型（操作票）
                    Map<String, Object> map = new HashMap<>();
                    // 构建t_cruise_point_instance
                    List<TCruisePointInstance> tCruisePointInstanceList = buildTCruisePointInstance(operationTaskModelMap, droneId);
                    List<Long> instanceList = new ArrayList<>();
                    tCruisePointInstanceList.forEach(tCruisePointInstance -> instanceList.add(tCruisePointInstance.getInstanceId()));
                    if (instanceList.size() > 0) {
                        String simulationSteps = operationTaskModelMap.get("simulation_steps").toString();//操作票初始状态
                        map.put("type", 456);
                        map.put("instanceList", instanceList);
                        map.put("planName", taskName); //操作票名称
                        map.put("planCode", taskId); //操作票编码
                        map.put("droneId", droneId); //无人机id
                        map.put("upRegionId", upRegionId); //无人机所在区域
                        map.put("simulationSteps", simulationSteps);
                        Result result = Constant.restTemplatePost(Constant.PLAN_URL, map);
                        log.info("操作票入库：{}", result.getCode());
                    }
                    orderOperationSelfTask.remove(taskId);
                }
                //2.删除站端已删除的操作票
                orderOperationSelfTask.forEach(s -> {
                    Map<String, Object> order = new HashMap<>();
                    order.put("planCode", s);
                    Constant.restTemplateDelete(Constant.DELETE_PLAN_LIST, order);
                });
            }
        } catch (Exception e) {
            log.error("addDroneSelfTask error", e);
        }
    }

    /**
     * 构建t_cruise_point_instance
     */
    private List<TCruisePointInstance> buildTCruisePointInstance(Map<String, Object> taskModelMap, Long droneId) {
        //操作无人机拓展协议 添加设备属性列表--处理操作测点行为
        Map<String, String> inspectionIdAttrMap = new HashMap<>();
        if (Objects.nonNull(taskModelMap.get("device_attr_list"))) {
            String inspectionIdAttrString = taskModelMap.get("device_attr_list").toString();
            String[] inspectionIdAttrs = inspectionIdAttrString.split(",");
            List<String> inspectionIdAttrList = new ArrayList<>(Arrays.asList(inspectionIdAttrs));
            for (String inspectionIdAttr : inspectionIdAttrList) {
                String inspectionCode = StringUtils.substringBeforeLast(inspectionIdAttr, "_");
                String attr = StringUtils.substringAfterLast(inspectionIdAttr, "_");
                inspectionIdAttrMap.put(inspectionCode, attr);
            }
        }
        // 该任务下包含的点位
        String inspectionIdString = taskModelMap.get("device_list").toString();
        String[] inspectionIds = inspectionIdString.split(",");
        List<String> inspectionIdList = new ArrayList<>();
        Collections.addAll(inspectionIdList, inspectionIds);
        log.info("无人机对应点list==={}", inspectionIdList);
        List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>();
        for (String inspectionCode : inspectionIdList) {
            TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
            TStdRegion tStdRegion = tDroneInfoDao.selectTSRegionForStation(droneId);
            tCruisePointInstance.setStationId(tStdRegion.getRegionId().toString());
            tCruisePointInstance.setStationName(tStdRegion.getRegionName());
            tCruisePointInstance.setCruiseType(228);
            Map<String, Object> map = tDroneInspectionDao.selectInspection(inspectionCode);
            tCruisePointInstance.setCruiseId(Long.valueOf(map.get("inspection_id").toString()));
            tCruisePointInstance.setCruiseName(map.get("inspection_name").toString());
            tCruisePointInstance.setIfSy(1);
            tCruisePointInstance.setTextDesc(inspectionIdAttrMap.get(inspectionCode));
            tCruisePointInstanceList.add(tCruisePointInstance);
        }
        log.info("tCruisePointInstanceList==={}", tCruisePointInstanceList);
        tDroneInfoDao.batchInsertInstance(tCruisePointInstanceList);
        return tCruisePointInstanceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOperationStepsForRedis(XMLBaseModel xmlBaseModel) {
        Map<String, Object> operationStepsMaps = new HashMap<>(11);
        String droneCode = xmlBaseModel.getSendCode();
        try {
            String taskId = xmlBaseModel.getItems().get(0).get("task_code").toString();
            operationStepsMaps.put("droneCode", droneCode);
            operationStepsMaps.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
            operationStepsMaps.put("taskId", taskId);
            operationStepsMaps.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
            //当前操作测点id
            String deviceId = xmlBaseModel.getItems().get(0).get("device_id").toString();
            operationStepsMaps.put("deviceId", deviceId);
            //当前操作点对应相机路径
            List<String> cameraUrlList = tDroneInfoDao.selectCameraUrlByDeviceId(deviceId);
            operationStepsMaps.put("cameraUrlList", String.valueOf(cameraUrlList));
            //当前操作名称
            operationStepsMaps.put("currentOperation", xmlBaseModel.getItems().get(0).get("current_operation").toString());
            //上一步操作名称
            operationStepsMaps.put("lastOperation", xmlBaseModel.getItems().get(0).get("last_operation").toString());
            //下一步操作名称
            operationStepsMaps.put("nextOperation", xmlBaseModel.getItems().get(0).get("next_operation").toString());
            //操作任务步骤名称
            Map<String, String> map = redisTemplate.opsForHash().entries("DroneOperationSteps:" + droneCode + ":" + taskId);
            List<String> nowStepNameList = new ArrayList<>();
            if (Objects.nonNull(map.get("stepName"))) {
                String stepNameStringList = map.get("stepName");
                stepNameStringList = stepNameStringList.replaceAll("\\[", "").replaceAll("]", "");
                String[] stepNameArray = stepNameStringList.split(", ");
                nowStepNameList = new ArrayList<>(Arrays.asList(stepNameArray));
            }
            nowStepNameList.add(xmlBaseModel.getItems().get(0).get("step_name").toString());
            operationStepsMaps.put("stepName", String.valueOf(nowStepNameList));
            //无人机步骤状态
            operationStepsMaps.put("droneStepStatus", xmlBaseModel.getItems().get(0).get("drone_step_status").toString());
            //操作进度
            operationStepsMaps.put("process", xmlBaseModel.getItems().get(0).get("process").toString());
            //无人机确认消息入redis
            redisTemplate.opsForHash().putAll("DroneOperationSteps:" + droneCode + ":" + taskId, operationStepsMaps);
        } catch (Exception e) {
            log.error("无人机步骤消息错误！", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConfirmMsgForRedis(XMLBaseModel xmlBaseModel) throws Exception {
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, Object> jasonMaps = new HashMap<>();
        String droneCode = xmlBaseModel.getSendCode();
        String taskId = xmlBaseModel.getItems().get(0).get("task_code").toString();
        jasonMaps.put("type", "confirmMsg");
        jasonMaps.put("droneCode", droneCode);
        jasonMaps.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
        jasonMaps.put("taskId", taskId);
        //当前操作测点id
        String deviceId = xmlBaseModel.getItems().get(0).get("device_id").toString();
        jasonMaps.put("deviceId", deviceId);
        //点位类型名称
        jasonMaps.put("deviceTypeName", tDroneInfoDao.selectDeviceTypeName(deviceId));
        jasonMaps.put("content", xmlBaseModel.getItems().get(0).get("content"));
        String confirmPath = filePathMap.get("content") + "/" + xmlBaseModel.getItems().get(0).get("file_path").toString();
        String originalPath = "";
        if (xmlBaseModel.getItems().get(0).containsKey("original_file_path")) {
            originalPath = filePathMap.get("content") + "/" + xmlBaseModel.getItems().get(0).get("original_file_path").toString();
        }
        log.info("确认图片路径为：" + confirmPath);
        log.info("确认原始图片路径为：" + originalPath);
        String developMap = absoluteImgMap.get("content") + "/CFM";
        /*
         * 将ftp图copy到开发环境
         * */
        String splitArray[] = confirmPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        copyFileToDevelop(confirmPath, developMap);
        String confirmUrl = relativeImgMap.get("content") + "/CFM/" + fileName;
        String originalFileUrl = "";
        if (org.apache.commons.lang.StringUtils.isNotEmpty(originalPath)) {
            String splitArrayOriginalPath[] = originalPath.split("/");
            String originalPathName = splitArrayOriginalPath[splitArrayOriginalPath.length - 1];
            copyFileToDevelop(originalPath, developMap);
            originalFileUrl = relativeImgMap.get("content") + "/CFM/" + originalPathName;
        }
        //待确认图片
        jasonMaps.put("confirmUrl", confirmUrl);
        //原始图片
        jasonMaps.put("originalFileUrl", originalFileUrl);
        List<Map<String, Object>> confirmMapList = new ArrayList<>();
        confirmMapList = xmlBaseModel.getItems().stream().filter(res -> !res.get("type").equals("0")).collect(Collectors.toList());

        //操作选项拆分
        List<Map<String, Object>> splitConfirmMapList = new ArrayList<>();
        confirmMapList.forEach(map -> {
            if (map.get("type").equals("7")) {
                splitConfirmMapList.add(map);
            }
        });
        if (splitConfirmMapList.size() > 0) {
            confirmMapList.forEach(map -> {
                if (map.get("type").equals("2")) {
                    splitConfirmMapList.add(map);
                }
            });
            confirmMapList = confirmMapList.stream().filter(res -> !res.get("type").equals("7")).collect(Collectors.toList());
            confirmMapList = confirmMapList.stream().filter(res -> !res.get("type").equals("2")).collect(Collectors.toList());
        }
        jasonMaps.put("splitConfirmMapList", JSONArray.toJSONString(splitConfirmMapList));
        //转json字符串
        jasonMaps.put("confirmMapList", JSONArray.toJSONString(confirmMapList));
        //判定新的确认消息
        jasonMaps.put("newMessage", "true");
        //无人机确认消息入redis
        redisTemplate.opsForHash().putAll("DroneConfirmMsg:" + droneCode + ":" + taskId, jasonMaps);
        //发给前端
        jasonMaps.put("confirmMapList", confirmMapList);
        //webSocket通知前端确认消息
        String jsons = JSON.toJSONString(jasonMaps);
        log.info("确认消息生成-前端推送：" + jsons);
        Constant.postUrl(websocketUrl, jsons);
    }

    /**
     * 无人机巡检点告警及分析无人机巡检点结果后的告警
     *
     * @param warnInfo 告警信息
     * @return int
     */
    public int insertWarn(TWarnInfo warnInfo) {
        return tDroneInfoDao.insertWarn(warnInfo);
    }

    /**
     * 根据任务id和巡检点id查询告警id
     *
     * @param taskId     任务id
     * @param instanceId 巡检点id
     * @return Long
     */
    public Long selectWarnId(String taskId, Long instanceId) {
        return tDroneInfoDao.selectWarnId(taskId, instanceId);
    }

    /**
     * 判断是否生成告警，若是则更新任务结果表
     *
     * @param taskId         任务id
     * @param instanceId     巡检点id
     * @param cruiseResultId 任务结果巡检点id
     * @return int
     */
    public int updateIsWarn(String taskId, Long instanceId, String cruiseResultId) {
        int isWarnFlag = tDroneInfoDao.selectIsWarn(instanceId, taskId);
        if (isWarnFlag > 0) {
            tDroneInfoDao.updateIsWarn(cruiseResultId);
        }
        return isWarnFlag;
    }

    /**
     * 根据巡视点id查询该测点信息
     *
     * @param instanceId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId) {
        return tDroneInfoDao.selectDeviceMeteInfo(instanceId);
    }

    /**
     * 下发获得无人机控制权及任务模式切换的指令
     *
     * @param droneCode 无人机唯一标识
     * @param type      类型
     * @param command   命令
     * @param value     值
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String droneModeSwitch(String droneCode, String type, String command, String value) throws InterruptedException {
        List<Map<String, Object>> item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>(5);
        if (!"".equals(value) || null != value) {
            map.put("value", value);
        }
        item.add(map);

        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(sendCode)
                .setReceiveCode(droneCode)
                .setCode(droneCode)
                .setTime(DateTimeUtil.format(new Date()))
                .setType(type)
                .setCommand(command)
                .setItems(item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的无人机控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
        TimeUnit.MILLISECONDS.sleep(2000);

        return RobotServerHandler.getRobotResultMap().get("Code").toString();
    }

    /**
     * 判断任务是否属于无人机本体任务
     *
     * @param taskId 任务id
     * @return Long
     */
    public Long selectIsDroneTask(String taskId) {
        return tDroneInfoDao.selectIsDroneTask(taskId);
    }

    /**
     * 字典值查询
     *
     * @param dictNote 说明
     * @param colName  类型
     * @return String
     */
    public String selectDictCodeByNote(String dictNote, String colName) {
        return tDroneInfoDao.selectDictCodeByNote(dictNote, colName);
    }

    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     *
     * @param taskId 任务id
     * @return Long
     */
    public List<Long> selectInstanceForTaskGoOn(String taskId) {
        return tDroneInfoDao.selectInstanceForTaskGoOn(taskId);
    }

    /**
     * 根据type和command获取日志记录内容
     *
     * @param type    类型
     * @param command 命令
     * @param value   值
     * @return String
     */
    public String selectContentByCommand(String type, String command, String value) {
        if ("1".equals(type)) {
            switch (command) {
                case "1":
                    return "无人机远方复位";
                case "2":
                    return "无人机系统自检";
                case "3":
                    return "无人机一键返航";
                case "4":
                    return "无人机手动充电";
                case "5":
                    if ("1".equals(value)) {
                        return "无人机控制模式切换为任务模式";
                    } else if ("2".equals(value)) {
                        return "无人机控制模式切换为紧急定位模式";
                    } else if ("3".equals(value)) {
                        return "无人机控制模式切换为后台遥控模式";
                    } else if ("4".equals(value)) {
                        return "无人机控制模式切换为手持遥控模式";
                    }
                    break;
                case "6":
                    return "无人机控制权获得";
                case "7":
                    return "无人机控制权释放";
                default:
                    break;
            }
        } else if ("2".equals(type)) {
            switch (command) {
                case "1":
                    return "无人机前进";
                case "2":
                    return "无人机后退";
                case "3":
                    return "无人机左转";
                case "4":
                    return "无人机右转";
                case "5":
                    if ("1".equals(value)) {
                        return "无人机左转弯";
                    } else if ("2".equals(value)) {
                        return "无人机右转弯";
                    }
                    break;
                case "6":
                    return "无人机停止";
                default:
                    break;
            }
        } else if ("3".equals(type)) {
            switch (command) {
                case "1":
                    return "无人机云台上仰";
                case "2":
                    return "无人机云台下俯";
                case "3":
                    return "无人机云台左转";
                case "4":
                    return "无人机云台右转";
                case "5":
                    return "无人机云台上升";
                case "6":
                    return "无人机云台下降";
                case "7":
                    return "无人机云台预置位调用";
                case "8":
                    return "无人机云台停止";
                case "9":
                    return "无人机云台复位";
                case "10":
                    return "无人机云台预置位新增";
                case "11":
                    return "无人机云台预置位修改";
                case "12":
                    return "无人机云台预置位删除";
                default:
                    break;
            }
        } else if ("4".equals(type)) {
            switch (command) {
                case "1":
                    if ("1".equals(value)) {
                        return "打开无人机红外电源";
                    } else if ("2".equals(value)) {
                        return "关闭无人机红外电源";
                    }
                    break;
                case "2":
                    if ("1".equals(value)) {
                        return "打开无人机雨刷";
                    } else if ("2".equals(value)) {
                        return "关闭无人机雨刷";
                    }
                    break;
                case "3":
                    if ("1".equals(value)) {
                        return "打开无人机超声";
                    } else if ("2".equals(value)) {
                        return "关闭无人机超声";
                    }
                    break;
                case "4":
                    if ("1".equals(value)) {
                        return "打开无人机红外射灯";
                    } else if ("2".equals(value)) {
                        return "关闭无人机红外射灯";
                    }
                    break;
                default:
                    break;
            }
        } else if ("21".equals(type)) {
            switch (command) {
                case "1":
                    return "无人机可见光摄像机镜头拉近";
                case "2":
                    return "无人机可见光摄像机镜头拉远";
                case "3":
                    return "无人机可见光摄像机镜头拉焦停止";
                case "4":
                    return "无人机可见光摄像机焦距增加";
                case "5":
                    return "无人机可见光摄像机焦距减少";
                case "6":
                    return "无人机可见光摄像机自动聚焦";
                case "7":
                    return "无人机可见光摄像机抓图";
                case "8":
                    return "无人机可见光摄像机重启";
                case "9":
                    return "无人机可见光摄像机启动录像";
                case "10":
                    return "无人机可见光摄像机停止录像";
                case "11":
                    return "无人机可见光摄像机倍率值设置";
                case "12":
                    return "无人机可见光摄像机聚焦值设置";
                default:
                    break;
            }
        } else if ("22".equals(type)) {
            switch (command) {
                case "5":
                    return "无人机红外热像仪设定焦距值";
                case "6":
                    return "无人机红外热像仪自动聚焦";
                case "7":
                    return "无人机红外热像仪抓图";
                case "8":
                    return "无人机红外热像仪重启";
                default:
                    break;
            }
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String localPath, String targetName) {
        try {
            if ("".equals(localPath)) {
                return;
            }
            FtpsUtil.putFile(ftpsLocalPath + "/" + localPath, targetName,
                    serverUrl, Integer.valueOf(ftpsPort), key, ftpsUserName, ftpsPassWord);
        } catch (Exception e) {
            log.error("上传至ftps错误 " + e);
        }
    }

    /**
     * 离线情况同步无人机模型信息-上传文件
     *
     * @param file      文件
     * @param droneCode 无人机唯一标识
     * @return Result
     */
    public Result upLoadDroneModel(MultipartFile file, String droneCode) {
        Result result = new Result();
        try {
            if (file == null) {
                result.setCode(209, "文件错误，文件为null");
                result.setData(0);
                return result;
            }
            String fileName = "copy-droneModel.xml";
            String pathName = excelDataImport(file, fileName);
            // 解析xml文件
            XMLBaseModel droneModel = getXmlMessage(pathName);
            List<Map<String, Object>> droneMap = droneModel.getItems();
            Long droneId = selectDroneIdByCode(droneCode);
            // 无人机模型信息入库
            addDroneModel(droneMap, droneId);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return result;
    }

    /**
     * 离线情况同步无人机设备文件-上传文件
     *
     * @param file      文件
     * @param droneCode 无人机唯一标识
     * @return Result
     */
    public Result upLoadDroneDevice(MultipartFile file, String droneCode) {
        Result result = new Result();
        try {
            if (file == null) {
                result.setCode(209, "文件错误，文件为null");
                result.setData(0);
                return result;
            }
            String fileName = "copy-droneDevice.xml";
            String pathName = excelDataImport(file, fileName);
            // 解析xml文件
            XMLBaseModel droneDevice = getXmlMessage(pathName);
            List<Map<String, Object>> deviceMap = droneDevice.getItems();
            Long droneId = selectDroneIdByCode(droneCode);
            // 无人机设备点位入库
            addDevicePoint(deviceMap, droneId);
            addDevicePointRegion(deviceMap, droneId);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return result;
    }

    /**
     * 将上传文件写入临时文件
     *
     * @param file     文件
     * @param fileName 文件名
     * @return String
     */
    private String excelDataImport(MultipartFile file, String fileName) {
        Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String path = (String) mapForPicModelPath.get("content");
        // 将上传文件写入
        try {
            deleteDir(new File(path + File.separator + fileName));
            file.transferTo(new File(path + File.separator + fileName));
        } catch (NullPointerException | IOException e) {
            e.getMessage();
        }
        return path + "/" + fileName;
    }

    /**
     * 判断站端的任务是否在巡视主机上已完成
     *
     * @param droneCode 无人机唯一标识
     * @return String
     */
    public List<String> hasStandTaskIsFinish(String droneCode) {
        Long droneId = selectDroneIdByCode(droneCode);
        List<String> taskIdList = tDroneInfoDao.HasStandTaskIsFinish(droneId);
        log.info("站端在巡视主机显示未完成的任务=={}", taskIdList);
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            for (String taskId : taskIdList) {
                Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("DroneTaskStatus:" + droneCode + ":" + taskId);
                Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());
                try {
                    if (1 == taskState) {
                        tDroneInfoDao.updateStandTaskStatus(taskId, 240);
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }
        return taskIdList;
    }

    public int deleteInstanceId(List<Long> instanceIdList) {
        for (Long instanceId : instanceIdList) {
            tDroneInfoDao.deleteInstanceId(instanceId);
        }
        return 1;
    }

    /**
     * 查询无人机状态
     */
    @Transactional(rollbackFor = Exception.class)
    public String queryDroneStatus(String droneCode) {
        log.info("查询无人机状态:droneCode{}====", droneCode);
        Map<Object, Object> mapForDroneState = redisTemplate.opsForHash().entries("DroneStatus:" + droneCode + ":2");
        if (null != mapForDroneState && mapForDroneState.size() > 0) {
            return mapForDroneState.getOrDefault("value", "1").toString();
        }
        return OFF_LINE;
    }

    /**
     * 智能环境设备控制指令
     */
    public Result droneControl(HashMap<String, String> map) {
        log.info("下发智能环境设备控制指令,map===" + map);

        Result result = new Result();
        try {
            String droneCode = map.get("droneCode");
            log.info("sendCode:{},droneCode:{}====", sendCode, droneCode);
            if (StringUtils.isEmpty(droneCode)) {
                log.error("当前不存在无人机编码,没有成功将控制指令下发到无人机....");
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            }
            Map<Object, Object> mapForDroneState = redisTemplate.opsForHash().entries("DroneStatus:" + droneCode + ":2");
            Object droneStatus = mapForDroneState.get("value");
            log.info("droneStatus====" + droneStatus);
            if (Optional.ofNullable(droneStatus).isPresent()) {
                if ("1".equals(droneStatus)) {
                    log.error("该无人机处于离线状态,没有成功将控制指令下发到无人机......");
                    result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                } else {
                    log.info("开始生成xml");
                    String type = map.get("type");
                    String cmd = map.get("deviceStatus");
                    String value = map.get("deviceAttr");
                    String deviceType = map.get("deviceType");
                    String deviceId = map.get("deviceId");
                    List<Map<String, Object>> Item = new LinkedList<>();
                    Map<String, Object> maps = new HashMap<>();
                    if (StringUtils.equals("7", deviceType)) {  //7.空调
                        maps.put("value", value);
                    }
                    Item.add(maps);
                    XMLBaseModel xmlBaseModel = new XMLBaseModel()
                            .setSendCode(sendCode)
                            .setReceiveCode(droneCode)
                            .setType(type)
                            .setCode(deviceId)
                            .setTime(DateTimeUtil.getDateTimeString(new Date(), false))
                            .setCommand(cmd)
                            .setItems(Item);
                    String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                    log.info("生成的无人机控制xml是<start>" + xmlString + "<end>");
                    RobotServerHandler.send(generateByteOrder(xmlString, droneCode), droneCode);
                    result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    /**
     * 环控数据存储
     *
     * @date 2022/2/18
     */
    public void addWeatherInfo(JSONObject json) {
        log.info("接收微气象数据");
        try {
            if (Optional.ofNullable(json).isPresent() && json.getJSONArray("envDeviceStatusList").size() > 0) {
                String droneCode = json.getString("droneCode");
                //查询区域Id
                String regionId = tDroneInfoDao.selectRegionIdBydroneId(droneCode);
                JSONArray envDeviceStatusList = json.getJSONArray("envDeviceStatusList");
                redisTemplate.opsForHash().put("Weather", regionId, envDeviceStatusList);
            }
        } catch (Exception e) {
            log.error("获取天气信息错误:", e);
        }
    }

    /**
     * 环控告警数据入库
     */
    public void addEnvWarning(Map<String, String> envWarn) {
        log.info("接收环境设备告警开始");
        try {
            if (envWarn.size() > 0) {
                String type = null;
                envWarn.put("envWarnId", getUUID());
                switch (Integer.parseInt(envWarn.get("type"))) {
                    case 1:
                        type = "510";
                        break;
                    case 2:
                        type = "511";
                        break;
                    case 3:
                        type = "512";
                        break;
                    case 4:
                        type = "513";
                        break;
                    case 5:
                        type = "514";
                        break;
                    case 6:
                        type = "515";
                        break;
                    case 7:
                        type = "516";
                        break;
                    case 8:
                        type = "517";
                        break;
                    case 9:
                        type = "518";
                        break;
                    case 10:
                        type = "519";
                        break;
                    case 11:
                        type = "520";
                        break;
                    case 12:
                        type = "521";
                        break;
                    case 13:
                        type = "522";
                        break;
                    default:
                        break;
                }
                envWarn.put("type", type);
                tDroneInfoDao.insertEnv(envWarn);
            }
        } catch (Exception e) {
            log.error("接收环境设备告警数据错误:", e);
        }
    }

    /**
     * 32位UUID生成方法
     *
     * @return 32位UUID生成方法
     */
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();
        if (StringUtils.isNotEmpty(uuidStr)) {
            uuidStr = uuidStr.toUpperCase();
            uuidStr = uuidStr.replaceAll("-", "");
        } else {
            uuidStr = "";
        }
        return uuidStr;
    }

    public void updateTCruiseTask(Map<String, Object> taskMap) {
        tDroneInfoDao.updateTCruiseTask(taskMap);
    }
}

