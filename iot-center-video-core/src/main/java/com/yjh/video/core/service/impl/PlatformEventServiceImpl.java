/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service.impl;

import com.yjh.video.core.Constant;
import com.yjh.video.core.config.DynamicTask;
import com.yjh.video.core.config.UserSetting;
import com.yjh.video.core.gb28181.bean.ParentPlatform;
import com.yjh.video.core.gb28181.bean.ParentPlatformCatch;
import com.yjh.video.core.gb28181.bean.SendRtpItem;
import com.yjh.video.core.gb28181.bean.SubscribeHolder;
import com.yjh.video.core.gb28181.event.SipSubscribe;
import com.yjh.video.core.gb28181.transmit.cmd.impl.SIPCommanderFroPlatform;
import com.yjh.video.core.media.zlm.ZLMRTPServerFactory;
import com.yjh.video.core.media.zlm.dto.MediaServerItem;
import com.yjh.video.core.service.IMediaServerEventService;
import com.yjh.video.core.service.IPlatformEventService;
import com.yjh.video.core.storager.IRedisCatchStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/29
 * @since [产品/模块版本] （可选）
 */
public class PlatformEventServiceImpl implements IPlatformEventService {

    private final static Logger logger = LoggerFactory.getLogger(PlatformEventServiceImpl.class);

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private IMediaServerEventService mediaServerService;

    @Autowired
    private SIPCommanderFroPlatform commanderForPlatform;

    @Autowired
    private DynamicTask dynamicTask;

    @Autowired
    private ZLMRTPServerFactory zlmrtpServerFactory;

    @Autowired
    private SubscribeHolder subscribeHolder;


    /**
     * 平台上线
     *
     * @param parentPlatform 平台信息
     */
    @Override
    public void online(ParentPlatform parentPlatform) {
        logger.info("[国标级联]：{}, 平台上线/更新注册", parentPlatform.getServerGBId());
        // platformMapper.updateParentPlatformStatus(parentPlatform.getServerGBId(), true);
        ParentPlatformCatch parentPlatformCatch = redisCatchStorage.queryPlatformCatchInfo(parentPlatform.getServerGBId());
        if (parentPlatformCatch != null) {
            parentPlatformCatch.getParentPlatform().setStatus(true);
            redisCatchStorage.updatePlatformCatchInfo(parentPlatformCatch);
        }else {
            parentPlatformCatch = new ParentPlatformCatch();
            parentPlatformCatch.setParentPlatform(parentPlatform);
            parentPlatformCatch.setId(parentPlatform.getServerGBId());
            parentPlatform.setStatus(true);
            parentPlatformCatch.setParentPlatform(parentPlatform);
            redisCatchStorage.updatePlatformCatchInfo(parentPlatformCatch);
        }

        final String registerTaskKey = Constant.REGISTER_KEY_PREFIX + parentPlatform.getServerGBId();
        if (dynamicTask.contains(registerTaskKey)) {
            dynamicTask.stop(registerTaskKey);
        }
        // 添加注册任务
        dynamicTask.startDelay(registerTaskKey,
            // 注册失败（注册成功时由程序直接调用了online方法）
            ()-> {
                try {
                    commanderForPlatform.register(parentPlatform, eventResult -> offline(parentPlatform),null);
                } catch (InvalidArgumentException | ParseException | SipException e) {
                    logger.error("[命令发送失败] 国标级联定时注册: {}", e.getMessage());
                }
            },
            (parentPlatform.getExpires() - 10) *1000);

        final String keepaliveTaskKey = Constant.KEEPALIVE_KEY_PREFIX + parentPlatform.getServerGBId();
        if (!dynamicTask.contains(keepaliveTaskKey)) {
            // 添加心跳任务
            dynamicTask.startCron(keepaliveTaskKey,
                ()-> {
                    try {
                        commanderForPlatform.keepalive(parentPlatform, eventResult -> {
                            // 心跳失败
                            if (eventResult.type == SipSubscribe.EventResultType.timeout) {
                                // 心跳超时
                                ParentPlatformCatch platformCatch = redisCatchStorage.queryPlatformCatchInfo(parentPlatform.getServerGBId());
                                // 此时是第三次心跳超时， 平台离线
                                if (platformCatch.getKeepAliveReply()  == 2) {
                                    // 设置平台离线，并重新注册
                                    offline(parentPlatform);
                                    logger.info("[国标级联] {}，三次心跳超时后再次发起注册", parentPlatform.getServerGBId());
                                    try {
                                        commanderForPlatform.register(parentPlatform, eventResult1 -> {
                                            logger.info("[国标级联] {}，三次心跳超时后再次发起注册仍然失败，开始定时发起注册，间隔为1分钟", parentPlatform.getServerGBId());
                                            // 添加注册任务
                                            dynamicTask.startCron(registerTaskKey,
                                                // 注册失败（注册成功时由程序直接调用了online方法）
                                                ()->logger.info("[国标级联] {},平台离线后持续发起注册，失败", parentPlatform.getServerGBId()),
                                                60*1000);
                                        }, null);
                                    } catch (InvalidArgumentException | ParseException | SipException e) {
                                        logger.error("[命令发送失败] 国标级联 注册: {}", e.getMessage());
                                    }
                                }

                            }else {
                                logger.warn("[国标级联]发送心跳收到错误，code： {}, msg: {}", eventResult.statusCode, eventResult.msg);
                            }

                        }, eventResult -> {
                            // 心跳成功
                            // 清空之前的心跳超时计数
                            ParentPlatformCatch platformCatch = redisCatchStorage.queryPlatformCatchInfo(parentPlatform.getServerGBId());
                            if (platformCatch.getKeepAliveReply() > 0) {
                                platformCatch.setKeepAliveReply(0);
                                redisCatchStorage.updatePlatformCatchInfo(platformCatch);
                            }
                        });
                    } catch (SipException | InvalidArgumentException | ParseException e) {
                        logger.error("[命令发送失败] 国标级联 发送心跳: {}", e.getMessage());
                    }
                },
                (parentPlatform.getKeepTimeout() - 10)*1000);
        }
    }

