package com.yjh.platform.module.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    public SysDiskCleanupController(ISysDiskCleanupService sysDiskCleanupService, RedisTemplate redisTemplate) {
        this.sysDiskCleanupService = sysDiskCleanupService;
        this.redisTemplate = redisTemplate;
    }

    @ApiOperation(value = "查询历史磁盘清理任务")
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
    @PostMapping(value = "/add")
    public Result add(@RequestBody SysDiskCleanup sysDiskCleanup, HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
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
    @PostMapping(value = "/confirm")
    public Result confirm(@RequestBody Map<String, Object> cleanMap) {
        Result result = new Result();
        try {
            int cleanId = MapUtils.getIntValue(cleanMap, "id");
            SysDiskCleanup cleanup = sysDiskCleanupService.getById(cleanId);
            if (cleanup.getCleanStatus() != 0) {
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
    public Result recovery(@RequestBody Map<String, Object> cleanMap, @RequestParam(value = "id") int cleanId,
        @RequestParam(value = "type") int type) {
        Result result = new Result();
        try {
            result.setData(
                sysDiskCleanupService.recoveryTask(MapUtils.getIntValue(cleanMap, "id"), MapUtils.getIntValue(cleanMap, "type")));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("恢复数据出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "删除备份")
    @PostMapping(value = "/deleteBack")
    public Result deleteBack(@RequestBody Map<String, Object> cleanMap) {
        Result result = new Result();
        try {
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
