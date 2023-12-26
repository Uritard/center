package com.yjh.platform.module.iot.dao;

import com.yjh.platform.module.iot.entity.TIotDeviceWarn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import org.apache.ibatis.annotations.Param;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_warn(物联设备告警表)】的数据库操作Mapper
* @createDate 2023-12-19 17:58:29
* @Entity com.yjh.platform.module.iot.entity.TIotDeviceWarn
*/
public interface TIotDeviceWarnMapper extends BaseMapper<TIotDeviceWarn> {

    Long selectIdByIpAndNum(@Param(value = "ip") String ip, @Param(value = "channelNum") String channelNum);

    TIotDeviceWarn selectDeviceByIpAndNum(@Param(value = "ip") String ip, @Param(value = "channelNum") String channelNum);

    TWarnInfoDetail selectIotDeviceWarn(@Param(value = "warnId") Long warnId);
}




