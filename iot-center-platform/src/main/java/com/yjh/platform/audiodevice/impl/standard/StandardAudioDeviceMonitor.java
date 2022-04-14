package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.msg.ExtPeerState;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 设备在线状态监控
 * @author zilong
 * @date 2022/4/14
 */
@Slf4j
public class StandardAudioDeviceMonitor {

    public StandardAudioDeviceMonitor(ExecutorService executorService, RxBus bus) {
        bus.toFlowable(ExtPeerState.class)
                .observeOn(Schedulers.from(executorService))
                .subscribe(
                        this::onPeerState,
                        error -> log.warn("处理声纹设备消息出错", error)
                );
    }

    private void onPeerState(ExtPeerState peerState) {
        log.info("声纹设备({}){}线",
                peerState.getData().getClientContext().getSessionId(),
                peerState.getData().isConnected() ? "上" : "下"
        );
    }
}
