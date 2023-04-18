package com.yjh.accessrobot.module.command.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.ModelFileEnum;
import com.yjh.accessrobot.common.handler.MyException;
import com.yjh.accessrobot.common.smUtil.Demo;
import com.yjh.accessrobot.common.utils.FtpsUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.CreateModeXMLUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.LogsRecord;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.module.command.dao.*;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.device.utils.StatisticsUtil;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yjh.accessrobot.module.command.CruiseConstant.*;
import static com.yjh.accessrobot.module.command.CruiseConstant.CruiseTypeEnum.LINKAGE_TASK;
import static com.yjh.accessrobot.module.command.CruiseConstant.CruiseTypeEnum.NORMAL_TASK;
import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
 * @author tt
 * @since 2020-08-20
 */
@Service
public class RobotService {

    private Logger log = LoggerFactory.getLogger(RobotService.class);

    private static final String OFF_LINE = "离线";

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private DeviceStatisticInfoResultDao deviceStatisticInfoResultDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private TRobotInspectionAttrService TRobotInspectionService;
    @Autowired
    private TRobotRegionDao tRobotRegionDao;
    @Autowired
    private TCruisePointInstanceService tCruisePointInstanceService;
    @Resource
    private SysUserDao sysUserDao;
    @Value("${other.webSocketUrl}")
    private String webSocketUrl;

    @Resource
    LogsRecord logsRecord;

    @Autowired
    private TStdRegionService tStdRegionService;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Autowired
    private TCameraRecorderService tCameraRecorderService;
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private  TRobotInfoService tRobotInfoService;

    @Resource
    private TStdDeviceModelService tStdDeviceModelService;

    @Resource
    private TDeviceMaintenanceService tDeviceMaintenanceService;

    @Autowired
    private  TVoiceDeviceService tVoiceDeviceService;

    @Transactional(rollbackFor = Exception.class)
    public int updateAllRobotStatus() {
        return tRobotInfoDao.updateAllRobotStatus("离线");
    }

    private static final Set<String> NEED_CONFIRM_SET = new HashSet<>();
    static {
        NEED_CONFIRM_SET.add("1_1"); // 机器人远方复位
        NEED_CONFIRM_SET.add("1_3"); // 机器人一键返航
        NEED_CONFIRM_SET.add("1_5"); // 机器人控制模式切换
        NEED_CONFIRM_SET.add("1_6"); // 机器人控制权获得
        NEED_CONFIRM_SET.add("1_7"); // 机器人控制权释放
        NEED_CONFIRM_SET.add("3_9"); // 机器人云台复位
        NEED_CONFIRM_SET.add("22_8"); // 机器人红外热像仪重启
        NEED_CONFIRM_SET.add("21_8"); // 机器人可见光摄像机重启
        NEED_CONFIRM_SET.add("20001_2"); //无人机系统自检
        NEED_CONFIRM_SET.add("20001_3"); //无人机一键返航
        NEED_CONFIRM_SET.add("20001_4"); //无人机自动降落
        NEED_CONFIRM_SET.add("20001_5"); //无人机控制模式
        NEED_CONFIRM_SET.add("20001_6"); //无人机控制权获得
        NEED_CONFIRM_SET.add("20001_7"); //无人机控制权释放
        NEED_CONFIRM_SET.add("20001_8"); //无人机电源管理
        NEED_CONFIRM_SET.add("20002_7"); //无人机急停
        NEED_CONFIRM_SET.add("20003_5"); //无人机云台重置
        NEED_CONFIRM_SET.add("20005_1"); //机巢
        NEED_CONFIRM_SET.add("20005_2"); //机巢急停
        NEED_CONFIRM_SET.add("20005_3"); //机巢舱门
    }

