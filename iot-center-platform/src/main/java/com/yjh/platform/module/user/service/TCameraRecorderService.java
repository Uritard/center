package com.yjh.platform.module.user.service;

import com.yjh.platform.common.utils.smUtil.ModelDecodeUtil;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
import com.yjh.platform.module.user.entity.TCameraRecorderExcel;
import com.yjh.platform.module.video.service.CameraConService;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.RecordEntity;
import com.yjh.video.api.entity.response.DeviceStatusResp;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.IRecordService;
import com.yjh.video.api.service.VideoServiceFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yc
* @since 2020-08-24
*/
@Service
public class TCameraRecorderService {

    @Autowired
    private TCameraRecorderDao tCameraRecorderDao;
    @Autowired
    private TCameraScreenDao tCameraScreenDao;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCameraInfoService tCameraInfoService;
    @Autowired
    private CameraConService cameraConService;

    private Logger log = LoggerFactory.getLogger(TCameraRecorderService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraRecorder tCameraRecorder) {
        ModelDecodeUtil.decodeField(tCameraRecorder, "identityCode");
        return this.tCameraRecorderDao.insert(tCameraRecorder);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long recordId) {
        //判断录像机下是否有摄像机
        {
            List<Long> list = tCameraRecorderDao.selectHaveCamera(recordId);
            if(list != null && list.size() > 0){
                return  -1;
            }
        }
        return this.tCameraRecorderDao.deleteByPrimaryId(recordId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedRecord(String recordIds) {
        List<String> list= Arrays.asList(recordIds.split(","));
        for(String item:list){
            List<Long> listHave = tCameraRecorderDao.selectHaveCamera(Long.valueOf(item));
            if(listHave != null && listHave.size() > 0){
                return  -1;
            }
        }
        return this.tCameraRecorderDao.deleteSelectedRecord(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraRecorder tCameraRecorder) {
        List<Long> cameraList = tCameraInfoService.selectCameraByRecord(tCameraRecorder.getRecordId());
        if (CollectionUtils.isNotEmpty(cameraList)) {
            cameraList.forEach(cameraId -> tCameraInfoService.stopStream(cameraId));
        }
        ModelDecodeUtil.decodeField(tCameraRecorder, "identityCode");
        return this.tCameraRecorderDao.update(tCameraRecorder);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCameraRecorderByDict selectByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.selectByPrimaryId(recordId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> select(Long recordId, String recordName,Integer recorderModel, String recorderType,Integer vendorId,String pmsId,String aliasName, String recordIp, String protocol, Integer httpPort, Integer transPort, Integer rtspPort, String identityManager, String identityCode, String protocolUrl, Integer maxChannel, Integer hddSize, Integer bufferDay, Integer timeLong,String unit) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.select(recordId, recordName,recorderModel, recorderType,vendorId,pmsId, aliasName, recordIp, protocol, httpPort, transPort, rtspPort, identityManager, identityCode, protocolUrl, maxChannel, hddSize, bufferDay, timeLong,unit);
        return tCameraRecorderByDictList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> selectByPage(Integer recordType,String aliasName,String unit,Integer vendorId, Integer recorderModel,String recordName) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.selectByPage(recordType,aliasName,unit,vendorId,recorderModel,recordName);

        log.info("nvr列表" + tCameraRecorderByDictList);
        // nvr的ID列表
        List<String> deviceIdList = tCameraRecorderByDictList.stream()
                .filter(tCameraRecorderByDict -> tCameraRecorderByDict.getRecordId() != null && tCameraRecorderByDict.getDeviceChannel() != null)
                .map(TCameraRecorderByDict::getDeviceChannel).distinct().collect(Collectors.toList());

        // nvr id存在则进行nvr状态查询， 反之直接设置nvr状态为未知
        if (deviceIdList.size() > 0) {
            IRecordService iRecordService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IRecordService.class);
            RecordEntity build = RecordEntity.builder().deviceIdList(deviceIdList).build();
            Result<List<DeviceStatusResp>> deviceStatusRespResult = iRecordService.queryNVRStatus(build);

            //接口调用成功进行状态设置，反之设为未知
            if (deviceStatusRespResult.isSuccess()) {
                List<DeviceStatusResp> deviceStatusRespList = deviceStatusRespResult.getData();
                Map<String, DeviceStatusResp> deviceStatusRespMap = deviceStatusRespList.stream()
                        .collect(Collectors.toMap(DeviceStatusResp::getDeviceId, deviceStatusResp -> deviceStatusResp));

                // 循环遍历数组进行nvr状态设置
                for (TCameraRecorderByDict tCameraRecorderByDict : tCameraRecorderByDictList) {
                    if (tCameraRecorderByDict.getDeviceChannel() == null) {
                        tCameraRecorderByDict.setRecorderStatus("未知");
                        continue;
                    }
                    DeviceStatusResp deviceStatusResp = deviceStatusRespMap.get(tCameraRecorderByDict.getDeviceChannel());
                    if (deviceStatusResp != null) {
                        if (deviceStatusResp.getOnLine()) {
                            tCameraRecorderByDict.setRecorderStatus("在线");
                        } else {
                            tCameraRecorderByDict.setRecorderStatus("离线");
                        }
                    } else {
                        tCameraRecorderByDict.setRecorderStatus("未知");
                    }
                }
            } else {
                tCameraRecorderByDictList.forEach(tCameraRecorderByDict -> tCameraRecorderByDict.setRecorderStatus("未知"));
            }
        } else {
            tCameraRecorderByDictList.forEach(tCameraRecorderByDict -> tCameraRecorderByDict.setRecorderStatus("未知"));
        }
        log.info("nvr状态列表" + tCameraRecorderByDictList);
        return tCameraRecorderByDictList;
    }


    @Transactional(rollbackFor = Exception.class)
    public boolean synchronizeFromPMS(String pmsId) throws Exception{
        Long recorderId = tCameraRecorderDao.selectRecorderIdByPmsId(pmsId);

        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String filePathAndName = resMap.get("content") +  "/PMS/RecorderPMS.xml";
//        String filePathAndName = "D:/testform/PMS/RecorderPMS.xml";
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

                        TCameraRecorder tCameraRecorder = new TCameraRecorder()
                                .setRecordId(recorderId);
                        if (map.containsKey("recordName") && (!"".equals(map.get("recordName")))) {
                            tCameraRecorder.setRecordName(map.get("recordName").toString());
                        }
                        if (map.containsKey("recorderModel") && (!"".equals(map.get("recorderModel")))) {
                            Integer recorderModel = Integer.valueOf(tRobotInfoDao.selectDictCode("recorder_model",map.get("recorderModel").toString()));
                            tCameraRecorder.setRecorderModel(recorderModel);
                        }
                        if (map.containsKey("recorderType") && (!"".equals(map.get("recorderType")))) {
                            String recorderType = tRobotInfoDao.selectDictCode("recorder_type",map.get("recorderType").toString());
                            tCameraRecorder.setRecorderType(recorderType);
                        }
                        if (map.containsKey("vendorId") && (!"".equals(map.get("vendorId")))) {
                            Integer vendorId = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_vendor",map.get("vendorId").toString()));
                            tCameraRecorder.setVendorId(vendorId);
                        }
                        if (map.containsKey("aliasName") && (!"".equals(map.get("aliasName")))) {
                            tCameraRecorder.setAliasName(map.get("aliasName").toString());
                        }
                        if (map.containsKey("recordIp") && (!"".equals(map.get("recordIp")))) {
                            tCameraRecorder.setRecordIp(map.get("recordIp").toString());
                        }
                        if (map.containsKey("protocol") && (!"".equals(map.get("protocol")))) {
                            String protocol = tRobotInfoDao.selectDictCode("protocol_type",map.get("protocol").toString());
                            tCameraRecorder.setProtocol(protocol);
                        }
                        if (map.containsKey("httpPort") && (!"".equals(map.get("httpPort")))) {
                            tCameraRecorder.setHttpPort(Integer.valueOf(map.get("httpPort").toString()));
                        }
                        if (map.containsKey("transPort") && (!"".equals(map.get("transPort")))) {
                            tCameraRecorder.setTransPort(Integer.valueOf(map.get("transPort").toString()));
                        }
                        if (map.containsKey("rtspPort") && (!"".equals(map.get("rtspPort")))) {
                            tCameraRecorder.setRtspPort(Integer.valueOf(map.get("rtspPort").toString()));
                        }
                        if (map.containsKey("identityManager") && (!"".equals(map.get("identityManager")))) {
                            tCameraRecorder.setIdentityManager(map.get("identityManager").toString());
                        }
                        if (map.containsKey("identityCode") && (!"".equals(map.get("identityCode")))) {
                            tCameraRecorder.setIdentityCode(map.get("identityCode").toString());
                        }
                        if (map.containsKey("protocolUrl") && (!"".equals(map.get("protocolUrl")))) {
                            tCameraRecorder.setProtocolUrl(map.get("protocolUrl").toString());
                        }
                        if (map.containsKey("maxChannel") && (!"".equals(map.get("maxChannel")))) {
                            tCameraRecorder.setMaxChannel(Integer.valueOf(map.get("maxChannel").toString()));
                        }
                        if (map.containsKey("hddSize") && (!"".equals(map.get("hddSize")))) {
                            tCameraRecorder.setHddSize(Integer.valueOf(map.get("hddSize").toString()));
                        }
                        if (map.containsKey("bufferDay") && (!"".equals(map.get("bufferDay")))) {
                            tCameraRecorder.setBufferDay(Integer.valueOf(map.get("bufferDay").toString()));
                        }
                        if (map.containsKey("timeLong") && (!"".equals(map.get("timeLong")))) {
                            tCameraRecorder.setTimeLong(Integer.valueOf(map.get("timeLong").toString()));
                        }
                        if (map.containsKey("unit") && (!"".equals(map.get("unit")))) {
                            tCameraRecorder.setUnit(map.get("unit").toString());
                        }
                        log.info("tCameraRecorder==="+tCameraRecorder);
                        tCameraRecorderDao.update(tCameraRecorder);
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
    public List<TCameraRecorder> importCameraRecorder(List<TCameraRecorderExcel> excelEntities) {
        List<TCameraRecorder> tCameraRecorders = new ArrayList<>();
        excelEntities.forEach(t -> {
            TCameraRecorder tCameraRecorder = new TCameraRecorder();
            tCameraRecorder.setRecordName(t.getRecordName());
            tCameraRecorder.setPmsId(t.getPmsId());
            tCameraRecorder.setAliasName(t.getAliasName());
            tCameraRecorder.setRecorderType(t.getRecorderType());
            tCameraRecorder.setRecorderModel(t.getRecorderModel());
            tCameraRecorder.setVendorId(t.getVendorId());
            tCameraRecorder.setUnit(t.getUnit());
            tCameraRecorder.setRecordIp(t.getRecordIp());
            tCameraRecorder.setHttpPort(t.getHttpPort());
            tCameraRecorder.setRtspPort(t.getRtspPort());
            tCameraRecorder.setProtocol(t.getProtocol());
            tCameraRecorder.setProtocolUrl(t.getProtocolUrl());
            tCameraRecorder.setIdentityManager(t.getIdentityManager());
            tCameraRecorder.setIdentityCode(t.getIdentityCode());
            tCameraRecorder.setMaxChannel(t.getMaxChannel());
            tCameraRecorder.setBufferDay(t.getBufferDay());
            tCameraRecorder.setHddSize(t.getHddSize());
            tCameraRecorder.setTimeLong(t.getTimeLong());
            tCameraRecorders.add(tCameraRecorder);
        });
        if (CollectionUtils.isNotEmpty(tCameraRecorders)) {
            this.batchInsert(tCameraRecorders);
        }
        return tCameraRecorders;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderDetail> selectIdAndName(){
        return tCameraRecorderDao.selectIdAndName();
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraRecorder> list) {
        return this.tCameraRecorderDao.batchInsert(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllPMSId() {
        return tCameraRecorderDao.selectAllPMSId();
    }
    @Transactional(rollbackFor = Exception.class)
    public String selectPmsIdById(Long recordId) {
        return tCameraRecorderDao.selectPmsIdById(recordId);
    }

}

