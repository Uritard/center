/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.video.core.Constant;
import com.yjh.video.core.VideoManagerConstants;
import com.yjh.video.core.config.DynamicTask;
import com.yjh.video.core.config.SipConfig;
import com.yjh.video.core.config.UserSetting;
import com.yjh.video.core.gb28181.bean.SSRCInfo;
import com.yjh.video.core.gb28181.event.EventPublisher;
import com.yjh.video.core.gb28181.session.SsrcConfig;
import com.yjh.video.core.gb28181.session.VideoStreamSessionManager;
import com.yjh.video.core.media.zlm.ZLMRESTfulUtils;
import com.yjh.video.core.media.zlm.ZLMRTPServerFactory;
import com.yjh.video.core.media.zlm.dto.MediaServerItem;
import com.yjh.video.core.service.IMediaServerEventService;
import com.yjh.video.core.utils.DateUtil;
import com.yjh.video.core.utils.redis.RedisUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
@Component
public class MediaServerEventServiceImpl implements IMediaServerEventService {
    private final static Logger logger = LoggerFactory.getLogger(MediaServerEventServiceImpl.class);

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private SipConfig sipConfig;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private ZLMRTPServerFactory zlmrtpServerFactory;

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    private VideoStreamSessionManager streamSession;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private DynamicTask dynamicTask;


    /**
     * 初始化
     */
    @Override
    public void updateVmServer(List<MediaServerItem> mediaServerItemList) {
        logger.info("[zlm] 缓存初始化 ");
        for (MediaServerItem mediaServerItem : mediaServerItemList) {
            if (ObjectUtils.isEmpty(mediaServerItem.getId())) {
                continue;
            }
            // 更新
            if (mediaServerItem.getSsrcConfig() == null) {
                SsrcConfig ssrcConfig = new SsrcConfig(mediaServerItem.getId(), null, sipConfig.getDomain());
                mediaServerItem.setSsrcConfig(ssrcConfig);
                RedisUtil.set(VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerItem.getId(), mediaServerItem);
            }
            // 查询redis是否存在此mediaServer
            String key = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerItem.getId();
            if (!RedisUtil.hasKey(key)) {
                RedisUtil.set(key, mediaServerItem);
            }

        }
    }

    @Override
    public List<MediaServerItem> getAll() {
        List<MediaServerItem> result = new ArrayList<>();
        List<Object> mediaServerKeys = RedisUtil.scan(String.format("%S*", VideoManagerConstants.MEDIA_SERVER_PREFIX+ userSetting.getServerId() + "_" ));
        String onlineKey = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        for (Object mediaServerKey : mediaServerKeys) {
            String key = (String) mediaServerKey;
            MediaServerItem mediaServerItem = (MediaServerItem) RedisUtil.get(key);
            // 检查状态
            Double aDouble = RedisUtil.zScore(onlineKey, mediaServerItem.getId());
            if (aDouble != null) {
                mediaServerItem.setStatus(true);
            }
            result.add(mediaServerItem);
        }
        result.sort((serverItem1, serverItem2)->{
            int sortResult = 0;
            LocalDateTime localDateTime1 = LocalDateTime.parse(serverItem1.getCreateTime(), DateUtil.formatter);
            LocalDateTime localDateTime2 = LocalDateTime.parse(serverItem2.getCreateTime(), DateUtil.formatter);

            sortResult = localDateTime1.compareTo(localDateTime2);
            return  sortResult;
        });
        return result;
    }

