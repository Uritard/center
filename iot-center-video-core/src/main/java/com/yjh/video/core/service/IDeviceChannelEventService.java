package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.bean.Device;
import com.yjh.video.core.gb28181.bean.DeviceChannel;
import com.yjh.video.core.utils.Coordtransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 国标通道业务类
 * @author lin
 */
public interface IDeviceChannelEventService {
    Logger logger = LoggerFactory.getLogger(IDeviceChannelEventService.class);

    /**
     * 更新gps信息
     */
    default DeviceChannel updateGps(DeviceChannel deviceChannel, Device device) {
        if (deviceChannel.getLongitude()*deviceChannel.getLatitude() > 0) {
            if (device == null) {
                logger.error("device is null");
            }

            if ("WGS84".equals(device.getGeoCoordSys())) {
                deviceChannel.setLongitudeWgs84(deviceChannel.getLongitude());
                deviceChannel.setLatitudeWgs84(deviceChannel.getLatitude());
                Double[] position = Coordtransform.WGS84ToGCJ02(deviceChannel.getLongitude(), deviceChannel.getLatitude());
                deviceChannel.setLongitudeGcj02(position[0]);
                deviceChannel.setLatitudeGcj02(position[1]);
            }else if ("GCJ02".equals(device.getGeoCoordSys())) {
                deviceChannel.setLongitudeGcj02(deviceChannel.getLongitude());
                deviceChannel.setLatitudeGcj02(deviceChannel.getLatitude());
                Double[] position = Coordtransform.GCJ02ToWGS84(deviceChannel.getLongitude(), deviceChannel.getLatitude());
                deviceChannel.setLongitudeWgs84(position[0]);
                deviceChannel.setLatitudeWgs84(position[1]);
            }else {
                deviceChannel.setLongitudeGcj02(0.00);
                deviceChannel.setLatitudeGcj02(0.00);
                deviceChannel.setLongitudeWgs84(0.00);
                deviceChannel.setLatitudeWgs84(0.00);
            }
        }else {
            deviceChannel.setLongitudeGcj02(deviceChannel.getLongitude());
            deviceChannel.setLatitudeGcj02(deviceChannel.getLatitude());
            deviceChannel.setLongitudeWgs84(deviceChannel.getLongitude());
            deviceChannel.setLatitudeWgs84(deviceChannel.getLatitude());
        }
        return deviceChannel;
    }


    /**
     * 添加设备通道
     *
     * @param deviceId 设备id
     * @param channel 通道
     */
    void updateChannel(String deviceId, DeviceChannel channel);

    /**
     * 批量添加设备通道
     *
     * @param deviceId 设备id
     * @param channels 多个通道
     */
    int updateChannels(String deviceId, List<DeviceChannel> channels);

}
