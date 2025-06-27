package com.yjh.platform.module.simple.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.simple.entity.PatrolDeviceVersion;
import com.yjh.platform.module.simple.service.SimpleUpgradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述> 简易机器人远程升级接口
 *
 * @author shaobinfen
 * @date 2025/6/25
 * @since [产品/模块版本](可选)
 */
@Slf4j
@RestController
@RequestMapping("/remoteUpgrade/v1")
@Api(value = "/remoteUpgrade", tags = "简易机器人远程升级接口")
@RequiredArgsConstructor
public class SimpleUpgradeController {

    private final SimpleUpgradeService simpleUpgradeService;

    @ApiOperation(value = "版本升级包上传")
    @PostMapping(value = "/upgradePackageUpload")
    @Logs(title = "版本升级包上传", content = "根据用户传递的参数将版本升级包上传到指定目录", logType = 8)
    public Result upgradePackageUpload(@RequestParam(value = "file") MultipartFile file,
                                       @RequestParam(value = "robotType") Integer robotType,
                                       @RequestParam(value = "text") String text,
                                       HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            simpleUpgradeService.uploadUpgradeFile(file, robotType, text, userId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("简易机器人版本升级包上传失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "简易机器人远程升级接口")
    @GetMapping(value = "/remoteUpgrade")
    @Logs(title = "远程升级", content = "根据用户传递的参数向巡视设备发送远程升级指令", logType = 5, authority = "1234")
    public Result remoteUpgrade(@RequestParam(value = "id") Long id,
                                @RequestParam(value = "robotList") String robotList,
                                HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            simpleUpgradeService.upgradeNotify(id, robotList, userId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("简易机器人远程升级失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询巡视设备版本包信息", content = "根据用户传递的参数分页查询巡视设备版本包信息", logType = 1)
    public Result selectByPage(@RequestBody PatrolDeviceVersion patrolDeviceVersion) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(
                    patrolDeviceVersion.getPageNum() != null ? patrolDeviceVersion.getPageNum() : 1,
                    patrolDeviceVersion.getPageSize() != null ? patrolDeviceVersion.getPageSize() : 0,
                    true, null, true
            );
            List<PatrolDeviceVersion> list = simpleUpgradeService.selectPage(patrolDeviceVersion);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @Logs(title = "批量删除巡视设备版本包数据", content = "根据用户传递的参数批量删除巡视设备版本包数据", logType = 4)
    public Result batchDelete(@RequestParam(value = "ids") String ids) {
        Result result = new Result();
        try {
            simpleUpgradeService.batchDelete(ids);
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除失败：" + e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除错误:", e);
        }
        return result;
    }
}
