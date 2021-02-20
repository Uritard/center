package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.ModelCreator;
import com.yjh.platform.module.device.entity.ModelInfo;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import com.yjh.platform.module.device.service.TStdMetemodelService;
import com.yjh.platform.module.device.entity.TStdMeteModel;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author tt
 * @since 2020-08-07
 */
@RestController
@RequestMapping("/tStdMeteModel/v1")
@Api(value = "/tStdMeteModel", description = "系统测点模版表操作接口")
public class TStdMetemodelController {

    @Autowired
    private final TStdMetemodelService tStdMetemodelService;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelController.class);

    public TStdMetemodelController(TStdMetemodelService tStdMetemodelService) {
        this.tStdMetemodelService = tStdMetemodelService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增系统测点模版",content = "根据用户传递的参数新增系统测点模版",logType = 2)
    public Result add(@Validated  @RequestBody TStdMeteModel tStdMeteModel) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.add(tStdMeteModel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除系统测点模版",content = "根据用户传递的参数删除系统测点模版",logType = 4)
    public Result delete(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.deleteByPrimaryId(modelId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点模版删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改系统测点模版",content = "根据用户传递的参数修改系统测点模版",logType = 3)
    public Result update(@RequestBody TStdMeteModel tStdMeteModel) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.update(tStdMeteModel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("系统测点模版更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询系统测点模版",content = "根据用户传递的参数查询系统测点模版",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "modelId") Long modelId) {
        Result result = new Result();
        try {
            TStdMeteModel tStdMeteModel = tStdMetemodelService.selectByPrimaryId(modelId);
            result.setData(tStdMeteModel);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询系统测点模版",content = "根据用户传递的参数查询系统测点模版",logType = 1)
    public Result select(@RequestParam(value = "modelId", required = false) Long modelId,
                            @RequestParam(value = "modelName", required = false) String modelName,
                            @RequestParam(value = "deviceType", required = false) Integer deviceType,
                            @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {
            List<TStdMeteModel> list = tStdMetemodelService.select(modelId, modelName, deviceType, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询系统测点模版",content = "根据用户传递的参数分页查询系统测点模版",logType = 1)
    public Result selectByPage(@RequestBody TStdMeteModel tStdMeteModel,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TStdMeteModel> list = tStdMetemodelService.selectByPage(tStdMeteModel);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入系统测点模版",content = "根据用户传递的参数批量插入系统测点模版",logType = 2)
    public Result batchAdd(@Validated @RequestBody List<TStdMeteModel> list) {
        Result result = new Result();
        try {
        result.setData(tStdMetemodelService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("系统测点模版批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据模版的设备类型查询对应的初始测点信息")
    @RequestMapping(value = "/selectMeteByDeviceType", method = RequestMethod.GET)
    @Logs(title = "根据模版的设备类型查询对应的初始测点信息",content = "根据模版的设备类型查询对应的初始测点信息",logType = 1)
    public Result selectMeteByDeviceType(@RequestParam(value = "deviceType") Integer deviceType) {
        Result result = new Result();
        try {
            List<TStdMeteModelDetail> list = tStdMetemodelService.selectMeteByDeviceType(deviceType);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询对应的初始测点信息失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入模版测点")
    @RequestMapping(value = "/batchAddModelMete", method = RequestMethod.POST)
    @Logs(title = "批量插入模版测点",content = "根据用户传递的参数批量插入模板测点",logType = 2)
    public Result batchAddModelMete(@RequestBody List<TStdMeteModelDetail> list) {
        Result result = new Result();
        try {
            result.setData(tStdMetemodelService.batchAddModelMete(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入模版测点失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete",method = RequestMethod.DELETE)
    @Logs(title = "批量删除模版测点",content = "根据用户传递的参数批量删除数据",logType = 4)
    public Result batchDetele(@RequestParam(value = "list")List<String> list){
        Result result=new Result();
        try{
            result.setData(tStdMetemodelService.batchDelete(list));
        }catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("系统测点模版删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版删除错误:", e);
        }
        return result;
    }




    @ApiOperation(value = "查询设备类型-模板树")
    @RequestMapping(value = "/selectDeviceTypeModelTree",method = RequestMethod.GET)
    @Logs(title = "查询设备类型-模板树",content = "查询设备类型模板树",logType = 1)
    public Result selectDeviceTypeModelTree(@RequestParam(value = "deviceType",required = false)String deviceType){
        Result result=new Result();
        try{
            result.setData(tStdMetemodelService.selectDeviceTypeModelTree(deviceType));

        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败失败描述：", e);
        }

        return  result;
    }



    @ApiOperation(value = "新建模板")
    @RequestMapping(value = "/addModel",method = RequestMethod.POST)
    @Logs(title = "新建模板",content = "根据用户传递的参数新增数据",logType = 2)
    public Result addModel(@RequestBody ModelCreator modelCreator){
        Result result =new Result();
        try {
            result.setData(tStdMetemodelService.addModel(modelCreator));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统测点模版新建失败：" + e);
        }

        return result;
    }




   @ApiOperation(value = "查看当前模板信息")
   @RequestMapping(value = "/selectModel",method = RequestMethod.GET)
   @Logs(title = "查看当前模板信息",content = "查看当前模板信息",logType = 1)
    public Result selectModel(@RequestParam(value = "modelId")Long modelId){
        Result result=new Result();
       try{
           result.setData(tStdMetemodelService.selectModel(modelId));

       }catch (Exception e) {
           result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
           log.error("查询失败描述：", e);
       }
        return  result;

   }


    @ApiOperation(value = "修改当前模板信息")
    @RequestMapping(value = "updateModel",method = RequestMethod.PUT)
    @Logs(title = "修改当前模板信息",content = "根据用户传递的参数修改数据",logType = 3)
    public Result updateModel (@RequestBody ModelCreator modelCreator){
        Result result =new Result();
        try {
            result.setData(tStdMetemodelService.updateModel(modelCreator));
        }  catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("系统测点模版更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统测点模版更新错误:", e);
        }

        return  result;

    }


    @ApiOperation(value = "下载模板")
    @RequestMapping(value = "download",method = RequestMethod.GET)
    @Logs(title = "下载模板",content = "下载模板",logType = 5)
    public Result download () {
        Result result =new Result();
        try {
            result.setData(tStdMetemodelService.createModel());
        }  catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("生成模板异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("生成模板错误:", e);
        }

        return  result;
    }

    @ApiOperation(value = "导入模板")
    @RequestMapping(value = "upload",method = RequestMethod.POST)
    @Logs(title = "导入模板",content = "导入模板",logType = 5)
    public Result upload (@RequestParam(value="file", required=false) MultipartFile file) {
        Result result =new Result();
        try {
            result=tStdMetemodelService.insertModel(file);
        }  catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("生成模板异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("生成模板错误:", e);
        }

        return  result;
    }


}
