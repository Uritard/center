package com.yjh.accessvideo.hik.handler;

import com.sun.jna.Pointer;
import com.yjh.accessvideo.hik.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 丫C
 * @date 2023/4/23
 */
@Slf4j
public class FLoginResultCallBackS implements HCNetSDK.FLoginResultCallBack {
    @Override
    public int invoke(int lUserID, int dwResult, HCNetSDK.NET_DVR_DEVICEINFO_V30 lpDeviceinfo, Pointer pUser) {
        log.info("lpDeviceinfo：{}", lpDeviceinfo);
        return 0;
    }
}
