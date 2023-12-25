package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.iot.entity.TIotDeviceExtend;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import com.yjh.platform.module.iot.dao.TIotDevicePointMapper;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_point(物联设备测点表)】的数据库操作Service实现
* @createDate 2023-11-28 14:48:41
*/
@Service
@Slf4j
public class TIotDevicePointServiceImpl extends ServiceImpl<TIotDevicePointMapper, TIotDevicePoint>
    implements TIotDevicePointService{

    @Autowired
    @Qualifier("serviceRestTemplate")
    private RestTemplate serviceRestTemplate;

    @Override
    public boolean save(TIotDevicePoint devicePoint) {
        boolean ret = super.save(devicePoint);

        // 8 分钟后将设备加入到采集列表中，并触发一次采集，若8分钟内添加多个通道，以最后一次添加为准
        String scheduleTaskId = "IotDevice_" + devicePoint.getIotDeviceId();
        ScheduledMapConfig.schedule(scheduleTaskId, 8*60, devicePoint, (device)->{
            Result result = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/add?id={0}", Result.class, devicePoint.getIotDeviceId());
            log.info("add device collect, {}", result);
        });

        return ret;
    }
}




