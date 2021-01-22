package com.yjh.platform.module.user.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TSequentialConf;
import com.yjh.platform.module.user.dao.TSequentialConfDao;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TSequentialConf tSequentialConf) {
        tSequentialConf.setCfgMeteId(tSequentialConf.getCfgDeviceId());
        return this.tSequentialConfDao.add(tSequentialConf);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cfgDeviceId) {
        return this.tSequentialConfDao.deleteByPrimaryId(cfgDeviceId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TSequentialConf tSequentialConf) {
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
        areaInfoItem2.setUpId(-1L);
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
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);
        //遥测
        List<AreaInfo> listItem4 = this.tSequentialConfDao.selectForTCfgMete(4,cfgDeviceName);
        AreaInfo areaInfoItem4 = new AreaInfo();
        areaInfoItem2.setId(4L);
        areaInfoItem2.setUpId(-4L);
        areaInfoItem2.setLabel("遥调");
        areaInfoItem2.setChildren(listItem2);
        areaInfoItem2.setInfoType("meteKind");
        list.add(areaInfoItem2);

        return list;
    }



}

