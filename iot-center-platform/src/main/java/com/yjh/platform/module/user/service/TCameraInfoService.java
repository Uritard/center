package com.yjh.platform.module.user.service;


import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.KeepWatchJob;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.smUtil.ModelDecodeUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.*;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.entity.output.SysUserDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
* @author tt
* @since 2020-07-23
*/
@Service
public class TCameraInfoService {

    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserDevicePermissionDao sysUserDevicePermissionDao;
    @Resource
    private TStdRegionDao tStdRegionDao;

    public static final Long BUSINESS_ROLE_ID = 1235L;
    private Logger log = LoggerFactory.getLogger(TCameraInfoService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraInfo tCameraInfo) {
        //摄像头新增前先新增诊断监测点
//        TCameraRecorderByDict recorder=tCameraRecorderDao.selectByPrimaryId(tCameraInfo.getRecordId());
//        Channel channel=new Channel();
//        try {
//            diagnosePointStandardThreshold(channel);
//            channel.setId(RandomStringUtils.randomAlphanumeric(12));
//            channel.setIp(recorder.getRecordIp());
//            channel.setPort(recorder.getHttpPort().toString());
//            channel.setUserName(recorder.getIdentityManager());
//            channel.setUserPwd(recorder.getIdentityCode());
//            Integer realChannelNum=tCameraInfo.getChannelNum()+32;
//            channel.setChanIndex(realChannelNum.toString());
//            channel.setProtocol("0");
//            switch (tCameraInfo.getIsControl()){
//                case 0:
//                    channel.setDevType("1");
//                    break;
//                case 1:
//                    channel.setDevType("0");
//                    break;
//                default:
//                    break;
//            }
//            channel.setDevBrand("0");
//
//         Constant.otherServerEntity(channel, Constant.DIAGNOSE_CHANNEL_OPERATE);
//
//            log.info("channel:"+channel);
//        }catch (Exception e){
//            log.error("监测点新增失败："+e);
//            return 0;
//        }
        Channel channel=acrossAddMonitor(tCameraInfo);
        tCameraInfo.setMonitorId(channel.getId());
        ModelDecodeUtil.decodeField(tCameraInfo, "cameraCode");
        this.tCameraInfoDao.insert(tCameraInfo);
        //新增机器人时，对所有用户赋予机器人权限
        List<SysUserDTO> sysUserDTOList = sysUserDao.selectUserByRoleId(BUSINESS_ROLE_ID);
        if (!CollectionUtils.isEmpty(sysUserDTOList)) {
            List<SysUserDevicePermissionDO> permissionDOS = new ArrayList<>(sysUserDTOList.size());
            sysUserDTOList.forEach(sysUserDTO -> {
                SysUserDevicePermissionDO sysUserDevicePermissionDO = new SysUserDevicePermissionDO();
                sysUserDevicePermissionDO.setUserId(sysUserDTO.getUserId());
                sysUserDevicePermissionDO.setMonitorDeviceId(tCameraInfo.getCameraId());
                permissionDOS.add(sysUserDevicePermissionDO);
            });
            sysUserDevicePermissionDao.batchInsert(permissionDOS);
        }
        return this.intoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cameraId) {
        //判断相机下是否有预置位
        {
            List<Long> list = tCameraInfoDao.selectHavePreset(cameraId);
            if(list != null && list.size()>0){
                return -1;
            }
        }
        Map<String,String> channelMap=new HashMap<>();
        channelMap.put("channelId", tCameraInfoDao.selectMonitorId(cameraId));
        if(Objects.nonNull(channelMap.get("channelId"))) {
            try {
                Constant.crossServerDelete(Constant.DIAGNOSE_CHANNEL_DELETE, channelMap);
            }catch (Exception e){
                log.error("调用vqd服务异常", e);
                // return -2;
            }
        }
        this.tCameraInfoDao.deleteByPrimaryId(cameraId);
        return this.intoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedCamera(String cameraIds) {
        List<String> list = Arrays.asList(cameraIds.split(","));
        for(String item:list){
            List<Long> haveList = tCameraInfoDao.selectHavePreset(Long.valueOf(item));
            if(haveList != null && haveList.size()>0){
                return -1;
            }

            Map<String,String> channelMap=new HashMap<>();
            channelMap.put("channelId", tCameraInfoDao.selectMonitorId(Long.valueOf(item)));
            if(Objects.nonNull(channelMap.get("channelId"))) {
                try {
                    Constant.crossServerDelete(Constant.DIAGNOSE_CHANNEL_DELETE, channelMap);
                }catch (Exception e){
                    log.error("调用vqd服务异常", e);
                    // return -2;
                }
            }
        }
        int i = this.tCameraInfoDao.deleteSelectedCamera(list);
        this.intoRedis();
        return i;
    }



    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraInfo tCameraInfo) {
        TCameraRecorderByDict recorder=tCameraRecorderDao.selectByPrimaryId(tCameraInfo.getRecordId());
        try {
            log.info("channelId----------:"+tCameraInfo.getMonitorId());
            Result result=Constant.otherServerGet(tCameraInfo.getMonitorId(),Constant.DIAGNOSE_CHANNEL_GET);  //获取已存在的监测点信息
            log.info("result------------:"+result.getMessage());
            Map<String,String> channels=(Map<String,String>)result.getData();
            log.info("channels-----"+channels);

            if(Objects.nonNull(channels.get("id"))) {     //该相机监测点已存在时：--修改监测点
                Channel channel = new Channel();
                channel.setId(channels.get("id"));
                channel.setCheckFlag(channels.get("checkFlag"));
                channel.setSignalPoint(channels.get("signalPoint"));
                channel.setBlurPoint(channels.get("blurPoint"));
                channel.setContrastPoint(channels.get("contrastPoint"));
                channel.setBrightPoint(channels.get("brightPoint"));
                channel.setDarkPoint(channels.get("darkPoint"));
                channel.setChromaPoint(channels.get("chromaPoint"));
                channel.setMonoPoint(channels.get("monoPoint"));
                channel.setNoisePoint(channels.get("noisePoint"));
                channel.setStreakPoint(channels.get("streakPoint"));
                channel.setFreezePoint(channels.get("freezePoint"));
                channel.setShakePoint(channels.get("shakePoint"));
                channel.setFlashPoint(channels.get("flashPoint"));
                channel.setScenePoint(channels.get("scenePoint"));
                channel.setCoverPoint(channels.get("coverPoint"));
                channel.setPtzPoint(channels.get("ptzPoint"));
                channel.setStreamType(channels.get("streamType"));
                channel.setProtocol(channels.get("protocol"));
                channel.setDevType(channels.get("devType"));
                channel.setDevBrand(channels.get("devBrand"));
                channel.setIp(recorder.getRecordIp());
                channel.setPort(recorder.getHttpPort().toString());
                channel.setUserName(recorder.getIdentityManager());
                channel.setUserPwd(recorder.getIdentityCode());
                Integer realChannelNum = tCameraInfo.getChannelNum() /*+ 32*/;
                channel.setChanIndex(realChannelNum.toString());


                Result result1 = Constant.otherServerEntity(channel, Constant.DIAGNOSE_CHANNEL_OPERATE);
                log.info("re========:" + result1.getData());
            }else {   //相机监测点不存在时（相机监测点新增失败）：--新增监测点
                Channel channel=acrossAddMonitor(tCameraInfo);
                tCameraInfo.setMonitorId(channel.getId());
            }
        }catch (Exception e){
            log.error("监测点修改失败：", e);
            return 0;
        }finally {
            ModelDecodeUtil.decodeField(tCameraInfo, "cameraCode");
            return this.tCameraInfoDao.update(tCameraInfo);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public TCameraInfoByDict selectByPrimaryId(Long cameraId) {
        return this.tCameraInfoDao.selectByPrimaryId(cameraId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByRegionId(Long regionId) {
        return tCameraInfoDao.selectByRegionId(regionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByCameraName(String cameraName) {
        return tCameraInfoDao.selectByCameraName(cameraName);
    }

    public List<TCameraInfo> select(Long cameraId, String cameraName, Integer cameraModel, String pmsId, String aliasName, String recordId,
                                    Long upRegionId, Integer streamType, Integer protocolType, String cameraIp, String url, Integer cameraType, Integer isControl) {
        return tCameraInfoDao.select(cameraId, cameraName, cameraModel, pmsId, aliasName, recordId, upRegionId, streamType, protocolType,
                cameraIp, url, cameraType, isControl);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByPage(Integer cameraType,String aliasName,String unit,String address,String cameraVendor,
                                                Integer cameraModel,String cameraName,List<Long> regionIdList) {
        List<TCameraInfoByDict> tCameraInfoByDict = tCameraInfoDao.selectByPage(cameraType,aliasName,unit,address,cameraVendor,cameraModel,cameraName,regionIdList);

        Map<String,String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            if (Objects.nonNull(re)){
                map.putAll((Map<String,String>)re.getData());
            }
        }
        for (TCameraInfoByDict xi : tCameraInfoByDict){
            if (map.containsKey(xi.getCameraId().toString())){
                if ("0".equals(map.get(xi.getCameraId().toString()))){
                    xi.setCameraStatus("离线");
                }else{
                    xi.setCameraStatus("在线");
                }
            }else{
                xi.setCameraStatus("未知");
            }
        }
        return tCameraInfoByDict;
    }
    private static Result cameraStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }
    @Transactional(rollbackFor = Exception.class)
    public boolean synchronizeFromPMS(String pmsId) throws DocumentException {
        Long cameraId = tCameraInfoDao.selectCameraIdByPmsId(pmsId);

        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String filePathAndName = resMap.get("content") +  "/PMS/CameraPMS.xml";
//        String filePathAndName = "D:/testform/PMS/CameraPMS.xml";
        log.info("路径是==="+filePathAndName);

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        //解析xml
        Element rootElement = document.getRootElement();

        Iterator iterator = rootElement.elementIterator();
        Map<String,Object> map = new HashMap<>();
        try {
            while (iterator.hasNext()) {
                Element stu = (Element) iterator.next();
                List<Attribute> attributes = stu.attributes();

                for (Attribute attribute : attributes) {
                    if (pmsId.equals(attribute.getValue())) {
                        Iterator iterator1 = stu.elementIterator();
                        while (iterator1.hasNext()) {
                            Element stuChild = (Element) iterator1.next();
                            map.put(stuChild.getName(), stuChild.getStringValue());
                        }
                        TCameraInfo tCameraInfo = new TCameraInfo()
                                .setCameraId(cameraId);
                        if (map.containsKey("cameraName") && (!"".equals(map.get("cameraName")))) {
                            tCameraInfo.setCameraName(map.get("cameraName").toString());
                        }
                        if (map.containsKey("cameraModel") && (!"".equals(map.get("cameraModel")))) {
                            Integer cameraModel = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_model",map.get("cameraModel").toString()));
                            tCameraInfo.setCameraModel(cameraModel);
                        }
                        if (map.containsKey("aliasName") && (!"".equals(map.get("aliasName")))) {
                            tCameraInfo.setAliasName(map.get("aliasName").toString());
                        }
                        if (map.containsKey("recordId") && (!"".equals(map.get("recordId")))){
                            tCameraInfo.setRecordId(Long.valueOf(map.get("recordId").toString()));
                        }
                        if (map.containsKey("upRegionId") && (!"".equals(map.get("upRegionId")))){
                            tCameraInfo.setUpRegionId(Long.valueOf(map.get("upRegionId").toString()));
                        }
                        if (map.containsKey("channelNum") && (!"".equals(map.get("channelNum")))) {
                            tCameraInfo.setChannelNum(Integer.valueOf(map.get("channelNum").toString()));
                        }
                        if (map.containsKey("cameraNum") && (!"".equals(map.get("cameraNum")))) {
                            tCameraInfo.setCameraNum(Integer.valueOf(map.get("cameraNum").toString()));
                        }
                        if (map.containsKey("smsId") && (!"".equals(map.get("smsId")))) {
                            tCameraInfo.setSmsId(Integer.valueOf(map.get("smsId").toString()));
                        }
                        if (map.containsKey("rmsId") && (!"".equals(map.get("rmsId")))) {
                            tCameraInfo.setRmsId(Integer.valueOf(map.get("rmsId").toString()));
                        }
                        if (map.containsKey("monitorId") && (!"".equals(map.get("monitorId")))) {
                            tCameraInfo.setMonitorId(map.get("monitorId").toString());
                        }
                        if (map.containsKey("vendorId") && (!"".equals(map.get("vendorId")))) {
                            Integer vendorId = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_vendor",map.get("vendorId").toString()));
                            tCameraInfo.setVendorId(vendorId);
                        }
                        if (map.containsKey("streamType") && (!"".equals(map.get("streamType")))) {
                            tCameraInfo.setStreamType(Integer.valueOf(map.get("streamType").toString()));
                        }
                        if (map.containsKey("protocolType") && (!"".equals(map.get("protocolType")))) {
                            tCameraInfo.setProtocolType(Integer.valueOf(map.get("protocolType").toString()));
                        }
                        if (map.containsKey("url") && (!"".equals(map.get("url")))) {
                            tCameraInfo.setUrl(map.get("url").toString());
                        }
                        if (map.containsKey("cameraIp") && (!"".equals(map.get("cameraIp")))) {
                            tCameraInfo.setCameraIp(map.get("cameraIp").toString());
                        }
                        if (map.containsKey("port") && (!"".equals(map.get("port")))) {
                            tCameraInfo.setPort(Integer.valueOf(map.get("port").toString()));
                        }
                        if (map.containsKey("infreadPort") && (!"".equals(map.get("infreadPort")))) {
                            tCameraInfo.setInfreadPort(Integer.valueOf(map.get("infreadPort").toString()));
                        }
                        if (map.containsKey("cameraManager") && (!"".equals(map.get("cameraManager")))) {
                            tCameraInfo.setCameraManager(map.get("cameraManager").toString());
                        }
                        if (map.containsKey("cameraCode") && (!"".equals(map.get("cameraCode")))) {
                            tCameraInfo.setCameraCode(map.get("cameraCode").toString());
                        }
                        if (map.containsKey("cameraType") && (!"".equals(map.get("cameraType")))) {
                            Integer cameraType = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_type",map.get("cameraType").toString()));
                            tCameraInfo.setCameraType(cameraType);
                        }
                        if (map.containsKey("isControl") && (!"".equals(map.get("isControl")))) {
                            tCameraInfo.setIsControl(Integer.valueOf(map.get("isControl").toString()));
                        }
                        if (map.containsKey("latitude") && (!"".equals(map.get("latitude")))) {
                            tCameraInfo.setLatitude(map.get("latitude").toString());
                        }
                        if (map.containsKey("longitude") && (!"".equals(map.get("longitude")))) {
                            tCameraInfo.setLongitude(map.get("longitude").toString());
                        }
                        if (map.containsKey("address") && (!"".equals(map.get("address")))) {
                            tCameraInfo.setAddress(map.get("address").toString());
                        }
                        if (map.containsKey("unit") && (!"".equals(map.get("unit")))){
                            tCameraInfo.setUnit(map.get("unit").toString());
                        }
                        log.info("tCameraInfo==="+tCameraInfo);
                        tCameraInfoDao.update(tCameraInfo);
//                    return true;
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<CameraInfo> selectCameraByTaskId(Long taskId) {
        return this.tCameraInfoDao.selectCameraByTaskId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> selectPresetTree() {
        List<TCamreaPresetTree> cameraList = tCameraInfoDao.selectCameraId();
        List<TCamreaPresetTree> tCamreaPresetTreeList = tCameraInfoDao.batchSelectPreset();
        List<Map<String, Object>> cameraPresetTreeTemList = new ArrayList<>();
        for (TCamreaPresetTree tCamrea : cameraList) {
            Map<String, Object> cameraPresetTreeTem = new HashMap<>();
            List<Map<String, Object>> presetList = new ArrayList<>();
            Long cameraId = tCamrea.getCameraId();
            for (TCamreaPresetTree tCamreaPresetTree : tCamreaPresetTreeList) {
                Long cameraIdTem = tCamreaPresetTree.getCameraId();
                if (Objects.nonNull(cameraIdTem) && Objects.equals(cameraId, cameraIdTem)) {
                    Map<String, Object> presetMap = new HashMap<>();
                    presetMap.put("id", tCamreaPresetTree.getPresetId());
                    presetMap.put("label", tCamreaPresetTree.getPresetName());
                    presetMap.put("infoType", "preset");
                    presetMap.put("upId", cameraId);
                    presetMap.put("cameraType", tCamreaPresetTree.getCameraType());
                    presetList.add(presetMap);
                }
            }
            cameraPresetTreeTem.put("id", cameraId);
            cameraPresetTreeTem.put("label", tCamrea.getCameraName());
            cameraPresetTreeTem.put("infoType", "camera");
            cameraPresetTreeTem.put("upId", "-1");
            cameraPresetTreeTem.put("children", presetList);
            if(presetList.size() ==0  ){
                continue;
            }
            cameraPresetTreeTemList.add(cameraPresetTreeTem);
        }
        Map<String, Object> cameraPresetTree = new HashMap<>();
        cameraPresetTree.put("children", cameraPresetTreeTemList);
        cameraPresetTree.put("label", "摄像机预置位树");
        cameraPresetTree.put("id", "-1");
        cameraPresetTree.put("upId", "null");
        cameraPresetTree.put("infoType", "tree");
        List<Map<String, Object>> cameraPresetList = new ArrayList<>();
        cameraPresetList.add(cameraPresetTree);
        return cameraPresetList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectCameraByRecord(Long recordId){
        return tCameraInfoDao.selectCameraByRecord(recordId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int intoRedis() {
        List<TCameraInfo> list = tCameraInfoDao.select(null, null, null, null, null, null, null, null, null, null, null, null, null);
        for (TCameraInfo item : list) {
            Long cameraId = item.getCameraId();
            String str = "camera_info:" + cameraId;
            Map<String, String> map = redisTemplate.opsForHash().entries(str);
            if (MapUtils.isEmpty(map)) {
                map = new HashMap<>(8);
            }
            if (StringUtils.isEmpty(map.get("state"))) {
                map.put("state", "0");
            }
            map.put("cameraId", String.valueOf(cameraId));
            map.put("cameraIp", item.getCameraIp());
            map.put("pmsId", item.getPmsId());
            redisTemplate.opsForHash().putAll(str, map);
        }
        return 1;
    }


    @Transactional(rollbackFor = Exception.class)
    public Channel diagnosePointStandardThreshold(Channel channel){
        channel.setSignalPoint("90");
        channel.setBlurPoint("65");
        channel.setContrastPoint("75");
        channel.setBrightPoint("55");
        channel.setDarkPoint("55");
        channel.setChromaPoint("25");
        channel.setMonoPoint("90");
        channel.setNoisePoint("60");
        channel.setStreakPoint("25");
        channel.setFreezePoint("90");
        channel.setShakePoint("10");
        channel.setFlashPoint("40");
        channel.setScenePoint("10");
        channel.setCoverPoint("80");
        channel.setPtzPoint("10");

        channel.setCheckFlag("1");
        channel.setStreamType("0");
        return channel;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllPMSId() {
        return tCameraInfoDao.selectAllPMSId();
    }
    @Transactional(rollbackFor = Exception.class)
    public String selectPmsIdById(Long cameraId) {
        return tCameraInfoDao.selectPmsIdById(cameraId);
    }

    //跨服新增监测点
    @Transactional(rollbackFor = Exception.class)
    public Channel acrossAddMonitor(TCameraInfo tCameraInfo){
        TCameraRecorderByDict recorder=tCameraRecorderDao.selectByPrimaryId(tCameraInfo.getRecordId());
        Channel channel=new Channel();
        try {
            diagnosePointStandardThreshold(channel);
            channel.setId(RandomStringUtils.randomAlphanumeric(12));
            channel.setIp(recorder.getRecordIp());
            channel.setPort(recorder.getHttpPort().toString());
            channel.setUserName(recorder.getIdentityManager());
            channel.setUserPwd(recorder.getIdentityCode());
            Integer realChannelNum = tCameraInfo.getChannelNum() /*+ 32*/;
            channel.setChanIndex(realChannelNum.toString());
            channel.setProtocol("0");
            switch (tCameraInfo.getIsControl()) {
                case 0:
                    channel.setDevType("1");
                    break;
                case 1:
                    channel.setDevType("0");
                    break;
                default:
                    break;
            }
            channel.setDevBrand("0");

            Constant.otherServerEntity(channel, Constant.DIAGNOSE_CHANNEL_OPERATE);

            log.info("channel:" + channel);
        }catch (Exception e){
            log.error("监测点新增失败"+e);
        }

        return channel;
    }

    @Transactional(rollbackFor = Exception.class)
    public HashMap<String,Object> selectByCameraIdOrRobotId(Long id,Integer type){
        if(id > 40000){
            return tCameraInfoDao.selectByCameraId(id);
        }
        if(id <40000 && id > 8000){
            if(type == 1){//可见光
                return tCameraInfoDao.selectByRobotIdByLight(id);
            }
            if(type == 2){//红外
                return tCameraInfoDao.selectByRobotIdByInferad(id);
            }
        }
        return null;
    }

    public void startKeepWatch(){
        KeepWatchJob keepWatchJob = new KeepWatchJob(redisTemplate,tCameraInfoDao,tCameraPresetDao);
        Thread thread = new Thread(keepWatchJob);
        thread.setDaemon(true);
        thread.start();
    }

    @Transactional(rollbackFor = Exception.class)
    public void stopStream(Long cameraId) {
        try {
            Result result=Constant.otherServerGet(String.valueOf(cameraId), Constant.CAMERA_STREAM_STOP);
            log.info((String) result.getData());
        } catch (Exception e) {
            log.info("cameraId: {} stopStream failed", cameraId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> importCameraInfo(List<TCameraInfoExcel> excelEntities) {
        List<TCameraInfo> tCameraInfos = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        List<TStdRegion> stdRegionList = tStdRegionDao.selectAll();
        List<TCameraRecorder> tCameraRecorderList = tCameraRecorderDao.selectAll();
        excelEntities.forEach(t -> {
            TCameraInfo tCameraInfo = new TCameraInfo();
            List<TStdRegion> regionList = stdRegionList.stream().filter(r -> r.getRegionName().equals(t.getRegionName())).collect(Collectors.toList());
            List<TCameraRecorder> recorderList = tCameraRecorderList.stream().filter(r -> r.getRecordName().equals(t.getRecordName())).collect(Collectors.toList());
            //只插入匹配的数据 其余不插入
            if (CollectionUtils.isNotEmpty(regionList) && CollectionUtils.isNotEmpty(recorderList)){
                tCameraInfo.setUpRegionId(regionList.get(0).getRegionId());
                tCameraInfo.setRecordId(recorderList.get(0).getRecordId());
                tCameraInfo.setPmsId(StringUtils.isNotEmpty(t.getPmsId()) ? t.getPmsId() : "");
                tCameraInfo.setCameraType(t.getCameraType());
                tCameraInfo.setCameraModel(t.getCameraModel());
                tCameraInfo.setCameraName(t.getCameraName());
                tCameraInfo.setAliasName(t.getAliasName());
                tCameraInfo.setCameraIp(t.getCameraIp());
                tCameraInfo.setPort(t.getPort());
                tCameraInfo.setInfreadPort(t.getInfreadPort());
                tCameraInfo.setCameraManager(t.getCameraManager());
                tCameraInfo.setCameraCode(t.getCameraCode());
                tCameraInfo.setChannelNum(t.getChannelNum());
                tCameraInfo.setUnit(t.getUnit());
                tCameraInfo.setAddress(t.getAddress());
                tCameraInfo.setVendorId(t.getVendorId());
                tCameraInfo.setLongitude(t.getLongitude());
                tCameraInfo.setLatitude(t.getLatitude());
                tCameraInfo.setIsControl("云台球机".equals(t.getIsControlStr()) ? 1 : 0);
                tCameraInfo.setCommissionDate(t.getCommissionDate());
                tCameraInfos.add(tCameraInfo);
            } else {
                errorList.add("【" + t.getCameraName() + "】设备所属区域或录像机不存在");
            }
        });
        if (CollectionUtils.isNotEmpty(tCameraInfos)) {
            tCameraInfoDao.batchInsert(tCameraInfos);
            this.intoRedis();
        }
        return errorList;
    }

}



