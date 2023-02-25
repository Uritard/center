package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.module.device.dao.TCameraPresetMapper;
import com.yjh.accesstcp.module.device.entity.TCameraPreset;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.utils.FtpsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/10
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TCameraPresetService {

    @Autowired
    private TCameraPresetMapper tCameraPresetMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FtpsUtil ftpsUtil;

    /**
     * 处理相机预置位同步消息
     *
     * @param xmlBaseModel xmlBaseModel
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCameraPresetInfo(XMLBaseModel xmlBaseModel) {
        if (!CollectionUtils.isEmpty(xmlBaseModel.getItems())) {
            xmlBaseModel.getItems().forEach(map -> {
                processOnePreset(map);
            });
        }
    }

    /**
     * 处理单个与之位
     *
     * @param presetInfoMap presetInfoMap
     */
    private void processOnePreset(Map<String, Object> presetInfoMap) {
        try {
            updatePresetPtz(presetInfoMap);
            updatePresetImg(presetInfoMap);
        } catch (Exception e) {
            log.error("processOnePreset err", e);
        }
    }

    /**
     * 更新PresetPtz字段
     *
     * @param presetInfoMap presetInfoMap
     */
    private void updatePresetPtz(Map<String, Object> presetInfoMap) {
        TCameraPreset tCameraPreset = new TCameraPreset();
        tCameraPreset.setPresetId(Long.parseLong(presetInfoMap.get("presetId").toString()));
        tCameraPreset.setPresetPtz(presetInfoMap.get("presetPtz").toString());
        tCameraPresetMapper.updatePtzByPrimaryKey(tCameraPreset);
    }

    /**
     * 更新预置位图片（从巡视主机拷贝覆盖到本地）
     *
     * @param presetInfoMap presetInfoMap
     */
    private void updatePresetImg(Map<String, Object> presetInfoMap) throws NoSuchAlgorithmException {
        TCameraPreset tCameraPreset = tCameraPresetMapper.selectByPrimaryKey(Long.parseLong(presetInfoMap.get("presetId").toString()));
        String devPath = getImageLocalPath(tCameraPreset.getPresetImg());
        String oriFtpsPath = presetInfoMap.get("presetImg").toString();

        ftpsUtil.downloadFile(devPath, oriFtpsPath);
    }

    /**
     * 将presetImg路劲从ftps路径变成绝对路径
     *
     * @param presetImg presetImg
     * @return result
     */
    private String getImageLocalPath(String presetImg) {
        // /home/yjh_iot_center/iot-picture/specimens
        String presetRealImgPath = (String) redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath", "content");
        // https://172.24.39.9/imgs/specimens
        String presetImgPath = (String) redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");

        return presetImg.replaceAll(presetRealImgPath, presetImgPath);
    }
}
