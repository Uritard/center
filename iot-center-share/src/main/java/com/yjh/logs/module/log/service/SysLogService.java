package com.yjh.logs.module.log.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.CaseFormat;
import com.yjh.logs.common.utils.NumToStringUtil;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.commons.result.ResultCodeEnum;
import com.yjh.logs.module.log.dao.SysLogDao;
import com.yjh.logs.module.log.entity.LongAnalyseDetail;
import com.yjh.logs.module.log.entity.SysLog;
import com.yjh.logs.module.log.entity.SysLogDetail;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author tt
* @since 2021-01-14
*/
@Service
public class SysLogService {

    @Autowired
    private SysLogDao sysLogDao;

    private Logger log = LoggerFactory.getLogger(SysLogService.class);

    @Resource
    private RedisTemplate redisTemplate;


    @Value("${spring.websocket.send.url}")
    private String WEBSOCKET_SEND_URL;

    @Transactional(rollbackFor = Exception.class)
    public Result insert(String logType, String ip,String title,Integer state,String content,Long userId,String userName,String requestOrigin,String requestPath,String requestMethod) {

        Result result = new Result();
        try{
            {
                //判断是否需要写入日志
                Map<String, String> mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogTime");
                if (mapForParam != null && mapForParam.size() > 0) {
                    String noLogTime = mapForParam.get("content");//日志时间 2021-03-04 00:00:00~2021-03-05 00:00:00
                    if (noLogTime != null) {
                        String[] timeList = noLogTime.split("~");
                        if (timeList.length < 2) {
                            //时间格式不对
                            log.info("时间格式不对" + noLogTime);
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                //String isIn = NumToStringUtil.findType(logType);
                                if("0".equals(logType) || "1".equals(logType)|| "2".equals(logType)
                                        || "3".equals(logType)
                                        || "4".equals(logType)
                                        || "5".equals(logType)
                                        || "8".equals(logType)
                                        || "9".equals(logType)
                                        || "10".equals(logType)
                                        || "11".equals(logType)
                                        || "12".equals(logType)
                                        || "13".equals(logType)
                                        || "15".equals(logType)
                                        || "18".equals(logType)
                                        || "16".equals(logType)){
                                    Date start = simpleDateFormat.parse(timeList[0]);
                                    Date end = simpleDateFormat.parse(timeList[1]);
                                    Date now = new Date();
                                    if (start.getTime() <= now.getTime() && end.getTime() >= now.getTime()) {
                                        //满足日期格式
                                        result.setData("此条日志无需入库，原因：日志时间");
                                        return result;
                                    }
                                }

                            } catch (ParseException e) {
                                log.error("日期格式错误" + e);
                            }

                        }
                    }

                }
//                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogResult");
//                if (mapForParam != null && mapForParam.size() > 0) {
//                    String noLogResult = mapForParam.get("content");//日志结果  成功
//                    if (noLogResult != null) {
//                        //1：成功 2：失败
//                        if ("成功".equals(noLogResult)) {
//                            if (state == 1) {
//                                result.setData("此条日志无需入库，原因：日志结果");
//                                return result;
//                            }
//                        }
//                        if ("失败".equals(noLogResult)) {
//                            if (state == 2) {
//                                result.setData("此条日志无需入库，原因：日志结果");
//                                return result;
//                            }
//                        }
//                    }
//                }
//                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogTitle");
//                if (mapForParam != null && mapForParam.size() > 0) {
//                    String noLogTitle = mapForParam.get("content");//日志标题  多个标题以，隔开  用户登录，用户登出
//                    if (noLogTitle != null) {
//                        //精确匹配
//                        String[] resultList = noLogTitle.split("-");
//                        if (resultList.length > 0) {
//                            for (String item : resultList) {
//                                if (item.equals(title)) {
//                                    result.setData("此条日志无需入库，原因：日志标题");
//                                    return result;
//                                }
//                            }
//                        }
//                    }
//                }
                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogType");
                if (mapForParam != null && mapForParam.size() > 0) {
                    String noLogType = mapForParam.get("content");//日志类型
                    if (noLogType != null) {
                        String[] typeList = noLogType.split("-");
                        if (typeList.length > 0) {
                            for (String item : typeList) {
                                //todo 日志类型
                                item = NumToStringUtil.findType(item);
                                if (item != null && item.equals(logType)) {
                                    result.setData("此条日志无需入库，原因：日志类型");
                                    return result;
                                }
                            }
                        }
                    }
                }
//                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogUserIdentity");
//                if (mapForParam != null && mapForParam.size() > 0) {
//                    String noLogUserIdentity = mapForParam.get("content");//用户身份
//                    if (noLogUserIdentity != null) {
//                        String[] userList = noLogUserIdentity.split("-");
//                        if (userList.length > 0) {
//                            for (String item : userList) {
//                                if (item.equals(userName)) {
//                                    result.setData("此条日志无需入库，原因：用户身份");
//                                    return result;
//                                }
//                            }
//                        }
//                    }
//                }
            }
            SysLog sysLog = new SysLog();
            sysLog.setLogType(logType);
            sysLog.setIp(ip);
            sysLog.setTitle(title);
            sysLog.setState(state);
            sysLog.setContent(content);
            sysLog.setUserId(userId);
            sysLog.setUserName(userName);
            sysLog.setRequestOrigin(requestOrigin);
            sysLog.setRequestPath(requestPath);
            sysLog.setRequestMethod(requestMethod);
            sysLog.setCreateTime(new Date());
            Random random = new Random();
            if (state == 1) {
                new Thread(() -> {
                    String mathRandom = String.valueOf(random.nextInt(50)) + "000";
                    long mathLong = Long.valueOf(mathRandom);
                    log.info("mathLong: " + mathLong);
                    try {
                        Thread.sleep(mathLong);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    result.setData(sysLogDao.insert(sysLog));
                }).start();
            } else {
                Map<String, Object> errMessage = new HashMap<>();
                errMessage.put("type", "errorLog");
                errMessage.put("content", content);
                errMessage.put("state", state);
                String json = JSON.toJSONString(errMessage);
                new Thread(() -> {
                    try {
                        postUrl(json);
                        // Result result1 = restTemplate.getForObject(Constant.WEB_SCOKET, Result.class, json);
                    } catch (Exception e) {
                        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                        log.error("发送失败：" + e);
                    }
                    if(state==3){
                        redisTemplate.opsForValue().set("errorLog", content, 5, TimeUnit.MINUTES);
                    }
                    String mathRandom = String.valueOf(random.nextInt(50)) + "000";
                    long mathLong = Long.valueOf(mathRandom);
                    log.info("mathLong: " + mathLong);
                    try {
                        Thread.sleep(mathLong);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    result.setData(sysLogDao.insert(sysLog));
                }).start();
            }
        }catch (Exception e){

        }
//        if(Objects.isNull(sysLog)){
//            log.error("SysLogService.addOperateLog >>> error: parameter is null.");
//            return 1;
//        }
        return result;
    }


    //请求webSocket发送方法
    public String postUrl(String json) throws IOException, URISyntaxException {
        //  String url = redisTemplate.opsForHash().get("t_sys_param:webSocketUrl", "content").toString();
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(WEBSOCKET_SEND_URL).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long logId) {
        return this.sysLogDao.deleteByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysLog sysLog) {
        return this.sysLogDao.update(sysLog);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysLog selectByPrimaryId(Long logId) {
        return this.sysLogDao.selectByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLog> select(Long logId, String logType, String ip, String title, Integer state, String content, Long userId, String userName, String requestOrigin, String requestPath, String requestMethod, Date createTime) {
        List<SysLog> sysLogList = sysLogDao.select(logId, logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod, createTime);
        return sysLogList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLogDetail> selectByPage(String userName, String title, Date startTime, Date endTime,String logType, String state) {
        List<SysLogDetail> sysOperateLogList = sysLogDao.selectByPage(userName,title,startTime,endTime,logType, state);
        for (SysLogDetail item:sysOperateLogList) {
            String type = item.getLogType();
            if(type != null && !"".equals(type)){
                type = NumToStringUtil.findType(type);
                if(type != null){
                    item.setLogType(type);
                }
            }
            String states = item.getState();
            if(states != null && !"".equals(states)){
                if("1".equals(states)){
                    states = "成功";
                }
                if("2".equals(states)){
                    states = "失败";
                }
                if("3".equals(states)){
                    states = "异常";
                }
                item.setState(states);
            }
        }
        return sysOperateLogList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<SysLogDetail> selectByPageAsc(String userName, String title, Date startTime, Date endTime,String logType, String state) {
        List<SysLogDetail> sysOperateLogList = sysLogDao.selectByPageAsc(userName,title,startTime,endTime,logType, state);
        for (SysLogDetail item:sysOperateLogList) {
            String type = item.getLogType();
            if(type != null && !"".equals(type)){
                type = NumToStringUtil.findType(type);
                if(type != null){
                    item.setLogType(type);
                }
            }
            String states = item.getState();
            if(states != null && !"".equals(states)){
                if("1".equals(states)){
                    states = "成功";
                }
                if("2".equals(states)){
                    states = "失败";
                }
                if("3".equals(states)){
                    states = "异常";
                }
                item.setState(states);
            }
        }
        return sysOperateLogList;
    }

    public List<SysLogDetail> selectByPageSort(String userName, String title, Date startTime, Date endTime,String logType, String state, String field, String sortOrder) {
        // 驼峰转下划线
        String sortField = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field);

        List<SysLogDetail> sysOperateLogList = sysLogDao.selectByPageSort(userName,title,startTime,endTime,logType, state, sortField, sortOrder);
        for (SysLogDetail item:sysOperateLogList) {
            String type = item.getLogType();
            if(type != null && !"".equals(type)){
                type = NumToStringUtil.findType(type);
                if(type != null){
                    item.setLogType(type);
                }
            }
            String states = item.getState();
            if(states != null && !"".equals(states)){
                if("1".equals(states)){
                    states = "成功";
                }
                if("2".equals(states)){
                    states = "失败";
                }
                if("3".equals(states)){
                    states = "异常";
                }
                item.setState(states);
            }
        }
        return sysOperateLogList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysLog> list) {
        return this.sysLogDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> logAnalyze(){
        List<LongAnalyseDetail> listForAna = this.sysLogDao.logAnalyze();
        List<Map<String,String>> listForRe = new ArrayList<>();
        for(int i = 0; i < 29;i++){
            Map<String,String> map = new HashMap<>();
            String count = "0";
            for(LongAnalyseDetail item:listForAna){
                if(String.valueOf(i).equals(item.getLogType())){
                    count = item.getCount();
                }
            }
            // 如果数据为0，则不返回给前端
            if(!"0".equals(count)){
                map.put("logType",NumToStringUtil.findType(String.valueOf(i)));
                map.put("count",count);
                listForRe.add(map);
            }
        }
        //List<Map<String,Integer>> listForAna = this.sysLogDao.logAnalyze(list);
        return listForRe;
    }
}

