package com.yjh.logs.module.log.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.logs.common.Constant;
import com.yjh.logs.common.logs.Logs;
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
import org.springframework.beans.factory.annotation.Value;
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
            result.setData(sysLogService.insert(logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
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
    @Logs(title = "查询日志", content = "根据用户传递的参数查询日志", logType = 1, authority = "1236")
    public Result selectByPage(@RequestParam(value = "userName", required = false) String userName,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "logType", required = false) String logType,
                               @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                               @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                               @RequestParam(value = "sortFlag", required = false) int sortFlag,
                               @RequestParam(value = "state", required = false) String state,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "0") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize,HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, String> enmap = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1236".equals(userRole)){
                //权限不够；
                throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            String isDecode = enmap.get("content");
            if ("true".equals(isDecode)) {
                userName = Demo.decrypt(userName);
            }
            if ("14".equals(logType)) {
                logType = "";
            }
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            if(sortFlag==1){
                List<SysLogDetail> list = sysLogService.selectByPageAsc(userName, title, startTime, endTime, logType, state);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }else{
                List<SysLogDetail> list = sysLogService.selectByPage(userName, title, startTime, endTime, logType, state);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
            //Long userId = Long.valueOf(request.getHeader("userId"));
            String userNames=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userId,"userName"));
            String iP=request.getHeader("HTTP_X_FORWARDED_FOR");
            this.insert("1",iP,"日志数据",1,"根据用户传递的参数查询日志数据",userId,userNames,String.valueOf(request.getRequestURL()),request.getRequestURI(),request.getMethod());
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
    @Logs(title = "日志统计", content = "根据用户传递的参数统计日志", logType = 1, authority = "1236")
    public Result logAnalyze(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1236".equals(userRole)){
                //权限不够；
                throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(sysLogService.logAnalyze());
//        } catch (BusinessException e) {
//            result.setMessage(10008, "用户无权限");
            //log.error("日志统计失败：" + e);
        }catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("日志统计失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "是否返回错误日志")
    @RequestMapping(value = "/errLog", method = RequestMethod.GET)
    public Result errLog(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            Map map=new HashMap();
            Integer roleId=Integer.valueOf((String) redisTemplate.opsForHash().get("userInfo:"+userId,"roleId"));
            if(roleId==1234||roleId==1236){
                String errorLog = (String) redisTemplate.opsForValue().get("errorLog");
                if (StringUtils.isNotBlank(errorLog)) {
                    map.put("content",errorLog);
                    redisTemplate.delete("errorLog");
                    map.put("code",0);
                    result.setData(map);
                }else{
                    map.put("code",1);
                    result.setData(map);
                }
            }else{
                map.put("code",1);
                result.setData(map);
            }
//            String errorLog = (String) redisTemplate.opsForValue().get("errorLog");
//            if (StringUtils.isNotBlank(errorLog)) {
//                map.put("state",errorLog);
//                redisTemplate.delete("errorLog");
//                map.put("code",0);
//                result.setData(map);
//            } else {
//                map.put("code",1);
//                result.setData(map);
//            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("日志统计失败：" + e);
        }
        return  result;
    }


    @ApiOperation(value = "导出")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @Logs(title = "日志导出", content = "根据用户传递的参数导出日志", logType = 9, authority = "1236")
    public Result export(@RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                         @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                         @RequestParam(value = "sortFlag", required = false) int sortFlag,
                         @RequestParam(value = "state", required = false) String state,
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
                List<SysLogDetail> list = sysLogService.selectByPageAsc(userName, title, startTime, endTime, logType, state);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }else{
                List<SysLogDetail> list = sysLogService.selectByPage(userName, title, startTime, endTime, logType, state);
                resultMap.put("count", page.getTotal());
                resultMap.put("list", list);
                result.setData(resultMap);
            }
            Long userId = Long.valueOf(request.getHeader("userId"));
            String token = request.getHeader("token");
            String userNames=String.valueOf(redisTemplate.opsForHash().get("appKey:" + userId + ":" + token, "userName"));
            String iP=request.getHeader("HTTP_X_FORWARDED_FOR");
//            this.insert("9",iP,"导出",1,"日志导出",userId,userNames,String.valueOf(request.getRequestURL()),request.getRequestURI(),request.getMethod());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
