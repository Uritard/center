package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2023/02/07
 */
@Slf4j
@Service
public class SilentHandler {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IntelAnalysisService intelAnalysisService;
    @Autowired
    private TCameraPresetDao tCameraPresetDao;

    /**
     * 静默结果处理
     *
     * @param imgPath     图片ftp路径
     * @param presetId    预置位id
     */
    public void silentHandler(String imgPath,String presetId){

        //将图片copy到resultImg下面
        String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
        String resultImg = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));

        // 文件路径
        String temporaryFilePath = ftpsFilePath + "/" + imgPath;
        log.info("temporaryFilePath==={}", temporaryFilePath);
        String tarPath = resultImg+"jm/"+presetId+"_"+System.currentTimeMillis()+".jpg";

        FileUtil.copyFileUsingStream(temporaryFilePath,tarPath);
        // 调用算法接口分析结果
        List<Analysis> analysisList = new ArrayList<>();
        TCameraPreset tCameraPreset = tCameraPresetDao.selectIsDownSystemPreset(Long.valueOf(presetId));

        Analysis analysis = new Analysis()
                // 暂定静默监视识别类型为12,没有实际意义
                .setAnalyseType("12")
                .setInstanceId(tCameraPreset.getPresetId())
                .setTaskId("jm")
                .setPicPath(tarPath);
        analysisList.add(analysis);
        List<Response> responseList= intelAnalysisService.picAnalyseNoDetection(analysisList);
        log.info("param:{} result:{}", StringUtils.join(analysisList),StringUtils.join( responseList));
    }
}
