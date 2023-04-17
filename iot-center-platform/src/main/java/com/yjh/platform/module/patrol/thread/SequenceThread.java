package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author hyh
 * 顺控文件处理
 * @since 2022/8/25
 **/
@Slf4j
public class SequenceThread implements Runnable {

    private final RedisTemplate redisTemplate;
    private final String meteId;
    private final UPatrolTaskService uPatrolTaskService;
    private final String filePath;
    private final ApplicationProperties applicationProperties;
    private final IntelAnalysisService intelAnalysisService;

    public SequenceThread(RedisTemplate redisTemplate, String meteId, String filePath) {
        this.redisTemplate = redisTemplate;
        this.meteId = meteId;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
        this.filePath = filePath;
        this.applicationProperties = StaticContextAccessor.getBean(ApplicationProperties.class);
        this.intelAnalysisService = StaticContextAccessor.getBean(IntelAnalysisService.class);
    }

    @Override
    public void run() {
        try {
            Map<String, Object> map = uPatrolTaskService.selectForSequenceInfoByMeteId(meteId).get(0);
            String imgPath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content")+"/"+filePath;

            String[] str = filePath.split("/");
            String ftpFileName = str[str.length-1];
            String resultImagePath = redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content") + ftpFileName;
            log.info("算法指定的路径为：{}", resultImagePath);

            log.info("imgPath:{} resultImagePath:{}",imgPath,resultImagePath);
            FileUtil.copyFileUsingStream(imgPath, resultImagePath);
            Analysis analysis = new Analysis();
            analysis.setAnalyseType("6");
            analysis.setInstanceId(Long.valueOf(map.get("cfgDeviceId").toString()));
            analysis.setIsAi(1);
            analysis.setTaskId("yjsk#meteId="+map.get("cfgDeviceId"));
            analysis.setPicPath(resultImagePath);
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            intelAnalysisService.picAnalyseNoDetection(analysisList);
        } catch (Exception e) {
            log.error("一键顺控-变位信号-调用算法识别主机失败", e);
        }
    }

    /**
     * 将文件上传至巡视主机ftp服务器
     *
     * @param sourcePath 源文件地址
     * @param targetPathName 目标文件地址名称
     */
    public void uploadFileToFtps(String sourcePath, String targetPathName, ApplicationProperties.FtpsConfig ftpsConfig) {
        try {
            if(StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPathName)) {return;}
            FtpsUtil.putFile(sourcePath, targetPathName, ftpsConfig.getIp(), ftpsConfig.getPort(), ftpsConfig.getUserName(), ftpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至巡视主机ftp服务器错误:{}", e);
        }
    }
}
