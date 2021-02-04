package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.dao.TVoiceDeviceDao;

import java.util.*;

import com.yjh.platform.module.device.entity.VoiceDevice;
import com.yjh.platform.module.user.entity.AreaInfoDetail;
import org.apache.ibatis.annotations.Param;
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
    public List<TVoiceDevice> select(String voiceDeviceId, String voiceDeviceName, Long stdDeviceId, String deviceType, String configId,Long upRegionId) {
        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId, upRegionId);
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
    public List<AreaInfo> selectVoiceDeviceTree(String voiceDeviceName) {
//        List<VoiceDevice> re = new ArrayList<>();
//        VoiceDevice voiceDeviceTree = new VoiceDevice();
//        voiceDeviceTree.setInfoType("tree");
//        voiceDeviceTree.setLabel("音频设备树");
//        voiceDeviceTree.setId("-1");
//        List<TVoiceDevice> tVoiceDeviceList = tVoiceDeviceDao.selectAll();
//        List<VoiceDevice> child = new ArrayList<>();
//        for (TVoiceDevice item:tVoiceDeviceList) {
//            VoiceDevice childDevice = new VoiceDevice();
//            childDevice.setId(item.getVoiceDeviceId());
//            childDevice.setLabel(item.getVoiceDeviceName());
//            childDevice.setUpId("1");
//            childDevice.setInfoType("device");
//            childDevice.setUpName("音频设备树");
//            child.add(childDevice);
//        }
//        voiceDeviceTree.setChildren(child);
//        re.add(voiceDeviceTree);
//        return re;
        List<AreaInfo> listTree = new ArrayList<>();
        listTree = tVoiceDeviceDao.selectAll(voiceDeviceName);
        List<AreaInfo> areaInfoCountryList = new ArrayList<>();
        for(Iterator<AreaInfo> it = listTree.iterator(); it.hasNext();){
            AreaInfo areaInfoMap = it.next();
            if (Objects.nonNull(areaInfoMap.getUpId()) && areaInfoMap.getUpId()==-1) {
                AreaInfo areaInfoCountry = new AreaInfo();
                areaInfoCountry.setId(areaInfoMap.getId());
                areaInfoCountry.setLabel(areaInfoMap.getLabel());
                areaInfoCountry.setInfoType(areaInfoMap.getInfoType());
                areaInfoCountryList.add(areaInfoCountry);
            }
        }
        //获取相机的状态
        diGui(areaInfoCountryList, listTree);
        return areaInfoCountryList;
    }
    private void diGui(List<AreaInfo> areaInfoList, List<AreaInfo> listTree) {
        for(AreaInfo areaInfo : areaInfoList){
            List<AreaInfo> childrenList = new ArrayList<>();
            for(Iterator<AreaInfo> it = listTree.iterator();it.hasNext();){
                AreaInfo areaInfoMap = it.next();
                if (Objects.equals(areaInfo.getId(), areaInfoMap.getUpId())) {
                    AreaInfo areaInfoTem = new AreaInfo();
                    areaInfoTem.setId(areaInfoMap.getId());
                    areaInfoTem.setUpId(areaInfoMap.getUpId());
                    areaInfoTem.setLabel(areaInfoMap.getLabel());
                    areaInfoTem.setInfoType(areaInfoMap.getInfoType());
                    areaInfoTem.setUpName(areaInfoMap.getUpName());
                    childrenList.add(areaInfoTem);
                }
            }
            if (childrenList.size()>0 ) {
                areaInfo.setChildren(childrenList);
                diGui(childrenList, listTree);
            }
        }
    }
}

