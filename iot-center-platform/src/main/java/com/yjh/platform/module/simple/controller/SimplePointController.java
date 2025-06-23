package com.yjh.platform.module.simple.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.simple.entity.BasePhotoBuild;
import com.yjh.platform.module.simple.entity.BasePhotoInfo;
import com.yjh.platform.module.simple.entity.InitialTask;
import com.yjh.platform.module.simple.entity.CalibrationDataBuild;
import com.yjh.platform.module.simple.service.SimplePointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RestController
@RequestMapping("/simplePoint/v1")
@Api(value = "/simplePoint", tags = "简易机器人接口")
@RequiredArgsConstructor
public class SimplePointController {

    private final SimplePointService simplePointService;

    @ApiOperation(value = "获取底图")
    @GetMapping(value = "/basePhoto")
    public Result basePhoto(@RequestParam(value = "deviceId", required = false) Long deviceId) {
        Result result = new Result();
        try {
            result.setData(simplePointService.basePhoto(deviceId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取底图接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "底图拆分")
    @PostMapping(value = "/splitPhoto")
    public Result splitPhoto(@RequestBody BasePhotoInfo basePhotoInfo) {
        Result result = new Result();
        try {
            result.setData(simplePointService.splitPhoto(basePhotoInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("底图拆分接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "底图保存")
    @PostMapping(value = "/basePhotoBuild")
    public Result basePhotoBuild(@RequestBody @Valid BasePhotoBuild photoBuild) {
        Result result = new Result();
        try {
            result.setData(simplePointService.basePhotoBuild(photoBuild));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("底图保存接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "插入si300任务")
    @RequestMapping(value = "/initTaskAdd", method = RequestMethod.POST)
    @Logs(title = "新增si300初始任务", content = "根据用户传递的参数新增初始任务", logType = 2, authority = "1234")
    public Result initTaskAdd(@RequestBody InitialTask initialTask) {
        Result result = new Result();
        try {
            Long robotId = initialTask.getRobotId();
            List<Long> devices = initialTask.getDevices();
            result.setData(simplePointService.initTaskAdd(devices,robotId));
        } catch (BusinessException e) {
            result.setMessage(e.getCode(), e.getMessage());
            log.error("新增si300初始任务错误: {}", e.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "查询初始任务状态")
    @RequestMapping(value = "/initTaskStatus", method = RequestMethod.POST)
    public Result initTaskStatus(@RequestParam(value = "robotId", required = false) Long robotId,
                                 @RequestParam(value = "regionId", required = false) Long regionId) {
        Result result = new Result();
        try {
            result.setData(simplePointService.initTaskStatus(robotId,regionId));
        } catch (BusinessException e) {
            result.setMessage(e.getCode(), e.getMessage());
            log.error("查询初始任务状态: {}", e.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "子点位标定数据保存")
    @PostMapping(value = "/calibrationDataBuild")
    public Result calibrationDataBuild(@RequestBody @Valid CalibrationDataBuild dataBuild) {
        Result result = new Result();
        try {
            result.setData(simplePointService.calibrationDataBuild(dataBuild));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("子点位标定数据保存接口调用错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "查询子点位标定数据")
    @GetMapping(value = "/selectCalibrationDataBuild")
    public Result selectCalibrationDataBuild(@RequestParam(value = "inspectionId") Long inspectionId) {
        Result result = new Result();
        try {
            result.setData(simplePointService.selectCalibrationDataBuild(inspectionId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("子点位标定数据保存接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "标定数据批量上传")
    @PostMapping(value = "/calibrationDataBatchUpload")
    public Result calibrationDataBatchUpload(@RequestBody List<Long> inspectionIds) {
        Result result = new Result();
        try {
            result.setData(simplePointService.calibrationDataBatchUpload(inspectionIds));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("标定数据批量上传接口调用错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "算法测试识别")
    @GetMapping(value = "/analyseTest")
    public Result analyseTest(HttpServletRequest request, @RequestParam(value = "inspectionId", required = false) Long inspectionId) {
        Result result = new Result();
        try {
            String userIdStr = request.getHeader("userId");
            Long userId = NumberUtils.toLong(userIdStr, 10001L);
            result.setData(simplePointService.analyseTest(inspectionId, userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("算法测试识别接口调用错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "查询巡视点位的算法分析结果数据")
    @GetMapping(value = "/selectMeterAnalyseResult")
    public Result selectMeterAnalyseResult(@RequestParam(value = "inspectionId", required = false) Long inspectionId) {
        Result result = new Result();
        try {
            result.setData(simplePointService.selectMeterAnalyseResult(inspectionId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询巡视点位的结果数据失败描述：", e);
        }
        return result;
    }
}