    @Override
    public List<MediaServerItem> getAllOnline() {
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        Set<String> mediaServerIdSet = RedisUtil.zRevRange(key, 0, -1);

        List<MediaServerItem> result = new ArrayList<>();
        if (mediaServerIdSet != null && mediaServerIdSet.size() > 0) {
            for (String mediaServerId : mediaServerIdSet) {
                String serverKey = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerId;
                result.add((MediaServerItem) RedisUtil.get(serverKey));
            }
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public MediaServerItem getOne(String mediaServerId) {
        if (StringUtils.isEmpty(mediaServerId)) {
            return null;
        }
        String key = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerId;
        return (MediaServerItem)RedisUtil.get(key);
    }

    @Override
    public void clearMediaServerForOnline() {
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        RedisUtil.del(key);
    }

    @Override
    public void releaseSsrc(String mediaServerItemId, String ssrc) {
        MediaServerItem mediaServerItem = getOne(mediaServerItemId);
        if (mediaServerItem == null || ssrc == null) {
            return;
        }
        SsrcConfig ssrcConfig = mediaServerItem.getSsrcConfig();
        ssrcConfig.releaseSsrc(ssrc);
        mediaServerItem.setSsrcConfig(ssrcConfig);
        String key = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerItem.getId();
        RedisUtil.set(key, mediaServerItem);
    }

    @Override
    public SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, boolean ssrcCheck, boolean isPlayback) {
        return openRTPServer(mediaServerItem, streamId, null, ssrcCheck,isPlayback);
    }

    @Override
    public SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String ssrc, boolean ssrcCheck, boolean isPlayback) {
        return openRTPServer(mediaServerItem, streamId, ssrc, ssrcCheck, isPlayback, null);
    }

