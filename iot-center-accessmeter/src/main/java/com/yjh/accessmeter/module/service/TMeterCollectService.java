package com.yjh.accessmeter.module.service;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.module.dao.TMeterDao;
import com.yjh.accessmeter.module.dao.TMeterLogDao;
import com.yjh.accessmeter.module.device.entity.TMeter;
import com.yjh.accessmeter.netty.DLT645Decoder;
import com.yjh.accessmeter.netty.DLT645Encoder;
import com.yjh.accessmeter.netty.DLT645Message;
import com.yjh.accessmeter.netty.DLT645MessgeHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TMeterCollectService {

    private Bootstrap bootstrap;

    public static Map<Long, Channel> map = new ConcurrentHashMap<>();
    @Autowired
    private ThreadPoolTaskScheduler asyncExecutor;
    @Resource
    private TMeterDao tMeterDao;
    @Resource
    private TMeterLogDao tMeterLogDao;

    public void add(Long id) {
        TMeter tMeter = tMeterDao.selectByPrimaryKey(id);
        if (tMeter == null) {
            log.info("没有查到电表配置 id:{}", id);
            return;
        }
        if (map.containsKey(id)) {
            log.info("该电表已经连接 id:{}", id);
            return;
        }
        asyncExecutor.execute(() -> initMeter(tMeter));
    }

    public void delete(Long id) {
        Channel channel = map.get(id);
        if (channel == null) {
            log.info("与该电表未连接 id:{}", id);
            return;
        }
        channel.close();
        map.remove(id);
        log.info("已删除电表 id:{}", id);
    }

    public void collect(Long id) {
        Channel channel = map.get(id);
        if (channel == null) {
            log.error("与该电表未连接 id:{}", id);
        } else {
            sendCollectMsg(channel);
        }
    }

    private void sendCollectMsg(Channel channel) {
        TMeter tMeter = channel.attr(Constant.tMeterAttributeKey).get();
        try {
            DLT645Message dlt645Message = new DLT645Message();
            dlt645Message.setControlCode(Constant.CONTROLL_CODE_REQUEST);
            dlt645Message.setAddress(tMeter.getAddress());
            dlt645Message.setData(Constant.DATA_TYPE_POSITVICE_POWER_TOTAL);
            channel.writeAndFlush(dlt645Message);
            Thread.sleep(500);
            dlt645Message.setData(Constant.DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL);
            channel.writeAndFlush(dlt645Message);
            Thread.sleep(500);
            dlt645Message.setData(Constant.DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL);
            channel.writeAndFlush(dlt645Message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void init() {
        log.info("加载电表配置开始");
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).
                    option(ChannelOption.SO_KEEPALIVE, true).
                    option(ChannelOption.TCP_NODELAY, true).
                    option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT).
                    option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).
                    handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new DLT645Decoder(),
                                    new DLT645Encoder(),
                                    new DLT645MessgeHandler(tMeterDao, tMeterLogDao));
                        }
                    });
            List<TMeter> tMeterList = tMeterDao.selectAll();
            if (CollectionUtils.isEmpty(tMeterList)) {
                log.info("没有查到电表配置");
                return;
            }
            for (TMeter tMeter : tMeterList) {
                asyncExecutor.execute(() -> initMeter(tMeter));
            }
        } catch (Exception e) {
            log.error("启动异常", e);
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Scheduled(cron = "${accessmeter.collectdata.cron}")
    public void collectDataTask() {
        log.info("开始采集所有电表电量");
        for (Channel channel : map.values()) {
            sendCollectMsg(channel);
        }
    }

    public void initMeter(TMeter tMeter) {
        log.info("开始连接电表 tMeter：{}", tMeter);
        ChannelFuture channelFuture = bootstrap.connect(tMeter.getIp(), tMeter.getPort());
        channelFuture.channel().attr(Constant.tMeterAttributeKey).set(tMeter);
    }
}
