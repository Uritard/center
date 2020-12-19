package com.yjh.accessrobot.netty.server;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @author YC
 * @date 2020/12/18 15:38
 */
public class OneDealThread  implements Runnable{

    private Map<String,String> threadMap;

    private RedisTemplate redisTemplate;

    public OneDealThread(Map<String,String> threadMap,RedisTemplate redisTemplate){
        this.threadMap = threadMap;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        try {
            /*String taskId = threadMap.get("taskCode");
            if (Constant.flagMap.get(taskId) == 0){
                CruiseResultDealThread cruiseResultDealThread = new CruiseResultDealThread(threadMap,redisTemplate);
                TaskExecutePool.getInstance().execute(cruiseResultDealThread);
            }*/
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
