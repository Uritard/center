package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.input.DevicePermissionCommand;
import com.yjh.platform.module.user.service.DevicePermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/devicePermission/v1")
@Api(value = "/sysMenu", description = "设备权限接口")
public class DevicePermissionController {
    @Autowired
    private DevicePermissionService devicePermissionService;


    private Logger log = LoggerFactory.getLogger(DevicePermissionController.class);

    @PostMapping(value = "/insert")
    @ApiOperation(value = "插入")
    @Logs(title = "插入用户权限数据",content = "插入用户权限数据",logType = 2)
    public Result insert(@RequestBody DevicePermissionCommand devicePermissionCommand) {
        Result result = new Result();
        try {
            result.setData(devicePermissionService.insert(devicePermissionCommand));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("插入用户权限数据:", e);
        }
        return result;
    }


    @GetMapping(value = "/getSelectedByUserId/{userId}")
    @ApiOperation(value = "查询")
    @Logs(title = "查询用户权限数据",content = "查询用户权限数据",logType = 2)
    public Result getSelectedByUserId(@PathVariable("userId")Long id) {
        Result result = new Result();
        try {
            result.setData(devicePermissionService.getSelectedByUserId(id));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.QUERYERROR.getCode(), ResultCodeEnum.QUERYERROR.getName());
            log.error("查询用户权限数据:", e);
        }
        return result;
    }


}
