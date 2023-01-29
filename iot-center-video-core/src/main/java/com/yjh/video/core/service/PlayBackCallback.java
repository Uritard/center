package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.transmit.callback.RequestMessage;
import com.yjh.video.core.service.bean.PlayBackResult;

public interface PlayBackCallback {

    void call(PlayBackResult<RequestMessage> msg);

}
