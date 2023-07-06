package com.yjh.platform.module.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import com.yjh.platform.module.config.service.ISysDiskCleanupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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

    public SysDiskCleanupController(ISysDiskCleanupService sysDiskCleanupService) {
        this.sysDiskCleanupService = sysDiskCleanupService;
    }

    @ApiOperation(value = "查询历史磁盘清理任务")
    @GetMapping(value = "/selectByPage")
    public Result selectByPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize) {
        Result result = new Result();
        try {
            IPage<SysDiskCleanup> page = new Page<SysDiskCleanup>(pageNum, pageSize).addOrder(OrderItem.desc("create_time"));
            QueryWrapper<SysDiskCleanup> queryWrapper = new QueryWrapper<>(new SysDiskCleanup()).eq("clean_status", 2);
            IPage<SysDiskCleanup> pages = sysDiskCleanupService.page(page, queryWrapper);

            result.setData(pages);
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
    public Result add(@RequestBody SysDiskCleanup sysDiskCleanup) {
        Result result = new Result();
        try {
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
    public Result confirm(@RequestParam(value = "id") int cleanId) {
        Result result = new Result();
        try {
            boolean up = sysDiskCleanupService.update().eq("id", cleanId).set("clean_status", 2).set("update_time", new Date()).update();
            if (up) {
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
    public Result recovery(@RequestParam(value = "id") int cleanId, @RequestParam(value = "type") int type) {
        Result result = new Result();
        try {
            result.setData(sysDiskCleanupService.recoveryTask(cleanId, type));
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
    public Result deleteBack(@RequestParam(value = "id") int cleanId) {
        Result result = new Result();
        try {
            result.setData(sysDiskCleanupService.deleteBack(cleanId));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("删除备份出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
