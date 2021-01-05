package com.yjh.platform.module.user.controller;

import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.user.service.TRobotInfoService;
import com.yjh.platform.module.user.entity.TRobotInfo;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;



/**
 * @author tt
 * @since 2020-08-05
 */
@RestController
@RequestMapping("/tRobotInfo/v1")
@Api(value = "/tRobotInfo", description = "机器人表操作接口")
public class TRobotInfoController {

    @Autowired
    private final TRobotInfoService tRobotInfoService;

    @Autowired
    private final TStdDeviceService tStdDeviceService;

    private Logger log = LoggerFactory.getLogger(TRobotInfoController.class);

    public TRobotInfoController(TRobotInfoService tRobotInfoService, TStdDeviceService tStdDeviceService) {
        this.tRobotInfoService = tRobotInfoService;
        this.tStdDeviceService = tStdDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(HttpServletRequest request, @RequestBody TRobotInfo tRobotInfo) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            tRobotInfo.setCreateBy(userId);

            if(tRobotInfo.getRobotIp()!= null && !"".equals(tRobotInfo.getRobotIp())){
                String robotIp = tRobotInfo.getRobotIp();
                if (!robotIp.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                    result.setCode(ResultCodeEnum.CODE10104.getCode(), ResultCodeEnum.CODE10104.getName());
                }
            }
            if(tRobotInfo.getRobotPort()!=null && !"".equals(tRobotInfo.getRobotPort())){
                String robotPort = tRobotInfo.getRobotPort().toString();
                if (!robotPort.matches("^([1-9]|[1-9]\\d{1,3}|[1-6][0-5][0-5][0-3][0-5])$")) {
                    result.setCode(ResultCodeEnum.CODE10105.getCode(), ResultCodeEnum.CODE10105.getName());
                }
            }
           result.setData(tRobotInfoService.insert(tRobotInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加机器人错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "robotId", required = true) Long robotId) {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.deleteByPrimaryId(robotId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除机器人异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除机器人错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(HttpServletRequest request, @RequestBody TRobotInfo tRobotInfo) {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            tRobotInfo.setUpdateBy(userId);
            result.setData(tRobotInfoService.update(tRobotInfo));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新机器人异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "robotId", required = true) Long robotId) {
        Result result = new Result();
        try {
            TRobotInfo tRobotInfo = tRobotInfoService.selectByPrimaryId(robotId);
            result.setData(tRobotInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "robotCode", required = false) String robotCode,
                         @RequestParam(value = "robotName", required = false) String robotName,
                         @RequestParam(value = "robotStatus", required = false) String robotStatus,
                         @RequestParam(value = "robotType", required = false) Integer robotType,
                         @RequestParam(value = "robotIp", required = false) String robotIp,
                         @RequestParam(value = "robotPort", required = false) Integer robotPort,
                         @RequestParam(value = "upRegionName", required = false) String upRegionName,
                         @RequestParam(value = "lightIp", required = false) String lightIp,
                         @RequestParam(value = "lightPort", required = false) String lightPort,
                         @RequestParam(value = "lightUsername", required = false) String lightUsername,
                         @RequestParam(value = "lightPassword", required = false) String lightPassword,
                         @RequestParam(value = "lnferadIp", required = false) String lnferadIp,
                         @RequestParam(value = "inferadPort", required = false) Integer inferadPort,
                         @RequestParam(value = "inferadUsername", required = false) String inferadUsername,
                         @RequestParam(value = "inferadPassword", required = false) String inferadPassword,
                         @RequestParam(value = "photePath", required = false) String photePath,
                         @RequestParam(value = "createBy", required = false) String createBy,
                         @RequestParam(value = "createDate", required = false) Date createDate,
                         @RequestParam(value = "updateBy", required = false) String updateBy,
                         @RequestParam(value = "updateDate", required = false) Date updateDate,
                         @RequestParam(value = "robotFactory", required = false) String robotFactory,
                         @RequestParam(value = "isUse", required = false) String isUse,
                         @RequestParam(value = "commissionDate", required = false) Date commissionDate,
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                         @RequestParam(value = "robotPosition", required = false) String robotPosition,
                         @RequestParam(value = "remarks", required = false) String remarks) {
        Result result = new Result();
        try {
            List<TRobotInfo> list = tRobotInfoService.select(robotId, robotCode, robotName, robotStatus, robotType, robotIp, robotPort, upRegionName, lightIp, lightPort, lightUsername, lightPassword, lnferadIp, inferadPort, inferadUsername, inferadPassword, photePath, createBy, createDate, updateBy, updateDate, robotFactory, isUse, commissionDate, upRegionId, robotPosition, remarks);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页模糊查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TRobotInfo tRobotInfo,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> upRegionIds = tStdDeviceService.selectRegionIdTree(tRobotInfo.getUpRegionId());
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TRobotInfo> list = tRobotInfoService.selectByPage(tRobotInfo,upRegionIds);
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
    public Result batchInsert(@RequestBody List<TRobotInfo> list) {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询所有机器人巡检点信息树")
    @RequestMapping(value = "/selectInspectionTree", method = RequestMethod.GET)
    public Result selectInspectionTree() {
        Result result = new Result();
        try {
            List<Map<String, Object>> robotInspectionTree = tRobotInfoService.selectInspectionTree();
            result.setData(robotInspectionTree);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有机器人巡检点信息树失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "robotIds", required = true) String robotIds) {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.batchDelete(robotIds));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }
}
