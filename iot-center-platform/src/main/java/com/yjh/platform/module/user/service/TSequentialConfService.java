package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.task.dao.TCfgDataCurrentDao;
import com.yjh.platform.module.task.entity.TCfgDataCurrent;
import com.yjh.platform.module.user.controller.TSequentialConfController;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.user.dao.TSequentialConfDao;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2021-01-21
*/
@Service
public class TSequentialConfService{

    @Autowired
    private TSequentialConfDao tSequentialConfDao;
    @Autowired
    private TCfgDataCurrentDao tCfgDataCurrentDao;


    private Logger log = LoggerFactory.getLogger(TSequentialConfService.class);

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TSequentialConf tSequentialConf) {
        List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
        if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
            return -1;
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        if(tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId()) != null){
           return tSequentialConfDao.update(tSequentialConf);
        }else {
            return this.tSequentialConfDao.add(tSequentialConf);
        }
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.deleteByPrimaryId(cfgDeviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TSequentialConf tSequentialConf) {
        TSequentialConf old = tSequentialConfDao.selectByPrimaryId(tSequentialConf.getCfgDeviceId());
        if(old.getCameraId() != tSequentialConf.getCameraId()){
            List<Long> cameraIdList = tSequentialConfDao.selectCameraId();
            if(cameraIdList != null && cameraIdList.size() >0 && cameraIdList.contains(tSequentialConf.getCameraId())){
                return -1;
            }
        }
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        return this.tSequentialConfDao.update(tSequentialConf);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TSequentialConf selectByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.selectByPrimaryId(cfgDeviceId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> select(String cfgDeviceId, String cfgMeteId, Long presetId, String identifyResult) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.select(cfgDeviceId, cfgMeteId, presetId, identifyResult);
        return tSequentialConfList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TSequentialConf> selectByPage(String cfgDeviceName) {
        List<TSequentialConf> tSequentialConfList = tSequentialConfDao.selectByPage(cfgDeviceName);
        return tSequentialConfList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TSequentialConf> list) {
        return this.tSequentialConfDao.batchAdd(list);
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String cfgDeviceId) {
    List<String> list1= Arrays.asList(cfgDeviceId.split(","));
    return this.tSequentialConfDao.batchDelete(list1);
    }

    @Logs(title = "查询四遥树信息", code = "TCfgUnionRule",content = "查询四遥信息")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectForCfgDeviceTree(String cfgDeviceName){
        List<AreaInfo> list = new LinkedList<>();
        //遥信
        List<AreaInfo> listItem1 = this.tSequentialConfDao.selectForTCfgMete(1,cfgDeviceName);
        AreaInfo areaInfoItem1 = new AreaInfo();
        areaInfoItem1.setId(1L);
        areaInfoItem1.setUpId(-1L);
        areaInfoItem1.setLabel("遥信");
        areaInfoItem1.setChildren(listItem1);
        areaInfoItem1.setInfoType("meteKind");
        list.add(areaInfoItem1);
        //遥测
        List<AreaInfo> listItem2 = this.tSequentialConfDao.selectForTCfgMete(2,cfgDeviceName);
        AreaInfo areaInfoItem2 = new AreaInfo();
        areaInfoItem2.setId(2L);
        areaInfoItem2.setUpId(-2L);
        areaInfoItem2.setLabel("遥测");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);
        //遥测
        List<AreaInfo> listItem3 = this.tSequentialConfDao.selectForTCfgMete(3,cfgDeviceName);
        AreaInfo areaInfoItem3 = new AreaInfo();
        areaInfoItem2.setId(3L);
        areaInfoItem2.setUpId(-3L);
        areaInfoItem2.setLabel("遥控");
        areaInfoItem2.setChildren(listItem3);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem3);
        //遥测
        List<AreaInfo> listItem4 = this.tSequentialConfDao.selectForTCfgMete(4,cfgDeviceName);
        AreaInfo areaInfoItem4 = new AreaInfo();
        areaInfoItem2.setId(4L);
        areaInfoItem2.setUpId(-4L);
        areaInfoItem2.setLabel("遥调");
        areaInfoItem2.setChildren(listItem4);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem4);

        return list;
    }

    @Logs(title = "顺控联动", code = "TCfgUnionRule",content = "查询四遥信息")
    @Transactional(rollbackFor = Exception.class)
    public String sequential(String meteId){
        {
            Map<String,String> map = this.sequentialInfo(meteId).get(0);
            Map<String, Object> jasonMaps2 = new HashMap<>();
            jasonMaps2.put("type", "newSequential");
            jasonMaps2.put("cfgDeviceId", meteId);
            jasonMaps2.put("sort", map.get("sort"));
            jasonMaps2.put("state", map.get("state"));
            String json = JSON.toJSONString(jasonMaps2);
            log.info("发送给前端的消息：" + json);
            //结果
//            {"type": "newSequentialResult",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合",
//                    "identifyResult": "合"
//            }
//            顺控
//            {"type": "newSequential",
//                    "cfgDeviceId": "1001",
//                    "sort": "1",
//                    "state": "控合"
//            }


            //todo 生成一个巡视任务

            //todo 发给算法进行分析

            //todo 生成顺控文件
        }
       return "ok";
    }

    @Logs(title = "顺控联动", code = "TCfgUnionRule",content = "查询四遥信息")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> sequentialInfo(String cfgDeviceId){
        //todo 写入识别结果
         return tSequentialConfDao.selectForSequenceInfo(cfgDeviceId);
    }



}