    @Override
    public SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String presetSsrc, boolean ssrcCheck, boolean isPlayback,
        Integer port) {
        if (mediaServerItem == null || StringUtils.isEmpty(mediaServerItem.getId())) {
            return null;
        }
        // 获取mediaServer可用的ssrc
        String key = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + mediaServerItem.getId();

        SsrcConfig ssrcConfig = mediaServerItem.getSsrcConfig();
        if (ssrcConfig == null) {
            logger.info("media server [ {} ] ssrcConfig is null", mediaServerItem.getId());
            return null;
        }else {
            String ssrc;
            if (presetSsrc != null) {
                ssrc = presetSsrc;
            }else {
                if (isPlayback) {
                    ssrc = ssrcConfig.getPlayBackSsrc();
                }else {
                    ssrc = ssrcConfig.getPlaySsrc();
                }
            }

            if (streamId == null) {
                streamId = String.format("%08x", Integer.parseInt(ssrc)).toUpperCase();
            }
            int rtpServerPort;
            if (mediaServerItem.isRtpEnable()) {
                rtpServerPort = zlmrtpServerFactory.createRTPServer(mediaServerItem, streamId, ssrcCheck?Integer.parseInt(ssrc):0, port);
            } else {
                rtpServerPort = mediaServerItem.getRtpProxyPort();
            }
            RedisUtil.set(key, mediaServerItem);
            return new SSRCInfo(rtpServerPort, ssrc, streamId);
        }
    }

    @Override
    public void closeRTPServer(MediaServerItem mediaServerItem, String streamId) {
        if (mediaServerItem == null) {
            return;
        }
        zlmrtpServerFactory.closeRTPServer(mediaServerItem, streamId);
        releaseSsrc(mediaServerItem.getId(), streamId);
    }

    @Override
    public void closeRTPServer(String mediaServerId, String streamId) {
        MediaServerItem mediaServerItem = this.getOne(mediaServerId);
        closeRTPServer(mediaServerItem, streamId);
    }

    @Override
    public void clearRTPServer(MediaServerItem mediaServerItem) {
        mediaServerItem.setSsrcConfig(new SsrcConfig(mediaServerItem.getId(), null, sipConfig.getDomain()));
        RedisUtil.zAdd(VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId(), mediaServerItem.getId(), 0);
    }

    @Override
    public boolean checkRtpServer(MediaServerItem mediaServerItem, String app, String stream) {
        JSONObject rtpInfo = zlmresTfulUtils.getRtpInfo(mediaServerItem, stream);
        if(rtpInfo.getInteger("code") == 0){
            return rtpInfo.getBoolean("exist");
        }
        return false;
    }

    @Override
    public void zlmServerOffline(String mediaServerId) {
        delete(mediaServerId);
        final String zlmKeepaliveKey = Constant.ZLM_KEEPALIVE_KEY_PREFIX + mediaServerId;
        dynamicTask.stop(zlmKeepaliveKey);
    }

    @Override
    public void delete(String id) {
        RedisUtil.zRemove(VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId(), id);
        String key = VideoManagerConstants.MEDIA_SERVER_PREFIX + userSetting.getServerId() + "_" + id;
        RedisUtil.del(key);
    }


    @Override
    public void resetOnlineServerItem(MediaServerItem serverItem) {
        // 更新缓存
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        // 使用zset的分数作为当前并发量， 默认值设置为0
        if (RedisUtil.zScore(key, serverItem.getId()) == null) {  // 不存在则设置默认值 已存在则重置
            RedisUtil.zAdd(key, serverItem.getId(), 0L);
            // 查询服务流数量
            zlmresTfulUtils.getMediaList(serverItem, null, null, "rtsp",(mediaList ->{
                Integer code = mediaList.getInteger("code");
                if (code == 0) {
                    JSONArray data = mediaList.getJSONArray("data");
                    if (data != null) {
                        RedisUtil.zAdd(key, serverItem.getId(), data.size());
                    }
                }
            }));
        }else {
            clearRTPServer(serverItem);
        }
    }


    @Override
    public void addCount(String mediaServerId) {
        if (mediaServerId == null) {
            return;
        }
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        RedisUtil.zIncrScore(key, mediaServerId, 1);

    }

    @Override
    public void removeCount(String mediaServerId) {
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();
        RedisUtil.zIncrScore(key, mediaServerId, - 1);
    }

    /**
     * 获取负载最低的节点
     * @return MediaServerItem
     */
    @Override
    public MediaServerItem getMediaServerForMinimumLoad() {
        String key = VideoManagerConstants.MEDIA_SERVERS_ONLINE_PREFIX + userSetting.getServerId();

        if (RedisUtil.zSize(key)  == null || RedisUtil.zSize(key) == 0) {
            if (RedisUtil.zSize(key)  == null || RedisUtil.zSize(key) == 0) {
                logger.info("获取负载最低的节点时无在线节点");
                return null;
            }
        }

        // 获取分数最低的，及并发最低的
        Set<Object> objects = RedisUtil.zRange(key, 0, -1);
        ArrayList<Object> mediaServerObjectS = new ArrayList<>(objects);

        String mediaServerId = (String)mediaServerObjectS.get(0);
        return getOne(mediaServerId);
    }

    /**
     * 对zlm服务器进行基础配置
     * @param mediaServerItem 服务ID
     * @param restart 是否重启zlm
     */
    @Override
    public void setZLMConfig(MediaServerItem mediaServerItem, boolean restart) {
        logger.info("[ZLM] 正在设置 ：{} -> {}:{}",
            mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
        String protocol = sslEnabled ? "https" : "http";
        String hookPrex = String.format("%s://%s:%s/index/hook", protocol, mediaServerItem.getHookIp(), serverPort);

        Map<String, Object> param = new HashMap<>();
        param.put("api.secret",mediaServerItem.getSecret()); // -profile:v Baseline
        param.put("ffmpeg.cmd","%s -fflags nobuffer -i %s -c:a aac -strict -2 -ar 44100 -ab 48k -c:v libx264  -f flv %s");
        param.put("hook.enable","1");
        param.put("hook.on_flow_report",String.format("%s/on_flow_report", hookPrex));
        param.put("hook.on_play",String.format("%s/on_play", hookPrex));
        param.put("hook.on_http_access",String.format("%s/on_http_access", hookPrex));
        param.put("hook.on_publish", String.format("%s/on_publish", hookPrex));
        param.put("hook.on_record_ts",String.format("%s/on_record_ts", hookPrex));
        param.put("hook.on_rtsp_auth",String.format("%s/on_rtsp_auth", hookPrex));
        param.put("hook.on_rtsp_realm",String.format("%s/on_rtsp_realm", hookPrex));
        param.put("hook.on_server_started",String.format("%s/on_server_started", hookPrex));
        param.put("hook.on_shell_login",String.format("%s/on_shell_login", hookPrex));
        param.put("hook.on_stream_changed",String.format("%s/on_stream_changed", hookPrex));
        param.put("hook.on_stream_none_reader",String.format("%s/on_stream_none_reader", hookPrex));
        param.put("hook.on_stream_not_found",String.format("%s/on_stream_not_found", hookPrex));
        param.put("hook.on_server_keepalive",String.format("%s/on_server_keepalive", hookPrex));
        param.put("hook.on_send_rtp_stopped",String.format("%s/on_send_rtp_stopped", hookPrex));
        if (mediaServerItem.getRecordAssistPort() > 0) {
            param.put("hook.on_record_mp4",String.format("http://127.0.0.1:%s/api/record/on_record_mp4", mediaServerItem.getRecordAssistPort()));
        }else {
            param.put("hook.on_record_mp4","");
        }
        param.put("hook.timeoutSec","20");
        param.put("general.streamNoneReaderDelayMS",mediaServerItem.getStreamNoneReaderDelayMS()==-1?"3600000":mediaServerItem.getStreamNoneReaderDelayMS() );
        // 推流断开后可以在超时时间内重新连接上继续推流，这样播放器会接着播放。
        // 置0关闭此特性(推流断开会导致立即断开播放器)
        // 此参数不应大于播放器超时时间
        // 优化此消息以更快的收到流注销事件
        param.put("general.continue_push_ms", "3000" );
        // 最多等待未初始化的Track时间，单位毫秒，超时之后会忽略未初始化的Track, 设置此选项优化那些音频错误的不规范流，
        // 等zlm支持给每个rtpServer设置关闭音频的时候可以不设置此选项
        //        param.put("general.wait_track_ready_ms", "3000" );
        if (mediaServerItem.isRtpEnable() && !ObjectUtils.isEmpty(mediaServerItem.getRtpPortRange())) {
            param.put("rtp_proxy.port_range", mediaServerItem.getRtpPortRange().replace(",", "-"));
        }

        JSONObject responseJSON = zlmresTfulUtils.setServerConfig(mediaServerItem, param);

        if (responseJSON != null && responseJSON.getInteger("code") == 0) {
            if (restart) {
                logger.info("[ZLM] 设置成功,开始重启以保证配置生效 {} -> {}:{}",
                    mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
                zlmresTfulUtils.restartServer(mediaServerItem);
            }else {
                logger.info("[ZLM] 设置成功 {} -> {}:{}",
                    mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
            }


        }else {
            logger.info("[ZLM] 设置zlm失败 {} -> {}:{}",
                mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort());
        }

    }

    @Override
    public boolean checkMediaRecordServer(String ip, int port) {
        boolean result = false;
        OkHttpClient client = new OkHttpClient();
        String url = String.format("http://%s:%s/index/api/record",  ip, port);
        Request request = new Request.Builder()
            .get()
            .url(url)
            .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null) {
                result = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public void updateMediaServerKeepalive(String mediaServerId, JSONObject data) {
        MediaServerItem mediaServerItem = getOne(mediaServerId);
        if (mediaServerItem == null) {
            // zlm连接重试
            logger.warn("[更新ZLM 保活信息]失败，未找到流媒体信息,尝试重连zlm");
            //            reloadZlm();
            mediaServerItem = getOne(mediaServerId);
            if (mediaServerItem == null) {
                // zlm连接重试
                logger.warn("[更新ZLM 保活信息]失败，未找到流媒体信息");
                return;
            }
        }

        final String zlmKeepaliveKey = Constant.ZLM_KEEPALIVE_KEY_PREFIX + mediaServerItem.getId();
        dynamicTask.stop(zlmKeepaliveKey);
        dynamicTask.startDelay(zlmKeepaliveKey, new KeepAliveTimeoutRunnable(mediaServerItem), (mediaServerItem.getHookAliveInterval() + 5) * 1000);
    }

    public class KeepAliveTimeoutRunnable implements Runnable{

        private MediaServerItem serverItem;

        public KeepAliveTimeoutRunnable(MediaServerItem serverItem) {
            this.serverItem = serverItem;
        }

        @Override
        public void run() {
            logger.info("[zlm心跳到期]：" + serverItem.getId());
            // 发起http请求验证zlm是否确实无法连接，如果确实无法连接则发送离线事件，否则不作处理
            JSONObject mediaServerConfig = zlmresTfulUtils.getMediaServerConfig(serverItem);
            if (mediaServerConfig != null && mediaServerConfig.getInteger("code") == 0) {
                logger.info("[zlm心跳到期]：{}验证后zlm仍在线，恢复心跳信息,请检查zlm是否可以正常向wvp发送心跳", serverItem.getId());
                // 添加zlm信息
                updateMediaServerKeepalive(serverItem.getId(), mediaServerConfig);
            }else {
                publisher.zlmOfflineEventPublish(serverItem.getId());
            }
        }
    }
}
