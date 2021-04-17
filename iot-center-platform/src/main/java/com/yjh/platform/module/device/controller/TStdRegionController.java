package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.AreaInfoRegionCode;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.device.entity.TStdRegion;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author tt
 * @since 2020-07-27
 */
@RestController
@RequestMapping("/tStdRegion/v1")
@Api(value = "/tStdRegion", description = "标准区域表操作接口")
public class TStdRegionController {

    @Autowired
    private final TStdRegionService tStdRegionService;

    @Autowired
    private TStdDeviceService tStdDeviceService;

    private Logger log = LoggerFactory.getLogger(TStdRegionController.class);

    public TStdRegionController(TStdRegionService tStdRegionService) {
        this.tStdRegionService = tStdRegionService;
    }

    @ApiOperation(value = "新增区域")
    @RequestMapping(value = "/addRegion", method = RequestMethod.POST)
    @Logs(title = "新增区域",content = "根据用户传递的参数新增标准区域数据",logType = 2,authority = "1234")
    public Result insert(@Validated @RequestBody TStdRegion tStdRegion) {
        Result result = new Result();
        try {
            result.setData(tStdRegionService.insert(tStdRegion));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增区域错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除区域",content = "根据用户传递的参数删除标准区域数据",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "regionId", required = true) Long regionId) {
        Result result = new Result();
        try {
            int re  = tStdRegionService.deleteByPrimaryId(regionId);
            if(re == -1){
                result.setCode(209,"此区域下存在子区域或者设备");
            }else {
                result.setData(re);
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除区域异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除区域错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改区域",content = "根据用户传递的参数修改标准区域",logType = 3,authority = "1234")
    public Result update(@RequestBody TStdRegion tStdRegion) {
        Result result = new Result();
        try {
            result.setData(tStdRegionService.update(tStdRegion));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新区域异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新区域错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询区域",content = "根据用户传递的参数查询标准区域",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "regionId", required = true) Long regionId) {
        Result result = new Result();
        try {
            TStdRegion tStdRegion = tStdRegionService.selectByPrimaryId(regionId);
            result.setData(tStdRegion);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询区域",content = "根据用户传递的参数查询标准区域",logType = 1)
    public Result select(@RequestParam(value = "regionId", required = false) Long regionId,
                            @RequestParam(value = "regionName", required = false) String regionName,
                            @RequestParam(value = "sort", required = false) Integer sort,
                            @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                            @RequestParam(value = "upRegionIds", required = false) String upRegionIds,
                            @RequestParam(value = "regionCode", required = false) Integer regionCode,
                            @RequestParam(value = "stationId", required = false) String stationId,
                            @RequestParam(value = "state", required = false) Integer state,
                            @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TStdRegion> list = tStdRegionService.select(regionId, regionName, sort, upRegionId, upRegionIds, regionCode, stationId, state, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询区域",content = "根据用户传递的参数查询标准区域",logType = 1)
    public Result selectByPage(@RequestBody TStdRegion tStdRegion
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tStdRegion.getPageNum()!=null?tStdRegion.getPageNum():1, tStdRegion.getPageSize()!=null?tStdRegion.getPageSize():0, true,null,true);
            List<TStdRegion> list = tStdRegionService.selectByPage(tStdRegion);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("区域分页查询失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据区域名称模糊查询区域树")
    @RequestMapping(value = "/selectRegTreeByName", method = RequestMethod.GET)
    @Logs(title = "根据区域名称模糊查询区域树",content = "根据用区域名称模糊查询区域树",logType = 1,authority = "1234")
    public Result selectRegTreeByRegName(@RequestParam(value = "regionName", required = false) String regionName) {
        Result result = new Result();
        try {
            //List<AreaInfo> devTreeList = tStdDeviceService.selectRegionTreeByName(regionName);
            List<AreaInfoRegionCode> devTreeList = tStdRegionService.selectRegTreeByRegName(regionName);
            result.setData(devTreeList);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("区域树模糊查询失败：", e);
        }
        return result;
    }

}
