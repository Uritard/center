package com.yjh.logs.module.log.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.logs.common.smUtil.Demo;
import com.yjh.logs.commons.result.BusinessException;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.commons.result.ResultCodeEnum;
import com.yjh.logs.commons.websocket.WebSocketServer;
import com.yjh.logs.module.log.entity.SysLog;
import com.yjh.logs.module.log.entity.SysLogDetail;
import com.yjh.logs.module.log.service.SysLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author tt
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/sysLog/v1")
@Api(value = "/sysLog", description = "审计日志表操作接口")
public class SysLogController {

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
                        ConcurrentHashMap<String, WebSocketServer> webSocketMap = WebSocketServer.getInstance().getWebSocketMap();
                        for (String us : webSocketMap.keySet()) {
                            webSocketMap.get(us).sendMessage(json);
                        }
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
            Map<String,String> enmap= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode =enmap.get("content");
            if("true".equals(isDecode)) {
                userName= Demo.decrypt(userName);
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
                               @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
                               @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endTime,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "0") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String,String> enmap= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
            String isDecode =enmap.get("content");
            if("true".equals(isDecode)) {
                userName= Demo.decrypt(userName);
            }
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<SysLogDetail> list = sysLogService.selectByPage(userName,title,startTime,endTime);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
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

}
