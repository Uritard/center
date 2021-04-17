package com.yjh.platform.module.user.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.user.entity.VideoIntercom;
import com.yjh.platform.module.user.service.VideoIntercomService;
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
 * @author prozac.G
 */
@RestController
@RequestMapping("/videoIntercom/v1")
@Api(value = "/videoIntercom", description = "可视对讲机信息表操作接口")
public class VideoIntercomContrller {
    private Logger log = LoggerFactory.getLogger(VideoIntercomContrller.class);

    @Autowired
    private VideoIntercomService videoIntercomService;
    @Autowired
    private TStdRegionDao tStdRegionDao;


    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增可视对讲信息",content = "根据用户传递的参数新增可视信息",logType = 2)
    public Result insert(@Validated @RequestBody VideoIntercom videoIntercom) {
        Result result = new Result();
        try {
            int res= videoIntercomService.insert(videoIntercom);
            log.info("新增res:"+res);
            if (res == 0) {
                result.setMessage(ResultCodeEnum.CODE2.getCode(), ResultCodeEnum.CODE2.getName());
            } else {
                result.setData(res);
            }
        }catch (Exception e)
        {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除信息",content = "根据用户传递的参数删除信息",logType = 4)
    public Result delete(@RequestParam(value = "videoIntercomId", required = true) Long videoIntercomId) {
        Result result = new Result();
        try {
            int re = videoIntercomService.deleteByPrimaryId(videoIntercomId);
            log.info("删除re:"+re);
            if(re == 0){
                result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            }else{
                result.setData(re);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/deleteVideoIntercom", method = RequestMethod.DELETE)
    @Logs(title = "批量删除信息",content = "根据用户传递的参数批量删除信息",logType = 4)
    public Result deleteVideoIntercom(@RequestParam(value = "videoIntercomIdS", required = true) String videoIntercomIdS) {
        Result result = new Result();
        try {
            int re = videoIntercomService.deleteVideoIntercom(videoIntercomIdS);
            if(re == -1){
                result.setCode(ResultCodeEnum.DELETEERROR.getCode(), ResultCodeEnum.DELETEERROR.getName());
            }else{
                result.setData(re);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改信息",content = "根据用户传递的参数修改信息",logType = 3)
    public Result update(@RequestBody VideoIntercom videoIntercom) {
        Result result = new Result();
        try {
            int re = videoIntercomService.update(videoIntercom);
            log.info("修改re:"+re);
            if(re == 0){
                result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());

            }else{
                result.setData(re);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询信息",content = "根据用户传递的参数查询信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "videoIntercomId", required = true) Long videoIntercomId) {
        Result result = new Result();
        try {
            VideoIntercom videoIntercom = videoIntercomService.selectByPrimaryId(videoIntercomId);
            result.setData(videoIntercom);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据间隔id查询所有信息")
    @RequestMapping(value = "/selectByRegionId", method = RequestMethod.GET)
    @Logs(title = "查询信息",content = "根据用户传递的参数查询信息",logType = 1)
    public Result selectByRegionId(@RequestParam(value = "regionId", required = false) Long regionId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<VideoIntercom> list = videoIntercomService.selectByRegionId(regionId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }



    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询信息",content = "根据用户传递的参数分页查询信息",logType = 1,authority = "1235")
    public Result selectByPage(@RequestParam(value = "cameraName", required = false) String cameraName,
                               @RequestParam(value = "regionId", required = false) Long regionId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            List<Long> regionIdList =  tStdRegionDao.selectDownId(regionId);
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<VideoIntercom> list = videoIntercomService.selectByPage(cameraName,regionIdList);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


}
