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
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.service.TCameraInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tt
 * @since 2020-07-23
 */
@RestController
@RequestMapping("/tCameraInfo/v1")
@Api(value = "/tCameraInfo", description = "摄像头信息表操作接口")
public class TCameraInfoController {

    @Autowired
    private final TCameraInfoService tCameraInfoService;
    @Autowired
    private TStdRegionDao tStdRegionDao;
    @Autowired
    private LogsRecord logsRecord;

    @Autowired
    private final TStdDeviceService tStdDeviceService;

    @Autowired
    private RedisTemplate redisTemplate;
    private Logger log = LoggerFactory.getLogger(TCameraInfoController.class);

    public TCameraInfoController(TCameraInfoService tCameraInfoService, TStdDeviceService tStdDeviceService) {
        this.tCameraInfoService = tCameraInfoService;
        this.tStdDeviceService = tStdDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增相机信息",content = "根据用户传递的参数新增相机信息",logType = 2,authority = "1234")
    public Result insert( @Validated @RequestBody  TCameraInfo tCameraInfo) {

        Result result = new Result();
        try {
            List<String> selectAllPMSIdList = tCameraInfoService.selectAllPMSId();
            if (StringUtils.hasLength(tCameraInfo.getPmsId()) && selectAllPMSIdList.contains(tCameraInfo.getPmsId())) {
                result.setMessage(209, "PMS编码已存在，不可重复");
            } else {
                tCameraInfo.setEdgeCode((String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM + "edgeCode", "content"));
                int state = tCameraInfoService.insert(tCameraInfo);
                if (state == 0) {
                    result.setMessage(ResultCodeEnum.CODE2.getCode(), ResultCodeEnum.CODE2.getName());
                } else {
                    result.setData(state);
                    //有变动 同步模型
                    Constant.modelUpload("1001");
                    Constant.modelUpload("1002");
                    Constant.modelUpload("4");
                }
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除相机信息",content = "根据用户传递的参数删除相机信息",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            int re = tCameraInfoService.deleteByPrimaryId(cameraId);
            if(re == -1){
                result.setCode(209,"此摄像机下存在预置位");
            }else if(re == -2){
                result.setCode(209,"诊断服务异常，删除失败");
            }else {
                result.setData(re);
                //有变动 同步模型
                Constant.modelUpload("1001");
                Constant.modelUpload("1002");
                Constant.modelUpload("4");
            }
            //result.setData(tCameraInfoService.deleteByPrimaryId(cameraId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除相机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/deleteSelectedCamera", method = RequestMethod.POST)
    @Logs(title = "批量删除相机信息",content = "根据用户传递的参数批量删除相机信息",logType = 4,authority = "1234")
    public Result deleteSelectedCamera(@RequestParam(value = "cameraIds", required = true) String cameraIds) {
        Result result = new Result();
        try {
            int re = tCameraInfoService.deleteSelectedCamera(cameraIds);
            if(re == -1){
                result.setCode(209,"摄像机下存在预置位");
            }else if(re == -2){
                result.setCode(209,"诊断服务异常，删除失败");
            }else {
                result.setData(re);
                //有变动 同步模型
                Constant.modelUpload("1001");
                Constant.modelUpload("1002");
                Constant.modelUpload("4");
            }
            //result.setData(tCameraInfoService.deleteSelectedCamera(cameraIds));
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改相机信息",content = "根据用户传递的参数修改相机信息",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TCameraInfo tCameraInfo) {
        Result result = new Result();
        try {
            String pmsId = tCameraInfoService.selectPmsIdById(tCameraInfo.getCameraId());
            List<String> allPmsIdList = tCameraInfoService.selectAllPMSId();
            if (StringUtils.hasLength(tCameraInfo.getPmsId()) && !tCameraInfo.getPmsId().equals(pmsId) && allPmsIdList.contains(tCameraInfo.getPmsId())) {
                result.setMessage(209, "PMS编码已存在，不可重复");
            } else {
                int state = tCameraInfoService.update(tCameraInfo);
                if (state == 0) {
                    result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
                } else {
                    tCameraInfoService.stopStream(tCameraInfo.getCameraId());
                    result.setData(state);
                    //有变动 同步模型
                    Constant.modelUpload("1001");
                    Constant.modelUpload("1002");
                    Constant.modelUpload("4");
                }
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新相机异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询相机信息",content = "根据用户传递的参数查询相机信息",logType = 1,authority = "1234")
    public Result selectByPrimaryId(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            TCameraInfoByDict tCameraInfoByDict = tCameraInfoService.selectByPrimaryId(cameraId);
            result.setData(tCameraInfoByDict);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据间隔id查询所有摄像机信息")
    @RequestMapping(value = "/selectByRegionId", method = RequestMethod.GET)
    @Logs(title = "查询相机信息",content = "根据用户传递的参数查询相机信息",logType = 1,authority = "1234")
    public Result selectByRegionId(@RequestParam(value = "regionId", required = false) Long regionId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByRegionId(regionId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据摄像机名称查询信息")
    @RequestMapping(value = "/selectByCameraName", method = RequestMethod.GET)
    @Logs(title = "查询相机信息",content = "根据用户传递的参数查询相机信息",logType = 1)
    public Result selectByCameraName(@RequestParam(value = "cameraName", required = false) String cameraName,
                                     @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByCameraName(cameraName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询相机信息",content = "根据用户传递的参数查询相机信息",logType = 1,authority = "1234")
    public Result select(@RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "cameraName", required = false) String cameraName,
                         @RequestParam(value = "cameraModel", required = false) Integer cameraModel,
                         @RequestParam(value = "pmsId", required = false) String pmsId,
                         @RequestParam(value = "aliasName", required = false) String aliasName,
                         @RequestParam(value = "recordId", required = false) String recordId,
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                         @RequestParam(value = "streamType", required = false) Integer streamType,
                         @RequestParam(value = "protocolType", required = false) Integer protocolType,
                         @RequestParam(value = "cameraIp", required = false) String cameraIp,
                         @RequestParam(value = "url", required = false) String url,
                         @RequestParam(value = "cameraType", required = false) Integer cameraType,
                         @RequestParam(value = "isControl", required = false) Integer isControl) {
        Result result = new Result();
        try {
            List<TCameraInfo> list = tCameraInfoService.select(cameraId, cameraName, cameraModel, pmsId, aliasName, recordId, upRegionId, streamType, protocolType,
                    cameraIp, url, cameraType, isControl);
                    result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
//    @Logs(title = "查询相机信息",content = "根据用户传递的参数分页查询相机信息",logType = 1,authority = "1234")
    public Result selectByPage(@RequestParam(value = "aliasName", required = false) String aliasName,
                                @RequestParam(value = "unit", required = false) String unit,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "cameraVendor", required = false) String cameraVendor,
                               @RequestParam(value = "cameraType", required = false) Integer cameraType,
                               @RequestParam(value = "cameraModel", required = false) Integer cameraModel,
                               @RequestParam(value = "cameraName", required = false) String cameraName,
                               @RequestParam(value = "regionId", required = false) Long regionId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出台账信息","摄像机台账导出");
            }else{
                logsRecord.LogsSend(request,"1","查询相机信息","根据用户传递的参数分页查询相机信息");
            }
//            List<Long> upRegionIds = tStdDeviceService.selectRegionIdTree(tCameraInfo.getUpRegionId());
            List<Long> regionIdList =  tStdRegionDao.selectDownId(regionId);
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByPage(cameraType,aliasName,unit,address,cameraVendor,cameraModel,cameraName,regionIdList);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "从PMS系统同步摄像机信息")
    @RequestMapping(value = "/synchronizeFromPMS", method = RequestMethod.GET)
    @Logs(title = "从PMS系统同步摄像机信息",content = "从pms系统同步摄像机信息",logType = 5,authority = "1234")
    public Result synchronizeFromPMS(@RequestParam(value = "pmsId",required = false) String pmsId) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.synchronizeFromPMS(pmsId));
        } catch (Exception e) {
            if (!StringUtils.hasLength(pmsId)){
                result.setCode(209,"PMS编码为空,请先添加PMS编码");
                result.setData(false);
            }else {
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                log.error("从PMS系统同步摄像机信息失败描述：" + e);
            }
        }
        return result;
    }
    @ApiOperation(value = "B-查询一个任务下的摄像头信息")
    @RequestMapping(value = "/selectCameraByTaskId",method =RequestMethod.GET)
    @Logs(title = "查询一个任务下的摄像头信息",content = "根据用户传递的参数任务下的相机信息",logType = 1)
    public Result selectCameraByTaskId(@RequestParam(value = "taskId")Long taskId){
        Result result=new Result();
     try{
         result.setData(tCameraInfoService.selectCameraByTaskId(taskId));
     }catch (Exception e){
         result.setCode(ResultCodeEnum.UPDATEERROR.getCode(),ResultCodeEnum.UPDATEERROR.getName());
         log.error("失败描述",e);
     }
        return  result;
    }

    @ApiOperation(value = "查询所有摄像头预置位信息树")
    @RequestMapping(value = "/selectPresetTree", method = RequestMethod.GET)
    @Logs(title = "查询所有摄像头预置位信息树",content = "查询相机预置位树",logType = 1)
    public Result selectPresetTree() {
        Result result = new Result();
        try {
            List<Map<String, Object>> cameraPresetTree = tCameraInfoService.selectPresetTree();
            result.setData(cameraPresetTree);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("摄像头预置位信息树失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "写入缓存")
    @RequestMapping(value = "/intoRedis", method = RequestMethod.GET)
    public Result intoRedis(){
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.intoRedis());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("写入缓存失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询相机或者机器人相机信息")
    @RequestMapping(value = "/selectByCameraIdOrRobotId", method = RequestMethod.GET)
    @Logs(title = "查询相机或者机器人相机信息",content = "根据用户传递的参数查询相机信息",logType = 1)
    public Result selectByCameraIdOrRobotId(@RequestParam(value = "cameraId", required = true) Long cameraId,
                                            @RequestParam(value = "type", required = false) Integer type) {
        Result result = new Result();
        try {
                result.setData(this.tCameraInfoService.selectByCameraIdOrRobotId(cameraId,type));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
