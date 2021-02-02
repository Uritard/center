package com.yjh.platform.module.user.service;


import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.*;
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

import java.io.File;
import java.util.*;




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

    private Logger log = LoggerFactory.getLogger(TCameraInfoService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraInfo tCameraInfo) {
        this.tCameraInfoDao.insert(tCameraInfo);
        return this.intoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cameraId) {
        this.tCameraInfoDao.deleteByPrimaryId(cameraId);
        return this.intoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedCamera(String cameraIds) {
        List<String> list = Arrays.asList(cameraIds.split(","));
        int i = this.tCameraInfoDao.deleteSelectedCamera(list);
        this.intoRedis();
        return i;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraInfo tCameraInfo) {
        return this.tCameraInfoDao.update(tCameraInfo);
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

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> select(Long cameraId, String cameraName, Integer cameraModel,String pmsId,String aliasName, String recordId,
                                          Long upRegionId, Integer channelNum, Integer smsId, Integer rmsId,String monitorId,
                                          Integer vendorId, Integer streamType, Integer protocolType, String cameraIp,
                                          String url, Integer port, Integer cameraType, Integer isControl, String latitude,
                                          String longitude, String address,String unit) {
        return tCameraInfoDao.select(cameraId, cameraName, cameraModel,pmsId,aliasName,
                recordId, upRegionId, channelNum, smsId, rmsId, monitorId,vendorId, streamType, protocolType, cameraIp, url,
                port, cameraType,isControl,latitude, longitude, address, unit);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraInfoByDict> selectByPage(String aliasName,String unit,String address,String cameraVendor,
                                                Integer cameraModel,String cameraName,List<Long> regionIdList) {
        List<TCameraInfoByDict> tCameraInfoByDict = tCameraInfoDao.selectByPage(aliasName,unit,address,cameraVendor,cameraModel,cameraName,regionIdList);

        Map<String,String> map = new HashMap<>();
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            HashMap<String, Object> recordIdMap = new HashMap<>();
            recordIdMap.put("recordId",recordId );
            Result re = cameraStates(recordIdMap);
            map.putAll((Map<String,String>)re.getData());
        }
        for (TCameraInfoByDict xi : tCameraInfoByDict){
            if (map.containsKey(xi.getCameraId().toString())){
                if ("0".equals(map.get(xi.getCameraId().toString()))){
                    xi.setCameraStatus("离线");
                }else{
                    xi.setCameraStatus("在线");
                }
            }else{
                xi.setCameraStatus("离线");
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
//        String filePathAndName = "D:/testform/PMS/摄像机PMS系统.xml";
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

                    Integer cameraModel = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_model",map.get("cameraModel").toString()));
                    Integer vendorId = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_vendor",map.get("vendorId").toString()));
                    Integer cameraType = Integer.valueOf(tRobotInfoDao.selectDictCode("camera_type",map.get("cameraType").toString()));

                    TCameraInfo tCameraInfo = new TCameraInfo()
                            .setCameraId(cameraId)
                            .setCameraModel(cameraModel)
                            .setAliasName(map.get("aliasName").toString())
                            .setChannelNum(Integer.valueOf(map.get("channelNum").toString()))
                            .setVendorId(vendorId)
                            .setCameraIp(map.get("cameraIp").toString())
                            .setPort(Integer.valueOf(map.get("port").toString()))
                            .setCameraType(cameraType)
                            .setIsControl(Integer.valueOf(map.get("isControl").toString()))
                            .setAddress(map.get("address").toString())
                            .setUnit(map.get("unit").toString());
                    log.info("tCameraInfo==="+tCameraInfo);
                    tCameraInfoDao.update(tCameraInfo);
                    return true;
                }
            }
        }
        return false;
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
    public int intoRedis() {
        List<Long> list =tCameraInfoDao.selectCameraAll();
        for (Long item:list) {
            Map<String,String> map = new HashMap<>();
            map.put("cameraId",item.toString());
            map.put("state","0");
            String str = "camera_info:"+item;
            redisTemplate.opsForHash().putAll(str, map);
        }
       return 1;
    }



}



