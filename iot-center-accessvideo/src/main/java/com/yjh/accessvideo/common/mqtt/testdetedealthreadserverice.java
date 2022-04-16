package com.yjh.accessvideo.common.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import com.yjh.accessvideo.netty.client.DataDealThread;
import com.yjh.accessvideo.thread.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class testdetedealthreadserverice {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    private String syncWebsocketUrl="xxx";
    private ChannelHandlerContext ctx=null;



    public void senddatadeal(Object resposneobj){
        String usefulBody=JSONObject.toJSONString(resposneobj);
        DataDealThread dataDealThread = new DataDealThread(usefulBody, redisTemplate, analyseDataOperateService, ctx,syncWebsocketUrl);
        TaskExecutePool.getInstance().execute(dataDealThread);
    }


}
