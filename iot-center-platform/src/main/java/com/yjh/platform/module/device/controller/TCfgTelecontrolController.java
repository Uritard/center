package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TCfgTelecontrolService;
import com.yjh.platform.module.device.entity.TCfgTelecontrol;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
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
 * @since 2020-08-25
 */
@RestController
@RequestMapping("/tCfgTelecontrol/v1")
@Api(value = "/tCfgTelecontrol", description = "遥控量表操作接口")
public class TCfgTelecontrolController {

    @Autowired
    private final TCfgTelecontrolService tCfgTelecontrolService;

    private Logger log = LoggerFactory.getLogger(TCfgTelecontrolController.class);

    public TCfgTelecontrolController(TCfgTelecontrolService tCfgTelecontrolService) {
        this.tCfgTelecontrolService = tCfgTelecontrolService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TCfgTelecontrol tCfgTelecontrol) {
        Result result = new Result();
        try {
            result.setData(tCfgTelecontrolService.insert(tCfgTelecontrol));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥控量表操作添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            result.setData(tCfgTelecontrolService.deleteByPrimaryId(deviceId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("遥控量表操作删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("遥控量表操作删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCfgTelecontrol tCfgTelecontrol) {
        Result result = new Result();
        try {
            result.setData(tCfgTelecontrolService.update(tCfgTelecontrol));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("遥控量表操作更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量表操作更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            TCfgTelecontrol tCfgTelecontrol = tCfgTelecontrolService.selectByPrimaryId(deviceId);
            result.setData(tCfgTelecontrol);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "deviceId", required = false) String deviceId,
                            @RequestParam(value = "meteId", required = false) String meteId,
                            @RequestParam(value = "meteName", required = false) String meteName,
                            @RequestParam(value = "meteIndex", required = false) Integer meteIndex,
                            @RequestParam(value = "meteCid", required = false) Integer meteCid,
                            @RequestParam(value = "controlStatus", required = false) Integer controlStatus,
                            @RequestParam(value = "enableString", required = false) String enableString,
                            @RequestParam(value = "succeedString", required = false) String succeedString,
                            @RequestParam(value = "triggerString", required = false) String triggerString,
                            @RequestParam(value = "controlValue", required = false) Integer controlValue,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "deviceType", required = false) String deviceType,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "describer", required = false) String describer) {
        Result result = new Result();
        try {
            List<TCfgTelecontrol> list = tCfgTelecontrolService.select(deviceId, meteId, meteName, meteIndex, meteCid, controlStatus, enableString, succeedString, triggerString, controlValue, meteCode, deviceType, description, describer);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgTelecontrol tCfgTelecontrol,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgTelecontrol> list = tCfgTelecontrolService.selectByPage(tCfgTelecontrol);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("遥控量表操作失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TCfgTelecontrol> list) {
        Result result = new Result();
        try {
        result.setData(tCfgTelecontrolService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("遥控量表操作批量插入失败：" + e);
        }
        return result;
    }

}
