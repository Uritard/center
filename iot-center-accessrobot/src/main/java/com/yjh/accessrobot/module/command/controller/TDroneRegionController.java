package com.yjh.accessrobot.module.command.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.TDroneRegion;
import com.yjh.accessrobot.module.command.service.TDroneRegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author YC
 * @since 2021-01-28
 */
@RestController
@RequestMapping("/t-drone-region/v1")
@Api(value = "/t-drone-region", tags = "无人机区域层级表操作接口")
public class TDroneRegionController {

    @Autowired
    private final TDroneRegionService tDroneRegionService;

    private Logger log = LoggerFactory.getLogger(TDroneRegionController.class);

    public TDroneRegionController(TDroneRegionService tDroneRegionService) {
        this.tDroneRegionService = tDroneRegionService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TDroneRegion tDroneRegion) {
        Result result = new Result();
        try {
            result.setData(tDroneRegionService.insert(tDroneRegion));
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
    public Result delete(@RequestParam(value = "regionId", required = true) String regionId) {
        Result result = new Result();
        try {
            result.setData(tDroneRegionService.deleteByPrimaryId(regionId));
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
    public Result update(@RequestBody TDroneRegion tDroneRegion) {
        Result result = new Result();
        try {
            result.setData(tDroneRegionService.update(tDroneRegion));
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
    public Result selectByPrimaryId(@RequestParam(value = "regionId", required = true) String regionId) {
        Result result = new Result();
        try {
            TDroneRegion tDroneRegion = tDroneRegionService.selectByPrimaryId(regionId);
            result.setData(tDroneRegion);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "regionId", required = false) String regionId,
                         @RequestParam(value = "regionName", required = false) String regionName,
                         @RequestParam(value = "sort", required = false) Integer sort,
                         @RequestParam(value = "deviceType", required = false) Integer deviceType,
                         @RequestParam(value = "upRegionId", required = false) String upRegionId,
                         @RequestParam(value = "upRegionIds", required = false) String upRegionIds,
                         @RequestParam(value = "regionType", required = false) Integer regionType,
                         @RequestParam(value = "stationId", required = false) String stationId,
                         @RequestParam(value = "state", required = false) Integer state,
                         @RequestParam(value = "droneId", required = false) Long droneId,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TDroneRegion> list = tDroneRegionService.select(regionId, regionName, sort, deviceType, upRegionId, upRegionIds, regionType, stationId, state, droneId,createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TDroneRegion tDroneRegion
        ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tDroneRegion.getPageNum()!=null?tDroneRegion.getPageNum():1, tDroneRegion.getPageSize()!=null?tDroneRegion.getPageSize():100 );
            List<TDroneRegion> list = tDroneRegionService.selectByPage(tDroneRegion);
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
    public Result batchInsert(@RequestBody List<TDroneRegion> list) {
        Result result = new Result();
        try {
        result.setData(tDroneRegionService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
