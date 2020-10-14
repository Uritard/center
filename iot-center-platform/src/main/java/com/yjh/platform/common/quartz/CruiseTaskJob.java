package com.yjh.platform.common.quartz;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.TCruiseTaskAttr;
import com.yjh.platform.module.task.entity.TCruiseTaskDel;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    TCruiseTaskDelDao tCruiseTaskDelDao;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeviceDataJob.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";
    //http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}

    //    @Scheduled(fixedRate = 20000)

    /**
     * 巡视任务类
     * @param context
     */
    public void executeInternal(JobExecutionContext context) {
        //判断任务是否需要执行
        String taskId = context.getMergedJobDataMap().getString("taskId");
        SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
        String taskDate = simpleDateFormat.format(context.getFireTime());
        Date date = null;
        try {
            date = simpleDateFormat.parse(taskDate);
        } catch (Exception e) { e.getMessage(); }
        List<TCruiseTaskDel> tCruiseTaskDelList =tCruiseTaskDelDao.select(taskId,date,null);

        if(tCruiseTaskDelList != null && tCruiseTaskDelList.size()>0){
            //不需要执行任务
            log.info(taskDate+"此时间任务不需要执行");
        }else {
            log.info(taskDate+"需要执行的任务");
            try {
                log.info("正在进行任务");
                log.info(context.getJobInstance().toString());
                List<Long> instanceIdList = tCruiseTaskAttrDao.selectInstanceId(taskId);
                List<TCruisePointInstance> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);
                for (TCruisePointInstance item : instancesList) {
                    //一次循环 一个巡检点
                    if (228 == item.getCruiseType()) {
                        //todo 机器人
                    }
                    if (229 == item.getCruiseType()) {
                        //视频
                        TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(item.getCruiseId());
                        //1.转到预置位
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("presetId", item.getCruiseId());
                        map.put("cameraId", tCameraPreset.getCameraId());
                        log.info(map.toString());
                        move(map);
                        //2.抓图
                        HashMap<String, Object> map2 = new HashMap<>();
                        map2.put("cameraId", tCameraPreset.getCameraId());
                        picture(map2);
                    }
                    if (230 == item.getCruiseType()) {
                        //todo 红外
                    }
                    if (231 == item.getCruiseType()) {
                        //todo 在线监控
                    }
                    if (232 == item.getCruiseType()) {
                        //todo scala
                    }
                }
                //todo 任务完成，生成结果
                log.info("完成任务执行");
            } catch (Exception e) {
                log.error("定时任务异常" + e);
            }
        }

    }
    //相机抓图
    private void picture(HashMap map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(PICTURE_URL, String.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    //相机转到预置位
    private void move(HashMap<String,Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
