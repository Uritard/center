package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.service.TCameraRecorderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yc
 * @since 2020-08-24
 */
@RestController
@RequestMapping("/tCameraRecorder/v1")
@Api(value = "/tCameraRecorder", description = "录像服务器表操作接口")
public class TCameraRecorderController {

    @Autowired
    private final TCameraRecorderService tCameraRecorderService;

    private Logger log = LoggerFactory.getLogger(TCameraRecorderController.class);

    public TCameraRecorderController(TCameraRecorderService tCameraRecorderService) {
        this.tCameraRecorderService = tCameraRecorderService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody @Validated TCameraRecorder tCameraRecorder) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.insert(tCameraRecorder));
            sendPostRequest(Constant.NVR_REGISTER_URL,tCameraRecorder.getRecordId());
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
    public Result delete(@RequestParam(value = "recordId", required = true) Long recordId) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.deleteByPrimaryId(recordId));
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
    @RequestMapping(value = "/deleteSelectedRecord", method = RequestMethod.DELETE)
    public Result deleteSelectedRecord(@RequestParam(value = "recordIds", required = true) String recordIds) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.deleteSelectedRecord(recordIds));
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
    public Result update(@RequestBody TCameraRecorder tCameraRecorder) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.update(tCameraRecorder));
            sendPostRequest(Constant.NVR_REGISTER_URL,tCameraRecorder.getRecordId());
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
    public Result selectByPrimaryId(@RequestParam(value = "recordId", required = true) Long recordId) {
        Result result = new Result();
        try {
            TCameraRecorderByDict tCameraRecorderByDict = tCameraRecorderService.selectByPrimaryId(recordId);
            result.setData(tCameraRecorderByDict);
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
                            @RequestParam(value = "recorderType", required = false) String recorderType,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "recordIp", required = false) String recordIp,
                            @RequestParam(value = "protocol", required = false) String protocol,
                            @RequestParam(value = "httpPort", required = false) Integer httpPort,
                            @RequestParam(value = "transPort", required = false) Integer transPort,
                            @RequestParam(value = "rtspPort", required = false) Integer rtspPort,
                            @RequestParam(value = "userName", required = false) String userName,
                            @RequestParam(value = "pwd", required = false) String pwd,
                            @RequestParam(value = "protocolUrl", required = false) String protocolUrl,
                            @RequestParam(value = "maxChannel", required = false) Integer maxChannel,
                            @RequestParam(value = "hddSize", required = false) Integer hddSize,
                            @RequestParam(value = "bufferDay", required = false) Integer bufferDay,
                            @RequestParam(value = "timeLong", required = false) Integer timeLong) {
        Result result = new Result();
        try {
            List<TCameraRecorderByDict> list = tCameraRecorderService.select(recordId, recordName, recorderType, aliasName, recordIp, protocol, httpPort, transPort, rtspPort, userName, pwd, protocolUrl, maxChannel, hddSize, bufferDay, timeLong);
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
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraRecorderByDict> list = tCameraRecorderService.selectByPage(tCameraRecorder);
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
    public Result batchInsert(@RequestBody List<TCameraRecorder> list) {
        Result result = new Result();
        try {
        result.setData(tCameraRecorderService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询Id和name")
    @RequestMapping(value = "/selectIdAndName", method = RequestMethod.GET)
    public Result selectIdAndName() {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.selectIdAndName());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询Id和name失败：" + e);
        }
        return result;
    }
    public Result sendPostRequest(String url,Long recordId) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,recordId);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

}
