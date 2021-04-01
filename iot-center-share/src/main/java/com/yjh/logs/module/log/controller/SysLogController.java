package com.yjh.logs.module.log.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.logs.common.Constant;
import com.yjh.logs.common.smUtil.Demo;
import com.yjh.logs.common.utils.NumToStringUtil;
import com.yjh.logs.commons.result.BusinessException;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.commons.result.ResultCodeEnum;
import com.yjh.logs.module.log.entity.SysLog;
import com.yjh.logs.module.log.entity.SysLogDetail;
import com.yjh.logs.module.log.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @author tt
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/sysLog/v1")
@Api(value = "/sysLog", description = "审计日志表操作接口")
public class SysLogController {

    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private final SysLogService sysLogService;
    @Resource
    private RedisTemplate redisTemplate;
    private Logger log = LoggerFactory.getLogger(SysLogController.class);

    public SysLogController(SysLogService sysOperateLogService) {
        this.sysLogService = sysOperateLogService;
    }


    //请求webSocket发送方法
    public String postUrl(String json) throws IOException, URISyntaxException {
        String url = redisTemplate.opsForHash().get("t_sys_param:webSocketUrl", "content").toString();
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(url).setParameter("json", json).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "ip", required = false) String ip,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "requestOrigin", required = false) String requestOrigin,
                         @RequestParam(value = "requestPath", required = false) String requestPath,
                         @RequestParam(value = "requestMethod", required = false) String requestMethod) {
        Result result = new Result();
        try {
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
                                Date start = simpleDateFormat.parse(timeList[0]);
                                Date end = simpleDateFormat.parse(timeList[1]);
                                Date now = new Date();
                                if (start.getTime() <= now.getTime() && end.getTime() >= now.getTime()) {
                                    //满足日期格式
                                    result.setData("此条日志无需入库，原因：日志时间");
                                    return result;
                                }
                            } catch (ParseException e) {
                                log.error("日期格式错误" + e);
                            }

                        }
                    }

                }
                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogResult");
                if (mapForParam != null && mapForParam.size() > 0) {
                    String noLogResult = mapForParam.get("content");//日志结果  成功
                    if (noLogResult != null) {
                        //1：成功 2：失败
                        if ("成功".equals(noLogResult)) {
                            if (state == 1) {
                                result.setData("此条日志无需入库，原因：日志结果");
                                return result;
                            }
                        }
                        if ("失败".equals(noLogResult)) {
                            if (state == 2) {
                                result.setData("此条日志无需入库，原因：日志结果");
                                return result;
                            }
                        }
                    }
                }
                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogTitle");
                if (mapForParam != null && mapForParam.size() > 0) {
                    String noLogTitle = mapForParam.get("content");//日志标题  多个标题以，隔开  用户登录，用户登出
                    if (noLogTitle != null) {
                        //精确匹配
                        String[] resultList = noLogTitle.split("-");
                        if (resultList.length > 0) {
                            for (String item : resultList) {
                                if (item.equals(title)) {
                                    result.setData("此条日志无需入库，原因：日志标题");
                                    return result;
                                }
                            }
                        }
                    }
                }
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
                mapForParam = redisTemplate.opsForHash().entries("t_sys_param:noLogUserIdentity");
                if (mapForParam != null && mapForParam.size() > 0) {
                    String noLogUserIdentity = mapForParam.get("content");//用户身份
                    if (noLogUserIdentity != null) {
                        String[] userList = noLogUserIdentity.split("-");
                        if (userList.length > 0) {
                            for (String item : userList) {
                                if (item.equals(userName)) {
                                    result.setData("此条日志无需入库，原因：用户身份");
                                    return result;
                                }
                            }
                        }
                    }
                }
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
                    result.setData(sysLogService.insert(sysLog));
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
                        String err = "错误日志";
                        redisTemplate.opsForValue().set("errorLog", err, 5, TimeUnit.MINUTES);
                        // Result result1 = restTemplate.getForObject(Constant.WEB_SCOKET, Result.class, json);
                    } catch (Exception e) {
                        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                        log.error("发送失败：" + e);
                    }
                    String mathRandom = String.valueOf(random.nextInt(50)) + "000";
                    long mathLong = Long.valueOf(mathRandom);
                    log.info("mathLong: " + mathLong);
                    try {
                        Thread.sleep(mathLong);
                    } catch (Exception e) {
                        e.getMessage();
                    }
                    result.setData(sysLogService.insert(sysLog));
                }).start();
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加日志错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "logId", required = true) Long logId) {
        Result result = new Result();
        try {
            result.setData(sysLogService.deleteByPrimaryId(logId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除日志异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除日志错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody SysLog sysLog) {
        Result result = new Result();
        try {
            result.setData(sysLogService.update(sysLog));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新日志异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新日志错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "logId", required = true) Long logId) {
        Result result = new Result();
        try {
            SysLog sysLog = sysLogService.selectByPrimaryId(logId);
            result.setData(sysLog);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询日志失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "logId", required = false) Long logId,
                         @RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "ip", required = false) String ip,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "requestOrigin", required = false) String requestOrigin,
                         @RequestParam(value = "requestPath", required = false) String requestPath,
                         @RequestParam(value = "requestMethod", required = false) String requestMethod,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
            }
            List<SysLog> list = sysLogService.select(logId, logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "userName", required = false) String userName,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "logType", required = false) String logType,
                               @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                               @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                               @RequestParam(value = "sortFlag", required = false) int sortFlag,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "0") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");

            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
            }
            if ("14".equals(logType)) {
                logType = "";
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            if(sortFlag==1){
                List<SysLogDetail> list = sysLogService.selectByPageAsc(userName, title, startTime, endTime, logType);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }else{
                List<SysLogDetail> list = sysLogService.selectByPage(userName, title, startTime, endTime, logType);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<SysLog> list) {
        Result result = new Result();
        try {
            result.setData(sysLogService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "日志统计")
    @RequestMapping(value = "/logAnalyze", method = RequestMethod.GET)
    public Result logAnalyze() {
        Result result = new Result();
        try {
            result.setData(sysLogService.logAnalyze());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("日志统计失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "是否返回错误日志")
    @RequestMapping(value = "/errLog", method = RequestMethod.GET)
    public Result errLog() {
        Result result = new Result();
        try {
            Map map=new HashMap();
            String errorLog = (String) redisTemplate.opsForValue().get("errorLog");
            if (StringUtils.isNotBlank(errorLog)) {
                redisTemplate.delete("errorLog");
                map.put("code",0);
                result.setData(map);
            } else {
                map.put("code",1);
                result.setData(map);
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("日志统计失败：" + e);
        }
        return  result;
    }


    @ApiOperation(value = "导出")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public Result export(@RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                         @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                         @RequestParam(value = "sortFlag", required = false) int sortFlag,
                         @RequestParam(value = "pageNum", required = false, defaultValue = "0") int pageNum,
                         @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,
                         HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");

            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
            }
            if ("14".equals(logType)) {
                logType = "";
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            if(sortFlag==1){
                List<SysLogDetail> list = sysLogService.selectByPageAsc(userName, title, startTime, endTime, logType);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }else{
                List<SysLogDetail> list = sysLogService.selectByPage(userName, title, startTime, endTime, logType);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
            Long userId = Long.valueOf(request.getHeader("userId"));
            String token = request.getHeader("token");
            String userNames=String.valueOf(redisTemplate.opsForHash().get("appKey:" + userId + ":" + token, "userName"));
            String iP=request.getHeader("HTTP_X_FORWARDED_FOR");
            this.insert("9",iP,"导出",1,"日志导出",userId,userNames,String.valueOf(request.getRequestURL()),request.getRequestURI(),request.getMethod());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