    /**
     * 巡视主机下发控制指令到机器人
     *
     * @param robotCode 机器人唯一标识
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
    public Map<String, Object> feignRobotControl(String robotCode, String type, String command, String value, String direction, String key, Long userId, String password, HttpServletRequest request, String content) throws Exception {
        String todayTime = DateTimeUtil.format(new Date(), "yyyy/MM/dd");
        Map scmap = new HashMap();
        String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));

        String typeCommand = type + "_" + command;
        if (NEED_CONFIRM_SET.contains(typeCommand)) {
            Map<String, Object> zcz = this.booleanZcz(key, userId, password, request);
            if (zcz.get("code") != null) {
                return zcz;
            }
        }

        List<Map<String, Object>> item = new LinkedList<>();
        Map<String, Object> map = new HashMap<>(3);
        if (StringUtils.isNotEmpty(value)) {
            map.put("value", value);
        }
        if (StringUtils.isNotEmpty(direction)) {
            map.put("direction", direction);
        }
        item.add(map);

        if (StringUtils.isEmpty(robotCode)) {
            log.error("==========没有设置巡视设备编码==========");
            scmap.put("code", 3);
            scmap.put("result", "当前不存在该巡视设备编码,请先添加");

            //记录控制巡视设备具体操作，操作失败
            if (StringUtils.isNotEmpty(content)) {
                logsRecord.LoginLogsSend(request, "5", "控制巡视设备", content, userName, String.valueOf(userId), 2);
            }

            return scmap;
        }
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        String originCode = robotCode;
        if (StringUtils.equals(OFF_LINE, robotStatus)) {
            log.error("==========该巡视设备处于离线状态,没有成功将控制指令下发到巡视设备==========");
            scmap.put("code", 3);
            scmap.put("result", "巡视设备不在线");

            //记录控制巡视设备具体操作，操作失败
            if (StringUtils.isNotEmpty(content)) {
                logsRecord.LoginLogsSend(request, "5", "控制巡视设备", content, userName, String.valueOf(userId), 2);
            }

            return scmap;
        } else {
            TRobotInfo tRobotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
            int robotType = 159;
//            List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(robotCode, 1);
            // 如果stdRegionList为空 则表示下级接的是机器人/无人机
//            boolean isEdge = CollectionUtils.isNotEmpty(stdRegionList);
            robotType = tRobotInfoDao.selectRobotTypeByCode(robotCode);
            if (tRobotInfo.getOriginId() != null){
                robotCode = tRobotInfo.getEdgeCode();
            }

            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(Constant.sendCode)
                    .setReceiveCode(robotCode)
                    .setCode(String.valueOf(tRobotInfo.getRobotNum()))
                    .setTime(DateTimeUtil.format(new Date()))
                    .setType(type)
                    .setCommand(command)
                    .setItems(item);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的控制指令xml是<start>{}<end>", xmlString);

            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + originCode + ":61");
            Map<String, String> robotTaskStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + originCode + ":41");
            String robotTaskStatus = robotTaskStatusMap.get("value");
            String robotPattern = robotStatusMap.get("value");

            // 判断当前巡视系统是否拥有巡视设备控制权
            String controlKey = "RobotStatus:" + originCode + ":811";
            if (StringUtils.equalsAny(typeCommand, "1_6", "20001_6")) {
                redisTemplate.opsForValue().set(controlKey, "1");
            } else if (StringUtils.equalsAny(typeCommand, "1_7", "20001_7")) {
                redisTemplate.opsForValue().set(controlKey, "0");
            } else {
                String controlStatus = (String)redisTemplate.opsForValue().get(controlKey);
                if (!"1".equals(controlStatus)) {
                    log.error("==========当前巡视设备未获取控制权,无法操作==========");
                    scmap.put("code", 3);
                    scmap.put("result", "请先获取当前巡视设备控制权");

                    //记录控制巡视设备具体操作，操作失败
                    if (StringUtils.isNotEmpty(content)) {
                        logsRecord.LoginLogsSend(request, "5", "控制巡视设备", content, userName, String.valueOf(userId), 2);
                    }

                    return scmap;
                }
            }
            redisTemplate.expire(controlKey, 10, TimeUnit.MINUTES);

            if (StringUtils.equals("2", robotTaskStatus) || StringUtils.equals("4", robotTaskStatus)) {
                String returnMsg = "2".equals(robotTaskStatus) ? "巡视状态" : "检修状态";
                log.error("==========当前巡视设备处于" + returnMsg + ",无法操作==========");
                scmap.put("code", 3);
                scmap.put("result", "当前巡视设备处于" + returnMsg + ",无法操作");

                //记录控制巡视设备具体操作，操作失败
                if (StringUtils.isNotEmpty(content)) {
                    logsRecord.LoginLogsSend(request, "5", "控制巡视设备", content, userName, String.valueOf(userId), 2);
                }

                return scmap;
            }
            boolean flag = StringUtils.equals("1", robotPattern)
                    && robotType != 157
                    && !("1".equals(type) && "5".equals(command)) //切换模式
                    && !("20001".equals(type) && "5".equals(command)) //切换模式
                    && !("1".equals(type) && "8".equals(command)) //急停
                    && !("20001".equals(type) && "7".equals(command)); //急停
            if (Boolean.TRUE.equals(flag)) {
                log.error("==========当前巡视设备处于任务模式,请切换模式==========");
                scmap.put("code", 3);
                scmap.put("result", "当前巡视设备处于任务模式,请切换模式");
            } else {
                RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);

                // Capture and video file return
                String filePath = null;
                if (Objects.nonNull(RobotServerHandler.getRobotResultMap().get("Item"))
                        && JSONObject.parseObject(JSON.toJSONString(RobotServerHandler.getRobotResultMap().get("Item"))).containsKey("file_path")) {
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
            }

        //记录控制巡视设备具体操作
        if (StringUtils.isNotEmpty(content)) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", content, userName, String.valueOf(userId), 1);
        }
            return scmap;
        }
    }

    /**
     * 巡视主机下发模型文件同步指令到机器人
     *
     * @param robotCode 机器人唯一标识
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean feignRobotTransfer(String robotCode) {
        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (StringUtils.equals(OFF_LINE, robotStatus)) {
            log.error("==========该巡视设备处于离线状态,没有成功将模型文件同步指令下发到巡视设备==========");
            return false;
        }
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(Constant.sendCode)
                .setReceiveCode(robotCode)
                .setCode(Constant.stationCode)
                .setTime(DateTimeUtil.format(new Date()))
                .setType("61")
                .setCommand("1");
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的模型文件同步指令xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        return true;
    }


    /**
     * 巡视主机下发模型文件同步指令到边缘节点
     * @param edgeCode 边缘节点编码
     * @param command 类型
     * @return boolean
     */
    public boolean feignEdgeTransfer(String edgeCode, String command) {
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeCode, 1);
        if (CollectionUtils.isNotEmpty(stdRegionList)) {
            String stationCode = stdRegionList.get(0).getStationId();
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode(Constant.sendCode)
                    .setReceiveCode(edgeCode)
                    .setCode(stationCode)
                    .setTime(DateTimeUtil.format(new Date()))
                    .setType("61")
                    .setCommand(command);
            String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
            log.info("生成的模型文件同步指令xml是<start>{}<end>", xmlString);
            RobotServerHandler.send(generateByteOrder(xmlString, edgeCode), edgeCode);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 区域巡视主机生成联动、反向联动或顺控文件后，联动文件下发至边缘节点
     * @param edgeCode 边缘节点编码
     * @param command 类型
     * @param filePath 文件路径
     * <1>: =联动配置文件
     * <2>: =一键顺控视频确认反馈信息文件
     * <3>: =反向联动信息转发文件
     * @return boolean
     */
    public boolean linkageFileTransfer(String edgeCode, String command, String filePath) {
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeCode, 1);
        if (CollectionUtils.isNotEmpty(stdRegionList)) {
            try {
                List<Map<String, Object>> mapList = new ArrayList<>();
                Map<String, Object> map = new HashMap<>(2);
                String stationCode = stdRegionList.get(0).getStationId();
                //<1>: =联动配置文件
                if ("1".equals(command)) {
                    filePath = createLinkageModel(edgeCode, stationCode);
                }
                map.put("file_path", filePath);
                mapList.add(map);
                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setSendCode(Constant.sendCode)
                        .setReceiveCode(edgeCode)
                        .setCode(stationCode)
                        .setTime(DateTimeUtil.format(new Date()))
                        .setType("71")
                        .setCommand(command)
                        .setItems(mapList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                log.info("生成的联动文件下发指令xml是<start>{}<end>", xmlString);
                RobotServerHandler.send(generateByteOrder(xmlString, edgeCode), edgeCode);
                return true;
            } catch (Exception e) {
                log.error("联动文件下发失败", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 创建联动配置文件
     * @return
     * @throws Exception
     */
    private String createLinkageModel(String edgeCode, String stationCode) throws Exception {
        //联动配置文件
        Map<String, String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        String ftpsFilePath = System.getProperty("os.name").toUpperCase().startsWith("WINDOWS") ? "C:\\robotData\\Model" : mapForPath.get("content");
        List<Map<String,Object>> infoList = tRobotInfoDao.selectTCfgUnionRule(edgeCode);
        return CreateModeXMLUtil.createXmlFile(infoList, ftpsFilePath, stationCode, "linkage_model.xml", "Effect_Config");
    }

    /**
     * 生成发送byte指令，附带测试
     *
     * @param xmlString xml数据
     * @param robotCode 机器人唯一标识
     * @return byte[]
     */
    public byte[] generateByteOrder(String xmlString, String robotCode) {
        long sendSessionId = Constant.AtomicSessionId.addAndGet(1);
        ChannelHandlerContext context = RobotServerHandler.getChannelHandlerContextByRobot(robotCode);
        log.info("context是<start>{}<end>", context);
        if (context != null) {
            // 请求报文每次累加1
//            sendSessionId = Constant.AtomicSessionId.addAndGet(1);
            Constant.sendSessionId = sendSessionId;
        } else {
            Constant.sendSessionId = 0L;
            Constant.AtomicSessionId.set(0);
        }
        log.info("-------------这是刚发命令的请求{}-------------", sendSessionId);
        return PlatformPacketUtil.createPacket(sendSessionId, 0, true, xmlString);
    }

    /**
     * 更新机器人的在线状态
     *
     * @param robotCode   机器人唯一标识
     * @param robotStatus 在线状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRobotInfo(String robotCode, String robotStatus) {
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(robotCode, 1);
        // 如果边缘节点 code 不为空，则表示底端上传数据的是边缘节点，不是机器人或无人机
        boolean isEdge = CollectionUtils.isNotEmpty(stdRegionList);
        if (isEdge) {
            TStdRegion tStdRegion = new TStdRegion();
            tStdRegion.setRegionId(stdRegionList.get(0).getRegionId());
            tStdRegion.setEdgeStatus(robotStatus);
            int res = tStdRegionDao.update(tStdRegion);
            log.info("edgeCode为==={},regionId为==={}的节点状态是==={},修改结果==={}", robotCode, tStdRegion.getRegionId(), tStdRegion.getEdgeStatus(), res);
            //边缘节点下线  机器人所有状态全部下线
            if ("离线".equals(robotStatus)){
                tStdRegionDao.updateRobotStatus(robotCode);
            }
        } else {
            Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);
            if (Objects.nonNull(robotId)) {
                TRobotInfo tRobotInfo = new TRobotInfo()
                        .setRobotId(robotId)
                        .setRobotStatus(robotStatus);
                StatisticsUtil.onlineDuration(tRobotInfo);
                int res = tRobotInfoDao.update(tRobotInfo);
                log.info("robotCode为==={},robotId为==={}的巡视设备状态是==={},修改结果==={}", robotCode, robotId, tRobotInfo.getRobotStatus(), res);
                TRobotInfo robotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
                RobotStatusObserver.postNetStatus(robotInfo.getRobotName(), robotInfo.getRobotNum(), robotStatus);
            }
        }
    }

    /**
     * 查询表中所有的机器人
     *
     * @return List<String>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllRobotCode() {
        return tRobotInfoDao.selectAllRobotCode();
    }

    /**
     * 处理机器人返回的模型文件
     *
     * @param map 机器人返回的模型文件相关信息
     * @param nodeCode 节点
     * @return void
     */
    public void addRobotFile(Map<String, Object> map, String nodeCode) {
        if (StringUtils.isEmpty(nodeCode)){
            log.error("机器人或边点编码为空, Code：{}", nodeCode);
            throw new RuntimeException("机器人编码为空");
        }
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")){
            filePathMap.put("content","C:\\robotData\\Model");
        }
        String filePathPrefix = filePathMap.get("content");

        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(nodeCode, 1);
        // 如果边缘节点 code 不为空，则表示底端上传数据的是边缘节点，不是机器人或无人机
        boolean isEdge = CollectionUtils.isNotEmpty(stdRegionList);

        Long robotId = isEdge ? 1 : tRobotInfoDao.selectRobotIdByCode(nodeCode);

        if (MapUtils.isEmpty(map)) {
            return;
        }
        try {
            log.info("map=={}", map);
            map.forEach((k,v)->{
                String filePath = filePathPrefix + File.separator + v;
                try {
                    List<Map<String, Object>> mapList = new ArrayList<>();
                    if(!k.equals("map_file_path") && !k.equals("source_file_path")) {
                        XMLBaseModel model = getXmlMessage(filePath);
                        mapList = model.getItems();
                    }
                    switch (k){
                        case "device_file_path":
                            if (isEdge){
                                syncModelUpdate("1", v.toString(), nodeCode);
                            }else {
                                // Device Point Info
                                addDevicePoint(mapList, robotId);
                                // Device Point Region Info
                                addDevicePointRegion(mapList, robotId);
                            }
                            break;
                        case "robot_file_path":
                            // Robot Model Info
                            if (isEdge){
                                dealRobotFile(filePath, nodeCode, Constant.ROBOT);
                            }else {
                                addRobotModel(mapList, robotId);
                            }
                            break;
                        case "property_file_path":
                            //Property Info 属性信息 与点位绑定
                            addPropertyModel(mapList, robotId);
                            break;
                        case "region_file_path":
                            dealRegionFile(filePath, nodeCode);
                            break;
                        case "map_file_path":
                            dealMapFile(filePath, nodeCode);
                            break;
                        case "host_file_path":
                            dealHostFilePath(filePath, nodeCode);
                            break;
                        case "video_file_path":
                            dealCameraFile(filePath, nodeCode);
                            break;
                        case "drone_file_path":
                            dealRobotFile(filePath, nodeCode, Constant.DRONE);
                            break;
                        case "voice_file_path":
                            dealVoiceFile(filePath, nodeCode);
                            break;
                        case "record_file_path":
                            dealRecordFile(filePath, nodeCode);
                            break;
                        case "overhaularea_file_path":
                            dealMaintenanceFilePath(filePath, nodeCode);
                            break;
                        case "source_file_path":
                            dealSourceFile(filePath, nodeCode);
                            break;
                        default:
                            log.warn("模型解析未定义，{}: {}", k, filePath);
                            break;
                    }
                } catch (DocumentException e) {
                    log.error("解析模型失败，modelPath: {}", filePath, e);
                }
            });
        }catch (Exception e){
            log.error("处理机器人返回的模型文件异常: ", e);
        }
    }
    /**
     * 机器人模型文件信息处理
     *
     * @param robotMap 模型文件信息
     * @param robotId  机器人id
     * @return void
     */
    public void addRobotModel(List<Map<String, Object>> robotMap, Long robotId) {
        if (CollectionUtils.isEmpty(robotMap)) {
            return;
        }
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");

        String picPath = filePathMap.get("content") + "/" + robotMap.get(0).get("mappath").toString();
        log.info("图片路径为：{}", picPath);

        TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Map<String, Object> robotModelMap = robotMap.get(0);
            //生产日期
            String productionDate = String.valueOf(robotModelMap.get("production_date"));
            if (StringUtils.isNotEmpty(productionDate) && !"null".equals(productionDate)) {
                tRobotInfo.setMadeDate(simpleDateFormat.parse(productionDate));
            }
            //生产编号
            String productionCode = String.valueOf(robotModelMap.get("production_code"));
            if (StringUtils.isNotEmpty(productionCode) && !"null".equals(productionCode)) {
                tRobotInfo.setAppearanceNumber(productionCode);
            }
            //生产厂家
            String manufacturer = String.valueOf(robotModelMap.get("manufacturer"));
            if (StringUtils.isNotEmpty(manufacturer) && !"null".equals(manufacturer)) {
                String robotFactory = tRobotInfoDao.selectDictCodeByNote(manufacturer, "robot_factory");
                if (robotFactory != null) {
                    tRobotInfo.setRobotFactory(robotFactory);
                }
            }
            //使用单位
            String useUnit = String.valueOf(robotModelMap.get("use_unit"));
            if (StringUtils.isNotEmpty(useUnit) && !"null".equals(useUnit)) {
                tRobotInfo.setBuildingUser(useUnit);
            }
//                String istransport = String.valueOf(robotModelMap.get("istransport"));
            //设备来源
            String deviceSource = String.valueOf(robotModelMap.get("device_source"));
            if (StringUtils.isNotEmpty(deviceSource) && !"null".equals(deviceSource)) {
                tRobotInfo.setRobotSource(deviceSource);
            }
        } catch (Exception e) {
            log.error("模型文件解析异常:", e);
        }

        String[] splitArray = picPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        String developMap = absoluteImgMap.get("content") + "/Map";
        copyFileToDevelop(picPath, developMap);
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;

        tRobotInfo.setRobotId(robotId)
                .setPhotePath(developRelativeUrl);
        tRobotInfoDao.update(tRobotInfo);
    }

    /**
     * @param propertyMapList 属性文件信息
     */
    private void addPropertyModel(List<Map<String, Object>> propertyMapList, Long robotId) {
        if (CollectionUtils.isNotEmpty(propertyMapList)) {
            List<TRobotInspectionAttr> robotInspectionAttrList = new ArrayList<>();
            for (Map<String, Object> propertyMap : propertyMapList) {
                TRobotInspectionAttr tRobotInspectionAttr = new TRobotInspectionAttr()
                        .setRobotId(robotId)
                        .setInspectionCode(propertyMap.get("device_id").toString())
                        .setX(Integer.parseInt(propertyMap.get("x").toString()))
                        .setY(Integer.parseInt(propertyMap.get("y").toString()))
                        .setWidth(Integer.parseInt(propertyMap.get("width").toString()))
                        .setHeight(Integer.parseInt(propertyMap.get("height").toString()));
                robotInspectionAttrList.add(tRobotInspectionAttr);
            }
            //删除该机器人的测点属性表
            TRobotInspectionService.deleteByRobotId(robotId);
            TRobotInspectionService.batchInsert(robotInspectionAttrList);
        }
    }

    /**
     * 更新机器人测点信息
     *
     * @param deviceMapList 设备点位文件信息
     * @param robotId       机器人id
     * @return void
     */
    private void addDevicePoint(List<Map<String, Object>> deviceMapList, Long robotId) {
        List<TRobotInspection> tRobotInspectionsInfoModelFile = new ArrayList<>();

        String linkMete = redisTemplate.opsForHash().entries("t_sys_param:linkMete").get("content").toString();
        for (Map<String, Object> deviceMap : deviceMapList) {
            try {
                TRobotInspection tRobotInspection = new TRobotInspection();
                tRobotInspection.setInspectionCode(String.valueOf(deviceMap.getOrDefault("device_id", "")));
                tRobotInspection.setRobotId(robotId);
                tRobotInspection
                    .setInspectionName(deviceMap.getOrDefault("main_device_name", "") + "/" + deviceMap.getOrDefault("device_name", ""));
                // save_type_list和recognition_type_list为空的话，容错  默认为jpg和1
                tRobotInspection.setDeviceName(String.valueOf(deviceMap.getOrDefault("device_name", "")));
                tRobotInspection.setSaveTypeList(String.valueOf(deviceMap.getOrDefault("save_type_list", "jpg")));
                tRobotInspection.setComponentId(String.valueOf(deviceMap.getOrDefault("component_id", "")));
                tRobotInspection.setComponentName(String.valueOf(deviceMap.getOrDefault("component_name", "")));
                tRobotInspection.setMainDeviceId(String.valueOf(deviceMap.getOrDefault("main_device_id", "")));
                tRobotInspection.setMainDeviceName(String.valueOf(deviceMap.getOrDefault("main_device_name", "")));
                tRobotInspection.setBayId(String.valueOf(deviceMap.getOrDefault("bay_id", "")));
                tRobotInspection.setBayName(String.valueOf(deviceMap.getOrDefault("bay_name", "")));
                tRobotInspection.setAreaName(String.valueOf(deviceMap.getOrDefault("area_name", "")));
                tRobotInspection.setAreaId(String.valueOf(deviceMap.getOrDefault("area_id", "")));
                tRobotInspection.setDeviceType(String.valueOf(deviceMap.getOrDefault("device_type", "")));
                tRobotInspection.setRecognitionTypeList(String.valueOf(deviceMap.getOrDefault("recognition_type_list", "1")));
                int inspectionType = 1;
                if (deviceMap.containsKey("point_type") && !"".equals(deviceMap.get("point_type").toString())) {
                    inspectionType = Integer.parseInt(deviceMap.get("point_type").toString());
                }
                tRobotInspection.setInspectionType(inspectionType);
                if (!"".equals(deviceMap.get("meter_type").toString())) {
                    Integer meterType = selectDictCode("meterType", deviceMap.get("meter_type").toString(), "meter_type");
                    tRobotInspection.setMeterType(meterType);
                }
                if (!"".equals(deviceMap.get("appearance_type").toString())) {
                    Integer appearanceType =
                        selectDictCode("appearanceType", deviceMap.get("appearance_type").toString(), "appearance_type");
                    tRobotInspection.setAppearanceType(appearanceType);
                }
                if (deviceMap.containsKey("main_operation_type") && !"".equals(deviceMap.get("main_operation_type").toString())) {
                    Integer mainOperationType =
                        selectDictCode("mainOperationType", deviceMap.get("main_operation_type").toString(), "main_operation_type");
                    tRobotInspection.setMainOperationType(mainOperationType);
                }
                if (deviceMap.containsKey("operation_type") && !"".equals(deviceMap.get("operation_type").toString())) {
                    Integer operationType = selectDictCode("operationType", deviceMap.get("operation_type").toString(), "operation_type");
                    tRobotInspection.setOperationType(operationType);
                }
                if (!"".equals(deviceMap.get("phase").toString())) {
                    tRobotInspection.setPhase(deviceMap.get("phase").toString());
                }
                if (!"".equals(deviceMap.get("device_info").toString())) {
                    tRobotInspection.setDeviceInfo(deviceMap.get("device_info").toString());
                }
                if (deviceMap.containsKey("property_pic_path") && !"".equals(deviceMap.get("property_pic_path").toString())) {
                    String relativePropertyPicPath = convertPropertyPicPath(deviceMap.get("property_pic_path").toString());
                    tRobotInspection.setPropertyPicPath(relativePropertyPicPath);
                }

                tRobotInspectionsInfoModelFile.add(tRobotInspection);
                //            log.info("获得的deviceList是：" + deviceList);
            } catch (Exception e) {
                log.error("获取设备点位信息异常:", e);
            }
        }

        // 该机器人现有的巡检点
        List<String> nowInspectionList = tRobotInspectionDao.selectAllByRobotId(robotId);
        //        log.info("机器人现有的deviceList是：" + nowList);

        List<TRobotInspection> newInspectionList = new ArrayList<>();
        for (TRobotInspection tRobotInspection : tRobotInspectionsInfoModelFile) {
            if (nowInspectionList.contains(tRobotInspection.getInspectionCode())) {
                nowInspectionList.remove(tRobotInspection.getInspectionCode());
            } else {
                newInspectionList.add(tRobotInspection);
            }
        }
        log.info("最后要插库的deviceList是==={}", newInspectionList.size());
        log.info("准备要删除的inspectionCodeList是==={}", nowInspectionList.size());
        if (CollectionUtils.isNotEmpty(nowInspectionList)) {
            List<Long> inspectionIdList = tRobotInspectionDao.selectInspectionIdList(nowInspectionList);
            //            log.info("这些inspectionCode对应的inspectionIdList是==" + inspectionIdList);
            List<Long> instanceIdList = tRobotInspectionDao.selectInstanceIdList(inspectionIdList);
            //            log.info("这些inspectionId对应的instanceIdList是==" + instanceIdList);

            // 删库TRI
            int deleteCountTRI = 0, deleteCountTCPI = 0, deleteCountTCPA = 0, deleteCountTDM = 0;
            if (CollectionUtils.isNotEmpty(inspectionIdList)) {
                deleteCountTRI = tRobotInspectionDao.batchDeleteTRobotInspection(inspectionIdList);
            }
            if (CollectionUtils.isNotEmpty(instanceIdList)) {
                //查询出instanceId对应的设备测点信息
                List<Long> deviceMeteIds = tRobotInspectionDao.selectDeviceMeteIdByInstanceId(instanceIdList);
                // 删库TCPI
                deleteCountTCPI = tRobotInspectionDao.batchDeleteTCruisePointInstance(instanceIdList);
                // 删库TCPA
                deleteCountTCPA = tRobotInspectionDao.batchDeleteTCruisePlanAttr(instanceIdList);
                //查询出清理后的无绑定关系的测点
                List<Long> deviceMeteIdsDeleted = tRobotInspectionDao.selectDeviceMeteIdByDeviceMeteId(deviceMeteIds);
                deleteCountTDM = tRobotInspectionDao.batchDeleteTDeviceMete(deviceMeteIdsDeleted);
            }
            log.info("TRI删除条数=={},TCPI删除条数=={},TCPA删除条数=={},TDM删除条数=={}", deleteCountTRI, deleteCountTCPI, deleteCountTCPA,deleteCountTDM);
        }
        if (CollectionUtils.isNotEmpty(newInspectionList)) {
            // 插库TRI
            int res = tRobotInspectionDao.batchInsertTRobotInspection(newInspectionList);
            log.info("TRI插入条数=={}", res);
        }
        List<TRobotInspection> updateInspections = tRobotInspectionsInfoModelFile
            .stream()
            .filter(o->newInspectionList.contains(o))
            .collect(Collectors.toList());;

        log.info("准备更新的deviceList是=={}", updateInspections.size());
        for (TRobotInspection item : updateInspections) {
            // 更新TRI
            tRobotInspectionDao.update(item);
        }

        //自动建立并连接测点
        if ("true".equals(linkMete)) {
            tCruisePointInstanceService.instanceLinkAuto(tRobotInspectionsInfoModelFile,robotId);
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
     * 更新机器人测点区域信息
     *
     * @param deviceMapList 设备点位文件信息
     * @param robotId       机器人id
     * @return void
     */
    private void addDevicePointRegion(List<Map<String, Object>> deviceMapList, Long robotId) {
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
        for (TRobotRegion str : lst) {
            if (nowRobotRegionList.contains(str.getRegionId())) {
                nowRobotRegionList.remove(str.getRegionId());
            } else {
                addRobotRegionList.add(str);
            }
        }
        log.info("最后要插库的regionList是=={}", addRobotRegionList);
        log.info("准备要删除的regionList是=={}", nowRobotRegionList);
        if (CollectionUtils.isNotEmpty(nowRobotRegionList)) {
            int res = tRobotRegionDao.batchDelete(nowRobotRegionList);
            log.info("TRR删除条数=={}", res);
        }
        if (CollectionUtils.isNotEmpty(addRobotRegionList)) {
            // 插库TRR
            int res = tRobotRegionDao.batchInsert(addRobotRegionList);
            log.info("TRR的插入条数是=={}", res);
        }
        if (lst.containsAll(addRobotRegionList)) {
            lst.removeAll(addRobotRegionList);
        }
        log.info("准备更新的list是=={}", lst);
        for (TRobotRegion tRobotRegion : lst) {
            // 更新TRR
            tRobotRegionDao.update(tRobotRegion);
        }
    }

    /**
     * 机器人控制、任务下发及检修区域下发指令返回响应处理
     *
     * @param xmlBaseModel     xml格式的内容
     * @param receiveSessionId 接收会话序列号
     * @return Map<String, Object>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receivingResponse(XMLBaseModel xmlBaseModel, long receiveSessionId) {
        RobotServerHandler.getRobotResultMap().put("Code", xmlBaseModel.getCode());
        RobotServerHandler.getRobotResultMap().put("receiveSessionId", receiveSessionId);

        String todayTime = DateTimeUtil.format(new Date(), "yyyy/MM/dd");

        if (Constant.sendSessionId == receiveSessionId) {
            log.info("-------------这是刚发命令的响应{}-------------", receiveSessionId);
            if (Objects.isNull(xmlBaseModel.getItems()) || xmlBaseModel.getItems().isEmpty()) {
//                RobotServerHandler.getRobotResultMap().put("Item", null);
            } else {
                for (Map<String,Object> map : xmlBaseModel.getItems()) {
                    RobotServerHandler.getRobotResultMap().put("Item", map);

                    Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
                    Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
                    Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");

                    // 文件在ftp服务器上的绝对路径
                    String temporaryPath = filePathMap.get("content");

                    if (map.containsKey("file_path")) {
                        String ftpFilePath = String.valueOf(map.get("file_path"));
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

                        // 将机器人摄像机抓图结果从ftp服务器上的复制到开发环境
                        copyFileToDevelop(temporaryPath, developAbsoluteUrl);
                        RobotServerHandler.getRobotResultMap().put("developAbsoluteUrl", developAbsoluteUrl);
                        RobotServerHandler.getRobotResultMap().put("developRelativeUrl", developRelativeUrl);
                    }
                    if (ModelFileEnum.getMapByNameSet(map.keySet()) != null) {
                        String code = ModelFileEnum.getMapByNameSet(map.keySet()).get("code");
                        String name = ModelFileEnum.getMapByNameSet(map.keySet()).get("name");
                        syncModelUpdate(code,map.get(name).toString(),xmlBaseModel.getSendCode());
                    }
                }
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
     * 机器人检修区域下发
     *
     * @param resMap 传来的机器人检修区域相关信息
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String deviceMaintenanceIssued(Map<String, Object> resMap) {
        List<Map<String, Object>> itemList = new ArrayList<>();
        String onlineCode = String.valueOf(resMap.get("online_code"));
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByRegionCodeAndState(onlineCode, 1);
        String stationId = CollectionUtils.isNotEmpty(tStdRegionList) ? tStdRegionList.get(0).getStationId() : Constant.stationCode;
        resMap.remove("online_code");
        itemList.add(resMap);
        XMLBaseModel xmlBaseModel =
            new XMLBaseModel()
                .setSendCode(Constant.sendCode)
                .setReceiveCode(onlineCode)
                .setCode(stationId)
                .setType("81")
                .setCommand("4")
                .setItems(itemList);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的检修区域指令xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, onlineCode), onlineCode);
        return "true";
    }

    /**
     * 判断机器人是否正常运作之后再下发任务
     *
     * @param itemMap 传来的机器人任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTaskIssued(Map<String, List<RobotTaskInstanceInfo>> itemMap) throws Exception {
        List<RobotTaskInstanceInfo> robotTaskInfoList = itemMap.get("robotTaskInfoList");
        log.info("robotTaskInfoList是==={}", robotTaskInfoList);

        for (RobotTaskInstanceInfo item : robotTaskInfoList) {
            if (StringUtils.isNotEmpty(item.getRobotCode())) {
                // 机器人-任务下发
                boolean robotStatus = checkRobotStatus(item);
                if (robotStatus) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                feignRobotTask(item);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    };
                    TaskExecutePool.getInstance().execute(runnable);
                }
            }
            if (StringUtils.isNotEmpty(item.getEdgeCode())){
                // 边缘节点-任务下发
                boolean edgeStatus = checkEdgeStatus(item.getEdgeCode());
                if (edgeStatus) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                feignEdgeTask(item);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    };
                    TaskExecutePool.getInstance().execute(runnable);
                }
            }
        }
    }

    /**
     * 判断机器人是否正在运作
     *
     * @param item
     * @return
     * @throws InterruptedException
     */
    private boolean checkRobotStatus(RobotTaskInstanceInfo item) throws InterruptedException {
        String robotOnlineStatus = tRobotInfoDao.selectStatusByRobotCode(item.getRobotCode());
        if (OFF_LINE.equals(robotOnlineStatus)) {
            log.info("=========={}该机器人处于离线状态,没有成功将任务下发到机器人,巡视结果数据默认==========", item.getRobotCode());
            return false;
        } else {
            Map<String, String> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + item.getRobotCode() + ":41");
            String robotStatus = mapForRobotState.get("value");
            if ("4".equals(robotStatus)) {
                log.info("=========={}该机器人处于检修状态,没有成功将任务下发到机器,巡视结果数据默认==========", item.getRobotCode());
                return false;
            } else {
                // 控制权获得
//                String controlRes = robotModeSwitch(item.getRobotCode(), "1", "6", "");
//                if ("200".equals(controlRes)) {
                //操作任务直接下发（前端切换操作模式）
                if (item.getCruiseType() == 456 || item.getCruiseType() == 508 || item.getCruiseType() == 509) {
                    return true;
                } else {
                    // 任务模式
                    String taskModelRes = robotModeSwitch(item.getRobotCode(), "1", "5", "1");
//                        if ("200".equals(taskModelRes)) {
//                            return true;
//                        }else if ("500".equals(taskModelRes)) {
//                            log.info("==========机器人任务模式切换失败==========");
//                            return false;
//                        }
                    return true;
                }
//                }else if ("500".equals(controlRes)) {
//                    log.info("==========机器人控制权获得失败==========");
//                    return false;
//                }
            }
        }
    }

    /**
     * 正常任务及联动任务下发
     *
     * @param item 传来的机器人任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignRobotTask(RobotTaskInstanceInfo item) {
        log.info("*****Start building information about the ROBOT task*****");

        String robotCode = item.getRobotCode();
        String taskId = item.getTaskId();

        StringJoiner str = new StringJoiner(",");
        // 机器人任务临时信息存放至redis
        putInfoToRedisForRobot(robotCode, taskId, str, item.getInstanceList());

        packageXMLBaseModel(item, robotCode, taskId, str);
    }

    private void putInfoToRedisForRobot(String robotCode, String taskId, StringJoiner str, List<String> instanceIdList) {
        List<Map<String, String>> redisInfoList = new ArrayList<>();
        try {
            // 将instanceIdList放缓存，以备后续使用
            Map<String, Object> instanceListMap = new HashMap<>(5);
            instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
            instanceListMap.put("taskId", taskId);
            redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskId, instanceListMap);

            // 根据instanceIdList查询inspectionCodeList
            for (String instanceId : instanceIdList) {
                String inspectionCode = tRobotInfoDao.selectInspectionCode(instanceId);
                str.add(inspectionCode);

                Map<String, String> redisInfoMap = new HashMap<>(16);
                redisInfoMap.put("robotCode", robotCode);
                redisInfoMap.put("instanceId", instanceId + "");
                redisInfoMap.put("inspectionCode", inspectionCode);
                // redisInfoMap.put("cruiseTime", DateTimeUtil.format(new Date()));
                // redisInfoMap.put("taskId", taskId);
                redisInfoList.add(redisInfoMap);
            }
            for (int i = 0; i < redisInfoList.size(); i++) {
                redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + redisInfoList.get(i).get("robotCode")
                        + ":" + redisInfoList.get(i).get("inspectionCode"), redisInfoList.get(i));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断边缘节点是否在线是否正在运作
     *
     * @param  edgeCode
     * @return boolean
     */
    public boolean checkEdgeStatus(String edgeCode){
        String edgeOnlineStatus = tRobotInfoDao.selectStatusByEdgeCode(edgeCode);

        if (StringUtils.equals(OFF_LINE, edgeOnlineStatus)){
            log.info("=========={}该边缘节点处于离线状态,没有成功将任务下发到边缘节点,巡视结果数据默认==========", edgeCode);
            return false;
        }
        return true;
    }

    /**
     * 正常任务及联动任务下发
     * @param item 传来的边缘节点任务相关信息
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void feignEdgeTask(RobotTaskInstanceInfo item) {
        log.info("*****Start building information about the EDGE task*****");

        String edgeCode = item.getEdgeCode();
        String taskId = item.getTaskId();
        StringJoiner str = new StringJoiner(",");
        for (String instanceId : item.getInstanceList()) {
            str.add(instanceId);
        }
        // 边缘节点任务临时信息存放至redis
        Map<String, Object> instanceListMap = new HashMap<>(5);
        instanceListMap.put("instanceIdList", String.valueOf(item.getInstanceList()));
        instanceListMap.put("taskId", taskId);
        redisTemplate.opsForHash().putAll("RobotTaskStatus:" + edgeCode + ":" + taskId, instanceListMap);

        packageXMLBaseModel(item, edgeCode, taskId, str);
    }

    /**
     * 组装任务XML内容并下发
     *
     * @param uniqueFlag 唯一标识
     * @param item       任务信息
     * @param taskId     任务id
     * @param str        点位id
     */
    private void packageXMLBaseModel(RobotTaskInstanceInfo item, String uniqueFlag, String taskId, StringJoiner str) {
        Map<String, Object> resMap = packageItem(str, item, taskId);
        String code = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
        List<TStdRegion> tStdRegionList = tStdRegionDao.selectByRegionCodeAndState(uniqueFlag, 1);
        if (CollectionUtils.isNotEmpty(tStdRegionList)) {
            code = tStdRegionList.get(0).getStationId();
        }

        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setType(String.valueOf(resMap.get("type")))
                .setSendCode(Constant.sendCode)
                .setReceiveCode(uniqueFlag)
                .setCode(code)
                .setTime(DateTimeUtil.format(new Date()))
                .setCommand("1")
                .setItems((List<Map<String, Object>>) resMap.get("mapList"));
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的任务的xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, uniqueFlag), uniqueFlag);
    }

    /**
     * 给下级节点下发任务控制指令
     * @param robotTaskControlMap 传来的任务相关信息
     * @return int
     */
    @Transactional(rollbackFor = Exception.class)
    public Result feignRobotTaskControl(Map<String, Object> robotTaskControlMap) {
        Result result = new Result();
        log.info("robotTaskControlMap==={}", robotTaskControlMap);
        try {
            String json = JSONObject.toJSONString(robotTaskControlMap.get("robotCodeList"));
            if (StringUtils.equals("null", json)) {
                return result;
            }

            List<String> robotCodeList = JSON.parseArray(json, String.class);
            log.info("robotCodeList==={}", robotCodeList);
            String taskId = String.valueOf(robotTaskControlMap.get("taskId"));
            // 1.任务启动 2.任务暂停 3.任务继续 4.任务停止
            String commandValue = String.valueOf(robotTaskControlMap.get("commandValue"));
            String code = "";
            String tasSkPatrolledId = (String) redisTemplate.opsForHash().get("countForAbnormal:" + taskId, "taskPatrolledId");
            for (String robotCode : robotCodeList) {
//                TRobotInfo tRobotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
                String status;
                String isEdge = tRobotInfoDao.selectStatusByEdgeCode(robotCode);
                if (StringUtils.isNotEmpty(isEdge)){
                    // 下级节点
                    status = tRobotInfoDao.selectStatusByEdgeCode(robotCode);
                }else{
                    // 设备
                    status = tRobotInfoDao.selectStatusByRobotCode(robotCode);
                }

//                if (StringUtils.isNotEmpty(tRobotInfo.getEdgeCode()) && StringUtils.isNotEmpty(tRobotInfo.getOriginId())) {
//                    status = tRobotInfoDao.selectStatusByEdgeCode(tRobotInfo.getEdgeCode());
//                    robotCode = tRobotInfo.getEdgeCode();
//                } else {
//                    status = tRobotInfoDao.selectStatusByRobotCode(robotCode);
//                }
                if (Objects.equals(OFF_LINE, status)) {
                    result.setMessage(209, "The edge is currently offline......");
                    return result;
                }
                code = "1".equals(commandValue) ? taskId : tasSkPatrolledId;
                log.info("taskId=={}, receiveCode=={}, tasSkPatrolledId=={}, code=={}", taskId, robotCode, tasSkPatrolledId, code);

                if (StringUtils.isEmpty(code)) {
                    log.info("节点未上报任务状态信息,无法获取当前巡视任务id");
                    return result;
                }

                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("41")
                        .setSendCode(Constant.sendCode)
                        .setReceiveCode(robotCode)
                        .setCode(code)
                        .setCommand(commandValue)
                        .setTime(DateTimeUtil.format(new Date()));
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                log.info("生成的任务控制xml是<start>{}<end>", xmlString);

                RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
            }
        } catch (Exception e) {
            log.error("给下级节点下发任务控制指令异常:", e);
        }
        return result;
    }

    /**
     * 组装任务下发item内容
     *
     * @param str    点位id
     * @param item   任务信息
     * @param taskId 任务id
     * @return Map<String, Object>
     */
    private Map<String, Object> packageItem(StringJoiner str, RobotTaskInstanceInfo item, String taskId) {
        Map<String, Object> taskItemMap = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(16);

        String deviceIdList = str.toString();

        try {
            map.put("task_code", Optional.ofNullable(taskId).orElse(""));
            map.put("task_name", Optional.ofNullable(item.getTaskName()).orElse(""));
            map.put("device_list", Optional.ofNullable(deviceIdList).orElse(""));
            if (Objects.isNull(item.getUnionTaskStatus())) {
                log.info("This is a normal task！！！！！！！！！！！！！");
                String planType = null;
                switch (item.getCruiseType()) {
                    case ROUTINE_PATROL:
                        planType = "1";
                        break;
                    case SPECIAL_PATROL:
                        planType = "2";
                        break;
                    case LIGHTS_OUT_PATROL:
                    case SPECIAL_PROJECT_PATROL:
                        planType = "3";
                        break;
                    case TOTAL_PATROL:
                    case CUSTOM_PATROL:
                        planType = "4";
                        break;
                    case OPERATION_ORDER_PATROL:
                    case SINGLE_DEVICE_PATROL:
                    case EMERGENCY_PATROL:
                        planType = "5";
                        break;
                    default:
                        break;
                }
                map.put("type", Optional.ofNullable(planType).orElse(""));
                map.put("priority", Optional.ofNullable(item.getPriority()).orElse(""));
                map.put("device_level", item.getDeviceLevel());
                // 定期和立即任务参数
                map.put("fixed_start_time", Optional.ofNullable(item.getFixedStartTime()).orElse(""));
                // 周期任务参数
                map.put("cycle_month", Optional.ofNullable(item.getCycleMonth()).orElse(""));
                map.put("cycle_week", Optional.ofNullable(item.getCycleWeek()).orElse(""));
                map.put("cycle_execute_time", Optional.ofNullable(item.getCycleExecuteTime()).orElse(""));
                map.put("cycle_start_time", Optional.ofNullable(item.getCycleStartTime()).orElse(""));
                map.put("cycle_end_time", Optional.ofNullable(item.getCycleEndTime()).orElse(""));
                // 间隔任务参数
                map.put("interval_number", Optional.ofNullable(item.getIntervalNumber()).orElse(""));
                map.put("interval_type", Optional.ofNullable(item.getIntervalType()).orElse(""));
                map.put("interval_execute_time", Optional.ofNullable(item.getIntervalExecuteTime()).orElse(""));
                map.put("interval_start_time", Optional.ofNullable(item.getIntervalStartTime()).orElse(""));
                map.put("interval_end_time", Optional.ofNullable(item.getIntervalEndTime()).orElse(""));

                map.put("invalid_start_time", Optional.ofNullable(item.getInvalidStartTime()).orElse(""));
                map.put("invalid_end_time", Optional.ofNullable(item.getInvalidEndTime()).orElse(""));
                map.put("isenable", Optional.ofNullable(item.getIsenable()).orElse("0"));
                map.put("creator", Optional.ofNullable(item.getCreator()).orElse("1"));
                map.put("create_time", Optional.ofNullable(item.getCreator()).orElse(DateTimeUtil.format(new Date())));
                mapList.add(map);
                taskItemMap.put("type", NORMAL_TASK.getType());
                taskItemMap.put("mapList", mapList);
            } else {
                log.info("This is a linkage task！！！！！！！！！！！！！");
                map.put("priority", "4");
                map.put("device_level", 3);
                mapList.add(map);
                taskItemMap.put("type", LINKAGE_TASK.getType());
                taskItemMap.put("mapList", mapList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return taskItemMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public UPatrolTask selectTaskByTaskCode(String taskCode){
        return tRobotInfoDao.selectTaskByTaskCode(taskCode);
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
        log.info("confirmMessageMap==={}", confirmMessageMap);
        String robotCode = confirmMessageMap.get("robotCode").toString();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("机器人编码为空,robotCode：{}", robotCode);
            throw new MyException("机器人编码为空");
        }
        String taskId = confirmMessageMap.get("taskId").toString();
        List<Map<String, Object>> cfmList = (List<Map<String, Object>>) confirmMessageMap.get("confirmMsgList");
        Map<String, Object> robotConfirmMsg = new HashMap<>();
        robotConfirmMsg.put("newMessage", "false");
        for (Map<String, Object> map : cfmList) {
            boolean flag = false;
            if (map.get("type").equals("1") && map.get("value").equals("")) {
                robotConfirmMsg = redisTemplate.opsForHash().entries("RobotConfirmMsg:" + robotCode + ":" + taskId);
                robotConfirmMsg.put("confirmMapList", robotConfirmMsg.get("splitConfirmMapList").toString());
                robotConfirmMsg.put("splitConfirmMapList", "");
                robotConfirmMsg.put("newMessage", "true");
                redisTemplate.opsForHash().putAll("RobotConfirmMsg:" + robotCode + ":" + taskId, robotConfirmMsg);
                //webSocket通知前端确认消息
                robotConfirmMsg.put("confirmMapList", JSONArray.parseArray(robotConfirmMsg.get("confirmMapList").toString()));
                String jsons = JSON.toJSONString(robotConfirmMsg);
                log.info("确认消息生成-前端推送：" + jsons);
                try {
                    Constant.postUrl(webSocketUrl, jsons);
                } catch (IOException | URISyntaxException e) {
                    log.error(e.getMessage(), e);
                }
                flag = true;
            }
            if (flag) {
                return result;
            }
            //线路保护装置下发的命令都为新消息 不修改状态
            if (map.get("type").equals("10")) {
                robotConfirmMsg.put("newMessage", "true");
            }
        }

        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (robotStatus.equals("离线")) {
            result.setMessage(209, "该机器人处于离线状态,没有成功将确认消息下发到机器人......");
            return result;
        } else {
            if (!cfmList.isEmpty()) {
                XMLBaseModel xmlBaseModel = new XMLBaseModel()
                        .setType("51")
                        .setSendCode(Constant.sendCode)
                        .setReceiveCode(robotCode)
                        .setCode(taskId)
                        .setCommand("1")
                        .setTime(DateTimeUtil.format(new Date()))
                        .setItems(cfmList);
                String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                log.info("生成的任务控制xml是<start>" + xmlString + "<end>");
                Map<String, String> robotControlModelMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":61");
                String robotPattern = robotControlModelMap.get("value");
                if ("5".equals(robotPattern)) {
                    RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
                } else {
                    result.setMessage(209, "该机器人未处于操作模式！！！");
                }
            }
        }
        redisTemplate.opsForHash().putAll("RobotConfirmMsg:" + robotCode + ":" + taskId, robotConfirmMsg);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectTaskResultId(String taskId) {
        return tRobotInfoDao.selectTaskResultId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectRealCodeByInstanceId(Long instanceId) {
        return tRobotInfoDao.selectRealCodeByInstanceId(instanceId);
    }

    public String selectRealTaskId(String robotTaskId) {
        return tRobotInfoDao.selectRealTaskId(robotTaskId);
    }

    public String selectTaskId(String robotTaskId) {
        return tRobotInfoDao.selectTaskId(robotTaskId);
    }
    public TCruiseTask selectCruiseTask(String taskId, String taskCode){
        return tRobotInfoDao.selectCruiseTask(taskId, taskCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectTCruiseTask(String taskId) {
        return tRobotInfoDao.selectTCruiseTask(taskId);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> selectDictCodeByColName(String colName) {
        return tRobotInfoDao.selectDictCodeByColName(colName);
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
     *
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
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "获取验证码", sysUserCurrent.getUserName(), String.valueOf(userId), 1);
            map.put("code", 1);
            map.put("result", random);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isEmpty(password) && keys != null) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "获取密钥", sysUserCurrent.getUserName(), String.valueOf(userId), 1);
            map.put("code", 1);
            map.put("result", keys);
            return map;
        } else if (StringUtils.isEmpty(key) && StringUtils.isNoneBlank(password) && keys == null) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "未输入密钥", sysUserCurrent.getUserName(), String.valueOf(userId), 2);
            map.put("code", 2);
            map.put("result", "密钥不正确");
            return map;
        } else if (keys == null && StringUtils.isNoneBlank(key) && StringUtils.isNoneBlank(password)) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "密钥已过期", sysUserCurrent.getUserName(), String.valueOf(userId), 2);
            map.put("code", 2);
            map.put("result", "密钥不正确");
            return map;
        } else if (!key.equals(keys)) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "密钥不正确", sysUserCurrent.getUserName(), String.valueOf(userId), 2);
            map.put("code", 2);
            map.put("result", "密钥不正确");
            return map;
        } else if (!Demo.decryptDB(sysUserCurrent.getPassword()).equals(password)) {
            logsRecord.LoginLogsSend(request, "5", "控制巡视设备", "密码错误", sysUserCurrent.getUserName(), String.valueOf(userId), 2);
            map.put("code", 2);
            map.put("result", "用户密码错误，请重新输入");
            return map;
        }
        Constant.map.remove(String.valueOf(userId));
        return map;
    }

    /**
     * 结果可靠性统计
     *
     * @param map xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String cruiseStatistic(Map<String,String> map) {
        Map<String,Object> item = Maps.newHashMap();
        item.put("begin_time",map.get("beginDate"));
        item.put("end_time",map.get("endDate"));
        item.put("type",map.get("type"));
        item.put("year",map.get("year"));
        item.put("month",map.get("month"));

        List<Map<String, Object>> items = new LinkedList<>();
        items.add(item);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(Constant.sendCode)
                .setReceiveCode(map.get("robotCode"))
                .setCode(Constant.stationCode)
                .setType("121")
                .setCommand(map.get("command"))
                .setItems(items);

        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);

        RobotServerHandler.send(generateByteOrder(xmlString, map.get("robotCode")), map.get("robotCode"));
        return "success";
    }
    /**
     * 上层服务即集控命令下发
     *
     * @param xmlBaseModel xml格式的内容
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String upSystemCommand(XMLBaseModel xmlBaseModel) {
        String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
        //上级下发的 code 是robotNum
        TRobotInfo tRobotInfo = tRobotInfoDao.selectRobotInfoByRobotNum(xmlBaseModel.getCode(), "");
        String receiveCode = tRobotInfo.getEdgeCode();
        //到边缘节点 receiveCode 为巡视设备的唯一标识
        if (EdgeEnum.EDGE_NODE.getCode().equals(edgeLevel)) {
            receiveCode = tRobotInfo.getRobotCode();
        }

        xmlBaseModel.setSendCode(Constant.sendCode);
        xmlBaseModel.setReceiveCode(receiveCode);
        log.info("xmlBaseModel {} ", xmlBaseModel);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的机器人控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, receiveCode), receiveCode);
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
        Map<String, List<XMLBaseModel>> robotMap = new HashMap<>();
        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        robotMap.put("list", list);
        robotTaskStates(robotMap);
        return "success";
    }

    public Result robotTaskStates(Map<String, List<XMLBaseModel>> robotMap) {
        Result result = new Result();
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                result = StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.SEND_ROBOT_URL, robotMap, Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 将机器人传来的信息对应到巡视主机
     *
     * @param valueName 类型名称
     * @param value     值
     * @param colName   巡视主机对应类型
     * @return Integer
     */
    public Integer selectDictCode(String valueName, String value, String colName) {
        return NumberUtils.toInt(tRobotInfoDao.selectDictCodeByUpdict(colName, value));
    }

    /**
     * 若修改机器人编码或删除机器人断开在线的机器人连接
     *
     * @param robotCode 机器人唯一标识
     * @param robotId   机器人id
     * @return String
     */
    public String removeLink(String robotCode, Long robotId) {
        RobotServerHandler.removeLink(robotCode);
        return OFF_LINE;
    }

    public List<TCruiseTaskResultDetail> selectRepairTCTRDList(String taskId) {
        return this.tRobotInfoDao.selectRepairTCTRDList(taskId);
    }

    public List<TCruiseDataResult> selectRepairTCDRList(String taskId) {
        return this.tRobotInfoDao.selectRepairTCDRList(taskId);
    }

    public TCruisePointInstanceDetail selectForTask(Long instanceId) {
        return this.tRobotInfoDao.selectForTask(instanceId);
    }

    /**
     * 根据测点查询关联算法信息
     *
     * @param deviceMeteId 测点信息
     * @return List<TAlgorithmInfo>
     */
    public List<TAlgorithmInfo> selectByDeviceMeteId(Long deviceMeteId) {
        return this.tRobotInfoDao.selectByDeviceMeteId(deviceMeteId);
    }

    /**
     * 根据测点id查询测点信息
     *
     * @param deviceMeteId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMete(Long deviceMeteId) {
        return this.tRobotInfoDao.selectDeviceMete(deviceMeteId);
    }

    /**
     * 机器人站端任务信息入库
     *
     * @param taskModelMapList 机器人本体任务相关数据
     * @param xmlBaseModel     xml格式的内容
     * @return int
     */
    public void addRobotSelfTask(List<Map<String, Object>> taskModelMapList, XMLBaseModel xmlBaseModel) {
        try {
            Long robotId = tRobotInfoDao.selectRobotIdByCode(xmlBaseModel.getSendCode());
            Long upRegionId = tRobotInfoDao.selectByPrimaryId(robotId).getUpRegionId();
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
                    tCruiseTask.setRobotId(robotId);
                    // 机器人本体任务
                    tCruiseTask.setTaskType(271);
                    if (!"".equals(cruiseTaskModelMap.get("priority").toString())) {
                        tCruiseTask.setTaskLevel(Integer.valueOf(cruiseTaskModelMap.get("priority").toString()));
                    }
                    tCruiseTask.setCreateTime(new Date());
                    tCruiseTaskList.add(tCruiseTask);
                    log.info("tCruiseTaskList==={}", tCruiseTaskList);

                    // 构建t_cruise_point_instance
                    List<TCruisePointInstance> tCruisePointInstanceList = buildTCruisePointInstance(cruiseTaskModelMap, robotId);

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
                        redisInfoMap.put("robotCode", xmlBaseModel.getSendCode());
                        redisInfoMap.put("instanceId", tCruisePointInstance.getInstanceId() + "");
                        Map<String, Object> map = tRobotRegionDao.selectInspectionId(tCruisePointInstance.getCruiseId());
                        redisInfoMap.put("inspectionCode", map.get("inspection_code").toString());
                        redisInfoMap.put("deviceName", map.get("region_name").toString());
                        redisInfoMap.put("taskId", taskId);
                        redisInfoMap.put("inspectionName", tCruisePointInstance.getCruiseName());
                        redisInfoList.add(redisInfoMap);
                    }
                    log.info("redisInfoList是==={}", redisInfoList);
                    // 放数据到缓存
                    for (Map<String, String> stringStringMap : redisInfoList) {
                        redisTemplate.opsForHash().putAll("Robot_SPAndIN_Info:" + stringStringMap.get("robotCode")
                                + ":" + stringStringMap.get("inspectionCode"), stringStringMap);
                    }

                    log.info("tCruiseTaskAttrList==={}", tCruiseTaskAttrList);
                    tRobotInfoDao.batchInsertTaskAttr(tCruiseTaskAttrList);

                    // 将instanceIdList放缓存，以备后续使用
                    log.info("instanceIdList是==={}", instanceIdList);
                    Map<String, Object> instanceListMap = new HashMap<>(5);
                    instanceListMap.put("instanceIdList", String.valueOf(instanceIdList));
                    instanceListMap.put("taskId", taskId);
                    redisTemplate.opsForHash().putAll("RobotTaskStatus:" + xmlBaseModel.getSendCode() + ":" + taskId, instanceListMap);

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
                if (CollectionUtils.isNotEmpty(tCruiseTaskList)) {
                    tRobotInfoDao.batchInsertTask(tCruiseTaskList);
                }
            }
            //1.处理操作任务模型(操作票)
            List<Map<String, Object>> operationSelfTask = taskModelMapList.stream().filter(task -> "5".equals(task.get("type").toString())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(operationSelfTask)) {
                Map<String, Object> params = new HashMap<>();
                params.put("robotId", robotId);
                List<String> orderOperationSelfTask = (List<String>) Constant.restTemplateGet(Constant.GET_PLAN_LIST, params).getData();
                //1.添加或更新操作票
                for (Map<String, Object> operationTaskModelMap : operationSelfTask) {
                    // 构建t_cruise_task
                    String taskId = operationTaskModelMap.get("task_code").toString();
                    String taskName = operationTaskModelMap.get("task_name").toString();
                    //任务类型5 ： 操作类任务模型（操作票）
                    Map<String, Object> map = new HashMap<>();
                    // 构建t_cruise_point_instance
                    List<TCruisePointInstance> tCruisePointInstanceList = buildTCruisePointInstance(operationTaskModelMap, robotId);
                    List<Long> instanceList = new ArrayList<>();
                    tCruisePointInstanceList.forEach(tCruisePointInstance -> instanceList.add(tCruisePointInstance.getInstanceId()));
                    if (instanceList.size() > 0) {
                        String simulationSteps = operationTaskModelMap.get("simulation_steps").toString();//操作票初始状态
                        map.put("type", 456);
                        map.put("instanceList", instanceList);
                        map.put("planName", taskName); //操作票名称
                        map.put("planCode", taskId); //操作票编码
                        map.put("robotId", robotId); //机器人id
                        map.put("upRegionId", upRegionId); //机器人所在区域
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
            log.error("addRobotSelfTask error", e);
        }
    }

    /**
     * 构建t_cruise_point_instance
     */
    private List<TCruisePointInstance> buildTCruisePointInstance(Map<String, Object> taskModelMap, Long robotId) {
        //操作机器人拓展协议 添加设备属性列表--处理操作测点行为
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
        log.info("机器人对应点list==={}", inspectionIdList);
        List<TCruisePointInstance> tCruisePointInstanceList = new ArrayList<>();
        for (String inspectionCode : inspectionIdList) {
            TCruisePointInstance tCruisePointInstance = new TCruisePointInstance();
            TStdRegion tStdRegion = tRobotInfoDao.selectTSRegionForStation(robotId);
            tCruisePointInstance.setStationId(tStdRegion.getRegionId().toString());
            tCruisePointInstance.setStationName(tStdRegion.getRegionName());
            tCruisePointInstance.setCruiseType(228);
            Map<String, Object> map = tRobotInspectionDao.selectInspection(inspectionCode);
            tCruisePointInstance.setCruiseId(Long.valueOf(map.get("inspection_id").toString()));
            tCruisePointInstance.setCruiseName(map.get("inspection_name").toString());
            tCruisePointInstance.setIfSy(1);
            tCruisePointInstance.setTextDesc(inspectionIdAttrMap.get(inspectionCode));
            tCruisePointInstanceList.add(tCruisePointInstance);
        }
        log.info("tCruisePointInstanceList==={}", tCruisePointInstanceList);
        tRobotInfoDao.batchInsertInstance(tCruisePointInstanceList);
        return tCruisePointInstanceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOperationStepsForRedis(XMLBaseModel xmlBaseModel) {
        Map<String, Object> operationStepsMaps = new HashMap<>(11);
        String robotCode = xmlBaseModel.getSendCode();
        try {
            String taskId = xmlBaseModel.getItems().get(0).get("task_code").toString();
            operationStepsMaps.put("robotCode", robotCode);
            operationStepsMaps.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
            operationStepsMaps.put("taskId", taskId);
            operationStepsMaps.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
            //当前操作测点id
            String deviceId = xmlBaseModel.getItems().get(0).get("device_id").toString();
            operationStepsMaps.put("deviceId", deviceId);
            //当前操作点对应相机路径
            List<String> cameraUrlList = tRobotInfoDao.selectCameraUrlByDeviceId(deviceId);
            operationStepsMaps.put("cameraUrlList", String.valueOf(cameraUrlList));
            //当前操作名称
            operationStepsMaps.put("currentOperation", xmlBaseModel.getItems().get(0).get("current_operation").toString());
            //上一步操作名称
            operationStepsMaps.put("lastOperation", xmlBaseModel.getItems().get(0).get("last_operation").toString());
            //下一步操作名称
            operationStepsMaps.put("nextOperation", xmlBaseModel.getItems().get(0).get("next_operation").toString());
            //操作任务步骤名称
            Map<String, String> map = redisTemplate.opsForHash().entries("RobotOperationSteps:" + robotCode + ":" + taskId);
            List<String> nowStepNameList = new ArrayList<>();
            if (Objects.nonNull(map.get("stepName"))) {
                String stepNameStringList = map.get("stepName");
                stepNameStringList = stepNameStringList.replaceAll("\\[", "").replaceAll("]", "");
                String[] stepNameArray = stepNameStringList.split(", ");
                nowStepNameList = new ArrayList<>(Arrays.asList(stepNameArray));
            }
            nowStepNameList.add(xmlBaseModel.getItems().get(0).get("step_name").toString());
            operationStepsMaps.put("stepName", String.valueOf(nowStepNameList));
            //机器人步骤状态
            operationStepsMaps.put("robotStepStatus", xmlBaseModel.getItems().get(0).get("robot_step_status").toString());
            //操作进度
            operationStepsMaps.put("process", xmlBaseModel.getItems().get(0).get("process").toString());
            //机器人确认消息入redis
            redisTemplate.opsForHash().putAll("RobotOperationSteps:" + robotCode + ":" + taskId, operationStepsMaps);
        } catch (Exception e) {
            log.error("机器人步骤消息错误！", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConfirmMsgForRedis(XMLBaseModel xmlBaseModel) throws Exception {
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, Object> jasonMaps = new HashMap<>();
        String robotCode = xmlBaseModel.getSendCode();
        String taskId = xmlBaseModel.getItems().get(0).get("task_code").toString();
        jasonMaps.put("type", "confirmMsg");
        jasonMaps.put("robotCode", robotCode);
        jasonMaps.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
        jasonMaps.put("taskId", taskId);
        //当前操作测点id
        String deviceId = xmlBaseModel.getItems().get(0).get("device_id").toString();
        jasonMaps.put("deviceId", deviceId);
        //点位类型名称
        jasonMaps.put("deviceTypeName", tRobotInfoDao.selectDeviceTypeName(deviceId));
        jasonMaps.put("content", xmlBaseModel.getItems().get(0).get("content"));
        String confirmPath = filePathMap.get("content") + "/" + xmlBaseModel.getItems().get(0).get("file_path").toString();
        String originalPath = "";
        if (xmlBaseModel.getItems().get(0).containsKey("original_file_path")) {
            originalPath = filePathMap.get("content") + "/" + xmlBaseModel.getItems().get(0).get("original_file_path").toString();
        }
        log.info("确认图片路径为：" + confirmPath);
        log.info("确认原始图片路径为：" + originalPath);
        String developMap = absoluteImgMap.get("content") + "/CFM/" + taskId;
        /*
         * 将ftp图copy到开发环境
         * */
        String splitArray[] = confirmPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        copyFileToDevelop(confirmPath, developMap);
        String confirmUrl = relativeImgMap.get("content") + "/CFM/" + taskId + "/" + fileName;
        String originalFileUrl = "";
        if (org.apache.commons.lang.StringUtils.isNotEmpty(originalPath)) {
            String splitArrayOriginalPath[] = originalPath.split("/");
            String originalPathName = splitArrayOriginalPath[splitArrayOriginalPath.length - 1];
            copyFileToDevelop(originalPath, developMap);
            originalFileUrl = relativeImgMap.get("content") + "/CFM/" + taskId + "/" + originalPathName;
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
        //机器人确认消息入redis
        redisTemplate.opsForHash().putAll("RobotConfirmMsg:" + robotCode + ":" + taskId, jasonMaps);
        //发给前端
        jasonMaps.put("confirmMapList", confirmMapList);
        //webSocket通知前端确认消息
        String jsons = JSON.toJSONString(jasonMaps);
        log.info("确认消息生成-前端推送：" + jsons);
        Constant.postUrl(webSocketUrl, jsons);
    }

    /**
     * 机器人巡检点告警及分析机器人巡检点结果后的告警
     *
     * @param warnInfo 告警信息
     * @return int
     */
    public int insertWarn(TWarnInfo warnInfo) {
        return tRobotInfoDao.insertWarn(warnInfo);
    }

    /**
     * 根据任务id和巡检点id查询告警id
     *
     * @param taskId     任务id
     * @param instanceId 巡检点id
     * @return Long
     */
    public Long selectWarnId(String taskId, Long instanceId) {
        return tRobotInfoDao.selectWarnId(taskId, instanceId);
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
        int isWarnFlag = tRobotInfoDao.selectIsWarn(instanceId, taskId);
        if (isWarnFlag > 0) {
            tRobotInfoDao.updateIsWarn(cruiseResultId);
            return 1;
        }
        return 0;
    }

    /**
     * 查询当前任务的告警数
     *
     * @param taskId 任务id
     * @return int
     */
    public int selectAlarmNumByTaskId(String taskId) {
        return tRobotInfoDao.selectAlarmNumByTaskId(taskId);
    }

    /**
     * 根据巡视点id查询该测点信息
     *
     * @param instanceId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId) {
        return tRobotInfoDao.selectDeviceMeteInfo(instanceId);
    }

    /**
     * 更新异常点位数量
     *
     * @param updateResult
     * @return int
     */
    public int updateTCruiseTaskResult(TCruiseTaskResult updateResult) {
        return tRobotInfoDao.updateTCruiseTaskResult(updateResult);
    }

    /**
     * 下发获得机器人控制权及任务模式切换的指令
     *
     * @param robotCode 机器人唯一标识
     * @param type      类型
     * @param command   命令
     * @param value     值
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

        TRobotInfo tRobotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(Constant.sendCode)
                .setReceiveCode(robotCode)
                .setCode(String.valueOf(tRobotInfo.getRobotNum()))
                .setTime(DateTimeUtil.format(new Date()))
                .setType(type)
                .setCommand(command)
                .setItems(item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的机器人控制xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
        TimeUnit.MILLISECONDS.sleep(2000);

//        return RobotServerHandler.getRobotResultMap().get("Code").toString();
        return "200";
    }

    /**
     * 判断任务是否属于机器人本体任务
     *
     * @param taskId 任务id
     * @return Long
     */
    public Long selectIsRobotTask(String taskId) {
        return tRobotInfoDao.selectIsRobotTask(taskId);
    }

    /**
     * 字典值查询
     *
     * @param dictNote 说明
     * @param colName  类型
     * @return String
     */
    public String selectDictCodeByNote(String dictNote, String colName) {
        return tRobotInfoDao.selectDictCodeByNote(dictNote, colName);
    }

    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     *
     * @param taskId 任务id
     * @return Long
     */
    public List<Long> selectInstanceForTaskGoOn(String taskId) {
        return tRobotInfoDao.selectInstanceForTaskGoOn(taskId);
    }

    public void uploadFile(String localPath, String targetName) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if ("".equals(localPath)) {
                        return;
                    }
                    Map<String, String> upSystemFtps = redisTemplate.opsForHash().entries("upSystemFtps");
                    String ftpsLocalPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
                    String upSystemFtpsIp = upSystemFtps.get("upSystemFtpsIp");
                    String upSystemFtpsPort = upSystemFtps.get("upSystemFtpsPort");
                    String upSystemFtpsUsername = upSystemFtps.get("upSystemFtpsUsername");
                    String upSystemFtpsPassword = upSystemFtps.get("upSystemFtpsPassword");
                    FtpsUtil.putFile(ftpsLocalPath + "/" + localPath, targetName, upSystemFtpsIp, Integer.parseInt(upSystemFtpsPort), upSystemFtpsUsername, upSystemFtpsPassword);
                } catch (Exception e) {
                    log.error("上传至ftps错误 " + e);
                }
            }
        };
        TaskExecutePool.getInstance().execute(runnable);
    }

    /**
     * 离线情况同步机器人模型信息-上传文件
     *
     * @param file      文件
     * @param robotCode 机器人唯一标识
     * @return Result
     */
    public Result upLoadRobotModel(MultipartFile file, String robotCode) {
        Result result = new Result();
        String pathName = null;
        try {
            if (file == null) {
                result.setCode(209, "文件错误，文件为null");
                result.setData(0);
                return result;
            }

            if (!FileUtil.checkFileName(file.getOriginalFilename(), "xml")) {
                result.setCode(209, "请上传指定的文件");
                result.setData(0);
                return result;
            }
            String fileName = "copy-robotModel.xml";
            pathName = excelDataImport(file, fileName);
            // 解析xml文件
            XMLBaseModel robotModel = getXmlMessage(pathName);
            List<Map<String, Object>> robotMap = robotModel.getItems();
            if (CollectionUtils.isEmpty(robotMap)) {
                log.error("文件未解析到正确内容，请确认文件内容是否正确");
                result.setCode(209, "请上传指定格式及模版的文件");
                result.setData(0);
                return result;
            }
            Long robotId = selectRobotIdByCode(robotCode);
            // 机器人模型信息入库
            addRobotModel(robotMap, robotId);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            result.setCode(209, "请上传指定格式及模版的文件");
            result.setData(0);
            return result;
        } finally {
            deleteTmpFile(pathName);
        }
        return result;
    }

    /**
     * 离线情况同步机器人设备文件-上传文件
     *
     * @param file      文件
     * @param robotCode 机器人唯一标识
     * @return Result
     */
    public Result upLoadRobotDevice(MultipartFile file, String robotCode) {
        Result result = new Result();
        String pathName = null;
        try {
            if (file == null) {
                result.setCode(209, "文件错误，文件为null");
                result.setData(0);
                return result;
            }
            if (!FileUtil.checkFileName(file.getOriginalFilename(), "xml")) {
                result.setCode(209, "请上传指定格式的文件");
                result.setData(0);
                return result;
            }
            String fileName = "copy-robotDevice.xml";
            pathName = excelDataImport(file, fileName);
            // 解析xml文件
            XMLBaseModel robotDevice = getXmlMessage(pathName);
            List<Map<String, Object>> deviceMap = robotDevice.getItems();
            if (CollectionUtils.isEmpty(deviceMap)) {
                log.error("文件未解析到正确内容，请确认文件内容是否正确");
                result.setCode(209, "请上传指定格式及模版的文件");
                result.setData(0);
                return result;
            }
            Long robotId = selectRobotIdByCode(robotCode);
            // 机器人设备点位入库
            addDevicePoint(deviceMap, robotId);
            addDevicePointRegion(deviceMap, robotId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(209, "请上传指定格式及模版的文件");
            result.setData(0);
            return result;
        } finally {
            deleteTmpFile(pathName);
        }
        return result;
    }

    public void deleteTmpFile(String pathName) {
        if (StringUtils.isNotEmpty(pathName)) {
            boolean clear = Boolean.parseBoolean((String) redisTemplate.opsForHash().get("t_sys_param:tempReflectClear", "content"));
            if (clear) {
                FileUtils.deleteQuietly(new File(pathName));
            }
        }
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
            log.error(e.getMessage(), e);
        }
        return path + "/" + fileName;
    }

    /**
     * 判断站端的任务是否在巡视主机上已完成
     *
     * @param robotCode 机器人唯一标识
     * @return String
     */
    public List<String> hasStandTaskIsFinish(String robotCode) {
        Long robotId = selectRobotIdByCode(robotCode);
        List<String> taskIdList = tRobotInfoDao.HasStandTaskIsFinish(robotId);
        log.info("站端在巡视主机显示未完成的任务=={}", taskIdList);
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            for (String taskId : taskIdList) {
                Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
                Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());
                try {
                    if (1 == taskState) {
                        tRobotInfoDao.updateStandTaskStatus(taskId, 240);
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
            tRobotInfoDao.deleteInstanceId(instanceId);
        }
        return 1;
    }

    /**
     * 查询机器人状态
     */
    @Transactional(rollbackFor = Exception.class)
    public String queryRobotStatus(String robotCode) {
        log.info("查询机器人状态:robotCode{}====", robotCode);
        Map<Object, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
        if (null != mapForRobotState && mapForRobotState.size() > 0) {
            return mapForRobotState.getOrDefault("value", "1").toString();
        }
        return OFF_LINE;
    }

    /**
     * 智能环境设备控制指令
     */
    public Result robotControl(HashMap<String, String> map) {
        log.info("下发智能环境设备控制指令,map===" + map);

        Result result = new Result();
        try {
            String robotCode = map.get("robotCode");
            log.info("sendCode:{},robotCode:{}====", Constant.sendCode, robotCode);
            if (StringUtils.isEmpty(robotCode)) {
                log.error("当前不存在环控设备编码,没有成功将控制指令下发到环控设备....");
                result.setMessage(200, "当前不存在环控设备编码,没有成功将控制指令下发到环控设备....");
            }
            Map<Object, Object> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
            Object robotStatus = mapForRobotState.get("value");
            log.info("robotStatus====" + robotStatus);
            if (Optional.ofNullable(robotStatus).isPresent()) {
                if ("1".equals(robotStatus)) {
                    log.error("该环控设备处于离线状态,没有成功将控制指令下发到环控设备......");
                    result.setMessage(200, "该环控设备处于离线状态,没有成功将控制指令下发到环控设备......");
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
                            .setSendCode(Constant.sendCode)
                            .setReceiveCode(robotCode)
                            .setType(type)
                            .setCode(deviceId)
                            .setTime(DateTimeUtil.getDateTimeString(new Date(), false))
                            .setCommand(cmd)
                            .setItems(Item);
                    String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
                    log.info("生成的机器人控制xml是<start>" + xmlString + "<end>");
                    RobotServerHandler.send(generateByteOrder(xmlString, robotCode), robotCode);
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
                String robotCode = json.getString("robotCode");
                //查询区域Id
                String regionId = tRobotInfoDao.selectRegionIdByrobotId(robotCode);
                JSONArray envDeviceStatusList = json.getJSONArray("envDeviceStatusList");
                redisTemplate.opsForHash().put("Weather", regionId, JSONArray.toJSONString(envDeviceStatusList));
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
                tRobotInfoDao.insertEnv(envWarn);
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
        tRobotInfoDao.updateTCruiseTask(taskMap);
    }

    public void lowTaskGoOn(String taskId) {
        String lowTaskKey = "lowTask:" + taskId;
        List<String> lowTaskList = redisTemplate.opsForList().range(lowTaskKey, 0, -1);
//        List<String> lowTaskList = new ArrayList<>();
//        lowTaskList.add("111");
//        lowTaskList.add("222");

        if (lowTaskList != null && lowTaskList.size() > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("lowTaskIdList", lowTaskList);
            Constant.otherServerList(lowTaskList, Constant.GET_LOW_TASK_GO_ON);
        }
    }

    public Integer selectRobotType(String robotCode) {
        return tRobotInfoDao.selectRobotType(robotCode);
    }

    public Integer selectIsAlarmByTask(String taskId, String instanceId) {
        return tRobotInfoDao.selectIsAlarmByTask(taskId, Long.valueOf(instanceId));
    }

    public Integer updatePicPath(String taskId, String instanceId, String imagePath) {
        return tRobotInfoDao.updatePicPath(taskId, Long.valueOf(instanceId), imagePath);
    }

    public void syncModelUpdate(String type, String filePath, String edgeCode) {
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        Map<String, String> mapForPreset = redisTemplate.opsForHash().entries("t_sys_param:presetImgPath");
        Map<String, String> mapForPresetReal = redisTemplate.opsForHash().entries("t_sys_param:presetRealImgPath");
        String edgeLevel = (String)redisTemplate.opsForHash().get("t_sys_param:edgeLevel","content");
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
            filePathMap.put("content", "C:\\robotData\\Model");
        }
        switch (type) {
            case "1":
                log.info("设备点位模型 {}", filePath);
                dealDevicePointModel(filePathMap.get("content") + File.separator + filePath, filePathMap.get("content"),
                        mapForPreset.get("content"), mapForPresetReal.get("content"), edgeCode, edgeLevel);
                break;
            case "2":
                log.info("边缘节点模型 {}", filePath);
                dealHostFilePath(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            case "3":
                log.info("机器人模型 {}", filePath);
                dealRobotFile(filePathMap.get("content") + File.separator + filePath,edgeCode, Constant.ROBOT);
                break;
            case "4":
                log.info("摄像机模型 {}", filePath);
                dealCameraFile(filePathMap.get("content") + File.separator + filePath,edgeCode);
                break;
            case "5":
                log.info("无人机模型 {}", filePath);
                dealRobotFile(filePathMap.get("content") + File.separator + filePath,edgeCode, Constant.DRONE);
                break;
            case "6":
                log.info("声纹模型 {}", filePath);
                dealVoiceFile(filePathMap.get("content") + File.separator + filePath,edgeCode);
                break;
            case "8":
                log.info("检修区域配置模型 {}", filePath);
                dealMaintenanceFilePath(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            case "9":
                log.info("地图文件 {}", filePath);
                dealMapFile(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            case "10":
                log.info("设备资源信息配置文件 {}", filePath);
                dealSourceFile(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            case "1001":
                log.info("区域文件模型 {}", filePath);
                dealRegionFile(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            case "1002":
                log.info("录像机文件模型 {}", filePath);
                dealRecordFile(filePathMap.get("content") + File.separator + filePath, edgeCode);
                break;
            default:
                break;
        }
    }

    /**
     * 处理检修区域文件
     * @param filePath
     * @param edgeCode
     */
    public void dealMaintenanceFilePath(String filePath, String edgeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> list = model.getItems();
            List<TDeviceMaintenanceModel> deviceMaintenanceModelList = list.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, TDeviceMaintenanceModel.class)).collect(Collectors.toList());
            tDeviceMaintenanceService.saveReportData(deviceMaintenanceModelList, edgeCode);
            log.info("节点 {} 的检修区域模型解析完成", edgeCode);
        } catch (Exception e) {
            log.error("检修区域模型处理失败", e);
        }
    }


    /**
     * 处理节点文件
     * @param filePath
     * @param nodeCode
     */
    private void dealHostFilePath(String filePath, String nodeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel propertyModel = getXmlMessage(filePath);
            Map<String, Object> hostMap = propertyModel.getItems().get(0);
            //放到缓存里
            redisTemplate.opsForHash().putAll("edge:" + nodeCode, hostMap);
        } catch (Exception e) {
            log.info("解析节点文件失败：", e);
        }
    }

    /**
     * 处理地图文件
     *
     * @param picPath 地图文件路径
     */
    private void dealMapFile(String picPath, String edgeCode) {
        Map<String, String> absoluteImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageAbsolute");
        Map<String, String> relativeImgMap = redisTemplate.opsForHash().entries("t_sys_param:ftpImageRelative");
        String[] splitArray = picPath.split("/");
        String fileName = splitArray[splitArray.length - 1];
        String developMap = absoluteImgMap.get("content") + "/Map";
        copyFileToDevelop(picPath, developMap);
        String developRelativeUrl = relativeImgMap.get("content") + "/Map/" + fileName;
        String robotNum = StringUtils.substringBefore(fileName, "_");
        log.info("更新 robotNum 为{}的地图文件", robotNum);
        Long robotId = tRobotInfoDao.selectRobotIdByRobotNum(robotNum, edgeCode);
        if (Objects.nonNull(robotId)) {
            TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
            tRobotInfo.setPhotePath(developRelativeUrl);
            tRobotInfoDao.update(tRobotInfo);
        }
    }

    /**
     * 处理设备资源信息配置文件
     *
     * @param filePath 设备资源信息配置文件路径
     */
    private void dealSourceFile(String filePath, String edgeCode) {
        log.info("处理设备资源信息配置文件：filePath:{}, edgeCode:{}",filePath,edgeCode);
        TStdRegion stdRegion = Optional.of(tStdRegionDao.selectByRegionCodeAndState(edgeCode, Constant.STATE_LOCAL)).map(tStdRegionList1 -> tStdRegionList1.get(0)).orElse(null);
        if (Objects.isNull(stdRegion)) {
            log.error("未配置该边缘节点 edgeCode:{}", edgeCode);
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();
            String data = stringBuilder.toString();
            String regex = "\\#.*?(是|否)";
            Matcher matcher = Pattern.compile(regex).matcher(data);
            List<SYAllInfo> list = new LinkedList<>();
            while (matcher.find()) {
                String str = matcher.group();
                str = str.replaceAll("\\t", " ");

                String[] strArray = str.split("\\s+");
                if ("".equals(strArray[0])) {
                    strArray = Arrays.copyOfRange(strArray, 1, strArray.length);
                }
                //取数据
                SYAllInfo syAllInfo = new SYAllInfo();
                syAllInfo.setStationId(strArray[1]);
                String[] mete = strArray[3].split("/");
                String meteName = "";
                if (mete.length > 1) {
                    meteName = mete[mete.length - 2] + "/" + mete[mete.length - 1] + "-" + strArray[4];
                } else {
                    meteName = mete[mete.length - 1] + "-" + strArray[4];
                }
                syAllInfo.setMeteId(stdRegion.getStationId() + strArray[2]);
                syAllInfo.setMeteName(meteName);
                syAllInfo.setDeviceId(stdRegion.getStationId() + strArray[2]);
                syAllInfo.setEdgeCode(edgeCode);
                syAllInfo.setDeviceName(strArray[3]);
                Integer meteKind = strArray[4].contains("遥信") ? 1 : (strArray[4].contains("遥测") ? 2 : (strArray[4].contains("遥控") ? 3 : (strArray[4].contains("遥调") ? 4 : 5)));
                syAllInfo.setMeteKind(meteKind);
                syAllInfo.setMeteCode(strArray[5]);
                list.add(syAllInfo);
                //list.add(str);
            }
            Constant.otherServerList(list, Constant.SOURCE_FILE_URL);

            //将文件复制到存放设备资源配置的地方
            String sourcePath = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
            String fileName = "source_file_model.cime";
            File dir = new File(sourcePath);

            FileUtil.copyFileUsingStream(filePath,sourcePath+"/"+fileName);
        } catch (Exception e) {
            log.error("设备资源信息配置文件处理失败", e);
        }
    }

    public void dealVoiceFile(String filePath, String edgeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> list = model.getItems();
            List<VoiceDeviceModel> voiceDeviceModelList = list.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, VoiceDeviceModel.class)).collect(Collectors.toList());
            tVoiceDeviceService.saveReportData(voiceDeviceModelList, edgeCode);
            log.info("声纹文件处理结束 edgeCode:{}", edgeCode);
        } catch (Exception e) {
            log.error("设备点位模型处理失败", e);
        }
    }


    /**
     * 处理设备点位模型
     * @param filePath 设备点位模型路径
     * @param edgeCode 边缘节点编码
     */
    @Transactional(rollbackFor = Exception.class)
    public void dealDevicePointModel(String filePath, String ftpsPath, String presetPath, String presetRealPath, String edgeCode, String edgeLevel) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> deviceModelList = model.getItems();
            tStdDeviceModelService.saveReportData(deviceModelList,ftpsPath,presetPath,presetRealPath, edgeCode, edgeLevel);
            log.info("节点 {} 的设备点位模型解析完成", edgeCode);
        } catch (Exception e) {
            log.error("设备点位模型处理失败", e);
        }
    }

    public void dealCameraFile(String filePath, String edgeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> mapList = model.getItems();
            List<CameraModel> cameraModelList = mapList.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, CameraModel.class)).collect(Collectors.toList());
            tCameraInfoService.saveReportData(cameraModelList,edgeCode);
            log.info("摄像机文件处理结束 edgeCode:{}", edgeCode);
        } catch (DocumentException e) {
            log.error("录像机文件处理失败", e);
        }
    }
    public void dealRobotFile(String filePath, String edgeCode,String type) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> mapList = model.getItems();
            List<RobotModel> robotModelList = mapList.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, RobotModel.class)).collect(Collectors.toList());
            tRobotInfoService.saveReportData(robotModelList,edgeCode,type);
            log.info("机器人文件处理结束 edgeCode:{} type:{}", edgeCode,type);
        } catch (Exception e) {
            log.error("机器人/无人机文件处理失败", e);
        }
    }

    public void dealRecordFile(String filePath, String edgeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> mapList = model.getItems();
            List<TCameraRecorder> tCameraRecorderList = mapList.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, TCameraRecorder.class)).collect(Collectors.toList());
            tCameraRecorderService.saveReportData(tCameraRecorderList,edgeCode);
            log.info("录像机文件处理结束 edgeCode:{}", edgeCode);
        } catch (DocumentException e) {
            log.error("录像机文件处理失败", e);
        }
    }

    public void dealRegionFile(String filePath, String edgeCode) {
        if (StringUtils.isBlank(filePath)) {
            log.info("file path is null");
            return;
        }
        try {
            XMLBaseModel model = getXmlMessage(filePath);
            List<Map<String, Object>> mapList = model.getItems();
            List<TStdRegion> tStdRegionList = mapList.stream().map(JSON::toJSONString).map(jsonString -> JSON.parseObject(jsonString, TStdRegion.class)).collect(Collectors.toList());
            tStdRegionService.saveReportData(tStdRegionList, edgeCode);
            log.info("区域处理结束 edgeCode:{}", edgeCode);
            // 刷新缓存
            SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class).getForObject(Constant.REGION_REFRESH_URL, Result.class);
        } catch (Exception e) {
            log.error("区域文件处理失败", e);
        }
    }

    public XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }

    public Boolean selectByRegionCodeAndState(String sendCode, Integer state){
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(sendCode, state);
        return CollectionUtils.isNotEmpty(stdRegionList);
    }

    /**
     * 判断消息发送是直连机器人 还是 下级节点机器人
     * @param xmlBaseModel
     * @param sendCode
     * @return
     */
    public String selectRobotOrEdgeRobot(XMLBaseModel xmlBaseModel, String sendCode){
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(sendCode, 1);
        // 如果边缘节点 code 不为空，则表示底端上传数据的是边缘节点，不是机器人或无人机
        boolean isEdge = CollectionUtils.isNotEmpty(stdRegionList);
        if (isEdge){
            if (xmlBaseModel.getItems().get(0).containsKey("nest_code")){
                String nestNum = xmlBaseModel.getItems().get(0).get("nest_code").toString();
                sendCode = tRobotInfoDao.selectRobotCodeByNestNum(nestNum);
            }else {
                String robotNum = xmlBaseModel.getItems().get(0).get("patroldevice_code").toString();
                String sendCodeItem = tRobotInfoDao.selectRobotInfoByRobotNum(robotNum, sendCode).getRobotCode();
                sendCode = StringUtils.isNotEmpty(sendCodeItem) ? sendCodeItem : sendCode;
            }
        }
        return sendCode;
    }

    public boolean isSubSystem(String robotCode){
        List<TStdRegion>  tStdRegionList=tStdRegionDao.selectByRegionCodeAndState(robotCode,Constant.STATE_LOCAL);
        return CollectionUtils.isNotEmpty(tStdRegionList);
    }

    public int changeStatistic(String deviceCode,String deviceRun,String robotCode) {
        DeviceStatisticInfoResult deviceStatisticInfoResult = deviceStatisticInfoResultDao.select(deviceCode);
        if (deviceStatisticInfoResult != null) {
            deviceStatisticInfoResult.setDeviceRun(deviceRun);
            return    deviceStatisticInfoResultDao.changeRun(deviceStatisticInfoResult);
        }
        else {
            deviceStatisticInfoResult = new DeviceStatisticInfoResult();
            TRobotInfo tRobotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
            deviceStatisticInfoResult.setDeviceCode(tRobotInfo.getRobotNum());
            deviceStatisticInfoResult.setDeviceName(tRobotInfo.getRobotName());
            deviceStatisticInfoResult.setDeviceRun(deviceRun);
            deviceStatisticInfoResult.setDeviceResumeDate(new Date());
            return deviceStatisticInfoResultDao.insertSelective(deviceStatisticInfoResult);
        }
    }

    public void dealStatistic(String sendCode,List<Map<String, Object>> list) {
        Map<String,Object> firstMap = list.get(0);
        StringBuffer stringBuffer = new StringBuffer("statisticForUpSystem:");
        stringBuffer.append(sendCode);
        stringBuffer.append("_");
        stringBuffer.append(firstMap.get("command"));
        if (firstMap.get("type") == null) {
            stringBuffer.append(":total:");
            redisTemplate.opsForHash().putAll(stringBuffer.toString(),firstMap);
        }else {
            stringBuffer.append(firstMap.get("type"));
            stringBuffer.append(":");
            stringBuffer.append(firstMap.get("param_year"));
            stringBuffer.append("_");
            stringBuffer.append(firstMap.get("param_month"));
            stringBuffer.append(":");
            switch (String.valueOf(firstMap.get("type"))) {
                case "1":
                    list.forEach(map -> redisTemplate.opsForHash().putAll(stringBuffer +map.get("day").toString() ,map));
                    break;
                case "2":
                    list.forEach(map -> redisTemplate.opsForHash().putAll(stringBuffer +map.get("week").toString() ,map));
                    break;
                case "3":
                    list.forEach(map -> redisTemplate.opsForHash().putAll(stringBuffer +map.get("month").toString() ,map));
                    break;
            }
        }


    }

    public List<Long> selectInstanceIdByDeviceId(List<String> deviceIds,String taskCode) {
        return tRobotInspectionDao.selectInstanceIdByDeviceId(deviceIds,taskCode);
    }

    /**
     * 查询是否是无人机
     * @return List<String>
     */
    public boolean selectIsDrone(String robotCode) {
        return Objects.nonNull(tRobotInfoDao.selectIsDrone(robotCode));
    }

    public String selectRealTaskId(String taskCode, Date date) {
        return tRobotInfoDao.selectRealTaskIdByTime(taskCode, date);
    }
}

