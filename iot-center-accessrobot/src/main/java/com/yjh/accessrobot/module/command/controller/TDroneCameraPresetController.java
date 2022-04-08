package com.yjh.accessrobot.module.command.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.command.entity.TDroneCameraPreset;
import com.yjh.accessrobot.module.command.service.TDroneCameraPresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lqh
 * @since 2021-03-15
 */
@RestController
@RequestMapping("/tDroneCameraPreset/v1")
@Api(value = "/tDroneCameraPreset", tags = "无人机预位置表操作接口")
public class TDroneCameraPresetController {

    @Autowired
    private final TDroneCameraPresetService tDroneCameraPresetService;
    @Autowired
    private DroneController droneController;

    private Logger log = LoggerFactory.getLogger(TDroneCameraPresetController.class);

    public TDroneCameraPresetController(TDroneCameraPresetService tDroneCameraPresetService) {
        this.tDroneCameraPresetService = tDroneCameraPresetService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(HttpServletRequest request, @RequestBody TDroneCameraPreset tDroneCameraPreset) {
        Result result = new Result();
        try {
            result = (droneController.feignDroneControl(request, tDroneCameraPreset.getDroneCode(),"3","10", tDroneCameraPreset.getPresetNum().toString(),null,null,null,null,null));
            if(result != null  && result.getData() != null){
            Map<String,Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                log.info("object转map的东西==="+map);
                if(!"4".equals(map.get("code").toString())){
                    result.setCode(209,map.get("result").toString());
                    return  result;
                }
                int i = tDroneCameraPresetService.add(tDroneCameraPreset);
                if(i == -1){
                    result.setCode(209,"此点号已存在");
                    return result;
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
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(HttpServletRequest request,@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TDroneCameraPreset tDroneCameraPreset = tDroneCameraPresetService.selectByPrimaryId(presetId);
            result = (droneController.feignDroneControl(request,tDroneCameraPreset.getDroneCode(),"3","12", tDroneCameraPreset.getPresetNum().toString(),null,null,null,null,null));
            if(result != null && result.getData() != null){
                Map<String,Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                log.info("object转map的东西==="+map);
                if(!"4".equals(map.get("code").toString())){
                    result.setCode(209,map.get("result").toString());
                    return  result;
                }
                int i = tDroneCameraPresetService.deleteByPrimaryId(presetId);
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

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TDroneCameraPreset tDroneCameraPreset) {
        Result result = new Result();
        try {
            result.setData(tDroneCameraPresetService.update(tDroneCameraPreset));
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
    public Result selectByPrimaryId(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TDroneCameraPreset tDroneCameraPreset = tDroneCameraPresetService.selectByPrimaryId(presetId);
            result.setData(tDroneCameraPreset);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "presetId", required = false) Long presetId,
                            @RequestParam(value = "presetNum", required = false) Integer presetNum,
                            @RequestParam(value = "droneId", required = false) Long droneId,
                            @RequestParam(value = "droneCode", required = false) String droneCode,
                            @RequestParam(value = "presetName", required = false) String presetName,
                            @RequestParam(value = "creatorTime", required = false) Date creatorTime,
                            @RequestParam(value = "cameraType", required = false) Integer cameraType) {
        Result result = new Result();
        try {
            List<TDroneCameraPreset> list = tDroneCameraPresetService.select(presetId,presetNum, droneId, droneCode, presetName, creatorTime, cameraType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "presetNum", required = false) Integer presetNum,
                                @RequestParam(value = "presetName", required = false) String presetName,
                                @RequestParam(value = "cameraId", required = false) Long cameraId,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TDroneCameraPreset> list = tDroneCameraPresetService.selectByPage(presetNum,presetName, cameraId);
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
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TDroneCameraPreset> list) {
        Result result = new Result();
        try {
        result.setData(tDroneCameraPresetService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "presetIds") String presetIds) {
    Result result = new Result();
    try {
        result.setData(tDroneCameraPresetService.batchDelete(presetIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "无人机预置位调用")
    @RequestMapping(value = "/controlPreset", method = RequestMethod.GET)
    public Result controlPreset(HttpServletRequest request,@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TDroneCameraPreset tDroneCameraPreset = tDroneCameraPresetService.selectByPrimaryId(presetId);
            result = (droneController.feignDroneControl(request,tDroneCameraPreset.getDroneCode().toString(),"3","7",tDroneCameraPreset.getPresetNum().toString(),null,null,null,null,null));
            if(result != null  && result.getData() != null){
                Map<String,Object> map = JSONObject.parseObject(JSON.toJSONString(result.getData()));
                log.info("object转map的东西==="+map);
                if(!"4".equals(map.get("code").toString())){
                    result.setCode(209,map.get("result").toString());
                }
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("无人机预置位调用异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("无人机预置位调用错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "无人机预置位树")
    @RequestMapping(value = "/dronePresetTree", method = RequestMethod.GET)
    public Result dronePresetTree(@RequestParam(value = "droneId") Long droneId) {
        Result result = new Result();
        try {
            result.setData(this.tDroneCameraPresetService.dronePresetTree(droneId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }
}
