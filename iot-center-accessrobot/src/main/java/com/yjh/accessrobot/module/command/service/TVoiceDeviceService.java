package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.dao.TVoiceConfigMapper;
import com.yjh.accessrobot.module.command.dao.TVoiceDeviceMapper;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import com.yjh.accessrobot.module.command.entity.TVoiceConfig;
import com.yjh.accessrobot.module.command.entity.TVoiceDevice;
import com.yjh.accessrobot.module.command.entity.VoiceDeviceModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/14
 * @since [产品/模块版本] （可选）
 */
@Service
public class TVoiceDeviceService {

    @Autowired
    private TVoiceDeviceMapper tVoiceDeviceMapper;
    @Autowired
    private TVoiceConfigMapper tVoiceConfigMapper;
    @Autowired
    private TStdRegionDao tStdRegionDao;

    @Transactional
    public void saveReportData(List<VoiceDeviceModel> voiceDeviceModelList, String edgeNode) {
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, null);
        Map<String, Long> stdRegionMap = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, TStdRegion::getRegionId));
        Map<String, TVoiceConfig> voiceConfigMap = new HashMap<>();
        List<TVoiceDevice> tVoiceDeviceList = voiceDeviceModelList.stream().peek(voiceDeviceModel -> {
            TVoiceConfig tVoiceConfig = new TVoiceConfig();
            tVoiceConfig.setConfigId(null);
            tVoiceConfig.setFtpUrl(voiceDeviceModel.getFtpUrl());
            tVoiceConfig.setStationId(voiceDeviceModel.getStationId());
            tVoiceConfig.setOwner(voiceDeviceModel.getOwner());
            tVoiceConfig.setOwnerCode(voiceDeviceModel.getOwnerCode());
            tVoiceConfig.setPort(voiceDeviceModel.getPort());
            tVoiceConfig.setChannelNum(voiceDeviceModel.getChannelNum());
            tVoiceConfig.setAbsoluPath(voiceDeviceModel.getAbsoluPath());
            tVoiceConfig.setRelativePath(voiceDeviceModel.getRelativePath());
            tVoiceConfig.setDbValue(voiceDeviceModel.getDbValue());
            tVoiceConfig.setFValue(voiceDeviceModel.getFValue());
            tVoiceConfig.setMpValue(voiceDeviceModel.getMpValue());
            tVoiceConfig.setFilePath(voiceDeviceModel.getFilePath());
            tVoiceConfig.setPmsId(voiceDeviceModel.getPmsId());
            voiceConfigMap.put(voiceDeviceModel.getVoiceDeviceId().toString(), tVoiceConfig);
        }).map(voiceDeviceModel -> {
            TVoiceDevice tVoiceDevice = new TVoiceDevice();
            tVoiceDevice.setVoiceDeviceId(null);
            tVoiceDevice.setVoiceDeviceName(voiceDeviceModel.getVoiceDeviceName());
            tVoiceDevice.setStdDeviceId(null);
            tVoiceDevice.setDeviceType(voiceDeviceModel.getDeviceType());
            tVoiceDevice.setConfigId(voiceDeviceModel.getConfigId());
            tVoiceDevice.setUpRegionId(stdRegionMap.get(voiceDeviceModel.getUpRegionId().toString()));
            tVoiceDevice.setState(voiceDeviceModel.getState());
            tVoiceDevice.setVoiceCode(voiceDeviceModel.getPatroldeviceCode());
            tVoiceDevice.setVoiceType(voiceDeviceModel.getVoiceType());
            tVoiceDevice.setVoiceModel(voiceDeviceModel.getVoiceModel());
            tVoiceDevice.setVoiceFactory(voiceDeviceModel.getVoiceFactory());
            tVoiceDevice.setEdgeCode(edgeNode);
            tVoiceDevice.setOriginId(voiceDeviceModel.getVoiceDeviceId().toString());
            return tVoiceDevice;
        }).collect(Collectors.toList());
        List<TVoiceDevice> oldVoiceDeviceList = tVoiceDeviceMapper.selectByEdgeCode(edgeNode);
        // 不存在旧数据则更新
        if (CollectionUtils.isEmpty(oldVoiceDeviceList)) {
            if(CollectionUtils.isNotEmpty( tVoiceDeviceList )){
                tVoiceConfigMapper.insertBatch(voiceConfigMap.values());
                tVoiceDeviceList.forEach(tVoiceDevice -> {
                    TVoiceConfig tVoiceConfig = voiceConfigMap.get(tVoiceDevice.getOriginId());
                    tVoiceDevice.setConfigId(tVoiceConfig.getConfigId());
                });
                tVoiceDeviceMapper.insertBatch(tVoiceDeviceList);
            }
            //否则对比
        } else {
            Map<String, TVoiceDevice> oldTVoiceDeviceMap = oldVoiceDeviceList.stream().collect(Collectors.toMap(TVoiceDevice::getOriginId, Function.identity()));
            Map<String, TVoiceDevice> newTVoiceDeviceMap = tVoiceDeviceList.stream().collect(Collectors.toMap(TVoiceDevice::getOriginId, Function.identity()));
            // 更新的数据
            SetUtils.SetView<String> updateIdCollection = SetUtils.intersection(oldTVoiceDeviceMap.keySet(), newTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                tVoiceDeviceList.stream().filter(tCameraRecorder -> updateIdCollection.contains(tCameraRecorder.getOriginId())).forEach(tVoiceDevice -> {
                    TVoiceDevice oldVoiceDevice = oldTVoiceDeviceMap.get(tVoiceDevice.getOriginId());
                    TVoiceConfig tVoiceConfig = voiceConfigMap.get(tVoiceDevice.getOriginId());
                    tVoiceConfig.setConfigId(oldVoiceDevice.getConfigId());
                    tVoiceConfigMapper.updateByPrimaryKey(tVoiceConfig);
                    tVoiceDevice.setVoiceDeviceId(oldVoiceDevice.getVoiceDeviceId());
                    tVoiceDevice.setConfigId(oldVoiceDevice.getConfigId());
                    tVoiceDeviceMapper.updateByPrimaryKey(tVoiceDevice);
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdCollection = SetUtils.difference(oldTVoiceDeviceMap.keySet(), newTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdCollection)) {
                tVoiceConfigMapper.deleteByDeviceEdgeCodeAndOriginId(edgeNode, deleteIdCollection);
                tVoiceDeviceMapper.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdCollection);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdCollection = SetUtils.difference(newTVoiceDeviceMap.keySet(), oldTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdCollection)) {
                List<TVoiceConfig> insertTVoiceConfigList = insertIdCollection.stream().map(voiceConfigMap::get).collect(Collectors.toList());
                tVoiceConfigMapper.insertBatch(insertTVoiceConfigList);
                List<TVoiceDevice> insertVoiceDeviceList = tVoiceDeviceList.stream().filter(tVoiceDevice -> insertIdCollection.contains(tVoiceDevice.getOriginId())).peek(tVoiceDevice -> {
                    TVoiceConfig tVoiceConfig = voiceConfigMap.get(tVoiceDevice.getOriginId());
                    tVoiceDevice.setConfigId(tVoiceConfig.getConfigId());
                }).collect(Collectors.toList());
                tVoiceDeviceMapper.insertBatch(insertVoiceDeviceList);
            }
        }
    }
}

