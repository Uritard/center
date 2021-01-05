package com.yjh.logs.module.log.controller;

import com.yjh.logs.commons.utils.RandomUtil;
import com.yjh.logs.module.log.entity.SysLogsTime;
import com.yjh.logs.module.log.service.SysLogsService;
import com.yjh.logs.module.log.entity.SysLogs;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.commons.result.ResultCodeEnum;
import com.yjh.logs.commons.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


/**
 * @author tt
 * @since 2020-08-12
 */
@RestController
@RequestMapping("/sysLogs/v1")
@Api(value = "/sysLogs", description = "审计日志表操作接口")
public class SysLogsController {

    @Autowired
    private final SysLogsService sysLogsService;

    private Logger log = LoggerFactory.getLogger(SysLogsController.class);

    public SysLogsController(SysLogsService sysLogsService) {
        this.sysLogsService = sysLogsService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "ip", required = false) String ip,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName) {
        Result result = new Result();
        try {
            SysLogs sysLogs = new SysLogs();
            sysLogs.setLogType(logType);
            sysLogs.setIp(ip);
            sysLogs.setTitle(title);
            sysLogs.setState(state);
            sysLogs.setContent(content);
            sysLogs.setUserId(userId);
            sysLogs.setUserName(userName);
            Random random = new Random();
            new Thread(() -> {
                String mathRandom = String.valueOf(random.nextInt(50))+"000";
                long mathLong = Long.valueOf(mathRandom);
                log.info("mathLong: "+mathLong);
                try { Thread.sleep(mathLong); } catch (Exception e) {e.getMessage();}
                result.setData(sysLogsService.insert(sysLogs));
            }).start();
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/addBody", method = RequestMethod.POST)
    public Result addBody(HttpServletRequest request, @RequestBody SysLogs sysLogs) {
        Result result = new Result();
        try {
            Map<String, String[]> map = request.getParameterMap();
            System.out.println("map: "+ map);
            result.setData(sysLogsService.insert(sysLogs));
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
    public Result delete(@RequestParam(value = "logId", required = true) String logId) {
        Result result = new Result();
        try {
            result.setData(sysLogsService.deleteByPrimaryId(logId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除日志异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody SysLogs sysLogs) {
        Result result = new Result();
        try {
            result.setData(sysLogsService.update(sysLogs));
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
    public Result selectByPrimaryId(@RequestParam(value = "logId", required = true) String logId) {
        Result result = new Result();
        try {
            SysLogs sysLogs = sysLogsService.selectByPrimaryId(logId);
            result.setData(sysLogs);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "logId", required = false) String logId,
                         @RequestParam(value = "logType", required = false) String logType,
                         @RequestParam(value = "ip", required = false) String ip,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "createTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date createTime) {
        Result result = new Result();
        try {
            List<SysLogs> list = sysLogsService.select(logId, logType, ip, title, state, content, userId, userName, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody SysLogsTime sysLogsTime,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<SysLogs> list = sysLogsService.selectByPage(sysLogsTime);
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
    public Result batchInsert(@RequestBody List<SysLogs> list) {
        Result result = new Result();
        try {
            result.setData(sysLogsService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入日志失败：" + e);
        }
        return result;
    }

}
