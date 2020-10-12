package com.yjh.platform.common.quartz;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class CruiseTaskJob extends QuartzJobBean {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    TCameraPresetDao tCameraPresetDao;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeviceDataJob.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}";
    //http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}

    //    @Scheduled(fixedRate = 20000)

    /**
     * 巡视任务类
     * @param context
     */
    public void executeInternal(JobExecutionContext context) {
        try {
            log.info("正在进行定时任务");
            List<Long> instanceIdList = tCruiseTaskAttrDao.selectInstanceId(Constant.taskId);
            List<TCruisePointInstance> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);
            for (TCruisePointInstance item:instancesList) {
                if ("228".equals(item.getCruiseType())){
                    //todo 机器人
                }
                if ("229".equals(item.getCruiseType())){
                    //视频
                    //1.转到预置位
                    move(item.getCruiseId());
                    //2.抓图
                    TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(item.getCruiseId());
                    picture(tCameraPreset.getCameraId());
                }
                if ("230".equals(item.getCruiseType())){
                    //todo 红外
                }
                if ("231".equals(item.getCruiseType())){
                    //todo 在线监控
                }
                if ("232".equals(item.getCruiseType())){
                    //todo scala
                }
            }
        log.info("完成定时任务执行");
        } catch (Exception e) {
            log.error("定时任务异常" + e);
        }

    }
    //相机抓图
    private void picture(Long params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(PICTURE_URL, params, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    //相机转到预置位
    private void move(Long params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(MOVE_URL, params, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
