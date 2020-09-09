package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.SysRoleRobot;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import com.yjh.platform.module.user.service.TCameraPresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * @author yc
 * @since 2020-08-18
 */
@RestController
@RequestMapping("/tCameraPreset/v1")
@Api(value = "/tCameraPreset", description = "摄像机预置位表操作接口")
public class TCameraPresetController {

    @Autowired
    private final TCameraPresetService tCameraPresetService;

    private Logger log = LoggerFactory.getLogger(TCameraPresetController.class);

    public TCameraPresetController(TCameraPresetService tCameraPresetService) {
        this.tCameraPresetService = tCameraPresetService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.insert(tCameraPreset));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加预置位信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.deleteByPrimaryId(presetId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/deleteSelectedPreset", method = RequestMethod.DELETE)
    public Result deleteSelectedPreset(@RequestParam(value = "presetIds") String presetIds) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.deleteSelectedPreset(presetIds));
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
    public Result update(@RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.update(tCameraPreset));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新预置位信息异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新预置位信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TCameraPreset tCameraPreset = tCameraPresetService.selectByPrimaryId(presetId);
            result.setData(tCameraPreset);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据摄像机id查询所有预置位信息")
    @RequestMapping(value = "/selectByCameraId", method = RequestMethod.GET)
    public Result selectByCameraId(@RequestParam(value = "cameraId", required = false) Long cameraId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCameraPreset> list = tCameraPresetService.selectByCameraId(cameraId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据预置位名称查询信息")
    @RequestMapping(value = "/selectByPresetName", method = RequestMethod.GET)
    public Result selectByPresetName(@RequestParam(value = "presetName", required = false) String presetName,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCameraPreset> list = tCameraPresetService.selectByPresetName(presetName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "presetId", required = false) Long presetId,
                         @RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "presetNum", required = false) Integer presetNum,
                         @RequestParam(value = "presetName", required = false) String presetName,
                         @RequestParam(value = "creatorUser", required = false) String creatorUser,
                         @RequestParam(value = "creatorTime", required = false) Date creatorTime,
                         @RequestParam(value = "isUse", required = false) Integer isUse,
                         @RequestParam(value = "presetImg", required = false) String presetImg,
                         @RequestParam(value = "inspectionPostion", required = false) Integer inspectionPostion,
                         @RequestParam(value = "collectStatus", required = false) Integer collectStatus,
                         @RequestParam(value = "calibrationStatus", required = false) Integer calibrationStatus) {
        Result result = new Result();
        try {
            List<TCameraPreset> list = tCameraPresetService.select(presetId, cameraId, presetNum, presetName, creatorUser, creatorTime, isUse, presetImg, inspectionPostion, collectStatus, calibrationStatus);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCameraPreset tCameraPreset,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCameraPresetExpand> list = tCameraPresetService.selectByPage(tCameraPreset);
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
    public Result batchInsert(@RequestBody List<TCameraPreset> list) {
        Result result = new Result();
        try {
        result.setData(tCameraPresetService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
