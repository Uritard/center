package com.yjh.platform.module.device.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Repository
public interface TCfgDeviceDao {

    int insert(TCfgDevice tCfgDevice);
    int deleteByPrimaryId(@Param(value = "deviceId") String deviceId);
    int update(TCfgDevice tCfgDevice);
    TCfgDevice selectByPrimaryId(@Param(value = "deviceId") String deviceId);
    List<TCfgDevice> select(@Param(value = "deviceId") String deviceId,
                                @Param(value = "deviceName") String deviceName,
                                @Param(value = "deviceType") String deviceType,
                                @Param(value = "deviceCode") String deviceCode,
                                @Param(value = "stationId") Long stationId,
                                @Param(value = "relationCode") String relationCode,
                                @Param(value = "createTime") Date createTime,
                                @Param(value = "updateTime") Date updateTime,
                                @Param(value = "remark") String remark);
    List<HashMap<String,Object>> selectByPage(TCfgDeviceDetail tCfgDeviceDetail);

    int batchInsert(List<TCfgDevice> list);

    //四摇
    int insertIntoTeleadjust(TCfgTeleadjust tCfgTeleadjust);
    int insertIntoTelecontrol(TCfgTelecontrol tCfgTelecontrol);
    int insertIntoTelemeter(TCfgTelemeter tCfgTelemeter);
    int insertIntoTelesignal(TCfgTelesignal tCfgTelesignal);

}
