package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.service.TWiringDiagramService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
 * @author 丫C
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/tWiringDiagram/v1")
@Api(value = "/tWiringDiagram", tags = "主接线图表操作接口")
public class TWiringDiagramController {

    private final TWiringDiagramService tWiringDiagramService;

    private final Logger log = LoggerFactory.getLogger(TWiringDiagramController.class);
    private static final String USERID = "userId";

    public TWiringDiagramController(TWiringDiagramService tWiringDiagramService) {
        this.tWiringDiagramService = tWiringDiagramService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    public Result insert(MultipartFile file,
                         @RequestParam(value = "regionId", required = false) Long regionId,
                         @RequestParam(value = "regionName", required = false) String regionName,
                         HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = NumberUtils.toLong(request.getHeader(USERID));
            result = tWiringDiagramService.insert(file, regionId, regionName, userId, result);
        } catch (BusinessException e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("上传主接线图片异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("上传主接线图片错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    public Result delete(@RequestParam(value = "wiringDiagramId") Integer wiringDiagramId,
                         HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = NumberUtils.toLong(request.getHeader(USERID));
            result.setData(tWiringDiagramService.deleteByPrimaryId(wiringDiagramId, userId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除主接线图片异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除主接线图片错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @PostMapping(value = "/update")
    public Result update(MultipartFile file,
                         @RequestParam(value = "regionId", required = false) Long regionId,
                         @RequestParam(value = "regionName", required = false) String regionName,
                         HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = NumberUtils.toLong(request.getHeader(USERID));
            result = tWiringDiagramService.update(file, regionId, regionName, userId, result);
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("更新主接线图片异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新主接线图片错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @GetMapping(value = "/selectByPrimaryId")
    public Result selectByPrimaryId(@RequestParam(value = "wiringDiagramId") Integer wiringDiagramId) {
        Result result = new Result();
        try {
            result.setData(tWiringDiagramService.selectByPrimaryId(wiringDiagramId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "条件查询")
    @GetMapping(value = "/selectByCondition")
    public Result selectByCondition(@RequestParam(value = "regionId", required = false) Long regionId) {
        Result result = new Result();
        try {
            result.setData(tWiringDiagramService.selectByCondition(regionId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("条件查询失败描述：", e);
        }
        return result;
    }

}
