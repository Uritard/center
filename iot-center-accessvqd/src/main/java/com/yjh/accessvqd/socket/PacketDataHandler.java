package com.yjh.accessvqd.socket;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.accessvqd.commons.utils.xmlAnalyse.ChanResultXML;
import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *Author-czh
 * 2021-1-21
 * 诊断结果数据解析与入库
 */
public class PacketDataHandler {

    private Logger log = LoggerFactory.getLogger(PacketDataHandler.class);


    private ChanResultService chanResultService;
    private TDiagnosePlanDao tDiagnosePlanDao;
    private RedisTemplate redisTemplate;

    public PacketDataHandler(ChanResultService chanResultService,TDiagnosePlanDao tDiagnosePlanDao,RedisTemplate redisTemplate) {
        this.chanResultService = chanResultService;
        this.tDiagnosePlanDao=tDiagnosePlanDao;
        this.redisTemplate=redisTemplate;
    }

    public void analysisChanResult(String chanResult){
        log.info("result--"+chanResult);
        try {
            //二次拆包
            log.info("index"+chanResult.indexOf("<?"));
            //xml转实体
            // TODO: 2021/2/7   index:报文开始处理位置--会出现波动--后期修改
            ChanResult chanResult1=ChanResultXML.unPackingXMl(chanResult.substring(137));
            log.info("对象:"+chanResult1);
            //获取监测点ID
            Set<String>keys=redisScan("diagnosePlan");
            //诊断点结果与任务匹配
            for(String key:keys){
                if(redisTemplate.opsForList().remove(key,0,chanResult1.getChannelId()) !=0){ //监测点ID移除成功
                    chanResult1.setDiagnosePlanId( key.replaceAll("diagnosePlan:",""));
                    log.info("size:------"+redisTemplate.opsForList().size(key));
                    if(redisTemplate.opsForList().size(key)==0){
                       log.info("updatePlan:"+tDiagnosePlanDao.updateEndTime(chanResult1.getDiagnosePlanId(),new Date()));
                    }
                }

            }
            //结果入库
            chanResultService.insert(chanResult1);
         }catch (Exception e){
            e.printStackTrace();
        }

    }
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }


}
