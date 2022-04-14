package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.entity.TRecordFileInfo;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TRecordFileInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/4/11
 **/
@RestController
@RequestMapping("/tRecordFile/v1")
@Api(value = "/tRecordFile", description = "录像文件接口")
@Slf4j
public class TRecordFileInfoController {

    @Resource
    private TRecordFileInfoService tRecordFileInfoService;

    public TRecordFileInfoController(TRecordFileInfoService tRecordFileInfoService) {
        this.tRecordFileInfoService = tRecordFileInfoService;
    }

    @ApiOperation(value = "删除")
    @DeleteMapping(value = "/delete")
    @Logs(title = "删除录像文件", content = "根据用户传递的参数删除录像文件", logType = 4, authority = "1235")
    public Result delete(@RequestParam(value = "id", required = true) Long id) {
        Result result = new Result();
        try {
            int re = tRecordFileInfoService.deleteByPrimaryId(id);
            result.setData(re);
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除录像文件异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除录像文件错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "分页模糊查询")
    @GetMapping(value = "/selectByPage")
    @Logs(title = "查询录像文件", content = "根据用户传递的参数分页查询录像文件", logType = 1, authority = "1235")
    public Result selectByPage(@RequestParam(value = "cameraId", required = false) Long cameraId,
                               @RequestParam(value = "startTime", required = false) String startTime,
                               @RequestParam(value = "stopTime", required = false)  String endTime,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>(2);
        try {
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TRecordFileInfo> list = tRecordFileInfoService.selectByPage(cameraId, startTime, endTime);
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
