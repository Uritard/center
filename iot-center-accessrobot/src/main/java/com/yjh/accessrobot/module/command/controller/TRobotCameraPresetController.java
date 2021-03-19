package com.yjh.accessrobot.module.command.controller;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.service.TRobotCameraPresetService;
import com.yjh.accessrobot.module.command.entity.TRobotCameraPreset;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


/**
 * @author lqh
 * @since 2021-03-15
 */
@RestController
@RequestMapping("/tRobotCameraPreset/v1")
@Api(value = "/tRobotCameraPreset", description = "机器人预位置表操作接口")
public class TRobotCameraPresetController {

    @Autowired
    private final TRobotCameraPresetService tRobotCameraPresetService;
    @Autowired
    private RobotController robotController;

    private Logger log = LoggerFactory.getLogger(TRobotCameraPresetController.class);

    public TRobotCameraPresetController(TRobotCameraPresetService tRobotCameraPresetService) {
        this.tRobotCameraPresetService = tRobotCameraPresetService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(HttpServletRequest request, @RequestBody TRobotCameraPreset tRobotCameraPreset) {
        Result result = new Result();
        try {
            int i = tRobotCameraPresetService.add(tRobotCameraPreset);
            if(i == -1){
                result.setCode(209,"此点号已存在");
            }
            result = (robotController.feignRobotControl(request,tRobotCameraPreset.getRobotCode().toString(),"3","10",tRobotCameraPreset.getPresetNum().toString(),null,null,null));
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
    public Result delete(HttpServletRequest request,@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TRobotCameraPreset tRobotCameraPreset = tRobotCameraPresetService.selectByPrimaryId(presetId);
            int i = tRobotCameraPresetService.deleteByPrimaryId(presetId);
            result = (robotController.feignRobotControl(request,tRobotCameraPreset.getRobotCode().toString(),"3","12",tRobotCameraPreset.getPresetNum().toString(),null,null,null));
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
    public Result update(@RequestBody TRobotCameraPreset tRobotCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tRobotCameraPresetService.update(tRobotCameraPreset));
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
    public Result selectByPrimaryId(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TRobotCameraPreset tRobotCameraPreset = tRobotCameraPresetService.selectByPrimaryId(presetId);
            result.setData(tRobotCameraPreset);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "presetId", required = false) Long presetId,
                            @RequestParam(value = "presetNum", required = false) Integer presetNum,
                            @RequestParam(value = "robotId", required = false) Long robotId,
                            @RequestParam(value = "robotCode", required = false) String robotCode,
                            @RequestParam(value = "presetName", required = false) String presetName,
                            @RequestParam(value = "creatorTime", required = false) Date creatorTime,
                            @RequestParam(value = "cameraType", required = false) Integer cameraType) {
        Result result = new Result();
        try {
            List<TRobotCameraPreset> list = tRobotCameraPresetService.select(presetId,presetNum, robotId, robotCode, presetName, creatorTime, cameraType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "presetNum", required = false) Integer presetNum,
                                @RequestParam(value = "presetName", required = false) String presetName,
                                @RequestParam(value = "cameraId", required = false) Long cameraId,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TRobotCameraPreset> list = tRobotCameraPresetService.selectByPage(presetNum,presetName, cameraId);
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
    public Result batchAdd(@RequestBody List<TRobotCameraPreset> list) {
        Result result = new Result();
        try {
        result.setData(tRobotCameraPresetService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "presetIds") String presetIds) {
    Result result = new Result();
    try {
//        List<String> list1= Arrays.asList(presetIds.split(","));
//        for ()
        result.setData(tRobotCameraPresetService.batchDelete(presetIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "机器人预置位调用")
    @RequestMapping(value = "/controlPreset", method = RequestMethod.GET)
    public Result controlPreset(HttpServletRequest request,@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TRobotCameraPreset tRobotCameraPreset = tRobotCameraPresetService.selectByPrimaryId(presetId);
            result = (robotController.feignRobotControl(request,tRobotCameraPreset.getRobotCode().toString(),"3","7",tRobotCameraPreset.getPresetNum().toString(),null,null,null));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("机器人预置位调用异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人预置位调用错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "机器人预置位树")
    @RequestMapping(value = "/robotPresetTree", method = RequestMethod.GET)
    public Result robotPresetTree(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(this.tRobotCameraPresetService.robotPresetTree(robotId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
