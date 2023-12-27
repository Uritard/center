package com.yjh.platform.module.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.module.iot.dao.TIotDevicePointMapper;
import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.yjh.platform.module.iot.service.TIotDevicePointService;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_point(物联设备测点表)】的数据库操作Service实现
* @createDate 2023-11-28 14:48:41
*/
@Service
@Slf4j
public class TIotDevicePointServiceImpl extends ServiceImpl<TIotDevicePointMapper, TIotDevicePoint>
    implements TIotDevicePointService{

    @Override
    public List<TIotDevicePoint> listByDeviceId(Long deviceId) {
        QueryWrapper<TIotDevicePoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iot_device_id", deviceId);
        return list(queryWrapper);
    }
}




