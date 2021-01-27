package com.yjh.logs.module.log.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.logs.commons.logs.OperateLogDto;
import com.yjh.logs.commons.result.BusinessException;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.commons.result.ResultCodeEnum;
import com.yjh.logs.module.log.entity.SysOperateLog;
import com.yjh.logs.module.log.service.SysOperateLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tt
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/sysOperateLog/v1")
@Api(value = "/sysOperateLog", description = "审计日志表操作接口")
public class SysOperateLogController {

    @Autowired
    private final SysOperateLogService sysOperateLogService;

    private Logger log = LoggerFactory.getLogger(SysOperateLogController.class);

    public SysOperateLogController(SysOperateLogService sysOperateLogService) {
        this.sysOperateLogService = sysOperateLogService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody OperateLogDto operateLogDto) {
        Result result = new Result();
        try {
            result.setData(sysOperateLogService.insert(operateLogDto));
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
            result.setData(sysOperateLogService.deleteByPrimaryId(logId));
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
    public Result update(@RequestBody SysOperateLog sysOperateLog) {
        Result result = new Result();
        try {
            result.setData(sysOperateLogService.update(sysOperateLog));
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
            SysOperateLog sysOperateLog = sysOperateLogService.selectByPrimaryId(logId);
            result.setData(sysOperateLog);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询日志失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "logId", required = false) Long logId,
                         @RequestParam(value = "traceId", required = false) String traceId,
                         @RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "ip", required = false) String ip,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "requestOrigin", required = false) String requestOrigin,
                         @RequestParam(value = "requestPath", required = false) String requestPath,
                         @RequestParam(value = "requestMethod", required = false) Integer requestMethod,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<SysOperateLog> list = sysOperateLogService.select(logId, traceId, logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody SysOperateLog sysOperateLog,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<SysOperateLog> list = sysOperateLogService.selectByPage(sysOperateLog);
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
    public Result batchInsert(@RequestBody List<SysOperateLog> list) {
        Result result = new Result();
        try {
        result.setData(sysOperateLogService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
