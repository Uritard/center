package com.yjh.platform.audiodevice.impl.standard;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.audiodevice.impl.standard.tcp.InboundMessage;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 录音数据分发
 *
 * @author zilong
 * @date 2022/4/14
 */
@Slf4j
public class AudioDataDispatcher {

    private final AudioDeviceManager audioDeviceManager;

    public AudioDataDispatcher(ExecutorService executorService, RxBus bus, AudioDeviceManager audioDeviceManager) {
        this.audioDeviceManager = audioDeviceManager;
        bus.toFlowable(InboundMessage.class)
                .observeOn(Schedulers.from(executorService))
                .subscribe(
                        this::onAudioMessage,
                        error -> log.warn("处理声纹设备消息出错", error)
                );
    }

    private void onAudioMessage(InboundMessage inboundMessage) {
        log.info("收到声纹设备({})的录音数据", inboundMessage.getDeviceId());
        AudioDevice audioDevice = audioDeviceManager.getAudioDevice(inboundMessage.getDeviceId());
        if (audioDevice == null) {
            log.warn("声纹设备还未注册({})", inboundMessage.getDeviceId());
        }
        if (audioDevice instanceof StandardAudioDevice) {
            StandardAudioDevice standardAudioDevice = (StandardAudioDevice) audioDevice;
            standardAudioDevice.onAudioData(inboundMessage);
        }
    }

}
