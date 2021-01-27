package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.yjh.platform.module.device.entity.VoiceDevice;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-12-01
*/
@Service
public class TVoiceDeviceService{

    @Autowired
    private TVoiceDeviceDao tVoiceDeviceDao;

    @Transactional(rollbackFor = Exception.class)
    public int add(TVoiceDevice tVoiceDevice) {
        return this.tVoiceDeviceDao.add(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String voiceDeviceId) {
        return this.tVoiceDeviceDao.deleteByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TVoiceDevice tVoiceDevice) {
        return this.tVoiceDeviceDao.update(tVoiceDevice);
    }

    @Transactional(rollbackFor = Exception.class)
    public TVoiceDevice selectByPrimaryId(String voiceDeviceId) {
        return this.tVoiceDeviceDao.selectByPrimaryId(voiceDeviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> select(String voiceDeviceId, String voiceDeviceName, Long stdDeviceId, String deviceType, String configId) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TVoiceDevice> selectByPage(TVoiceDevice tVoiceDevice) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.selectByPage(tVoiceDevice);
        return tVoiceDeviceList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TVoiceDevice> list) {
        return this.tVoiceDeviceDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String voiceDeviceId) {
    List<String> list1= Arrays.asList(voiceDeviceId.split(","));
    return this.tVoiceDeviceDao.batchDelete(list1);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<VoiceDevice> selectVoiceDeviceTree() {
        List<VoiceDevice> re = new ArrayList<>();
        VoiceDevice voiceDeviceTree = new VoiceDevice();
        voiceDeviceTree.setInfoType("tree");
        voiceDeviceTree.setLabel("音频设备树");
        voiceDeviceTree.setId("-1");
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.selectAll();
        List<VoiceDevice> child = new ArrayList<>();
        for (TVoiceDevice item:tVoiceDeviceList) {
            VoiceDevice childDevice = new VoiceDevice();
            childDevice.setId(item.getVoiceDeviceId());
            childDevice.setLabel(item.getVoiceDeviceName());
            childDevice.setUpId("1");
            childDevice.setInfoType("device");
            childDevice.setUpName("音频设备树");
            child.add(childDevice);
        }
        voiceDeviceTree.setChildren(child);
        re.add(voiceDeviceTree);
        return re;
    }
}

