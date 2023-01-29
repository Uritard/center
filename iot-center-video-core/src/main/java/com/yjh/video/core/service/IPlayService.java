package com.yjh.video.core.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.video.core.gb28181.bean.Device;
import com.yjh.video.core.gb28181.bean.InviteStreamCallback;
import com.yjh.video.core.gb28181.bean.InviteStreamInfo;
import com.yjh.video.core.gb28181.bean.SSRCInfo;
import com.yjh.video.core.gb28181.bean.msg.WVPResult;
import com.yjh.video.core.gb28181.event.SipSubscribe;
import com.yjh.video.core.media.zlm.ZlmHttpHookSubscribe;
import com.yjh.video.core.media.zlm.dto.MediaServerItem;
import com.yjh.video.core.media.zlm.dto.StreamInfo;
import com.yjh.video.core.service.bean.PlayResult;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 点播处理
 */
public interface IPlayService {

    void onPublishHandlerForPlay(MediaServerItem mediaServerItem, JSONObject resonse, String deviceId, String channelId, String uuid);

    void play(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo, Device device, String channelId,
              ZlmHttpHookSubscribe.Event hookEvent, SipSubscribe.Event errorEvent,
              InviteTimeOutCallback timeoutCallback, String uuid);
    PlayResult play(MediaServerItem mediaServerItem, String deviceId, String channelId, ZlmHttpHookSubscribe.Event event, SipSubscribe.Event errorEvent, Runnable timeoutCallback);

    MediaServerItem getNewMediaServerItem(Device device);

    void onPublishHandlerForDownload(InviteStreamInfo inviteStreamInfo, String deviceId, String channelId, String toString);

    DeferredResult<WVPResult<StreamInfo>> playBack(String deviceId, String channelId, String startTime, String endTime, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);
    DeferredResult<WVPResult<StreamInfo>> playBack(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo,String deviceId, String channelId, String startTime, String endTime, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);

    void zlmServerOffline(String mediaServerId);

    DeferredResult<WVPResult<StreamInfo>> download(String deviceId, String channelId, String startTime, String endTime, int downloadSpeed, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);
    DeferredResult<WVPResult<StreamInfo>> download(MediaServerItem mediaServerItem, SSRCInfo ssrcInfo,String deviceId,  String channelId, String startTime, String endTime, int downloadSpeed, InviteStreamCallback infoCallBack, PlayBackCallback hookCallBack);

    StreamInfo getDownLoadInfo(String deviceId, String channelId, String stream);

    void zlmServerOnline(String mediaServerId);
}
