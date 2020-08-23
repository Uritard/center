package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.TCameraRecorderService;
import com.yjh.platform.module.user.entity.TCameraRecorder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author czh
 * @since 2020-08-13
 */
@RestController
@RequestMapping("/tCameraRecorder/v1")
@Api(value = "/tCameraRecorder", description = "摄像头录像机信息表操作接口")
public class   TCameraRecorderController {

    @Autowired
    private final TCameraRecorderService tCameraRecorderService;

    private Logger log = LoggerFactory.getLogger(TCameraRecorderController.class);

    public TCameraRecorderController(TCameraRecorderService tCameraRecorderService) {
        this.tCameraRecorderService = tCameraRecorderService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCameraRecorder tCameraRecorder) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.insert(tCameraRecorder));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增录像机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "recordId", required = true) Long recordId) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.deleteByPrimaryId(recordId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除录像机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除录像机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TCameraRecorder tCameraRecorder) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.update(tCameraRecorder));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新录像机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新录像机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "recordId", required = true) Long recordId) {
        Result result = new Result();
        try {
            TCameraRecorder tCameraRecorder = tCameraRecorderService.selectByPrimaryId(recordId);
            result.setData(tCameraRecorder);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "recordId", required = false) Long recordId,
                         @RequestParam(value = "recordName", required = false) String recordName,
                         @RequestParam(value = "aliasName", required = false) String aliasName,
                         @RequestParam(value = "recordIp", required = false) String recordIp,
                         @RequestParam(value = "protocal", required = false) String protocal,
                         @RequestParam(value = "httpPort", required = false) Integer httpPort,
                         @RequestParam(value = "transPort", required = false) Integer transPort,
                         @RequestParam(value = "rtspPort", required = false) Integer rtspPort,
                         @RequestParam(value = "userName", required = false) String userName,
                         @RequestParam(value = "pwd", required = false) String  pwd,
                         @RequestParam(value = "root", required = false) String root,
                         @RequestParam(value = "maxChannel", required = false) Integer maxChannel,
                         @RequestParam(value = "hddSize", required = false) Integer hddSize,
                         @RequestParam(value = "buffer_day", required = false) Integer buffer_day,
                         @RequestParam(value = "timeLong", required = false) Integer timeLong) {
        Result result = new Result();
        try {
            List<TCameraRecorder> list = tCameraRecorderService.select(recordId, recordName, aliasName, recordIp, protocal, httpPort, transPort, rtspPort, userName, pwd,root,maxChannel, hddSize, buffer_day, timeLong);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCameraRecorder tCameraRecorder,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCameraRecorder> list = tCameraRecorderService.selectByPage(tCameraRecorder);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
