package com.yjh.platform.module.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DictConvertUtil;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import com.yjh.platform.module.config.service.ISysDiskCleanupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 磁盘清理记录 前端控制器
 * </p>
 *
 * @author Chenfei
 * @since 2023-06-30
 */
@Slf4j
@RestController
@RequestMapping("/diskCleanup/v1")
@Api(value = "/diskCleanup", tags = "磁盘清理接口")
public class SysDiskCleanupController {

    private final ISysDiskCleanupService sysDiskCleanupService;
    private final RedisTemplate redisTemplate;
    private final LogsRecord logsRecord;

    public SysDiskCleanupController(ISysDiskCleanupService sysDiskCleanupService, RedisTemplate redisTemplate, LogsRecord logsRecord) {
        this.sysDiskCleanupService = sysDiskCleanupService;
        this.redisTemplate = redisTemplate;
        this.logsRecord = logsRecord;
    }

    @ApiOperation(value = "查询历史磁盘清理任务")
    @Logs(title = "磁盘清理记录查询", content = "查询历史磁盘清理任务", logType = 1, authority = "1234")
    @GetMapping(value = "/selectByPage")
    public Result selectByPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        Result result = new Result();
        try {
            IPage<SysDiskCleanup> page = new Page<SysDiskCleanup>(pageNum, pageSize).addOrder(OrderItem.desc("create_time"));
            QueryWrapper<SysDiskCleanup> queryWrapper = new QueryWrapper<>(new SysDiskCleanup()).eq("clean_status", 2);
            page = sysDiskCleanupService.page(page, queryWrapper);
            DictConvertUtil.optional("backExpire").covertToDict(page.getRecords());

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", page.getTotal());
            resultMap.put("list", page.getRecords());

            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询历史磁盘清理任务出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "查询正在执行的磁盘清理任务")
    @Logs(title = "磁盘清理", content = "查询正在执行的磁盘清理任务", logType = 1, authority = "1234")
    @GetMapping(value = "/runningCleanup")
    public Result runningCleanup() {
        Result result = new Result();
        try {
            result.setData(sysDiskCleanupService.runningCleanup());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询正在执行的磁盘清理任务出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "新增磁盘清理任务")
    @Logs(title = "磁盘清理新增", content = "新增磁盘清理任务", logType = 2, authority = "1234")
    @PostMapping(value = "/add")
    public Result add(@RequestBody SysDiskCleanup sysDiskCleanup, HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            if (Constant.MANAGER_USER_ID != NumberUtils.toLong(userId, Constant.MANAGER_USER_ID)) {
                result.setMessage(ResultCodeEnum.CODE10008.getCode(), "仅系统默认管理员可进行磁盘清理操作");
                return result;
            }

            String userName = (String)redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
            sysDiskCleanup.setCreator(userName);
            result.setData(sysDiskCleanupService.addTask(sysDiskCleanup));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("新增磁盘清理任务失败：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "磁盘清理任务执行完成确认")
    @Logs(title = "磁盘清理确认", content = "磁盘清理任务执行完成确认", logType = 5, authority = "1234")
    @PostMapping(value = "/confirm")
    public Result confirm(@RequestBody Map<String, Object> cleanMap, HttpServletRequest request) {
        Result result = new Result();
        try {
            if (Constant.MANAGER_USER_ID != NumberUtils.toLong(request.getHeader("userId"), Constant.MANAGER_USER_ID)) {
                result.setMessage(ResultCodeEnum.CODE10008.getCode(), "仅系统默认管理员可进行磁盘清理确认");
                return result;
            }

            int cleanId = MapUtils.getIntValue(cleanMap, "id");
            SysDiskCleanup cleanup = sysDiskCleanupService.getById(cleanId);
            if (cleanup == null || cleanup.getCleanStatus() != 1) {
                result.setCode(ResultCodeEnum.CODE10009.getCode(), "当前任务未完成或已经确认，无法进行确认");
                return result;
            }
            boolean up = sysDiskCleanupService.update().eq("id", cleanId).set("clean_status", 2).set("update_time", new Date()).update();
            if (!up) {
                result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), "更新任务状态失败");
            }
            result.setData(up);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("磁盘清理任务执行完成确认出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "恢复数据")
    @PostMapping(value = "/recovery")
    public Result recovery(@RequestBody Map<String, Object> cleanMap, HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            String userName = (String)redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
            int type = MapUtils.getIntValue(cleanMap, "type");
            String name = type == 1 ? "数据库记录" : "数据文件";

            if (Constant.MANAGER_USER_ID != NumberUtils.toLong(userId, Constant.MANAGER_USER_ID)) {
                result.setMessage(ResultCodeEnum.CODE10008.getCode(), "仅系统默认管理员可进行数据恢复");
                logsRecord.LoginLogsSend(request, "3", "磁盘清理数据恢复", "恢复磁盘清理删除的" + name, userName, userId, 2);
                return result;
            }

            logsRecord.LoginLogsSend(request, "3", "磁盘清理数据恢复", "恢复磁盘清理删除的" + name, userName, userId, 1);

            result.setData(sysDiskCleanupService.recoveryTask(MapUtils.getIntValue(cleanMap, "id"), type));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("恢复数据出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "删除备份")
    @Logs(title = "磁盘清理备份删除", content = "删除磁盘清理备份的数据文件", logType = 4, authority = "1234")
    @PostMapping(value = "/deleteBack")
    public Result deleteBack(@RequestBody Map<String, Object> cleanMap, HttpServletRequest request) {
        Result result = new Result();
        try {
            if (Constant.MANAGER_USER_ID != NumberUtils.toLong(request.getHeader("userId"), Constant.MANAGER_USER_ID)) {
                result.setMessage(ResultCodeEnum.CODE10008.getCode(), "仅系统默认管理员可删除备份");
                return result;
            }

            result.setData(sysDiskCleanupService.deleteBack(MapUtils.getIntValue(cleanMap, "id")));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("删除备份出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