    @Override
    public void offline(ParentPlatform parentPlatform) {
        logger.info("[平台离线]：{}", parentPlatform.getServerGBId());
        ParentPlatformCatch parentPlatformCatch = redisCatchStorage.queryPlatformCatchInfo(parentPlatform.getServerGBId());
        parentPlatformCatch.setKeepAliveReply(0);
        parentPlatformCatch.setRegisterAliveReply(0);
        ParentPlatform parentPlatformInCatch = parentPlatformCatch.getParentPlatform();
        parentPlatformInCatch.setStatus(false);
        parentPlatformCatch.setParentPlatform(parentPlatformInCatch);
        redisCatchStorage.updatePlatformCatchInfo(parentPlatformCatch);
        // platformMapper.updateParentPlatformStatus(parentPlatform.getServerGBId(), false);

        // 停止所有推流
        logger.info("[平台离线] {}, 停止所有推流", parentPlatform.getServerGBId());
        stopAllPush(parentPlatform.getServerGBId());
        // 清除注册定时
        logger.info("[平台离线] {}, 停止定时注册任务", parentPlatform.getServerGBId());
        final String registerTaskKey = Constant.REGISTER_KEY_PREFIX + parentPlatform.getServerGBId();
        if (dynamicTask.contains(registerTaskKey)) {
            dynamicTask.stop(registerTaskKey);
        }
        // 清除心跳定时
        logger.info("[平台离线] {}, 停止定时发送心跳任务", parentPlatform.getServerGBId());
        final String keepaliveTaskKey = Constant.KEEPALIVE_KEY_PREFIX + parentPlatform.getServerGBId();
        if (dynamicTask.contains(keepaliveTaskKey)) {
            // 添加心跳任务
            dynamicTask.stop(keepaliveTaskKey);
        }
        // 停止目录订阅回复
        logger.info("[平台离线] {}, 停止订阅回复", parentPlatform.getServerGBId());
        subscribeHolder.removeAllSubscribe(parentPlatform.getServerGBId());
    }


    private void stopAllPush(String platformId) {
        List<SendRtpItem> sendRtpItems = redisCatchStorage.querySendRTPServer(platformId);
        if (sendRtpItems != null && sendRtpItems.size() > 0) {
            for (SendRtpItem sendRtpItem : sendRtpItems) {
                redisCatchStorage.deleteSendRTPServer(platformId, sendRtpItem.getChannelId(), null, null);
                MediaServerItem mediaInfo = mediaServerService.getOne(sendRtpItem.getMediaServerId());
                Map<String, Object> param = new HashMap<>(3);
                param.put("vhost", "__defaultVhost__");
                param.put("app", sendRtpItem.getApp());
                param.put("stream", sendRtpItem.getStreamId());
                zlmrtpServerFactory.stopSendRtpStream(mediaInfo, param);
            }
        }
    }

}
