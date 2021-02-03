package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.service.TCameraScreenService;
import com.yjh.platform.module.user.entity.TCameraScreen;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


/**
 * @author lqh
 * @since 2020-10-16
 */
@RestController
@RequestMapping("/tCameraScreen/v1")
@Api(value = "/tCameraScreen", description = "分屏配置表操作接口")
public class TCameraScreenController {

    @Autowired
    private final TCameraScreenService tCameraScreenService;

    private Logger log = LoggerFactory.getLogger(TCameraScreenController.class);

    public TCameraScreenController(TCameraScreenService tCameraScreenService) {
        this.tCameraScreenService = tCameraScreenService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增分屏配置信息",content = "根据用户传递的参数新增分屏配置信息",logType = 2)
    public Result add(@RequestBody TCameraScreen tCameraScreen) {

        Result result = new Result();
        try {
            result.setData(tCameraScreenService.add(tCameraScreen));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除分屏配置信息",content = "根据用户传递的参数删除分屏配置信息",logType = 4)
    public Result delete(@RequestParam(value = "userId", required = true) Long userId) {
        Result result = new Result();
        try {
            result.setData(tCameraScreenService.deleteByPrimaryId(userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改分屏配置信息",content = "根据用户传递的参数修改分屏配置信息",logType = 3)
    public Result update(HttpServletRequest request,@RequestBody TCameraScreen tCameraScreen) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            result.setData(tCameraScreenService.update(tCameraScreen,userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询分屏配置信息",content = "根据用户传递的参数查询分屏配置信息",logType = 1)
    public Result selectByPrimaryId(HttpServletRequest request) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            result.setData(tCameraScreenService.selectByPrimaryId(Long.parseLong(userId)));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询分屏配置信息",content = "根据用户传递的参数查询分屏配置信息",logType = 1)
    public Result select(@RequestParam(value = "userId", required = false) Long userId,
                            @RequestParam(value = "screenNum", required = false) String screenNum,
                            @RequestParam(value = "cameraIds", required = false) String cameraIds,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TCameraScreen> list = tCameraScreenService.select(userId, screenNum, cameraIds, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询分屏配置信息",content = "根据用户传递的参数分页查询分屏配置信息",logType = 1)
    public Result selectByPage(@RequestBody TCameraScreen tCameraScreen,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraScreen> list = tCameraScreenService.selectByPage(tCameraScreen);
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
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入分屏配置信息",content = "根据用户传递的参数批量插入分屏配置信息",logType = 2)
    public Result batchAdd(@RequestBody List<TCameraScreen> list) {
        Result result = new Result();
        try {
        result.setData(tCameraScreenService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "批量删除分屏配置信息",content = "根据用户传递的参数批量删除分屏配置信息",logType = 4)
    public Result batchDelete(@RequestParam(value = "userIds") String userIds) {
    Result result = new Result();
    try {
        result.setData(tCameraScreenService.batchDelete(userIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "摄像机状态树")
    @RequestMapping(value = "/cameraStateTree", method = RequestMethod.GET)
    @Logs(title = "查询分屏配置信息",content = "根据用户传递的参数查询摄像机状态树",logType = 1)
    public Result cameraStateTree(@RequestParam(value = "cameraName",required = false) String cameraName,
                                  @RequestParam(value = "flag",required = false) Integer flag) {
        Result result = new Result();
        try {
            result.setData(tCameraScreenService.cameraStateTree(cameraName,flag));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
