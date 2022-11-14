package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.module.command.dao.TStdRegionDao;
import com.yjh.accessrobot.module.command.dao.TVoiceDeviceMapper;
import com.yjh.accessrobot.module.command.entity.TStdRegion;
import com.yjh.accessrobot.module.command.entity.TVoiceDevice;
import com.yjh.accessrobot.module.command.entity.VoiceDeviceModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TStdRegionDao tStdRegionDao;

    @Transactional
    public void saveReportData(List<VoiceDeviceModel> voiceDeviceModelList, String edgeNode) {
        List<TStdRegion> stdRegionList = tStdRegionDao.selectByRegionCodeAndState(edgeNode, null);
        Map<String, Long> stdRegionMap = stdRegionList.stream().collect(Collectors.toMap(TStdRegion::getOriginRegionId, TStdRegion::getRegionId));
        List<TVoiceDevice> tVoiceDeviceList = voiceDeviceModelList.stream().map(voiceDeviceModel -> {
            TVoiceDevice tVoiceDevice = new TVoiceDevice();
            tVoiceDevice.setVoiceDeviceId(null);
            tVoiceDevice.setVoiceDeviceName(voiceDeviceModel.getVoiceDeviceName());
            tVoiceDevice.setStdDeviceId(voiceDeviceModel.getStdDeviceId());
            tVoiceDevice.setDeviceType(voiceDeviceModel.getDeviceType());
            tVoiceDevice.setConfigId(voiceDeviceModel.getConfigId());
            tVoiceDevice.setUpRegionId(stdRegionMap.get(voiceDeviceModel.getUpRegionId().toString()));
            tVoiceDevice.setState(voiceDeviceModel.getState());
            tVoiceDevice.setVoiceCode(voiceDeviceModel.getVoiceCode());
            tVoiceDevice.setVoiceType(voiceDeviceModel.getVoiceType());
            tVoiceDevice.setVoiceModel(voiceDeviceModel.getVoiceModel());
            tVoiceDevice.setVoiceFactory(voiceDeviceModel.getVoiceFactory());
            tVoiceDevice.setEdgeCode(edgeNode);
            tVoiceDevice.setOriginId(voiceDeviceModel.getPatroldeviceCode());
            return tVoiceDevice;
        }).collect(Collectors.toList());
        List<TVoiceDevice> oldVoiceDeviceList = tVoiceDeviceMapper.selectByEdgeCode(edgeNode);
        if (CollectionUtils.isNotEmpty(oldVoiceDeviceList)) {
            tVoiceDeviceMapper.insertBatch(tVoiceDeviceList);
        } else {
            Map<String, TVoiceDevice> oldTVoiceDeviceMap = oldVoiceDeviceList.stream().collect(Collectors.toMap(TVoiceDevice::getOriginId, Function.identity()));
            Map<String, TVoiceDevice> newTVoiceDeviceMap = tVoiceDeviceList.stream().collect(Collectors.toMap(TVoiceDevice::getOriginId, Function.identity()));
            // 更新的数据
            SetUtils.SetView<String> updateIdCollection = SetUtils.intersection(oldTVoiceDeviceMap.keySet(), newTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(updateIdCollection)) {
                tVoiceDeviceList.stream().filter(tCameraRecorder -> updateIdCollection.contains(tCameraRecorder.getOriginId())).forEach(tVoiceDevice -> {
                    TVoiceDevice oldVoiceDevice = oldTVoiceDeviceMap.get(tVoiceDevice.getOriginId());
                    tVoiceDevice.setVoiceDeviceId(oldVoiceDevice.getVoiceDeviceId());
                    tVoiceDeviceMapper.updateByPrimaryKey(tVoiceDevice);
                });
            }
            //删除的数据
            SetUtils.SetView<String> deleteIdCollection = SetUtils.difference(oldTVoiceDeviceMap.keySet(), newTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(deleteIdCollection)) {
                tVoiceDeviceMapper.deleteByEdgeCodeAndOriginId(edgeNode, deleteIdCollection);
            }
            //新增的数据
            SetUtils.SetView<String> insertIdCollection = SetUtils.difference(newTVoiceDeviceMap.keySet(), oldTVoiceDeviceMap.keySet());
            if (CollectionUtils.isNotEmpty(insertIdCollection)) {
                List<TVoiceDevice> insertVoiceDeviceList = tVoiceDeviceList.stream().filter(tVoiceDevice -> insertIdCollection.contains(tVoiceDevice.getOriginId())).collect(Collectors.toList());
                tVoiceDeviceMapper.insertBatch(insertVoiceDeviceList);
            }
        }
    }
}

