package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TRobotDeviceConfig;
import com.yjh.platform.module.user.service.TRobotDeviceConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/7/5
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tRobotDeviceConfig/v1")
@Api(value = "/tRobotDeviceConfig", tags = "机器人与设备关联表操作接口")
@Slf4j
public class TRobotDeviceConfigController {

    @Resource
    private TRobotDeviceConfigService tRobotDeviceConfigService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增机器人设备绑定信息", content = "根据用户传递的参数新增机器人设备绑定信息", logType = 2, authority = "1234")
    public Result insert(@Validated @RequestBody TRobotDeviceConfig tRobotDeviceConfig, HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));
            result.setData(tRobotDeviceConfigService.insert(tRobotDeviceConfig, userName));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
            log.error("添加机器人设备绑定错误:", b);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加机器人设备绑定错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    @Logs(title = "删除机器人设备绑定信息", content = "根据用户传递的参数删除机器人设备绑定信息", logType = 2, authority = "1234")
    public Result delete(@RequestParam(value = "deviceConfigId") Long deviceConfigId) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.deleteByPrimaryId(deviceConfigId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
            log.error("取消机器人设备绑定错误:", b);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("取消机器人设备绑定错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "一键删除")
    @PostMapping(value = "/deleteByRobotId")
    @Logs(title = "删除机器人设备绑定信息", content = "一键删除机器人设备绑定信息", logType = 2, authority = "1234")
    public Result deleteByRobotId(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.deleteByRobotId(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
            log.error("取消机器人设备绑定错误:", b);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("取消机器人设备绑定错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @PostMapping(value = "/update")
    @Logs(title = "更新机器人设备绑定信息", content = "根据用户传递的参数更新机器人设备绑定信息", logType = 2, authority = "1234")
    public Result update(@Validated @RequestBody TRobotDeviceConfig tRobotDeviceConfig, HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            String userName = String.valueOf(redisTemplate.opsForHash().get("userInfo:" + userId, "userName"));
            result.setData(tRobotDeviceConfigService.updateByPrimaryKey(tRobotDeviceConfig, userName));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
            log.error("更新机器人设备绑定错误:", b);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新机器人设备绑定错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @GetMapping(value = "/selectByPrimaryId")
    public Result selectByPrimaryId(@RequestParam(value = "deviceConfigId") Long deviceConfigId) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.selectByPrimaryKey(deviceConfigId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "条件查询")
    @GetMapping(value = "/selectDeviceConfigByRobotId")
    public Result selectDeviceConfigByRobotId(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.selectDeviceConfigByRobotId(robotId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("条件查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询机柜与地图点绑定信息")
    @GetMapping(value = "/selectMapNodeDeviceByRobotId")
    public Result selectMapNodeDeviceByRobotId(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.selectMapNodeDeviceByRobotId(robotId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("条件查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新地图入口处")
    @PostMapping(value = "/updateEntrance")
    public Result updateEntrance(@RequestParam(value = "robotId") Long robotId,
                                 @RequestParam(value = "entrance") Integer entrance) {
        Result result = new Result();
        try {
            result.setData(tRobotDeviceConfigService.updateEntrance(robotId, entrance));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "设备树查询")
    @RequestMapping(value = "/selectDevTree", method = RequestMethod.GET)
    @Logs(title = "设备树查询",content = "设备树查询",logType = 1)
    public Result selectDeviceTree() {
        Result result = new Result();
        try {
            List<AreaInfo> devTreeList = tRobotDeviceConfigService.selectDeviceTree();
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
