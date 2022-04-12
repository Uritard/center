package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
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

    private Logger log = LoggerFactory.getLogger(TCameraRecorderService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraRecorder tCameraRecorder) {
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
    public List<TCameraRecorderByDict> selectByPage(String aliasName,String unit,Integer vendorId, Integer recorderModel,String recordName) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.selectByPage(aliasName,unit,vendorId,recorderModel,recordName);

        for(TCameraRecorderByDict res : tCameraRecorderByDictList) {
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId", res.getRecordId());
            Result re = recorderStates(recordIdMap);
            log.info("re---"+re);
            if (Objects.nonNull(re)){
                Map<String,Object> mapRes = JSONObject.parseObject(JSON.toJSONString(re.getData()));
                if ("200".equals(mapRes.get("errorCode: ").toString())){
                    res.setRecorderStatus("在线");
                }else {
                    res.setRecorderStatus("离线");
                }
            }else {
                res.setRecorderStatus("未知");
            }
        }

        return tCameraRecorderByDictList;
    }

    private static Result recorderStates(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(Constant.CAMERA_STATES, Result.class,map);
            }
        } catch (Exception e) {

        }
        return re;
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

