package com.yjh.platform.module.user.controller;

import com.yjh.platform.module.user.entity.TCameraGroupDetail;
import com.yjh.platform.module.user.service.TCameraGroupService;
import com.yjh.platform.module.user.entity.TCameraGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
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


/**
 * @author lqh
 * @since 2020-11-17
 */
@RestController
@RequestMapping("/tCameraGroup/v1")
@Api(value = "/tCameraGroup", description = "相机分组表操作接口")
public class TCameraGroupController {

    @Autowired
    private final TCameraGroupService tCameraGroupService;

    private Logger log = LoggerFactory.getLogger(TCameraGroupController.class);

    public TCameraGroupController(TCameraGroupService tCameraGroupService) {
        this.tCameraGroupService = tCameraGroupService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody @Validated TCameraGroup tCameraGroup) {
        Result result = new Result();
        try {
            result.setData(tCameraGroupService.add(tCameraGroup));
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
    public Result delete(@RequestParam(value = "groupId", required = true) Long groupId) {
        Result result = new Result();
        try {
            result.setData(tCameraGroupService.deleteByPrimaryId(groupId));
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
    public Result update(@RequestBody TCameraGroup tCameraGroup) {
        Result result = new Result();
        try {
            result.setData(tCameraGroupService.update(tCameraGroup));
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
    public Result selectByPrimaryId(@RequestParam(value = "groupId", required = true) Long groupId) {
        Result result = new Result();
        try {
            if(groupId == -1){
                result.setData(new ArrayList<>());
            }else {
                TCameraGroupDetail tCameraGroupDetail = tCameraGroupService.selectByPrimaryId(groupId);
                result.setData(tCameraGroupDetail);
            }

        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "groupId", required = false) Long groupId,
                            @RequestParam(value = "groupName", required = false) String groupName,
                            @RequestParam(value = "cameraIds", required = false) String cameraIds,
                            @RequestParam(value = "remarks", required = false) String remarks) {
        Result result = new Result();
        try {
            List<TCameraGroup> list = tCameraGroupService.select(groupId, groupName, cameraIds, remarks);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCameraGroup tCameraGroup,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraGroup> list = tCameraGroupService.selectByPage(tCameraGroup);
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
    public Result batchAdd(@RequestBody List<TCameraGroup> list) {
        Result result = new Result();
        try {
        result.setData(tCameraGroupService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "groupIds") String groupIds) {
    Result result = new Result();
    try {
        result.setData(tCameraGroupService.batchDelete(groupIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }


    @ApiOperation(value = "获取分组树")
    @RequestMapping(value = "/groupTree", method = RequestMethod.GET)
    public Result groupTree() {
        Result result = new Result();
        try {
            result.setData(tCameraGroupService.groupTree());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("获取分组树失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "获取所有的相机")
    @RequestMapping(value = "/selectAllCamera", method = RequestMethod.GET)
    public Result selectAllCamera() {
        Result result = new Result();
        try {
            result.setData(tCameraGroupService.selectAllCamera());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("获取分组树失败描述：", e);
        }
        return result;
    }

//    @ApiOperation(value = "获取相机树")
//    @RequestMapping(value = "/cameraTree", method = RequestMethod.GET)
//    public Result cameraTree() {
//        Result result = new Result();
//        try {
//            result.setData(tCameraGroupService.cameraTree());
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
//            log.error("获取分组树失败描述：", e);
//        }
//        return result;
//    }
}
