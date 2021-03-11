package com.yjh.platform.module.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.entity.TCruiseTaskDel;
import com.yjh.platform.module.task.service.TCruiseTaskDelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tt
 * @since 2020-09-14
 */
@RestController
@RequestMapping("/tCruiseTaskDel/v1")
@Api(value = "/tCruiseTaskDel", description = "周期任务删除记录表操作接口")
public class TCruiseTaskDelController {

    @Autowired
    private final TCruiseTaskDelService tCruiseTaskDelService;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskDelController.class);

    public TCruiseTaskDelController(TCruiseTaskDelService tCruiseTaskDelService) {
        this.tCruiseTaskDelService = tCruiseTaskDelService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增周期任务删除记录",content = "根据用户传递的参数新增周期任务删除记录数据",logType = 2)
    public Result insert(@Validated @RequestBody TCruiseTaskDel tCruiseTaskDel) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskDelService.insert(tCruiseTaskDel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加周期任务删除记录错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除周期任务删除记录",content = "根据用户传递的参数删除周期任务删除记录数据",logType = 4)
    public Result delete(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskDelService.deleteByPrimaryId(taskId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除周期任务删除记录异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改周期任务删除记录",content = "根据用户传递的参数修改周期任务删除记录数据",logType = 3)
    public Result update(@RequestBody TCruiseTaskDel tCruiseTaskDel) {
        Result result = new Result();
        try {
            result.setData(tCruiseTaskDelService.update(tCruiseTaskDel));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新周期任务删除记录异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询周期任务删除记录",content = "根据用户传递的参数查询周期任务删除记录信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "taskId", required = true) String taskId) {
        Result result = new Result();
        try {
            TCruiseTaskDel tCruiseTaskDel = tCruiseTaskDelService.selectByPrimaryId(taskId);
            result.setData(tCruiseTaskDel);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询周期任务删除记录",content = "根据用户传递的参数查询周期任务删除记录信息",logType = 1)
    public Result select(@RequestParam(value = "taskId", required = false) String taskId,
                         @RequestParam(value = "delTime", required = false) Date delTime,
                         @RequestParam(value = "createTime", required = false) Date createTime) {
        Result result = new Result();
        try {
            List<TCruiseTaskDel> list = tCruiseTaskDelService.select(taskId, delTime, createTime);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询周期任务删除记录",content = "根据用户传递的参数分页查询周期任务删除记录信息",logType = 1)
    public Result selectByPage(@RequestBody TCruiseTaskDel tCruiseTaskDel
                            ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCruiseTaskDel.getPageNum()!=null?tCruiseTaskDel.getPageNum():1, tCruiseTaskDel.getPageSize()!=null?tCruiseTaskDel.getPageSize():0,true,null,true);
            List<TCruiseTaskDel> list = tCruiseTaskDelService.selectByPage(tCruiseTaskDel);
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
    @Logs(title = "批量插入周期任务删除记录",content = "根据用户传递的参数批量插入周期任务删除记录数据",logType = 2)
    public Result batchInsert(@RequestBody List<TCruiseTaskDel> list) {
        Result result = new Result();
        try {
        result.setData(tCruiseTaskDelService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

}
