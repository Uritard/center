package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.entity.TCameraInfoByDict;
import com.yjh.platform.module.user.service.TCameraInfoService;
import com.yjh.platform.module.user.entity.TCameraInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.web.multipart.MultipartFile;


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

    private Logger log = LoggerFactory.getLogger(TCameraInfoController.class);

    public TCameraInfoController(TCameraInfoService tCameraInfoService) {
        this.tCameraInfoService = tCameraInfoService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result insert(@RequestBody TCameraInfo tCameraInfo) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.insert(tCameraInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增相机错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "cameraId", required = true) Long cameraId) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.deleteByPrimaryId(cameraId));
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
    @RequestMapping(value = "/deleteSelectedCamera", method = RequestMethod.DELETE)
    public Result deleteSelectedCamera(@RequestParam(value = "cameraIds[]", required = true) String[] cameraIds) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.deleteSelectedCamera(cameraIds));
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
    public Result update(@RequestBody TCameraInfo tCameraInfo) {
        Result result = new Result();
        try {
            result.setData(tCameraInfoService.update(tCameraInfo));
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
    public Result selectByRegionId(@RequestParam(value = "regionId", required = false) Long regionId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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
    public Result selectByCameraName(@RequestParam(value = "cameraName", required = false) String cameraName,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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
    public Result select(@RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "cameraName", required = false) String cameraName,
                         @RequestParam(value = "aliasName", required = false) String aliasName,
                         @RequestParam(value = "recordId", required = false) String recordId,
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                         @RequestParam(value = "channelNum", required = false) Integer channelNum,
                         @RequestParam(value = "smsId", required = false) Integer smsId,
                         @RequestParam(value = "rmsId", required = false) Integer rmsId,
                         @RequestParam(value = "vendorId", required = false) Integer vendorId,
                         @RequestParam(value = "streamType", required = false) Integer streamType,
                         @RequestParam(value = "protocolType", required = false) Integer protocolType,
                         @RequestParam(value = "url", required = false) String url,
                         @RequestParam(value = "port", required = false) Integer port,
                         @RequestParam(value = "cameraType", required = false) Integer cameraType,
                         @RequestParam(value = "isControl", required = false) Integer isControl) {
        Result result = new Result();
        try {
            List<TCameraInfoByDict> list = tCameraInfoService.select(cameraId, cameraName, aliasName, recordId, upRegionId, channelNum, smsId, rmsId, vendorId, streamType, protocolType, url, port, cameraType, isControl);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody TCameraInfo tCameraInfo,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TCameraInfoByDict> list = tCameraInfoService.selectByPage(tCameraInfo);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    /*@ApiOperation(value = "导入", notes = "导入")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result importExcel(@RequestParam("excelFile") MultipartFile excelFile) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(excelFile.getInputStream());//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始
            String deviceId = null;
            List<TCameraInfo> metes = new ArrayList<>();
            StringBuffer errMsg = new StringBuffer();
            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                *//*以字符串的方式获取第i+1行的第二列的值*//*
                deviceId = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? deviceId : row.getCell(1).getStringCellValue();
                TCameraInfo devicemete = new TCameraInfo();
                devicemete.setMeteId(row.getCell(4).getStringCellValue());
                devicemete.setDeviceId(deviceId);
                if (row.getCell(6) == null || row.getCell(6).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(6).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(6).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不能为空<br>");
                    continue;
                }
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    devicemete.setRawMeteType(ExcelPoiUtil.getValue(row.getCell(6)));
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    continue;
                }
                if (row.getCell(7) == null || row.getCell(7).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(7).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(7).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,系数不能为空<br>");
                    continue;
                }
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    devicemete.setRemark(ExcelPoiUtil.getValue(row.getCell(7)));
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "H列,系数不是数值类型<br>");
                    continue;
                }
                metes.add(devicemete);
            }
            deviceService.batchUpdateMete(metes);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            logger.error("导入失败：" + e);
        }
        return result;
    }*/



}
