package com.yjh.platform.module.user.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.TCameraScreenDao;
import com.yjh.platform.module.user.dao.TWiringConfigDao;
import com.yjh.platform.module.user.entity.TWiringConfig;
import com.yjh.platform.module.user.entity.TWiringConfigVo;
import com.yjh.platform.module.video.controller.CameraConController;
import com.yjh.platform.module.video.service.CameraConService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 丫C
 * @since 2023-06-19
 */
@Service
public class TWiringConfigService {

    private final TWiringConfigDao tWiringConfigDao;
    private final TStdRegionDao tStdRegionDao;
    private final RedisTemplate<String, ?> redisTemplate;
    private final TCameraScreenDao tCameraScreenDao;
    private final CameraConService cameraConService;

    public TWiringConfigService(TWiringConfigDao tWiringConfigDao, TStdRegionDao tStdRegionDao, RedisTemplate<String, ?> redisTemplate,
                                TCameraScreenDao tCameraScreenDao, CameraConService cameraConService) {
        this.tWiringConfigDao = tWiringConfigDao;
        this.tStdRegionDao = tStdRegionDao;
        this.redisTemplate = redisTemplate;
        this.tCameraScreenDao = tCameraScreenDao;
        this.cameraConService = cameraConService;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insert(List<TWiringConfigVo> tWiringConfigList) {
        return this.tWiringConfigDao.insert(tWiringConfigList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long wiringConfigId) {
        return this.tWiringConfigDao.deleteByPrimaryId(wiringConfigId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByWiringDiagramId(Integer wiringDiagramId) {
        return this.tWiringConfigDao.deleteByWiringDiagramId(wiringDiagramId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(List<TWiringConfig> tWiringConfigList, Long userId) {
        String userName = (String) redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
        tWiringConfigList.forEach(tWiringConfig ->
                tWiringConfig.setUpdateTime(new Date()).setUpdatePerson(userName));
        return this.tWiringConfigDao.update(tWiringConfigList);
    }

    @Transactional(rollbackFor = Exception.class)
    public TWiringConfig selectByPrimaryId(Long wiringConfigId) {
        return this.tWiringConfigDao.selectByPrimaryId(wiringConfigId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TWiringConfigVo> selectByCondition(Long regionId) {
        if (-1 == regionId) {
            TStdRegion tStdRegion = tStdRegionDao.selectRootRegion();
            regionId = tStdRegion.getRegionId();
        }
        List<TWiringConfigVo> tWiringConfigVos = tWiringConfigDao.selectByCondition(regionId);
        DictConvertUtil.DictOptional optional = DictConvertUtil.optionalAliasColName("cruiseType", "equipmentType");
        DictConvertUtil.DICT.covertToDict(tWiringConfigVos, optional);

        // 在线状态查询  返回的map包含在线/离线的  不包含未知状态的
        Map<String,String> statusMap = new HashMap<>(8);
        List<Long> recordIdList = tCameraScreenDao.selectRecordId();
        for(Long recordId:recordIdList){
            Map<String, String> recordIdMap = cameraConService.getCameraStatus(recordId);
            if(recordIdMap == null){
                continue;
            }
            statusMap.putAll(recordIdMap);
        }

        tWiringConfigVos.forEach(tWiringConfigVo ->
            tWiringConfigVo.setOnlineState(NumberUtils.toInt(statusMap.getOrDefault(tWiringConfigVo.getEquipmentId().toString(), "0")))
        );
        return tWiringConfigVos;
    }
}
