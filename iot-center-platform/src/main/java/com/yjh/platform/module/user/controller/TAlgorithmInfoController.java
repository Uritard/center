package com.yjh.platform.module.user.controller;

import com.mysql.jdbc.StringUtils;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.service.TAlgorithmInfoService;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.validation.BindingResult;
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


/**
 * @author tt
 * @since 2020-08-06
 */
@RestController
@RequestMapping("/tAlgorithmInfo/v1")
@Api(value = "/tAlgorithmInfo", description = "算法表操作接口")
public class TAlgorithmInfoController {

    @Autowired
    private final TAlgorithmInfoService tAlgorithmInfoService;

    private Logger log = LoggerFactory.getLogger(TAlgorithmInfoController.class);

    public TAlgorithmInfoController(TAlgorithmInfoService tAlgorithmInfoService) {
        this.tAlgorithmInfoService = tAlgorithmInfoService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增算法信息",content = "根据用户传递的参数新增算法信息",logType = 2)
    public Result insert( @Validated @RequestBody TAlgorithmInfo tAlgorithmInfo) {

        Result result = new Result();
        try {
            result.setData(tAlgorithmInfoService.insert(tAlgorithmInfo));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_algorithm_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(),"算法名称重复");
                result.setData("算法名称重复");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("算法添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除算法信息",content = "根据用户传递的参数删除算法信息",logType = 4)
    public Result delete(@RequestParam(value = "algorithmId", required = true) Long algorithmId) {
        Result result = new Result();
        try {
            int re  = tAlgorithmInfoService.deleteByPrimaryId(algorithmId);
            if(re == -1){
                result.setCode(209,"此算法已配置到测点");
            }else {
                result.setData(re);
            }
            //result.setData(tAlgorithmInfoService.deleteByPrimaryId(algorithmId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除算法异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除算法错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改算法信息",content = "根据用户传递的参数修改算法信息",logType = 3)
    public Result update(@RequestBody TAlgorithmInfo tAlgorithmInfo) {
        Result result = new Result();
        try {
            result.setData(tAlgorithmInfoService.update(tAlgorithmInfo));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新算法异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新算法错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询算法信息",content = "根据用户传递的参数查询算法信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "algorithmId", required = true) Long algorithmId) {
        Result result = new Result();
        try {
            TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoService.selectByPrimaryId(algorithmId);
            result.setData(tAlgorithmInfo);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询算法失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询算法信息",content = "根据用户传递的参数查询算法信息",logType = 1)
    public Result select(@RequestParam(value = "algorithmId", required = false) Long algorithmId,
                            @RequestParam(value = "algorithmName", required = false) String algorithmName,
                            @RequestParam(value = "aliasName", required = false) String aliasName,
                            @RequestParam(value = "describel", required = false) String describel,
                            @RequestParam(value = "algorithmCode", required = false) String algorithmCode,
                            @RequestParam(value = "analyseType", required = false) String analyseType,
                            @RequestParam(value = "isAi", required = false) Integer isAi) {
        Result result = new Result();
        try {
            List<TAlgorithmInfo> list = tAlgorithmInfoService.
                    select(algorithmId, algorithmName, aliasName, describel, algorithmCode, analyseType,isAi);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询算法信息",content = "根据用户传递的参数对应查询算法信息",logType = 1)
    public Result selectByPage(@RequestBody TAlgorithmInfo tAlgorithmInfo
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tAlgorithmInfo.getPageNum()!=null?tAlgorithmInfo.getPageNum():1, tAlgorithmInfo.getPageSize()!=null?tAlgorithmInfo.getPageSize():0,true,null,true);
            List<TAlgorithmInfo> list = tAlgorithmInfoService.selectByPage(tAlgorithmInfo);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("分页查询算法失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入算法信息",content = "根据用户传递的参数批量插入算法信息",logType = 2)
    public Result batchInsert(  @RequestBody List<TAlgorithmInfo> list) {
        Result result = new Result();
        try {
            
            int re  = tAlgorithmInfoService.batchInsert(list);
            if(re == -1){
                result.setCode(209,"算法已配置到测点");
            }else {
                result.setData(re);
            }
        //result.setData(tAlgorithmInfoService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入算法失败：" + e);
        }
        return result;
    }



}
