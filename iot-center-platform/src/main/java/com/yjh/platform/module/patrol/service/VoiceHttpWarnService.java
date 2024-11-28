package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.patrol.entity.TStdDeviceMete;
import com.yjh.platform.module.task.dao.TWarnInfoDao;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.entity.VoiceAlarm;
import com.yjh.platform.module.task.entity.VoiceInstance;
import com.yjh.platform.module.task.entity.XMLBaseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/11/7
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceHttpWarnService {

    private final TWarnInfoDao tWarnInfoDao;

    private final PatrolResultHandler patrolResultHandler;

    private final AutoreviewHandler autoreviewHandler;

    private final AnalyseDataOperateService analyseDataOperateService;

    public int alarm(VoiceAlarm voiceAlarm) {
        //根据hcCode查询声纹相关点位信息，构建告警数据信息
        VoiceInstance voiceInstance = tWarnInfoDao.selectInstanceByVoiceCode(voiceAlarm.getHcCode());
        if (Objects.isNull(voiceInstance)) {
            log.info("未查到 {} 声纹设备的测点信息！", voiceAlarm.getHcCode());
            return 0;
        }
        String parentPath = FileUtil.concatPath(SysParamConfig.getSysContent("resultImgPath"), "resultImg/silentTask/");
        String filePath = FileUtil.getFilePath(parentPath, voiceAlarm.getHcCode(), "wav");
        try {
            File file = new File(filePath);
            URL url = new URL(voiceAlarm.getRawDataUrl());
            FileUtils.copyURLToFile(url, file);
        } catch (IOException e) {
            log.error("声纹文件下载失败", e);
        }
        String resultRealImg = filePath.replaceAll(SysParamConfig.getSysContent("resultImgPath"),
                Constant.RESULT_IMG_REAL_PATH);
        voiceInstance.setImagePath(resultRealImg);
        voiceInstance.setFilePath(filePath);
        return voiceWarnHandler(voiceInstance, voiceAlarm);
    }

    /**
     * 土星声纹厂家上报声纹处理
     */
    public int voiceWarnHandler(VoiceInstance voiceInstance, VoiceAlarm voiceAlarm) {
        int result = 0;
        try {
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setWarnTime(voiceAlarm.getTimestamp());
            tWarnInfo.setWarnType(501);
            tWarnInfo.setConfMode(276);
            tWarnInfo.setDefectModel(450);
            tWarnInfo.setAlarmSource(998);
            tWarnInfo.setWarnLevel(132);
            tWarnInfo.setWarnName("声纹告警数据");
            tWarnInfo.setWarnContent(voiceAlarm.getAlarmDesc());
            tWarnInfo.setDeviceId(voiceInstance.getDeviceId());
            tWarnInfo.setCunstomId(voiceInstance.getCustomId());
            tWarnInfo.setInstanceId(voiceInstance.getInstanceId());
            tWarnInfo.setStdMeteId(voiceInstance.getDeviceMeteId());
            tWarnInfo.setDeviceName(voiceInstance.getDeviceName());
            tWarnInfo.setDeviceMeteName(voiceInstance.getMeteName());
            tWarnInfo.setImagePath(voiceInstance.getImagePath());
            //自动审核判断
            autoreviewHandler.autoreviewCheckAlarm(tWarnInfo);
            result = tWarnInfoDao.insert(tWarnInfo);
            ThreadPoolUtil.PATROL_POOL.addThread(() -> {
                Map<String, String> infoMap = new HashMap<>(8);
                infoMap.put("alarmLevel", "132");
                infoMap.put("defectModel", "450");
                infoMap.put("warnId", String.valueOf(tWarnInfo.getWarnId()));
                TStdDeviceMete tStdDevicemete = new TStdDeviceMete();
                tStdDevicemete.setDeviceMeteId(voiceInstance.getDeviceMeteId());
                patrolResultHandler.alarmPopUp(tStdDevicemete, infoMap);
                // 告警上报上一级系统
                alarmToUpSystem(tWarnInfo, voiceInstance);
            });
        } catch (Exception e) {
            log.error("声纹告警处理结果异常：", e);
        }
        return result;
    }

    private void alarmToUpSystem(TWarnInfo tWarnInfo, VoiceInstance voiceInstance) {
        String warnTime = DateTimeUtil.format(tWarnInfo.getWarnTime());
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> xmlItem = new HashMap<>(16);
        xmlBaseModel.setType("63");
        xmlItem.put("patroldevice_code", voiceInstance.getVoiceCode());
        xmlItem.put("patroldevice_name", voiceInstance.getVoiceDeviceName());
        xmlItem.put("alarm_level", "2");

        try {
            xmlItem.put("monitor_type", "");
            //音频文件
            xmlItem.put("file_type", "3");
            String edgeCode = SysParamConfig.getSysContent("edgeId");

            String timeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String ftpsTarPath = edgeCode + "/voice/" + timeFormat.substring(0, 4)
                    + "/" + timeFormat.substring(4, 6) + "/" + timeFormat.substring(6, 8)
                    + "/" + voiceInstance.getVoiceCode() + "_" + System.currentTimeMillis() + ".wav";
            analyseDataOperateService.uploadFileToUpFtps(voiceInstance.getFilePath(), ftpsTarPath);
            xmlItem.put("file_path", ftpsTarPath);
            xmlItem.put("time", warnTime);
            xmlItem.put("content", tWarnInfo.getWarnContent());
            xmlItem.put("origin_id", tWarnInfo.getWarnId());
            xmlItems.add(xmlItem);
            xmlBaseModel.setItems(xmlItems);
            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> alarmMap = new HashMap<>(3);
            alarmMap.put("list", list);
            log.info("土星http声纹告警上报: {}", com.alibaba.fastjson.JSON.toJSONString(alarmMap));
            Constant.otherServer(alarmMap, Constant.TCP_URL);
        } catch (Exception e) {
            log.error("向上级系统上报土星http声纹告警出错: ", e);
        }
    }
}
