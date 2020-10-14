package com.yjh.platform.module.task.controller;

import com.google.common.base.FinalizablePhantomReference;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TWarnInfoService;

import java.util.HashMap;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
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
 * @author czh
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tWarnInfo/v1")
@Api(value = "/tWarnInfo", description = "告警信息表操作接口")
public class TWarnInfoController {

    @Autowired
    private final TWarnInfoService tWarnInfoService;
    @Autowired
    private  SysUserDao sysUserDao;

    private Logger log = LoggerFactory.getLogger(TWarnInfoController.class);

    public TWarnInfoController(TWarnInfoService tWarnInfoService) {
        this.tWarnInfoService = tWarnInfoService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.insert(tWarnInfo));
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
    public Result delete(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.deleteByPrimaryId(warnId));
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
    public Result update(@RequestBody TWarnInfo tWarnInfo) {
        Result result = new Result();
        try {
            result.setData(tWarnInfoService.update(tWarnInfo));
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
    public Result selectByPrimaryId(@RequestParam(value = "warnId", required = true) Long warnId) {
        Result result = new Result();
        try {
            TWarnInfo tWarnInfo = tWarnInfoService.selectByPrimaryId(warnId);
            result.setData(tWarnInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "warnId", required = false) Long warnId,
                            @RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                            @RequestParam(value = "warnTime", required = false) Date warnTime,
                            @RequestParam(value = "warnType", required = false) Integer warnType,
                            @RequestParam(value = "deviceId", required = false) Long deviceId,
                            @RequestParam(value = "cunstomId", required = false) String cunstomId,
                            @RequestParam(value = "instanceId", required = false) Long instanceId,
                            @RequestParam(value = "stdMeteId", required = false) Long stdMeteId,
                            @RequestParam(value = "confMode", required = false) Integer confMode,
                            @RequestParam(value = "confTime", required = false) Date confTime,
                            @RequestParam(value = "confUserId", required = false) String confUserId,
                            @RequestParam(value = "confInfo", required = false) String confInfo,
                            @RequestParam(value = "ifWarnDisable", required = false) Integer ifWarnDisable,
                            @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                            @RequestParam(value = "warnSubtype", required = false) Integer warnSubtype,
                            @RequestParam(value = "deviceCode", required = false) String deviceCode,
                            @RequestParam(value = "imagePath", required = false) String imagePath,
                            @RequestParam(value = "videoPath", required = false) String videoPath,
                            @RequestParam(value = "value", required = false) String value,
                            @RequestParam(value = "defect", required = false) Integer defect,
                            @RequestParam(value = "defectLevel", required = false) Integer defectLevel,
                            @RequestParam(value = "outRange", required = false) String outRange,
                            @RequestParam(value = "linkMessage", required = false) String linkMessage) {
        Result result = new Result();
        try {
            List<TWarnInfo> list = tWarnInfoService.select(warnId, warnLevel, warnTime, warnType, deviceId, cunstomId, instanceId, stdMeteId, confMode, confTime, confUserId, confInfo, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, defect, defectLevel, outRange, linkMessage);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    public Result batchInsert(@RequestBody List<TWarnInfo> list) {
        Result result = new Result();
        try {
        result.setData(tWarnInfoService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询所有告警")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                            @RequestParam(value = "confMode", required = false) Integer confMode,
                            @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                            @RequestParam(value = "startTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                            @RequestParam(value = "edTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                            @RequestParam(value = "deviceName", required = false) String deviceName,
                            @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                            @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TWarnInfoDetail> list = tWarnInfoService.selectByPage(warnLevel, confMode, alarmSource,startTime,endTime,deviceName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有告警失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "统计近一周的所有告警数据")
    @RequestMapping(value = "/countOnWeek", method = RequestMethod.GET)
    public Result countOnWeek(@RequestParam(value = "warnLevel", required = false) Integer warnLevel,
                                      @RequestParam(value = "confMode", required = false) Integer confMode,
                                      @RequestParam(value = "alarmSource", required = false) Integer alarmSource,
                                      @RequestParam(value = "value", required = false) String value,
                                      @RequestParam(value = "startTime", required = false)  @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startTime,
                                      @RequestParam(value = "edTime", required = false)@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
                                      @RequestParam(value = "deviceName", required = false) String deviceName,
                                      @RequestParam(value = "deviceType", required = false) Integer deviceType){
        Result result = new Result();
        try {
            List<TutHistoryStatistical> list = tWarnInfoService.countOnWeek(warnLevel, confMode, alarmSource, value,startTime,endTime,deviceName,deviceType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "根据设备类型统计告警数据")
    @RequestMapping(value = "/countByDeviceType", method = RequestMethod.GET)
    public Result countByDeviceType(){
        Result result = new Result();
        try {
            List<DevicetypeDetail> list = tWarnInfoService.countByDeviceType();
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("统计告警数据失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "确认，忽略")
    @RequestMapping(value = "/updateForOk", method = RequestMethod.GET)
    public Result updateForOk(//HttpServletRequest httpServletRequest,
                              @RequestParam(value = "warnIds", required = false) String warnIds,
                              @RequestParam(value = "confMode", required = false) Integer confMode){
//HttpServletRequest httpServletRequest,
        Result result = new Result();
        try {
//            Long userId = Long.valueOf(httpServletRequest.getHeader("userId"));
//            SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
//            String name = sysUser.getUserName();
            Long userId = 10001L;
            result.setData(this.tWarnInfoService.updateForOk(userId,warnIds,confMode));

        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("确认，忽略失败描述：", e);
        }
        return result;
    }


}
