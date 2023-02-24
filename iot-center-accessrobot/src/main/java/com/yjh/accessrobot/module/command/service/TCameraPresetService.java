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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private RobotService robotService;

    @Autowired
    private RedisTemplate redisTemplate;

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
            .setType("2023")
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
