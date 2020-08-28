package com.yjh.platform.module.task.controller;

import com.yjh.platform.module.task.service.TCfgAlarmCurrentService;
import com.yjh.platform.module.task.entity.TCfgAlarmCurrent;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
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
 * @author czh
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tCfgAlarmCurrent/v1")
@Api(value = "/tCfgAlarmCurrent", description = "活动告警表操作接口")
public class TCfgAlarmCurrentController {

    @Autowired
    private final TCfgAlarmCurrentService tCfgAlarmCurrentService;

    private Logger log = LoggerFactory.getLogger(TCfgAlarmCurrentController.class);

    public TCfgAlarmCurrentController(TCfgAlarmCurrentService tCfgAlarmCurrentService) {
        this.tCfgAlarmCurrentService = tCfgAlarmCurrentService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCfgAlarmCurrent tCfgAlarmCurrent) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmCurrentService.insert(tCfgAlarmCurrent));
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
    public Result delete(@RequestParam(value = "alarmNo", required = true) Long alarmNo) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmCurrentService.deleteByPrimaryId(alarmNo));
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
    public Result update(@RequestBody TCfgAlarmCurrent tCfgAlarmCurrent) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmCurrentService.update(tCfgAlarmCurrent));
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
    public Result selectByPrimaryId(@RequestParam(value = "alarmNo", required = true) Long alarmNo) {
        Result result = new Result();
        try {
            TCfgAlarmCurrent tCfgAlarmCurrent = tCfgAlarmCurrentService.selectByPrimaryId(alarmNo);
            result.setData(tCfgAlarmCurrent);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "alarmNo", required = false) Long alarmNo,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "alarmTime", required = false) Date alarmTime,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "alarmValue", required = false) String alarmValue,
                            @RequestParam(value = "alarmDesc", required = false) String alarmDesc,
                            @RequestParam(value = "confirmState", required = false) Integer confirmState,
                            @RequestParam(value = "confirmPeople", required = false) String confirmPeople,
                            @RequestParam(value = "confirmTime", required = false) Date confirmTime,
                            @RequestParam(value = "confirmRemark", required = false) String confirmRemark,
                            @RequestParam(value = "defect", required = false) Integer defect,
                            @RequestParam(value = "defectLevel", required = false) Integer defectLevel,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "isClear", required = false) String isClear,
                            @RequestParam(value = "showType", required = false) String showType,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TCfgAlarmCurrent> list = tCfgAlarmCurrentService.select(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, meteCode, isClear, showType, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCfgAlarmCurrent tCfgAlarmCurrent,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCfgAlarmCurrent> list = tCfgAlarmCurrentService.selectByPage(tCfgAlarmCurrent);
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
    public Result batchInsert(@RequestBody List<TCfgAlarmCurrent> list) {
        Result result = new Result();
        try {
        result.setData(tCfgAlarmCurrentService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
