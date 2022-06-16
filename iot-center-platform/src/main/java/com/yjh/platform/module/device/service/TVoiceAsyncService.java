/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.concurrent.Future;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/6/16
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TVoiceAsyncService {

    @Async
    public Future<VoiceDeviceAllInfoDetail> syncVoiceState(VoiceDeviceAllInfoDetail item) {
        try {
            if(ping(item.getFtpUrl())){
                item.setState("在线");
            }else {
                item.setState("离线");
            }
        } catch (Exception e){
            log.error("获取状态失败：", e);
        }
        return new AsyncResult<>(item);
    }

    public boolean ping(String ipAddress) {
        try {
            int timeOut = 1500;  //超时应该在 1500ms 以上
            boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);
            // 当返回值是true时，说明host是可用的，false则不可。
            return status;
        }catch (Exception e){
            log.error("获取状态失败："+e);
        }
        return false;
    }

}
