package com.yjh.platform.configuration;

import com.yjh.commons.NamedThreadFactory;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.socket.BaseSocketServer;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.audiodevice.impl.AudioDeviceFactory;
import com.yjh.platform.audiodevice.impl.AudioDeviceManagerImpl;
import com.yjh.platform.audiodevice.impl.standard.AudioDataDispatcher;
import com.yjh.platform.audiodevice.impl.standard.StandardAudioDevice;
import com.yjh.platform.audiodevice.impl.standard.StandardAudioDeviceMonitor;
import com.yjh.platform.audiodevice.impl.standard.tcp.MessageCodec;
import com.yjh.platform.audiodevice.impl.standard.tcp.PacketCodecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 声纹设备功能组装
 *
 * @author zilong
 * @date 2022/4/14
 */
@Configuration
public class AudioDeviceConfig {
    @Value("${audio.tcp.server.port}")
    private int port;

    @Bean
    public RxBus serverRxBus() {
        return new RxBus();
    }

    @Bean
    public ExecutorService serverOutboundExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("serverOutboundExecutor"));
    }

    @Bean
    public ExecutorService serverMsgProcessingExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("serverMsgProcessingExecutor"));
    }

    @Bean
    public MsgChannel serverMsgChannel(ExecutorService serverOutboundExecutor, RxBus serverRxBus) {
        return new RxBusMsgChannel(
                serverRxBus,
                msg -> msg instanceof Msg.Outbound,
                new MessageCodec(),
                serverOutboundExecutor
        );
    }

    @Bean
    public BaseSocketServer socketServer(MsgChannel serverMsgChannel) {
        return new BaseSocketServer("0.0.0.0", port, serverMsgChannel, new PacketCodecFactory()
        );
    }

    @Bean
    public AudioDeviceManager audioDeviceManager() {
        //TODO: 调试目的
        AudioDeviceManager audioDeviceManager = new AudioDeviceManagerImpl();
        audioDeviceManager.registerAudioDevice("AQIDBAUG", new StandardAudioDevice());
        return audioDeviceManager;
    }

    @Bean
    public AudioDataDispatcher audioDataDispatcher(
            ExecutorService serverMsgProcessingExecutor,
            RxBus serverRxBus,
            AudioDeviceManager audioDeviceManager
    ) {
        return new AudioDataDispatcher(serverMsgProcessingExecutor, serverRxBus, audioDeviceManager);
    }

    @Bean
    public StandardAudioDeviceMonitor standardAudioDeviceMonitor(
            ExecutorService serverMsgProcessingExecutor,
            RxBus serverRxBus) {
        return new StandardAudioDeviceMonitor(serverMsgProcessingExecutor, serverRxBus);
    }
}
