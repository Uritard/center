package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.TCameraRecorderDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderDetail;
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
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TCameraRecorderService.class);

    @Logs(title = "插入", code = "tCameraRecorder",content = "根据web传递的参数插入录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.insert(tCameraRecorder);
    }

    @Logs(title = "删除", code = "tCameraRecorder",content = "根据web传递的参数删除录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.deleteByPrimaryId(recordId);
    }

    @Logs(title = "批量删除", code = "tCameraRecorder",content = "根据web传递的参数批量删除录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedRecord(String recordIds) {
        List<String> list= Arrays.asList(recordIds.split(","));
        return this.tCameraRecorderDao.deleteSelectedRecord(list);
    }

    @Logs(title = "更新", code = "tCameraRecorder",content = "根据web传递的参数更新录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraRecorder tCameraRecorder) {
        return this.tCameraRecorderDao.update(tCameraRecorder);
    }

    @Logs(title = "主键查询", code = "tCameraRecorder",content = "根据web传递的参数查询录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public TCameraRecorderByDict selectByPrimaryId(Long recordId) {
        return this.tCameraRecorderDao.selectByPrimaryId(recordId);
    }

    @Logs(title = "查询", code = "tCameraRecorder",content = "根据web传递的参数查询录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> select(Long recordId, String recordName,Integer recorderModel, String recorderType,Integer vendorId,String pmsId,String aliasName, String recordIp, String protocol, Integer httpPort, Integer transPort, Integer rtspPort, String userName, String pwd, String protocolUrl, Integer maxChannel, Integer hddSize, Integer bufferDay, Integer timeLong,String unit) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.select(recordId, recordName,recorderModel, recorderType,vendorId,pmsId, aliasName, recordIp, protocol, httpPort, transPort, rtspPort, userName, pwd, protocolUrl, maxChannel, hddSize, bufferDay, timeLong,unit);
        return tCameraRecorderByDictList;
    }

    @Logs(title = "分页查询", code = "tCameraRecorder",content = "根据web传递的参数查询录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderByDict> selectByPage(String aliasName,String unit,Integer vendorId, Integer recorderModel,String recordName) {
        List<TCameraRecorderByDict> tCameraRecorderByDictList = tCameraRecorderDao.selectByPage(aliasName,unit,vendorId,recorderModel,recordName);

        for(TCameraRecorderByDict res : tCameraRecorderByDictList) {
            HashMap<String, Object> recordIdMap = new HashMap<>();
            log.info("recordId==="+res.getRecordId());
            recordIdMap.put("recordId", res.getRecordId());
            Result re = recorderStates(recordIdMap);
            log.info("re---"+re);
            Map<String,Object> mapRes = JSONObject.parseObject(JSON.toJSONString(re.getData()));
            log.info("object转map的东西==="+mapRes);
            if ("403".equals(mapRes.get("errorCode: ").toString())){
                res.setRecorderStatus("离线");
            }else {
                res.setRecorderStatus("在线");
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
    @Logs(title = "从PMS系统同步录像机信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public boolean synchronizeFromPMS(String pmsId) throws Exception{
        Long recorderId = tCameraRecorderDao.selectRecorderIdByPmsId(pmsId);

        Map<String,String> resMap = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String filePathAndName = resMap.get("content") +  "/PMS/RecorderPMS.xml";
//        String filePathAndName = "D:/testform/PMS/录像机PMS系统.xml";
        log.info("路径是==="+filePathAndName);

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        //解析xml
        Element rootElement = document.getRootElement();

        Iterator iterator = rootElement.elementIterator();
        Map<String,Object> map = new HashMap<>();
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
                    log.info("map的结果是===" + map);

                    Integer recorderModel = Integer.valueOf(tRobotInfoDao.selectDictCode("recorder_model",map.get("recorderModel").toString()));
                    String recorderType = tRobotInfoDao.selectDictCode("recorder_type",map.get("recorderType").toString());
                    Integer vendorId = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_vendor",map.get("vendorId").toString()));


                    TCameraRecorder tCameraRecorder = new TCameraRecorder()
                            .setRecordId(recorderId)
                            .setRecorderModel(recorderModel)
                            .setRecorderType(recorderType)
                            .setVendorId(vendorId)
                            .setAliasName(map.get("aliasName").toString())
                            .setRecordIp(map.get("recordIp").toString())
                            .setProtocol(map.get("protocol").toString())
                            .setHttpPort(Integer.valueOf(map.get("httpPort").toString()))
                            .setTransPort(Integer.valueOf(map.get("transPort").toString()))
                            .setRtspPort(Integer.valueOf(map.get("rtspPort").toString()))
                            .setUserName(map.get("userName").toString())
                            .setPwd(map.get("pwd").toString())
                            .setProtocolUrl(map.get("protocolUrl").toString())
                            .setMaxChannel(Integer.valueOf(map.get("maxChannel").toString()))
                            .setHddSize(Integer.valueOf(map.get("hddSize").toString()))
                            .setBufferDay(Integer.valueOf(map.get("bufferDay").toString()))
                            .setTimeLong(Integer.valueOf(map.get("timeLong").toString()))
                            .setUnit(map.get("unit").toString());
                    log.info("tCameraRecorder==="+tCameraRecorder);
                    tCameraRecorderDao.update(tCameraRecorder);
                    return true;
                }
            }
        }
        return false;
    }
    @Logs(title = "查询部分信息", code = "tCameraRecorder",content = "根据web传递的参数查询录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraRecorderDetail> selectIdAndName(){
        return tCameraRecorderDao.selectIdAndName();
    }

    @Logs(title = "批量插入", code = "tCameraRecorder",content = "根据web传递的参数批量插入录像服务器信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraRecorder> list) {
        return this.tCameraRecorderDao.batchInsert(list);
    }

}

