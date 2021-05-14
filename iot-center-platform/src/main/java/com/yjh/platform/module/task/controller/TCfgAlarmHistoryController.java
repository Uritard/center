package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.entity.TCfgAlarmHistoryResultInfo;
import com.yjh.platform.module.task.service.TCfgAlarmHistoryService;
import com.yjh.platform.module.task.entity.TCfgAlarmHistory;

import java.math.BigDecimal;
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
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author czh
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tCfgAlarmHistory/v1")
@Api(value = "/tCfgAlarmHistory", description = "历史告警表操作接口")
public class TCfgAlarmHistoryController {

    @Autowired
    private final TCfgAlarmHistoryService tCfgAlarmHistoryService;

    private Logger log = LoggerFactory.getLogger(TCfgAlarmHistoryController.class);

    public TCfgAlarmHistoryController(TCfgAlarmHistoryService tCfgAlarmHistoryService) {
        this.tCfgAlarmHistoryService = tCfgAlarmHistoryService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增历史告警数据",content = "根据用户传递的参数新增历史告警数据",logType = 2)
    public Result insert(@Validated @RequestBody TCfgAlarmHistory tCfgAlarmHistory) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmHistoryService.insert(tCfgAlarmHistory));
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
    @Logs(title = "删除历史告警数据",content = "根据用户传递的参数删除历史告警数据",logType = 4)
    public Result delete(@RequestParam(value = "alarmNo", required = true) Long alarmNo) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmHistoryService.deleteByPrimaryId(alarmNo));
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
    @Logs(title = "修改历史告警数据",content = "根据用户传递的参数修改历史告警数据",logType = 3)
    public Result update(@Validated @RequestBody TCfgAlarmHistory tCfgAlarmHistory) {
        Result result = new Result();
        try {
            result.setData(tCfgAlarmHistoryService.update(tCfgAlarmHistory));
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
    @Logs(title = "查询历史告警数据",content = "根据用户传递的参数查询历史告警信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "alarmNo", required = true) Long alarmNo) {
        Result result = new Result();
        try {
            TCfgAlarmHistory tCfgAlarmHistory = tCfgAlarmHistoryService.selectByPrimaryId(alarmNo);
            result.setData(tCfgAlarmHistory);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询历史告警数据",content = "根据用户传递的参数查询历史告警信息",logType = 1)
    public Result select(@RequestParam(value = "alarmNo", required = false) Long alarmNo,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "meteId", required = false) Long meteId,
                            @RequestParam(value = "alarmTime", required = false) Date alarmTime,
                            @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                            @RequestParam(value = "alarmValue", required = false) String alarmValue,
                            @RequestParam(value = "alarmDesc", required = false) String alarmDesc,
                            @RequestParam(value = "clearTime", required = false) Date clearTime,
                            @RequestParam(value = "clearValue", required = false) BigDecimal clearValue,
                            @RequestParam(value = "confirmState", required = false) Integer confirmState,
                            @RequestParam(value = "confirmPeople", required = false) String confirmPeople,
                            @RequestParam(value = "confirmTime", required = false) Date confirmTime,
                            @RequestParam(value = "confirmRemark", required = false) String confirmRemark,
                            @RequestParam(value = "defect", required = false) Integer defect,
                            @RequestParam(value = "defectLevel", required = false) Integer defectLevel,
                            @RequestParam(value = "forceClearReason", required = false) String forceClearReason,
                            @RequestParam(value = "meteCode", required = false) String meteCode,
                            @RequestParam(value = "isClear", required = false) String isClear,
                            @RequestParam(value = "showType", required = false) String showType,
                            @RequestParam(value = "updateTime", required = false) Date updateTime) {
        Result result = new Result();
        try {
            List<TCfgAlarmHistory> list = tCfgAlarmHistoryService.select(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc,
                    clearTime, clearValue, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, forceClearReason, meteCode,
                    isClear, showType, updateTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询历史告警数据",content = "根据用户传递的参数分页查询历史告警信息",logType = 1)
    public Result selectByPage(@RequestParam(value = "alarmNo", required = false) Long alarmNo,
                               @RequestParam(value = "deviceId", required = false) Long deviceId,
                               @RequestParam(value = "cunstomId", required = false) String cunstomId,
                               @RequestParam(value = "meteId", required = false) Long meteId,
                               @RequestParam(value = "alarmTime", required = false) Date alarmTime,
                               @RequestParam(value = "alarmLevel", required = false) Integer alarmLevel,
                               @RequestParam(value = "alarmValue", required = false) String alarmValue,
                               @RequestParam(value = "alarmDesc", required = false) String alarmDesc,
                               @RequestParam(value = "clearTime", required = false) Date clearTime,
                               @RequestParam(value = "clearValue", required = false) BigDecimal clearValue,
                               @RequestParam(value = "confirmState", required = false) Integer confirmState,
                               @RequestParam(value = "confirmPeople", required = false) String confirmPeople,
                               @RequestParam(value = "confirmTime", required = false) Date confirmTime,
                               @RequestParam(value = "confirmRemark", required = false) String confirmRemark,
                               @RequestParam(value = "defect", required = false) Integer defect,
                               @RequestParam(value = "defectLevel", required = false) Integer defectLevel,
                               @RequestParam(value = "forceClearReason", required = false) String forceClearReason,
                               @RequestParam(value = "meteCode", required = false) String meteCode,
                               @RequestParam(value = "isClear", required = false) String isClear,
                               @RequestParam(value = "showType", required = false) String showType,
                               @RequestParam(value = "updateTime", required = false) Date updateTime,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgAlarmHistory> list = tCfgAlarmHistoryService.selectByPage(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc,
                    clearTime, clearValue, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, forceClearReason, meteCode,
                    isClear, showType, updateTime);
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
    @Logs(title = "批量插入历史告警数据",content = "根据用户传递的参数批量插入历史告警数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCfgAlarmHistory> list) {
        Result result = new Result();
        try {
        result.setData(tCfgAlarmHistoryService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }


    @ApiOperation(value = "查询联动详细结果")
    @RequestMapping(value = "/selectUnionCruiseResult", method = RequestMethod.GET)
    @Logs(title = "查询联动详细结果",content = "查询联动详细结果",logType = 1)
    public Result selectUnionCruiseResult(@RequestParam(value = "unionId", required = false) String  unionId,
                                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCfgAlarmHistoryResultInfo> list = tCfgAlarmHistoryService.selectUnionCruiseResult(unionId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

}
