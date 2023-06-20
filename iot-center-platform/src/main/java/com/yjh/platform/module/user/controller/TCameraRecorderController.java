package com.yjh.platform.module.user.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CameraRecordModelExcelListener;
import com.yjh.platform.module.user.entity.TCameraRecorder;
import com.yjh.platform.module.user.entity.TCameraRecorderByDict;
import com.yjh.platform.module.user.entity.TCameraRecorderExcel;
import com.yjh.platform.module.user.service.TCameraRecorderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
    private LogsRecord logsRecord;
    @Autowired
    private final TCameraRecorderService tCameraRecorderService;

    private Logger log = LoggerFactory.getLogger(TCameraRecorderController.class);

    public TCameraRecorderController(TCameraRecorderService tCameraRecorderService) {
        this.tCameraRecorderService = tCameraRecorderService;
    }
    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增录像服务器信息",content = "根据用户传递的参数新增录像服务器信息",logType = 2,authority = "1234")

    public Result insert( @Validated @RequestBody TCameraRecorder tCameraRecorder) {

        Result result = new Result();
        try {
            tCameraRecorder.setEdgeCode((String)redisTemplate.opsForHash().get(Constant.T_SYS_PARAM+"edgeCode","content"));
            List<String> selectAllPMSIdList = tCameraRecorderService.selectAllPMSId();
            if (StringUtils.hasLength(tCameraRecorder.getPmsId()) &&  selectAllPMSIdList.contains(tCameraRecorder.getPmsId())) {
                result.setMessage(209, "PMS编码已存在，不可重复");
            } else {
                result.setData(tCameraRecorderService.insert(tCameraRecorder));
                sendPostRequest(Constant.NVR_REGISTER_URL,tCameraRecorder.getRecordId());
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("1002");
                }
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除录像服务器信息",content = "根据用户传递的参数删除录像服务器信息",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "recordId", required = true) Long recordId) {
        Result result = new Result();
        try {
            int re  = tCameraRecorderService.deleteByPrimaryId(recordId);
            if(re == -1){
                result.setCode(209,"此录像服务器下存在摄像机");
            }else {
                result.setData(re);
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("1002");
                }
            }
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
    @RequestMapping(value = "/deleteSelectedRecord", method = RequestMethod.POST)
    @Logs(title = "批量删除录像服务器信息",content = "根据用户传递的参数批量删除录像服务器信息",logType = 4,authority = "1234")
    public Result deleteSelectedRecord(@RequestParam(value = "recordIds", required = true) String recordIds) {
        Result result = new Result();
        try {
            int re  = tCameraRecorderService.deleteSelectedRecord(recordIds);
            if(re == -1){
                result.setCode(209,"录像服务器下存在摄像机");
            }else {
                result.setData(re);
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("1002");
                }
            }
            //result.setData(tCameraRecorderService.deleteSelectedRecord(recordIds));
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
    @Logs(title = "修改录像服务器信息",content = "根据用户传递的参数修改录像服务器信息",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TCameraRecorder tCameraRecorder) {
        Result result = new Result();
        try {
            String pmsId = tCameraRecorderService.selectPmsIdById(tCameraRecorder.getRecordId());
            List<String> allPmsIdList = tCameraRecorderService.selectAllPMSId();
            if (StringUtils.hasLength(tCameraRecorder.getPmsId()) && !tCameraRecorder.getPmsId().equals(pmsId) && allPmsIdList.contains(tCameraRecorder.getPmsId())) {
                result.setMessage(209, "PMS编码已存在，不可重复");
            } else {
                log.info("tCameraRecorderName: "+tCameraRecorder.getRecordName());
                result.setData(tCameraRecorderService.update(tCameraRecorder));
                sendPostRequest(Constant.NVR_REGISTER_URL, tCameraRecorder.getRecordId());
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("1002");
                }
            }
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
    @Logs(title = "查询录像服务器信息",content = "根据用户传递的参数查询录像服务器信息",logType = 1,authority = "1234")
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
    @Logs(title = "查询录像服务器信息",content = "根据用户传递的参数查询录像服务器信息",logType = 1)
    public Result select(@RequestParam(value = "recordId", required = false) Long recordId,
                            @RequestParam(value = "recordName", required = false) String recordName,
                         @RequestParam(value = "recorderModel", required = false) Integer recorderModel,
                         @RequestParam(value = "recorderType", required = false) String recorderType,
                         @RequestParam(value = "vendorId", required = false) Integer vendorId,
                         @RequestParam(value = "pmsId", required = false) String pmsId,
                         @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "recordIp", required = false) String recordIp,
                            @RequestParam(value = "protocol", required = false) String protocol,
                            @RequestParam(value = "httpPort", required = false) Integer httpPort,
                            @RequestParam(value = "transPort", required = false) Integer transPort,
                            @RequestParam(value = "rtspPort", required = false) Integer rtspPort,
                            @RequestParam(value = "identityManager", required = false) String identityManager,
                            @RequestParam(value = "identityCode", required = false) String identityCode,
                            @RequestParam(value = "protocolUrl", required = false) String protocolUrl,
                            @RequestParam(value = "maxChannel", required = false) Integer maxChannel,
                            @RequestParam(value = "hddSize", required = false) Integer hddSize,
                            @RequestParam(value = "bufferDay", required = false) Integer bufferDay,
                            @RequestParam(value = "timeLong", required = false) Integer timeLong,
                            @RequestParam(value = "unit", required = false) String unit) {
        Result result = new Result();
        try {
            List<TCameraRecorderByDict> list = tCameraRecorderService.select(recordId, recordName, recorderModel,recorderType, vendorId,pmsId,aliasName, recordIp, protocol, httpPort, transPort, rtspPort, identityManager, identityCode, protocolUrl, maxChannel, hddSize, bufferDay, timeLong,unit);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
//    @Logs(title = "查询录像服务器信息",content = "根据用户传递的参数分页查询录像服务器信息",logType = 1,authority = "1234")
    public Result selectByPage(@RequestParam(value = "aliasName", required = false) String aliasName,
                               @RequestParam(value = "unit", required = false) String unit,
                               @RequestParam(value = "vendorId", required = false) Integer vendorId,
                               @RequestParam(value = "recorderModel", required = false) Integer recorderModel,
                               @RequestParam(value = "recordType", required = false) Integer recordType,
                               @RequestParam(value = "recordName", required = false) String recordName,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出台账信息","录像机台账导出");
            }else{
                logsRecord.LogsSend(request,"1","查询录像服务器信息","根据用户传递的参数分页查询录像服务器信息");
            }
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraRecorderByDict> list = tCameraRecorderService.selectByPage(recordType,aliasName,unit,vendorId,recorderModel,recordName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "从PMS系统同步录像机信息")
    @RequestMapping(value = "/synchronizeFromPMS", method = RequestMethod.GET)
    @Logs(title = "从PMS系统同步录像机信息",content = "从pms系统同步录像服务器信息",logType = 5,authority = "1234")
    public Result synchronizeFromPMS(@RequestParam(value = "pmsId",required = false) String pmsId) {
        Result result = new Result();
        try {
            result.setData(tCameraRecorderService.synchronizeFromPMS(pmsId));
        } catch (Exception e) {
            if (!StringUtils.hasLength(pmsId)){
                result.setCode(209,"PMS编码为空,请先添加PMS编码");
                result.setData(false);
            }else {
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                log.error("从PMS系统同步录像机信息失败描述：" + e);
            }
        }
        return result;
    }
    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入录像服务器信息",content = "根据用户传递的参数批量插入录像服务器信息",logType = 2)
    public Result batchInsert( @RequestBody List<TCameraRecorder> list) {
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
    @Logs(title = "查询录像服务器信息",content = "根据用户传递的参数录像服务器信息",logType = 1)
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

    @ApiOperation(value = "导入NVR台账")
    @RequestMapping(value = "/importCameraRecorder", method = RequestMethod.POST)
    @Logs(title = "导入NVR台账",content = "根据用户传递的参数导入NVR台账",logType = 5)
    public Result importCameraRecorder(MultipartFile file) {
        Result result = new Result();
        try {
            InputStream inputStream = file.getInputStream();
            CameraRecordModelExcelListener cameraRecordModelExcelListener = new CameraRecordModelExcelListener();
            ReadSheet readSheet = new ReadSheet(0);
            EasyExcelFactory.read(inputStream, TCameraRecorderExcel.class,cameraRecordModelExcelListener).headRowNumber(1).build().read(readSheet);
            List<TCameraRecorderExcel> excelEntities = cameraRecordModelExcelListener.getExcelEntities();
            if (CollectionUtils.isNotEmpty(excelEntities)) {
                tCameraRecorderService.importCameraRecorder(excelEntities);
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("导入NVR台账失败：" + e);
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
