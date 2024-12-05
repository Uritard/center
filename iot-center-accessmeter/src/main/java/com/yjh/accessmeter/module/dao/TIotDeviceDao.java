/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.dao;

import com.yjh.accessmeter.module.device.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface TIotDeviceDao {

    List<IotDevice> selectAll();

    IotDevice selectByPrimaryKey(Long id);

    List<IotDevicePoint> selectPointByDeviceId(@Param(value = "deviceId") Long deviceId);

    int insertData(IotDeviceData deviceData);

    int batchInsertData(@Param("list") List<IotDeviceDataEx> deviceDataList);

    String getSystemConfig(@Param("configType") String type, @Param("configKey") String key);

    List<IotDevice> selectAllMeter();

    List<IotDeviceDataEx> selectByIpAndAddress(@Param("ip") String ip,@Param("address") String address);
    List<String> selectGatewayIdByIp(@Param("ip") String ip);
    int banchInsertLinkageConfig(@Param("list") List<LinkageConfig> list);
    int deleteLinkageConfigByAddress(@Param("address") String address);

    List<IotDevice> selectDeviceByIp(@Param("ip") String ip,@Param("protocolModel") String protocolModel);

    String selectDataByType(@Param("iotDeviceId") Long iotDeviceId,
        @Param("pointId") Long pointId);
}
