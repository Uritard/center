package com.yjh.video.core.gb28181.task.impl;

import com.yjh.video.core.gb28181.task.IMobilePositionSubscribeSend;
import com.yjh.video.core.gb28181.task.ISubscribeTask;
import com.yjh.video.core.utils.SpringBeanFactory;

/**
 * 向已经订阅(移动位置)的上级发送MobilePosition消息
 * @author lin
 */
public class MobilePositionSubscribeHandlerTask implements ISubscribeTask {


    private IMobilePositionSubscribeSend positionSubscribeSend;
    private String platformId;


    public MobilePositionSubscribeHandlerTask(String platformId) {
        this.positionSubscribeSend = SpringBeanFactory.getBean("platformServiceImpl");
        this.platformId = platformId;
    }

    @Override
    public void run() {
        positionSubscribeSend.sendNotifyMobilePosition(this.platformId);
    }

    @Override
    public void stop() {

    }
}
