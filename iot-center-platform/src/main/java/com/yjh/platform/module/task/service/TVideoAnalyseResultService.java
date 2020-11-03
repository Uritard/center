package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.yjh.platform.module.task.entity.TVideoAnalyseResult;
import com.yjh.platform.module.task.dao.TVideoAnalyseResultDao;

import java.util.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TVideoAnalyseResultService{

    @Autowired
    private TVideoAnalyseResultDao tVideoAnalyseResultDao;
    @Autowired
    private RedisTemplate redisTemplate;

    @Logs(title = "插入", code = "TVideoAnalyseResult",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.tVideoAnalyseResultDao.insert(tVideoAnalyseResult);
    }

    @Logs(title = "删除", code = "TVideoAnalyseResult",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long algorithmResultId) {
        return this.tVideoAnalyseResultDao.deleteByPrimaryId(algorithmResultId);
    }

    @Logs(title = "更新", code = "TVideoAnalyseResult",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TVideoAnalyseResult tVideoAnalyseResult) {
        return this.tVideoAnalyseResultDao.update(tVideoAnalyseResult);
    }

    @Logs(title = "主键查询", code = "TVideoAnalyseResult",content = "根据web传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TVideoAnalyseResult selectByPrimaryId(Long algorithmResultId) {
        return this.tVideoAnalyseResultDao.selectByPrimaryId(algorithmResultId);
    }

    @Logs(title = "查询", code = "TVideoAnalyseResult",content = "根据web传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAnalyseResult> select(Long algorithmResultId, Date analyseTime, String analyseConfId, String algorithmResult, String algorithmPicture, Integer algorithmStatus, String resultRate, String resultDescribe, Integer reserver) {
        List<TVideoAnalyseResult> tVideoAnalyseResultList = tVideoAnalyseResultDao.select(algorithmResultId, analyseTime, analyseConfId, algorithmResult, algorithmPicture, algorithmStatus, resultRate, resultDescribe, reserver);
//      for(Integer i=0;i<5;i++){
//          String name="AnlyseResult:"+i.toString();
//          Map map=new HashMap();
//          map.put("taskId","1111");
//          redisTemplate.opsForHash().putAll(name,map);
//      }
//
//
//        String str="{\n\"msgType\": \"3\", \n\"msgData\":{\n\"result1\":{\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\",\n\"registerKey\": \"yijiahe\"\n},\n\"result2\":{\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket002\",\n\"registerKey\": \"yijiahe\"\n}\n}\n}\n";
//        JSONObject jsonObject=JSON.parseObject(str);
//        String string =jsonObject.get("msgData").toString();
//        JSONObject jsonObject1=JSON.parseObject(string);
//        Iterator iterator=jsonObject1.entrySet().iterator();
//        while (iterator.hasNext()){
//            Map.Entry entry=(Map.Entry)iterator.next();
//            JSONObject jsonObject2=JSON.parseObject( entry.getValue().toString());
//            System.out.print(jsonObject2.get("srcNode")+"&&&&&&&&&&&");
//        }
        return tVideoAnalyseResultList;
    }

    @Logs(title = "分页查询", code = "TVideoAnalyseResult",content = "根据web传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TVideoAnalyseResult> selectByPage(TVideoAnalyseResult tVideoAnalyseResult) {
        List<TVideoAnalyseResult> tVideoAnalyseResultList = tVideoAnalyseResultDao.selectByPage(tVideoAnalyseResult);
        return tVideoAnalyseResultList;
    }

    @Logs(title = "批量插入", code = "TVideoAnalyseResult",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TVideoAnalyseResult> list) {
        return this.tVideoAnalyseResultDao.batchInsert(list);
    }

}

