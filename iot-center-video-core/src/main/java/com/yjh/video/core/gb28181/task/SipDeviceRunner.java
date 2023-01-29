package com.yjh.video.core.gb28181.task;

import com.yjh.video.core.config.UserSetting;
import com.yjh.video.core.gb28181.bean.Device;
import com.yjh.video.core.service.IDeviceEventService;
import com.yjh.video.core.storager.IRedisCatchStorage;
import com.yjh.video.core.storager.IVideoManagerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 系统启动时控制设备
 * @author lin
 */
@Component
@Order(value=4)
public class SipDeviceRunner implements CommandLineRunner {

    @Autowired
    private IVideoManagerStorage storager;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private IDeviceEventService deviceService;

    @Override
    public void run(String... args) throws Exception {
        List<Device> deviceList = deviceService.getAllOnlineDevice();

        for (Device device : deviceList) {
            if (deviceService.expire(device)){
                deviceService.offline(device.getDeviceId());
            }else {
                deviceService.online(device);
            }
        }
        // 重置cseq计数
        redisCatchStorage.resetAllCSEQ();
    }
}
