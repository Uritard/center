package com.yjh.platform.module.devicemete.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.configuration.UserManager;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreview;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreviewDetail;
import com.yjh.platform.module.devicemete.service.ITCfgAutoreviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自动审核配置表 前端控制器
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
@RestController
@RequestMapping("/autoreview/v1")
@Api(tags = "自动审核配置")
@Slf4j
@RequiredArgsConstructor
public class TCfgAutoreviewController {
    private final ITCfgAutoreviewService autoreviewService;
    private final UserManager userManager;

    @ApiOperation(value = "查询自动审核列表")
    @Logs(title = "自动审核列表查询", content = "查询自动审核列表", logType = 1, authority = "1234")
    @GetMapping(value = "/selectByPage")
    public Result selectByPage(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String autoreviewName, @RequestParam(required = false) String autoreviewType,
        @RequestParam(required = false) String defectType, @RequestParam(required = false) String alarmType,
        @RequestParam(required = false) String deviceType, @RequestParam(required = false) String deviceName,
        @RequestParam(required = false) String meteName, @RequestParam(required = false) String labelAttri) {
        Result result = new Result();
        try {
            IPage<TCfgAutoreview> page = new Page<TCfgAutoreview>(pageNum, pageSize).addOrder(OrderItem.desc("create_time"));

            page = autoreviewService.selectByPage(page, autoreviewName, autoreviewType, defectType, alarmType, deviceType, deviceName,
                meteName, labelAttri);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", page.getTotal());
            resultMap.put("list", page.getRecords());

            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询自动审核列表：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "查询自动审核列表")
    @Logs(title = "自动审核列表查询", content = "查询自动审核列表", logType = 1, authority = "1234")
    @GetMapping(value = "/selectByPageMini")
    public Result selectByPageMini(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String autoreviewName, @RequestParam(required = false) String autoreviewType,
        @RequestParam(required = false, defaultValue = "-1") int autoDetailType, @RequestParam(required = false) String typeId,
        @RequestParam(required = false) String typeName) {
        Result result = new Result();
        try {
            IPage<TCfgAutoreview> page = new Page<TCfgAutoreview>(pageNum, pageSize).addOrder(OrderItem.desc("create_time"));

            page = autoreviewService.selectByPageMini(page, autoreviewName, autoreviewType, autoDetailType, typeId, typeName);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", page.getTotal());
            resultMap.put("list", page.getRecords());

            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询自动审核列表：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "查询自动审核详情")
    @Logs(title = "自动审核详情查询", content = "查询自动审核详情", logType = 1, authority = "1234")
    @GetMapping(value = "/selectDetail")
    public Result selectDetail(@RequestParam long autoreviewId, @RequestParam(required = false, defaultValue = "-1") int autoDetailType,
        @RequestParam(required = false) String typeId, @RequestParam(required = false) String typeName) {
        Result result = new Result();
        try {
            List<TCfgAutoreviewDetail> detailList = autoreviewService.selectDetailList(autoreviewId, autoDetailType, typeId, typeName);

            result.setData(detailList);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("查询自动审核详情：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "新增自动审核配置")
    @Logs(title = "自动审核新增", content = "新增自动审核配置", logType = 2, authority = "1234")
    @PostMapping(value = "/add")
    public Result add(@RequestBody TCfgAutoreview autoreview) {
        Result result = new Result();

        try {
            autoreview.setCreateUser(userManager.getUserName());
            result.setData(autoreviewService.addAutoreview(autoreview));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("添加自动审核出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }

        return result;
    }

    @ApiOperation(value = "更新自动审核配置")
    @Logs(title = "自动审核更新", content = "更新自动审核配置", logType = 3, authority = "1234")
    @PostMapping(value = "/update")
    public Result update(@RequestBody TCfgAutoreview autoreview) {
        Result result = new Result();
        try {
            result.setData(autoreviewService.updateAutoreview(autoreview));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("更新自动审核配置：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "删除自动审核配置")
    @Logs(title = "自动审核删除", content = "删除自动审核配置", logType = 4, authority = "1234")
    @PostMapping(value = "/delete")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(autoreviewService.removeAutoreview(id));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("删除自动审核出错：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
