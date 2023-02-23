package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.dao.TCameraPresetMapper;
import com.yjh.accessrobot.module.command.entity.TCameraPreset;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private RobotService robotService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 处理相机预置位同步消息
     *
     * @param xmlBaseModel xmlBaseModel
     * @param receiveSessionId receiveSessionId
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCameraPresetInfo(XMLBaseModel xmlBaseModel, long receiveSessionId) {
        if (Constant.sendSessionId == receiveSessionId) {
            log.info("-------------这是刚发命令的响应{}-------------", receiveSessionId);
            if (!CollectionUtils.isEmpty(xmlBaseModel.getItems())) {
                xmlBaseModel.getItems().forEach(map -> {
                    processOnePreset(map);
                });
            }
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
        tCameraPreset.setPresetId((Long)presetInfoMap.get("presetId"));
        tCameraPreset.setPresetPtz(presetInfoMap.get("presetPtz").toString());
        tCameraPresetMapper.updatePtzByPrimaryKey(tCameraPreset);
    }

    /**
     * 更新预置位图片（从巡视主机拷贝覆盖到本地）
     *
     * @param presetInfoMap presetInfoMap
     */
    private void updatePresetImg(Map<String, Object> presetInfoMap) {
        TCameraPreset tCameraPreset = tCameraPresetMapper.selectByPrimaryKey((Long)presetInfoMap.get("presetId"));
        String devPath = getImageLocalPath(tCameraPreset.getPresetImg());
        String oriFtpsPath = presetInfoMap.get("presetImg").toString();

        robotService.copyFileToDevelop(oriFtpsPath, devPath);
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

        return presetImg.replaceAll(presetImgPath, presetRealImgPath);
    }

    public void sycPresetInfoToEdge(TCameraPreset tCameraPreset) {
        if (StringUtils.isNotEmpty(tCameraPreset.getEdgeCode())){
            // 边缘节点-任务下发
            boolean edgeStatus = robotService.checkEdgeStatus(tCameraPreset.getEdgeCode());
            if (edgeStatus) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            presetModeSwitch(tCameraPreset);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                };
                TaskExecutePool.getInstance().execute(runnable);
            }
        }
    }

    /**
     * 下发同步预置位信息的指令
     *
     * @param tCameraPreset 值
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    private void presetModeSwitch(TCameraPreset tCameraPreset) throws InterruptedException {
        List<Map<String, Object>> item = getPresetItem(tCameraPreset);
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
            .setSendCode(Constant.sendCode)
            .setReceiveCode(tCameraPreset.getEdgeCode())
            .setCode("1111")
            .setTime(DateTimeUtil.format(new Date()))
            .setType("1")
            .setCommand("5")
            .setItems(item);
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        log.info("生成的同步预置位信息xml是<start>{}<end>", xmlString);
        RobotServerHandler.send(robotService.generateByteOrder(xmlString, tCameraPreset.getEdgeCode()), tCameraPreset.getEdgeCode());
    }

    private List<Map<String, Object>> getPresetItem(TCameraPreset tCameraPreset) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("presetId", tCameraPreset.getPresetId());
        map.put("presetImg", tCameraPreset.getPresetImg());
        map.put("presetPtz", tCameraPreset.getPresetPtz());
        return Arrays.asList(map);
    }
}
