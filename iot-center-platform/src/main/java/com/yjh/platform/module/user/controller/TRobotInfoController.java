package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.service.TStdDeviceService;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.module.user.service.TRobotInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-05
 */
@RestController
@RequestMapping("/tRobotInfo/v1")
@Api(value = "/tRobotInfo")
public class TRobotInfoController {

    @Autowired
    private final TRobotInfoService tRobotInfoService;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private LogsRecord logsRecord;

    @Autowired
    private final TStdDeviceService tStdDeviceService;

    @Autowired
    private RedisTemplate redisTemplate;
    private Logger log = LoggerFactory.getLogger(TRobotInfoController.class);

    public TRobotInfoController(TRobotInfoService tRobotInfoService, TStdDeviceService tStdDeviceService) {
        this.tRobotInfoService = tRobotInfoService;
        this.tStdDeviceService = tStdDeviceService;
    }

    /**
     * 新增机器人或无人机履历信息
     *
     * @param request request
     * @param tRobotInfo tRobotInfo
     * @return result
     */
    @ApiOperation(value = "插入履历信息")
    @PostMapping(value = "/addResumeInfo")
    @Logs(title = "新增履历信息",content = "根据用户传递的参数新增机器人或无人机履历信息",logType = 2,authority = "1234", codeName="droneType")
    public Result insertRobotResumeInfo(HttpServletRequest request, @Validated @RequestBody TRobotInfo tRobotInfo) {
        Result result = new Result();
        try {
            TRobotInfo robotInfoTemp = tRobotInfoService.selectByPrimaryId(tRobotInfo.getRobotId());
            // 从robotInfoTemp获取 缺陷记录、大修记录、退出再重放记录内容，并追加到tRobotInfo中
            result.setData(tRobotInfoService.updateRobotResumeInfo(tRobotInfo, robotInfoTemp));
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }

        return result;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增机器人信息",content = "根据用户传递的参数新增机器人信息",logType = 2,authority = "1234", codeName="droneType")
    public Result insert(HttpServletRequest request, @Validated @RequestBody  TRobotInfo tRobotInfo) {

        Result result = new Result();
        try {
            tRobotInfo.setEdgeCode((String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeCode", "content"));
            Long userId = Long.valueOf(request.getHeader("userId"));
            if (tRobotInfo.getRobotIp() != null && !"".equals(tRobotInfo.getRobotIp())) {
                String robotIp = tRobotInfo.getRobotIp();
                if (!robotIp.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                    result.setCode(ResultCodeEnum.CODE10104.getCode(), ResultCodeEnum.CODE10104.getName());
                }
            }
            Integer robotPort = tRobotInfo.getRobotPort();
            if (Objects.nonNull(robotPort)) {
                if (robotPort < 1 || robotPort > 65535) {
                    result.setCode(ResultCodeEnum.CODE10105.getCode(), ResultCodeEnum.CODE10105.getName());
                }
            }
            List<String> allRobotCodeList = tRobotInfoService.selectAllRobotCode2();
            if (allRobotCodeList.contains(tRobotInfo.getRobotCode())){
                result.setMessage(209, "该实物ID已存在，不可重复");
                return result;
            }
            List<String> allRobotNumList = tRobotInfoService.selectAllRobotNum();
            if (allRobotNumList.contains(tRobotInfo.getRobotNum())){
                result.setMessage(209, "该设备编码已存在，不可重复");
                return result;
            }
            result.setData(tRobotInfoService.insert(tRobotInfo,userId));
            //有变动 同步模型
            String modelType = tRobotInfo.getDroneType() != null ? "5" : "3";
            Constant.modelUpload("1001");
            Constant.modelUpload(modelType);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加机器人错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    // @Logs(title = "删除机器人信息",content = "根据用户传递的参数删除机器人信息",logType = 4,authority = "1234", codeName="droneType")
    public Result delete(@RequestParam(value = "robotId", required = true) Long robotId, HttpServletRequest request) {
        Result result = new Result();
        try {
            int re = tRobotInfoService.deleteByPrimaryId(robotId, request);
            if(re == -1){
                result.setCode(209,"此机器人下存在测点或巡视点");
            }else {
                result.setData(re);
                //有变动 同步模型
                Constant.modelUpload("1001");
                Constant.modelUpload(String.valueOf(re));
            }
            //result.setData(tRobotInfoService.deleteByPrimaryId(robotId));
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
    @PostMapping(value = "/update")
    @Logs(title = "修改机器人信息",content = "根据用户传递的参数修改机器人信息",logType = 3,authority = "1234", codeName="droneType")
    public Result update(HttpServletRequest request,@Validated @RequestBody TRobotInfo tRobotInfo) {
        Result result = new Result();
        try {
            TRobotInfo robotInfoTemp = tRobotInfoService.selectByPrimaryId(tRobotInfo.getRobotId());
            List<String> allRobotCodeList = tRobotInfoService.selectAllRobotCode2();
            if (!Objects.equals(robotInfoTemp.getRobotCode(), tRobotInfo.getRobotCode()) && allRobotCodeList.contains(tRobotInfo.getRobotCode())){
                result.setMessage(209, "该实物ID已存在，不可重复");
                return result;
            }
            List<String> allRobotNumList = tRobotInfoService.selectAllRobotNum();
            if (!Objects.equals(robotInfoTemp.getRobotNum(), tRobotInfo.getRobotNum()) && allRobotNumList.contains(tRobotInfo.getRobotNum())){
                result.setMessage(209, "该设备编码已存在，不可重复");
                return result;
            }
            Long userId = Long.valueOf(request.getHeader("userId"));

            if (tRobotInfo.getRobotIp() != null && !"".equals(tRobotInfo.getRobotIp())) {
                String robotIp = tRobotInfo.getRobotIp();
                if (!robotIp.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                    result.setCode(ResultCodeEnum.CODE10104.getCode(), ResultCodeEnum.CODE10104.getName());
                }
            }
            Integer robotPort = tRobotInfo.getRobotPort();
            if (Objects.nonNull(robotPort)) {
                if (robotPort < 1 || robotPort > 65535) {
                    result.setCode(ResultCodeEnum.CODE10105.getCode(), ResultCodeEnum.CODE10105.getName());
                }
            }
            //设置为-1,用于清空前端传null值
            if(tRobotInfo.getRecordId() == null) {
                tRobotInfo.setRecordId(-1L);
            }

            result.setData(tRobotInfoService.update(tRobotInfo,userId));
            //有变动 同步模型
            String modelType = tRobotInfo.getDroneType() != null ? "5" : "3";
            Constant.modelUpload("1001");
            Constant.modelUpload(modelType);

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
    @GetMapping(value = "/selectByPrimaryId")
    @Logs(title = "查询巡检设备信息",content = "根据用户传递的参数查询巡检设备信息",logType = 1,authority = "1234")
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
    @GetMapping(value = "/select")
    @Logs(title = "查询巡检设备信息",content = "根据用户传递的参数查询巡检设备信息",logType = 1)
    public Result select(@RequestParam(value = "robotId", required = false) Long robotId,
                         @RequestParam(value = "robotCode", required = false) String robotCode,
                         @RequestParam(value = "robotNum", required = false) String robotNum,
                         @RequestParam(value = "robotName", required = false) String robotName,
                         @RequestParam(value = "robotStatus", required = false) String robotStatus,
                         @RequestParam(value = "robotType", required = false) Integer robotType,
                         @RequestParam(value = "robotIp", required = false) String robotIp,
                         @RequestParam(value = "robotPort", required = false) Integer robotPort,
                         @RequestParam(value = "upRegionName", required = false) String upRegionName,
                         @RequestParam(value = "lightIp", required = false) String lightIp,
                         @RequestParam(value = "lightPort", required = false) String lightPort,
                         @RequestParam(value = "identityManager", required = false) String identityManager,
                         @RequestParam(value = "identityCode", required = false) String identityCode,
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
                         @RequestParam(value = "robotSource", required = false) String robotSource,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam(value = "buildingUser", required = false) String buildingUser,
                         @RequestParam(value = "appearanceNumber", required = false) String appearanceNumber,
                         @RequestParam(value = "defectRecord", required = false) String defectRecord,
                         @RequestParam(value = "repairRecord", required = false) String repairRecord,
                         @RequestParam(value = "exitPutIntoRecord", required = false) String exitPutIntoRecord,
                         @RequestParam(value = "remarks", required = false) String remarks) {
        Result result = new Result();
        try {
            List<TRobotInfo> list = tRobotInfoService.select(robotId, robotCode, robotNum, robotName, robotStatus, robotType,
                    robotIp, robotPort, upRegionName, lightIp, lightPort, identityManager, identityCode, lnferadIp,
                    inferadPort, inferadUsername, inferadPassword, photePath, createBy, createDate, updateBy,
                    updateDate, robotFactory, isUse, commissionDate, upRegionId, robotPosition,robotSource,
                    address,buildingUser,appearanceNumber,defectRecord,repairRecord,exitPutIntoRecord,remarks);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页模糊查询")
    @GetMapping(value = "/selectByPage")
//    @Logs(title = "查询机器人信息",content = "根据用户传递的参数分页查询机器人信息",logType = 1,authority = "1234", codeName="type")
    public Result selectByPage(@RequestParam(value = "robotName", required = false) String robotName,
                               @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                               @RequestParam(value = "buildingUser", required = false) String buildingUser,
                               @RequestParam(value = "robotFactory", required = false) Integer robotFactory,
                               @RequestParam(value = "robotType", required = false) Integer robotType,
                               @RequestParam(value = "droneType", required = false) Integer droneType,
                               @RequestParam(value = "robotPosition", required = false) Integer robotPosition,
                               @RequestParam(value = "robotSource", required = false) String robotSource,
                               @RequestParam(value = "isUse", required = false) Integer isUse,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "type", required = false) Integer type,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String deviceTypeName = (type != null && type == 2) ? "无人机" : "机器人";
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出",deviceTypeName + "台账信息导出");
            }else{
                logsRecord.LogsSend(request,"1","查询" + deviceTypeName + "台账信息","根据用户传递的参数分页查询" + deviceTypeName + "台账信息");
            }
//            List<Long> upRegionIds = tStdDeviceService.selectRegionIdTree(tRobotInfo.getUpRegionId());
            List<Long> regionIdList =  tStdRegionDao.selectDownId(upRegionId);
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TRobotInfo> list = tRobotInfoService.selectByPage(robotPosition,robotName,buildingUser, robotFactory,robotType,droneType,robotSource,isUse,address,type,regionIdList);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "从PMS系统同步机器人信息")
    @GetMapping(value = "/synchronizeFromPMS")
    @Logs(title = "从PMS系统同步巡检设备信息",content = "从pms系统同步巡检设备信息",logType = 5,authority = "1234")
    public Result synchronizeFromPMS(@RequestParam(value = "robotCode") String robotCode,HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            result.setData(tRobotInfoService.synchronizeFromPMS(robotCode,userId));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("从PMS系统同步巡检设备信息失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "批量插入")
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入巡检设备信息",content = "根据用户传递的参数批量插入巡检设备信息",logType = 2)
    public Result batchInsert(@RequestBody List<TRobotInfo> list) {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询所有机器人巡检点信息树 inspectionType: 1-巡检点 2-操作点")
    @GetMapping(value = "/selectInspectionTree")
    @Logs(title = "查询所有机器人巡检点信息树",content = "根据用户传递的参数查询机器人巡检点树",logType = 1,authority = "1234", codeName="type")
    public Result selectInspectionTree(@RequestParam(value = "inspectionType", required = false) Integer inspectionType,
                                       @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                                       @RequestParam(value = "type", required = true) Integer type) {
        Result result = new Result();
        try {
            List<Map<String, Object>> robotInspectionTree = tRobotInfoService.selectInspectionTree(inspectionType, upRegionId, type);
            result.setData(robotInspectionTree);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询所有机器人巡检点信息树失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @PostMapping(value = "/batchDelete")
    @Logs(title = "批量删除巡检设备信息",content = "根据用户传递的参数批量删除巡检设备信息",logType = 4)
    public Result batchDelete(@RequestParam(value = "robotIds", required = true) String robotIds) {
        Result result = new Result();
        try {
            int re = tRobotInfoService.batchDelete(robotIds);
            if(re == -1){
                result.setCode(209,"此机器人下存在测点或巡视点");
                return result;
            }
            result.setData(re);
            //有变动 同步模型
            Constant.modelUpload("1001");
            Constant.modelUpload("3");
            //result.setData(tRobotInfoService.batchDelete(robotIds));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "从PMS系统同步巡检设备台账信息2")
    @GetMapping(value = "/synchronizeFromPMS2")
    @Logs(title = "从PMS系统同步机器人台账信息",content = "从pms系统同步巡检设备台账信息",logType = 5,authority = "1234")
    public Result synchronizeFromPMS2() {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.synchronizeFromPMS2());
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("发生异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发生错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "读取从PMS系统获取的文件再生成xml")
    @GetMapping(value = "/generateXMLByFile")
    @Logs(title = "读取从PMS系统获取的文件再生成xml",content = "读取pms系统的文件并生成xml",logType = 5)
    public Result generateXMLByFile() {
        Result result = new Result();
        try {
            result.setData(tRobotInfoService.generateXMLByFile());
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("发生异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发生错误:", e);
        }
        return result;
    }
}
